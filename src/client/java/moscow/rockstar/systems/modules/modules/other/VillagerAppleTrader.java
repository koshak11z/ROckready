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
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ModuleInfo(name = "VillagerAppleTrader", category = ModuleCategory.OTHER, desc = "Auto emerald/shop, villager trades, apple craft and autosell")
public class VillagerAppleTrader extends BaseModule {
    private static final long STEP_DELAY_MS = 280L;
    private static final long COMMAND_WAIT_MS = 900L;
    private static final long WINDOW_WAIT_MS = 3500L;
    private static final long TRADE_COOLDOWN_MS = 30_000L;
    private static final long AUTOSELL_INTERVAL_MS = 40_000L;
    private static final long INVEST_INTERVAL_MS = 60_000L;
    private static final int SHOP_GOLD_FALLBACK_SLOT = 11;
    private static final int SHOP_EMERALD_FALLBACK_SLOT = 15;
    private static final int SHOP_CONFIRM_SLOT = 2;
    private static final int SELL_CONFIRM_FALLBACK_SLOT = 25;
    private static final int CRAFT_OUTPUT_SLOT = 0;
    private static final int[] CRAFT_GRID_SLOTS = new int[]{1,2,3,4,5,6,7,8,9};

    private final BooleanSetting autoBuyEmeralds = new BooleanSetting(this, "Покупать изумруды").enable();
    private final BooleanSetting tradeVillagers = new BooleanSetting(this, "Торговать с жителями").enable();
    private final BooleanSetting craftGoldBlocks = new BooleanSetting(this, "Крафтить блоки золота").enable();
    private final BooleanSetting takeApples = new BooleanSetting(this, "Брать яблоки").enable();
    private final BooleanSetting craftEnchantedApples = new BooleanSetting(this, "Крафтить чарки").enable();
    private final BooleanSetting autoSellApples = new BooleanSetting(this, "Авто продажа чарок");
    private final BooleanSetting autoInvest = new BooleanSetting(this, "Авто инвест");
    private final BooleanSetting useDonateCommands = new BooleanSetting(this, "Донат-команды").enable();
    private final BooleanSetting withdrawForEmeralds = new BooleanSetting(this, "Снимать с клана на изумруды").enable();
    private final BooleanSetting withdrawStorageFirst = new BooleanSetting(this, "Снимать лоты из хранилища").enable();
    private final BooleanSetting relistOnBuy = new BooleanSetting(this, "Перевыставлять после покупки").enable();
    private final BooleanSetting debugLog = new BooleanSetting(this, "Лог авто-селла").enable();
    private final SliderSetting minEmeralds = new SliderSetting(this, "Мин. изумрудов").min(0.0f).max(256.0f).step(1.0f).currentValue(20.0f);
    private final SliderSetting villagerRadius = new SliderSetting(this, "Радиус жителей").min(4.0f).max(96.0f).step(1.0f).currentValue(48.0f);
    private final SliderSetting autoSellSlots = new SliderSetting(this, "Слотов продажи").min(1.0f).max(45.0f).step(1.0f).currentValue(8.0f);
    private final SliderSetting emeraldPrice = new SliderSetting(this, "Цена изумруда").min(1.0f).max(100000.0f).step(1.0f).currentValue(5000.0f);
    private final SliderSetting craftingTableRadius = new SliderSetting(this, "Радиус верстака").min(1.0f).max(16.0f).step(1.0f).currentValue(5.0f);
    private final SliderSetting investAmount = new SliderSetting(this, "Сумма инвеста").min(1.0f).max(100000000.0f).step(1.0f).currentValue(100000.0f);
    private final StringSetting autoSellPrice = new StringSetting(this, "Цена чарки").text("169999");
    private final StringSetting craftingTablePos = new StringSetting(this, "Координата верстака").text("");

    private State state = State.IDLE;
    private long nextActionAt;
    private long openedCommandAt;
    private long lastAutoSellAt;
    private long lastInvestAt;
    private MerchantEntity targetVillager;
    private int selectedTrade = -1;
    private int craftPhase;
    private int autoSellPlaced;
    private final Map<String, Long> villagerCooldowns = new HashMap<>();
    private final Timer timer = new Timer();

