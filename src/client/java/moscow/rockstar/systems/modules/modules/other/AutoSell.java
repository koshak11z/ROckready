package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.setting.settings.StringSetting;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Locale;

@ModuleInfo(name = "AutoSell", category = ModuleCategory.OTHER, desc = "srcrich AutoSell state-machine port")
public class AutoSell extends BaseModule {
    private static final int CONFIRM_SLOT_FALLBACK = 25;

    private final SliderSetting stackSizeSetting = new SliderSetting(this, "Размер стака").min(1.0f).max(64.0f).step(1.0f).currentValue(1.0f);
    private final SliderSetting priceSetting = new SliderSetting(this, "Цена").min(1.0f).max(10000000.0f).step(1.0f).currentValue(399999.0f);
    private final SliderSetting actionDelaySetting = new SliderSetting(this, "Задержка").min(100.0f).max(2000.0f).step(50.0f).currentValue(700.0f).suffix(" ms");
    private final StringSetting itemIdSetting = new StringSetting(this, "Item ID").text("minecraft:enchanted_golden_apple");
    private final BooleanSetting restartOnBuy = new BooleanSetting(this, "Перезапуск после покупки").enable();
    private final SliderSetting relistIntervalSetting = new SliderSetting(this, "Интервал переставления").min(5.0f).max(600.0f).step(5.0f).currentValue(40.0f).suffix(" sec");
    private final BooleanSetting debugSetting = new BooleanSetting(this, "Debug").enable();

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
    private int slotsFilledInSession;

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null || mc.player.networkHandler == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();
        long delay = (long)this.actionDelaySetting.getCurrentValue();
        if (now - this.lastActionAt < delay && this.state != State.WAIT_PURCHASE) return;

