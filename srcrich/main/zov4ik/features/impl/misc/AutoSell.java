package im.zov4ik.features.impl.misc;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.features.module.setting.implement.TextSetting;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.client.managers.event.EventHandler;

import java.util.Locale;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoSell extends Module {

    public static AutoSell getInstance() {
        return Instance.get(AutoSell.class);
    }

    private static final int CANCEL_SLOT_47 = 47;
    private static final int CONFIRM_SLOT_FALLBACK = 25;

    final SliderSettings stackSizeSetting = new SliderSettings("Размер стака", "Сколько предметов класть в каждый слот sellgui")
            .setValue(1f).range(1, 64);
    final SliderSettings priceSetting = new SliderSettings("Цена", "Цена за лот")
            .setValue(399999f).range(1, 10000000);
    final SliderSettings actionDelaySetting = new SliderSettings("Задержка", "Задержка между действиями (мс)")
            .setValue(700f).range(100, 2000);
    final TextSetting itemIdSetting = new TextSetting("Item ID", "Идентификатор предмета для продажи (minecraft:enchanted_golden_apple)")
            .setText("minecraft:enchanted_golden_apple").setMin(0).setMax(96);
    final BooleanSetting restartOnBuy = new BooleanSetting("Перезапуск после покупки", "Перезапускать цикл при сообщении 'у вас купили'")
            .setValue(true);
    final SliderSettings relistIntervalSetting = new SliderSettings("Интервал переставления", "Каждые N секунд бот заново выставляет товары")
            .setValue(40f).range(5, 600);
    final BooleanSetting debugSetting = new BooleanSetting("Debug", "Логировать действия в чат")
            .setValue(true);

    private enum State {
        IDLE,
        SEND_AH,
        WAIT_AH,
        CLICK_47,
        WAIT_LISTINGS,
        CANCEL_LISTINGS,
        CLOSE_LISTINGS,
        SEND_SELLGUI,
        WAIT_SELLGUI,
        PLACE_ITEMS,
        CLICK_CONFIRM,
        WAIT_CONFIRMATION,
        CYCLE_END,
        WAIT_PURCHASE
    }

    private State state = State.IDLE;
    private long lastActionAt;
    private int ahSyncId = -1;
    private int sellguiSyncId = -1;
    private int listingsClicksLeft;
    private int confirmRetries;
    private boolean confirmSeen;
    private int expectedCursorAfterPickup = -1;
    private int slotsFilledInSession = 0;

    public AutoSell() {
        super("AutoSell", "Auto Sell", ModuleCategory.MISC);
        setup(priceSetting, stackSizeSetting, itemIdSetting, actionDelaySetting, relistIntervalSetting, restartOnBuy, debugSetting);
    }

    @Override
    public void activate() {
        super.activate();
        resetCycle();
        state = State.SEND_AH;
        lastActionAt = 0L;
        info("§aAutoSell включён. Цена=" + priceSetting.getInt() + " стак=" + stackSizeSetting.getInt());
    }

    @Override
    public void deactivate() {
        super.deactivate();
        state = State.IDLE;
        info("§cAutoSell выключен");
    }

    private void resetCycle() {
        ahSyncId = -1;
        sellguiSyncId = -1;
        listingsClicksLeft = 0;
        confirmRetries = 0;
        confirmSeen = false;
        expectedCursorAfterPickup = -1;
        slotsFilledInSession = 0;
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.player.networkHandler == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();
        long delay = actionDelaySetting.getInt();
        if (now - lastActionAt < delay) return;

        switch (state) {
            case IDLE:
                return;
            case WAIT_PURCHASE: {
                long intervalMs = relistIntervalSetting.getInt() * 1000L;
                if (now - lastActionAt >= intervalMs) {
                    info("§7Прошло " + relistIntervalSetting.getInt() + " сек — переставляю товары");
                    resetCycle();
                    state = State.SEND_AH;
                    lastActionAt = 0L;
                }
                return;
            }
            case SEND_AH: {
                mc.player.networkHandler.sendChatCommand("ah");
                state = State.WAIT_AH;
                stamp(now);
                break;
            }
            case WAIT_AH: {
                if (mc.currentScreen instanceof GenericContainerScreen s && !s.getScreenHandler().slots.isEmpty()) {
                    ahSyncId = s.getScreenHandler().syncId;
                    debug("AH открыт, слотов=" + s.getScreenHandler().slots.size() + " sync=" + ahSyncId);
                    state = State.CLICK_47;
                    stamp(now);
                } else if (now - lastActionAt > 5000) {
                    info("§cНе удалось открыть /ah");
                    finishCycle();
                }
                break;
            }
            case CLICK_47: {
                if (!(mc.currentScreen instanceof GenericContainerScreen s)) {
                    state = State.SEND_AH;
                    stamp(now);
                    break;
                }
                ScreenHandler ahH = s.getScreenHandler();
                int enderSlot = findEnderChestSlot(ahH);
                if (enderSlot < 0) {
                    info("§cЭндер-сундук не найден в /ah, иду сразу на sellgui");
                    if (mc.currentScreen != null) mc.player.closeHandledScreen();
                    state = State.SEND_SELLGUI;
                    stamp(now);
                    break;
                }
                debug("Клик эндер-сундук в слоте " + enderSlot);
                click(ahH.syncId, enderSlot, 0, SlotActionType.PICKUP);
                state = State.WAIT_LISTINGS;
                stamp(now);
                break;
            }
            case WAIT_LISTINGS: {
                if (mc.currentScreen instanceof GenericContainerScreen s && s.getScreenHandler().syncId != ahSyncId) {
                    listingsClicksLeft = countContainerItems(s.getScreenHandler());
                    state = State.CANCEL_LISTINGS;
                    stamp(now);
                } else if (now - lastActionAt > 4000) {
                    state = State.CLOSE_LISTINGS;
                    stamp(now);
                }
                break;
            }
            case CANCEL_LISTINGS: {
                if (!(mc.currentScreen instanceof GenericContainerScreen s)) {
                    state = State.SEND_SELLGUI;
                    stamp(now);
                    break;
                }
                if (listingsClicksLeft <= 0) {
                    state = State.CLOSE_LISTINGS;
                    stamp(now);
                    break;
                }
                ScreenHandler h = s.getScreenHandler();
                if (h.slots.isEmpty()) {
                    state = State.CLOSE_LISTINGS;
                    stamp(now);
                    break;
                }
                Slot first = h.slots.get(0);
                if (first == null || !first.hasStack() || first.inventory instanceof PlayerInventory) {
                    state = State.CLOSE_LISTINGS;
                    stamp(now);
                    break;
                }
                click(h.syncId, 0, 0, SlotActionType.PICKUP);
                listingsClicksLeft--;
                stamp(now);
                break;
            }
            case CLOSE_LISTINGS: {
                if (mc.currentScreen != null) mc.player.closeHandledScreen();
                state = State.SEND_SELLGUI;
                stamp(now);
                break;
            }
            case SEND_SELLGUI: {
                mc.player.networkHandler.sendChatCommand("ah sellgui " + priceSetting.getInt());
                state = State.WAIT_SELLGUI;
                stamp(now);
                break;
            }
            case WAIT_SELLGUI: {
                if (mc.currentScreen instanceof GenericContainerScreen s && s.getScreenHandler().slots.size() > CONFIRM_SLOT_FALLBACK) {
                    sellguiSyncId = s.getScreenHandler().syncId;
                    slotsFilledInSession = 0;
                    expectedCursorAfterPickup = -1;
                    state = State.PLACE_ITEMS;
                    stamp(now);
                } else if (now - lastActionAt > 5000) {
                    info("§cНе удалось открыть /ah sellgui");
                    finishCycle();
                }
                break;
            }
            case PLACE_ITEMS: {
                if (!(mc.currentScreen instanceof GenericContainerScreen s) || s.getScreenHandler().syncId != sellguiSyncId) {
                    state = State.SEND_SELLGUI;
                    stamp(now);
                    break;
                }
                placeItemsStep(s.getScreenHandler(), now);
                break;
            }
            case CLICK_CONFIRM: {
                if (!(mc.currentScreen instanceof GenericContainerScreen s) || s.getScreenHandler().syncId != sellguiSyncId) {
                    finishCycle();
                    break;
                }
                ScreenHandler h = s.getScreenHandler();
                if (!h.getCursorStack().isEmpty()) {
                    depositCursorBack(h);
                    stamp(now);
                    break;
                }
                int confirmSlot = findConfirmSlotId(h);
                if (confirmSlot < 0) {
                    info("§cНе найдена кнопка подтверждения (slime_ball)");
                    state = State.CYCLE_END;
                    stamp(now);
                    break;
                }
                confirmSeen = false;
                click(h.syncId, confirmSlot, 0, SlotActionType.PICKUP);
                state = State.WAIT_CONFIRMATION;
                confirmRetries++;
                stamp(now);
                break;
            }
            case WAIT_CONFIRMATION: {
                if (confirmSeen) {
                    info("§aУспешно выставлено");
                    state = State.CYCLE_END;
                    stamp(now);
                } else if (now - lastActionAt > 3500) {
                    if (confirmRetries < 2) {
                        info("§eНет подтверждения, ретрай " + confirmRetries);
                        state = State.CLICK_CONFIRM;
                        stamp(now);
                    } else {
                        info("§cПодтверждение не получено");
                        state = State.CYCLE_END;
                        stamp(now);
                    }
                }
                break;
            }
            case CYCLE_END: {
                if (mc.currentScreen != null) mc.player.closeHandledScreen();
                if (countItemsInInventory() >= stackSizeSetting.getInt()) {
                    info("§7В инвентаре ещё есть предметы — новый цикл");
                    resetCycle();
                    state = State.SEND_SELLGUI;
                    stamp(now);
                } else {
                    info("§7Ожидаю покупки...");
                    state = State.WAIT_PURCHASE;
                    stamp(now);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (e.getType() != PacketEvent.Type.RECEIVE) return;
        if (!(e.getPacket() instanceof GameMessageS2CPacket m)) return;
        String text = m.content().getString().toLowerCase(Locale.ROOT);
        if (text.contains("на продажу успешно выставлено")) {
            confirmSeen = true;
        }
        if (restartOnBuy.isValue() && text.contains("у вас купили") && state == State.WAIT_PURCHASE) {
            info("§aОбнаружена покупка — запускаю новый цикл");
            resetCycle();
            state = State.SEND_AH;
            lastActionAt = 0L;
        }
    }

    private void stamp(long now) {
        lastActionAt = now;
    }

    private void finishCycle() {
        if (mc.currentScreen != null) mc.player.closeHandledScreen();
        state = State.WAIT_PURCHASE;
        lastActionAt = System.currentTimeMillis();
    }

    private int countContainerItems(ScreenHandler h) {
        int n = 0;
        for (Slot slot : h.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            if (slot.hasStack()) n++;
        }
        return n;
    }

    private int countItemsInInventory() {
        if (mc.player == null) return 0;
        int n = 0;
        net.minecraft.item.Item target = resolveTargetItem();
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            if (target == null || s.getItem() == target) n += s.getCount();
        }
        return n;
    }

    private net.minecraft.item.Item resolveTargetItem() {
        String id = itemIdSetting.getText();
        if (id == null || id.isBlank()) return null;
        Identifier ident = Identifier.tryParse(id.trim());
        if (ident == null) return null;
        net.minecraft.item.Item item = Registries.ITEM.get(ident);
        return item == net.minecraft.item.Items.AIR ? null : item;
    }

    /**
     * Per-tick step: does exactly ONE atomic interaction (pickup, place, or deposit-back).
     * The outer tick gate (actionDelay) provides safe spacing between actions.
     * Refuses to act if cursor state diverges from expectations (safety against server-side mishandling).
     */
    private void placeItemsStep(ScreenHandler h, long now) {
        net.minecraft.item.Item target = resolveTargetItem();
        ItemStack cursor = h.getCursorStack();

        // SAFETY: if cursor has the wrong item, put it back before doing anything else
        if (target != null && !cursor.isEmpty() && cursor.getItem() != target) {
            debug("§cНа курсоре чужой предмет (" + Registries.ITEM.getId(cursor.getItem()) + "), возвращаю");
            depositCursorBack(h);
            stamp(now);
            return;
        }

        Slot emptyTarget = findEmptyPlacementSlot(h);
        if (emptyTarget == null) {
            debug("Все слоты sellgui заполнены (" + slotsFilledInSession + ") — иду подтверждать");
            if (!h.getCursorStack().isEmpty()) {
                depositCursorBack(h);
                stamp(now);
                return;
            }
            state = State.CLICK_CONFIRM;
            stamp(now);
            return;
        }

        // If cursor empty: pickup ONE source stack from inventory
        if (cursor.isEmpty()) {
            Slot src = findInventorySourceSlot(h, target);
            if (src == null) {
                debug("В инвентаре нет предметов — заполнено " + slotsFilledInSession + " слотов, к подтверждению");
                state = State.CLICK_CONFIRM;
                stamp(now);
                return;
            }
            int srcCount = src.getStack().getCount();
            debug("ЛКМ source-slot=" + src.id + " count=" + srcCount + " (заполнено " + slotsFilledInSession + ")");
            click(h.syncId, src.id, 0, SlotActionType.PICKUP);
            expectedCursorAfterPickup = srcCount;
            stamp(now);
            return;
        }

        // SAFETY: verify cursor count is what we expect after a pickup
        if (expectedCursorAfterPickup > 0 && cursor.getCount() != expectedCursorAfterPickup) {
            debug("§cНеожиданный курсор: " + cursor.getCount() + " вместо " + expectedCursorAfterPickup + " — стоп");
            info("§cAutoSell остановлен: курсор не сошёлся. Проверь инвентарь!");
            setState(false);
            return;
        }
        expectedCursorAfterPickup = -1;

        // Place ONE item into empty slot via right-click
        int before = cursor.getCount();
        debug("ПКМ в слот " + emptyTarget.id + " (курсор=" + before + ")");
        click(h.syncId, emptyTarget.id, 1, SlotActionType.PICKUP);
        int after = h.getCursorStack().getCount();
        int delta = before - after;
        debug("После ПКМ курсор=" + after + " (Δ=" + delta + ")");

        // SAFETY: refuse to continue if more than 1 item left the cursor (server may have eaten the stack)
        if (delta > 1) {
            info("§cAutoSell СТОП: сервер забрал " + delta + " предметов вместо 1!");
            setState(false);
            return;
        }
        // SAFETY: also stop if nothing happened — avoid infinite loop
        if (delta < 0) {
            info("§cAutoSell СТОП: курсор увеличился (Δ=" + delta + ")");
            setState(false);
            return;
        }

        if (delta == 1) {
            slotsFilledInSession++;
        }
        stamp(now);
    }

    /**
     * @return 1 if placed one stack, 0 if no empty placement slot left, -1 if not enough items.
     */
    private int tryPlaceOneStack(ScreenHandler h) {
        int stackSize = stackSizeSetting.getInt();
        if (stackSize <= 0) return 0;

        Slot emptyTarget = findEmptyPlacementSlot(h);
        if (emptyTarget == null) {
            debug("Нет пустых контейнер-слотов");
            return 0;
        }
        debug("Целевой пустой слот=" + emptyTarget.id);

        net.minecraft.item.Item target = resolveTargetItem();

        // Step 1: gather >= stackSize items on cursor by picking up from inventory sources
        int safety = 64;
        while (h.getCursorStack().getCount() < stackSize && safety-- > 0) {
            Slot source = findInventorySourceSlot(h, target);
            if (source == null) break;
            debug("PICKUP из инв-слота " + source.id + " count=" + source.getStack().getCount());
            click(h.syncId, source.id, 0, SlotActionType.PICKUP);
        }

        ItemStack cursor = h.getCursorStack();
        int cursorCount = cursor.getCount();
        debug("После сбора курсор=" + cursorCount + " нужно=" + stackSize);

        if (cursor.isEmpty() || cursorCount == 0) {
            debug("Курсор пуст — нет предметов");
            return -1;
        }

        // If we didn't collect enough total items, abort and put back
        if (cursorCount < stackSize) {
            debug("Недостаточно предметов: курсор=" + cursorCount + "/" + stackSize);
            depositCursorBack(h);
            return -1;
        }

        // Step 2: dump excess (cursorCount - stackSize) back into an empty inventory slot, 1 by 1
        int excess = cursorCount - stackSize;
        if (excess > 0) {
            Slot dump = findEmptyInventorySlot(h);
            if (dump == null) {
                // No empty inv slot — try matching slot (will merge cursor into it)
                Slot match = findInventorySourceSlot(h, cursor.getItem());
                if (match != null && match.getStack().getCount() < match.getStack().getMaxCount()) {
                    debug("Нет пустого инв-слота, мерджу остаток в слот " + match.id);
                    // Can't precisely split into a non-empty slot without overshoot; abort safely
                }
                debug("Не нашёл пустой инв-слот для сброса излишка " + excess);
                depositCursorBack(h);
                return 0;
            }
            debug("Сбрасываю " + excess + " шт. в инв-слот " + dump.id);
            for (int i = 0; i < excess; i++) {
                click(h.syncId, dump.id, 1, SlotActionType.PICKUP); // right-click: drop 1 from cursor
            }
            debug("После сброса курсор=" + h.getCursorStack().getCount());
        }

        // Step 3: single LEFT-click to deposit exact stack into the sellgui slot
        debug("LEFT-CLICK размещения в слот " + emptyTarget.id + " количество=" + h.getCursorStack().getCount());
        click(h.syncId, emptyTarget.id, 0, SlotActionType.PICKUP);

        // Step 4: any remainder back to inventory
        if (!h.getCursorStack().isEmpty()) {
            debug("Остаток на курсоре после размещения=" + h.getCursorStack().getCount() + ", возвращаю");
            depositCursorBack(h);
        }
        return 1;
    }

    private Slot findEmptyInventorySlot(ScreenHandler h) {
        for (Slot slot : h.slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            if (!slot.hasStack()) return slot;
        }
        return null;
    }

    private void debug(String text) {
        if (debugSetting.isValue()) info("§8" + text);
    }

    private Slot findInventorySourceSlot(ScreenHandler h, net.minecraft.item.Item target) {
        for (Slot slot : h.slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            ItemStack st = slot.getStack();
            if (st.isEmpty()) continue;
            if (target != null && st.getItem() != target) continue;
            return slot;
        }
        return null;
    }

    private Slot findEmptyPlacementSlot(ScreenHandler h) {
        for (Slot slot : h.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            if (!slot.hasStack()) return slot;
        }
        return null;
    }

    private int findEnderChestSlot(ScreenHandler h) {
        for (Slot slot : h.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            ItemStack st = slot.getStack();
            if (st.isEmpty()) continue;
            if (st.getItem() == Items.ENDER_CHEST) return slot.id;
        }
        return -1;
    }

    private int findConfirmSlotId(ScreenHandler h) {
        for (Slot slot : h.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            ItemStack st = slot.getStack();
            if (!st.isEmpty() && st.getItem() == Items.SLIME_BALL) return slot.id;
        }
        // Fallback: scan by display name containing "продаж" / "подтвер"
        for (Slot slot : h.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            ItemStack st = slot.getStack();
            if (st.isEmpty()) continue;
            String name = st.getName().getString().toLowerCase(Locale.ROOT);
            if (name.contains("продаж") || name.contains("подтвер") || name.contains("выстав")) return slot.id;
        }
        if (h.slots.size() > CONFIRM_SLOT_FALLBACK) return CONFIRM_SLOT_FALLBACK;
        return -1;
    }

    private void depositCursorBack(ScreenHandler h) {
        if (h.getCursorStack().isEmpty()) return;
        // Try to deposit into an empty inventory slot
        for (Slot slot : h.slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            if (!slot.hasStack()) {
                click(h.syncId, slot.id, 0, SlotActionType.PICKUP);
                return;
            }
        }
        // Fallback: merge into any matching inventory slot
        ItemStack cursor = h.getCursorStack();
        for (Slot slot : h.slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            ItemStack st = slot.getStack();
            if (!st.isEmpty() && st.getItem() == cursor.getItem()) {
                click(h.syncId, slot.id, 0, SlotActionType.PICKUP);
                return;
            }
        }
    }

    private void click(int syncId, int slotId, int button, SlotActionType action) {
        try {
            mc.interactionManager.clickSlot(syncId, slotId, button, action, mc.player);
        } catch (Exception ignored) {
        }
    }

    private void info(String text) {
        ChatMessage.brandmessage("§7[AutoSell] " + text);
    }

    public void applyCommandArgs(int price, int stackSize) {
        priceSetting.setValue((float) price);
        stackSizeSetting.setValue((float) stackSize);
    }
}