    private enum State {
        IDLE, SHOP_WITHDRAW, SHOP_OPEN, SHOP_CLICK_GOLD, SHOP_CLICK_EMERALD, SHOP_CONFIRM,
        FIND_VILLAGER, OPEN_VILLAGER, SELECT_TRADE, TAKE_TRADE_RESULT,
        CRAFT_GOLD_BLOCKS, TAKE_APPLES, CRAFT_ENCHANTED_APPLES,
        AUTOSELL_OPEN, AUTOSELL_PLACE, AUTOSELL_CONFIRM, AUTOSELL_COMMAND,
        INVEST, WALK_TO_CRAFTING_TABLE
    }

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.currentScreen instanceof ChatScreen) return;
        long now = System.currentTimeMillis();
        if (now < this.nextActionAt) return;
        this.villagerCooldowns.entrySet().removeIf(e -> e.getValue() <= now);
        switch (this.state) {
            case IDLE -> tickIdle(now);
            case SHOP_WITHDRAW -> tickShopWithdraw();
            case SHOP_OPEN -> tickShopOpen(now);
            case SHOP_CLICK_GOLD -> tickShopClickGold();
            case SHOP_CLICK_EMERALD -> tickShopClickEmerald();
            case SHOP_CONFIRM -> tickShopConfirm();
            case FIND_VILLAGER -> tickFindVillager();
            case OPEN_VILLAGER -> tickOpenVillager(now);
            case SELECT_TRADE -> tickSelectTrade();
            case TAKE_TRADE_RESULT -> tickTakeTradeResult();
            case CRAFT_GOLD_BLOCKS -> tickCraftGoldBlocks();
            case TAKE_APPLES -> tickTakeApples(now);
            case CRAFT_ENCHANTED_APPLES -> tickCraftEnchantedApples();
            case AUTOSELL_OPEN -> tickAutoSellOpen(now);
            case AUTOSELL_PLACE -> tickAutoSellPlace();
            case AUTOSELL_CONFIRM -> tickAutoSellConfirm();
            case AUTOSELL_COMMAND -> tickAutoSellCommand();
            case INVEST -> tickInvest();
            case WALK_TO_CRAFTING_TABLE -> tickWalkToCraftingTable();
        }
    };

    private final EventListener<ReceivePacketEvent> onPacket = event -> {
        if (!(event.getPacket() instanceof GameMessageS2CPacket packet)) return;
        String low = packet.content().getString().toLowerCase();
        if (this.relistOnBuy.isEnabled() && (low.contains("у вас купили") || low.contains("купил ваш") || low.contains("sold"))) {
            this.state = State.AUTOSELL_OPEN;
            this.openedCommandAt = 0L;
            this.delay(500L);
        }
    };

    private void tickIdle(long now) {
        if (this.autoInvest.isEnabled() && now - this.lastInvestAt > INVEST_INTERVAL_MS) { this.state = State.INVEST; return; }
        if (this.autoBuyEmeralds.isEnabled() && countItem(Items.EMERALD) < Math.round(this.minEmeralds.getCurrentValue())) { this.state = this.withdrawForEmeralds.isEnabled() ? State.SHOP_WITHDRAW : State.SHOP_OPEN; return; }
        if (this.tradeVillagers.isEnabled() && countItem(Items.EMERALD) > 0) { this.state = State.FIND_VILLAGER; return; }
        if (this.craftGoldBlocks.isEnabled() && countItem(Items.GOLD_INGOT) >= 9) { this.state = State.CRAFT_GOLD_BLOCKS; return; }
        if (this.takeApples.isEnabled() && countItem(Items.APPLE) < 8) { this.state = State.TAKE_APPLES; this.openedCommandAt = 0L; return; }
        if (this.craftEnchantedApples.isEnabled() && countItem(Items.APPLE) > 0 && countItem(Items.GOLD_BLOCK) >= 8) { this.state = State.CRAFT_ENCHANTED_APPLES; return; }
        if (this.autoSellApples.isEnabled() && countItem(Items.ENCHANTED_GOLDEN_APPLE) > 0 && now - this.lastAutoSellAt > AUTOSELL_INTERVAL_MS) { this.state = State.AUTOSELL_OPEN; this.openedCommandAt = 0L; return; }
        this.delay(700L);
    }

    private void tickShopWithdraw() {
        long stackCost = (long)Math.ceil(this.emeraldPrice.getCurrentValue()) * 64L;
        this.sendCommand("clan withdraw " + stackCost);
        this.state = State.SHOP_OPEN;
        this.delay(COMMAND_WAIT_MS);
    }

    private void tickShopOpen(long now) {
        if (hasOpenContainer()) { this.state = State.SHOP_CLICK_GOLD; this.delay(250L); return; }
        if (this.openedCommandAt == 0L || now - this.openedCommandAt > WINDOW_WAIT_MS) {
            this.sendCommand("shop");
            this.openedCommandAt = now;
            this.delay(COMMAND_WAIT_MS);
        } else this.delay(150L);
    }

    private void tickShopClickGold() {
        ScreenHandler handler = mc.player.currentScreenHandler;
        int slot = findContainerSlotByItem(handler, Items.GOLD_INGOT);
        if (slot < 0) slot = SHOP_GOLD_FALLBACK_SLOT;
        click(slot, 0, SlotActionType.PICKUP, "shop_gold");
        this.state = State.SHOP_CLICK_EMERALD;
        this.delay(650L);
    }

    private void tickShopClickEmerald() {
        ScreenHandler handler = mc.player.currentScreenHandler;
        int before = countItem(Items.EMERALD);
        int slot = findContainerSlotByItemOrText(handler, Items.EMERALD, "изумруд", "emerald");
        if (slot < 0) slot = SHOP_EMERALD_FALLBACK_SLOT;
        click(slot, 1, SlotActionType.PICKUP, "shop_emerald_rclick");
        this.state = countItem(Items.EMERALD) > before ? State.IDLE : State.SHOP_CONFIRM;
        this.delay(700L);
    }

    private void tickShopConfirm() {
        if (hasOpenContainer()) click(SHOP_CONFIRM_SLOT, 0, SlotActionType.PICKUP, "shop_confirm");
        closeScreen();
        this.state = State.IDLE;
        this.openedCommandAt = 0L;
        this.delay(500L);
    }

    private void tickFindVillager() {
        double radius = this.villagerRadius.getCurrentValue();
        Optional<MerchantEntity> nearest = mc.world.getEntitiesByClass(MerchantEntity.class, mc.player.getBoundingBox().expand(radius), this::isVillagerReady).stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)));
        if (nearest.isEmpty()) { this.state = State.IDLE; this.delay(1500L); return; }
        this.targetVillager = nearest.get();
        this.state = State.OPEN_VILLAGER;
    }

    private void tickOpenVillager(long now) {
        if (mc.player.currentScreenHandler instanceof MerchantScreenHandler) { this.state = State.SELECT_TRADE; this.delay(200L); return; }
        if (this.targetVillager == null || !this.targetVillager.isAlive()) { this.state = State.IDLE; return; }
        mc.interactionManager.interactEntity(mc.player, this.targetVillager, Hand.MAIN_HAND);
        this.openedCommandAt = now;
        this.delay(600L);
    }

    private void tickSelectTrade() {
        if (!(mc.player.currentScreenHandler instanceof MerchantScreenHandler handler)) { this.state = State.IDLE; return; }
        this.selectedTrade = findGoldForEmeraldTrade(handler);
        if (this.selectedTrade < 0) { cooldownVillager(); closeScreen(); this.state = State.IDLE; this.delay(400L); return; }
        mc.player.networkHandler.sendPacket(new SelectMerchantTradeC2SPacket(this.selectedTrade));
        this.state = State.TAKE_TRADE_RESULT;
        this.delay(300L);
    }

    private void tickTakeTradeResult() {
        if (!(mc.player.currentScreenHandler instanceof MerchantScreenHandler handler)) { this.state = State.IDLE; return; }
        if (handler.getSlot(2).hasStack()) click(2, 0, SlotActionType.QUICK_MOVE, "trade_result");
        TradeOffer offer = handler.getRecipes().get(this.selectedTrade);
        if (offer == null || offer.isDisabled() || countItem(Items.EMERALD) <= 0) {
            cooldownVillager(); closeScreen(); this.state = State.IDLE;
        }
        this.delay(STEP_DELAY_MS);
    }

    private void tickCraftGoldBlocks() {
        if (this.useDonateCommands.isEnabled()) {
            this.sendCommand("craft");
            this.delay(COMMAND_WAIT_MS);
        } else if (!(mc.player.currentScreenHandler instanceof net.minecraft.screen.CraftingScreenHandler)) {
            BlockPos table = findCraftingTable();
            if (table == null) { this.state = State.IDLE; return; }
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(table.toCenterPos(), Direction.UP, table, false));
            this.delay(500L);
            return;
        }
        if (hasOpenContainer()) {
            int slot = findInventorySlot(Items.GOLD_INGOT);
            for (int grid : CRAFT_GRID_SLOTS) if (slot >= 0) click(slot, 0, SlotActionType.PICKUP, "gold_pick");
            if (mc.player.currentScreenHandler.getSlot(CRAFT_OUTPUT_SLOT).hasStack()) click(CRAFT_OUTPUT_SLOT, 0, SlotActionType.QUICK_MOVE, "gold_block_result");
        }
        closeScreen(); this.state = State.IDLE; this.delay(500L);
    }

    private void tickTakeApples(long now) {
        if (hasOpenContainer()) {
            int slot = findContainerSlotByItem(mc.player.currentScreenHandler, Items.APPLE);
            if (slot >= 0) click(slot, 0, SlotActionType.QUICK_MOVE, "take_apples");
            closeScreen(); this.state = State.IDLE; this.delay(600L); return;
        }
        if (this.openedCommandAt == 0L || now - this.openedCommandAt > WINDOW_WAIT_MS) {
            this.sendCommand("clan storage");
            this.openedCommandAt = now;
            this.delay(COMMAND_WAIT_MS);
        } else this.delay(150L);
    }

    private void tickCraftEnchantedApples() {
        if (this.useDonateCommands.isEnabled()) {
            this.sendCommand("craft");
            this.delay(COMMAND_WAIT_MS);
        }
        // Рецепт в GUI/верстаке серверный: кладём яблоко + gold blocks в сетку и забираем output, если появился.
        if (hasOpenContainer()) {
            int apple = findInventorySlot(Items.APPLE);
            int goldBlock = findInventorySlot(Items.GOLD_BLOCK);
            if (apple >= 0) click(apple, 0, SlotActionType.PICKUP, "apple_pick");
            if (goldBlock >= 0) for (int ignored : CRAFT_GRID_SLOTS) click(goldBlock, 0, SlotActionType.PICKUP, "goldblock_grid");
            if (mc.player.currentScreenHandler.getSlot(CRAFT_OUTPUT_SLOT).hasStack()) click(CRAFT_OUTPUT_SLOT, 0, SlotActionType.QUICK_MOVE, "egap_result");
        }
        closeScreen(); this.state = State.IDLE; this.delay(700L);
    }

    private void tickAutoSellOpen(long now) {
        if (hasOpenContainer()) { this.autoSellPlaced = 0; this.state = State.AUTOSELL_PLACE; this.delay(200L); return; }
        if (this.withdrawStorageFirst.isEnabled()) this.sendCommand("ah storage");
        this.sendCommand("ah sellgui");
        this.openedCommandAt = now;
        this.delay(COMMAND_WAIT_MS);
    }

    private void tickAutoSellPlace() {
        int slot = findInventorySlot(Items.ENCHANTED_GOLDEN_APPLE);
        if (slot >= 0 && this.autoSellPlaced < this.autoSellSlots.getCurrentValue()) {
            click(slot, 0, SlotActionType.QUICK_MOVE, "autosell_place");
            this.autoSellPlaced++;
            this.delay(120L);
            return;
        }
        this.state = State.AUTOSELL_CONFIRM;
    }

    private void tickAutoSellConfirm() {
        if (hasOpenContainer()) click(SELL_CONFIRM_FALLBACK_SLOT, 0, SlotActionType.PICKUP, "autosell_confirm");
        this.state = State.AUTOSELL_COMMAND;
        this.delay(300L);
    }

    private void tickAutoSellCommand() {
        this.sendCommand("ah sell " + this.autoSellPrice.getText());
        this.lastAutoSellAt = System.currentTimeMillis();
        this.state = State.IDLE;
        this.delay(1000L);
    }

    private void tickInvest() {
        this.sendCommand("clan invest " + (long)this.investAmount.getCurrentValue());
        this.lastInvestAt = System.currentTimeMillis();
        this.state = State.IDLE;
        this.delay(1500L);
    }

    private void tickWalkToCraftingTable() { this.state = State.IDLE; }

    private boolean isVillagerReady(MerchantEntity merchant) {
        if (merchant == null || merchant.isRemoved() || !merchant.isAlive()) return false;
        if (merchant.squaredDistanceTo(mc.player) > this.villagerRadius.getCurrentValue() * this.villagerRadius.getCurrentValue()) return false;
        return !this.villagerCooldowns.containsKey(villagerKey(merchant));
    }

    private int findGoldForEmeraldTrade(MerchantScreenHandler handler) {
        for (int i = 0; i < handler.getRecipes().size(); i++) {
            TradeOffer offer = handler.getRecipes().get(i);
            if (offer == null || offer.isDisabled()) continue;
            if (!isItem(offer.getSellItem(), Items.GOLD_INGOT)) continue;
            if (isItem(offer.getDisplayedFirstBuyItem(), Items.EMERALD) || isItem(offer.getDisplayedSecondBuyItem(), Items.EMERALD)) return i;
        }
        return -1;
    }

    private int findContainerSlotByItem(ScreenHandler handler, Item item) {
        if (handler == null) return -1;
        for (Slot slot : handler.slots) if (slot.hasStack() && slot.getStack().isOf(item)) return slot.id;
        return -1;
    }

    private int findContainerSlotByItemOrText(ScreenHandler handler, Item item, String... words) {
        if (handler == null) return -1;
        for (Slot slot : handler.slots) {
            if (!slot.hasStack()) continue;
            ItemStack stack = slot.getStack();
            if (stack.isOf(item)) return slot.id;
            String name = stack.getName().getString().toLowerCase();
            for (String word : words) if (name.contains(word.toLowerCase())) return slot.id;
        }
        return -1;
    }

    private int findInventorySlot(Item item) {
        if (mc.player == null) return -1;
        for (Slot slot : mc.player.currentScreenHandler.slots) if (slot.hasStack() && slot.getStack().isOf(item)) return slot.id;
        return -1;
    }

    private BlockPos findCraftingTable() {
        String raw = this.craftingTablePos.getText();
        if (raw != null && !raw.isBlank()) {
            String[] parts = raw.trim().split("\\s+");
            if (parts.length == 3) try { return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])); } catch (Exception ignored) {}
        }
        int radius = (int)this.craftingTableRadius.getCurrentValue();
        BlockPos base = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(base.add(-radius, -2, -radius), base.add(radius, 2, radius))) if (mc.world.getBlockState(pos).isOf(Blocks.CRAFTING_TABLE)) return pos.toImmutable();
        return null;
    }

    private boolean hasOpenContainer() { return mc.currentScreen instanceof GenericContainerScreen || mc.player.currentScreenHandler != mc.player.playerScreenHandler; }
    private boolean isItem(ItemStack stack, Item item) { return stack != null && !stack.isEmpty() && stack.isOf(item); }
    private int countItem(Item item) { int c = 0; for (ItemStack stack : mc.player.getInventory().main) if (isItem(stack, item)) c += stack.getCount(); return c; }
    private void click(int slot, int button, SlotActionType action, String reason) { if (this.debugLog.isEnabled()) MessageUtility.info(Text.of("§7[Trader] " + reason + " slot=" + slot)); mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, button, action, mc.player); }
    private void sendCommand(String command) { mc.player.networkHandler.sendChatCommand(command.startsWith("/") ? command.substring(1) : command); }
    private void closeScreen() { mc.player.closeHandledScreen(); }
    private void cooldownVillager() { if (this.targetVillager != null) this.villagerCooldowns.put(villagerKey(this.targetVillager), System.currentTimeMillis() + TRADE_COOLDOWN_MS); this.targetVillager = null; }
    private String villagerKey(MerchantEntity merchant) { Vec3d p = merchant.getPos(); return merchant.getUuidAsString() + "@" + (int)p.x + ":" + (int)p.y + ":" + (int)p.z; }
    private void delay(long ms) { this.nextActionAt = System.currentTimeMillis() + ms; }

    @Override
    public void onEnable() { this.state = State.IDLE; this.nextActionAt = 0L; super.onEnable(); }
    @Override
    public void onDisable() { this.state = State.IDLE; super.onDisable(); }
}