        switch (this.state) {
            case IDLE -> {}
            case WAIT_PURCHASE -> {
                long intervalMs = (long)this.relistIntervalSetting.getCurrentValue() * 1000L;
                if (now - this.lastActionAt >= intervalMs) {
                    this.debug("Прошло " + (int)this.relistIntervalSetting.getCurrentValue() + " сек — переставляю товары");
                    this.resetCycle();
                    this.state = State.SEND_AH;
                    this.lastActionAt = 0L;
                }
            }
            case SEND_AH -> {
                mc.player.networkHandler.sendChatCommand("ah");
                this.state = State.WAIT_AH;
                this.stamp(now);
            }
            case WAIT_AH -> {
                if (mc.currentScreen instanceof GenericContainerScreen screen && !screen.getScreenHandler().slots.isEmpty()) {
                    this.ahSyncId = screen.getScreenHandler().syncId;
                    this.debug("AH открыт sync=" + this.ahSyncId + " slots=" + screen.getScreenHandler().slots.size());
                    this.state = State.CLICK_47;
                    this.stamp(now);
                } else if (now - this.lastActionAt > 5000L) {
                    this.info("§cНе удалось открыть /ah");
                    this.finishCycle();
                }
            }
            case CLICK_47 -> {
                if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
                    this.state = State.SEND_AH;
                    this.stamp(now);
                    return;
                }
                ScreenHandler handler = screen.getScreenHandler();
                int enderSlot = this.findEnderChestSlot(handler);
                if (enderSlot < 0) {
                    this.debug("Эндер-сундук в /ah не найден, перехожу сразу к sellgui");
                    if (mc.currentScreen != null) mc.player.closeHandledScreen();
                    this.state = State.SEND_SELLGUI;
                    this.stamp(now);
                    return;
                }
                this.click(handler.syncId, enderSlot, 0, SlotActionType.PICKUP);
                this.state = State.WAIT_LISTINGS;
                this.stamp(now);
            }
            case WAIT_LISTINGS -> {
                if (mc.currentScreen instanceof GenericContainerScreen screen && screen.getScreenHandler().syncId != this.ahSyncId) {
                    this.listingsClicksLeft = this.countContainerItems(screen.getScreenHandler());
                    this.debug("Старых лотов найдено: " + this.listingsClicksLeft);
                    this.state = State.CANCEL_LISTINGS;
                    this.stamp(now);
                } else if (now - this.lastActionAt > 4000L) {
                    this.state = State.CLOSE_LISTINGS;
                    this.stamp(now);
                }
            }
            case CANCEL_LISTINGS -> {
                if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
                    this.state = State.SEND_SELLGUI;
                    this.stamp(now);
                    return;
                }
                ScreenHandler handler = screen.getScreenHandler();
                if (this.listingsClicksLeft <= 0 || handler.slots.isEmpty()) {
                    this.state = State.CLOSE_LISTINGS;
                    this.stamp(now);
                    return;
                }
                Slot first = handler.slots.get(0);
                if (first == null || !first.hasStack() || first.inventory instanceof PlayerInventory) {
                    this.state = State.CLOSE_LISTINGS;
                    this.stamp(now);
                    return;
                }
                this.click(handler.syncId, 0, 0, SlotActionType.PICKUP);
                this.listingsClicksLeft--;
                this.stamp(now);
            }
            case CLOSE_LISTINGS -> {
                if (mc.currentScreen != null) mc.player.closeHandledScreen();
                this.state = State.SEND_SELLGUI;
                this.stamp(now);
            }
            case SEND_SELLGUI -> {
                mc.player.networkHandler.sendChatCommand("ah sellgui " + (int)this.priceSetting.getCurrentValue());
                this.state = State.WAIT_SELLGUI;
                this.stamp(now);
            }
            case WAIT_SELLGUI -> {
                if (mc.currentScreen instanceof GenericContainerScreen screen && screen.getScreenHandler().slots.size() > CONFIRM_SLOT_FALLBACK) {
                    this.sellguiSyncId = screen.getScreenHandler().syncId;
                    this.slotsFilledInSession = 0;
                    this.expectedCursorAfterPickup = -1;
                    this.state = State.PLACE_ITEMS;
                    this.stamp(now);
                } else if (now - this.lastActionAt > 5000L) {
                    this.info("§cНе удалось открыть /ah sellgui");
                    this.finishCycle();
                }
            }
            case PLACE_ITEMS -> {
                if (!(mc.currentScreen instanceof GenericContainerScreen screen) || screen.getScreenHandler().syncId != this.sellguiSyncId) {
                    this.state = State.SEND_SELLGUI;
                    this.stamp(now);
                    return;
                }
                this.placeItemsStep(screen.getScreenHandler(), now);
            }
            case CLICK_CONFIRM -> {
                if (!(mc.currentScreen instanceof GenericContainerScreen screen) || screen.getScreenHandler().syncId != this.sellguiSyncId) {
                    this.finishCycle();
                    return;
                }
                ScreenHandler handler = screen.getScreenHandler();
                if (!handler.getCursorStack().isEmpty()) {
                    this.depositCursorBack(handler);
                    this.stamp(now);
                    return;
                }
                int confirmSlot = this.findConfirmSlotId(handler);
                if (confirmSlot < 0) {
                    this.info("§cНе найдена кнопка подтверждения");
                    this.state = State.CYCLE_END;
                    this.stamp(now);
                    return;
                }
                this.confirmSeen = false;
                this.click(handler.syncId, confirmSlot, 0, SlotActionType.PICKUP);
                this.state = State.WAIT_CONFIRMATION;
                this.confirmRetries++;
                this.stamp(now);
            }
            case WAIT_CONFIRMATION -> {
                if (this.confirmSeen) {
                    this.info("§aУспешно выставлено");
                    this.state = State.CYCLE_END;
                    this.stamp(now);
                } else if (now - this.lastActionAt > 3500L) {
                    if (this.confirmRetries < 2) {
                        this.info("§eНет подтверждения, ретрай " + this.confirmRetries);
                        this.state = State.CLICK_CONFIRM;
                    } else {
                        this.info("§cПодтверждение не получено");
                        this.state = State.CYCLE_END;
                    }
                    this.stamp(now);
                }
            }
            case CYCLE_END -> {
                if (mc.currentScreen != null) mc.player.closeHandledScreen();
                if (this.countItemsInInventory() >= (int)this.stackSizeSetting.getCurrentValue()) {
                    this.debug("В инвентаре ещё есть предметы — новый sellgui цикл");
                    this.resetCycle();
                    this.state = State.SEND_SELLGUI;
                } else {
                    this.debug("Ожидаю покупки...");
                    this.state = State.WAIT_PURCHASE;
                }
                this.stamp(now);
            }
        }
    };

    private final EventListener<ReceivePacketEvent> onPacket = event -> {
        if (!(event.getPacket() instanceof GameMessageS2CPacket packet)) return;
        String text = packet.content().getString().toLowerCase(Locale.ROOT);
        if (text.contains("на продажу успешно выставлено")) this.confirmSeen = true;
        if (this.restartOnBuy.isEnabled() && text.contains("у вас купили") && this.state == State.WAIT_PURCHASE) {
            this.info("§aОбнаружена покупка — запускаю новый цикл");
            this.resetCycle();
            this.state = State.SEND_AH;
            this.lastActionAt = 0L;
        }
    };

    @Override
    public void onEnable() {
        super.onEnable();
        this.resetCycle();
        this.state = State.SEND_AH;
        this.lastActionAt = 0L;
        this.info("§aAutoSell включён. Цена=" + (int)this.priceSetting.getCurrentValue() + " стак=" + (int)this.stackSizeSetting.getCurrentValue());
    }

    @Override
    public void onDisable() {
        this.state = State.IDLE;
        super.onDisable();
    }

    private void resetCycle() {
        this.ahSyncId = -1;
        this.sellguiSyncId = -1;
        this.listingsClicksLeft = 0;
        this.confirmRetries = 0;
        this.confirmSeen = false;
        this.expectedCursorAfterPickup = -1;
        this.slotsFilledInSession = 0;
    }

    private void stamp(long now) { this.lastActionAt = now; }

    private void finishCycle() {
        if (mc.currentScreen != null) mc.player.closeHandledScreen();
        this.state = State.WAIT_PURCHASE;
        this.lastActionAt = System.currentTimeMillis();
    }

    private int countContainerItems(ScreenHandler handler) {
        int count = 0;
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            if (slot.hasStack()) count++;
        }
        return count;
    }

    private int countItemsInInventory() {
        Item target = this.resolveTargetItem();
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (target == null || stack.getItem() == target) count += stack.getCount();
        }
        return count;
    }

    private Item resolveTargetItem() {
        String id = this.itemIdSetting.getText();
        if (id == null || id.isBlank()) return null;
        Identifier identifier = Identifier.tryParse(id.trim());
        if (identifier == null) return null;
        Item item = Registries.ITEM.get(identifier);
        return item == Items.AIR ? null : item;
    }

    private void placeItemsStep(ScreenHandler handler, long now) {
        Item target = this.resolveTargetItem();
        ItemStack cursor = handler.getCursorStack();
        if (target != null && !cursor.isEmpty() && cursor.getItem() != target) {
            this.debug("§cНа курсоре чужой предмет, возвращаю");
            this.depositCursorBack(handler);
            this.stamp(now);
            return;
        }
        Slot emptyTarget = this.findEmptyPlacementSlot(handler);
        if (emptyTarget == null) {
            if (!handler.getCursorStack().isEmpty()) {
                this.depositCursorBack(handler);
                this.stamp(now);
                return;
            }
            this.state = State.CLICK_CONFIRM;
            this.stamp(now);
            return;
        }
        if (cursor.isEmpty()) {
            Slot source = this.findInventorySourceSlot(handler, target);
            if (source == null) {
                this.state = State.CLICK_CONFIRM;
                this.stamp(now);
                return;
            }
            this.expectedCursorAfterPickup = source.getStack().getCount();
            this.click(handler.syncId, source.id, 0, SlotActionType.PICKUP);
            this.stamp(now);
            return;
        }
        if (this.expectedCursorAfterPickup > 0 && cursor.getCount() != this.expectedCursorAfterPickup) {
            this.info("§cСТОП: курсор не сошёлся " + cursor.getCount() + " вместо " + this.expectedCursorAfterPickup);
            this.disable();
            return;
        }
        this.expectedCursorAfterPickup = -1;
        int before = cursor.getCount();
        this.click(handler.syncId, emptyTarget.id, 1, SlotActionType.PICKUP);
        int after = handler.getCursorStack().getCount();
        int delta = before - after;
        if (delta > 1) {
            this.info("§cСТОП: сервер забрал " + delta + " предметов вместо 1");
            this.disable();
            return;
        }
        if (delta < 0) {
            this.info("§cСТОП: курсор увеличился");
            this.disable();
            return;
        }
        if (delta == 1) this.slotsFilledInSession++;
        if (this.slotsFilledInSession >= (int)this.stackSizeSetting.getCurrentValue() && !handler.getCursorStack().isEmpty()) {
            this.depositCursorBack(handler);
        }
        if (this.slotsFilledInSession >= (int)this.stackSizeSetting.getCurrentValue()) this.state = State.CLICK_CONFIRM;
        this.stamp(now);
    }

    private Slot findInventorySourceSlot(ScreenHandler handler, Item target) {
        for (Slot slot : handler.slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            if (target != null && stack.getItem() != target) continue;
            return slot;
        }
        return null;
    }

    private Slot findEmptyPlacementSlot(ScreenHandler handler) {
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            if (!slot.hasStack()) return slot;
        }
        return null;
    }

    private int findEnderChestSlot(ScreenHandler handler) {
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.getItem() == Items.ENDER_CHEST) return slot.id;
        }
        return -1;
    }

    private int findConfirmSlotId(ScreenHandler handler) {
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.getItem() == Items.SLIME_BALL) return slot.id;
        }
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            String name = stack.getName().getString().toLowerCase(Locale.ROOT);
            if (name.contains("продаж") || name.contains("подтвер") || name.contains("выстав")) return slot.id;
        }
        return handler.slots.size() > CONFIRM_SLOT_FALLBACK ? CONFIRM_SLOT_FALLBACK : -1;
    }

    private void depositCursorBack(ScreenHandler handler) {
        if (handler.getCursorStack().isEmpty()) return;
        for (Slot slot : handler.slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            if (!slot.hasStack()) {
                this.click(handler.syncId, slot.id, 0, SlotActionType.PICKUP);
                return;
            }
        }
        ItemStack cursor = handler.getCursorStack();
        for (Slot slot : handler.slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.getItem() == cursor.getItem()) {
                this.click(handler.syncId, slot.id, 0, SlotActionType.PICKUP);
                return;
            }
        }
    }

    private void click(int syncId, int slotId, int button, SlotActionType action) {
        try { mc.interactionManager.clickSlot(syncId, slotId, button, action, mc.player); } catch (Exception ignored) {}
    }

    private void debug(String text) { if (this.debugSetting.isEnabled()) this.info("§8" + text); }
    private void info(String text) { MessageUtility.info(Text.of("§7[AutoSell] " + text)); }

    public void applyCommandArgs(int price, int stackSize) {
        this.priceSetting.currentValue((float)price);
        this.stackSizeSetting.currentValue((float)stackSize);
    }
}
