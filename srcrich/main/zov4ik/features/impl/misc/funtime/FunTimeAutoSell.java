package im.zov4ik.features.impl.misc.funtime;

import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.originalitems.ItemRegistry;
import im.zov4ik.features.impl.misc.autobuy.catalog.util.AuctionUtils;
import im.zov4ik.features.impl.misc.funtime.util.FunTimeAutoSellUtil;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.math.time.TimerUtil;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.Slot;

public class FunTimeAutoSell extends Module {
    private final SliderSettings delay = new SliderSettings("Задержка", "Задержка между действиями (мс)").range(100, 2000);

    private final TimerUtil timer = TimerUtil.create();

    private SellState state = SellState.IDLE;
    private int originalItemSlot = -1;
    private String currentItemName = "";
    private double lowestPricePerItem = Double.MAX_VALUE;
    private int minPricePerItem = 0;
    private ItemStack sellingStack = ItemStack.EMPTY;

    private enum SellState {
        IDLE,
        SELECTING_ITEM,
        MOVING_TO_HOTBAR,
        SEARCHING,
        WAITING_GUI,
        SCANNING,
        SELLING,
        RETURNING_ITEM
    }

    public FunTimeAutoSell() {
        super("FunTimeAutoSell", "FunTime AutoSell", ModuleCategory.MISC);
        delay.setValue(500f);
        setup(delay);
    }

    @Override
    public void activate() {
        super.activate();
        state = SellState.SELECTING_ITEM;
        originalItemSlot = -1;
        timer.resetCounter();
        msg("§aFunTime AutoSell включён");
    }

    @Override
    public void deactivate() {
        super.deactivate();
        state = SellState.IDLE;
        msg("§cFunTime AutoSell выключен");
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!isState()) return;
        if (!timer.hasTimeElapsed((long) delay.getValue())) return;

        switch (state) {
            case SELECTING_ITEM -> handleSelectingItem();
            case MOVING_TO_HOTBAR -> handleMovingToHotbar();
            case SEARCHING -> handleSearching();
            case WAITING_GUI -> handleWaitingGui();
            case SCANNING -> handleScanning();
            case SELLING -> handleSelling();
            case RETURNING_ITEM -> handleReturningItem();
            default -> {}
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!isState()) return;
        if (event.getType() != PacketEvent.Type.RECEIVE) return;
        if (!(event.getPacket() instanceof GameMessageS2CPacket packet)) return;

        String text = packet.content().getString();
        if (FunTimeAutoSellUtil.isErrorMessage(text)) {
            if (state == SellState.WAITING_GUI || state == SellState.SEARCHING) {
                msg("§cПредмет не распознан сервером, возвращаю...");
                state = SellState.RETURNING_ITEM;
                timer.resetCounter();
            }
        }
    }

    private void handleSelectingItem() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            originalItemSlot = i;
            sellingStack = stack.copy();
            currentItemName = stack.getName().getString()
                    .replaceAll("§.", "")
                    .replace("[★]", "")
                    .replace("[⚒]", "")
                    .replace("[❄]", "")
                    .replace("[🍹]", "")
                    .trim();

            minPricePerItem = 0;
            for (AutoBuyableItem item : ItemRegistry.getFunTimeItems()) {
                if (AuctionUtils.compareItem(stack, item.createItemStack())) {
                    minPricePerItem = item.getSettings().getBuyBelow();
                    break;
                }
            }

            mc.player.getInventory().setSelectedSlot(0);

            if (i == 0) {
                state = SellState.SEARCHING;
            } else {
                state = SellState.MOVING_TO_HOTBAR;
            }

            timer.resetCounter();
            return;
        }

        msg("§aВсе предметы проданы!");
        switchState();
    }

    private void handleMovingToHotbar() {
        int slotInHandler = originalItemSlot;
        if (originalItemSlot < 9) {
            slotInHandler = originalItemSlot + 36;
        }

        FunTimeAutoSellUtil.moveToFirstSlot(slotInHandler);
        state = SellState.SEARCHING;
        timer.resetCounter();
    }

    private void handleSearching() {
        if (mc.player == null || mc.player.networkHandler == null) return;
        mc.player.networkHandler.sendChatCommand("ah search " + currentItemName);
        state = SellState.WAITING_GUI;
        timer.resetCounter();
    }

    private void handleWaitingGui() {
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            String title = screen.getTitle().getString().toLowerCase();
            if (title.contains("аукцион") || title.contains("поиск") || title.contains("search")) {
                state = SellState.SCANNING;
                lowestPricePerItem = Double.MAX_VALUE;
                timer.resetCounter();
                return;
            }
        }

        if (timer.hasTimeElapsed(5000)) {
            state = SellState.RETURNING_ITEM;
            timer.resetCounter();
        }
    }

    private void handleScanning() {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            state = SellState.RETURNING_ITEM;
            return;
        }

        boolean foundAny = false;
        int slotsToCheck = Math.min(45, screen.getScreenHandler().slots.size());
        for (int i = 0; i < slotsToCheck; i++) {
            Slot slot = screen.getScreenHandler().slots.get(i);
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            if (!AuctionUtils.compareItem(stack, sellingStack)) continue;

            int price = AuctionUtils.getPrice(stack);
            if (price > 0) {
                double pricePerItem = (double) price / Math.max(1, stack.getCount());
                if (pricePerItem < lowestPricePerItem) {
                    lowestPricePerItem = pricePerItem;
                    foundAny = true;
                }
            }
        }

        if (foundAny) {
            mc.player.closeHandledScreen();
            state = SellState.SELLING;
        } else {
            msg("§cТовар не найден на аукционе, возвращаю в инвентарь");
            mc.player.closeHandledScreen();
            state = SellState.RETURNING_ITEM;
        }
        timer.resetCounter();
    }

    private void handleSelling() {
        mc.player.getInventory().setSelectedSlot(0);
        ItemStack stack = mc.player.getInventory().getStack(0);
        if (stack.isEmpty()) {
            state = SellState.SELECTING_ITEM;
            return;
        }

        int count = stack.getCount();
        int sellPrice = (int) (lowestPricePerItem * count) - 1;
        int minTotalSellPrice = minPricePerItem * count;

        if (sellPrice < minTotalSellPrice) {
            sellPrice = minTotalSellPrice;
        }
        if (sellPrice <= 0) sellPrice = 1;

        FunTimeAutoSellUtil.sellItem(sellPrice, count);
        msg("§aВыставил §f" + currentItemName + " §7(x" + count + ") §aза §e" + sellPrice + "$");

        state = SellState.SELECTING_ITEM;
        timer.resetCounter();
    }

    private void handleReturningItem() {
        if (mc.currentScreen != null) {
            mc.player.closeHandledScreen();
        }
        FunTimeAutoSellUtil.putBack(0, originalItemSlot);
        msg("§7Предмет §f" + currentItemName + " §7возвращён на место");

        state = SellState.SELECTING_ITEM;
        timer.resetCounter();
    }

    private void msg(String text) {
        ChatMessage.brandmessage(text);
    }
}
