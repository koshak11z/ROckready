//package ru.zov.implement.features.modules.misc;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.experimental.FieldDefaults;
//import lombok.experimental.NonFinal;
//import net.minecraft.client.gui.DrawContext;
//import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
//import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
//import net.minecraft.client.texture.NativeImage;
//import net.minecraft.client.util.ScreenshotRecorder;
//import net.minecraft.client.render.RenderLayer;
//import net.minecraft.item.ArmorItem;
//import net.minecraft.item.BowItem;
//import net.minecraft.item.CrossbowItem;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.item.MaceItem;
//import net.minecraft.item.SwordItem;
//import net.minecraft.item.MiningToolItem;
//import net.minecraft.item.TridentItem;
//import net.minecraft.registry.Registries;
//import net.minecraft.screen.slot.Slot;
//import net.minecraft.screen.slot.SlotActionType;
//import net.minecraft.scoreboard.Scoreboard;
//import net.minecraft.scoreboard.ScoreboardDisplaySlot;
//import net.minecraft.scoreboard.ScoreboardEntry;
//import net.minecraft.scoreboard.ScoreboardObjective;
//import net.minecraft.scoreboard.Team;
//import net.minecraft.text.Text;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.math.MathHelper;
//import org.lwjgl.glfw.GLFW;
//import ru.zov.api.event.EventHandler;
//import ru.zov.api.feature.module.Module;
//import ru.zov.api.feature.module.ModuleCategory;
//import ru.zov.api.feature.module.setting.Setting;
//import ru.zov.api.feature.module.setting.implement.BindSetting;
//import ru.zov.api.feature.module.setting.implement.SelectSetting;
//import ru.zov.api.feature.module.setting.implement.TextSetting;
//import ru.zov.api.feature.module.setting.implement.ValueSetting;
//import ru.zov.api.system.font.FontRenderer;
//import ru.zov.api.system.font.Fonts;
//import ru.zov.api.system.shape.ShapeProperties;
//import ru.zov.common.util.auction.SpookyTimePriceParser;
//import ru.zov.common.util.color.ColorUtil;
//import ru.zov.common.util.math.MathUtil;
//import ru.zov.common.util.render.Render2DUtil;
//import ru.zov.common.util.render.ScissorManager;
//import ru.zov.common.util.other.StopWatch;
//import ru.zov.common.util.task.TaskPriority;
//import ru.zov.common.util.task.scripts.Script;
//import ru.zov.core.Main;
//import ru.zov.implement.events.container.HandledScreenEvent;
//import ru.zov.implement.events.keyboard.KeyEvent;
//import ru.zov.implement.events.keyboard.MouseScrollEvent;
//import ru.zov.implement.events.packet.PacketEvent;
//import ru.zov.implement.events.player.TickEvent;
//import ru.zov.implement.features.modules.combat.killaura.rotation.Angle;
//import ru.zov.implement.features.modules.combat.killaura.rotation.RotationConfig;
//import ru.zov.implement.features.modules.combat.killaura.rotation.RotationController;
//import ru.zov.implement.features.modules.combat.killaura.rotation.angle.HolyWorldSmoothMode;
//import ru.zov.implement.features.modules.combat.killaura.rotation.angle.LinearSmoothMode;
//import ru.zov.implement.features.modules.combat.killaura.rotation.angle.SpookyTimeSmoothMode;
//import ru.zov.implement.features.modules.misc.autobuy.AutoBuyCategory;
//import ru.zov.implement.features.modules.misc.autobuy.AutoBuyItem;
//import ru.zov.implement.features.modules.misc.autobuy.AutoBuyScreen;
//import ru.zov.implement.features.modules.misc.autobuy.catalog.items.AutoBuyableItem;
//import ru.zov.implement.features.modules.misc.autobuy.catalog.originalitems.ItemRegistry;
//import ru.zov.implement.features.modules.misc.telegram.TelegramBotBridge;
//import ru.zov.implement.features.modules.misc.telegram.TelegramCommand;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//@Getter
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class AutoBuy extends Module {
//    private static final int AUCTION_SCAN_DELAY_MS = 25;
//    private static final int BUY_ACTION_DELAY_MS = 1;
//    private static final int SPOOKYTIME_REFRESH_SLOT = 49;
//    private static final int HOLYWORLD_REFRESH_SLOT = 47;
//    private static final int HOLYWORLD_CONFIRM_TOP_SIZE = 27;
//    private static final int HOLYWORLD_CONFIRM_CENTER_SLOT = 13;
//    private static final int HOLYWORLD_CONFIRM_ACCEPT_PREFERRED_SLOT = 10;
//    private static final int HOLYWORLD_CONFIRM_DECLINE_PREFERRED_SLOT = 16;
//    private static final int SPOOKYTIME_REFRESH_DELAY_MS = 450;
//    private static final int HOLYWORLD_REFRESH_MIN_DELAY_MS = 415;
//    private static final int HOLYWORLD_REFRESH_MAX_DELAY_MS = 435;
//    private static final int HOLYWORLD_CONFIRM_DELAY_MS = 1;
//    private static final long HOLYWORLD_CONFIRM_TIMEOUT_MS = 5000L;
//    private static final long HOLYWORLD_SESSION_MIN_MS = 100_000L;
//    private static final long HOLYWORLD_SESSION_MAX_MS = 140_000L;
//    private static final long HOLYWORLD_FRENZY_MIN_MS = 5_000L;
//    private static final long HOLYWORLD_FRENZY_MAX_MS = 25_000L;
//    private static final long HOLYWORLD_AUCTION_LOOK_MIN_MS = 60L;
//    private static final long HOLYWORLD_AUCTION_LOOK_MAX_MS = 320L;
//    private static final long HOLYWORLD_FRENZY_LOOK_MIN_MS = 20L;
//    private static final long HOLYWORLD_FRENZY_LOOK_MAX_MS = 90L;
//    private static final int HISTORY_PANEL_WIDTH = 188;
//    private static final int HISTORY_HEADER_HEIGHT = 30;
//    private static final int HISTORY_ENTRY_HEIGHT = 31;
//    private static final int HISTORY_ENTRY_GAP = 6;
//    private static final int HISTORY_MAX_ENTRIES = 4500;
//    private static final long HISTORY_REMOVE_ANIM_MS = 180L;
//    private static final long HISTORY_MERGE_WINDOW_MS = 5_000L;
//    private static final long PURCHASE_CONFIRM_TIMEOUT_MS = 2_500L;
//    private static final long PURCHASE_SUCCESS_MESSAGE_WINDOW_MS = 200L;
//    private static final long PURCHASE_REBUY_GUARD_MS = 200L;
//    private static final long AUCTION_FINGERPRINT_STABLE_MS = 120L;
//    private static final String PURCHASE_FAILED_PREFIX = "\u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0437\u0430\u0431\u0440\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442";
//    private static final Identifier BUTTON_TEXTURE = Identifier.of("minecraft", "widget/button");
//    private static final Identifier BUTTON_HOVER_TEXTURE = Identifier.of("minecraft", "widget/button_highlighted");
//    private static final Pattern SALE_MESSAGE_PATTERN = Pattern.compile("^(.+?)\\s+купил у вас\\s+\\[(.+?)]\\s*x(\\d+)\\s+за\\s+([0-9\\s]+).*$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
//    private static final Pattern PURCHASE_MESSAGE_PATTERN = Pattern.compile("^\\u0432\\u044b\\s+\\u043a\\u0443\\u043f\\u0438\\u043b\\u0438\\s+\\[(.+?)]\\s*x\\s*(\\d+)\\s+\\u0443\\s+.+?\\s+\\u0437\\u0430\\s+([0-9\\s]+).*$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
//    private static final RotationConfig HOLYWORLD_ROTATION_CONFIG =
//            new RotationConfig(new HolyWorldSmoothMode(), false, true);
//
//    BindSetting openGuiBind = new BindSetting("Open GUI", "\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u043a\u043e\u043d\u0444\u0438\u0433 AutoBuy").setKey(GLFW.GLFW_KEY_P);
//    SelectSetting serverMode = new SelectSetting("Mode", "AutoBuy server mode")
//            .value("HolyWorld", "FunTime", "SpookyTime").selected("FunTime");
//    SelectSetting telegramChatMode = new SelectSetting("Telegram Chat Mode", "Who can send Telegram commands")
//            .value("Whitelist", "Global")
//            .selected("Whitelist");
//    TextSetting telegramApiToken = new TextSetting("Telegram API", "Telegram bot token for AutoBuy commands")
//            .setText("")
//            .setMin(0)
//            .setMax(128);
//    TextSetting telegramGroupId = new TextSetting("Telegram Group IDs", "Telegram whitelist IDs (comma separated)")
//            .setText("")
//            .setMin(0)
//            .setMax(256);
//    ValueSetting autoSetupDiscount = new ValueSetting("AutoSetup Discount", "Percent discount for AutoSetup")
//            .setValue(5F).range(0F, 50F).visible(() -> false);
//    Map<String, List<AutoBuyItem>> itemsByMode = new LinkedHashMap<>();
//    AutoBuyScreen screen;
//    SpookyTimePriceParser priceParser = new SpookyTimePriceParser();
//    StopWatch refreshWatch = new StopWatch();
//    StopWatch scanWatch = new StopWatch();
//    Script autoBuyScript = new Script();
//    TelegramBotBridge telegramBotBridge = new TelegramBotBridge();
//    @NonFinal
//    boolean autoBuyEnabled = false;
//    @NonFinal
//    boolean autoSetupEnabled = false;
//    @NonFinal
//    long autoBuyStartMs;
//    @NonFinal
//    int buyClicks;
//    @NonFinal
//    int refreshCount;
//    @NonFinal
//    long lastRefreshMs;
//    @NonFinal
//    long nextRefreshDelayMs;
//    @NonFinal
//    long totalRefreshInterval;
//    @NonFinal
//    int refreshIntervals;
//    @NonFinal
//    AutoBuyItem autoSetupItem;
//    @NonFinal
//    int autoSetupIndex;
//    @NonFinal
//    int autoSetupStage;
//    @NonFinal
//    StopWatch autoSetupWatch = new StopWatch();
//    @NonFinal
//    int lastMouseX, lastMouseY;
//    @NonFinal
//    ButtonBounds autoBuyBounds;
//    @NonFinal
//    long lastAuctionSeenMs;
//    @NonFinal
//    ButtonBounds historyListBounds;
//    @NonFinal
//    ButtonBounds hoveredHistoryEntryBounds;
//    @NonFinal
//    float historyScroll;
//    @NonFinal
//    float historyScrollAnimated;
//    @NonFinal
//    PurchaseHistoryEntry hoveredHistoryEntry;
//    @NonFinal
//    List<PurchaseHistoryEntry> purchaseHistory = new ArrayList<>();
//    @NonFinal
//    LinkedHashSet<String> blockedAuctionListings = new LinkedHashSet<>();
//    @NonFinal
//    StopWatch holyWorldLookWatch = new StopWatch();
//    @NonFinal
//    long holyWorldNextLookDelayMs;
//    @NonFinal
//    long holyWorldSessionDeadlineMs;
//    @NonFinal
//    long holyWorldFrenzyDeadlineMs;
//    @NonFinal
//    AutoBuyItem holyWorldPendingConfirmationItem;
//    @NonFinal
//    int holyWorldPendingUnitPrice = -1;
//    @NonFinal
//    long holyWorldPendingConfirmationDeadlineMs;
//    @NonFinal
//    PurchaseHistoryEntry holyWorldPendingHistoryEntry;
//    @NonFinal
//    AutoBuyItem pendingPurchaseConfirmationItem;
//    @NonFinal
//    int pendingPurchaseUnitPrice = -1;
//    @NonFinal
//    long pendingPurchaseConfirmationDeadlineMs;
//    @NonFinal
//    PurchaseHistoryEntry pendingPurchaseHistoryEntry;
//    @NonFinal
//    long pendingPurchaseStartedMs;
//    @NonFinal
//    long purchaseCooldownUntilMs;
//    @NonFinal
//    String lastAuctionFingerprint = "";
//    @NonFinal
//    String pendingAuctionFingerprint = "";
//    @NonFinal
//    long pendingAuctionFingerprintSinceMs;
//    @NonFinal
//    long autoBuyStartCoins = -1L;
//    @NonFinal
//    long autoBuyStartCoinsMs;
//    @NonFinal
//    long lastKnownCoins = -1L;
//    @NonFinal
//    long lastTelegramCommandChatId;
//    @NonFinal
//    long lastTelegramWarningMs;
//    @NonFinal
//    String lastTelegramWarning = "";
//
//    public AutoBuy() {
//        super("AutoBuy", "Auto Buy", ModuleCategory.MISC);
//        buildCatalog();
//
//        List<Setting> settings = new ArrayList<>();
//        settings.add(openGuiBind);
//        settings.add(serverMode);
//        settings.add(telegramChatMode);
//        settings.add(telegramApiToken);
//        settings.add(telegramGroupId);
//        settings.add(autoSetupDiscount);
//        itemsByMode.values().stream()
//                .flatMap(List::stream)
//                .forEach(item -> {
//                    settings.add(item.getPriceSetting());
//                    settings.add(item.getDurabilitySetting());
//                    settings.add(item.getThornsSetting());
//                });
//        setup(settings.toArray(Setting[]::new));
//
//        screen = new AutoBuyScreen(this);
//        setState(true);
//    }
//
//    @Override
//    public void deactivate() {
//        if (mc.currentScreen instanceof AutoBuyScreen autoBuyScreen && autoBuyScreen.belongsTo(this)) {
//            autoBuyScreen.forceClose();
//        }
//        autoBuyScript.cleanup();
//        RotationController.INSTANCE.reset();
//        resetHolyWorldState();
//        clearPendingPurchaseConfirmation();
//        resetAuctionFingerprintState();
//    }
//
//    @EventHandler
//    public void onKey(KeyEvent e) {
//        if (e.type() == net.minecraft.client.util.InputUtil.Type.MOUSE && e.action() == 1) {
//            if (mc.currentScreen instanceof GenericContainerScreen screen && shouldShowAuctionOverlay(screen)) {
//                if (hoveredHistoryEntry != null
//                        && hoveredHistoryEntryBounds != null
//                        && hoveredHistoryEntryBounds.contains(lastMouseX, lastMouseY)
//                        && (e.key() == GLFW.GLFW_MOUSE_BUTTON_RIGHT || e.key() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
//                    if (holyWorldPendingHistoryEntry == hoveredHistoryEntry) {
//                        holyWorldPendingHistoryEntry = null;
//                    }
//                    startRemovingHistoryEntry(hoveredHistoryEntry);
//                    hoveredHistoryEntry = null;
//                    hoveredHistoryEntryBounds = null;
//                    historyScroll = MathHelper.clamp(historyScroll, getMinHistoryScroll(historyListBounds != null ? historyListBounds.h : 0), 0.0F);
//                    return;
//                }
//                if (autoBuyBounds != null && autoBuyBounds.contains(lastMouseX, lastMouseY)) {
//                    if (e.key() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
//                        setAutoBuyEnabled(!autoBuyEnabled);
//                    }
//                    return;
//                }
//            }
//        }
//
//        if (openGuiBind.getKey() == GLFW.GLFW_KEY_UNKNOWN) {
//            return;
//        }
//        if (!e.isKeyDown(openGuiBind.getKey(), true)) {
//            return;
//        }
//
//        if (mc.currentScreen instanceof AutoBuyScreen autoBuyScreen && autoBuyScreen.belongsTo(this)) {
//            autoBuyScreen.requestClose();
//            return;
//        }
//
//        screen.open();
//    }
//
//    @EventHandler
//    public void onHandledScreen(HandledScreenEvent e) {
//        if (!(mc.currentScreen instanceof GenericContainerScreen screen) || !shouldShowAuctionOverlay(screen)) {
//            return;
//        }
//
//        lastMouseX = e.getMouseX();
//        lastMouseY = e.getMouseY();
//
//        DrawContext context = e.getDrawContext();
//        int offsetX = (screen.width - e.getBackgroundWidth()) / 2;
//        int offsetY = (screen.height - e.getBackgroundHeight()) / 2;
//
//        String buyLabel = "AutoBuy:";
//        String buyStatus = autoBuyEnabled ? "\u0432\u043a\u043b" : "\u0432\u044b\u043a\u043b";
//        int buyTextWidth = mc.textRenderer.getWidth(buyLabel) + 4 + mc.textRenderer.getWidth(buyStatus);
//        int buttonHeight = 20;
//        int buttonY = Math.max(2, offsetY - buttonHeight - 2);
//        int buttonWidth = Math.min(Math.max(112, buyTextWidth + 24), Math.max(112, e.getBackgroundWidth() - 12));
//        int buttonX = offsetX + (e.getBackgroundWidth() - buttonWidth) / 2;
//
//        autoBuyBounds = new ButtonBounds(buttonX, buttonY, buttonWidth, buttonHeight);
//
//        int labelColor = 0xFFE8E8E8;
//        int offColor = 0xFFFF4B4B;
//        int onColor = 0xFF4BFF4B;
//        drawPurchaseHistory(context, screen, offsetX, offsetY, e.getBackgroundWidth(), e.getBackgroundHeight());
//
//        context.getMatrices().push();
//        context.getMatrices().translate(0.0F, 0.0F, 400.0F);
//        RenderSystem.disableDepthTest();
//        boolean hoverBuy = autoBuyBounds.contains(lastMouseX, lastMouseY);
//        drawVanillaButton(context, autoBuyBounds, hoverBuy);
//        drawButtonText(context, autoBuyBounds, buyLabel, buyStatus, labelColor, autoBuyEnabled ? onColor : offColor);
//        drawAutoBuyInfo(context, offsetX, buttonY, e.getBackgroundWidth());
//        drawAuctionLabels(context, screen, offsetX, offsetY, e.getBackgroundHeight());
//        RenderSystem.enableDepthTest();
//        context.getMatrices().pop();
//    }
//
//    @EventHandler
//    public void onMouseScroll(MouseScrollEvent e) {
//        if (!(mc.currentScreen instanceof GenericContainerScreen screen) || !shouldShowAuctionOverlay(screen)) {
//            return;
//        }
//        if (historyListBounds == null || !historyListBounds.contains(lastMouseX, lastMouseY)) {
//            return;
//        }
//
//        historyScroll += (float) e.getVertical() * 20.0F;
//        historyScroll = MathHelper.clamp(historyScroll, getMinHistoryScroll(historyListBounds.h), 0.0F);
//        e.cancel();
//    }
//
//    @EventHandler
//    public void onPacket(PacketEvent e) {
//        if (e.getType() != PacketEvent.Type.RECEIVE || !(e.getPacket() instanceof GameMessageS2CPacket gameMessage)) {
//            return;
//        }
//
//        String rawMessage = gameMessage.content().getString();
//        handleSaleMessage(rawMessage);
//        handlePurchaseMessage(rawMessage);
//    }
//
//    @EventHandler
//    public void onTick(TickEvent e) {
//        tickTelegramCommands();
//        autoBuyScript.update();
//        tickHolyWorldLook();
//        expireHolyWorldPendingConfirmation();
//        expirePendingPurchaseConfirmation();
//        cleanupPurchaseHistory();
//
//        if (autoSetupEnabled) {
//            setAutoSetupEnabled(false);
//        }
//
//        if (!isHolyWorldMode() && (holyWorldSessionDeadlineMs != 0L || holyWorldFrenzyDeadlineMs != 0L)) {
//            RotationController.INSTANCE.reset();
//            resetHolyWorldState();
//        }
//
//        if (autoBuyEnabled && autoBuyStartCoins < 0L) {
//            captureStartCoins(System.currentTimeMillis());
//        }
//
//        if (!autoBuyEnabled) {
//            autoBuyScript.cleanup();
//            return;
//        }
//
//        if (mc.currentScreen instanceof GenericContainerScreen screen && isHolyWorldPurchaseConfirmScreen(screen)) {
//            handleHolyWorldPurchaseConfirm(screen);
//            return;
//        }
//
//        if (mc.currentScreen instanceof AutoBuyScreen) {
//            return;
//        }
//
//        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
//            return;
//        }
//
//        if (!isAuctionScreen(screen)) {
//            return;
//        }
//
//        lastAuctionSeenMs = System.currentTimeMillis();
//        updateAuctionFingerprint(screen);
//
//        if (isHolyWorldMode() && handleHolyWorldSessionTimeout()) {
//            return;
//        }
//
//        if (!autoBuyScript.isFinished()) {
//            return;
//        }
//
//        if (!scanWatch.finished(AUCTION_SCAN_DELAY_MS)) {
//            return;
//        }
//        scanWatch.reset();
//
//        if (hasPendingPurchaseConfirmation()) {
//            return;
//        }
//
//        AutoBuyCandidate candidate = findBuyCandidate(screen);
//        if (candidate != null) {
//            scheduleBuy(screen, candidate);
//            return;
//        }
//
//        if (refreshWatch.finished(getRefreshDelayMs())) {
//            clickRefresh(screen);
//            refreshWatch.reset();
//        }
//    }
//
//    public List<AutoBuyCategory> categories() {
//        return List.of(AutoBuyCategory.ALL);
//    }
//
//    public List<AutoBuyItem> getItemsByCategory(AutoBuyCategory category) {
//        return getItems();
//    }
//
//    public List<AutoBuyItem> getConfiguredItems() {
//        return getItems().stream()
//                .filter(AutoBuyItem::hasPrice)
//                .sorted(Comparator.comparingInt(this::sortBucket)
//                        .thenComparing(AutoBuyItem::getDisplayName, String.CASE_INSENSITIVE_ORDER))
//                .toList();
//    }
//
//    public Optional<AutoBuyItem> detect(ItemStack stack, List<String> tooltipLines) {
//        return getItems().stream()
//                .sorted(Comparator
//                        .comparing(AutoBuyItem::isNeedsAdditionalCheck).reversed()
//                        .thenComparing(AutoBuyItem::getDisplayName))
//                .filter(item -> item.matches(stack, tooltipLines))
//                .findFirst();
//    }
//
//    public List<AutoBuyItem> getItems() {
//        List<AutoBuyItem> items = itemsByMode.get(serverMode.getSelected());
//        return items == null ? List.of() : List.copyOf(items);
//    }
//
//    private void buildCatalog() {
//        itemsByMode.clear();
//        ItemRegistry.reload();
//
//        itemsByMode.put("HolyWorld", buildModeItems("HolyWorld", ItemRegistry.getHolyWorld()));
//        itemsByMode.put("FunTime", buildModeItems("FunTime", ItemRegistry.getFunTimeItems()));
//        itemsByMode.put("SpookyTime", buildModeItems("SpookyTime", ItemRegistry.getSpookyTime()));
//    }
//
//    private List<AutoBuyItem> buildModeItems(String modeKey, List<AutoBuyableItem> sourceItems) {
//        List<AutoBuyItem> modeItems = new ArrayList<>();
//        Map<String, Integer> collisionIndex = new LinkedHashMap<>();
//        for (AutoBuyableItem sourceItem : sourceItems) {
//            ItemStack referenceStack = sourceItem.createItemStack();
//            if (referenceStack == null || referenceStack.isEmpty()) {
//                continue;
//            }
//
//            String displayName = sourceItem.getDisplayName();
//            String searchName = sourceItem.getSearchName();
//            String baseKey = slugify(searchName != null && !searchName.isBlank() ? searchName : displayName);
//            int duplicateIndex = collisionIndex.merge(baseKey, 1, Integer::sum);
//            String key = duplicateIndex == 1 ? baseKey : baseKey + "_" + duplicateIndex;
//            String modeAwareKey = slugify(modeKey + "_" + key);
//
//            TextSetting priceSetting = new TextSetting("price_" + modeAwareKey, "\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0446\u0435\u043d\u0430 \u0434\u043b\u044f " + displayName)
//                    .setText("")
//                    .setMin(0)
//                    .setMax(16)
//                    .visible(() -> false);
//
//            TextSetting durabilitySetting = new TextSetting("durability_" + modeAwareKey, "\u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c, % \u0434\u043b\u044f " + displayName)
//                    .setText("")
//                    .setMin(0)
//                    .setMax(3)
//                    .visible(() -> false);
//
//            SelectSetting thornsSetting = new SelectSetting("thorns_" + modeAwareKey, "\u0420\u0435\u0436\u0438\u043c \u0448\u0438\u043f\u043e\u0432 \u0434\u043b\u044f " + displayName)
//                    .value("\u041e\u0431\u0430", "\u0428\u0438\u043f\u044b", "\u0410\u043d\u0442\u0438\u0448\u0438\u043f")
//                    .selected("\u041e\u0431\u0430")
//                    .visible(() -> false);
//
//            boolean needsAdditionalCheck = sourceItem.needsAdditionalCheck() || "HolyWorld".equals(modeKey);
//
//            modeItems.add(new AutoBuyItem(
//                    modeAwareKey,
//                    displayName,
//                    searchName == null || searchName.isBlank() ? displayName : searchName,
//                    AutoBuyCategory.ALL,
//                    referenceStack,
//                    priceSetting,
//                    durabilitySetting,
//                    thornsSetting,
//                    needsAdditionalCheck
//            ));
//        }
//
//        modeItems.sort(Comparator.comparingInt(this::sortBucket)
//                .thenComparing(AutoBuyItem::getDisplayName, String.CASE_INSENSITIVE_ORDER));
//        return modeItems;
//    }
//
//    private int sortBucket(AutoBuyItem item) {
//        ItemStack stack = item.getIconStack();
//        Item type = stack.getItem();
//        if (type == Items.TOTEM_OF_UNDYING) {
//            return 0;
//        }
//        if (isWeaponItem(type)) {
//            return 2;
//        }
//        if (isToolItem(type)) {
//            return 1;
//        }
//        if (isArmorItem(type)) {
//            return 3;
//        }
//        return 4;
//    }
//
//    private boolean isToolItem(Item item) {
//        return item instanceof MiningToolItem && !(item instanceof SwordItem);
//    }
//
//    private boolean isWeaponItem(Item item) {
//        return item instanceof SwordItem
//                || item instanceof BowItem
//                || item instanceof CrossbowItem
//                || item instanceof TridentItem
//                || item instanceof MaceItem;
//    }
//
//    private boolean isArmorItem(Item item) {
//        return item instanceof ArmorItem || item == Items.ELYTRA;
//    }
//
//    private String slugify(String value) {
//        String normalized = AutoBuyItem.normalizeLine(value)
//                .replace(' ', '_')
//                .replaceAll("[^a-z0-9_\\u0430-\\u044f]", "");
//        if (!normalized.isBlank()) {
//            return normalized;
//        }
//        return "item_" + Integer.toUnsignedString(value.toLowerCase(Locale.ROOT).hashCode());
//    }
//
//    private boolean isAuctionScreen(GenericContainerScreen screen) {
//        return matchesAuctionScreen(screen, getAuctionRefreshSlot(), getAuctionRefreshItem());
//    }
//
//    private boolean isSpookyAuctionScreen(GenericContainerScreen screen) {
//        return matchesAuctionScreen(screen, SPOOKYTIME_REFRESH_SLOT, Items.NETHER_STAR);
//    }
//
//    private boolean matchesAuctionScreen(GenericContainerScreen screen, int refreshSlotIndex, Item refreshItem) {
//        if (screen == null || refreshSlotIndex < 0 || refreshItem == null) {
//            return false;
//        }
//
//        List<Slot> slots = screen.getScreenHandler().slots;
//        if (slots.size() <= refreshSlotIndex) {
//            return false;
//        }
//
//        Slot refreshSlot = slots.get(refreshSlotIndex);
//        return refreshSlot != null
//                && refreshSlot.hasStack()
//                && refreshSlot.getStack().getItem() == refreshItem;
//    }
//
//    private AutoBuyCandidate findBuyCandidate(GenericContainerScreen screen) {
//        List<AutoBuyItem> configuredItems = getConfiguredItems();
//        if (configuredItems.isEmpty()) {
//            return null;
//        }
//
//        Map<Item, List<AutoBuyItem>> configuredByItem = configuredItems.stream()
//                .collect(Collectors.groupingBy(item -> item.getIconStack().getItem()));
//
//        List<Slot> slots = screen.getScreenHandler().slots;
//        int endIndex = Math.min(slots.size() - 1, 44);
//
//        AutoBuyCandidate best = null;
//        for (int i = 0; i <= endIndex; i++) {
//            Slot slot = slots.get(i);
//            if (slot == null || !slot.hasStack()) {
//                continue;
//            }
//            if (slot.inventory instanceof net.minecraft.entity.player.PlayerInventory) {
//                continue;
//            }
//
//            ItemStack stack = slot.getStack();
//            List<AutoBuyItem> candidates = configuredByItem.get(stack.getItem());
//            if (candidates == null || candidates.isEmpty()) {
//                continue;
//            }
//
//            int unitPrice = priceParser.getUnitPrice(stack);
//            if (unitPrice <= 0) {
//                int totalPrice = priceParser.getPrice(stack);
//                if (totalPrice > 0) {
//                    unitPrice = totalPrice / Math.max(1, stack.getCount());
//                }
//            }
//            if (unitPrice <= 0) {
//                continue;
//            }
//
//            String listingFingerprint = buildListingFingerprint(i, stack, unitPrice);
//            if (blockedAuctionListings.contains(listingFingerprint)) {
//                continue;
//            }
//
//            for (AutoBuyItem candidate : candidates) {
//                if (!candidate.matches(stack, List.of()) || !candidate.hasPrice()) {
//                    continue;
//                }
//                if (isPurchaseCooldownActive()) {
//                    continue;
//                }
//                long maxPrice = candidate.getPriceValue();
//                if (maxPrice <= 0 || unitPrice > maxPrice) {
//                    continue;
//                }
//
//                if (best == null || unitPrice < best.price()) {
//                    best = new AutoBuyCandidate(slot, candidate, unitPrice, listingFingerprint);
//                }
//            }
//        }
//
//        return best;
//    }
//
//    private void scheduleBuy(GenericContainerScreen screen, AutoBuyCandidate candidate) {
//        if (candidate == null || candidate.slot() == null) {
//            return;
//        }
//
//        Slot slot = candidate.slot();
//        ItemStack previewStack = slot.getStack().copy();
//        int syncId = screen.getScreenHandler().syncId;
//        autoBuyScript.cleanup()
//                .addStep(0, () -> {
//                    buyClicks++;
//                    blockedAuctionListings.add(candidate.listingFingerprint());
//                    if (isHolyWorldMode()) {
//                        resolvePendingHistoryEntry(false, ItemStack.EMPTY);
//                        holyWorldPendingConfirmationItem = candidate.item();
//                        holyWorldPendingUnitPrice = candidate.price();
//                        holyWorldPendingConfirmationDeadlineMs = System.currentTimeMillis() + HOLYWORLD_CONFIRM_TIMEOUT_MS;
//                        holyWorldPendingHistoryEntry = beginPurchaseAttempt(previewStack);
//                    } else {
//                        startPendingPurchaseConfirmation(candidate, previewStack);
//                    }
//                    mc.interactionManager.clickSlot(syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
//                })
//                .addStep(BUY_ACTION_DELAY_MS, () -> {});
//    }
//
//    private void clickRefresh(GenericContainerScreen screen) {
//        int refreshSlotIndex = getAuctionRefreshSlot();
//        Item refreshItem = getAuctionRefreshItem();
//        List<Slot> slots = screen.getScreenHandler().slots;
//        if (refreshSlotIndex < 0 || slots.size() <= refreshSlotIndex) {
//            return;
//        }
//
//        Slot refreshSlot = slots.get(refreshSlotIndex);
//        if (refreshSlot == null || !refreshSlot.hasStack() || refreshSlot.getStack().getItem() != refreshItem) {
//            return;
//        }
//
//        long now = System.currentTimeMillis();
//        refreshCount++;
//        if (lastRefreshMs > 0L) {
//            totalRefreshInterval += now - lastRefreshMs;
//            refreshIntervals++;
//        }
//        lastRefreshMs = now;
//
//        if (isHolyWorldMode()) {
//            nextRefreshDelayMs = randomBetween(HOLYWORLD_REFRESH_MIN_DELAY_MS, HOLYWORLD_REFRESH_MAX_DELAY_MS);
//        }
//
//        pendingAuctionFingerprint = "";
//        pendingAuctionFingerprintSinceMs = 0L;
//        mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, refreshSlot.id, 0, SlotActionType.PICKUP, mc.player);
//    }
//
//    private void startPendingPurchaseConfirmation(AutoBuyCandidate candidate, ItemStack previewStack) {
//        clearPendingPurchaseConfirmation();
//        pendingPurchaseConfirmationItem = candidate.item();
//        pendingPurchaseUnitPrice = candidate.price();
//        pendingPurchaseStartedMs = System.currentTimeMillis();
//        pendingPurchaseConfirmationDeadlineMs = pendingPurchaseStartedMs + PURCHASE_CONFIRM_TIMEOUT_MS;
//        pendingPurchaseHistoryEntry = createPurchaseHistoryEntry(previewStack);
//    }
//
//    private void expirePendingPurchaseConfirmation() {
//        if (pendingPurchaseConfirmationDeadlineMs != 0L
//                && System.currentTimeMillis() > pendingPurchaseConfirmationDeadlineMs) {
//            clearPendingPurchaseConfirmation();
//        }
//    }
//
//    private boolean hasPendingPurchaseConfirmation() {
//        return pendingPurchaseConfirmationItem != null && pendingPurchaseConfirmationDeadlineMs != 0L;
//    }
//
//    private boolean isPurchaseCooldownActive() {
//        if (purchaseCooldownUntilMs == 0L || System.currentTimeMillis() > purchaseCooldownUntilMs) {
//            purchaseCooldownUntilMs = 0L;
//            return false;
//        }
//        return true;
//    }
//
//    private void updateAuctionFingerprint(GenericContainerScreen screen) {
//        String fingerprint = buildAuctionFingerprint(screen);
//        if (fingerprint.isEmpty()) {
//            return;
//        }
//
//        long now = System.currentTimeMillis();
//        if (!fingerprint.equals(pendingAuctionFingerprint)) {
//            pendingAuctionFingerprint = fingerprint;
//            pendingAuctionFingerprintSinceMs = now;
//            return;
//        }
//
//        if (now - pendingAuctionFingerprintSinceMs < AUCTION_FINGERPRINT_STABLE_MS) {
//            return;
//        }
//
//        if (lastAuctionFingerprint.isEmpty()) {
//            lastAuctionFingerprint = fingerprint;
//        } else if (!fingerprint.equals(lastAuctionFingerprint)) {
//            lastAuctionFingerprint = fingerprint;
//            blockedAuctionListings.clear();
//        }
//
//        pendingAuctionFingerprint = "";
//        pendingAuctionFingerprintSinceMs = 0L;
//    }
//
//    private String buildAuctionFingerprint(GenericContainerScreen screen) {
//        if (screen == null) {
//            return "";
//        }
//
//        List<Slot> slots = screen.getScreenHandler().slots;
//        int endIndex = Math.min(slots.size() - 1, 44);
//        int refreshSlotIndex = getAuctionRefreshSlot();
//        StringBuilder builder = new StringBuilder(Math.max(128, endIndex * 20));
//        for (int i = 0; i <= endIndex; i++) {
//            if (i == refreshSlotIndex) {
//                continue;
//            }
//
//            Slot slot = slots.get(i);
//            if (slot == null || slot.inventory instanceof net.minecraft.entity.player.PlayerInventory) {
//                continue;
//            }
//
//            builder.append(i).append('=');
//            if (!slot.hasStack()) {
//                builder.append("empty;");
//                continue;
//            }
//
//            ItemStack stack = slot.getStack();
//            int unitPrice = priceParser.getUnitPrice(stack);
//            if (unitPrice <= 0) {
//                int totalPrice = priceParser.getPrice(stack);
//                if (totalPrice > 0) {
//                    unitPrice = totalPrice / Math.max(1, stack.getCount());
//                }
//            }
//
//            builder.append(Registries.ITEM.getId(stack.getItem()))
//                    .append('|')
//                    .append(AutoBuyItem.normalizeLine(stack.getName().getString()))
//                    .append('|')
//                    .append(stack.getCount())
//                    .append('|')
//                    .append(stack.getDamage())
//                    .append('|')
//                    .append(unitPrice)
//                    .append(';');
//        }
//        return builder.toString();
//    }
//
//    private String buildListingFingerprint(int slotIndex, ItemStack stack, int unitPrice) {
//        if (stack == null || stack.isEmpty()) {
//            return slotIndex + "=empty";
//        }
//
//        return new StringBuilder(96)
//                .append(slotIndex)
//                .append('|')
//                .append(Registries.ITEM.getId(stack.getItem()))
//                .append('|')
//                .append(AutoBuyItem.normalizeLine(stack.getName().getString()))
//                .append('|')
//                .append(stack.getCount())
//                .append('|')
//                .append(stack.getDamage())
//                .append('|')
//                .append(unitPrice)
//                .toString();
//    }
//
//    private record AutoBuyCandidate(Slot slot, AutoBuyItem item, int price, String listingFingerprint) {
//    }
//
//    private record ButtonBounds(int x, int y, int w, int h) {
//        boolean contains(int mx, int my) {
//            return mx >= x && mx <= x + w && my >= y && my <= y + h;
//        }
//    }
//
//    private static final class PurchaseHistoryEntry {
//        ItemStack stack;
//        String title;
//        int count;
//        boolean purchased;
//        long createdAtMs;
//        long updatedAtMs;
//        float animatedY;
//        boolean removing;
//        long removingStartedMs;
//
//        PurchaseHistoryEntry(ItemStack stack, String title, int count, boolean purchased, long createdAtMs, long updatedAtMs, float animatedY, boolean removing, long removingStartedMs) {
//            this.stack = stack;
//            this.title = title;
//            this.count = count;
//            this.purchased = purchased;
//            this.createdAtMs = createdAtMs;
//            this.updatedAtMs = updatedAtMs;
//            this.animatedY = animatedY;
//            this.removing = removing;
//            this.removingStartedMs = removingStartedMs;
//        }
//    }
//
//    private PurchaseHistoryEntry createPurchaseHistoryEntry(ItemStack stack) {
//        if (stack == null || stack.isEmpty()) {
//            return null;
//        }
//
//        long now = System.currentTimeMillis();
//        return new PurchaseHistoryEntry(
//                stack.copy(),
//                stack.getName().getString(),
//                Math.max(1, stack.getCount()),
//                false,
//                now,
//                now,
//                Float.NaN,
//                false,
//                0L
//        );
//    }
//
//    private PurchaseHistoryEntry beginPurchaseAttempt(ItemStack stack) {
//        PurchaseHistoryEntry entry = createPurchaseHistoryEntry(stack);
//        pushPurchaseHistoryEntry(entry);
//        return entry;
//    }
//
//    private void pushPurchaseHistoryEntry(PurchaseHistoryEntry entry) {
//        if (entry == null) {
//            return;
//        }
//
//        purchaseHistory.add(0, entry);
//        while (purchaseHistory.size() > HISTORY_MAX_ENTRIES) {
//            purchaseHistory.remove(purchaseHistory.size() - 1);
//        }
//        historyScroll = 0.0F;
//    }
//
//    private void resolvePendingHistoryEntry(boolean purchased, ItemStack updatedStack) {
//        if (holyWorldPendingHistoryEntry == null) {
//            return;
//        }
//
//        PurchaseHistoryEntry entry = holyWorldPendingHistoryEntry;
//        if (updatedStack != null && !updatedStack.isEmpty()) {
//            entry.stack = updatedStack.copy();
//            entry.title = updatedStack.getName().getString();
//            entry.count = Math.max(1, updatedStack.getCount());
//        }
//        entry.purchased = purchased;
//        entry.updatedAtMs = System.currentTimeMillis();
//        holyWorldPendingHistoryEntry = null;
//    }
//
//    private void startRemovingHistoryEntry(PurchaseHistoryEntry entry) {
//        if (entry == null || entry.removing) {
//            return;
//        }
//        entry.removing = true;
//        entry.removingStartedMs = System.currentTimeMillis();
//        entry.updatedAtMs = entry.removingStartedMs;
//    }
//
//    private void cleanupPurchaseHistory() {
//        if (purchaseHistory.isEmpty()) {
//            return;
//        }
//
//        long now = System.currentTimeMillis();
//        purchaseHistory.removeIf(entry -> entry.removing && now - entry.removingStartedMs >= HISTORY_REMOVE_ANIM_MS);
//    }
//
//    private int getPurchasedEntryCount() {
//        int total = 0;
//        for (PurchaseHistoryEntry entry : purchaseHistory) {
//            if (entry.purchased && !entry.removing) {
//                total++;
//            }
//        }
//        return total;
//    }
//
//    private int getVisibleHistoryEntryCount() {
//        int total = 0;
//        for (PurchaseHistoryEntry entry : purchaseHistory) {
//            if (!entry.removing) {
//                total++;
//            }
//        }
//        return total;
//    }
//
//    private float getMinHistoryScroll(int listHeight) {
//        int visibleEntries = getVisibleHistoryEntryCount();
//        float contentHeight = visibleEntries <= 0
//                ? 0.0F
//                : visibleEntries * (HISTORY_ENTRY_HEIGHT + HISTORY_ENTRY_GAP) - HISTORY_ENTRY_GAP;
//        return Math.min(0.0F, listHeight - contentHeight);
//    }
//
//    private void setAutoBuyEnabled(boolean enabled) {
//        if (autoBuyEnabled == enabled) {
//            return;
//        }
//
//        long now = System.currentTimeMillis();
//        autoBuyEnabled = enabled;
//        autoBuyStartMs = enabled ? now : 0L;
//        buyClicks = 0;
//        refreshCount = 0;
//        totalRefreshInterval = 0;
//        refreshIntervals = 0;
//        lastRefreshMs = 0;
//        nextRefreshDelayMs = 0L;
//        purchaseCooldownUntilMs = 0L;
//        hoveredHistoryEntry = null;
//        hoveredHistoryEntryBounds = null;
//        historyListBounds = null;
//        refreshWatch.reset();
//        scanWatch.reset();
//        autoBuyScript.cleanup();
//        setAutoSetupEnabled(false);
//        resetHolyWorldState();
//        clearPendingPurchaseConfirmation();
//        resetAuctionFingerprintState();
//        autoBuyStartCoins = -1L;
//        autoBuyStartCoinsMs = 0L;
//        if (enabled) {
//            captureStartCoins(now);
//        }
//        if (enabled) {
//            openAuctionOnEnable();
//        } else {
//            RotationController.INSTANCE.reset();
//        }
//    }
//
//    private void setAutoSetupEnabled(boolean enabled) {
//        if (autoSetupEnabled == enabled) {
//            return;
//        }
//        autoSetupEnabled = enabled;
//        autoSetupIndex = 0;
//        autoSetupItem = null;
//        autoSetupStage = 0;
//        autoSetupWatch.reset();
//    }
//
//    private void tickTelegramCommands() {
//        String token = telegramApiToken.getText();
//        boolean globalMode = isTelegramGlobalMode();
//        List<Long> whitelistIds = getTelegramWhitelistIds();
//
//        telegramBotBridge.tick(token, 0L);
//
//        String telegramError = telegramBotBridge.consumeLastError();
//        if (telegramError != null && !telegramError.isBlank()) {
//            showTelegramWarning("Telegram error: " + telegramError);
//        }
//
//        if (token == null || token.isBlank()) {
//            return;
//        }
//
//        if (!globalMode && whitelistIds.isEmpty()) {
//            showTelegramWarning("Whitelist пуст. Используй .telegram chat id1,id2 или .telegram chat global");
//        }
//
//        for (TelegramCommand command : telegramBotBridge.drainCommands()) {
//            if (!globalMode && !whitelistIds.contains(command.chatId())) {
//                showTelegramWarning("Команда пришла из chat id " + command.chatId() + ", но он не в whitelist");
//                continue;
//            }
//            lastTelegramCommandChatId = command.chatId();
//            handleTelegramCommand(token.trim(), command.chatId(), command.text());
//        }
//    }
//
//    private boolean isTelegramGlobalMode() {
//        return "Global".equalsIgnoreCase(telegramChatMode.getSelected());
//    }
//
//    private List<Long> getTelegramWhitelistIds() {
//        return parseTelegramChatIds(telegramGroupId.getText());
//    }
//
//    private List<Long> parseTelegramChatIds(String rawIds) {
//        if (rawIds == null || rawIds.isBlank()) {
//            return List.of();
//        }
//
//        LinkedHashSet<Long> ids = new LinkedHashSet<>();
//        String[] tokens = rawIds.split("[,;\\s]+");
//        for (String token : tokens) {
//            if (token == null || token.isBlank()) {
//                continue;
//            }
//            try {
//                long value = Long.parseLong(token.trim());
//                if (value != 0L) {
//                    ids.add(value);
//                }
//            } catch (NumberFormatException ignored) {
//            }
//        }
//        return List.copyOf(ids);
//    }
//
//    private void showTelegramWarning(String message) {
//        if (message == null || message.isBlank()) {
//            return;
//        }
//        long now = System.currentTimeMillis();
//        if (message.equals(lastTelegramWarning) && now - lastTelegramWarningMs < 5000L) {
//            return;
//        }
//        lastTelegramWarning = message;
//        lastTelegramWarningMs = now;
//        if (mc.inGameHud != null) {
//            mc.inGameHud.getChatHud().addMessage(Text.literal("[AutoBuy/Telegram] " + message));
//        }
//    }
//
//    private void handleTelegramCommand(String token, long chatId, String messageText) {
//        String command = normalizeTelegramCommand(messageText);
//        if (command.isBlank() || (!command.startsWith("!") && !command.startsWith("/"))) {
//            return;
//        }
//
//        switch (command) {
//            case "!хелп", "!help", "/help", "/хелп" -> sendTelegramHelp(token, chatId);
//            case "!статс", "!stats", "/stats", "/статс" -> sendTelegramStats(token, chatId);
//            case "!скрин", "!screen", "/screen", "/скрин" -> sendTelegramScreenshot(token, chatId);
//            default -> {
//            }
//        }
//    }
//
//    private String normalizeTelegramCommand(String rawText) {
//        String normalized = AutoBuyItem.normalizeLine(rawText);
//        if (normalized.isBlank()) {
//            return "";
//        }
//
//        int spaceIndex = normalized.indexOf(' ');
//        String command = spaceIndex > 0 ? normalized.substring(0, spaceIndex) : normalized;
//        int mentionIndex = command.indexOf('@');
//        if (mentionIndex > 0) {
//            command = command.substring(0, mentionIndex);
//        }
//        return command;
//    }
//
//    private void sendTelegramHelp(String token, long chatId) {
//        String helpMessage = """
//                🤖 AutoBuy Telegram
//                📚 Команды:
//                • !хелп | !help | /help — список команд
//                • !скрин | !screen | /screen — скриншот Minecraft
//                • !статс | !stats | /stats — статистика и монеты/час
//                """;
//        telegramBotBridge.sendMessageAsync(token, chatId, helpMessage.trim());
//    }
//
//    private void sendTelegramStats(String token, long chatId) {
//        telegramBotBridge.sendMessageAsync(token, chatId, buildTelegramStatsMessage());
//    }
//
//    private void sendTelegramScreenshot(String token, long chatId) {
//        Path screenshotPath;
//        try (NativeImage screenshot = ScreenshotRecorder.takeScreenshot(mc.getFramebuffer())) {
//            screenshotPath = Files.createTempFile("zov_autobuy_", ".png");
//            screenshot.writeTo(screenshotPath);
//        } catch (IOException exception) {
//            telegramBotBridge.sendMessageAsync(token, chatId, "Не получилось сделать скриншот: " + exception.getMessage());
//            return;
//        } catch (RuntimeException exception) {
//            telegramBotBridge.sendMessageAsync(token, chatId, "Не получилось сделать скриншот.");
//            return;
//        }
//
//        telegramBotBridge.sendPhotoAsync(token, chatId, screenshotPath, "AutoBuy скриншот", true);
//    }
//
//    private void handleSaleMessage(String rawMessage) {
//        if (rawMessage == null || rawMessage.isBlank()) {
//            return;
//        }
//
//        String normalized = AutoBuyItem.normalizeLine(rawMessage);
//        if (!normalized.contains("купил у вас")) {
//            return;
//        }
//
//        Matcher matcher = SALE_MESSAGE_PATTERN.matcher(rawMessage);
//        if (!matcher.matches()) {
//            sendSaleToTelegram("неизвестно", "неизвестно", 0, -1L, rawMessage);
//            return;
//        }
//
//        String buyer = matcher.group(1).trim();
//        String item = matcher.group(2).trim();
//        int amount = parseIntSafe(AutoBuyItem.normalizeDigits(matcher.group(3)));
//        long totalPrice = parseLongSafe(AutoBuyItem.normalizeDigits(matcher.group(4)));
//        sendSaleToTelegram(buyer, item, amount, totalPrice, rawMessage);
//    }
//
//    private void handlePurchaseMessage(String rawMessage) {
//        if (!hasPendingPurchaseConfirmation() || rawMessage == null || rawMessage.isBlank()) {
//            return;
//        }
//
//        String normalized = AutoBuyItem.normalizeLine(rawMessage);
//        boolean success = normalized.contains("\u0432\u044b \u043a\u0443\u043f\u0438\u043b\u0438");
//        boolean failed = normalized.contains(PURCHASE_FAILED_PREFIX) && normalized.contains("\u0443\u0436\u0435 \u043a\u0443\u043f\u0438\u043b\u0438");
//        if (!success && !failed) {
//            return;
//        }
//
//        long now = System.currentTimeMillis();
//        if (success && (pendingPurchaseStartedMs == 0L || now - pendingPurchaseStartedMs > PURCHASE_SUCCESS_MESSAGE_WINDOW_MS)) {
//            clearPendingPurchaseConfirmation();
//            return;
//        }
//
//        ItemStack confirmedStack = pendingPurchaseHistoryEntry != null
//                ? pendingPurchaseHistoryEntry.stack.copy()
//                : ItemStack.EMPTY;
//        if (success) {
//            Matcher matcher = PURCHASE_MESSAGE_PATTERN.matcher(normalized);
//            if (!matcher.matches()) {
//                return;
//            }
//            if (!matchesPendingPurchaseMessage(matcher, confirmedStack)) {
//                return;
//            }
//
//            int amount = parseIntSafe(AutoBuyItem.normalizeDigits(matcher.group(2)));
//            if (amount > 0 && confirmedStack != null && !confirmedStack.isEmpty()) {
//                confirmedStack.setCount(amount);
//            }
//
//            if (confirmedStack == null || confirmedStack.isEmpty()) {
//                confirmedStack = pendingPurchaseConfirmationItem != null
//                        ? pendingPurchaseConfirmationItem.getIconStack()
//                        : ItemStack.EMPTY;
//            }
//
//            sendPurchasedItemToTelegram(confirmedStack, pendingPurchaseUnitPrice);
//            resolvePendingPurchaseConfirmation(true, confirmedStack);
//            return;
//        }
//
//        resolvePendingPurchaseConfirmation(false, confirmedStack);
//    }
//
//    private boolean matchesPendingPurchaseMessage(Matcher matcher, ItemStack pendingStack) {
//        if (matcher == null) {
//            return false;
//        }
//
//        String messageItemName = AutoBuyItem.normalizeLine(matcher.group(1));
//        int amount = parseIntSafe(AutoBuyItem.normalizeDigits(matcher.group(2)));
//        long totalPrice = parseLongSafe(AutoBuyItem.normalizeDigits(matcher.group(3)));
//        String expectedItemName = pendingStack != null && !pendingStack.isEmpty()
//                ? AutoBuyItem.normalizeLine(pendingStack.getName().getString())
//                : (pendingPurchaseConfirmationItem != null
//                ? AutoBuyItem.normalizeLine(pendingPurchaseConfirmationItem.getDisplayName())
//                : "");
//
//        if (!expectedItemName.isBlank()
//                && !messageItemName.equals(expectedItemName)
//                && !messageItemName.contains(expectedItemName)
//                && !expectedItemName.contains(messageItemName)) {
//            return false;
//        }
//
//        if (pendingStack != null && !pendingStack.isEmpty() && amount > 0 && pendingStack.getCount() > 0 && amount != pendingStack.getCount()) {
//            return false;
//        }
//
//        if (pendingPurchaseUnitPrice > 0 && amount > 0 && totalPrice > 0) {
//            long expectedTotal = (long) pendingPurchaseUnitPrice * amount;
//            if (expectedTotal != totalPrice) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    private void resolvePendingPurchaseConfirmation(boolean purchased, ItemStack updatedStack) {
//        PurchaseHistoryEntry entry = pendingPurchaseHistoryEntry;
//        if (entry != null) {
//            if (updatedStack != null && !updatedStack.isEmpty()) {
//                entry.stack = updatedStack.copy();
//                entry.title = updatedStack.getName().getString();
//                entry.count = Math.max(1, updatedStack.getCount());
//            }
//            entry.purchased = purchased;
//            entry.updatedAtMs = System.currentTimeMillis();
//            pushPurchaseHistoryEntry(entry);
//        }
//
//        purchaseCooldownUntilMs = Math.max(purchaseCooldownUntilMs, System.currentTimeMillis() + PURCHASE_REBUY_GUARD_MS);
//        clearPendingPurchaseConfirmation();
//    }
//
//    private void clearPendingPurchaseConfirmation() {
//        pendingPurchaseConfirmationItem = null;
//        pendingPurchaseUnitPrice = -1;
//        pendingPurchaseConfirmationDeadlineMs = 0L;
//        pendingPurchaseHistoryEntry = null;
//        pendingPurchaseStartedMs = 0L;
//    }
//
//    private void resetAuctionFingerprintState() {
//        lastAuctionFingerprint = "";
//        pendingAuctionFingerprint = "";
//        pendingAuctionFingerprintSinceMs = 0L;
//        blockedAuctionListings.clear();
//    }
//
//    private void sendSaleToTelegram(String buyer, String itemName, int amount, long totalPrice, String rawMessage) {
//        StringBuilder message = new StringBuilder();
//        message.append("💸 Продажа").append('\n');
//        message.append("👤 Покупатель: ").append(buyer).append('\n');
//        message.append("🧩 Предмет: ").append(itemName).append('\n');
//        if (amount > 0) {
//            message.append("📦 Кол-во: ").append(amount).append('\n');
//        }
//        if (totalPrice >= 0L) {
//            message.append("💰 Сумма: ").append(formatNumber(totalPrice)).append(" ⛃").append('\n');
//            if (amount > 0) {
//                long unitPrice = Math.max(1L, totalPrice / amount);
//                message.append("🏷 Цена за 1: ").append(formatNumber(unitPrice)).append(" ⛃").append('\n');
//            }
//        }
//        message.append("🌐 Режим: ").append(serverMode.getSelected());
//        if (totalPrice < 0L || amount <= 0) {
//            message.append('\n').append("📝 Сообщение: ").append(rawMessage);
//        }
//        sendTelegramToTargets(message.toString());
//    }
//
//    private void sendPurchasedItemToTelegram(ItemStack stack, int unitPrice) {
//        if (stack == null || stack.isEmpty()) {
//            return;
//        }
//
//        String itemName = stack.getName().getString();
//        int count = Math.max(1, stack.getCount());
//        StringBuilder message = new StringBuilder();
//        message.append("✅ Успешная покупка").append('\n');
//        message.append("🧩 Предмет: ").append(itemName).append('\n');
//        message.append("📦 Кол-во: ").append(count).append('\n');
//        if (unitPrice > 0) {
//            message.append("🏷 Цена за 1: ").append(formatNumber(unitPrice)).append(" ⛃").append('\n');
//            message.append("💰 Сумма: ").append(formatNumber((long) unitPrice * count)).append(" ⛃").append('\n');
//        }
//        message.append("🌐 Режим: ").append(serverMode.getSelected());
//        sendTelegramToTargets(message.toString());
//    }
//
//    private void sendTelegramToTargets(String message) {
//        if (message == null || message.isBlank()) {
//            return;
//        }
//
//        String token = telegramApiToken.getText();
//        if (token == null || token.isBlank()) {
//            return;
//        }
//
//        List<Long> targets = resolveTelegramTargets();
//        for (Long chatId : targets) {
//            if (chatId != null && chatId != 0L) {
//                telegramBotBridge.sendMessageAsync(token.trim(), chatId, message);
//            }
//        }
//    }
//
//    private List<Long> resolveTelegramTargets() {
//        if (isTelegramGlobalMode()) {
//            return lastTelegramCommandChatId != 0L ? List.of(lastTelegramCommandChatId) : List.of();
//        }
//
//        List<Long> whitelist = getTelegramWhitelistIds();
//        if (!whitelist.isEmpty()) {
//            return whitelist;
//        }
//        return lastTelegramCommandChatId != 0L ? List.of(lastTelegramCommandChatId) : List.of();
//    }
//
//    private int parseIntSafe(String value) {
//        if (value == null || value.isBlank()) {
//            return 0;
//        }
//        try {
//            return Integer.parseInt(value);
//        } catch (NumberFormatException ignored) {
//            return 0;
//        }
//    }
//
//    private long parseLongSafe(String value) {
//        if (value == null || value.isBlank()) {
//            return -1L;
//        }
//        try {
//            return Long.parseLong(value);
//        } catch (NumberFormatException ignored) {
//            return -1L;
//        }
//    }
//
//    private String buildTelegramStatsMessage() {
//        long now = System.currentTimeMillis();
//        long currentCoins = parseCoinsFromScoreboard();
//        if (currentCoins >= 0L) {
//            lastKnownCoins = currentCoins;
//        }
//
//        long uptimeMs = autoBuyEnabled && autoBuyStartMs > 0L ? now - autoBuyStartMs : 0L;
//        StringBuilder message = new StringBuilder();
//        message.append("📊 AutoBuy stats").append('\n');
//        message.append("⚙ Статус: ").append(autoBuyEnabled ? "включен" : "выключен").append('\n');
//        message.append("🌐 Режим: ").append(serverMode.getSelected()).append('\n');
//        message.append("⏱ Время работы: ").append(formatDuration(uptimeMs)).append('\n');
//
//        if (currentCoins >= 0L) {
//            message.append("🪙 Монеты сейчас: ").append(formatNumber(currentCoins)).append('\n');
//        } else if (lastKnownCoins >= 0L) {
//            message.append("🪙 Монеты сейчас: ").append(formatNumber(lastKnownCoins)).append(" (последние)").append('\n');
//        } else {
//            message.append("🪙 Монеты сейчас: не найдены в табло").append('\n');
//        }
//
//        boolean hasSessionCoins = autoBuyStartCoins >= 0L && autoBuyStartCoinsMs > 0L;
//        if (hasSessionCoins && currentCoins >= 0L) {
//            long elapsedMs = Math.max(1L, now - autoBuyStartCoinsMs);
//            double elapsedHours = elapsedMs / 3_600_000.0D;
//            long earned = currentCoins - autoBuyStartCoins;
//            long perHour = elapsedHours > 0.0D ? Math.round(earned / elapsedHours) : 0L;
//
//            message.append("🚀 Монеты на старте: ").append(formatNumber(autoBuyStartCoins)).append('\n');
//            message.append("💵 Заработано: ").append(formatSignedNumber(earned)).append('\n');
//            message.append("📈 Среднее за час: ").append(formatSignedNumber(perHour)).append('\n');
//            message.append("🗓 Прогноз за 24ч: ").append(formatSignedNumber(perHour * 24L)).append('\n');
//        } else {
//            message.append("📈 Среднее за час: нет данных (включи AutoBuy и открой scoreboard)").append('\n');
//        }
//
//        message.append("🖱 Клики покупки: ").append(buyClicks).append('\n');
//        message.append("✅ Подтверждено покупок: ").append(getPurchasedEntryCount()).append('\n');
//        message.append("🔄 Обновлений аукциона: ").append(refreshCount).append('\n');
//        message.append("🧰 Настроенных предметов: ").append(getConfiguredItems().size());
//        return message.toString();
//    }
//
//    private long parseTelegramChatId(String rawChatId) {
//        if (rawChatId == null || rawChatId.isBlank()) {
//            return 0L;
//        }
//        try {
//            return Long.parseLong(rawChatId.trim());
//        } catch (NumberFormatException ignored) {
//            return 0L;
//        }
//    }
//
//    private void captureStartCoins(long timestampMs) {
//        long startCoins = parseCoinsFromScoreboard();
//        if (startCoins < 0L) {
//            autoBuyStartCoins = -1L;
//            autoBuyStartCoinsMs = 0L;
//            return;
//        }
//        autoBuyStartCoins = startCoins;
//        autoBuyStartCoinsMs = timestampMs;
//        lastKnownCoins = startCoins;
//    }
//
//    private long parseCoinsFromScoreboard() {
//        if (mc.world == null) {
//            return -1L;
//        }
//
//        Scoreboard scoreboard = mc.world.getScoreboard();
//        if (scoreboard == null) {
//            return -1L;
//        }
//
//        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
//        if (objective == null) {
//            return -1L;
//        }
//
//        long coins = -1L;
//        for (ScoreboardEntry entry : scoreboard.getScoreboardEntries(objective)) {
//            String rawLine = Team.decorateName(scoreboard.getScoreHolderTeam(entry.owner()), entry.name()).getString();
//            String normalized = AutoBuyItem.normalizeLine(rawLine);
//            if (!normalized.contains("монет")) {
//                continue;
//            }
//
//            String digits = AutoBuyItem.normalizeDigits(rawLine);
//            if (digits.isBlank()) {
//                continue;
//            }
//
//            try {
//                long value = Long.parseLong(digits);
//                if (value > coins) {
//                    coins = value;
//                }
//            } catch (NumberFormatException ignored) {
//            }
//        }
//
//        return coins;
//    }
//
//    private String formatSignedNumber(long value) {
//        if (value > 0L) {
//            return "+" + formatNumber(value);
//        }
//        return formatNumber(value);
//    }
//
//    private String formatNumber(long value) {
//        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ');
//    }
//
//    private String formatDuration(long durationMs) {
//        long totalSeconds = Math.max(0L, durationMs / 1000L);
//        long hours = totalSeconds / 3600L;
//        long minutes = (totalSeconds % 3600L) / 60L;
//        long seconds = totalSeconds % 60L;
//
//        if (hours > 0L) {
//            return hours + "ч " + minutes + "м";
//        }
//        if (minutes > 0L) {
//            return minutes + "м " + seconds + "с";
//        }
//        return seconds + "с";
//    }
//
//    private void drawAutoBuyInfo(DrawContext context, int offsetX, int anchorY, int backgroundWidth) {
//        long now = System.currentTimeMillis();
//        double workSeconds = autoBuyEnabled && autoBuyStartMs > 0 ? (now - autoBuyStartMs) / 1000.0 : 0.0;
//        long avgRefresh = refreshIntervals > 0 ? totalRefreshInterval / refreshIntervals : 0;
//
//        String line1 = "\u0421\u0440\u0435\u0434\u043d\u0435\u0435 \u0432\u0440\u0435\u043c\u044f \u043e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u044f: " + avgRefresh + "ms";
//        String line2 = "\u041e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0439 \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0430: " + refreshCount;
//        String line3 = "\u0412\u0440\u0435\u043c\u044f \u0440\u0430\u0431\u043e\u0442\u044b: " + String.format(Locale.ROOT, "%.1f", workSeconds) + "s";
//
//        int centerX = offsetX + backgroundWidth / 2;
//        int y;
//        int color = 0xFFFFFFFF;
//
//        FontRenderer infoFont = Fonts.getSize(14, Fonts.Type.DEFAULT);
//        int w1 = (int) infoFont.getStringWidth(line1);
//        int w2 = (int) infoFont.getStringWidth(line2);
//        int w3 = (int) infoFont.getStringWidth(line3);
//        int lineHeight = Math.max(6, (int) infoFont.getStringHeight(line1));
//        int lineSpacing = Math.max(3, lineHeight - 4);
//        int totalHeight = lineSpacing * 2 + lineHeight;
//        y = Math.max(2, anchorY - totalHeight - 8);
//        infoFont.drawString(context.getMatrices(), line1, centerX - w1 / 2.0F, y, color);
//        infoFont.drawString(context.getMatrices(), line2, centerX - w2 / 2.0F, y + lineSpacing, color);
//        infoFont.drawString(context.getMatrices(), line3, centerX - w3 / 2.0F, y + lineSpacing * 2, color);
//    }
//
//    private void drawAuctionLabels(DrawContext context, GenericContainerScreen screen, int offsetX, int offsetY, int backgroundHeight) {
//        if (mc.player == null) {
//            return;
//        }
//
//        int color = 0x404040;
//        context.drawText(mc.textRenderer, screen.getTitle(), offsetX + 8, offsetY + 6, color, false);
//        context.drawText(mc.textRenderer, mc.player.getInventory().getDisplayName(), offsetX + 8, offsetY + backgroundHeight - 94, color, false);
//    }
//
//    private void drawPurchaseHistory(DrawContext context, GenericContainerScreen screen, int offsetX, int offsetY, int backgroundWidth, int backgroundHeight) {
//        int panelX = offsetX + backgroundWidth + 8;
//        if (panelX + HISTORY_PANEL_WIDTH > screen.width - 6) {
//            panelX = Math.max(6, offsetX - HISTORY_PANEL_WIDTH - 8);
//        }
//        int panelY = offsetY;
//        int panelH = backgroundHeight;
//        int listX = panelX + 6;
//        int listY = panelY + HISTORY_HEADER_HEIGHT + 4;
//        int listW = HISTORY_PANEL_WIDTH - 12;
//        int listH = panelH - HISTORY_HEADER_HEIGHT - 10;
//
//        historyListBounds = new ButtonBounds(panelX + 4, panelY + HISTORY_HEADER_HEIGHT, HISTORY_PANEL_WIDTH - 8, panelH - HISTORY_HEADER_HEIGHT - 4);
//        historyScroll = MathHelper.clamp(historyScroll, getMinHistoryScroll(listH), 0.0F);
//        historyScrollAnimated = MathHelper.lerp(0.22F, historyScrollAnimated, historyScroll);
//        hoveredHistoryEntry = null;
//        hoveredHistoryEntryBounds = null;
//
//        int panelFill = ColorUtil.multAlpha(0xFF141820, 0.86F);
//        int panelOutline = ColorUtil.multAlpha(0xFFC8D0D7, 0.70F);
//        int mutedText = ColorUtil.multAlpha(0xFFB7C0C8, 0.95F);
//        int titleColor = ColorUtil.multAlpha(0xFFF2F6FA, 1.0F);
//
//        blur.render(ShapeProperties.create(context.getMatrices(), panelX, panelY, HISTORY_PANEL_WIDTH, panelH)
//                .round(8).softness(135).color(0x26000000).build());
//        rectangle.render(ShapeProperties.create(context.getMatrices(), panelX, panelY, HISTORY_PANEL_WIDTH, panelH)
//                .round(8).thickness(1.05F).outlineColor(panelOutline).color(panelFill).build());
//
//        FontRenderer titleFont = Fonts.getSize(14, Fonts.Type.BOLD);
//        FontRenderer metaFont = Fonts.getSize(12, Fonts.Type.DEFAULT);
//        FontRenderer entryTitleFont = Fonts.getSize(13, Fonts.Type.BOLD);
//        FontRenderer entryMetaFont = Fonts.getSize(12, Fonts.Type.DEFAULT);
//
//        titleFont.drawString(context.getMatrices(), "\u041f\u043e\u043a\u0443\u043f\u043a\u0438", panelX + 9.0F, panelY + 9.0F, titleColor);
//        int visibleEntries = getVisibleHistoryEntryCount();
//        String headerMeta = "\u0423\u0441\u043f\u0435\u0448\u043d\u043e: " + getPurchasedEntryCount() + " / " + visibleEntries;
//        metaFont.drawString(context.getMatrices(), headerMeta, panelX + 9.0F, panelY + 19.0F, mutedText);
//
//        if (visibleEntries == 0 && purchaseHistory.isEmpty()) {
//            metaFont.drawCenteredString(context.getMatrices(), "\u041f\u043e\u043a\u0430 \u043f\u0443\u0441\u0442\u043e", panelX + HISTORY_PANEL_WIDTH / 2.0F, panelY + panelH / 2.0F - 4.0F, mutedText);
//            return;
//        }
//
//        ScissorManager scissor = Main.getInstance().getScissorManager();
//        scissor.push(context.getMatrices().peek().getPositionMatrix(), listX, listY, listW, listH);
//        long now = System.currentTimeMillis();
//        int visibleIndex = 0;
//        for (PurchaseHistoryEntry entry : purchaseHistory) {
//            float targetY = entry.removing
//                    ? (Float.isNaN(entry.animatedY) ? listY + historyScrollAnimated : entry.animatedY)
//                    : listY + historyScrollAnimated + visibleIndex * (HISTORY_ENTRY_HEIGHT + HISTORY_ENTRY_GAP);
//
//            if (Float.isNaN(entry.animatedY)) {
//                entry.animatedY = targetY;
//            } else if (!entry.removing) {
//                entry.animatedY = MathHelper.lerp(0.22F, entry.animatedY, targetY);
//            }
//
//            float removeProgress = entry.removing
//                    ? MathHelper.clamp((now - entry.removingStartedMs) / (float) HISTORY_REMOVE_ANIM_MS, 0.0F, 1.0F)
//                    : 0.0F;
//            float removeAlpha = 1.0F - removeProgress;
//            if (!entry.removing) {
//                visibleIndex++;
//            }
//            if (removeAlpha <= 0.01F) {
//                continue;
//            }
//
//            float drawY = entry.animatedY;
//            if (drawY + HISTORY_ENTRY_HEIGHT < listY || drawY > listY + listH) {
//                continue;
//            }
//
//            float reveal = MathHelper.clamp((now - entry.updatedAtMs) / 220.0F, 0.0F, 1.0F);
//            float shift = (1.0F - reveal) * 10.0F + removeProgress * 18.0F;
//            float alpha = (0.45F + reveal * 0.55F) * removeAlpha;
//            float drawX = listX + shift;
//            boolean hovered = !entry.removing && MathUtil.isHovered(lastMouseX, lastMouseY, drawX, drawY, listW - shift, HISTORY_ENTRY_HEIGHT);
//            if (hovered) {
//                hoveredHistoryEntry = entry;
//                hoveredHistoryEntryBounds = new ButtonBounds((int) drawX, (int) drawY, (int) (listW - shift), HISTORY_ENTRY_HEIGHT);
//            }
//
//            int entryFill = hovered
//                    ? ColorUtil.multAlpha(0xFF222833, alpha)
//                    : ColorUtil.multAlpha(0xFF181E27, alpha);
//            int entryOutline = hovered
//                    ? ColorUtil.multAlpha(0xFFE3EBF2, alpha)
//                    : ColorUtil.multAlpha(0xFF697481, alpha);
//            int statusColor = entry.purchased
//                    ? ColorUtil.multAlpha(0xFF66E08A, alpha)
//                    : ColorUtil.multAlpha(0xFFFF6F6F, alpha);
//            String elapsed = formatElapsedTime(entry.updatedAtMs > 0L ? entry.updatedAtMs : entry.createdAtMs, now);
//            float timeWidth = entryMetaFont.getStringWidth(elapsed);
//            float timeX = drawX + listW - shift - 8.0F - timeWidth;
//
//            rectangle.render(ShapeProperties.create(context.getMatrices(), drawX, drawY, listW - shift, HISTORY_ENTRY_HEIGHT)
//                    .round(6).thickness(1.0F).outlineColor(entryOutline).color(entryFill).build());
//
//            Render2DUtil.defaultDrawStack(context, entry.stack, drawX + 6.0F, drawY + 7.0F, false, false, 1.0F);
//
//            String title = entryTitleFont.trimToWidth(entry.title, (int) Math.max(24.0F, listW - 56.0F - timeWidth), false);
//            String status = (entry.purchased ? "\u041a\u0443\u043f\u043b\u0435\u043d\u043e" : "\u041d\u0435 \u043a\u0443\u043f\u043b\u0435\u043d\u043e") + " x" + entry.count;
//            entryTitleFont.drawString(context.getMatrices(), title, drawX + 28.0F, drawY + 7.0F, ColorUtil.multAlpha(0xFFF4F7FB, alpha));
//            entryMetaFont.drawString(context.getMatrices(), elapsed, timeX, drawY + 8.0F, ColorUtil.multAlpha(0xFFAEB8C2, alpha));
//            entryMetaFont.drawString(context.getMatrices(), status, drawX + 28.0F, drawY + 18.0F, statusColor);
//        }
//        scissor.pop();
//
//        float contentHeight = visibleEntries <= 0
//                ? 0.0F
//                : visibleEntries * (HISTORY_ENTRY_HEIGHT + HISTORY_ENTRY_GAP) - HISTORY_ENTRY_GAP;
//        if (contentHeight > listH + 1.0F) {
//            float trackX = panelX + HISTORY_PANEL_WIDTH - 4.0F;
//            float thumbH = Math.max(24.0F, listH * (listH / contentHeight));
//            float maxScroll = Math.max(1.0F, contentHeight - listH);
//            float progress = MathHelper.clamp(-historyScrollAnimated / maxScroll, 0.0F, 1.0F);
//            float thumbY = listY + progress * (listH - thumbH);
//            rectangle.render(ShapeProperties.create(context.getMatrices(), trackX, listY, 2.0F, listH)
//                    .round(2).color(ColorUtil.multAlpha(0xFF0D1015, 0.65F)).build());
//            rectangle.render(ShapeProperties.create(context.getMatrices(), trackX, thumbY, 2.0F, thumbH)
//                    .round(2).color(ColorUtil.multAlpha(0xFFD7E1E9, 0.88F)).build());
//        }
//
//        if (hoveredHistoryEntry != null) {
//            context.drawItemTooltip(mc.textRenderer, hoveredHistoryEntry.stack, lastMouseX, lastMouseY);
//        }
//    }
//
//    private void drawVanillaButton(DrawContext context, ButtonBounds bounds, boolean hovered) {
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        Identifier texture = hovered ? BUTTON_HOVER_TEXTURE : BUTTON_TEXTURE;
//        context.drawGuiTexture(RenderLayer::getGuiTextured, texture, bounds.x, bounds.y, bounds.w, bounds.h);
//    }
//
//    private void drawButtonText(DrawContext context, ButtonBounds bounds, String label, String status, int labelColor, int statusColor) {
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        int labelWidth = mc.textRenderer.getWidth(label);
//        int statusWidth = mc.textRenderer.getWidth(status);
//        int totalWidth = labelWidth + 4 + statusWidth;
//        int startX = bounds.x + (bounds.w - totalWidth) / 2;
//        int textY = bounds.y + (bounds.h - 8) / 2;
//        context.drawText(mc.textRenderer, label, startX, textY, labelColor, false);
//        context.drawText(mc.textRenderer, status, startX + labelWidth + 4, textY, statusColor, false);
//    }
//
//    private String formatElapsedTime(long sinceMs, long nowMs) {
//        long elapsedSeconds = Math.max(0L, (nowMs - sinceMs) / 1000L);
//        long hours = elapsedSeconds / 3600L;
//        long minutes = (elapsedSeconds % 3600L) / 60L;
//        long seconds = elapsedSeconds % 60L;
//
//        if (hours > 0L) {
//            return hours + "ч " + minutes + "м";
//        }
//        if (minutes > 0L) {
//            return minutes + "м " + seconds + "с";
//        }
//        return seconds + "с";
//    }
//
//    private void openAuctionOnEnable() {
//        if (!isHolyWorldMode() || mc.player == null || mc.player.networkHandler == null) {
//            return;
//        }
//        if (mc.currentScreen instanceof AutoBuyScreen) {
//            return;
//        }
//        if (mc.currentScreen instanceof GenericContainerScreen screen
//                && (isAuctionScreen(screen) || isHolyWorldPurchaseConfirmScreen(screen))) {
//            return;
//        }
//        mc.player.networkHandler.sendChatCommand("ah");
//    }
//
//    private void tickAutoSetup() {
//        autoBuyScript.cleanup();
//        if (!isSpookyTimeMode()) {
//            setAutoSetupEnabled(false);
//            return;
//        }
//
//        List<AutoBuyItem> queue = getConfiguredItems();
//        if (queue.isEmpty()) {
//            setAutoSetupEnabled(false);
//            return;
//        }
//
//        if (autoSetupIndex >= queue.size()) {
//            setAutoSetupEnabled(false);
//            return;
//        }
//
//        if (autoSetupItem == null) {
//            autoSetupItem = queue.get(autoSetupIndex);
//        }
//
//        switch (autoSetupStage) {
//            case 0 -> {
//                if (!autoSetupWatch.finished(220)) {
//                    return;
//                }
//                String searchName = autoSetupItem.getSearchName();
//                if (searchName == null || searchName.isBlank()) {
//                    autoSetupIndex++;
//                    autoSetupItem = null;
//                    autoSetupWatch.reset();
//                    return;
//                }
//                if (mc.player != null && mc.player.networkHandler != null) {
//                    mc.player.networkHandler.sendChatCommand("ah search " + searchName);
//                }
//                autoSetupStage = 1;
//                autoSetupWatch.reset();
//            }
//            case 1 -> {
//                if (mc.currentScreen instanceof GenericContainerScreen screen && isSpookyAuctionScreen(screen)) {
//                    autoSetupStage = 2;
//                    autoSetupWatch.reset();
//                    return;
//                }
//                if (autoSetupWatch.finished(2000)) {
//                    autoSetupStage = 0;
//                    autoSetupIndex++;
//                    autoSetupItem = null;
//                    autoSetupWatch.reset();
//                }
//            }
//            case 2 -> {
//                if (!(mc.currentScreen instanceof GenericContainerScreen screen) || !isSpookyAuctionScreen(screen)) {
//                    autoSetupStage = 1;
//                    autoSetupWatch.reset();
//                    return;
//                }
//                if (!autoSetupWatch.finished(300)) {
//                    return;
//                }
//                int cheapest = findCheapestPrice(screen, autoSetupItem);
//                if (cheapest > 0) {
//                    float discount = autoSetupDiscount.getValue() / 100.0F;
//                    long target = Math.max(1L, Math.round(cheapest * (1.0F - discount)));
//                    autoSetupItem.setRawPrice(String.valueOf(target));
//                }
//                autoSetupStage = 3;
//                autoSetupWatch.reset();
//            }
//            case 3 -> {
//                if (mc.currentScreen != null) {
//                    mc.currentScreen.close();
//                }
//                autoSetupStage = 4;
//                autoSetupWatch.reset();
//            }
//            case 4 -> {
//                if (!autoSetupWatch.finished(260)) {
//                    return;
//                }
//                autoSetupStage = 0;
//                autoSetupIndex++;
//                autoSetupItem = null;
//                autoSetupWatch.reset();
//            }
//            default -> {
//                autoSetupStage = 0;
//                autoSetupWatch.reset();
//            }
//        }
//    }
//
//    private int findCheapestPrice(GenericContainerScreen screen, AutoBuyItem item) {
//        int cheapest = Integer.MAX_VALUE;
//        List<Slot> slots = screen.getScreenHandler().slots;
//        int endIndex = Math.min(slots.size() - 1, 44);
//        for (int i = 0; i <= endIndex; i++) {
//            Slot slot = slots.get(i);
//            if (slot == null || !slot.hasStack()) {
//                continue;
//            }
//            if (slot.inventory instanceof net.minecraft.entity.player.PlayerInventory) {
//                continue;
//            }
//            ItemStack stack = slot.getStack();
//            if (!item.matches(stack, List.of())) {
//                continue;
//            }
//            int unitPrice = priceParser.getUnitPrice(stack);
//            if (unitPrice <= 0) {
//                int totalPrice = priceParser.getPrice(stack);
//                if (totalPrice > 0) {
//                    unitPrice = totalPrice / Math.max(1, stack.getCount());
//                }
//            }
//            if (unitPrice > 0 && unitPrice < cheapest) {
//                cheapest = unitPrice;
//            }
//        }
//        return cheapest == Integer.MAX_VALUE ? -1 : cheapest;
//    }
//
//    private boolean shouldShowAuctionOverlay(GenericContainerScreen screen) {
//        long now = System.currentTimeMillis();
//        if (isAuctionScreen(screen)) {
//            lastAuctionSeenMs = now;
//            return true;
//        }
//        return now - lastAuctionSeenMs < 3000L;
//    }
//
//    private void adjustAutoSetupDiscount(int delta) {
//        float value = autoSetupDiscount.getValue();
//        float next = value + delta;
//        if (next < autoSetupDiscount.getMin()) {
//            next = autoSetupDiscount.getMin();
//        } else if (next > autoSetupDiscount.getMax()) {
//            next = autoSetupDiscount.getMax();
//        }
//        autoSetupDiscount.setValue(Math.round(next));
//    }
//
//    private boolean isSneaking() {
//        return mc.options != null && mc.options.sneakKey.isPressed();
//    }
//
//    private boolean isSpookyTimeMode() {
//        return "SpookyTime".equals(serverMode.getSelected());
//    }
//
//    private boolean isHolyWorldMode() {
//        return "HolyWorld".equals(serverMode.getSelected());
//    }
//
//    private int getAuctionRefreshSlot() {
//        if (isHolyWorldMode()) {
//            return HOLYWORLD_REFRESH_SLOT;
//        }
//        if (isSpookyTimeMode()) {
//            return SPOOKYTIME_REFRESH_SLOT;
//        }
//        return -1;
//    }
//
//    private Item getAuctionRefreshItem() {
//        if (isHolyWorldMode()) {
//            return Items.EMERALD;
//        }
//        if (isSpookyTimeMode()) {
//            return Items.NETHER_STAR;
//        }
//        return Items.AIR;
//    }
//
//    private int getRefreshDelayMs() {
//        if (isHolyWorldMode()) {
//            if (nextRefreshDelayMs <= 0L) {
//                nextRefreshDelayMs = randomBetween(HOLYWORLD_REFRESH_MIN_DELAY_MS, HOLYWORLD_REFRESH_MAX_DELAY_MS);
//            }
//            return (int) nextRefreshDelayMs;
//        }
//        if (isSpookyTimeMode()) {
//            return SPOOKYTIME_REFRESH_DELAY_MS;
//        }
//        return SPOOKYTIME_REFRESH_DELAY_MS;
//    }
//
//    private boolean isHolyWorldPurchaseConfirmScreen(GenericContainerScreen screen) {
//        if (!isHolyWorldMode() || screen == null) {
//            return false;
//        }
//
//        String title = AutoBuyItem.normalizeLine(screen.getTitle().getString());
//        List<Slot> slots = screen.getScreenHandler().slots;
//        return title.contains("\u043f\u043e\u043a\u0443\u043f\u043a\u0430 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430")
//                || (slots.size() >= HOLYWORLD_CONFIRM_TOP_SIZE
//                && findHolyWorldConfirmButtonSlot(slots, Items.LIME_STAINED_GLASS_PANE, HOLYWORLD_CONFIRM_ACCEPT_PREFERRED_SLOT) != null
//                && findHolyWorldConfirmButtonSlot(slots, Items.RED_STAINED_GLASS_PANE, HOLYWORLD_CONFIRM_DECLINE_PREFERRED_SLOT) != null);
//    }
//
//    private void handleHolyWorldPurchaseConfirm(GenericContainerScreen screen) {
//        if (!autoBuyScript.isFinished()) {
//            return;
//        }
//
//        List<Slot> slots = screen.getScreenHandler().slots;
//        Slot centerSlot = slots.size() > HOLYWORLD_CONFIRM_CENTER_SLOT ? slots.get(HOLYWORLD_CONFIRM_CENTER_SLOT) : null;
//        ItemStack centerStack = centerSlot != null ? centerSlot.getStack() : ItemStack.EMPTY;
//        boolean matches = holyWorldPendingConfirmationItem != null
//                && centerStack != null
//                && !centerStack.isEmpty()
//                && holyWorldPendingConfirmationItem.matches(centerStack, List.of());
//
//        Slot actionSlot = findHolyWorldConfirmButtonSlot(
//                slots,
//                matches ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE,
//                matches ? HOLYWORLD_CONFIRM_ACCEPT_PREFERRED_SLOT : HOLYWORLD_CONFIRM_DECLINE_PREFERRED_SLOT
//        );
//        if (actionSlot == null) {
//            resolvePendingHistoryEntry(false, ItemStack.EMPTY);
//            clearHolyWorldPendingConfirmation();
//            return;
//        }
//
//        int syncId = screen.getScreenHandler().syncId;
//        ItemStack confirmStack = centerStack.copy();
//        autoBuyScript.cleanup()
//                .addStep(HOLYWORLD_CONFIRM_DELAY_MS, () -> {
//                    if (matches) {
//                        buyClicks++;
//                        sendPurchasedItemToTelegram(confirmStack, holyWorldPendingUnitPrice);
//                    }
//                    resolvePendingHistoryEntry(matches, matches ? confirmStack : ItemStack.EMPTY);
//                    mc.interactionManager.clickSlot(syncId, actionSlot.id, 0, SlotActionType.PICKUP, mc.player);
//                    clearHolyWorldPendingConfirmation();
//                })
//                .addStep(BUY_ACTION_DELAY_MS, () -> {});
//    }
//
//    private Slot findHolyWorldConfirmButtonSlot(List<Slot> slots, Item paneItem, int preferredIndex) {
//        if (slots == null || paneItem == null || slots.isEmpty()) {
//            return null;
//        }
//
//        if (preferredIndex >= 0 && preferredIndex < slots.size()) {
//            Slot preferred = slots.get(preferredIndex);
//            if (preferred != null && preferred.hasStack() && preferred.getStack().getItem() == paneItem) {
//                return preferred;
//            }
//        }
//
//        int endIndex = Math.min(HOLYWORLD_CONFIRM_TOP_SIZE, slots.size());
//        for (int i = 0; i < endIndex; i++) {
//            Slot slot = slots.get(i);
//            if (slot != null && slot.hasStack() && slot.getStack().getItem() == paneItem) {
//                return slot;
//            }
//        }
//        return null;
//    }
//
//    private void expireHolyWorldPendingConfirmation() {
//        if (holyWorldPendingConfirmationDeadlineMs != 0L
//                && System.currentTimeMillis() > holyWorldPendingConfirmationDeadlineMs) {
//            resolvePendingHistoryEntry(false, ItemStack.EMPTY);
//            clearHolyWorldPendingConfirmation();
//        }
//    }
//
//    private void clearHolyWorldPendingConfirmation() {
//        holyWorldPendingConfirmationItem = null;
//        holyWorldPendingUnitPrice = -1;
//        holyWorldPendingConfirmationDeadlineMs = 0L;
//        holyWorldPendingHistoryEntry = null;
//    }
//
//    private boolean handleHolyWorldSessionTimeout() {
//        if (!isHolyWorldMode()) {
//            return false;
//        }
//
//        long now = System.currentTimeMillis();
//        if (holyWorldSessionDeadlineMs == 0L) {
//            holyWorldSessionDeadlineMs = now + randomBetween(HOLYWORLD_SESSION_MIN_MS, HOLYWORLD_SESSION_MAX_MS);
//        }
//
//        if (now < holyWorldSessionDeadlineMs) {
//            return false;
//        }
//
//        autoBuyScript.cleanup();
//        if (mc.currentScreen != null) {
//            mc.currentScreen.close();
//        }
//        startHolyWorldFrenzy(now);
//        return true;
//    }
//
//    private void tickHolyWorldLook() {
//        if (!autoBuyEnabled || !isHolyWorldMode() || mc.player == null) {
//            return;
//        }
//
//        long now = System.currentTimeMillis();
//        boolean inAuction = mc.currentScreen instanceof GenericContainerScreen screen && isAuctionScreen(screen);
//
//        if (!inAuction && holyWorldSessionDeadlineMs > 0L && holyWorldFrenzyDeadlineMs == 0L && now - lastAuctionSeenMs > 2500L) {
//            holyWorldSessionDeadlineMs = 0L;
//        }
//
//        if (inAuction) {
//            if (holyWorldSessionDeadlineMs == 0L) {
//                holyWorldSessionDeadlineMs = now + randomBetween(HOLYWORLD_SESSION_MIN_MS, HOLYWORLD_SESSION_MAX_MS);
//            }
//            if (holyWorldLookWatch.finished(holyWorldNextLookDelayMs)) {
//                rotateHolyWorldHead(false);
//            }
//            return;
//        }
//
//        if (holyWorldFrenzyDeadlineMs > now) {
//            if (holyWorldLookWatch.finished(holyWorldNextLookDelayMs)) {
//                rotateHolyWorldHead(true);
//            }
//            return;
//        }
//
//        if (holyWorldFrenzyDeadlineMs != 0L) {
//            holyWorldFrenzyDeadlineMs = 0L;
//            RotationController.INSTANCE.reset();
//            if (mc.player != null && mc.player.networkHandler != null) {
//                mc.player.networkHandler.sendChatCommand("ah");
//                refreshWatch.reset();
//                scanWatch.reset();
//                nextRefreshDelayMs = 0L;
//            }
//        }
//    }
//
//    private void startHolyWorldFrenzy(long now) {
//        holyWorldSessionDeadlineMs = 0L;
//        holyWorldFrenzyDeadlineMs = now + randomBetween(HOLYWORLD_FRENZY_MIN_MS, HOLYWORLD_FRENZY_MAX_MS);
//        holyWorldNextLookDelayMs = 0L;
//        holyWorldLookWatch.reset();
//    }
//
//    private void rotateHolyWorldHead(boolean frenzy) {
//        ThreadLocalRandom random = ThreadLocalRandom.current();
//
//        // "Aggressive 360" logic: Vary speed and range
//        float speedMod = 0.85f + random.nextFloat() * 1.65f;
//        if (frenzy) speedMod *= 3.0f; // Ludicrous speed during frenzy
//
//        float yaw = random.nextFloat() * 360.0F - 180.0F;
//        float pitch = frenzy
//                ? random.nextFloat() * 179.0F - 89.5F
//                : random.nextFloat() * 132.0F - 60.0F;
//
//        Angle target = new Angle(yaw, pitch);
//
//        RotationController.INSTANCE.rotateTo(
//                new Angle.VecRotation(target, target.toVector()),
//                mc.player,
//                500, // Keep last rotation held active
//                HOLYWORLD_ROTATION_CONFIG,
//                TaskPriority.HIGH_IMPORTANCE_3,
//                this
//        );
//
//        long minD = frenzy ? 5L : 25L;
//        long maxD = frenzy ? 35L : 85L;
//
//        holyWorldNextLookDelayMs = (long)(randomBetween(minD, maxD) / speedMod);
//        holyWorldLookWatch.reset();
//    }
//
//    private void resetHolyWorldState() {
//        holyWorldSessionDeadlineMs = 0L;
//        holyWorldFrenzyDeadlineMs = 0L;
//        holyWorldNextLookDelayMs = 0L;
//        holyWorldLookWatch.reset();
//        resolvePendingHistoryEntry(false, ItemStack.EMPTY);
//        clearHolyWorldPendingConfirmation();
//    }
//
//    private long randomBetween(long minInclusive, long maxInclusive) {
//        return ThreadLocalRandom.current().nextLong(minInclusive, maxInclusive + 1L);
//    }
//}
