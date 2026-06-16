package im.zov4ik.features.impl.misc;

import antidaunleak.api.annotation.Native;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.TextSetting;
import im.zov4ik.features.module.setting.implement.ValueSetting;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.client.managers.event.EventHandler;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Порт mineflayer-бота из index.js под клиентскую базу 1.21.4.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VillagerAppleTrader extends Module {
    // ---------- Настройки скорости / таймингов ----------
    static long STEP_DELAY_MS = 280L;
    static long COMMAND_WAIT_MS = 900L;
    static long WINDOW_WAIT_MS = 3500L;
    static long TRADE_COOLDOWN_MS = 30_000L;
    static long AUTOSELL_INTERVAL_MS = 40_000L;
    static long INVEST_INTERVAL_MS = 60_000L; // [FIX] как часто инвестить

    // ---------- Fallback слоты GUI ----------
    static int SHOP_GOLD_FALLBACK_SLOT = 11;
    static int SHOP_EMERALD_FALLBACK_SLOT = 15;
    static int SHOP_CONFIRM_SLOT = 2;
    static int SELL_CONFIRM_FALLBACK_SLOT = 25; // [FIX] из AutoSell (slime_ball ~ слот 25)
    static int CRAFT_OUTPUT_SLOT = 0;
    static int[] CRAFT_GRID_SLOTS = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};

    // [FIX] донат-команды, которые надо глушить при выключенной настройке
    static List<String> DONATE_COMMANDS = List.of("/craft", "/ec", "/feed");

    BooleanSetting autoBuyEmeralds = new BooleanSetting("Покупать изумруды", "Покупает изумруды через /shop")
            .setValue(true);
    BooleanSetting tradeVillagers = new BooleanSetting("Торговать с жителями", "Покупает золото у жителей за изумруды")
            .setValue(true);
    BooleanSetting craftGoldBlocks = new BooleanSetting("Крафтить блоки золота", "Крафтит gold_ingot в gold_block")
            .setValue(true);
    BooleanSetting takeApples = new BooleanSetting("Брать яблоки", "Берет apple из /clan storage")
            .setValue(true);
    BooleanSetting craftEnchantedApples = new BooleanSetting("Крафтить чарки", "Крафтит зачарованные яблоки")
            .setValue(true);
    BooleanSetting autoSellApples = new BooleanSetting("Авто продажа чарок", "Снимает старые лоты и выставляет чарки через /ah sellgui")
            .setValue(false);
    BooleanSetting autoInvest = new BooleanSetting("Авто инвест", "Отправляет деньги в /clan invest")
            .setValue(false);

    // [FIX] переключатель донат-команд (как useDonateCommands в index.js)
    BooleanSetting useDonateCommands = new BooleanSetting("Донат-команды", "Использовать /craft, /ec, /feed. Выключи — крафт через верстак рядом")
            .setValue(true);
    // [FIX] снятие денег с клана на изумруды
    BooleanSetting withdrawForEmeralds = new BooleanSetting("Снимать с клана на изумруды", "Перед /shop снимает деньги с клан-банка")
            .setValue(true);
    // [FIX] снимать ли старые лоты из хранилища перед выставлением
    BooleanSetting withdrawStorageFirst = new BooleanSetting("Снимать лоты из хранилища", "Перед продажей заходит в /ah хранилище и снимает все предметы")
            .setValue(true);
    // [FIX] перевыставлять при покупке
    BooleanSetting relistOnBuy = new BooleanSetting("Перевыставлять после покупки", "Ловит 'у вас купили' и сразу переставляет лоты")
            .setValue(true);
    // [FIX] лог отладки авто-селла
    BooleanSetting debugLog = new BooleanSetting("Лог авто-селла", "Пишет в чат что делает авто-селл")
            .setValue(true);

    ValueSetting minEmeralds = new ValueSetting("Мин. изумрудов", "Если меньше — докупает emerald через /shop")
            .setValue(20F).range(0F, 256F);
    ValueSetting villagerRadius = new ValueSetting("Радиус жителей", "Радиус поиска жителей")
            .setValue(48F).range(4F, 96F);
    ValueSetting autoSellSlots = new ValueSetting("Слотов продажи", "Сколько чарок выставлять за цикл (по 1 в слот)")
            .setValue(8F).range(1F, 45F);
    // [FIX] цена изумруда для расчёта суммы снятия с клана (5000 * 64 как в index.js)
    ValueSetting emeraldPrice = new ValueSetting("Цена изумруда", "Стоимость 1 изумруда для расчёта /clan withdraw")
            .setValue(5000F).range(1F, 100000F);
    // [FIX] радиус поиска верстака, когда донат-команды выключены
    ValueSetting craftingTableRadius = new ValueSetting("Радиус верстака", "Поиск верстака рядом (если донат-команды выключены)")
            .setValue(5F).range(1F, 16F);
    // [FIX] сервер требует число в /clan invest, поэтому шлём фикс-сумму
    ValueSetting investAmount = new ValueSetting("Сумма инвеста", "Сколько кидать в /clan invest за раз")
            .setValue(100000F).range(1F, 100000000F);

    TextSetting autoSellPrice = new TextSetting("Цена чарки", "Цена для /ah sellgui")
            .setText("169999")
            .setMin(1)
            .setMax(12);
    // [FIX] координата верстака (пусто = искать рядом)
    TextSetting craftingTablePos = new TextSetting("Координата верстака", "x y z, если донат-команды выключены (пусто = искать рядом)")
            .setText("")
            .setMin(0)
            .setMax(32);

    @NonFinal State state = State.IDLE;
    @NonFinal long nextActionAt = 0L;
    @NonFinal long lastAutoSellAt = 0L;
    @NonFinal long lastInvestAt = 0L; // [FIX]
    @NonFinal MerchantEntity targetVillager;
    @NonFinal int selectedTrade = -1;
    @NonFinal int craftPhase = 0;
    @NonFinal int autoSellPlaced = 0;
    @NonFinal long openedCommandAt = 0L;
    // [FIX] цель-верстак и куда вернуться после открытия
    @NonFinal BlockPos targetCraftingTable;
    @NonFinal State craftReturnState = State.IDLE;
    @NonFinal long craftWalkStartedAt = 0L;
    // [FIX] поля авто-селла (из AutoSell)
    @NonFinal boolean confirmSeen = false;
    @NonFinal int ahSyncId = -1;
    @NonFinal int listingsClicksLeft = 0;
    @NonFinal int confirmRetries = 0;
    @NonFinal long storageClickedAt = 0L;
    @NonFinal long confirmClickedAt = 0L;

    Map<String, Long> villagerCooldowns = new HashMap<>();

    public VillagerAppleTrader() {
        super("VillagerAppleTrader", "Auto emerald/shop, villager trades, apples craft and autosell", ModuleCategory.MISC);
        setup(autoBuyEmeralds, tradeVillagers, craftGoldBlocks, takeApples, craftEnchantedApples, autoSellApples,
                autoInvest, useDonateCommands, withdrawForEmeralds, withdrawStorageFirst, relistOnBuy, debugLog,
                minEmeralds, villagerRadius, autoSellSlots, emeraldPrice, craftingTableRadius, investAmount,
                autoSellPrice, craftingTablePos);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        resetState();
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < nextActionAt) {
            return;
        }
        cleanupVillagerCooldowns(now);
        try {
            tickState(now);
        } catch (Exception exception) {
            ChatMessage.brandmessage("VillagerAppleTrader error: " + exception.getMessage());
            resetState();
            delay(1000L);
        }
    }

    // [FIX] ловим подтверждение/покупку из чата (как onPacket в AutoSell)
    @EventHandler
    public void onPacket(PacketEvent event) {
        if (mc.player == null) return;
        if (event.getType() != PacketEvent.Type.RECEIVE) return;
        if (!(event.getPacket() instanceof GameMessageS2CPacket message)) return;
        String text = message.content().getString().toLowerCase(Locale.ROOT);
        if (text.contains("на продажу успешно выставлено")) {
            confirmSeen = true;
        }
        if (relistOnBuy.isValue() && autoSellApples.isValue() && text.contains("у вас купили")) {
            dbg("обнаружена покупка — переставлю лоты в следующем цикле");
            lastAutoSellAt = 0L;
        }
    }

    private void tickState(long now) {
        switch (state) {
            case IDLE -> chooseNextTask(now);
            case SHOP_WITHDRAW -> tickShopWithdraw(); // [FIX]
            case SHOP_OPEN -> tickShopOpen(now);
            case SHOP_CLICK_GOLD -> tickShopClickGold();
            case SHOP_CLICK_EMERALD -> tickShopClickEmerald();
            case SHOP_CONFIRM -> tickShopConfirm();
            case FIND_VILLAGER -> tickFindVillager(now);
            case WALK_TO_VILLAGER -> tickWalkToVillager();
            case OPEN_VILLAGER -> tickOpenVillager();
            case TRADE_VILLAGER -> tickTradeVillager();
            case OPEN_CRAFT_BLOCKS -> tickOpenCraftBlocks();
            case WALK_TO_CRAFTING_TABLE -> tickWalkToCraftingTable(now); // [FIX]
            case CRAFT_GOLD_BLOCKS -> tickCraftGoldBlocks();
            case OPEN_APPLE_STORAGE -> tickOpenAppleStorage();
            case TAKE_APPLES -> tickTakeApples();
            case OPEN_CRAFT_EAPPLES -> tickOpenCraftEapples();
            case CRAFT_EAPPLES -> tickCraftEapples();
            case AUTOSELL_OPEN_AH -> tickAutoSellOpenAh(now);          // [FIX]
            case AUTOSELL_OPEN_STORAGE -> tickAutoSellOpenStorage(now); // [FIX]
            case AUTOSELL_WAIT_LISTINGS -> tickAutoSellWaitListings(now); // [FIX]
            case AUTOSELL_REMOVE_OLD -> tickAutoSellRemoveOld();        // [FIX]
            case AUTOSELL_OPEN_SELLGUI -> tickAutoSellOpenSellgui(now);
            case AUTOSELL_PLACE_ITEMS -> tickAutoSellPlaceItems();
            case AUTOSELL_CONFIRM -> tickAutoSellConfirm(now);          // [FIX]
            case AUTOSELL_WAIT_CONFIRM -> tickAutoSellWaitConfirm(now); // [FIX]
            case WAIT_WINDOW -> tickWaitWindow();
        }
    }

    private void chooseNextTask(long now) {
        closeChatIfOpen();
        if (craftGoldBlocks.isValue() && countItem(Items.GOLD_INGOT) >= 64) {
            state = State.OPEN_CRAFT_BLOCKS;
            return;
        }
        if (craftEnchantedApples.isValue() && countItem(Items.GOLD_BLOCK) >= 8) {
            int possible = countItem(Items.GOLD_BLOCK) / 8;
            if (takeApples.isValue() && countItem(Items.APPLE) < Math.min(possible, 64)) {
                state = State.OPEN_APPLE_STORAGE;
                return;
            }
            if (countItem(Items.APPLE) > 0) {
                state = State.OPEN_CRAFT_EAPPLES;
                return;
            }
        }
        // [FIX] авто-селл: сначала хранилище (снять старые лоты), потом sellgui
        if (autoSellApples.isValue() && now - lastAutoSellAt >= AUTOSELL_INTERVAL_MS && readPrice() > 0) {
            dbg("старт цикла продажи (чарок в инв=" + countSellApples() + ", цена=" + readPrice() + ")");
            resetAutoSellCycle();
            openedCommandAt = 0L;
            state = withdrawStorageFirst.isValue() ? State.AUTOSELL_OPEN_AH : State.AUTOSELL_OPEN_SELLGUI;
            return;
        }
        // [FIX] /clan invest требует число, а не "all"; шлём по интервалу и не блокируем остальные задачи
        if (autoInvest.isValue() && now - lastInvestAt >= INVEST_INTERVAL_MS) {
            long amount = (long) Math.ceil(investAmount.getValue());
            sendCommand("/clan invest " + amount);
            lastInvestAt = now;
            delay(1500L);
            return;
        }
        if (autoBuyEmeralds.isValue() && countItem(Items.EMERALD) < Math.round(minEmeralds.getValue())) {
            // [FIX] сначала снимаем с клана (если включено), потом /shop
            state = withdrawForEmeralds.isValue() ? State.SHOP_WITHDRAW : State.SHOP_OPEN;
            return;
        }
        if (tradeVillagers.isValue() && countItem(Items.EMERALD) > 0) {
            state = State.FIND_VILLAGER;
            return;
        }
        delay(700L);
    }

    // ---------- [FIX] /clan withdraw перед покупкой изумрудов ----------
    private void tickShopWithdraw() {
        long stackCost = (long) Math.ceil(emeraldPrice.getValue()) * 64L;
        ChatMessage.brandmessage("Снимаю с клана " + String.format("%,d", stackCost) + " на изумруды");
        sendCommand("/clan withdraw " + stackCost);
        openedCommandAt = 0L;
        state = State.SHOP_OPEN;
        delay(COMMAND_WAIT_MS);
    }

    // ---------- /shop emerald ----------
    private void tickShopOpen(long now) {
        if (hasOpenContainer()) {
            state = State.SHOP_CLICK_GOLD;
            delay(250L);
            return;
        }
        if (openedCommandAt == 0L || now - openedCommandAt > WINDOW_WAIT_MS) {
            sendCommand("/shop");
            openedCommandAt = now;
            delay(COMMAND_WAIT_MS);
            return;
        }
        delay(150L);
    }

    private void tickShopClickGold() {
        ScreenHandler handler = mc.player.currentScreenHandler;
        int slot = findContainerSlotByItem(handler, Items.GOLD_INGOT);
        if (slot < 0) slot = SHOP_GOLD_FALLBACK_SLOT;
        click(slot, 0, SlotActionType.PICKUP, "shop_gold");
        state = State.SHOP_CLICK_EMERALD;
        delay(650L);
    }

    private void tickShopClickEmerald() {
        ScreenHandler handler = mc.player.currentScreenHandler;
        int before = countItem(Items.EMERALD);
        int slot = findContainerSlotByItemOrText(handler, Items.EMERALD, "изумруд", "emerald");
        if (slot < 0) slot = SHOP_EMERALD_FALLBACK_SLOT;
        click(slot, 1, SlotActionType.PICKUP, "shop_emerald_rclick");
        state = countItem(Items.EMERALD) > before ? State.IDLE : State.SHOP_CONFIRM;
        delay(700L);
    }

    private void tickShopConfirm() {
        if (hasOpenContainer()) {
            click(SHOP_CONFIRM_SLOT, 0, SlotActionType.PICKUP, "shop_confirm");
        }
        closeScreen();
        state = State.IDLE;
        openedCommandAt = 0L;
        delay(500L);
    }

    // ---------- Villager trading ----------
    private void tickFindVillager(long now) {
        double radius = villagerRadius.getValue();
        Optional<MerchantEntity> nearest = mc.world.getEntitiesByClass(MerchantEntity.class,
                        mc.player.getBoundingBox().expand(radius), this::isVillagerReady)
                .stream()
                .min(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(mc.player)));
        if (nearest.isEmpty()) {
            state = State.IDLE;
            delay(1500L);
            return;
        }
        targetVillager = nearest.get();
        state = State.WALK_TO_VILLAGER;
        delay(50L);
    }

    private void tickWalkToVillager() {
        if (targetVillager == null || targetVillager.isRemoved() || !targetVillager.isAlive()) {
            state = State.FIND_VILLAGER;
            return;
        }
        double distance = mc.player.distanceTo(targetVillager);
        lookAt(targetVillager.getBoundingBox().getCenter());
        if (distance <= 2.7D) {
            stopMove();
            state = State.OPEN_VILLAGER;
            delay(120L);
            return;
        }
        mc.options.forwardKey.setPressed(true);
        mc.options.sprintKey.setPressed(false);
        if (mc.player.horizontalCollision) {
            mc.options.jumpKey.setPressed(true);
        } else {
            mc.options.jumpKey.setPressed(false);
        }
        delay(50L);
    }

    private void tickOpenVillager() {
        stopMove();
        if (targetVillager == null || targetVillager.isRemoved()) {
            state = State.FIND_VILLAGER;
            return;
        }
        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interactAt(targetVillager, false, Hand.MAIN_HAND, targetVillager.getBoundingBox().getCenter()));
        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(targetVillager, false, Hand.MAIN_HAND));
        state = State.TRADE_VILLAGER;
        delay(600L);
    }

    private void tickTradeVillager() {
        if (!(mc.player.currentScreenHandler instanceof MerchantScreenHandler handler)) {
            rememberVillagerCooldown();
            state = State.IDLE;
            delay(400L);
            return;
        }
        if (selectedTrade < 0) {
            selectedTrade = findGoldForEmeraldTrade(handler);
            if (selectedTrade < 0) {
                rememberVillagerCooldown();
                closeScreen();
                state = State.IDLE;
                delay(300L);
                return;
            }
        }
        TradeOffer offer = handler.getRecipes().get(selectedTrade);
        int cost = emeraldCost(offer);
        if (offer.isDisabled() || cost <= 0 || countItem(Items.EMERALD) < cost) {
            selectedTrade = -1;
            rememberVillagerCooldown();
            closeScreen();
            state = State.IDLE;
            delay(300L);
            return;
        }
        int before = countItem(Items.GOLD_INGOT);
        mc.player.networkHandler.sendPacket(new SelectMerchantTradeC2SPacket(selectedTrade));
        delay(120L);
        click(2, 0, SlotActionType.QUICK_MOVE, "villager_trade_out");
        if (countItem(Items.GOLD_INGOT) <= before && offer.isDisabled()) {
            selectedTrade = -1;
            rememberVillagerCooldown();
            closeScreen();
            state = State.IDLE;
            delay(400L);
            return;
        }
        delay(170L);
    }

    // ---------- Craft gold blocks ----------
    private void tickOpenCraftBlocks() {
        craftPhase = 0;
        if (useDonateCommands.isValue()) {
            sendCommand("/craft");
            state = State.CRAFT_GOLD_BLOCKS;
            delay(COMMAND_WAIT_MS);
            return;
        }
        // [FIX] донат выключен — идём к верстаку, а не тыкаем издалека
        beginWalkToCraftingTable(State.CRAFT_GOLD_BLOCKS);
    }

    private void tickCraftGoldBlocks() {
        if (!hasOpenContainer()) {
            state = State.IDLE;
            delay(500L);
            return;
        }
        if (countItem(Items.GOLD_INGOT) < 9) {
            closeScreen();
            state = State.IDLE;
            delay(300L);
            return;
        }
        for (int gridSlot : CRAFT_GRID_SLOTS) {
            int source = findPlayerSlotInHandler(mc.player.currentScreenHandler, Items.GOLD_INGOT);
            if (source < 0) break;
            click(source, 0, SlotActionType.PICKUP, "craft_block_pick");
            delay(40L);
            click(gridSlot, 1, SlotActionType.PICKUP, "craft_block_place_one");
            delay(40L);
            click(source, 0, SlotActionType.PICKUP, "craft_block_return");
        }
        click(CRAFT_OUTPUT_SLOT, 0, SlotActionType.QUICK_MOVE, "craft_block_take");
        delay(450L);
    }

    // ---------- Apple source ----------
    private void tickOpenAppleStorage() {
        sendCommand("/clan storage");
        state = State.TAKE_APPLES;
        delay(COMMAND_WAIT_MS);
    }

    private void tickTakeApples() {
        if (!hasOpenContainer()) {
            state = State.IDLE;
            delay(500L);
            return;
        }
        int slot = findContainerSlotByItem(mc.player.currentScreenHandler, Items.APPLE);
        if (slot >= 0) {
            click(slot, 0, SlotActionType.QUICK_MOVE, "take_apples");
            delay(300L);
        }
        closeScreen();
        state = State.IDLE;
        delay(300L);
    }

    // ---------- Craft enchanted apples ----------
    private void tickOpenCraftEapples() {
        craftPhase = 0;
        if (useDonateCommands.isValue()) {
            sendCommand("/craft");
            state = State.CRAFT_EAPPLES;
            delay(COMMAND_WAIT_MS);
            return;
        }
        // [FIX] донат выключен — идём к верстаку
        beginWalkToCraftingTable(State.CRAFT_EAPPLES);
    }

    private void tickCraftEapples() {
        if (!hasOpenContainer()) {
            state = State.IDLE;
            delay(500L);
            return;
        }
        if (countItem(Items.GOLD_BLOCK) < 8 || countItem(Items.APPLE) < 1) {
            closeScreen();
            state = State.IDLE;
            delay(300L);
            return;
        }
        int[] goldSlots = new int[]{1, 2, 3, 4, 6, 7, 8, 9};
        for (int gridSlot : goldSlots) {
            int source = findPlayerSlotInHandler(mc.player.currentScreenHandler, Items.GOLD_BLOCK);
            if (source < 0) break;
            click(source, 0, SlotActionType.PICKUP, "craft_eapple_gold_pick");
            click(gridSlot, 1, SlotActionType.PICKUP, "craft_eapple_gold_place");
            click(source, 0, SlotActionType.PICKUP, "craft_eapple_gold_return");
        }
        int appleSource = findPlayerSlotInHandler(mc.player.currentScreenHandler, Items.APPLE);
        if (appleSource >= 0) {
            click(appleSource, 0, SlotActionType.PICKUP, "craft_eapple_apple_pick");
            click(5, 1, SlotActionType.PICKUP, "craft_eapple_apple_place");
            click(appleSource, 0, SlotActionType.PICKUP, "craft_eapple_apple_return");
        }
        click(CRAFT_OUTPUT_SLOT, 0, SlotActionType.QUICK_MOVE, "craft_eapple_take");
        delay(650L);
    }

    // ---------- [FIX] Подход к верстаку ----------
    private void beginWalkToCraftingTable(State returnState) {
        BlockPos table = findCraftingTable();
        if (table == null) {
            ChatMessage.brandmessage("Донат-команды выключены, а верстака рядом нет — крафт невозможен");
            state = State.IDLE;
            delay(1500L);
            return;
        }
        targetCraftingTable = table;
        craftReturnState = returnState;
        craftWalkStartedAt = System.currentTimeMillis();
        state = State.WALK_TO_CRAFTING_TABLE;
        delay(50L);
    }

    private void tickWalkToCraftingTable(long now) {
        if (targetCraftingTable == null || mc.world == null) {
            stopMove();
            state = State.IDLE;
            delay(500L);
            return;
        }
        if (!mc.world.getBlockState(targetCraftingTable).isOf(Blocks.CRAFTING_TABLE)) {
            BlockPos again = findCraftingTable();
            if (again == null) {
                stopMove();
                ChatMessage.brandmessage("Верстак пропал — крафт невозможен");
                state = State.IDLE;
                delay(1000L);
                return;
            }
            targetCraftingTable = again;
        }
        if (now - craftWalkStartedAt > 8000L) {
            stopMove();
            dbg("не смог дойти до верстака за 8с — сброс");
            state = State.IDLE;
            delay(1000L);
            return;
        }
        Vec3d center = Vec3d.ofCenter(targetCraftingTable);
        lookAt(center);
        double distance = Math.sqrt(mc.player.squaredDistanceTo(center));
        if (distance <= 2.7D) {
            stopMove();
            interactBlock(targetCraftingTable);
            state = craftReturnState;
            delay(COMMAND_WAIT_MS);
            return;
        }
        mc.options.forwardKey.setPressed(true);
        mc.options.sprintKey.setPressed(false);
        if (mc.player.horizontalCollision) {
            mc.options.jumpKey.setPressed(true);
        } else {
            mc.options.jumpKey.setPressed(false);
        }
        delay(50L);
    }

    // ========== [FIX] AutoSell: хранилище -> снять лоты -> sellgui -> разложить -> подтвердить ==========

    // 1) открыть /ah
    private void tickAutoSellOpenAh(long now) {
        if (hasOpenContainer() && !mc.player.currentScreenHandler.slots.isEmpty()) {
            ahSyncId = mc.player.currentScreenHandler.syncId;
            dbg("/ah открыт, слотов=" + mc.player.currentScreenHandler.slots.size());
            state = State.AUTOSELL_OPEN_STORAGE;
            delay(300L);
            return;
        }
        if (openedCommandAt == 0L) {
            dbg("шлю /ah");
            sendCommand("/ah");
            openedCommandAt = now;
            delay(COMMAND_WAIT_MS);
            return;
        }
        if (now - openedCommandAt > 5000L) {
            dbg("/ah не открылся — иду сразу на sellgui");
            openedCommandAt = 0L;
            state = State.AUTOSELL_OPEN_SELLGUI;
            delay(500L);
            return;
        }
        delay(150L);
    }

    // 2) в /ah найти эндер-сундук (хранилище) и открыть его
    private void tickAutoSellOpenStorage(long now) {
        if (!hasOpenContainer()) {
            openedCommandAt = 0L;
            state = State.AUTOSELL_OPEN_AH;
            delay(500L);
            return;
        }
        ScreenHandler handler = mc.player.currentScreenHandler;
        int enderSlot = findEnderChestSlot(handler);
        if (enderSlot < 0) {
            dbg("хранилище (эндер-сундук) не найдено — иду на sellgui");
            closeScreen();
            openedCommandAt = 0L;
            state = State.AUTOSELL_OPEN_SELLGUI;
            delay(500L);
            return;
        }
        dbg("открываю хранилище, слот " + enderSlot);
        ahSyncId = handler.syncId;
        click(enderSlot, 0, SlotActionType.PICKUP, "ah_open_storage");
        storageClickedAt = now;
        state = State.AUTOSELL_WAIT_LISTINGS;
        delay(500L);
    }

    // 3) ждём окно хранилища (syncId сменился)
    private void tickAutoSellWaitListings(long now) {
        if (!hasOpenContainer()) {
            openedCommandAt = 0L;
            state = State.AUTOSELL_OPEN_SELLGUI;
            delay(500L);
            return;
        }
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (handler.syncId != ahSyncId) {
            listingsClicksLeft = countContainerItems(handler);
            dbg("хранилище открыто, предметов=" + listingsClicksLeft);
            state = State.AUTOSELL_REMOVE_OLD;
            delay(300L);
            return;
        }
        if (now - storageClickedAt > 4000L) {
            dbg("окно хранилища не сменилось — иду на sellgui");
            closeScreen();
            openedCommandAt = 0L;
            state = State.AUTOSELL_OPEN_SELLGUI;
            delay(500L);
            return;
        }
        delay(150L);
    }

    // 4) снять ВСЕ предметы из хранилища (клик по слоту 0, как в AutoSell)
    private void tickAutoSellRemoveOld() {
        if (!hasOpenContainer()) {
            openedCommandAt = 0L;
            state = State.AUTOSELL_OPEN_SELLGUI;
            delay(500L);
            return;
        }
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (listingsClicksLeft <= 0 || handler.slots.isEmpty()) {
            dbg("хранилище очищено — закрываю");
            closeScreen();
            openedCommandAt = 0L;
            state = State.AUTOSELL_OPEN_SELLGUI;
            delay(600L);
            return;
        }
        Slot first = handler.slots.get(0);
        if (first == null || !first.hasStack() || first.inventory instanceof PlayerInventory) {
            dbg("в слоте 0 нет лота — закрываю хранилище");
            closeScreen();
            openedCommandAt = 0L;
            state = State.AUTOSELL_OPEN_SELLGUI;
            delay(600L);
            return;
        }
        dbg("снимаю лот из слота 0 (осталось ~" + listingsClicksLeft + ")");
        click(0, 0, SlotActionType.PICKUP, "ah_withdraw_listing");
        listingsClicksLeft--;
        delay(STEP_DELAY_MS);
    }

    // 5) открыть sellgui напрямую и ждать окно
    private void tickAutoSellOpenSellgui(long now) {
        int price = readPrice();
        if (price <= 0) {
            dbg("цена некорректна, отмена");
            finishAutoSellCycle();
            return;
        }
        if (hasOpenContainer() && mc.player.currentScreenHandler.slots.size() > SELL_CONFIRM_FALLBACK_SLOT) {
            dbg("sellgui открыт, слотов=" + mc.player.currentScreenHandler.slots.size());
            dumpContainer("меню при открытии");
            openedCommandAt = 0L;
            autoSellPlaced = 0;
            state = State.AUTOSELL_PLACE_ITEMS;
            delay(400L);
            return;
        }
        if (openedCommandAt == 0L || now - openedCommandAt > WINDOW_WAIT_MS) {
            dbg("шлю /ah sellgui " + price);
            sendCommand("/ah sellgui " + price);
            openedCommandAt = now;
            delay(COMMAND_WAIT_MS);
            return;
        }
        delay(150L);
    }

    // 6) раскладка по 1 чарке: взять стак ЛКМ -> класть по 1 ПКМ в пустые слоты
    private void tickAutoSellPlaceItems() {
        if (!hasOpenContainer()) {
            dbg("окно закрылось во время раскладки");
            finishAutoSellCycle();
            return;
        }
        ScreenHandler handler = mc.player.currentScreenHandler;
        ItemStack cursor = handler.getCursorStack();
        int need = Math.round(autoSellSlots.getValue());

        int target = findEmptySellSlot(handler);
        if (autoSellPlaced >= need || target < 0) {
            if (!cursor.isEmpty()) {
                depositCursorBack(handler);
                delay(STEP_DELAY_MS);
                return;
            }
            dbg("раскладка завершена: выставлено=" + autoSellPlaced);
            dumpContainer("меню перед подтверждением");
            state = State.AUTOSELL_CONFIRM;
            delay(400L);
            return;
        }

        // на курсоре чужой предмет — вернуть
        if (!cursor.isEmpty() && !isSellApple(cursor)) {
            dbg("на курсоре не чарка — возвращаю");
            depositCursorBack(handler);
            delay(STEP_DELAY_MS);
            return;
        }

        // курсор пуст — берём стак чарок из инвентаря
        if (cursor.isEmpty()) {
            int src = findSellAppleInventorySlot(handler);
            if (src < 0) {
                dbg("чарок в инвентаре больше нет, выставлено=" + autoSellPlaced);
                state = State.AUTOSELL_CONFIRM;
                delay(400L);
                return;
            }
            dbg("беру стак чарок из слота " + src);
            click(src, 0, SlotActionType.PICKUP, "sell_pick_stack");
            delay(STEP_DELAY_MS);
            return;
        }

        // на курсоре чарки — кладём ОДНУ правым кликом
        int before = cursor.getCount();
        click(target, 1, SlotActionType.PICKUP, "sell_place_one"); // ПКМ = 1 шт
        int after = handler.getCursorStack().getCount();
        if (before - after == 1) {
            autoSellPlaced++;
            dbg("положил 1 чарку в слот " + target + " (всего " + autoSellPlaced + ")");
        } else {
            dbg("слот " + target + " не принял предмет (Δ=" + (before - after) + ")");
        }
        delay(STEP_DELAY_MS);
    }

    // 7) подтверждение (slime_ball) + ожидание чат-подтверждения с ретраями
    private void tickAutoSellConfirm(long now) {
        if (!hasOpenContainer()) {
            dbg("окно закрыто перед подтверждением");
            finishAutoSellCycle();
            return;
        }
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (!handler.getCursorStack().isEmpty()) {
            depositCursorBack(handler);
            delay(STEP_DELAY_MS);
            return;
        }
        int confirm = findConfirmSlot(handler);
        confirmSeen = false;
        dbg("жму подтверждение (slime_ball), слот " + confirm);
        click(confirm, 0, SlotActionType.PICKUP, "sell_confirm");
        confirmRetries++;
        confirmClickedAt = now;
        state = State.AUTOSELL_WAIT_CONFIRM;
        delay(500L);
    }

    private void tickAutoSellWaitConfirm(long now) {
        if (confirmSeen) {
            dbg("успешно выставлено");
            finishAutoSellCycle();
            return;
        }
        if (now - confirmClickedAt > 3500L) {
            if (confirmRetries < 2 && hasOpenContainer()) {
                dbg("нет подтверждения, ретрай " + confirmRetries);
                state = State.AUTOSELL_CONFIRM;
                delay(300L);
            } else {
                dbg("подтверждение не получено — завершаю цикл");
                finishAutoSellCycle();
            }
            return;
        }
        delay(150L);
    }

    private void finishAutoSellCycle() {
        closeScreen();
        lastAutoSellAt = System.currentTimeMillis();
        openedCommandAt = 0L;
        state = State.IDLE;
        delay(700L);
    }

    private void tickWaitWindow() {
        if (hasOpenContainer()) {
            state = State.IDLE;
            return;
        }
        delay(100L);
    }

    // ---------- [FIX] Поиск верстака ----------
    private BlockPos findCraftingTable() {
        BlockPos configured = parseBlockPos(craftingTablePos.getText());
        if (configured != null && mc.world.getBlockState(configured).isOf(Blocks.CRAFTING_TABLE)) {
            return configured;
        }
        int r = (int) Math.ceil(craftingTableRadius.getValue());
        BlockPos origin = mc.player.getBlockPos();
        BlockPos found = null;
        double best = Double.MAX_VALUE;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos p = origin.add(dx, dy, dz);
                    if (mc.world.getBlockState(p).isOf(Blocks.CRAFTING_TABLE)) {
                        double dist = p.getSquaredDistance(mc.player.getPos());
                        if (dist < best) {
                            best = dist;
                            found = p.toImmutable();
                        }
                    }
                }
            }
        }
        return found;
    }

    private boolean interactBlock(BlockPos pos) {
        if (mc.interactionManager == null) return false;
        Vec3d center = Vec3d.ofCenter(pos);
        lookAt(center);
        BlockHitResult hit = new BlockHitResult(center, Direction.UP, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        return true;
    }

    private BlockPos parseBlockPos(String text) {
        if (text == null) return null;
        String[] parts = text.trim().split("[,\\s]+");
        if (parts.length < 3) return null;
        try {
            return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    // ---------- Helpers ----------
    private void dbg(String msg) {
        if (debugLog.isValue()) {
            ChatMessage.brandmessage("[AutoSell] " + msg);
        }
    }

    private void dumpContainer(String when) {
        if (!debugLog.isValue() || mc.player == null) return;
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (handler == null) return;
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            ItemStack stack = slot.getStack();
            if (stack != null && !stack.isEmpty()) {
                ChatMessage.brandmessage("[" + when + "] " + slot.id + " = " + stack.getName().getString());
            }
        }
    }

    // [FIX] эндер-сундук (хранилище лотов) в /ah
    private int findEnderChestSlot(ScreenHandler handler) {
        if (handler == null) return -1;
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            if (slot.getStack().isOf(Items.ENDER_CHEST)) return slot.id;
        }
        return -1;
    }

    // [FIX] сколько предметов в контейнере (не в инвентаре игрока)
    private int countContainerItems(ScreenHandler handler) {
        int n = 0;
        if (handler == null) return 0;
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            if (slot.hasStack()) n++;
        }
        return n;
    }

    // [FIX] пустой слот меню (не инвентарь игрока)
    private int findEmptySellSlot(ScreenHandler handler) {
        if (handler == null) return -1;
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            if (!slot.hasStack()) return slot.id;
        }
        return -1;
    }

    // [FIX] слот инвентаря игрока с чаркой
    private int findSellAppleInventorySlot(ScreenHandler handler) {
        if (handler == null) return -1;
        for (Slot slot : handler.slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            if (isSellApple(slot.getStack())) return slot.id;
        }
        return -1;
    }

    // [FIX] кнопка подтверждения = slime_ball (как в AutoSell)
    private int findConfirmSlot(ScreenHandler handler) {
        if (handler == null) return SELL_CONFIRM_FALLBACK_SLOT;
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            if (slot.getStack().isOf(Items.SLIME_BALL)) return slot.id;
        }
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) continue;
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            String name = stack.getName().getString().toLowerCase(Locale.ROOT);
            if (name.contains("продаж") || name.contains("подтвер") || name.contains("выстав")) return slot.id;
        }
        return SELL_CONFIRM_FALLBACK_SLOT;
    }

    // [FIX] вернуть остаток с курсора в инвентарь
    private void depositCursorBack(ScreenHandler handler) {
        if (handler.getCursorStack().isEmpty()) return;
        for (Slot slot : handler.slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            if (!slot.hasStack()) {
                click(slot.id, 0, SlotActionType.PICKUP, "deposit_back_empty");
                return;
            }
        }
        ItemStack cursor = handler.getCursorStack();
        for (Slot slot : handler.slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.getItem() == cursor.getItem()) {
                click(slot.id, 0, SlotActionType.PICKUP, "deposit_back_merge");
                return;
            }
        }
    }

    private boolean isVillagerReady(MerchantEntity merchant) {
        if (merchant == null || merchant.isRemoved() || !merchant.isAlive()) return false;
        if (merchant.squaredDistanceTo(mc.player) > villagerRadius.getValue() * villagerRadius.getValue()) return false;
        return !villagerCooldowns.containsKey(villagerKey(merchant));
    }

    private int findGoldForEmeraldTrade(MerchantScreenHandler handler) {
        for (int i = 0; i < handler.getRecipes().size(); i++) {
            TradeOffer offer = handler.getRecipes().get(i);
            if (offer == null || offer.isDisabled()) continue;
            if (!isItem(offer.getSellItem(), Items.GOLD_INGOT)) continue;
            if (isItem(offer.getDisplayedFirstBuyItem(), Items.EMERALD) || isItem(offer.getDisplayedSecondBuyItem(), Items.EMERALD)) {
                return i;
            }
        }
        return -1;
    }

    private int emeraldCost(TradeOffer offer) {
        int cost = 0;
        if (isItem(offer.getDisplayedFirstBuyItem(), Items.EMERALD)) cost += offer.getDisplayedFirstBuyItem().getCount();
        if (isItem(offer.getDisplayedSecondBuyItem(), Items.EMERALD)) cost += offer.getDisplayedSecondBuyItem().getCount();
        return cost;
    }

    private boolean isItem(ItemStack stack, Item item) {
        return stack != null && !stack.isEmpty() && stack.isOf(item);
    }

    private int countItem(Item item) {
        int count = 0;
        for (ItemStack stack : mc.player.getInventory().main) {
            if (isItem(stack, item)) count += stack.getCount();
        }
        return count;
    }

    private int countSellApples() {
        int count = 0;
        for (ItemStack stack : mc.player.getInventory().main) {
            if (isSellApple(stack)) count += stack.getCount();
        }
        return count;
    }

    private boolean isSellApple(ItemStack stack) {
        return isItem(stack, Items.ENCHANTED_GOLDEN_APPLE) || isItem(stack, Items.GOLDEN_APPLE);
    }

    private boolean hasOpenContainer() {
        return mc.currentScreen instanceof GenericContainerScreen || mc.player.currentScreenHandler != mc.player.playerScreenHandler;
    }

    private int containerEnd(ScreenHandler handler) {
        if (handler == null || handler.slots == null) return 0;
        int size = handler.slots.size();
        return Math.max(0, size - 36);
    }

    private int findContainerSlotByItem(ScreenHandler handler, Item item) {
        if (handler == null) return -1;
        int end = containerEnd(handler);
        for (int i = 0; i < end && i < handler.slots.size(); i++) {
            if (handler.slots.get(i).getStack().isOf(item)) return i;
        }
        return -1;
    }

    private int findContainerSlotByItemOrText(ScreenHandler handler, Item item, String... words) {
        int direct = findContainerSlotByItem(handler, item);
        if (direct >= 0) return direct;
        if (handler == null) return -1;
        int end = containerEnd(handler);
        for (int i = 0; i < end && i < handler.slots.size(); i++) {
            ItemStack stack = handler.slots.get(i).getStack();
            String text = stackText(stack).toLowerCase(Locale.ROOT);
            for (String word : words) {
                if (text.contains(word.toLowerCase(Locale.ROOT))) return i;
            }
        }
        return -1;
    }

    private int findPlayerSlotInHandler(ScreenHandler handler, Item item) {
        if (handler == null) return -1;
        int start = containerEnd(handler);
        for (int i = start; i < handler.slots.size(); i++) {
            if (handler.slots.get(i).getStack().isOf(item)) return i;
        }
        return -1;
    }

    private String stackText(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        builder.append(stack.getName().getString()).append(' ');
        try {
            for (Text line : stack.getTooltip(Item.TooltipContext.DEFAULT, mc.player, net.minecraft.item.tooltip.TooltipType.BASIC)) {
                builder.append(line.getString()).append(' ');
            }
        } catch (Exception ignored) {
        }
        return builder.toString();
    }

    private void click(int slot, int button, SlotActionType action, String reason) {
        if (slot < 0 || mc.interactionManager == null || mc.player == null) return;
        try {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, button, action, mc.player);
        } catch (Exception exception) {
            ChatMessage.brandmessage("Click fail " + reason + ": " + exception.getMessage());
        }
    }

    private void sendCommand(String command) {
        if (mc.player == null || mc.player.networkHandler == null || command == null) return;
        String trimmed = command.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        // [FIX] глушим донат-команды, когда они выключены
        if (!useDonateCommands.isValue()) {
            for (String cmd : DONATE_COMMANDS) {
                if (lower.equals(cmd) || lower.startsWith(cmd + " ")) {
                    ChatMessage.brandmessage("Донат-команда " + trimmed + " заблокирована (донат-команды выключены)");
                    return;
                }
            }
        }
        closeChatIfOpen();
        if (trimmed.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(trimmed.substring(1));
        } else {
            mc.player.networkHandler.sendChatMessage(trimmed);
        }
    }

    private void closeChatIfOpen() {
        if (mc.currentScreen instanceof ChatScreen) {
            mc.currentScreen.close();
        }
    }

    private void closeScreen() {
        try {
            if (mc.currentScreen != null) mc.currentScreen.close();
            else if (mc.player != null) mc.player.closeHandledScreen();
        } catch (Exception ignored) {
        }
    }

    private void lookAt(Vec3d target) {
        Vec3d eyes = mc.player.getEyePos();
        double dx = target.x - eyes.x;
        double dy = target.y - eyes.y;
        double dz = target.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (MathHelper.atan2(dz, dx) * 57.2957763671875D) - 90.0F;
        float pitch = (float) -(MathHelper.atan2(dy, dist) * 57.2957763671875D);
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    private void stopMove() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sprintKey.setPressed(false);
    }

    private String villagerKey(MerchantEntity merchant) {
        return merchant.getBlockPos().getX() + "," + merchant.getBlockPos().getY() + "," + merchant.getBlockPos().getZ();
    }

    private void rememberVillagerCooldown() {
        if (targetVillager != null) {
            villagerCooldowns.put(villagerKey(targetVillager), System.currentTimeMillis());
        }
        targetVillager = null;
        selectedTrade = -1;
    }

    private void cleanupVillagerCooldowns(long now) {
        villagerCooldowns.entrySet().removeIf(entry -> now - entry.getValue() > TRADE_COOLDOWN_MS);
    }

    private int readPrice() {
        try {
            String raw = autoSellPrice.getText().replaceAll("[^0-9]", "");
            if (raw.isBlank()) return 0;
            return Integer.parseInt(raw);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void delay(long ms) {
        nextActionAt = System.currentTimeMillis() + Math.max(0L, ms);
    }

    private void resetAutoSellCycle() {
        autoSellPlaced = 0;
        confirmSeen = false;
        confirmRetries = 0;
        ahSyncId = -1;
        listingsClicksLeft = 0;
        storageClickedAt = 0L;
        confirmClickedAt = 0L;
    }

    private void resetState() {
        stopMove();
        state = State.IDLE;
        targetVillager = null;
        selectedTrade = -1;
        craftPhase = 0;
        openedCommandAt = 0L;
        lastInvestAt = 0L; // [FIX]
        targetCraftingTable = null; // [FIX]
        craftReturnState = State.IDLE;
        craftWalkStartedAt = 0L;
        resetAutoSellCycle(); // [FIX]
        nextActionAt = 0L;
    }

    private enum State {
        IDLE,
        SHOP_WITHDRAW, // [FIX]
        SHOP_OPEN,
        SHOP_CLICK_GOLD,
        SHOP_CLICK_EMERALD,
        SHOP_CONFIRM,
        FIND_VILLAGER,
        WALK_TO_VILLAGER,
        OPEN_VILLAGER,
        TRADE_VILLAGER,
        OPEN_CRAFT_BLOCKS,
        WALK_TO_CRAFTING_TABLE, // [FIX]
        CRAFT_GOLD_BLOCKS,
        OPEN_APPLE_STORAGE,
        TAKE_APPLES,
        OPEN_CRAFT_EAPPLES,
        CRAFT_EAPPLES,
        AUTOSELL_OPEN_AH,
        AUTOSELL_OPEN_STORAGE,
        AUTOSELL_WAIT_LISTINGS, // [FIX]
        AUTOSELL_REMOVE_OLD,
        AUTOSELL_OPEN_SELLGUI,
        AUTOSELL_PLACE_ITEMS,
        AUTOSELL_CONFIRM,
        AUTOSELL_WAIT_CONFIRM, // [FIX]
        WAIT_WINDOW
    }
}