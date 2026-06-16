package im.zov4ik.features.impl.misc.funtime;

import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.impl.misc.AutoBuy;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.originalitems.ItemRegistry;
import im.zov4ik.features.impl.misc.autobuy.catalog.settings.AutoBuySettingsManager;
import im.zov4ik.features.impl.misc.autobuy.catalog.util.AuctionUtils;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.client.managers.file.impl.AutoBuyConfigFile;
import im.zov4ik.utils.client.managers.file.impl.ModuleFile;
import im.zov4ik.utils.math.time.StopWatch;
import im.zov4ik.utils.math.time.TimerUtil;
import im.zov4ik.zov4ik;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunTimeAutoParser extends Module {
    private static final Pattern PAGE_PATTERN = Pattern.compile("\\[(\\d+)/(\\d+)]");

    private static final int MAX_PAGES_TO_SCAN = 5;
    private static final long ANTI_AFK_INTERVAL = 20000L;
    private static final long PAGE_CLICK_DELAY = 200L;
    private static final long CHECK_INTERVAL = 75L;

    private final SliderSettings discountPercent = new SliderSettings("Скидка %", "Процент скидки от мин. цены").range(0, 99);
    private final SliderSettings commandDelayMs = new SliderSettings("Задержка команды (мс)", "Задержка перед /ah search").range(50, 1000);
    private final SliderSettings maxRetries = new SliderSettings("Попыток", "Сколько раз пытаться при таймауте").range(1, 10);

    private final TimerUtil actionTimer = TimerUtil.create();
    private final TimerUtil commandTimer = TimerUtil.create();
    private final StopWatch antiAfkWatch = new StopWatch();

    private ParserState state = ParserState.IDLE;

    private int currentItemIndex = 0;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearchItem = "";
    private String[] currentAutoBuyNames = new String[0];
    private final List<AutoParserItems.ParserItemEntry> activeParserItems = new ArrayList<>();
    private final Map<String, Integer> lowestPricesFound = new HashMap<>();

    private int updatedCount = 0;
    private int skippedCount = 0;
    private int waitAttempts = 0;
    private int antiAfkAction = 0;
    private int retryCount = 0;
    private String lastFoundTitle = "";
    private int pageChangeAttempts = 0;
    private String titleBeforePageClick = "";
    private boolean commandSentThisCycle = false;
    private long lastCommandTime = 0L;

    private enum ParserState {
        IDLE,
        CLOSING_SCREEN,
        SENDING_COMMAND,
        WAITING_FOR_AUCTION,
        SCANNING_PAGE,
        CLICKING_NEXT_PAGE,
        WAITING_PAGE_CHANGE,
        FINISHING_ITEM,
        NEXT_ITEM,
        FINISHED
    }

    public FunTimeAutoParser() {
        super("FunTimeAutoParser", "FunTime AutoParser", ModuleCategory.MISC);
        discountPercent.setValue(60f);
        commandDelayMs.setValue(150f);
        maxRetries.setValue(3f);
        setup(discountPercent, commandDelayMs, maxRetries);
    }

    @Override
    public void activate() {
        super.activate();
        ChatMessage.brandmessage("§a[Parser] activate called");
        startParsing();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (state != ParserState.IDLE) {
            ChatMessage.brandmessage("§cFunTime AutoParser остановлен");
        }
        fullReset();
    }

    private void startParsing() {
        if (state != ParserState.IDLE) {
            ChatMessage.brandmessage("§eAutoParser уже запущен");
            return;
        }

        fullReset();

        List<AutoParserItems.ParserItemEntry> allItems = AutoParserItems.getItems();
        List<AutoBuyableItem> enabledItems = new ArrayList<>();
        for (AutoBuyableItem item : ItemRegistry.getFunTimeItems()) {
            if (item.isEnabled()) enabledItems.add(item);
        }

        for (AutoParserItems.ParserItemEntry parserItem : allItems) {
            boolean isEnabled = false;
            for (String autoBuyName : parserItem.getAutoBuyNames()) {
                for (AutoBuyableItem enabledItem : enabledItems) {
                    if (enabledItem.getDisplayName().equalsIgnoreCase(autoBuyName)) {
                        isEnabled = true;
                        break;
                    }
                }
                if (isEnabled) break;
            }
            if (isEnabled) activeParserItems.add(parserItem);
        }

        ChatMessage.brandmessage("§7[Parser] enabled FunTime items: §b" + enabledItems.size() + "§7, parser entries: §b" + allItems.size() + "§7, matched: §b" + activeParserItems.size());

        if (activeParserItems.isEmpty()) {
            ChatMessage.brandmessage("§cНет включенных предметов в Автобае FunTime для парсинга");
            ChatMessage.brandmessage("§eСовет: открой GUI AutoBuy (клавиша P), выбери Mode=FunTime и выдели нужные предметы (лкм по иконке).");
            setState(false);
            return;
        }

        ChatMessage.brandmessage("§a══════ AutoParser ══════");
        ChatMessage.brandmessage("§7Предметов: §b" + activeParserItems.size() + " §7| Скидка: §b" + (int) discountPercent.getValue() + "%");
        ChatMessage.brandmessage("§a═══════════════════════");

        prepareNextItem();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) return;
        if (state == ParserState.IDLE) return;

        if (antiAfkWatch.finished(ANTI_AFK_INTERVAL)) {
            performAntiAfk();
            antiAfkWatch.reset();
        }

        switch (state) {
            case CLOSING_SCREEN -> handleClosingScreen();
            case SENDING_COMMAND -> handleSendingCommand();
            case WAITING_FOR_AUCTION -> handleWaitingForAuction();
            case SCANNING_PAGE -> handleScanningPage();
            case CLICKING_NEXT_PAGE -> handleClickingNextPage();
            case WAITING_PAGE_CHANGE -> handleWaitingPageChange();
            case FINISHING_ITEM -> handleFinishingItem();
            case NEXT_ITEM -> handleNextItem();
            case FINISHED -> handleFinished();
            default -> {}
        }
    }

    private void performAntiAfk() {
        if (mc.player == null) return;

        antiAfkAction++;
        switch (antiAfkAction % 4) {
            case 0 -> {
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.setYaw(mc.player.getYaw() + 5);
            }
            case 1 -> {
                mc.player.swingHand(Hand.OFF_HAND);
                mc.player.setYaw(mc.player.getYaw() - 5);
            }
            case 2 -> {
                mc.player.setPitch(mc.player.getPitch() + 3);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            case 3 -> {
                mc.player.setPitch(mc.player.getPitch() - 3);
                mc.player.swingHand(Hand.OFF_HAND);
            }
        }
    }

    private void handleClosingScreen() {
        if (mc.currentScreen != null) {
            mc.player.closeHandledScreen();
            actionTimer.resetCounter();
            return;
        }

        if (actionTimer.hasTimeElapsed(200)) {
            state = ParserState.SENDING_COMMAND;
            commandSentThisCycle = false;
            commandTimer.resetCounter();
        }
    }

    private void handleSendingCommand() {
        if (mc.player == null || mc.player.networkHandler == null) {
            return;
        }

        if (!commandSentThisCycle) {
            if (System.currentTimeMillis() - lastCommandTime < (long) commandDelayMs.getValue()) {
                return;
            }

            mc.player.networkHandler.sendChatCommand("ah search " + currentSearchItem);

            commandSentThisCycle = true;
            lastCommandTime = System.currentTimeMillis();
            commandTimer.resetCounter();
            waitAttempts = 0;
            return;
        }

        if (commandTimer.hasTimeElapsed(500)) {
            state = ParserState.WAITING_FOR_AUCTION;
            actionTimer.resetCounter();
        }
    }

    private void handleWaitingForAuction() {
        if (!actionTimer.hasTimeElapsed(CHECK_INTERVAL)) return;
        actionTimer.resetCounter();

        waitAttempts++;

        if (waitAttempts > 5) {
            retryCount++;
            if (retryCount < (int) maxRetries.getValue()) {
                state = ParserState.CLOSING_SCREEN;
                commandSentThisCycle = false;
                waitAttempts = 0;
                lastFoundTitle = "";
                actionTimer.resetCounter();
                return;
            }
            skippedCount++;
            state = ParserState.NEXT_ITEM;
            return;
        }

        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            return;
        }

        String title = screen.getTitle().getString();
        String titleLower = title.toLowerCase();

        if (title.equals(lastFoundTitle)) {
            return;
        }

        if (titleLower.contains("не найден") || titleLower.contains("ничего") ||
                titleLower.contains("пусто") || titleLower.contains("нет результатов") ||
                titleLower.contains("товары не найдены") || titleLower.contains("not found")) {
            skippedCount++;
            state = ParserState.NEXT_ITEM;
            return;
        }

        Matcher matcher = PAGE_PATTERN.matcher(title);
        if (matcher.find()) {
            currentPage = Integer.parseInt(matcher.group(1));
            int realTotalPages = Integer.parseInt(matcher.group(2));
            totalPages = Math.min(realTotalPages, MAX_PAGES_TO_SCAN);
            lastFoundTitle = title;
            state = ParserState.SCANNING_PAGE;
            return;
        }

        String searchLower = currentSearchItem.toLowerCase();
        boolean titleMatchesSearch = titleLower.contains(searchLower) || containsAnyWord(titleLower, searchLower);

        if (titleLower.contains("поиск") || titleLower.contains("search") ||
                titleLower.contains("аукцион") || titleLower.contains("auction") ||
                titleLower.contains("ah") || titleMatchesSearch) {

            boolean hasItems = false;
            int slotsToCheck = Math.min(45, screen.getScreenHandler().slots.size());
            for (int i = 0; i < slotsToCheck; i++) {
                Slot slot = screen.getScreenHandler().slots.get(i);
                if (!slot.getStack().isEmpty()) {
                    int price = AuctionUtils.getPrice(slot.getStack());
                    if (price > 0) {
                        hasItems = true;
                        break;
                    }
                }
            }

            if (!hasItems) return;

            currentPage = 1;
            totalPages = 1;
            lastFoundTitle = title;
            state = ParserState.SCANNING_PAGE;
        }
    }

    private void handleScanningPage() {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            state = ParserState.FINISHING_ITEM;
            return;
        }

        String currentTitle = screen.getTitle().getString();
        Matcher matcher = PAGE_PATTERN.matcher(currentTitle);
        if (matcher.find()) {
            int actualPage = Integer.parseInt(matcher.group(1));
            if (actualPage > MAX_PAGES_TO_SCAN) {
                state = ParserState.FINISHING_ITEM;
                return;
            }
            currentPage = actualPage;
        }

        int slotsToScan = Math.min(45, screen.getScreenHandler().slots.size());

        int pricedSlots = 0;
        int matchedSlots = 0;
        String firstPricedName = null;
        for (int i = 0; i < slotsToScan; i++) {
            Slot slot = screen.getScreenHandler().slots.get(i);
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            int price = AuctionUtils.getPrice(stack);
            if (price <= 0) continue;
            pricedSlots++;
            if (firstPricedName == null) {
                firstPricedName = stack.getName().getString();
            }

            String itemName = stack.getName().getString();
            for (String autoBuyName : currentAutoBuyNames) {
                if (matchesItem(stack, itemName, autoBuyName)) {
                    matchedSlots++;
                    int currentLowest = lowestPricesFound.getOrDefault(autoBuyName, Integer.MAX_VALUE);
                    if (price < currentLowest) {
                        lowestPricesFound.put(autoBuyName, price);
                    }
                }
            }
        }

        ChatMessage.brandmessage("§8[Parser] §7page §b" + currentPage + "§7/§b" + totalPages + "§7 priced=§b" + pricedSlots + "§7 matched=§b" + matchedSlots + (firstPricedName != null ? "§7 first='§f" + firstPricedName + "§7'" : ""));

        if (currentPage < totalPages && currentPage < MAX_PAGES_TO_SCAN) {
            state = ParserState.CLICKING_NEXT_PAGE;
            titleBeforePageClick = currentTitle;
            actionTimer.resetCounter();
        } else {
            state = ParserState.FINISHING_ITEM;
        }
    }

    private void handleClickingNextPage() {
        if (!actionTimer.hasTimeElapsed(PAGE_CLICK_DELAY)) return;

        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            state = ParserState.FINISHING_ITEM;
            return;
        }

        try {
            int syncId = screen.getScreenHandler().syncId;
            mc.interactionManager.clickSlot(syncId, 50, 0, SlotActionType.PICKUP, mc.player);
        } catch (Exception ignored) {}

        state = ParserState.WAITING_PAGE_CHANGE;
        pageChangeAttempts = 0;
        actionTimer.resetCounter();
    }

    private void handleWaitingPageChange() {
        if (!actionTimer.hasTimeElapsed(100)) return;
        actionTimer.resetCounter();

        pageChangeAttempts++;

        if (pageChangeAttempts > 30) {
            state = ParserState.FINISHING_ITEM;
            return;
        }

        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            state = ParserState.FINISHING_ITEM;
            return;
        }

        String newTitle = screen.getTitle().getString();
        if (!newTitle.equals(titleBeforePageClick)) {
            Matcher matcher = PAGE_PATTERN.matcher(newTitle);
            if (matcher.find()) {
                int newPage = Integer.parseInt(matcher.group(1));
                if (newPage > MAX_PAGES_TO_SCAN) {
                    state = ParserState.FINISHING_ITEM;
                    return;
                }
            }
            state = ParserState.SCANNING_PAGE;
        }
    }

    private void handleFinishingItem() {
        int processed = 0;
        int notFoundLots = 0;
        for (Map.Entry<String, Integer> entry : lowestPricesFound.entrySet()) {
            String autoBuyName = entry.getKey();
            int lowestPrice = entry.getValue();

            if (lowestPrice < Integer.MAX_VALUE) {
                int discountedPrice = calculateDiscountedPrice(lowestPrice);
                if (updateAutoBuyPrice(autoBuyName, discountedPrice)) {
                    ChatMessage.brandmessage("§a✓ §f" + autoBuyName + "§7: " + formatPrice(lowestPrice) + " → §b" + formatPrice(discountedPrice));
                    updatedCount++;
                    processed++;
                } else {
                    ChatMessage.brandmessage("§e[Parser] §fno AutoBuy match for §7'" + autoBuyName + "'");
                }
            } else {
                notFoundLots++;
            }
        }
        if (processed == 0 && notFoundLots > 0) {
            ChatMessage.brandmessage("§8[Parser] §7finished '§f" + currentSearchItem + "§7', no lots found (" + notFoundLots + ")");
        }
        state = ParserState.NEXT_ITEM;
    }

    private void handleNextItem() {
        currentItemIndex++;
        lastFoundTitle = "";
        commandSentThisCycle = false;
        retryCount = 0;
        waitAttempts = 0;

        if (currentItemIndex >= activeParserItems.size()) {
            state = ParserState.FINISHED;
            return;
        }
        prepareNextItem();
    }

    private void handleFinished() {
        try {
            if (mc.player != null && mc.currentScreen != null) {
                mc.player.closeHandledScreen();
            }
        } catch (Exception ignored) {}

        ChatMessage.brandmessage("§a✓ AutoParser завершён! Обновлено: §b" + updatedCount + " §7Пропущено: §c" + skippedCount);
        fullReset();
        setState(false);
    }

    private void prepareNextItem() {
        if (currentItemIndex >= activeParserItems.size()) {
            state = ParserState.FINISHED;
            return;
        }

        AutoParserItems.ParserItemEntry entry = activeParserItems.get(currentItemIndex);
        currentSearchItem = entry.getSearchQuery();
        currentAutoBuyNames = entry.getAutoBuyNames();
        currentPage = 1;
        totalPages = 1;
        lowestPricesFound.clear();
        waitAttempts = 0;
        retryCount = 0;
        commandSentThisCycle = false;
        lastFoundTitle = "";

        for (String name : currentAutoBuyNames) {
            lowestPricesFound.put(name, Integer.MAX_VALUE);
        }

        ChatMessage.brandmessage("§7[" + (currentItemIndex + 1) + "/" + activeParserItems.size() + "] §b" + currentSearchItem);

        state = ParserState.CLOSING_SCREEN;
        actionTimer.resetCounter();
    }

    private void fullReset() {
        currentItemIndex = 0;
        currentPage = 1;
        totalPages = 1;
        currentSearchItem = "";
        currentAutoBuyNames = new String[0];
        activeParserItems.clear();
        lowestPricesFound.clear();
        updatedCount = 0;
        skippedCount = 0;
        waitAttempts = 0;
        antiAfkAction = 0;
        retryCount = 0;
        lastFoundTitle = "";
        pageChangeAttempts = 0;
        titleBeforePageClick = "";
        commandSentThisCycle = false;
        lastCommandTime = 0;
        state = ParserState.IDLE;

        actionTimer.resetCounter();
        commandTimer.resetCounter();
        antiAfkWatch.reset();
    }

    private boolean containsAnyWord(String title, String search) {
        String[] words = search.split("\\s+");
        for (String word : words) {
            if (word.length() >= 3 && title.contains(word)) return true;
        }
        return false;
    }

    private boolean matchesItem(ItemStack stack, String itemName, String autoBuyName) {
        String cleanItemName = im.zov4ik.features.impl.misc.autobuy.AutoBuyItem.normalizeLine(itemName);
        String cleanAutoBuyName = im.zov4ik.features.impl.misc.autobuy.AutoBuyItem.normalizeLine(autoBuyName
                .replace("[★] ", "")
                .replace("[⚒] ", "")
                .replace("[❄] ", "")
                .replace("[🍹] ", "")
                .replace("[★]", "")
                .replace("[⚒]", "")
                .replace("[❄]", "")
                .replace("[🍹]", ""));

        if (!cleanAutoBuyName.isBlank() && cleanItemName.contains(cleanAutoBuyName)) return true;
        if (!cleanItemName.isBlank() && cleanAutoBuyName.contains(cleanItemName)) return true;

        try {
            for (AutoBuyableItem item : ItemRegistry.getFunTimeItems()) {
                if (item.getDisplayName().equalsIgnoreCase(autoBuyName)) {
                    if (AuctionUtils.compareItem(stack, item.createItemStack())) return true;
                }
            }
        } catch (Exception ignored) {}

        return false;
    }

    private int calculateDiscountedPrice(int originalPrice) {
        double discount = discountPercent.getValue() / 100.0;
        return (int) (originalPrice * (1 - discount));
    }

    private boolean updateAutoBuyPrice(String itemName, int newPrice) {
        try {
            for (AutoBuyableItem item : ItemRegistry.getFunTimeItems()) {
                String displayName = item.getDisplayName();

                if (displayName.equals(itemName)) {
                    item.getSettings().setBuyBelow(newPrice);
                    AutoBuySettingsManager.getInstance().saveSettings(displayName, item.getSettings());
                    updateAutoBuyGuiPrice(displayName, newPrice);
                    return true;
                }

                String cleanDisplayName = displayName
                        .replace("[★] ", "")
                        .replace("[⚒] ", "")
                        .replace("[❄] ", "")
                        .replace("[🍹] ", "")
                        .trim();
                String cleanItemName = itemName
                        .replace("[★] ", "")
                        .replace("[⚒] ", "")
                        .replace("[❄] ", "")
                        .replace("[🍹] ", "")
                        .trim();

                if (cleanDisplayName.equalsIgnoreCase(cleanItemName)) {
                    item.getSettings().setBuyBelow(newPrice);
                    AutoBuySettingsManager.getInstance().saveSettings(displayName, item.getSettings());
                    updateAutoBuyGuiPrice(displayName, newPrice);
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void updateAutoBuyGuiPrice(String itemName, int newPrice) {
        AutoBuy autoBuy = getAutoBuy();
        if (autoBuy == null) {
            ChatMessage.brandmessage("§c[Parser] AutoBuy module not found, GUI not updated");
            return;
        }

        if (autoBuy.updateFunTimeItemPrice(itemName, newPrice)) {
            persistAutoBuyConfigs();
        } else {
            ChatMessage.brandmessage("§e[Parser] §fno GUI match for §7'" + itemName + "'");
        }
    }

    private AutoBuy getAutoBuy() {
        if (zov4ik.getInstance() == null || zov4ik.getInstance().getModuleProvider() == null) {
            return null;
        }
        return zov4ik.getInstance().getModuleProvider().get(AutoBuy.class);
    }

    private void persistAutoBuyConfigs() {
        if (zov4ik.getInstance() == null || zov4ik.getInstance().getFileController() == null) {
            return;
        }
        zov4ik.getInstance().getFileController().saveFile(ModuleFile.class);
        zov4ik.getInstance().getFileController().saveFile(AutoBuyConfigFile.class);
    }

    private String formatPrice(int price) {
        if (price >= 1_000_000) return String.format("%.2fM$", price / 1_000_000.0);
        if (price >= 1_000) return String.format("%.1fK$", price / 1_000.0);
        return price + "$";
    }
}
