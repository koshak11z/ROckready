package im.zov4ik.features.impl.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.Setting;
import im.zov4ik.features.module.setting.implement.BindSetting;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.features.module.setting.implement.TextSetting;
import im.zov4ik.features.module.setting.implement.ValueSetting;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.features.price.HolyWorldPriceParser;
import im.zov4ik.utils.features.price.SpookyTimePriceParser;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.utils.display.geometry.Render2D;
import im.zov4ik.utils.display.scissor.ScissorAssist;
import im.zov4ik.utils.math.time.StopWatch;
import im.zov4ik.utils.math.task.TaskPriority;
import im.zov4ik.utils.math.script.Script;
import im.zov4ik.zov4ik;
import im.zov4ik.events.container.HandledScreenEvent;
import im.zov4ik.events.keyboard.KeyEvent;
import im.zov4ik.events.keyboard.MouseScrollEvent;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.utils.features.aura.rotation.Angle;
import im.zov4ik.utils.features.aura.rotation.RotationConfig;
import im.zov4ik.utils.features.aura.rotation.RotationController;
import im.zov4ik.utils.features.aura.rotation.angle.HolyWorldSmoothMode;
import im.zov4ik.features.impl.misc.autobuy.AutoBuyCategory;
import im.zov4ik.features.impl.misc.autobuy.AutoBuyItem;
import im.zov4ik.display.screens.autobuy.AutoBuyScreen;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.originalitems.ItemRegistry;
import im.zov4ik.features.impl.misc.autobuy.catalog.settings.AutoBuySettingsManager;
import im.zov4ik.features.impl.misc.funtime.FunTimeAutoParser;
import im.zov4ik.features.impl.misc.funtime.FunTimeAutoSell;
import im.zov4ik.features.impl.misc.telegram.TelegramBotBridge;
import im.zov4ik.features.impl.misc.telegram.TelegramCommand;
import im.zov4ik.utils.interactions.inv.InventoryTask;
import im.zov4ik.common.repository.staff.StaffRepository;
import im.zov4ik.utils.client.managers.file.impl.AutoBuyConfigFile;
import im.zov4ik.utils.client.managers.file.impl.ModuleFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AutoBuy extends Module {
    private static final int AUCTION_SCAN_DELAY_MS = 25;
    private static final long AUCTION_SCAN_MIN_DELAY_MS = 0L;
    private static final long AUCTION_SCAN_MAX_DELAY_MS = 1L;
    // Левый Shift зажат -> обновление аукциона раз в столько мс (турбо).
    private static final int SHIFT_TURBO_REFRESH_DELAY_MS = 100;
    // Длительность тряски (обхода) после включения AutoBuy (мс). Больше = дольше трясёт.
    private static final long SHAKE_WINDOW_MS = 4_000L;
    private static final int BUY_ACTION_DELAY_MS = 1;
    private static final int SPOOKYTIME_REFRESH_SLOT = 49;
    private static final int FUNTIME_REFRESH_SLOT = 49;
    private static final int HOLYWORLD_REFRESH_SLOT = 47;
    private static final int HOLYWORLD_CONFIRM_TOP_SIZE = 27;
    private static final int HOLYWORLD_CONFIRM_CENTER_SLOT = 13;
    private static final int HOLYWORLD_CONFIRM_ACCEPT_PREFERRED_SLOT = 10;
    private static final int HOLYWORLD_CONFIRM_DECLINE_PREFERRED_SLOT = 16;
    private static final int SPOOKYTIME_REFRESH_DELAY_MS = 450;
    private static final int HOLYWORLD_REFRESH_MIN_DELAY_MS = 100;
    private static final int HOLYWORLD_REFRESH_MAX_DELAY_MS = 110;
    private static final int HOLYWORLD_TEST_REFRESH_MIN_DELAY_MS = 415;
    private static final int HOLYWORLD_TEST_REFRESH_MAX_DELAY_MS = 435;
    private static final int HOLYWORLD_REFRESH_SAFE_MIN_DELAY_MS = 1_200;
    private static final int HOLYWORLD_REFRESH_SAFE_MAX_DELAY_MS = 2_900;
    private static final int HOLYWORLD_CONFIRM_DELAY_MS = 1;
    private static final long HOLYWORLD_CONFIRM_TIMEOUT_MS = 5000L;
    private static final int HOLYWORLD_REFRESHES_BEFORE_RELOG_MIN = 400;
    private static final int HOLYWORLD_REFRESHES_BEFORE_RELOG_MAX = 500;
    private static final long HOLYWORLD_RANDOM_WALK_MIN_MS = 20_000L;
    private static final long HOLYWORLD_RANDOM_WALK_MAX_MS = 40_000L;
    private static final long HOLYWORLD_POST_SELL_WALK_MIN_MS = 5_000L;
    private static final long HOLYWORLD_POST_SELL_WALK_MAX_MS = 20_000L;
    private static final long HOLYWORLD_RETURN_BUFFER_MS = 5_000L;
    private static final long HOLYWORLD_RETURN_TIMEOUT_MS = 8_000L;
    private static final double HOLYWORLD_RETURN_DISTANCE = 1.15D;
    private static final double HOLYWORLD_STEP_BACK_DISTANCE = 1.0D;
    private static final long HOLYWORLD_STEP_BACK_TIMEOUT_MS = 2_500L;
    private static final long HOLYWORLD_MOVE_SWITCH_MIN_MS = 280L;
    private static final long HOLYWORLD_MOVE_SWITCH_MAX_MS = 900L;
    private static final long HOLYWORLD_MOVE_SWITCH_GENTLE_MIN_MS = 620L;
    private static final long HOLYWORLD_MOVE_SWITCH_GENTLE_MAX_MS = 2_100L;
    private static final float HOLYWORLD_ROTATE_STEP_YAW_AGGRESSIVE = 14.0F;
    private static final float HOLYWORLD_ROTATE_STEP_YAW_CALM = 9.0F;
    private static final float HOLYWORLD_ROTATE_STEP_PITCH_AGGRESSIVE = 8.0F;
    private static final float HOLYWORLD_ROTATE_STEP_PITCH_CALM = 5.0F;
    private static final long HOLYWORLD_ROUTE_LOOK_MIN_MS = 2_000L;
    private static final long HOLYWORLD_ROUTE_LOOK_MAX_MS = 4_500L;
    private static final long HOLYWORLD_ROUTE_STEP_TIMEOUT_MS = 40_000L;
    private static final long HOLYWORLD_TEST_SESSION_MIN_MS = 100_000L;
    private static final long HOLYWORLD_TEST_SESSION_MAX_MS = 140_000L;
    private static final long HOLYWORLD_TEST_FRENZY_MIN_MS = 5_000L;
    private static final long HOLYWORLD_TEST_FRENZY_MAX_MS = 25_000L;
    private static final long AUTOSETUP_REFRESH_MIN_DELAY_MS = 120L;
    private static final long AUTOSETUP_REFRESH_MAX_DELAY_MS = 220L;
    private static final int AUTOSETUP_REFRESH_CLICKS_MIN = 1;
    private static final int AUTOSETUP_REFRESH_CLICKS_MAX = 2;
    // Максимум времени на настройку одного предмета в AutoSetup (мс).
    private static final long AUTOSETUP_ITEM_MAX_MS = 2_000L;
    private static final long SCRIPTED_LOOK_MIN_MS = 10_000L;
    private static final long SCRIPTED_LOOK_MAX_MS = 25_000L;
    private static final Vec3d HOLYWORLD_ROUTE_POINT_ONE = new Vec3d(21.0D, 102.0D, 27.0D);
    private static final Vec3d HOLYWORLD_ROUTE_POINT_TWO = new Vec3d(59.0D, 102.0D, 34.0D);
    private static final int HOLYWORLD_STORAGE_SLOT = 45;
    private static final long HOLYWORLD_STORAGE_ACTION_DELAY_MS = 90L;
    private static final long HOLYWORLD_STORAGE_ACTION_DELAY_MIN_MS = 65L;
    private static final long HOLYWORLD_STORAGE_ACTION_DELAY_MAX_MS = 360L;
    private static final long HOLYWORLD_STORAGE_PREPARE_TIMEOUT_MS = 7_000L;
    private static final long HOLYWORLD_STORAGE_PREPARE_TIMEOUT_MIN_MS = 5_400L;
    private static final long HOLYWORLD_STORAGE_PREPARE_TIMEOUT_MAX_MS = 12_000L;
    private static final int HOLYWORLD_AUTOSELL_MAX_ATTEMPTS = 96;
    private static final int HOLYWORLD_AUTOSELL_MAX_ATTEMPTS_SAFE = 42;
    private static final int HOLYWORLD_AUTOSELL_MAX_CONFIRM_ATTEMPTS = 18;
    private static final int HOLYWORLD_AUTOSELL_NOFILTER_STREAK_LIMIT = 6;
    private static final int HOLYWORLD_AUTOSELL_NOFILTER_STREAK_SAFE_LIMIT = 4;
    private static final int HOLYWORLD_AUTOSELL_NOFILTER_TOTAL_LIMIT = 24;
    private static final int HOLYWORLD_AUTOSELL_NOFILTER_TOTAL_SAFE_LIMIT = 14;
    private static final long HOLYWORLD_AUTOSELL_NOFILTER_BACKOFF_MIN_MS = 15_000L;
    private static final long HOLYWORLD_AUTOSELL_NOFILTER_BACKOFF_MAX_MS = 45_000L;
    private static final long HOLYWORLD_AUTOSELL_RETRY_MIN_MS = 2_600L;
    private static final long HOLYWORLD_AUTOSELL_RETRY_MAX_MS = 11_800L;
    private static final long HOLYWORLD_AUTOSELL_COMMAND_DELAY_MS = 300L;
    private static final long HOLYWORLD_AUTOSELL_COMMAND_DELAY_MIN_MS = 220L;
    private static final long HOLYWORLD_AUTOSELL_COMMAND_DELAY_MAX_MS = 980L;
    private static final long HOLYWORLD_AUTOSELL_WAIT_PRICE_TIMEOUT_MS = 4_500L;
    private static final long HOLYWORLD_AUTOSELL_WAIT_CONFIRM_TIMEOUT_MS = 4_500L;
    private static final long HOLYWORLD_AUTOSELL_WAIT_PRICE_TIMEOUT_MIN_MS = 3_200L;
    private static final long HOLYWORLD_AUTOSELL_WAIT_PRICE_TIMEOUT_MAX_MS = 8_600L;
    private static final long HOLYWORLD_AUTOSELL_WAIT_CONFIRM_TIMEOUT_MIN_MS = 3_000L;
    private static final long HOLYWORLD_AUTOSELL_WAIT_CONFIRM_TIMEOUT_MAX_MS = 8_000L;
    private static final Pattern HOLYWORLD_AUTOSELL_OFFER_PATTERN = Pattern.compile("\\[(.+?)]\\s*x\\s*(\\d+).+?([0-9\\s]{2,})");
    private static final Pattern HOLYWORLD_AUTOSELL_PRICE_PATTERN = Pattern.compile("([0-9][0-9\\s]{1,})");
    private static final Pattern HOLYWORLD_SCOREBOARD_ANARCHY_PATTERN = Pattern.compile("#\\s*(\\d+)");
    private static final long HOLYWORLD_REOPEN_AH_DELAY_MS = 3_000L;
    private static final long HOLYWORLD_REOPEN_AH_DELAY_MIN_MS = 2_400L;
    private static final long HOLYWORLD_REOPEN_AH_DELAY_MAX_MS = 7_600L;
    private static final long HOLYWORLD_RCT_REUSE_COOLDOWN_MS = 45L * 60L * 1000L;
    private static final int HOLYWORLD_RCT_RETRY_MAX_ATTEMPTS = 2;
    private static final long HOLYWORLD_RCT_RETRY_MIN_DELAY_MS = 3_000L;
    private static final long HOLYWORLD_RCT_RETRY_MAX_DELAY_MS = 8_500L;
    private static final long HOLYWORLD_RCT_HUB_WAIT_TIMEOUT_MS = 22_000L;
    private static final long HOLYWORLD_RCT_RESULT_WAIT_MS = 8_000L;
    private static final int HOLYWORLD_RCT_COMPASS_RECOVERY_MAX_ATTEMPTS = 4;
    private static final long HOLYWORLD_RCT_COMPASS_RECOVERY_DELAY_MS = 1_800L;
    private static final long HOLYWORLD_TIMED_SELL_TRANSFER_TIMEOUT_MS = 22_000L;
    private static final long HOLYWORLD_TIMED_SELL_TRANSFER_TIMEOUT_MIN_MS = 16_000L;
    private static final long HOLYWORLD_TIMED_SELL_TRANSFER_TIMEOUT_MAX_MS = 40_000L;
    private static final long HOLYWORLD_TIMED_SELL_AH_OPEN_RETRY_MS = 3_000L;
    private static final long HOLYWORLD_TIMED_SELL_AH_OPEN_RETRY_MIN_MS = 2_200L;
    private static final long HOLYWORLD_TIMED_SELL_AH_OPEN_RETRY_MAX_MS = 6_400L;
    private static final int HOLYWORLD_STAFF_FOLLOW_TO_HUB_THRESHOLD = 3;
    private static final long HOLYWORLD_STAFF_FOLLOW_WINDOW_MS = 15L * 60L * 1000L;
    private static final long HOLYWORLD_STAFF_HUB_COOLDOWN_MS = 5L * 60L * 1000L;
    private static final long HOLYWORLD_STAFF_ACTION_DELAY_MS = 1_200L;
    private static final long HOLYWORLD_PERIODIC_BREAK_INTERVAL_MS = 30L * 60L * 1000L;
    private static final long HOLYWORLD_PERIODIC_BREAK_INTERVAL_MIN_MS = 25L * 60L * 1000L;
    private static final long HOLYWORLD_PERIODIC_BREAK_INTERVAL_MAX_MS = 35L * 60L * 1000L;
    private static final long HOLYWORLD_PERIODIC_BREAK_WALK_MIN_MS = 100_000L;
    private static final long HOLYWORLD_PERIODIC_BREAK_WALK_MAX_MS = 140_000L;
    private static final long HOLYWORLD_PERIODIC_BREAK_PAUSE_MIN_MS = 3L * 60L * 1000L;
    private static final long HOLYWORLD_PERIODIC_BREAK_PAUSE_MAX_MS = 5L * 60L * 1000L;
    private static final long HOLYWORLD_PERIODIC_BREAK_HUB_MIN_MS = 20_000L;
    private static final long HOLYWORLD_PERIODIC_BREAK_HUB_MAX_MS = 40_000L;
    private static final long HOLYWORLD_PERIODIC_BREAK_HUB_RETRY_MS = 6_000L;
    private static final long HOLYWORLD_PERIODIC_BREAK_HUB_RETRY_MIN_MS = 4_500L;
    private static final long HOLYWORLD_PERIODIC_BREAK_HUB_RETRY_MAX_MS = 10_500L;
    private static final long HOLYWORLD_PERIODIC_BREAK_REJOIN_TIMEOUT_MS = 90_000L;
    private static final int HOLYWORLD_RANDOM_RCT_MIN = 1;
    private static final int HOLYWORLD_RANDOM_RCT_MAX = 64;
    private static final int HOLYWORLD_RANDOM_RCT_HARD_MAX = 64;
    private static final int[] AUTOSELL_SCAN_STEPS = new int[]{1, 5, 7, 11, 13, 17, 19, 23, 25, 29, 31, 35};
    private static final Set<Integer> HOLYWORLD_BLOCKED_RCT = Set.of(2, 18, 39, 55);
    private static final long CHAT_TYPE_OPEN_DELAY_MIN_MS = 45L;
    private static final long CHAT_TYPE_OPEN_DELAY_MAX_MS = 180L;
    private static final long CHAT_TYPE_CHAR_DELAY_MIN_MS = 18L;
    private static final long CHAT_TYPE_CHAR_DELAY_MAX_MS = 95L;
    private static final long CHAT_TYPE_BETWEEN_COMMANDS_MIN_MS = 220L;
    private static final long CHAT_TYPE_BETWEEN_COMMANDS_MAX_MS = 650L;
    private static final long CHAT_TYPE_FAST_OPEN_DELAY_MIN_MS = 32L;
    private static final long CHAT_TYPE_FAST_OPEN_DELAY_MAX_MS = 120L;
    private static final long CHAT_TYPE_FAST_CHAR_DELAY_MIN_MS = 11L;
    private static final long CHAT_TYPE_FAST_CHAR_DELAY_MAX_MS = 58L;
    private static final long CHAT_TYPE_FAST_BETWEEN_COMMANDS_MIN_MS = 120L;
    private static final long CHAT_TYPE_FAST_BETWEEN_COMMANDS_MAX_MS = 360L;
    private static final long CHAT_TYPE_ULTRA_FAST_OPEN_DELAY_MIN_MS = 12L;
    private static final long CHAT_TYPE_ULTRA_FAST_OPEN_DELAY_MAX_MS = 46L;
    private static final long CHAT_TYPE_ULTRA_FAST_CHAR_DELAY_MIN_MS = 4L;
    private static final long CHAT_TYPE_ULTRA_FAST_CHAR_DELAY_MAX_MS = 19L;
    private static final long CHAT_TYPE_ULTRA_FAST_BETWEEN_COMMANDS_MIN_MS = 45L;
    private static final long CHAT_TYPE_ULTRA_FAST_BETWEEN_COMMANDS_MAX_MS = 135L;
    private static final int HISTORY_PANEL_WIDTH = 188;
    private static final int HISTORY_HEADER_HEIGHT = 30;
    private static final int HISTORY_ENTRY_HEIGHT = 31;
    private static final int HISTORY_ENTRY_GAP = 6;
    private static final int HISTORY_MAX_ENTRIES = 4500;
    private static final long HISTORY_REMOVE_ANIM_MS = 180L;
    private static final long HISTORY_MERGE_WINDOW_MS = 5_000L;
    private static final long PURCHASE_CONFIRM_TIMEOUT_MS = 2_500L;
    private static final long PURCHASE_SUCCESS_MESSAGE_WINDOW_MS = 200L;
    private static final long PURCHASE_REBUY_GUARD_MS = 200L;
    private static final long AUCTION_FINGERPRINT_STABLE_MS = 120L;
    private static final String PURCHASE_FAILED_PREFIX = "\u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0437\u0430\u0431\u0440\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442";
    private static final Identifier BUTTON_TEXTURE = Identifier.of("minecraft", "widget/button");
    private static final Identifier BUTTON_HOVER_TEXTURE = Identifier.of("minecraft", "widget/button_highlighted");
    private static final DateTimeFormatter DEBUG_AB_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DEBUG_AB_FILE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Pattern DEBUG_AB_BAN_FILTER = Pattern.compile("(?iu)(net\\s*vision|Р°РЅС‚Рё\\s*С‡РёС‚|Р°РЅС‚РёС‡РёС‚)");
    private static final long DEBUG_AB_HEARTBEAT_MS = 15_000L;
    private static final long DEBUG_AB_COMMAND_BURST_WINDOW_MS = 10_000L;
    private static final long DEBUG_AB_COMMAND_BURST_WARN_COUNT = 6L;
    private static final long DEBUG_AB_PREBAN_WINDOW_MS = 90_000L;
    private static final int DEBUG_AB_RECENT_EVENTS_LIMIT = 340;
    private static final long COMMAND_RATE_WINDOW_MS = 10_000L;
    private static final int COMMAND_RATE_MAX = 6;
    private static final int COMMAND_RATE_SAFE_MAX = 4;
    private static final long COMMAND_MIN_INTERVAL_MS = 280L;
    private static final long COMMAND_SAFE_MIN_INTERVAL_MS = 900L;
    private static final long SLOT_RATE_WINDOW_MS = 5_000L;
    private static final int SLOT_RATE_MAX = 70;
    private static final int SLOT_RATE_SAFE_MAX = 20;
    private static final long SLOT_MIN_INTERVAL_MS = 55L;
    private static final long SLOT_SAFE_MIN_INTERVAL_MS = 130L;
    private static final int HOLYWORLD_STORAGE_SLOT_MISS_BEFORE_CLOSE = 3;
    private static final int HOLYWORLD_SAFE_REFRESH_BURST_MIN = 45;
    private static final int HOLYWORLD_SAFE_REFRESH_BURST_MAX = 90;
    private static final long HOLYWORLD_SAFE_REFRESH_PAUSE_MIN_MS = 28_000L;
    private static final long HOLYWORLD_SAFE_REFRESH_PAUSE_MAX_MS = 95_000L;
    private static final int HOLYWORLD_NORMAL_REFRESH_BURST_MIN = 120;
    private static final int HOLYWORLD_NORMAL_REFRESH_BURST_MAX = 240;
    private static final long HOLYWORLD_NORMAL_REFRESH_PAUSE_MIN_MS = 900L;
    private static final long HOLYWORLD_NORMAL_REFRESH_PAUSE_MAX_MS = 2_800L;
    private static final Pattern SALE_MESSAGE_PATTERN = Pattern.compile("^(.+?)\\s+Р В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂР В Р’В» Р РЋРЎвЂњ Р В Р вЂ Р В Р’В°Р РЋР С“\\s+\\[(.+?)]\\s*x(\\d+)\\s+Р В Р’В·Р В Р’В°\\s+([0-9\\s]+).*$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern PURCHASE_MESSAGE_PATTERN = Pattern.compile("^\\u0432\\u044b\\s+\\u043a\\u0443\\u043f\\u0438\\u043b\\u0438\\s+\\[(.+?)]\\s*x\\s*(\\d+)\\s+\\u0443\\s+.+?\\s+\\u0437\\u0430\\s+([0-9\\s]+).*$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final RotationConfig HOLYWORLD_ROTATION_CONFIG =
            new RotationConfig(new HolyWorldSmoothMode(), true, true, false);

    BindSetting openGuiBind = new BindSetting("Open GUI", "\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u043a\u043e\u043d\u0444\u0438\u0433 AutoBuy").setKey(GLFW.GLFW_KEY_P);
    SelectSetting serverMode = new SelectSetting("Mode", "AutoBuy server mode")
            .value("HolyWorld", "FunTime", "SpookyTime").selected("FunTime");
    SelectSetting telegramChatMode = new SelectSetting("Telegram Chat Mode", "Who can send Telegram commands")
            .value("Whitelist", "Global")
            .selected("Whitelist")
            .visible(() -> false);
    TextSetting telegramApiToken = new TextSetting("Telegram API", "Telegram bot token for AutoBuy commands")
            .setText("")
            .setMin(0)
            .setMax(128)
            .visible(() -> false);
    TextSetting telegramGroupId = new TextSetting("Telegram Group IDs", "Telegram whitelist IDs (comma separated)")
            .setText("")
            .setMin(0)
            .setMax(256)
            .visible(() -> false);
    SelectSetting autoSellMode = new SelectSetting("AutoSell", "AutoSell for HolyWorld cycle")
            .value("Off", "On")
            .selected("Off")
            .visible(this::isHolyWorldMode);
    SelectSetting autoBuyWork = new SelectSetting("AutoBuy Work", "HolyWorld behavior profile")
            .value("Default", "Test")
            .selected("Default")
            .visible(this::isHolyWorldMode);
    SelectSetting autoSellTriggerMode = new SelectSetting("AutoSell Trigger", "When to start HolyWorld AutoSell")
            .value("Walk", "Timer")
            .selected("Walk")
            .visible(() -> isHolyWorldMode() && isAutoSellEnabled());
    TextSetting autoSellTimerMinutes = new TextSetting("AutoSell Every (min)", "Interval for timed HolyWorld AutoSell")
            .setText("5")
            .setMin(1)
            .setMax(3)
            .visible(this::isTimedAutoSellEnabled);
    TextSetting autoSellTimerLight = new TextSetting("AutoSell Light", "Light anarchy number used for timed AutoSell")
            .setText("1")
            .setMin(1)
            .setMax(2)
            .visible(this::isTimedAutoSellEnabled);
    BooleanSetting safeMode = new BooleanSetting("Safe Mode", "Conservative mode without walk/shake and with strict delays")
            .setValue(false)
            .visible(this::isHolyWorldMode);
    BooleanSetting leaveFromStaff = new BooleanSetting("Leave From Staff", "Leave to another anarchy when tracked staff joins")
            .setValue(false)
            .visible(this::isHolyWorldMode);
    BooleanSetting debugAb = new BooleanSetting("Debug Ab", "Writes AutoBuy debug log with ban markers")
            .setValue(false)
            .visible(this::isHolyWorldMode);
    ValueSetting autoSetupDiscount = new ValueSetting("AutoSetup Discount", "Percent from cheapest price for AutoSetup")
            .setValue(10F).range(1F, 100F).visible(() -> false);
    Map<String, List<AutoBuyItem>> itemsByMode = new LinkedHashMap<>();
    AutoBuyScreen screen;
    SpookyTimePriceParser priceParser = new SpookyTimePriceParser();
    HolyWorldPriceParser holyWorldPriceParser = new HolyWorldPriceParser();
    StopWatch refreshWatch = new StopWatch();
    StopWatch scanWatch = new StopWatch();
    Script autoBuyScript = new Script();
    TelegramBotBridge telegramBotBridge = new TelegramBotBridge();
    @NonFinal
    boolean autoBuyEnabled = false;
    @NonFinal
    boolean autoSetupEnabled = false;
    @NonFinal
    long autoBuyStartMs;
    @NonFinal
    int buyClicks;
    @NonFinal
    int refreshCount;
    @NonFinal
    long lastRefreshMs;
    @NonFinal
    long nextRefreshDelayMs;
    @NonFinal
    long nextAuctionScanDelayMs;
    @NonFinal
    long totalRefreshInterval;
    @NonFinal
    int refreshIntervals;
    @NonFinal
    AutoBuyItem autoSetupItem;
    @NonFinal
    int autoSetupIndex;
    @NonFinal
    int autoSetupStage;
    @NonFinal
    StopWatch autoSetupWatch = new StopWatch();
    @NonFinal
    long autoSetupNextActionMs;
    @NonFinal
    int autoSetupRefreshesLeft;
    @NonFinal
    boolean autoSetupChangedAnyPrice;
    @NonFinal
    int lastMouseX, lastMouseY;
    @NonFinal
    ButtonBounds autoBuyBounds;
    @NonFinal
    ButtonBounds autoSellBounds;
    @NonFinal
    ButtonBounds autoSetupBounds;
    @NonFinal
    ButtonBounds autoSetupSliderBounds;
    @NonFinal
    boolean autoSetupSliderDragging;
    @NonFinal
    long lastAuctionSeenMs;
    @NonFinal
    ButtonBounds historyListBounds;
    @NonFinal
    ButtonBounds hoveredHistoryEntryBounds;
    @NonFinal
    float historyScroll;
    @NonFinal
    float historyScrollAnimated;
    @NonFinal
    PurchaseHistoryEntry hoveredHistoryEntry;
    @NonFinal
    List<PurchaseHistoryEntry> purchaseHistory = new ArrayList<>();
    @NonFinal
    LinkedHashSet<String> blockedAuctionListings = new LinkedHashSet<>();
    @NonFinal
    StopWatch holyWorldLookWatch = new StopWatch();
    @NonFinal
    long holyWorldNextLookDelayMs;
    @NonFinal
    long holyWorldWalkDeadlineMs;
    @NonFinal
    long holyWorldTestSessionDeadlineMs;
    @NonFinal
    long holyWorldTestFrenzyDeadlineMs;
    @NonFinal
    long holyWorldStageStartedMs;
    @NonFinal
    long holyWorldNextMoveSwitchMs;
    @NonFinal
    long holyWorldTestWalkPauseUntilMs;
    @NonFinal
    long holyWorldTestWalkMoveUntilMs;
    @NonFinal
    long holyWorldTestWalkRestUntilMs;
    @NonFinal
    long holyWorldTestWalkSampleAtMs;
    @NonFinal
    Vec3d holyWorldTestWalkSamplePos = Vec3d.ZERO;
    @NonFinal
    int holyWorldTestWalkStuckStrikes;
    @NonFinal
    boolean scriptedLookActive;
    @NonFinal
    int scriptedLookPhase;
    @NonFinal
    long scriptedLookUntilMs;
    @NonFinal
    long scriptedLookNextStepMs;
    @NonFinal
    float scriptedLookRemainingSpinDeg;
    @NonFinal
    long scriptedLookCooldownUntilMs;
    @NonFinal
    boolean holyWorldMoveBack;
    @NonFinal
    boolean holyWorldMoveStrafeLeft;
    @NonFinal
    boolean holyWorldMoveStrafeRight;
    @NonFinal
    boolean holyWorldMoveJump;
    @NonFinal
    boolean holyWorldMoveSprint = true;
    @NonFinal
    long holyWorldLastRelogRefreshMark;
    @NonFinal
    int holyWorldNextRelogRefreshTarget = HOLYWORLD_REFRESHES_BEFORE_RELOG_MIN;
    @NonFinal
    long holyWorldSafeRefreshPauseUntilMs;
    @NonFinal
    int holyWorldSafeRefreshBurstClicks;
    @NonFinal
    int holyWorldSafeRefreshBurstTarget;
    @NonFinal
    long holyWorldNormalRefreshPauseUntilMs;
    @NonFinal
    int holyWorldNormalRefreshBurstClicks;
    @NonFinal
    int holyWorldNormalRefreshBurstTarget;
    @NonFinal
    boolean holyWorldNeedAuctionReopen;
    @NonFinal
    StopWatch holyWorldAuctionReopenWatch = new StopWatch();
    @NonFinal
    Vec3d holyWorldWalkStartPos = Vec3d.ZERO;
    @NonFinal
    Vec3d holyWorldStepBackStartPos = Vec3d.ZERO;
    @NonFinal
    Vec3d holyWorldCurrentWalkTarget = Vec3d.ZERO;
    @NonFinal
    Vec3d holyWorldRoutePointOneTarget = HOLYWORLD_ROUTE_POINT_ONE;
    @NonFinal
    Vec3d holyWorldRoutePointTwoTarget = HOLYWORLD_ROUTE_POINT_TWO;
    @NonFinal
    long holyWorldPostSellWalkUntilMs;
    @NonFinal
    int holyWorldRouteStep;
    @NonFinal
    long holyWorldRouteStepStartedMs;
    @NonFinal
    long holyWorldRouteInspectUntilMs;
    @NonFinal
    Vec3d holyWorldRouteReturnTarget = Vec3d.ZERO;
    @NonFinal
    int holyWorldRouteDurationCycle;
    @NonFinal
    HolyWorldPeriodicBreakState holyWorldPeriodicBreakState = HolyWorldPeriodicBreakState.IDLE;
    @NonFinal
    long holyWorldNextPeriodicBreakAtMs;
    @NonFinal
    long holyWorldPeriodicBreakWalkEndMs;
    @NonFinal
    long holyWorldPeriodicBreakHubEndMs;
    @NonFinal
    long holyWorldPeriodicBreakStateStartedMs;
    @NonFinal
    long holyWorldPeriodicBreakLastHubCommandMs;
    @NonFinal
    long holyWorldPeriodicBreakHoldUntilMs;
    @NonFinal
    int holyWorldPeriodicBreakTargetIndex;
    @NonFinal
    int holyWorldPeriodicBreakTargetAnarchy = -1;
    @NonFinal
    HolyWorldWalkState holyWorldWalkState = HolyWorldWalkState.IDLE;
    @NonFinal
    Map<Integer, Long> holyWorldRecentAnarchyEntries = new HashMap<>();
    @NonFinal
    HolyWorldAutoSellState holyWorldAutoSellState = HolyWorldAutoSellState.IDLE;
    @NonFinal
    long holyWorldAutoSellStateStartedMs;
    @NonFinal
    long holyWorldAutoSellLastActionMs;
    @NonFinal
    int holyWorldAutoSellAttempts;
    @NonFinal
    int holyWorldAutoSellConfirmAttempts;
    @NonFinal
    int holyWorldAutoSellOfferedCount = 1;
    @NonFinal
    long holyWorldAutoSellOfferedPrice = -1L;
    @NonFinal
    long holyWorldAutoSellRequiredPrice = -1L;
    @NonFinal
    boolean holyWorldAutoSellOfferReceived;
    @NonFinal
    boolean holyWorldAutoSellConfirmReceived;
    @NonFinal
    boolean holyWorldAutoSellNoItems;
    @NonFinal
    boolean holyWorldAutoSellNeedNextItem;
    @NonFinal
    boolean holyWorldAutoSellAuctionSlotsFull;
    @NonFinal
    int holyWorldAutoSellNoFilterStreak;
    @NonFinal
    int holyWorldAutoSellNoFilterTotal;
    @NonFinal
    long holyWorldAutoSellNoFilterBackoffUntilMs;
    @NonFinal
    boolean holyWorldAutoSellPauseWalk;
    @NonFinal
    boolean holyWorldTestAutoSellPendingSelling;
    @NonFinal
    long holyWorldTestAuctionReopenAtMs;
    @NonFinal
    int holyWorldTestAuctionReopenAttempts;
    @NonFinal
    boolean holyWorldTestSellUntilDone;
    @NonFinal
    boolean holyWorldWalkStartQueued;
    @NonFinal
    String holyWorldAutoSellOfferedItem = "";
    @NonFinal
    int holyWorldAutoSellScanStartIndex;
    @NonFinal
    int holyWorldAutoSellCurrentInventoryIndex = -1;
    @NonFinal
    Set<String> holyWorldAutoSellSkippedNamesThisCycle = new HashSet<>();
    @NonFinal
    int holyWorldStorageSlotMisses;
    @NonFinal
    AutoBuyItem holyWorldPendingConfirmationItem;
    @NonFinal
    int holyWorldPendingUnitPrice = -1;
    @NonFinal
    long holyWorldPendingConfirmationDeadlineMs;
    @NonFinal
    PurchaseHistoryEntry holyWorldPendingHistoryEntry;
    @NonFinal
    AutoBuyItem pendingPurchaseConfirmationItem;
    @NonFinal
    int pendingPurchaseUnitPrice = -1;
    @NonFinal
    long pendingPurchaseConfirmationDeadlineMs;
    @NonFinal
    PurchaseHistoryEntry pendingPurchaseHistoryEntry;
    @NonFinal
    long pendingPurchaseStartedMs;
    @NonFinal
    long purchaseCooldownUntilMs;
    @NonFinal
    String lastAuctionFingerprint = "";
    @NonFinal
    String pendingAuctionFingerprint = "";
    @NonFinal
    long pendingAuctionFingerprintSinceMs;
    @NonFinal
    long autoBuyStartCoins = -1L;
    @NonFinal
    long autoBuyStartCoinsMs;
    @NonFinal
    long lastKnownCoins = -1L;
    @NonFinal
    long lastTelegramCommandChatId;
    @NonFinal
    long lastTelegramWarningMs;
    @NonFinal
    String lastTelegramWarning = "";
    @NonFinal
    long holyWorldLastRctSentMs;
    @NonFinal
    int holyWorldLastRctAnarchy = -1;
    @NonFinal
    int holyWorldPendingRctAnarchy = -1;
    @NonFinal
    int holyWorldPendingRctAttempts;
    @NonFinal
    long holyWorldNextRctRetryMs;
    @NonFinal
    boolean holyWorldPendingRctNeedCompassRecovery;
    @NonFinal
    int holyWorldPendingRctCompassAttempts;
    @NonFinal
    long holyWorldNextRctCompassRecoveryMs;
    @NonFinal
    boolean holyWorldPendingRctNeedsHubRetry;
    @NonFinal
    long holyWorldPendingRctCreatedMs;
    @NonFinal
    Deque<String> pendingVisibleChatCommands = new ArrayDeque<>();
    @NonFinal
    Deque<Boolean> pendingVisibleChatFastCommands = new ArrayDeque<>();
    @NonFinal
    boolean typingVisibleChatCommand;
    @NonFinal
    boolean typingVisibleChatFastMode;
    @NonFinal
    boolean typingVisibleChatUltraFastMode;
    @NonFinal
    String typingVisibleChatText = "";
    @NonFinal
    int typingVisibleChatIndex;
    @NonFinal
    long typingVisibleChatNextActionMs;
    @NonFinal
    int typingVisibleChatTyposRemaining;
    @NonFinal
    boolean typingVisibleChatNeedsBackspace;
    @NonFinal
    int typingVisibleChatCharsSincePause;
    @NonFinal
    int typingVisibleChatPauseEveryChars;
    @NonFinal
    long typingVisibleChatStartedMs;
    @NonFinal
    Deque<Long> commandRateWindow = new ArrayDeque<>();
    @NonFinal
    long commandLastSentMs;
    @NonFinal
    Deque<Long> slotRateWindow = new ArrayDeque<>();
    @NonFinal
    long slotLastClickMs;
    @NonFinal
    long holyWorldNextAutoSellTryMs;
    @NonFinal
    HolyWorldTimedSellState holyWorldTimedSellState = HolyWorldTimedSellState.IDLE;
    @NonFinal
    long holyWorldTimedSellNextRunMs;
    @NonFinal
    long holyWorldTimedSellStateStartedMs;
    @NonFinal
    long holyWorldTimedSellLastActionMs;
    @NonFinal
    int holyWorldTimedSellTargetAnarchy = -1;
    @NonFinal
    int holyWorldTimedSellReturnAnarchy = -1;
    @NonFinal
    int holyWorldTimedSellPlannedAnarchy = -1;
    @NonFinal
    int holyWorldTimedSellTransferAttempts;
    @NonFinal
    boolean holyWorldTimedSellSellStageStarted;
    @NonFinal
    Set<String> holyWorldKnownOnlineStaff = new HashSet<>();
    @NonFinal
    String holyWorldTrackedStaffName = "";
    @NonFinal
    int holyWorldTrackedStaffHits;
    @NonFinal
    int holyWorldLastObservedAnarchy = -1;
    @NonFinal
    int holyWorldTrackedStaffLastAnarchy = -1;
    @NonFinal
    long holyWorldTrackedStaffLastSeenMs;
    @NonFinal
    long holyWorldStaffHubCooldownUntilMs;
    @NonFinal
    boolean holyWorldStaffHubReturnQueued;
    @NonFinal
    long holyWorldStaffLastActionMs;
    @NonFinal
    Path debugAbLogPath;
    @NonFinal
    long debugAbStartedMs;
    @NonFinal
    long debugAbLastHeartbeatMs;
    @NonFinal
    boolean debugAbBanLogged;
    @NonFinal
    HolyWorldAutoSellState debugAbLastAutoSellState;
    @NonFinal
    HolyWorldTimedSellState debugAbLastTimedSellState;
    @NonFinal
    HolyWorldWalkState debugAbLastWalkState;
    @NonFinal
    HolyWorldPeriodicBreakState debugAbLastBreakState;
    @NonFinal
    int debugAbLastAnarchy = -1;
    @NonFinal
    long debugAbQueuedCommands;
    @NonFinal
    long debugAbSentCommands;
    @NonFinal
    long debugAbLastCommandSentMs;
    @NonFinal
    long debugAbCommandBurstWindowStartedMs;
    @NonFinal
    long debugAbCommandBurstCount;
    @NonFinal
    long debugAbLastTypedCommandDurationMs;
    @NonFinal
    long debugAbSlotClickAttempts;
    @NonFinal
    long debugAbSlotClickSuccess;
    @NonFinal
    long debugAbSlotClickFailures;
    @NonFinal
    long debugAbAutoSellMoveAttempts;
    @NonFinal
    long debugAbAutoSellMoveFailures;
    @NonFinal
    long debugAbRefreshDeltaMinMs;
    @NonFinal
    long debugAbRefreshDeltaMaxMs;
    @NonFinal
    long debugAbRefreshDeltaTotalMs;
    @NonFinal
    long debugAbRefreshDeltaSamples;
    @NonFinal
    float debugAbLastYaw = Float.NaN;
    @NonFinal
    float debugAbLastPitch = Float.NaN;
    @NonFinal
    float debugAbMaxYawJump;
    @NonFinal
    float debugAbMaxPitchJump;
    @NonFinal
    long debugAbYawJumpTotalScaled;
    @NonFinal
    long debugAbPitchJumpTotalScaled;
    @NonFinal
    long debugAbRotationSamples;
    @NonFinal
    int debugAbMovementMask = -1;
    @NonFinal
    long debugAbMovementMaskChangedMs;
    @NonFinal
    long debugAbMovementMaskChanges;
    @NonFinal
    Deque<DebugAbRecentEvent> debugAbRecentEvents = new ArrayDeque<>();

    public AutoBuy() {
        super("AutoBuy", "Auto Buy", ModuleCategory.MISC);
        buildCatalog();

        List<Setting> settings = new ArrayList<>();
        settings.add(openGuiBind);
        settings.add(serverMode);
        settings.add(telegramChatMode);
        settings.add(telegramApiToken);
        settings.add(telegramGroupId);
        settings.add(autoSellMode);
        settings.add(autoBuyWork);
        settings.add(autoSellTriggerMode);
        settings.add(autoSellTimerMinutes);
        settings.add(autoSellTimerLight);
        settings.add(safeMode);
        settings.add(leaveFromStaff);
        settings.add(debugAb);
        settings.add(autoSetupDiscount);
        itemsByMode.values().stream()
                .flatMap(List::stream)
                .forEach(item -> {
                    settings.add(item.getPriceSetting());
                    settings.add(item.getDurabilitySetting());
                    settings.add(item.getThornsSetting());
                    settings.add(item.getSellEnabledSetting());
                    settings.add(item.getSetupEnabledSetting());
                });
        setup(settings.toArray(Setting[]::new));

        screen = new AutoBuyScreen(this);
        state = true;
    }

    @Override
    public void deactivate() {
        if (mc.currentScreen instanceof AutoBuyScreen autoBuyScreen && autoBuyScreen.belongsTo(this)) {
            autoBuyScreen.forceClose();
        }
        autoBuyScript.cleanup();
        stopHolyWorldRotation();
        resetHolyWorldState();
        clearPendingPurchaseConfirmation();
        resetAuctionFingerprintState();
        closeDebugAbSession("module_deactivate", System.currentTimeMillis());
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (e.type() == net.minecraft.client.util.InputUtil.Type.MOUSE && e.action() == 0 && e.key() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            autoSetupSliderDragging = false;
        }
        if (e.type() == net.minecraft.client.util.InputUtil.Type.MOUSE && e.action() == 1) {
            if (mc.currentScreen instanceof GenericContainerScreen screen && shouldShowAuctionOverlay(screen)) {
                if (hoveredHistoryEntry != null
                        && hoveredHistoryEntryBounds != null
                        && hoveredHistoryEntryBounds.contains(lastMouseX, lastMouseY)
                        && (e.key() == GLFW.GLFW_MOUSE_BUTTON_RIGHT || e.key() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
                    if (holyWorldPendingHistoryEntry == hoveredHistoryEntry) {
                        holyWorldPendingHistoryEntry = null;
                    }
                    startRemovingHistoryEntry(hoveredHistoryEntry);
                    hoveredHistoryEntry = null;
                    hoveredHistoryEntryBounds = null;
                    historyScroll = MathHelper.clamp(historyScroll, getMinHistoryScroll(historyListBounds != null ? historyListBounds.h : 0), 0.0F);
                    return;
                }
                if (autoSetupSliderBounds != null && autoSetupSliderBounds.contains(lastMouseX, lastMouseY)) {
                    if (e.key() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        autoSetupSliderDragging = true;
                        updateAutoSetupPercentByMouse(lastMouseX);
                    }
                    return;
                }
                if (autoBuyBounds != null && autoBuyBounds.contains(lastMouseX, lastMouseY)) {
                    if (e.key() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        setAutoBuyEnabled(!autoBuyEnabled);
                    }
                    return;
                }
                if (autoSellBounds != null && autoSellBounds.contains(lastMouseX, lastMouseY)) {
                    if (e.key() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        if (isFunTimeMode()) {
                            toggleFunTimeAutoSell();
                        } else {
                            toggleAutoSellSetting();
                        }
                    }
                    return;
                }
                if (autoSetupBounds != null && autoSetupBounds.contains(lastMouseX, lastMouseY)) {
                    if (e.key() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        if (isFunTimeMode()) {
                            toggleFunTimeAutoParser();
                        } else {
                            if (!autoBuyEnabled) {
                                setAutoBuyEnabled(true);
                            }
                            setAutoSetupEnabled(!autoSetupEnabled);
                        }
                    }
                    return;
                }
            }
        }

        if (openGuiBind.getKey() == GLFW.GLFW_KEY_UNKNOWN) {
            return;
        }
        if (!e.isKeyDown(openGuiBind.getKey(), true)) {
            return;
        }

        if (mc.currentScreen instanceof AutoBuyScreen autoBuyScreen && autoBuyScreen.belongsTo(this)) {
            autoBuyScreen.requestClose();
            return;
        }

        screen.open();
    }

    @EventHandler
    public void onHandledScreen(HandledScreenEvent e) {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen) || !shouldShowAuctionOverlay(screen)) {
            return;
        }

        lastMouseX = e.getMouseX();
        lastMouseY = e.getMouseY();

        DrawContext context = e.getDrawContext();
        int offsetX = (screen.width - e.getBackgroundWidth()) / 2;
        int offsetY = (screen.height - e.getBackgroundHeight()) / 2;

        boolean funTimeMode = isFunTimeMode();
        boolean autoSellOn = funTimeMode ? isFunTimeAutoSellEnabled() : isAutoSellEnabled();
        boolean autoSetupOn = funTimeMode ? isFunTimeAutoParserEnabled() : autoSetupEnabled;
        String buyLabel = "AutoBuy:";
        String buyStatus = autoBuyEnabled ? "\u0432\u043a\u043b" : "\u0432\u044b\u043a\u043b";
        String sellLabel = "AutoSell:";
        String sellStatus = autoSellOn ? "\u0432\u043a\u043b" : "\u0432\u044b\u043a\u043b";
        String setupLabel = funTimeMode ? "AutoParser:" : "AutoSetup:";
        String setupStatus = autoSetupOn ? "\u0432\u043a\u043b" : "\u0432\u044b\u043a\u043b";
        int buyTextWidth = mc.textRenderer.getWidth(buyLabel) + 4 + mc.textRenderer.getWidth(buyStatus);
        int sellTextWidth = mc.textRenderer.getWidth(sellLabel) + 4 + mc.textRenderer.getWidth(sellStatus);
        int setupTextWidth = mc.textRenderer.getWidth(setupLabel) + 4 + mc.textRenderer.getWidth(setupStatus);
        int buttonHeight = 20;
        int buttonY = Math.max(2, offsetY - buttonHeight - 2);
        boolean showAutoSellButton = isHolyWorldMode() || funTimeMode;
        boolean showAutoSetupControls = isHolyWorldMode() || funTimeMode;
        boolean showAutoSetupSlider = isHolyWorldMode();
        int buttonWidth = Math.min(Math.max(112, buyTextWidth + 24), Math.max(112, e.getBackgroundWidth() - 12));
        int buttonX = offsetX + (e.getBackgroundWidth() - buttonWidth) / 2;
        int sellButtonWidth = Math.min(Math.max(112, sellTextWidth + 24), Math.max(112, e.getBackgroundWidth() - 12));
        int setupButtonWidth = Math.min(Math.max(118, setupTextWidth + 24), Math.max(118, e.getBackgroundWidth() - 12));
        int sliderWidth = 132;
        int buttonsGap = 4;
        if (showAutoSellButton) {
            int totalW = buttonWidth + buttonsGap + sellButtonWidth;
            if (showAutoSetupControls) {
                totalW += buttonsGap + setupButtonWidth;
                if (showAutoSetupSlider) totalW += buttonsGap + sliderWidth;
            }
            int maxW = Math.max(112, e.getBackgroundWidth() - 12);
            if (totalW > maxW) {
                if (showAutoSetupControls) {
                    if (showAutoSetupSlider) {
                        sliderWidth = Math.max(92, Math.min(132, maxW / 4));
                        int buttonsTotal = maxW - sliderWidth - buttonsGap * 4;
                        int each = Math.max(82, buttonsTotal / 3);
                        buttonWidth = each;
                        sellButtonWidth = each;
                        setupButtonWidth = each;
                        totalW = buttonWidth + sellButtonWidth + setupButtonWidth + sliderWidth + buttonsGap * 4;
                    } else {
                        int each = Math.max(82, (maxW - buttonsGap * 3) / 3);
                        buttonWidth = each;
                        sellButtonWidth = each;
                        setupButtonWidth = each;
                        totalW = buttonWidth + sellButtonWidth + setupButtonWidth + buttonsGap * 3;
                    }
                } else {
                    int each = Math.max(92, (maxW - buttonsGap) / 2);
                    buttonWidth = each;
                    sellButtonWidth = each;
                    totalW = buttonWidth + buttonsGap + sellButtonWidth;
                }
            }
            int startX = offsetX + (e.getBackgroundWidth() - totalW) / 2;
            autoBuyBounds = new ButtonBounds(startX, buttonY, buttonWidth, buttonHeight);
            autoSellBounds = new ButtonBounds(startX + buttonWidth + buttonsGap, buttonY, sellButtonWidth, buttonHeight);
            if (showAutoSetupControls) {
                int setupX = autoSellBounds.x + autoSellBounds.w + buttonsGap;
                autoSetupBounds = new ButtonBounds(setupX, buttonY, setupButtonWidth, buttonHeight);
                if (showAutoSetupSlider) {
                    autoSetupSliderBounds = new ButtonBounds(autoSetupBounds.x + autoSetupBounds.w + buttonsGap, buttonY, sliderWidth, buttonHeight);
                } else {
                    autoSetupSliderBounds = null;
                }
            } else {
                autoSetupBounds = null;
                autoSetupSliderBounds = null;
            }
        } else {
            autoBuyBounds = new ButtonBounds(buttonX, buttonY, buttonWidth, buttonHeight);
            autoSellBounds = null;
            autoSetupBounds = null;
            autoSetupSliderBounds = null;
            autoSetupSliderDragging = false;
        }
        if (autoSetupSliderDragging && autoSetupSliderBounds != null) {
            updateAutoSetupPercentByMouse(lastMouseX);
        }

        int labelColor = 0xFFE8E8E8;
        int offColor = 0xFFFF4B4B;
        int onColor = 0xFF4BFF4B;
        drawPurchaseHistory(context, screen, offsetX, offsetY, e.getBackgroundWidth(), e.getBackgroundHeight());

        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 400.0F);
        RenderSystem.disableDepthTest();
        boolean hoverBuy = autoBuyBounds.contains(lastMouseX, lastMouseY);
        drawVanillaButton(context, autoBuyBounds, hoverBuy);
        drawButtonText(context, autoBuyBounds, buyLabel, buyStatus, labelColor, autoBuyEnabled ? onColor : offColor);
        if (autoSellBounds != null) {
            boolean hoverSell = autoSellBounds.contains(lastMouseX, lastMouseY);
            drawVanillaButton(context, autoSellBounds, hoverSell);
            drawButtonText(context, autoSellBounds, sellLabel, sellStatus, labelColor, autoSellOn ? onColor : offColor);
        }
        if (autoSetupBounds != null) {
            boolean hoverSetup = autoSetupBounds.contains(lastMouseX, lastMouseY);
            drawVanillaButton(context, autoSetupBounds, hoverSetup);
            drawButtonText(context, autoSetupBounds, setupLabel, setupStatus, labelColor, autoSetupOn ? onColor : offColor);
        }
        if (autoSetupSliderBounds != null) {
            boolean hoverSlider = autoSetupSliderBounds.contains(lastMouseX, lastMouseY) || autoSetupSliderDragging;
            drawAutoSetupSlider(context, autoSetupSliderBounds, hoverSlider);
        }
        drawAutoBuyInfo(context, offsetX, buttonY, e.getBackgroundWidth());
        drawAuctionLabels(context, screen, offsetX, offsetY, e.getBackgroundHeight());
        RenderSystem.enableDepthTest();
        context.getMatrices().pop();
    }

    @EventHandler
    public void onMouseScroll(MouseScrollEvent e) {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen) || !shouldShowAuctionOverlay(screen)) {
            return;
        }
        if (historyListBounds == null || !historyListBounds.contains(lastMouseX, lastMouseY)) {
            return;
        }

        historyScroll += (float) e.getVertical() * 20.0F;
        historyScroll = MathHelper.clamp(historyScroll, getMinHistoryScroll(historyListBounds.h), 0.0F);
        e.cancel();
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (e.getType() != PacketEvent.Type.RECEIVE || !(e.getPacket() instanceof GameMessageS2CPacket gameMessage)) {
            return;
        }

        String rawMessage = gameMessage.content().getString();
        handleDebugAbMessage(rawMessage);
        handleSaleMessage(rawMessage);
        handlePurchaseMessage(rawMessage);
        handleHolyWorldAutoSellMessage(rawMessage);
        handleHolyWorldRelogMessage(rawMessage);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        tickTelegramCommands();
        tickVisibleChatCommandTyping();
        autoBuyScript.update();
        tickHolyWorldStaffEscape();
        tickHolyWorldAutoSell();
        if (!isAutoBuyWorkTest()) {
            tickHolyWorldTimedSell();
            tickHolyWorldPeriodicBreak();
        } else {
            resetHolyWorldTimedSellState();
            resetHolyWorldPeriodicBreakState();
        }
        tickHolyWorldLook();
        expireHolyWorldPendingConfirmation();
        expirePendingPurchaseConfirmation();
        cleanupPurchaseHistory();
        tickDebugAbStateTracking();

        if (autoSetupEnabled) {
            tickAutoSetup();
            return;
        }

        if (!isHolyWorldMode() && (holyWorldWalkState != HolyWorldWalkState.IDLE
                || holyWorldTestSessionDeadlineMs != 0L
                || holyWorldTestFrenzyDeadlineMs != 0L)) {
            RotationController.INSTANCE.reset();
            resetHolyWorldState();
        }

        if (autoBuyEnabled && autoBuyStartCoins < 0L) {
            captureStartCoins(System.currentTimeMillis());
        }

        if (!autoBuyEnabled) {
            autoBuyScript.cleanup();
            return;
        }

        if (isHolyWorldStaffEscapeBlocking()) {
            return;
        }

        if (isHolyWorldPeriodicBreakActive()) {
            return;
        }

        if (mc.currentScreen instanceof GenericContainerScreen screen && isHolyWorldPurchaseConfirmScreen(screen)) {
            handleHolyWorldPurchaseConfirm(screen);
            return;
        }

        if (mc.currentScreen instanceof AutoBuyScreen) {
            return;
        }

        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            return;
        }

        if (!isAuctionScreen(screen)) {
            return;
        }

        lastAuctionSeenMs = System.currentTimeMillis();
        updateAuctionFingerprint(screen);

        if (isHolyWorldMode() && !isAutoBuyWorkTest() && holyWorldTimedSellState != HolyWorldTimedSellState.IDLE) {
            return;
        }

        if (isHolyWorldMode() && isAutoBuyWorkTest() && handleHolyWorldTestSessionTimeout()) {
            return;
        }

        if (isHolyWorldMode() && !isAutoBuyWorkTest() && tryStartHolyWorldRelogCycle(screen)) {
            return;
        }
        if (isHolyWorldMode() && (isHolyWorldAutoSellActive()
                || holyWorldAutoSellPauseWalk
                || holyWorldTestSellUntilDone
                || holyWorldTestAutoSellPendingSelling)) {
            return;
        }

        if (!autoBuyScript.isFinished()) {
            return;
        }

        if (isHolyWorldMode() && isAutoBuyWorkTest()) {
            if (nextAuctionScanDelayMs <= 0L) {
                nextAuctionScanDelayMs = pickHolyWorldTestScanDelayMs();
            }
            if (!scanWatch.finished(nextAuctionScanDelayMs)) {
                return;
            }
            scanWatch.reset();
            nextAuctionScanDelayMs = pickHolyWorldTestScanDelayMs();
        } else {
            if (nextAuctionScanDelayMs <= 0L) {
                nextAuctionScanDelayMs = pickAuctionScanDelayMs();
            }
            if (!scanWatch.finished(nextAuctionScanDelayMs)) {
                return;
            }
            scanWatch.reset();
            nextAuctionScanDelayMs = pickAuctionScanDelayMs();
        }

        if (hasPendingPurchaseConfirmation()) {
            return;
        }

        AutoBuyCandidate candidate = findBuyCandidate(screen);
        if (candidate != null) {
            scheduleBuy(screen, candidate);
            return;
        }

        if (refreshWatch.finished(getRefreshDelayMs())) {
            long now = System.currentTimeMillis();
            // При зажатом Shift игнорируем антиспам-паузы обновления.
            if (isHolyWorldMode() && !isAutoBuyWorkTest() && !isTurboRefreshKeyHeld() && getRefreshPauseLeftMs(now) > 0L) {
                return;
            }
            clickRefresh(screen);
            refreshWatch.reset();
        }
    }

    private void tickDebugAbStateTracking() {
        long now = System.currentTimeMillis();
        if (!isDebugAbEnabled()) {
            closeDebugAbSession("debug_setting_disabled", now);
            return;
        }
        if (!autoBuyEnabled) {
            return;
        }

        ensureDebugAbSession(now);
        if (debugAbLogPath == null) {
            return;
        }

        if (debugAbLastAutoSellState != holyWorldAutoSellState) {
            logDebugAb("STATE_AUTOSELL", debugAbLastAutoSellState + " -> " + holyWorldAutoSellState);
            debugAbLastAutoSellState = holyWorldAutoSellState;
        }
        if (debugAbLastTimedSellState != holyWorldTimedSellState) {
            logDebugAb("STATE_TIMEDSELL", debugAbLastTimedSellState + " -> " + holyWorldTimedSellState);
            debugAbLastTimedSellState = holyWorldTimedSellState;
        }
        if (debugAbLastWalkState != holyWorldWalkState) {
            logDebugAb("STATE_WALK", debugAbLastWalkState + " -> " + holyWorldWalkState);
            debugAbLastWalkState = holyWorldWalkState;
        }
        if (debugAbLastBreakState != holyWorldPeriodicBreakState) {
            logDebugAb("STATE_BREAK", debugAbLastBreakState + " -> " + holyWorldPeriodicBreakState);
            debugAbLastBreakState = holyWorldPeriodicBreakState;
        }

        int currentAnarchy = getCurrentHolyWorldAnarchyFromScoreboard();
        if (currentAnarchy > 0 && currentAnarchy != debugAbLastAnarchy) {
            logDebugAb("ANARCHY", "changed_to=" + currentAnarchy);
            debugAbLastAnarchy = currentAnarchy;
        }

        trackDebugAbMovementAndRotation(now);

        if (now - debugAbLastHeartbeatMs >= DEBUG_AB_HEARTBEAT_MS) {
            long avgRefreshDelta = debugAbRefreshDeltaSamples > 0L
                    ? debugAbRefreshDeltaTotalMs / debugAbRefreshDeltaSamples
                    : -1L;
            long yawAvgScaled = debugAbRotationSamples > 0L
                    ? debugAbYawJumpTotalScaled / debugAbRotationSamples
                    : 0L;
            long pitchAvgScaled = debugAbRotationSamples > 0L
                    ? debugAbPitchJumpTotalScaled / debugAbRotationSamples
                    : 0L;
            String refreshDeltaPart = debugAbRefreshDeltaSamples > 0L
                    ? (", refresh_dt_min=" + debugAbRefreshDeltaMinMs
                    + ", refresh_dt_avg=" + avgRefreshDelta
                    + ", refresh_dt_max=" + debugAbRefreshDeltaMaxMs)
                    : ", refresh_dt_min=-1, refresh_dt_avg=-1, refresh_dt_max=-1";
            logDebugAb("HEARTBEAT",
                    "refresh=" + refreshCount
                            + ", buys=" + buyClicks
                            + ", pending_rct=" + holyWorldPendingRctAnarchy
                            + ", autosell=" + holyWorldAutoSellState
                            + ", timed_sell=" + holyWorldTimedSellState
                            + ", walk=" + holyWorldWalkState
                            + ", refresh_pause_ms=" + getRefreshPauseLeftMs(now)
                            + ", queue=" + pendingVisibleChatCommands.size()
                            + ", cmd_queued=" + debugAbQueuedCommands
                            + ", cmd_sent=" + debugAbSentCommands
                            + ", cmd_last_ms=" + (debugAbLastCommandSentMs > 0L ? (now - debugAbLastCommandSentMs) : -1L)
                            + ", last_type_ms=" + debugAbLastTypedCommandDurationMs
                            + ", move_mask=" + formatDebugMovementMask(debugAbMovementMask)
                            + ", move_changes=" + debugAbMovementMaskChanges
                            + ", yaw_j_max=" + String.format(Locale.ROOT, "%.2f", debugAbMaxYawJump)
                            + ", yaw_j_avg=" + String.format(Locale.ROOT, "%.2f", yawAvgScaled / 1000.0D)
                            + ", pitch_j_max=" + String.format(Locale.ROOT, "%.2f", debugAbMaxPitchJump)
                            + ", pitch_j_avg=" + String.format(Locale.ROOT, "%.2f", pitchAvgScaled / 1000.0D)
                            + ", slot_click_ok=" + debugAbSlotClickSuccess
                            + ", slot_click_fail=" + debugAbSlotClickFailures
                            + ", autosell_swap_fail=" + debugAbAutoSellMoveFailures
                            + refreshDeltaPart);
            debugAbLastHeartbeatMs = now;
        }
    }

    public List<AutoBuyCategory> categories() {
        return List.of(AutoBuyCategory.ALL);
    }

    public List<AutoBuyItem> getItemsByCategory(AutoBuyCategory category) {
        return getItems();
    }

    public List<AutoBuyItem> getConfiguredItems() {
        return getItems().stream()
                .filter(AutoBuyItem::hasPrice)
                .sorted(Comparator.comparingInt(this::sortBucket)
                        .thenComparing(AutoBuyItem::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<AutoBuyItem> detect(ItemStack stack, List<String> tooltipLines) {
        return getItems().stream()
                .sorted(Comparator
                        .comparing(AutoBuyItem::isNeedsAdditionalCheck).reversed()
                        .thenComparing(AutoBuyItem::getDisplayName))
                .filter(item -> item.matches(stack, tooltipLines))
                .findFirst();
    }

    public List<AutoBuyItem> getItems() {
        List<AutoBuyItem> items = itemsByMode.get(serverMode.getSelected());
        return items == null ? List.of() : List.copyOf(items);
    }

    public boolean updateFunTimeItemPrice(String itemName, int newPrice) {
        List<AutoBuyItem> items = itemsByMode.get("FunTime");
        if (items == null || items.isEmpty()) {
            return false;
        }

        String cleanTarget = cleanAutoBuyName(itemName);
        boolean updated = false;
        for (AutoBuyItem item : items) {
            String cleanDisplay = cleanAutoBuyName(item.getDisplayName());
            String cleanSearch = cleanAutoBuyName(item.getSearchName());
            if (item.getDisplayName().equalsIgnoreCase(itemName)
                    || item.getSearchName().equalsIgnoreCase(itemName)
                    || cleanDisplay.equalsIgnoreCase(cleanTarget)
                    || cleanSearch.equalsIgnoreCase(cleanTarget)
                    || (!cleanTarget.isBlank() && (cleanDisplay.contains(cleanTarget) || cleanSearch.contains(cleanTarget)))
                    || (!cleanDisplay.isBlank() && cleanTarget.contains(cleanDisplay))
                    || (!cleanSearch.isBlank() && cleanTarget.contains(cleanSearch))) {
                item.setRawPrice(String.valueOf(newPrice));
                updated = true;
            }
        }
        return updated;
    }

    private void buildCatalog() {
        itemsByMode.clear();
        ItemRegistry.reload();

        itemsByMode.put("HolyWorld", buildModeItems("HolyWorld", ItemRegistry.getHolyWorld()));
        itemsByMode.put("FunTime", buildModeItems("FunTime", ItemRegistry.getFunTimeItems()));
        itemsByMode.put("SpookyTime", buildModeItems("SpookyTime", ItemRegistry.getSpookyTime()));
    }

    private List<AutoBuyItem> buildModeItems(String modeKey, List<AutoBuyableItem> sourceItems) {
        List<AutoBuyItem> modeItems = new ArrayList<>();
        Map<String, Integer> collisionIndex = new LinkedHashMap<>();
        for (AutoBuyableItem sourceItem : sourceItems) {
            ItemStack referenceStack = sourceItem.createItemStack();
            if (referenceStack == null || referenceStack.isEmpty()) {
                continue;
            }

            String displayName = sourceItem.getDisplayName();
            String searchName = sourceItem.getSearchName();
            String baseKey = slugify(searchName != null && !searchName.isBlank() ? searchName : displayName);
            int duplicateIndex = collisionIndex.merge(baseKey, 1, Integer::sum);
            String key = duplicateIndex == 1 ? baseKey : baseKey + "_" + duplicateIndex;
            String modeAwareKey = slugify(modeKey + "_" + key);
            int savedBuyBelow = AutoBuySettingsManager.getInstance().hasSettings(displayName) && sourceItem.getSettings() != null
                    ? sourceItem.getSettings().getBuyBelow()
                    : 0;

            TextSetting priceSetting = new TextSetting("price_" + modeAwareKey, "Максимальная цена для " + displayName)
                    .setText(savedBuyBelow > 0 ? String.valueOf(savedBuyBelow) : "")
                    .setMin(0)
                    .setMax(16)
                    .visible(() -> false);

            TextSetting durabilitySetting = new TextSetting("durability_" + modeAwareKey, "\u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c, % \u0434\u043b\u044f " + displayName)
                    .setText("")
                    .setMin(0)
                    .setMax(3)
                    .visible(() -> false);

            SelectSetting thornsSetting = new SelectSetting("thorns_" + modeAwareKey, "\u0420\u0435\u0436\u0438\u043c \u0448\u0438\u043f\u043e\u0432 \u0434\u043b\u044f " + displayName)
                    .value("\u041e\u0431\u0430", "\u0428\u0438\u043f\u044b", "\u0410\u043d\u0442\u0438\u0448\u0438\u043f")
                    .selected("\u041e\u0431\u0430")
                    .visible(() -> false);

            BooleanSetting sellEnabledSetting = new BooleanSetting("sell_enabled_" + modeAwareKey, "\u041f\u0440\u043e\u043f\u0443\u0441\u043a\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 AutoSell")
                    .setValue(false)
                    .visible(() -> false);
            BooleanSetting setupEnabledSetting = new BooleanSetting("setup_enabled_" + modeAwareKey, "\u041d\u0435 \u0443\u0447\u0438\u0442\u044b\u0432\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 AutoSetup")
                    .setValue(false)
                    .visible(() -> false);

            boolean needsAdditionalCheck = sourceItem.needsAdditionalCheck() || "HolyWorld".equals(modeKey);

            modeItems.add(new AutoBuyItem(
                    modeAwareKey,
                    displayName,
                    searchName == null || searchName.isBlank() ? displayName : searchName,
                    AutoBuyCategory.ALL,
                    referenceStack,
                    priceSetting,
                    durabilitySetting,
                    thornsSetting,
                    sellEnabledSetting,
                    setupEnabledSetting,
                    needsAdditionalCheck
            ));
        }

        modeItems.sort(Comparator.comparingInt(this::sortBucket)
                .thenComparing(AutoBuyItem::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        return modeItems;
    }

    private int sortBucket(AutoBuyItem item) {
        ItemStack stack = item.getIconStack();
        Item type = stack.getItem();
        if (type == Items.TOTEM_OF_UNDYING) {
            return 0;
        }
        if (isWeaponItem(type)) {
            return 2;
        }
        if (isToolItem(type)) {
            return 1;
        }
        if (isArmorItem(type)) {
            return 3;
        }
        return 4;
    }

    private boolean isToolItem(Item item) {
        return item instanceof MiningToolItem && !(item instanceof SwordItem);
    }

    private boolean isWeaponItem(Item item) {
        return item instanceof SwordItem
                || item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof TridentItem
                || item instanceof MaceItem;
    }

    private boolean isArmorItem(Item item) {
        return item instanceof ArmorItem || item == Items.ELYTRA;
    }

    private String slugify(String value) {
        String normalized = AutoBuyItem.normalizeLine(value)
                .replace(' ', '_')
                .replaceAll("[^a-z0-9_\\u0430-\\u044f]", "");
        if (!normalized.isBlank()) {
            return normalized;
        }
        return "item_" + Integer.toUnsignedString(value.toLowerCase(Locale.ROOT).hashCode());
    }

    private String cleanAutoBuyName(String value) {
        if (value == null) {
            return "";
        }
        return AutoBuyItem.normalizeLine(value
                .replace("[★] ", "")
                .replace("[⚒] ", "")
                .replace("[❄] ", "")
                .replace("[🍹] ", "")
                .trim());
    }

    private boolean isAuctionScreen(GenericContainerScreen screen) {
        if (isFunTimeMode()) {
            return isFunTimeAuctionScreen(screen);
        }
        return matchesAuctionScreen(screen, getAuctionRefreshSlot(), getAuctionRefreshItem());
    }

    private boolean isFunTimeAuctionScreen(GenericContainerScreen screen) {
        if (screen == null || screen.getScreenHandler().slots.size() <= FUNTIME_REFRESH_SLOT) {
            return false;
        }

        String title = screen.getTitle().getString().toLowerCase(Locale.ROOT);
        if (title.contains("аукцион") || title.contains("поиск") || title.contains("auction") || title.contains("search")) {
            return true;
        }

        Slot refreshSlot = screen.getScreenHandler().slots.get(FUNTIME_REFRESH_SLOT);
        return refreshSlot != null && refreshSlot.hasStack();
    }

    private boolean isSpookyAuctionScreen(GenericContainerScreen screen) {
        return matchesAuctionScreen(screen, SPOOKYTIME_REFRESH_SLOT, Items.NETHER_STAR);
    }

    private boolean matchesAuctionScreen(GenericContainerScreen screen, int refreshSlotIndex, Item refreshItem) {
        if (screen == null || refreshSlotIndex < 0 || refreshItem == null) {
            return false;
        }

        List<Slot> slots = screen.getScreenHandler().slots;
        if (slots.size() <= refreshSlotIndex) {
            return false;
        }

        Slot refreshSlot = slots.get(refreshSlotIndex);
        return refreshSlot != null
                && refreshSlot.hasStack()
                && refreshSlot.getStack().getItem() == refreshItem;
    }

    private AutoBuyCandidate findBuyCandidate(GenericContainerScreen screen) {
        if (isHolyWorldMode() && isStaffLeaveEnabled() && hasOnlineTrackedStaff()) {
            return null;
        }
        List<AutoBuyItem> configuredItems = getConfiguredItems();
        if (configuredItems.isEmpty()) {
            return null;
        }

        Map<Item, List<AutoBuyItem>> configuredByItem = configuredItems.stream()
                .collect(Collectors.groupingBy(item -> item.getIconStack().getItem()));

        List<Slot> slots = screen.getScreenHandler().slots;
        int endIndex = Math.min(slots.size() - 1, 44);

        AutoBuyCandidate best = null;
        for (int i = 0; i <= endIndex; i++) {
            Slot slot = slots.get(i);
            if (slot == null || !slot.hasStack()) {
                continue;
            }
            if (slot.inventory instanceof net.minecraft.entity.player.PlayerInventory) {
                continue;
            }

            ItemStack stack = slot.getStack();
            List<AutoBuyItem> candidates = configuredByItem.get(stack.getItem());
            if (candidates == null || candidates.isEmpty()) {
                continue;
            }

            int unitPrice = getUnitPrice(stack);
            if (unitPrice <= 0) {
                int totalPrice = getTotalPrice(stack);
                if (totalPrice > 0) {
                    unitPrice = totalPrice / Math.max(1, stack.getCount());
                }
            }
            if (unitPrice <= 0) {
                continue;
            }

            String listingFingerprint = buildListingFingerprint(i, stack, unitPrice);
            if (blockedAuctionListings.contains(listingFingerprint)) {
                continue;
            }

            for (AutoBuyItem candidate : candidates) {
                if (!candidate.matches(stack, List.of()) || !candidate.hasPrice()) {
                    continue;
                }
                if (isPurchaseCooldownActive()) {
                    continue;
                }
                long maxPrice = candidate.getPriceValue();
                if (maxPrice <= 0 || unitPrice > maxPrice) {
                    continue;
                }

                if (best == null || unitPrice < best.price()) {
                    best = new AutoBuyCandidate(slot, candidate, unitPrice, listingFingerprint);
                }
            }
        }

        return best;
    }

    private void scheduleBuy(GenericContainerScreen screen, AutoBuyCandidate candidate) {
        if (candidate == null || candidate.slot() == null) {
            return;
        }

        Slot slot = candidate.slot();
        ItemStack previewStack = slot.getStack().copy();
        int syncId = screen.getScreenHandler().syncId;
        int preBuyDelayMs = (int) randomBetween(24L, 220L);
        int postBuyDelayMs = (int) randomBetween(45L, 240L);
        autoBuyScript.cleanup()
                .addStep(preBuyDelayMs, () -> {
                    buyClicks++;
                    blockedAuctionListings.add(candidate.listingFingerprint());
                    if (isHolyWorldMode()) {
                        resolvePendingHistoryEntry(false, ItemStack.EMPTY);
                        holyWorldPendingConfirmationItem = candidate.item();
                        holyWorldPendingUnitPrice = candidate.price();
                        holyWorldPendingConfirmationDeadlineMs = System.currentTimeMillis() + HOLYWORLD_CONFIRM_TIMEOUT_MS;
                        holyWorldPendingHistoryEntry = beginPurchaseAttempt(previewStack);
                    } else {
                        startPendingPurchaseConfirmation(candidate, previewStack);
                    }
                    boolean clicked = clickSlotWithDebug(syncId, slot.id, 0, SlotActionType.QUICK_MOVE, "auction_buy");
                    if (!clicked) {
                        blockedAuctionListings.remove(candidate.listingFingerprint());
                        if (isHolyWorldMode()) {
                            resolvePendingHistoryEntry(false, ItemStack.EMPTY);
                            clearHolyWorldPendingConfirmation();
                        } else {
                            clearPendingPurchaseConfirmation();
                        }
                        purchaseCooldownUntilMs = Math.max(
                                purchaseCooldownUntilMs,
                                System.currentTimeMillis() + randomBetween(280L, 920L)
                        );
                    }
                })
                .addStep(postBuyDelayMs, () -> {});
    }

    private void clickRefresh(GenericContainerScreen screen) {
        int refreshSlotIndex = getAuctionRefreshSlot();
        Item refreshItem = getAuctionRefreshItem();
        List<Slot> slots = screen.getScreenHandler().slots;
        if (refreshSlotIndex < 0 || slots.size() <= refreshSlotIndex) {
            return;
        }

        Slot refreshSlot = slots.get(refreshSlotIndex);
        if (refreshSlot == null || !refreshSlot.hasStack()) {
            return;
        }
        if (!isFunTimeMode() && refreshSlot.getStack().getItem() != refreshItem) {
            return;
        }

        long now = System.currentTimeMillis();
        // Shift-клик (QUICK_MOVE) — мгновенное обновление без лага. PICKUP оставляем только для SpookyTime.
        SlotActionType action = (isFunTimeMode() || isHolyWorldMode()) ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP;
        if (!clickSlotWithDebug(screen.getScreenHandler().syncId, refreshSlot.id, 0, action, "auction_refresh")) {
            return;
        }

        refreshCount++;
        if (lastRefreshMs > 0L) {
            long delta = now - lastRefreshMs;
            totalRefreshInterval += delta;
            refreshIntervals++;
            recordDebugRefreshDelta(delta);
        }
        lastRefreshMs = now;

        if (isHolyWorldMode()) {
            if (isAutoBuyWorkTest()) {
                nextRefreshDelayMs = pickHolyWorldTestRefreshDelayMs();
            } else {
                nextRefreshDelayMs = pickHolyWorldRefreshDelayMs();
                if (isSafeModeEnabled()) {
                    trackSafeRefreshPattern(now);
                } else {
                    trackNormalRefreshPattern(now);
                }
            }
        }

        pendingAuctionFingerprint = "";
        pendingAuctionFingerprintSinceMs = 0L;
    }

    private void startPendingPurchaseConfirmation(AutoBuyCandidate candidate, ItemStack previewStack) {
        clearPendingPurchaseConfirmation();
        pendingPurchaseConfirmationItem = candidate.item();
        pendingPurchaseUnitPrice = candidate.price();
        pendingPurchaseStartedMs = System.currentTimeMillis();
        pendingPurchaseConfirmationDeadlineMs = pendingPurchaseStartedMs + PURCHASE_CONFIRM_TIMEOUT_MS;
        pendingPurchaseHistoryEntry = createPurchaseHistoryEntry(previewStack);
    }

    private void expirePendingPurchaseConfirmation() {
        if (pendingPurchaseConfirmationDeadlineMs != 0L
                && System.currentTimeMillis() > pendingPurchaseConfirmationDeadlineMs) {
            clearPendingPurchaseConfirmation();
        }
    }

    private boolean hasPendingPurchaseConfirmation() {
        return pendingPurchaseConfirmationItem != null && pendingPurchaseConfirmationDeadlineMs != 0L;
    }

    private boolean isPurchaseCooldownActive() {
        if (purchaseCooldownUntilMs == 0L || System.currentTimeMillis() > purchaseCooldownUntilMs) {
            purchaseCooldownUntilMs = 0L;
            return false;
        }
        return true;
    }

    private void updateAuctionFingerprint(GenericContainerScreen screen) {
        String fingerprint = buildAuctionFingerprint(screen);
        if (fingerprint.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (!fingerprint.equals(pendingAuctionFingerprint)) {
            pendingAuctionFingerprint = fingerprint;
            pendingAuctionFingerprintSinceMs = now;
            return;
        }

        if (now - pendingAuctionFingerprintSinceMs < AUCTION_FINGERPRINT_STABLE_MS) {
            return;
        }

        if (lastAuctionFingerprint.isEmpty()) {
            lastAuctionFingerprint = fingerprint;
        } else if (!fingerprint.equals(lastAuctionFingerprint)) {
            lastAuctionFingerprint = fingerprint;
            blockedAuctionListings.clear();
        }

        pendingAuctionFingerprint = "";
        pendingAuctionFingerprintSinceMs = 0L;
    }

    private String buildAuctionFingerprint(GenericContainerScreen screen) {
        if (screen == null) {
            return "";
        }

        List<Slot> slots = screen.getScreenHandler().slots;
        int endIndex = Math.min(slots.size() - 1, 44);
        int refreshSlotIndex = getAuctionRefreshSlot();
        StringBuilder builder = new StringBuilder(Math.max(128, endIndex * 20));
        for (int i = 0; i <= endIndex; i++) {
            if (i == refreshSlotIndex) {
                continue;
            }

            Slot slot = slots.get(i);
            if (slot == null || slot.inventory instanceof net.minecraft.entity.player.PlayerInventory) {
                continue;
            }

            builder.append(i).append('=');
            if (!slot.hasStack()) {
                builder.append("empty;");
                continue;
            }

            ItemStack stack = slot.getStack();
            int unitPrice = getUnitPrice(stack);
            if (unitPrice <= 0) {
                int totalPrice = getTotalPrice(stack);
                if (totalPrice > 0) {
                    unitPrice = totalPrice / Math.max(1, stack.getCount());
                }
            }

            builder.append(Registries.ITEM.getId(stack.getItem()))
                    .append('|')
                    .append(AutoBuyItem.normalizeLine(stack.getName().getString()))
                    .append('|')
                    .append(stack.getCount())
                    .append('|')
                    .append(stack.getDamage())
                    .append('|')
                    .append(unitPrice)
                    .append(';');
        }
        return builder.toString();
    }

    private String buildListingFingerprint(int slotIndex, ItemStack stack, int unitPrice) {
        if (stack == null || stack.isEmpty()) {
            return slotIndex + "=empty";
        }

        return new StringBuilder(96)
                .append(slotIndex)
                .append('|')
                .append(Registries.ITEM.getId(stack.getItem()))
                .append('|')
                .append(AutoBuyItem.normalizeLine(stack.getName().getString()))
                .append('|')
                .append(stack.getCount())
                .append('|')
                .append(stack.getDamage())
                .append('|')
                .append(unitPrice)
                .toString();
    }

    private record AutoBuyCandidate(Slot slot, AutoBuyItem item, int price, String listingFingerprint) {
    }

    private record AutoSellCandidate(int inventoryIndex, long requiredPrice, String itemName, AutoBuyItem configuredItem) {
    }

    private record ButtonBounds(int x, int y, int w, int h) {
        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    private enum HolyWorldWalkState {
        IDLE,
        PREPARE_STORAGE,
        RANDOM_WALK,
        RETURN_TO_ORIGIN,
        STEP_BACK
    }

    private enum HolyWorldAutoSellState {
        IDLE,
        OPEN_STORAGE,
        LOOT_STORAGE,
        CLOSE_STORAGE,
        REQUEST_PRICE,
        WAIT_PRICE,
        CONFIRM_PRICE,
        WAIT_CONFIRM,
        DONE
    }

    private enum HolyWorldTimedSellState {
        IDLE,
        TRANSFER_TO_SELL_LIGHT,
        WAIT_AUCTION_ON_SELL_LIGHT,
        SELLING,
        TRANSFER_BACK
    }

    private enum HolyWorldPeriodicBreakState {
        IDLE,
        WALK,
        HUB_WAIT,
        REJOIN_WAIT
    }

    private static final class DebugAbRecentEvent {
        long atMs;
        String line;

        DebugAbRecentEvent(long atMs, String line) {
            this.atMs = atMs;
            this.line = line;
        }
    }

    private static final class PurchaseHistoryEntry {
        ItemStack stack;
        String title;
        int count;
        boolean purchased;
        long createdAtMs;
        long updatedAtMs;
        float animatedY;
        boolean removing;
        long removingStartedMs;

        PurchaseHistoryEntry(ItemStack stack, String title, int count, boolean purchased, long createdAtMs, long updatedAtMs, float animatedY, boolean removing, long removingStartedMs) {
            this.stack = stack;
            this.title = title;
            this.count = count;
            this.purchased = purchased;
            this.createdAtMs = createdAtMs;
            this.updatedAtMs = updatedAtMs;
            this.animatedY = animatedY;
            this.removing = removing;
            this.removingStartedMs = removingStartedMs;
        }
    }

    private PurchaseHistoryEntry createPurchaseHistoryEntry(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        long now = System.currentTimeMillis();
        return new PurchaseHistoryEntry(
                stack.copy(),
                stack.getName().getString(),
                Math.max(1, stack.getCount()),
                false,
                now,
                now,
                Float.NaN,
                false,
                0L
        );
    }

    private PurchaseHistoryEntry beginPurchaseAttempt(ItemStack stack) {
        PurchaseHistoryEntry entry = createPurchaseHistoryEntry(stack);
        pushPurchaseHistoryEntry(entry);
        return entry;
    }

    private void pushPurchaseHistoryEntry(PurchaseHistoryEntry entry) {
        if (entry == null) {
            return;
        }

        purchaseHistory.add(0, entry);
        while (purchaseHistory.size() > HISTORY_MAX_ENTRIES) {
            purchaseHistory.remove(purchaseHistory.size() - 1);
        }
        historyScroll = 0.0F;
    }

    private void resolvePendingHistoryEntry(boolean purchased, ItemStack updatedStack) {
        if (holyWorldPendingHistoryEntry == null) {
            return;
        }

        PurchaseHistoryEntry entry = holyWorldPendingHistoryEntry;
        if (updatedStack != null && !updatedStack.isEmpty()) {
            entry.stack = updatedStack.copy();
            entry.title = updatedStack.getName().getString();
            entry.count = Math.max(1, updatedStack.getCount());
        }
        entry.purchased = purchased;
        entry.updatedAtMs = System.currentTimeMillis();
        holyWorldPendingHistoryEntry = null;
    }

    private void startRemovingHistoryEntry(PurchaseHistoryEntry entry) {
        if (entry == null || entry.removing) {
            return;
        }
        entry.removing = true;
        entry.removingStartedMs = System.currentTimeMillis();
        entry.updatedAtMs = entry.removingStartedMs;
    }

    private void cleanupPurchaseHistory() {
        if (purchaseHistory.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        purchaseHistory.removeIf(entry -> entry.removing && now - entry.removingStartedMs >= HISTORY_REMOVE_ANIM_MS);
    }

    private int getPurchasedEntryCount() {
        int total = 0;
        for (PurchaseHistoryEntry entry : purchaseHistory) {
            if (entry.purchased && !entry.removing) {
                total++;
            }
        }
        return total;
    }

    private int getVisibleHistoryEntryCount() {
        int total = 0;
        for (PurchaseHistoryEntry entry : purchaseHistory) {
            if (!entry.removing) {
                total++;
            }
        }
        return total;
    }

    private float getMinHistoryScroll(int listHeight) {
        int visibleEntries = getVisibleHistoryEntryCount();
        float contentHeight = visibleEntries <= 0
                ? 0.0F
                : visibleEntries * (HISTORY_ENTRY_HEIGHT + HISTORY_ENTRY_GAP) - HISTORY_ENTRY_GAP;
        return Math.min(0.0F, listHeight - contentHeight);
    }

    private void setAutoBuyEnabled(boolean enabled) {
        if (autoBuyEnabled == enabled) {
            return;
        }

        long now = System.currentTimeMillis();
        long previousStartMs = autoBuyStartMs;
        autoBuyEnabled = enabled;
        autoBuyStartMs = enabled ? now : 0L;
        buyClicks = 0;
        refreshCount = 0;
        totalRefreshInterval = 0;
        refreshIntervals = 0;
        lastRefreshMs = 0;
        nextRefreshDelayMs = 0L;
        nextAuctionScanDelayMs = 0L;
        holyWorldSafeRefreshPauseUntilMs = 0L;
        holyWorldSafeRefreshBurstClicks = 0;
        holyWorldSafeRefreshBurstTarget = 0;
        holyWorldNormalRefreshPauseUntilMs = 0L;
        holyWorldNormalRefreshBurstClicks = 0;
        holyWorldNormalRefreshBurstTarget = 0;
        holyWorldTestSessionDeadlineMs = 0L;
        holyWorldTestFrenzyDeadlineMs = 0L;
        purchaseCooldownUntilMs = 0L;
        holyWorldNextRelogRefreshTarget = randomRelogRefreshTarget();
        hoveredHistoryEntry = null;
        hoveredHistoryEntryBounds = null;
        historyListBounds = null;
        refreshWatch.reset();
        scanWatch.reset();
        autoBuyScript.cleanup();
        setAutoSetupEnabled(false);
        clearVisibleChatTypingState();
        commandRateWindow.clear();
        commandLastSentMs = 0L;
        slotRateWindow.clear();
        slotLastClickMs = 0L;
        resetHolyWorldState();
        clearPendingPurchaseConfirmation();
        resetAuctionFingerprintState();
        autoBuyStartCoins = -1L;
        autoBuyStartCoinsMs = 0L;
        if (enabled) {
            captureStartCoins(now);
            ensureDebugAbSession(now);
            debugAbBanLogged = false;
            logDebugAb("AUTOBUY_ENABLED",
                    "mode=" + serverMode.getSelected()
                            + ", autosell=" + autoSellMode.getSelected()
                            + ", trigger=" + autoSellTriggerMode.getSelected()
                            + ", safe=" + isSafeModeEnabled());
        }
        if (enabled) {
            openAuctionOnEnable();
        } else {
            holyWorldTestSellUntilDone = false;
            logDebugAb("AUTOBUY_DISABLED",
                    "uptime=" + formatDuration(Math.max(0L, previousStartMs > 0L ? now - previousStartMs : 0L))
                            + ", refresh=" + refreshCount
                            + ", buys=" + buyClicks);
            closeDebugAbSession("autobuy_disabled", now);
            stopHolyWorldRotation();
        }
    }

    private void toggleAutoSellSetting() {
        if (!isHolyWorldMode()) {
            return;
        }

        boolean enable = !isAutoSellEnabled();
        autoSellMode.setSelected(enable ? "On" : "Off");
        if (!enable) {
            holyWorldAutoSellPauseWalk = false;
            resetHolyWorldAutoSellState();
            resetHolyWorldTimedSellState();
        }
    }

    private void setAutoSetupEnabled(boolean enabled) {
        if (autoSetupEnabled == enabled) {
            return;
        }
        if (!enabled && autoSetupChangedAnyPrice) {
            persistAutoSetupConfigs();
        }
        autoSetupEnabled = enabled;
        autoSetupIndex = 0;
        autoSetupItem = null;
        autoSetupStage = 0;
        autoSetupRefreshesLeft = 0;
        autoSetupNextActionMs = 0L;
        autoSetupChangedAnyPrice = false;
        autoSetupWatch.reset();
        if (enabled) {
            startScriptedLookRoutine(System.currentTimeMillis(), true);
        } else {
            autoSetupSliderDragging = false;
        }
    }

    private void tickTelegramCommands() {
        String token = telegramApiToken.getText();
        boolean globalMode = isTelegramGlobalMode();
        List<Long> whitelistIds = getTelegramWhitelistIds();

        telegramBotBridge.tick(token, 0L);

        String telegramError = telegramBotBridge.consumeLastError();
        if (telegramError != null && !telegramError.isBlank()) {
            showTelegramWarning("Telegram error: " + telegramError);
        }

        if (token == null || token.isBlank()) {
            return;
        }

        if (!globalMode && whitelistIds.isEmpty()) {
            showTelegramWarning("Whitelist Р В РЎвЂ”Р РЋРЎвЂњР РЋР С“Р РЋРІР‚С™. Р В Р’ВР РЋР С“Р В РЎвЂ”Р В РЎвЂўР В Р’В»Р РЋР Р‰Р В Р’В·Р РЋРЎвЂњР В РІвЂћвЂ“ .telegram chat id1,id2 Р В РЎвЂР В Р’В»Р В РЎвЂ .telegram chat global");
        }

        for (TelegramCommand command : telegramBotBridge.drainCommands()) {
            if (!globalMode && !whitelistIds.contains(command.chatId())) {
                showTelegramWarning("Р В РЎв„ўР В РЎвЂўР В РЎВР В Р’В°Р В Р вЂ¦Р В РўвЂР В Р’В° Р В РЎвЂ”Р РЋР вЂљР В РЎвЂР РЋРІвЂљВ¬Р В Р’В»Р В Р’В° Р В РЎвЂР В Р’В· chat id " + command.chatId() + ", Р В Р вЂ¦Р В РЎвЂў Р В РЎвЂўР В Р вЂ¦ Р В Р вЂ¦Р В Р’Вµ Р В Р вЂ  whitelist");
                continue;
            }
            lastTelegramCommandChatId = command.chatId();
            handleTelegramCommand(token.trim(), command.chatId(), command.text());
        }
    }

    private boolean isTelegramGlobalMode() {
        return "Global".equalsIgnoreCase(telegramChatMode.getSelected());
    }

    private List<Long> getTelegramWhitelistIds() {
        return parseTelegramChatIds(telegramGroupId.getText());
    }

    private List<Long> parseTelegramChatIds(String rawIds) {
        if (rawIds == null || rawIds.isBlank()) {
            return List.of();
        }

        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        String[] tokens = rawIds.split("[,;\\s]+");
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            try {
                long value = Long.parseLong(token.trim());
                if (value != 0L) {
                    ids.add(value);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return List.copyOf(ids);
    }

    private void showTelegramWarning(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (message.equals(lastTelegramWarning) && now - lastTelegramWarningMs < 5000L) {
            return;
        }
        lastTelegramWarning = message;
        lastTelegramWarningMs = now;
        if (mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(Text.literal("[AutoBuy/Telegram] " + message));
        }
    }

    private void handleTelegramCommand(String token, long chatId, String messageText) {
        String command = normalizeTelegramCommand(messageText);
        if (command.isBlank() || (!command.startsWith("!") && !command.startsWith("/"))) {
            return;
        }

        switch (command) {
            case "!Р РЋРІР‚В¦Р В Р’ВµР В Р’В»Р В РЎвЂ”", "!help", "/help", "/Р РЋРІР‚В¦Р В Р’ВµР В Р’В»Р В РЎвЂ”" -> sendTelegramHelp(token, chatId);
            case "!Р РЋР С“Р РЋРІР‚С™Р В Р’В°Р РЋРІР‚С™Р РЋР С“", "!stats", "/stats", "/Р РЋР С“Р РЋРІР‚С™Р В Р’В°Р РЋРІР‚С™Р РЋР С“" -> sendTelegramStats(token, chatId);
            case "!Р РЋР С“Р В РЎвЂќР РЋР вЂљР В РЎвЂР В Р вЂ¦", "!screen", "/screen", "/Р РЋР С“Р В РЎвЂќР РЋР вЂљР В РЎвЂР В Р вЂ¦" -> sendTelegramScreenshot(token, chatId);
            default -> {
            }
        }
    }

    private String normalizeTelegramCommand(String rawText) {
        String normalized = AutoBuyItem.normalizeLine(rawText);
        if (normalized.isBlank()) {
            return "";
        }

        int spaceIndex = normalized.indexOf(' ');
        String command = spaceIndex > 0 ? normalized.substring(0, spaceIndex) : normalized;
        int mentionIndex = command.indexOf('@');
        if (mentionIndex > 0) {
            command = command.substring(0, mentionIndex);
        }
        return command;
    }

    private void sendTelegramHelp(String token, long chatId) {
        String helpMessage = """
                РЎР‚РЎСџР’В¤РІР‚вЂњ AutoBuy Telegram
                РЎР‚РЎСџРІР‚СљРЎв„ў Р В РЎв„ўР В РЎвЂўР В РЎВР В Р’В°Р В Р вЂ¦Р В РўвЂР РЋРІР‚в„–:
                Р Р†Р вЂљРЎС› !Р РЋРІР‚В¦Р В Р’ВµР В Р’В»Р В РЎвЂ” | !help | /help Р Р†Р вЂљРІР‚Сњ Р РЋР С“Р В РЎвЂ”Р В РЎвЂР РЋР С“Р В РЎвЂўР В РЎвЂќ Р В РЎвЂќР В РЎвЂўР В РЎВР В Р’В°Р В Р вЂ¦Р В РўвЂ
                Р Р†Р вЂљРЎС› !Р РЋР С“Р В РЎвЂќР РЋР вЂљР В РЎвЂР В Р вЂ¦ | !screen | /screen Р Р†Р вЂљРІР‚Сњ Р РЋР С“Р В РЎвЂќР РЋР вЂљР В РЎвЂР В Р вЂ¦Р РЋРІвЂљВ¬Р В РЎвЂўР РЋРІР‚С™ Minecraft
                Р Р†Р вЂљРЎС› !Р РЋР С“Р РЋРІР‚С™Р В Р’В°Р РЋРІР‚С™Р РЋР С“ | !stats | /stats Р Р†Р вЂљРІР‚Сњ Р РЋР С“Р РЋРІР‚С™Р В Р’В°Р РЋРІР‚С™Р В РЎвЂР РЋР С“Р РЋРІР‚С™Р В РЎвЂР В РЎвЂќР В Р’В° Р В РЎвЂ Р В РЎВР В РЎвЂўР В Р вЂ¦Р В Р’ВµР РЋРІР‚С™Р РЋРІР‚в„–/Р РЋРІР‚РЋР В Р’В°Р РЋР С“
                """;
        telegramBotBridge.sendMessageAsync(token, chatId, helpMessage.trim());
    }

    private void sendTelegramStats(String token, long chatId) {
        telegramBotBridge.sendMessageAsync(token, chatId, buildTelegramStatsMessage());
    }

    private void sendTelegramScreenshot(String token, long chatId) {
        Path screenshotPath;
        try (NativeImage screenshot = ScreenshotRecorder.takeScreenshot(mc.getFramebuffer())) {
            screenshotPath = Files.createTempFile("zov_autobuy_", ".png");
            screenshot.writeTo(screenshotPath);
        } catch (IOException exception) {
            telegramBotBridge.sendMessageAsync(token, chatId, "Р В РЎСљР В Р’Вµ Р В РЎвЂ”Р В РЎвЂўР В Р’В»Р РЋРЎвЂњР РЋРІР‚РЋР В РЎвЂР В Р’В»Р В РЎвЂўР РЋР С“Р РЋР Р‰ Р РЋР С“Р В РўвЂР В Р’ВµР В Р’В»Р В Р’В°Р РЋРІР‚С™Р РЋР Р‰ Р РЋР С“Р В РЎвЂќР РЋР вЂљР В РЎвЂР В Р вЂ¦Р РЋРІвЂљВ¬Р В РЎвЂўР РЋРІР‚С™: " + exception.getMessage());
            return;
        } catch (RuntimeException exception) {
            telegramBotBridge.sendMessageAsync(token, chatId, "Р В РЎСљР В Р’Вµ Р В РЎвЂ”Р В РЎвЂўР В Р’В»Р РЋРЎвЂњР РЋРІР‚РЋР В РЎвЂР В Р’В»Р В РЎвЂўР РЋР С“Р РЋР Р‰ Р РЋР С“Р В РўвЂР В Р’ВµР В Р’В»Р В Р’В°Р РЋРІР‚С™Р РЋР Р‰ Р РЋР С“Р В РЎвЂќР РЋР вЂљР В РЎвЂР В Р вЂ¦Р РЋРІвЂљВ¬Р В РЎвЂўР РЋРІР‚С™.");
            return;
        }

        telegramBotBridge.sendPhotoAsync(token, chatId, screenshotPath, "AutoBuy Р РЋР С“Р В РЎвЂќР РЋР вЂљР В РЎвЂР В Р вЂ¦Р РЋРІвЂљВ¬Р В РЎвЂўР РЋРІР‚С™", true);
    }

    private void handleDebugAbMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return;
        }

        boolean debugEnabled = isDebugAbEnabled();
        if (!autoBuyEnabled && !debugEnabled) {
            return;
        }
        String clean = rawMessage.replaceAll("\u00A7.", "").replace('\n', ' ').replace('\r', ' ').trim();
        if (clean.isBlank()) {
            return;
        }
        String normalized = AutoBuyItem.normalizeLine(clean).toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || !DEBUG_AB_BAN_FILTER.matcher(normalized).find()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (debugEnabled) {
            ensureDebugAbSession(now);
        }
        boolean banMarker = normalized.contains("\u0437\u0430\u0431\u0430\u043d")
                || normalized.contains("banned")
                || normalized.contains("ban")
                || normalized.contains("\u0430\u043d\u0442\u0438\u0447\u0438\u0442")
                || normalized.contains("netvision")
                || normalized.contains("net vision")
                || normalized.contains("\u043a\u0438\u043a");
        if (banMarker) {
            if (debugEnabled) {
                debugAbBanLogged = true;
                logDebugAb("BAN_MARKER",
                        "message=\"" + clean + "\""
                                + ", refresh=" + refreshCount
                                + ", buys=" + buyClicks
                                + ", autosell=" + holyWorldAutoSellState
                                + ", timed_sell=" + holyWorldTimedSellState
                                + ", walk=" + holyWorldWalkState
                                + ", cmd_sent=" + debugAbSentCommands
                                + ", cmd_queued=" + debugAbQueuedCommands
                                + ", cmd_burst_count=" + debugAbCommandBurstCount
                                + ", queue_pending=" + pendingVisibleChatCommands.size()
                                + ", slot_click_ok=" + debugAbSlotClickSuccess
                                + ", slot_click_fail=" + debugAbSlotClickFailures
                                + ", autosell_swap_fail=" + debugAbAutoSellMoveFailures
                                + ", move_mask=" + formatDebugMovementMask(debugAbMovementMask));
                logDebugAbRecentWindow(now, "ban_marker");
            }
            if (autoBuyEnabled) {
                triggerAntiCheatEmergencyStop("ban_marker");
            }
            return;
        }

        if (debugEnabled) {
            logDebugAb("FILTER_HIT", "message=\"" + clean + "\"");
        }
    }

    private void triggerAntiCheatEmergencyStop(String reason) {
        long now = System.currentTimeMillis();
        int queuedCommands = pendingVisibleChatCommands.size();
        int pendingRct = holyWorldPendingRctAnarchy;
        boolean autoSellWasEnabled = isAutoSellEnabled();

        clearVisibleChatTypingState();
        clearHolyWorldWalkState();
        clearHolyWorldPendingRctSequence();
        holyWorldAutoSellPauseWalk = false;
        resetHolyWorldAutoSellState();
        resetHolyWorldTimedSellState();
        resetHolyWorldPeriodicBreakState();
        commandRateWindow.clear();
        commandLastSentMs = 0L;
        slotRateWindow.clear();
        slotLastClickMs = 0L;

        if (autoSellWasEnabled) {
            autoSellMode.setSelected("Off");
        }

        logDebugAb("EMERGENCY_STOP",
                "reason=" + (reason == null || reason.isBlank() ? "unknown" : reason)
                        + ", autoBuy=" + autoBuyEnabled
                        + ", autosell_was=" + autoSellWasEnabled
                        + ", queue_cleared=" + queuedCommands
                        + ", pending_rct=" + pendingRct);

        if (autoBuyEnabled) {
            setAutoBuyEnabled(false);
        } else {
            closeDebugAbSession("anti_cheat_emergency", now);
        }
    }

    private void handleSaleMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return;
        }

        String normalized = AutoBuyItem.normalizeLine(rawMessage);
        if (!normalized.contains("Р В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂР В Р’В» Р РЋРЎвЂњ Р В Р вЂ Р В Р’В°Р РЋР С“")) {
            return;
        }

        Matcher matcher = SALE_MESSAGE_PATTERN.matcher(rawMessage);
        if (!matcher.matches()) {
            sendSaleToTelegram("Р В Р вЂ¦Р В Р’ВµР В РЎвЂР В Р’В·Р В Р вЂ Р В Р’ВµР РЋР С“Р РЋРІР‚С™Р В Р вЂ¦Р В РЎвЂў", "Р В Р вЂ¦Р В Р’ВµР В РЎвЂР В Р’В·Р В Р вЂ Р В Р’ВµР РЋР С“Р РЋРІР‚С™Р В Р вЂ¦Р В РЎвЂў", 0, -1L, rawMessage);
            return;
        }

        String buyer = matcher.group(1).trim();
        String item = matcher.group(2).trim();
        int amount = parseIntSafe(AutoBuyItem.normalizeDigits(matcher.group(3)));
        long totalPrice = parseLongSafe(AutoBuyItem.normalizeDigits(matcher.group(4)));
        sendSaleToTelegram(buyer, item, amount, totalPrice, rawMessage);
    }

    private void handlePurchaseMessage(String rawMessage) {
        if (!hasPendingPurchaseConfirmation() || rawMessage == null || rawMessage.isBlank()) {
            return;
        }

        String normalized = AutoBuyItem.normalizeLine(rawMessage);
        boolean success = normalized.contains("\u0432\u044b \u043a\u0443\u043f\u0438\u043b\u0438");
        boolean failed = normalized.contains(PURCHASE_FAILED_PREFIX) && normalized.contains("\u0443\u0436\u0435 \u043a\u0443\u043f\u0438\u043b\u0438");
        if (!success && !failed) {
            return;
        }

        long now = System.currentTimeMillis();
        if (success && (pendingPurchaseStartedMs == 0L || now - pendingPurchaseStartedMs > PURCHASE_SUCCESS_MESSAGE_WINDOW_MS)) {
            clearPendingPurchaseConfirmation();
            return;
        }

        ItemStack confirmedStack = pendingPurchaseHistoryEntry != null
                ? pendingPurchaseHistoryEntry.stack.copy()
                : ItemStack.EMPTY;
        if (success) {
            Matcher matcher = PURCHASE_MESSAGE_PATTERN.matcher(normalized);
            if (!matcher.matches()) {
                return;
            }
            if (!matchesPendingPurchaseMessage(matcher, confirmedStack)) {
                return;
            }

            int amount = parseIntSafe(AutoBuyItem.normalizeDigits(matcher.group(2)));
            if (amount > 0 && confirmedStack != null && !confirmedStack.isEmpty()) {
                confirmedStack.setCount(amount);
            }

            if (confirmedStack == null || confirmedStack.isEmpty()) {
                confirmedStack = pendingPurchaseConfirmationItem != null
                        ? pendingPurchaseConfirmationItem.getIconStack()
                        : ItemStack.EMPTY;
            }

            sendPurchasedItemToTelegram(confirmedStack, pendingPurchaseUnitPrice);
            resolvePendingPurchaseConfirmation(true, confirmedStack);
            return;
        }

        resolvePendingPurchaseConfirmation(false, confirmedStack);
    }

    private void handleHolyWorldAutoSellMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return;
        }
        if (!isHolyWorldMode()) {
            return;
        }
        if (holyWorldAutoSellState == HolyWorldAutoSellState.IDLE || holyWorldAutoSellState == HolyWorldAutoSellState.DONE) {
            return;
        }

        String cleanRaw = rawMessage.replaceAll("\u00A7.", "");
        String normalized = AutoBuyItem.normalizeLine(cleanRaw).toLowerCase(Locale.ROOT);

        boolean noMainHandItem = normalized.contains("\u0432\u043e\u0437\u044c\u043c\u0438\u0442\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0433\u043b\u0430\u0432\u043d\u0443\u044e \u0440\u0443\u043a\u0443");
        boolean auctionSlotsFull = normalized.contains("\u0432\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0431\u043e\u043b\u044c\u0448\u0435 \u0432\u044b\u0441\u0442\u0430\u0432\u043b\u044f\u0442\u044c \u0442\u043e\u0432\u0430\u0440\u044b \u043d\u0430 \u0430\u0443\u043a\u0446\u0438\u043e\u043d")
                || normalized.contains("\u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0431\u043e\u043b\u044c\u0448\u0435 \u0432\u044b\u0441\u0442\u0430\u0432\u043b\u044f\u0442\u044c \u0442\u043e\u0432\u0430\u0440\u044b");
        boolean noFilterFound = normalized.contains("\u043f\u043e \u0432\u0430\u0448\u0435\u043c\u0443 \u0437\u0430\u043f\u0440\u043e\u0441\u0443 \u043d\u0435 \u0431\u044b\u043b\u043e \u043d\u0430\u0439\u0434\u0435\u043d\u043e \u043d\u0438 \u043e\u0434\u043d\u043e\u0433\u043e \u0444\u0438\u043b\u044c\u0442\u0440\u0430");

        if (noMainHandItem) {
            logDebugAb("AUTOSELL_MSG", "no_main_hand_item");
            holyWorldAutoSellNeedNextItem = true;
            holyWorldAutoSellOfferReceived = true;
            holyWorldAutoSellNoItems = false;
            if (holyWorldAutoSellState == HolyWorldAutoSellState.WAIT_CONFIRM || holyWorldAutoSellState == HolyWorldAutoSellState.CONFIRM_PRICE) {
                holyWorldAutoSellConfirmReceived = true;
            }
            return;
        }

        if (auctionSlotsFull) {
            logDebugAb("AUTOSELL_MSG", "auction_slots_full");
            resetAutoSellNoFilterStreak();
            holyWorldAutoSellAuctionSlotsFull = true;
            holyWorldAutoSellOfferReceived = true;
            holyWorldAutoSellNoItems = true;
            holyWorldAutoSellConfirmReceived = true;
            return;
        }

        if (noFilterFound) {
            logDebugAb("AUTOSELL_MSG", "no_filter_found");
            registerAutoSellNoFilter(System.currentTimeMillis());
            markCurrentAutoSellItemSkippedForCycle();
            holyWorldAutoSellNeedNextItem = true;
            holyWorldAutoSellOfferReceived = true;
            holyWorldAutoSellNoItems = false;
            if (holyWorldAutoSellState == HolyWorldAutoSellState.WAIT_CONFIRM || holyWorldAutoSellState == HolyWorldAutoSellState.CONFIRM_PRICE) {
                holyWorldAutoSellConfirmReceived = true;
            }
            return;
        }

        boolean confirmPrompt = normalized.contains("/ah sell auto confirm");
        if (confirmPrompt) {
            logDebugAb("AUTOSELL_MSG", "confirm_prompt");
            resetAutoSellNoFilterStreak();
            holyWorldAutoSellOfferReceived = true;
            if (holyWorldAutoSellRequiredPrice < 0L) {
                holyWorldAutoSellRequiredPrice = 0L;
            }
            long promptPrice = extractLastPriceFromText(cleanRaw);
            if (promptPrice > 0L) {
                holyWorldAutoSellOfferedPrice = promptPrice;
            } else if (holyWorldAutoSellOfferedPrice <= 0L) {
                holyWorldAutoSellOfferedPrice = Math.max(1L, holyWorldAutoSellRequiredPrice);
            }
        }

        if (holyWorldAutoSellState == HolyWorldAutoSellState.WAIT_CONFIRM || holyWorldAutoSellState == HolyWorldAutoSellState.CONFIRM_PRICE) {
            boolean listed = normalized.contains("\u0432\u044b\u0441\u0442\u0430\u0432\u0438\u043b\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442")
                    || normalized.contains("\u043b\u043e\u0442 \u0432\u044b\u0441\u0442\u0430\u0432\u043b\u0435\u043d")
                    || normalized.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0432\u044b\u0441\u0442\u0430\u0432\u0438\u043b\u0438")
                    || (normalized.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u043e") && normalized.contains("\u043f\u0440\u043e\u0434\u0430\u0436"))
                    || (normalized.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u043e") && normalized.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d"));
            if (listed) {
                logDebugAb("AUTOSELL_MSG", "listed_success");
                resetAutoSellNoFilterStreak();
                holyWorldAutoSellConfirmReceived = true;
                return;
            }
        }

        if (holyWorldAutoSellState != HolyWorldAutoSellState.WAIT_PRICE && holyWorldAutoSellState != HolyWorldAutoSellState.REQUEST_PRICE) {
            return;
        }

        if (normalized.contains("Р В Р вЂ Р В РЎвЂўР В Р’В·Р РЋР Р‰Р В РЎВР В РЎвЂР РЋРІР‚С™Р В Р’Вµ Р В РЎвЂ”Р РЋР вЂљР В Р’ВµР В РўвЂР В РЎВР В Р’ВµР РЋРІР‚С™ Р В Р вЂ  Р В РЎвЂ“Р В Р’В»Р В Р’В°Р В Р вЂ Р В Р вЂ¦Р РЋРЎвЂњР РЋР вЂ№ Р РЋР вЂљР РЋРЎвЂњР В РЎвЂќР РЋРЎвЂњ")) {
            holyWorldAutoSellNeedNextItem = true;
            holyWorldAutoSellOfferReceived = true;
            holyWorldAutoSellNoItems = false;
            return;
        }

        if (normalized.contains("Р В Р вЂ¦Р В Р’Вµ Р В РЎВР В РЎвЂўР В Р’В¶Р В Р’ВµР РЋРІР‚С™Р В Р’Вµ Р В Р’В±Р В РЎвЂўР В Р’В»Р РЋР Р‰Р РЋРІвЂљВ¬Р В Р’Вµ Р В Р вЂ Р РЋРІР‚в„–Р РЋР С“Р РЋРІР‚С™Р В Р’В°Р В Р вЂ Р В Р’В»Р РЋР РЏР РЋРІР‚С™Р РЋР Р‰ Р РЋРІР‚С™Р В РЎвЂўР В Р вЂ Р В Р’В°Р РЋР вЂљР РЋРІР‚в„–")) {
            holyWorldAutoSellAuctionSlotsFull = true;
            holyWorldAutoSellOfferReceived = true;
            holyWorldAutoSellNoItems = true;
            return;
        }

        if (normalized.contains("Р В Р вЂ¦Р В Р’ВµР РЋРІР‚С™ Р В РЎвЂ”Р РЋР вЂљР В Р’ВµР В РўвЂР В РЎВР В Р’ВµР РЋРІР‚С™")
                || normalized.contains("\u043d\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442")
                || normalized.contains("Р В Р вЂ¦Р В Р’ВµР РЋРІР‚РЋР В Р’ВµР В РЎвЂ“Р В РЎвЂў Р В РЎвЂ”Р РЋР вЂљР В РЎвЂўР В РўвЂР В Р’В°Р В Р вЂ ")
                || normalized.contains("\u043d\u0435\u0447\u0435\u0433\u043e \u043f\u0440\u043e\u0434\u0430\u0432")
                || normalized.contains("Р В РЎвЂ”Р РЋРЎвЂњР РЋР С“Р РЋРІР‚С™Р В РЎвЂў")
                || normalized.contains("\u043f\u0443\u0441\u0442\u043e")
                || normalized.contains("Р В Р вЂ¦Р В Р’ВµР РЋРІР‚С™ Р В РЎвЂ”Р В РЎвЂўР В РўвЂР РЋРІР‚В¦Р В РЎвЂўР В РўвЂР РЋР РЏР РЋРІР‚В°Р В РЎвЂР РЋРІР‚В¦")) {
            holyWorldAutoSellNoItems = true;
            holyWorldAutoSellOfferReceived = true;
            return;
        }

        Matcher matcher = HOLYWORLD_AUTOSELL_OFFER_PATTERN.matcher(cleanRaw);
        String itemName = "";
        int amount = 1;
        long offeredPrice = -1L;
        if (matcher.find()) {
            itemName = matcher.group(1).trim();
            amount = Math.max(1, parseIntSafe(AutoBuyItem.normalizeDigits(matcher.group(2))));
            offeredPrice = parseLongSafe(AutoBuyItem.normalizeDigits(matcher.group(3)));
        }

        if (offeredPrice <= 0L) {
            offeredPrice = extractLastPriceFromText(cleanRaw);
        }
        if (itemName.isBlank()) {
            itemName = extractBracketItem(cleanRaw);
        }
        if (offeredPrice <= 0L || itemName.isBlank()) {
            return;
        }

        holyWorldAutoSellOfferedItem = itemName;
        holyWorldAutoSellOfferedCount = amount;
        holyWorldAutoSellOfferedPrice = offeredPrice;
        resetAutoSellNoFilterStreak();
        long resolvedRequiredPrice = resolveAutoSellRequiredPrice(itemName);
        if (resolvedRequiredPrice > 0L) {
            holyWorldAutoSellRequiredPrice = resolvedRequiredPrice;
        } else if (holyWorldAutoSellRequiredPrice < 0L) {
            holyWorldAutoSellRequiredPrice = 0L;
        }
        holyWorldAutoSellOfferReceived = true;
        logDebugAb("AUTOSELL_OFFER",
                "item=" + itemName
                        + ", count=" + amount
                        + ", offered=" + offeredPrice
                        + ", required=" + holyWorldAutoSellRequiredPrice);
    }

    private void handleHolyWorldRelogMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank() || !isHolyWorldMode()) {
            return;
        }
        if (holyWorldPendingRctAnarchy <= 0) {
            return;
        }

        String normalized = AutoBuyItem.normalizeLine(rawMessage).toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return;
        }

        boolean commandMissing = normalized.contains("\u043a\u043e\u043c\u0430\u043d\u0434\u044b \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442")
                || normalized.contains("\u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442")
                || normalized.contains("\u043d\u0435\u0432\u0435\u0440\u043d\u0430\u044f \u043a\u043e\u043c\u0430\u043d\u0434\u0430")
                || normalized.contains("unknown command")
                || normalized.contains("command not found");
        if (commandMissing) {
            holyWorldPendingRctNeedCompassRecovery = true;
            holyWorldNextRctCompassRecoveryMs = Math.min(
                    holyWorldNextRctCompassRecoveryMs <= 0L ? Long.MAX_VALUE : holyWorldNextRctCompassRecoveryMs,
                    System.currentTimeMillis() + 500L
            );
        }

        boolean retryNow = normalized.contains("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0432\u0430\u0441 \u043a \u0445\u0430\u0431\u0443")
                || normalized.contains("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c")
                || commandMissing;
        if (!retryNow) {
            return;
        }

        holyWorldNextRctRetryMs = Math.min(
                holyWorldNextRctRetryMs,
                System.currentTimeMillis() + HOLYWORLD_RCT_RETRY_MIN_DELAY_MS
        );
    }

    private boolean matchesPendingPurchaseMessage(Matcher matcher, ItemStack pendingStack) {
        if (matcher == null) {
            return false;
        }

        String messageItemName = AutoBuyItem.normalizeLine(matcher.group(1));
        int amount = parseIntSafe(AutoBuyItem.normalizeDigits(matcher.group(2)));
        long totalPrice = parseLongSafe(AutoBuyItem.normalizeDigits(matcher.group(3)));
        String expectedItemName = pendingStack != null && !pendingStack.isEmpty()
                ? AutoBuyItem.normalizeLine(pendingStack.getName().getString())
                : (pendingPurchaseConfirmationItem != null
                ? AutoBuyItem.normalizeLine(pendingPurchaseConfirmationItem.getDisplayName())
                : "");

        if (!expectedItemName.isBlank()
                && !messageItemName.equals(expectedItemName)
                && !messageItemName.contains(expectedItemName)
                && !expectedItemName.contains(messageItemName)) {
            return false;
        }

        if (pendingStack != null && !pendingStack.isEmpty() && amount > 0 && pendingStack.getCount() > 0 && amount != pendingStack.getCount()) {
            return false;
        }

        if (pendingPurchaseUnitPrice > 0 && amount > 0 && totalPrice > 0) {
            long expectedTotal = (long) pendingPurchaseUnitPrice * amount;
            if (expectedTotal != totalPrice) {
                return false;
            }
        }

        return true;
    }

    private void resolvePendingPurchaseConfirmation(boolean purchased, ItemStack updatedStack) {
        PurchaseHistoryEntry entry = pendingPurchaseHistoryEntry;
        if (entry != null) {
            if (updatedStack != null && !updatedStack.isEmpty()) {
                entry.stack = updatedStack.copy();
                entry.title = updatedStack.getName().getString();
                entry.count = Math.max(1, updatedStack.getCount());
            }
            entry.purchased = purchased;
            entry.updatedAtMs = System.currentTimeMillis();
            pushPurchaseHistoryEntry(entry);
        }

        purchaseCooldownUntilMs = Math.max(purchaseCooldownUntilMs, System.currentTimeMillis() + PURCHASE_REBUY_GUARD_MS);
        clearPendingPurchaseConfirmation();
    }

    private void clearPendingPurchaseConfirmation() {
        pendingPurchaseConfirmationItem = null;
        pendingPurchaseUnitPrice = -1;
        pendingPurchaseConfirmationDeadlineMs = 0L;
        pendingPurchaseHistoryEntry = null;
        pendingPurchaseStartedMs = 0L;
    }

    private void resetAuctionFingerprintState() {
        lastAuctionFingerprint = "";
        pendingAuctionFingerprint = "";
        pendingAuctionFingerprintSinceMs = 0L;
        blockedAuctionListings.clear();
    }

    private void sendSaleToTelegram(String buyer, String itemName, int amount, long totalPrice, String rawMessage) {
        StringBuilder message = new StringBuilder();
        message.append("РЎР‚РЎСџРІР‚в„ўРЎвЂ Р В РЎСџР РЋР вЂљР В РЎвЂўР В РўвЂР В Р’В°Р В Р’В¶Р В Р’В°").append('\n');
        message.append("РЎР‚РЎСџРІР‚ВР’В¤ Р В РЎСџР В РЎвЂўР В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В Р’В°Р РЋРІР‚С™Р В Р’ВµР В Р’В»Р РЋР Р‰: ").append(buyer).append('\n');
        message.append("РЎР‚РЎСџР’В§Р’В© Р В РЎСџР РЋР вЂљР В Р’ВµР В РўвЂР В РЎВР В Р’ВµР РЋРІР‚С™: ").append(itemName).append('\n');
        if (amount > 0) {
            message.append("РЎР‚РЎСџРІР‚СљР’В¦ Р В РЎв„ўР В РЎвЂўР В Р’В»-Р В Р вЂ Р В РЎвЂў: ").append(amount).append('\n');
        }
        if (totalPrice >= 0L) {
            message.append("РЎР‚РЎСџРІР‚в„ўР’В° Р В Р Р‹Р РЋРЎвЂњР В РЎВР В РЎВР В Р’В°: ").append(formatNumber(totalPrice)).append(" Р Р†РІР‚С”РЎвЂњ").append('\n');
            if (amount > 0) {
                long unitPrice = Math.max(1L, totalPrice / amount);
                message.append("РЎР‚РЎСџР РЏР’В· Р В Р’В¦Р В Р’ВµР В Р вЂ¦Р В Р’В° Р В Р’В·Р В Р’В° 1: ").append(formatNumber(unitPrice)).append(" Р Р†РІР‚С”РЎвЂњ").append('\n');
            }
        }
        message.append("РЎР‚РЎСџР Р‰РЎвЂ™ Р В Р’В Р В Р’ВµР В Р’В¶Р В РЎвЂР В РЎВ: ").append(serverMode.getSelected());
        if (totalPrice < 0L || amount <= 0) {
            message.append('\n').append("РЎР‚РЎСџРІР‚СљРЎСљ Р В Р Р‹Р В РЎвЂўР В РЎвЂўР В Р’В±Р РЋРІР‚В°Р В Р’ВµР В Р вЂ¦Р В РЎвЂР В Р’Вµ: ").append(rawMessage);
        }
        sendTelegramToTargets(message.toString());
    }

    private void sendPurchasedItemToTelegram(ItemStack stack, int unitPrice) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        String itemName = stack.getName().getString();
        int count = Math.max(1, stack.getCount());
        StringBuilder message = new StringBuilder();
        message.append("Р Р†РЎС™РІР‚В¦ Р В Р в‚¬Р РЋР С“Р В РЎвЂ”Р В Р’ВµР РЋРІвЂљВ¬Р В Р вЂ¦Р В Р’В°Р РЋР РЏ Р В РЎвЂ”Р В РЎвЂўР В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂќР В Р’В°").append('\n');
        message.append("РЎР‚РЎСџР’В§Р’В© Р В РЎСџР РЋР вЂљР В Р’ВµР В РўвЂР В РЎВР В Р’ВµР РЋРІР‚С™: ").append(itemName).append('\n');
        message.append("РЎР‚РЎСџРІР‚СљР’В¦ Р В РЎв„ўР В РЎвЂўР В Р’В»-Р В Р вЂ Р В РЎвЂў: ").append(count).append('\n');
        if (unitPrice > 0) {
            message.append("РЎР‚РЎСџР РЏР’В· Р В Р’В¦Р В Р’ВµР В Р вЂ¦Р В Р’В° Р В Р’В·Р В Р’В° 1: ").append(formatNumber(unitPrice)).append(" Р Р†РІР‚С”РЎвЂњ").append('\n');
            message.append("РЎР‚РЎСџРІР‚в„ўР’В° Р В Р Р‹Р РЋРЎвЂњР В РЎВР В РЎВР В Р’В°: ").append(formatNumber((long) unitPrice * count)).append(" Р Р†РІР‚С”РЎвЂњ").append('\n');
        }
        message.append("РЎР‚РЎСџР Р‰РЎвЂ™ Р В Р’В Р В Р’ВµР В Р’В¶Р В РЎвЂР В РЎВ: ").append(serverMode.getSelected());
        sendTelegramToTargets(message.toString());
    }

    private void sendTelegramToTargets(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        String token = telegramApiToken.getText();
        if (token == null || token.isBlank()) {
            return;
        }

        List<Long> targets = resolveTelegramTargets();
        for (Long chatId : targets) {
            if (chatId != null && chatId != 0L) {
                telegramBotBridge.sendMessageAsync(token.trim(), chatId, message);
            }
        }
    }

    private List<Long> resolveTelegramTargets() {
        if (isTelegramGlobalMode()) {
            return lastTelegramCommandChatId != 0L ? List.of(lastTelegramCommandChatId) : List.of();
        }

        List<Long> whitelist = getTelegramWhitelistIds();
        if (!whitelist.isEmpty()) {
            return whitelist;
        }
        return lastTelegramCommandChatId != 0L ? List.of(lastTelegramCommandChatId) : List.of();
    }

    private int parseIntSafe(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private long parseLongSafe(String value) {
        if (value == null || value.isBlank()) {
            return -1L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return -1L;
        }
    }

    private String buildTelegramStatsMessage() {
        long now = System.currentTimeMillis();
        long currentCoins = parseCoinsFromScoreboard();
        if (currentCoins >= 0L) {
            lastKnownCoins = currentCoins;
        }

        long uptimeMs = autoBuyEnabled && autoBuyStartMs > 0L ? now - autoBuyStartMs : 0L;
        StringBuilder message = new StringBuilder();
        message.append("РЎР‚РЎСџРІР‚СљР вЂ° AutoBuy stats").append('\n');
        message.append("Р Р†РЎв„ўРІвЂћСћ Р В Р Р‹Р РЋРІР‚С™Р В Р’В°Р РЋРІР‚С™Р РЋРЎвЂњР РЋР С“: ").append(autoBuyEnabled ? "Р В Р вЂ Р В РЎвЂќР В Р’В»Р РЋР вЂ№Р РЋРІР‚РЋР В Р’ВµР В Р вЂ¦" : "Р В Р вЂ Р РЋРІР‚в„–Р В РЎвЂќР В Р’В»Р РЋР вЂ№Р РЋРІР‚РЋР В Р’ВµР В Р вЂ¦").append('\n');
        message.append("РЎР‚РЎСџР Р‰РЎвЂ™ Р В Р’В Р В Р’ВµР В Р’В¶Р В РЎвЂР В РЎВ: ").append(serverMode.getSelected()).append('\n');
        message.append("Р Р†Р РЏР’В± Р В РІР‚в„ўР РЋР вЂљР В Р’ВµР В РЎВР РЋР РЏ Р РЋР вЂљР В Р’В°Р В Р’В±Р В РЎвЂўР РЋРІР‚С™Р РЋРІР‚в„–: ").append(formatDuration(uptimeMs)).append('\n');

        if (currentCoins >= 0L) {
            message.append("РЎР‚РЎСџР вЂћРІвЂћСћ Р В РЎС™Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР РЋРІР‚С™Р РЋРІР‚в„– Р РЋР С“Р В Р’ВµР В РІвЂћвЂ“Р РЋРІР‚РЋР В Р’В°Р РЋР С“: ").append(formatNumber(currentCoins)).append('\n');
        } else if (lastKnownCoins >= 0L) {
            message.append("РЎР‚РЎСџР вЂћРІвЂћСћ Р В РЎС™Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР РЋРІР‚С™Р РЋРІР‚в„– Р РЋР С“Р В Р’ВµР В РІвЂћвЂ“Р РЋРІР‚РЋР В Р’В°Р РЋР С“: ").append(formatNumber(lastKnownCoins)).append(" (Р В РЎвЂ”Р В РЎвЂўР РЋР С“Р В Р’В»Р В Р’ВµР В РўвЂР В Р вЂ¦Р В РЎвЂР В Р’Вµ)").append('\n');
        } else {
            message.append("РЎР‚РЎСџР вЂћРІвЂћСћ Р В РЎС™Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР РЋРІР‚С™Р РЋРІР‚в„– Р РЋР С“Р В Р’ВµР В РІвЂћвЂ“Р РЋРІР‚РЋР В Р’В°Р РЋР С“: Р В Р вЂ¦Р В Р’Вµ Р В Р вЂ¦Р В Р’В°Р В РІвЂћвЂ“Р В РўвЂР В Р’ВµР В Р вЂ¦Р РЋРІР‚в„– Р В Р вЂ  Р РЋРІР‚С™Р В Р’В°Р В Р’В±Р В Р’В»Р В РЎвЂў").append('\n');
        }

        boolean hasSessionCoins = autoBuyStartCoins >= 0L && autoBuyStartCoinsMs > 0L;
        if (hasSessionCoins && currentCoins >= 0L) {
            long elapsedMs = Math.max(1L, now - autoBuyStartCoinsMs);
            double elapsedHours = elapsedMs / 3_600_000.0D;
            long earned = currentCoins - autoBuyStartCoins;
            long perHour = elapsedHours > 0.0D ? Math.round(earned / elapsedHours) : 0L;

            message.append("РЎР‚РЎСџРЎв„ўР вЂљ Р В РЎС™Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР РЋРІР‚С™Р РЋРІР‚в„– Р В Р вЂ¦Р В Р’В° Р РЋР С“Р РЋРІР‚С™Р В Р’В°Р РЋР вЂљР РЋРІР‚С™Р В Р’Вµ: ").append(formatNumber(autoBuyStartCoins)).append('\n');
            message.append("РЎР‚РЎСџРІР‚в„ўР’Вµ Р В РІР‚вЂќР В Р’В°Р РЋР вЂљР В Р’В°Р В Р’В±Р В РЎвЂўР РЋРІР‚С™Р В Р’В°Р В Р вЂ¦Р В РЎвЂў: ").append(formatSignedNumber(earned)).append('\n');
            message.append("РЎР‚РЎСџРІР‚СљРІвЂљВ¬ Р В Р Р‹Р РЋР вЂљР В Р’ВµР В РўвЂР В Р вЂ¦Р В Р’ВµР В Р’Вµ Р В Р’В·Р В Р’В° Р РЋРІР‚РЋР В Р’В°Р РЋР С“: ").append(formatSignedNumber(perHour)).append('\n');
            message.append("РЎР‚РЎСџРІР‚вЂќРІР‚Сљ Р В РЎСџР РЋР вЂљР В РЎвЂўР В РЎвЂ“Р В Р вЂ¦Р В РЎвЂўР В Р’В· Р В Р’В·Р В Р’В° 24Р РЋРІР‚РЋ: ").append(formatSignedNumber(perHour * 24L)).append('\n');
        } else {
            message.append("РЎР‚РЎСџРІР‚СљРІвЂљВ¬ Р В Р Р‹Р РЋР вЂљР В Р’ВµР В РўвЂР В Р вЂ¦Р В Р’ВµР В Р’Вµ Р В Р’В·Р В Р’В° Р РЋРІР‚РЋР В Р’В°Р РЋР С“: Р В Р вЂ¦Р В Р’ВµР РЋРІР‚С™ Р В РўвЂР В Р’В°Р В Р вЂ¦Р В Р вЂ¦Р РЋРІР‚в„–Р РЋРІР‚В¦ (Р В Р вЂ Р В РЎвЂќР В Р’В»Р РЋР вЂ№Р РЋРІР‚РЋР В РЎвЂ AutoBuy Р В РЎвЂ Р В РЎвЂўР РЋРІР‚С™Р В РЎвЂќР РЋР вЂљР В РЎвЂўР В РІвЂћвЂ“ scoreboard)").append('\n');
        }

        message.append("РЎР‚РЎСџРІР‚вЂњР’В± Р В РЎв„ўР В Р’В»Р В РЎвЂР В РЎвЂќР В РЎвЂ Р В РЎвЂ”Р В РЎвЂўР В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂќР В РЎвЂ: ").append(buyClicks).append('\n');
        message.append("Р Р†РЎС™РІР‚В¦ Р В РЎСџР В РЎвЂўР В РўвЂР РЋРІР‚С™Р В Р вЂ Р В Р’ВµР РЋР вЂљР В Р’В¶Р В РўвЂР В Р’ВµР В Р вЂ¦Р В РЎвЂў Р В РЎвЂ”Р В РЎвЂўР В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂўР В РЎвЂќ: ").append(getPurchasedEntryCount()).append('\n');
        message.append("РЎР‚РЎСџРІР‚СњРІР‚С› Р В РЎвЂєР В Р’В±Р В Р вЂ¦Р В РЎвЂўР В Р вЂ Р В Р’В»Р В Р’ВµР В Р вЂ¦Р В РЎвЂР В РІвЂћвЂ“ Р В Р’В°Р РЋРЎвЂњР В РЎвЂќР РЋРІР‚В Р В РЎвЂР В РЎвЂўР В Р вЂ¦Р В Р’В°: ").append(refreshCount).append('\n');
        message.append("РЎР‚РЎСџР’В§Р’В° Р В РЎСљР В Р’В°Р РЋР С“Р РЋРІР‚С™Р РЋР вЂљР В РЎвЂўР В Р’ВµР В Р вЂ¦Р В Р вЂ¦Р РЋРІР‚в„–Р РЋРІР‚В¦ Р В РЎвЂ”Р РЋР вЂљР В Р’ВµР В РўвЂР В РЎВР В Р’ВµР РЋРІР‚С™Р В РЎвЂўР В Р вЂ : ").append(getConfiguredItems().size());
        return message.toString();
    }

    private long parseTelegramChatId(String rawChatId) {
        if (rawChatId == null || rawChatId.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(rawChatId.trim());
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private void captureStartCoins(long timestampMs) {
        long startCoins = parseCoinsFromScoreboard();
        if (startCoins < 0L) {
            autoBuyStartCoins = -1L;
            autoBuyStartCoinsMs = 0L;
            return;
        }
        autoBuyStartCoins = startCoins;
        autoBuyStartCoinsMs = timestampMs;
        lastKnownCoins = startCoins;
    }

    private long parseCoinsFromScoreboard() {
        if (mc.world == null) {
            return -1L;
        }

        Scoreboard scoreboard = mc.world.getScoreboard();
        if (scoreboard == null) {
            return -1L;
        }

        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) {
            return -1L;
        }

        long coins = -1L;
        for (ScoreboardEntry entry : scoreboard.getScoreboardEntries(objective)) {
            String rawLine = Team.decorateName(scoreboard.getScoreHolderTeam(entry.owner()), entry.name()).getString();
            String normalized = AutoBuyItem.normalizeLine(rawLine);
            boolean looksLikeCoinsLine = normalized.contains("Р В РЎВР В РЎвЂўР В Р вЂ¦Р В Р’ВµР РЋРІР‚С™")
                    || normalized.contains("Р СР С•Р Р…Р ВµРЎвЂљ")
                    || normalized.contains("Р СР С•Р Р…Р ВµРЎвЂљРЎвЂ№")
                    || normalized.contains("coins")
                    || normalized.contains("coin")
                    || normalized.contains("Р В±Р В°Р В»Р В°Р Р…РЎРѓ")
                    || normalized.contains("balance");
            if (!looksLikeCoinsLine) {
                continue;
            }

            String digits = AutoBuyItem.normalizeDigits(rawLine);
            if (digits.isBlank()) {
                continue;
            }

            try {
                long value = Long.parseLong(digits);
                if (value > coins) {
                    coins = value;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return coins;
    }

    private String formatSignedNumber(long value) {
        if (value > 0L) {
            return "+" + formatNumber(value);
        }
        return formatNumber(value);
    }

    private String formatNumber(long value) {
        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ');
    }

    private String formatDuration(long durationMs) {
        long totalSeconds = Math.max(0L, durationMs / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (hours > 0L) {
            return hours + "\u0447 " + minutes + "\u043c";
        }
        if (minutes > 0L) {
            return minutes + "\u043c " + seconds + "\u0441";
        }
        return seconds + "\u0441";
    }

    private void drawAutoBuyInfo(DrawContext context, int offsetX, int anchorY, int backgroundWidth) {
        long now = System.currentTimeMillis();
        double workSeconds = autoBuyEnabled && autoBuyStartMs > 0 ? (now - autoBuyStartMs) / 1000.0 : 0.0;
        long avgRefresh = refreshIntervals > 0 ? totalRefreshInterval / refreshIntervals : 0;

        List<String> lines = new ArrayList<>();
        String timedSellLine = buildTimedSellNextRunInfo(now);
        if (timedSellLine != null) {
            lines.add(timedSellLine);
        }
        if (isHolyWorldMode() && isSafeModeEnabled()) {
            lines.add("Safe mode: ON");
        }
        lines.add("\u0421\u0440\u0435\u0434\u043d\u0435\u0435 \u0432\u0440\u0435\u043c\u044f \u043e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u044f: " + avgRefresh + "ms");
        lines.add("\u041e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0439 \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0430: " + refreshCount);
        lines.add("\u0412\u0440\u0435\u043c\u044f \u0440\u0430\u0431\u043e\u0442\u044b: " + String.format(Locale.ROOT, "%.1f", workSeconds) + "s");

        int centerX = offsetX + backgroundWidth / 2;
        int color = 0xFFFFFFFF;

        FontRenderer infoFont = Fonts.getSize(14, Fonts.Type.DEFAULT);
        int lineHeight = Math.max(6, (int) infoFont.getStringHeight("A"));
        int lineSpacing = Math.max(3, lineHeight - 4);
        int totalHeight = lineSpacing * Math.max(0, lines.size() - 1) + lineHeight;
        int startY = Math.max(2, anchorY - totalHeight - 8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int w = (int) infoFont.getStringWidth(line);
            infoFont.drawString(context.getMatrices(), line, centerX - w / 2.0F, startY + i * lineSpacing, color);
        }
    }

    private String buildTimedSellNextRunInfo(long now) {
        if (!isHolyWorldMode() || !isAutoSellEnabled() || !isTimedAutoSellEnabled()) {
            return null;
        }

        int targetAnarchy = getPlannedTimedSellTargetAnarchy(now, getCurrentHolyWorldAnarchyFromScoreboard());
        if (targetAnarchy <= 0) {
            return "\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u0437\u0430\u0445\u043e\u0434 \u043d\u0430 \u0430\u043d\u043a\u0443: \u0441\u043b\u0443\u0447\u0430\u0439\u043d\u0430\u044f";
        }

        if (holyWorldTimedSellState != HolyWorldTimedSellState.IDLE) {
            return "\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u0437\u0430\u0445\u043e\u0434 \u043d\u0430 " + targetAnarchy + " \u0447\u0435\u0440\u0435\u0437: \u0432\u044b\u043f\u043e\u043b\u043d\u044f\u0435\u0442\u0441\u044f";
        }

        long intervalMs = getTimedAutoSellIntervalMinutes() * 60_000L;
        long nextRunMs = holyWorldTimedSellNextRunMs > 0L ? holyWorldTimedSellNextRunMs : now + intervalMs;
        long remainMs = Math.max(0L, nextRunMs - now);
        return "\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u0437\u0430\u0445\u043e\u0434 \u043d\u0430 " + targetAnarchy + " \u0447\u0435\u0440\u0435\u0437 " + formatDuration(remainMs);
    }

    private void drawAuctionLabels(DrawContext context, GenericContainerScreen screen, int offsetX, int offsetY, int backgroundHeight) {
        if (mc.player == null) {
            return;
        }

        int color = 0x404040;
        context.drawText(mc.textRenderer, screen.getTitle(), offsetX + 8, offsetY + 6, color, false);
        context.drawText(mc.textRenderer, mc.player.getInventory().getDisplayName(), offsetX + 8, offsetY + backgroundHeight - 94, color, false);
    }

    private void drawPurchaseHistory(DrawContext context, GenericContainerScreen screen, int offsetX, int offsetY, int backgroundWidth, int backgroundHeight) {
        int panelX = Math.max(6, offsetX - HISTORY_PANEL_WIDTH - 8);
        if (panelX + HISTORY_PANEL_WIDTH > screen.width - 6) {
            panelX = Math.min(screen.width - HISTORY_PANEL_WIDTH - 6, offsetX + backgroundWidth + 8);
        }
        int panelY = offsetY;
        int panelH = backgroundHeight;
        int listX = panelX + 6;
        int listY = panelY + HISTORY_HEADER_HEIGHT + 4;
        int listW = HISTORY_PANEL_WIDTH - 12;
        int listH = panelH - HISTORY_HEADER_HEIGHT - 10;

        historyListBounds = new ButtonBounds(panelX + 4, panelY + HISTORY_HEADER_HEIGHT, HISTORY_PANEL_WIDTH - 8, panelH - HISTORY_HEADER_HEIGHT - 4);
        historyScroll = MathHelper.clamp(historyScroll, getMinHistoryScroll(listH), 0.0F);
        historyScrollAnimated = MathHelper.lerp(0.22F, historyScrollAnimated, historyScroll);
        hoveredHistoryEntry = null;
        hoveredHistoryEntryBounds = null;

        int panelFill = ColorAssist.multAlpha(0xFF141820, 0.86F);
        int panelOutline = ColorAssist.multAlpha(0xFFC8D0D7, 0.70F);
        int mutedText = ColorAssist.multAlpha(0xFFB7C0C8, 0.95F);
        int titleColor = ColorAssist.multAlpha(0xFFF2F6FA, 1.0F);

        blur.render(ShapeProperties.create(context.getMatrices(), panelX, panelY, HISTORY_PANEL_WIDTH, panelH)
                .round(8).softness(135).color(0x26000000).build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), panelX, panelY, HISTORY_PANEL_WIDTH, panelH)
                .round(8).thickness(1.05F).outlineColor(panelOutline).color(panelFill).build());

        FontRenderer titleFont = Fonts.getSize(14, Fonts.Type.BOLD);
        FontRenderer metaFont = Fonts.getSize(12, Fonts.Type.DEFAULT);
        FontRenderer entryTitleFont = Fonts.getSize(13, Fonts.Type.BOLD);
        FontRenderer entryMetaFont = Fonts.getSize(12, Fonts.Type.DEFAULT);

        titleFont.drawString(context.getMatrices(), "\u041f\u043e\u043a\u0443\u043f\u043a\u0438", panelX + 9.0F, panelY + 9.0F, titleColor);
        int visibleEntries = getVisibleHistoryEntryCount();
        String headerMeta = "\u0423\u0441\u043f\u0435\u0448\u043d\u043e: " + getPurchasedEntryCount() + " / " + visibleEntries;
        metaFont.drawString(context.getMatrices(), headerMeta, panelX + 9.0F, panelY + 19.0F, mutedText);

        if (visibleEntries == 0 && purchaseHistory.isEmpty()) {
            metaFont.drawCenteredString(context.getMatrices(), "\u041f\u043e\u043a\u0430 \u043f\u0443\u0441\u0442\u043e", panelX + HISTORY_PANEL_WIDTH / 2.0F, panelY + panelH / 2.0F - 4.0F, mutedText);
            return;
        }

        ScissorAssist scissor = zov4ik.getInstance().getScissorManager();
        scissor.push(context.getMatrices().peek().getPositionMatrix(), listX, listY, listW, listH);
        long now = System.currentTimeMillis();
        int visibleIndex = 0;
        for (PurchaseHistoryEntry entry : purchaseHistory) {
            float targetY = entry.removing
                    ? (Float.isNaN(entry.animatedY) ? listY + historyScrollAnimated : entry.animatedY)
                    : listY + historyScrollAnimated + visibleIndex * (HISTORY_ENTRY_HEIGHT + HISTORY_ENTRY_GAP);

            if (Float.isNaN(entry.animatedY)) {
                entry.animatedY = targetY;
            } else if (!entry.removing) {
                entry.animatedY = MathHelper.lerp(0.22F, entry.animatedY, targetY);
            }

            float removeProgress = entry.removing
                    ? MathHelper.clamp((now - entry.removingStartedMs) / (float) HISTORY_REMOVE_ANIM_MS, 0.0F, 1.0F)
                    : 0.0F;
            float removeAlpha = 1.0F - removeProgress;
            if (!entry.removing) {
                visibleIndex++;
            }
            if (removeAlpha <= 0.01F) {
                continue;
            }

            float drawY = entry.animatedY;
            if (drawY + HISTORY_ENTRY_HEIGHT < listY || drawY > listY + listH) {
                continue;
            }

            float reveal = MathHelper.clamp((now - entry.updatedAtMs) / 220.0F, 0.0F, 1.0F);
            float shift = (1.0F - reveal) * 10.0F + removeProgress * 18.0F;
            float alpha = (0.45F + reveal * 0.55F) * removeAlpha;
            float drawX = listX + shift;
            boolean hovered = !entry.removing && Calculate.isHovered(lastMouseX, lastMouseY, drawX, drawY, listW - shift, HISTORY_ENTRY_HEIGHT);
            if (hovered) {
                hoveredHistoryEntry = entry;
                hoveredHistoryEntryBounds = new ButtonBounds((int) drawX, (int) drawY, (int) (listW - shift), HISTORY_ENTRY_HEIGHT);
            }

            int entryFill = hovered
                    ? ColorAssist.multAlpha(0xFF222833, alpha)
                    : ColorAssist.multAlpha(0xFF181E27, alpha);
            int entryOutline = hovered
                    ? ColorAssist.multAlpha(0xFFE3EBF2, alpha)
                    : ColorAssist.multAlpha(0xFF697481, alpha);
            int statusColor = entry.purchased
                    ? ColorAssist.multAlpha(0xFF66E08A, alpha)
                    : ColorAssist.multAlpha(0xFFFF6F6F, alpha);
            String elapsed = formatElapsedTime(entry.updatedAtMs > 0L ? entry.updatedAtMs : entry.createdAtMs, now);
            float timeWidth = entryMetaFont.getStringWidth(elapsed);
            float timeX = drawX + listW - shift - 8.0F - timeWidth;

            rectangle.render(ShapeProperties.create(context.getMatrices(), drawX, drawY, listW - shift, HISTORY_ENTRY_HEIGHT)
                    .round(6).thickness(1.0F).outlineColor(entryOutline).color(entryFill).build());

            Render2D.defaultDrawStack(context, entry.stack, drawX + 6.0F, drawY + 7.0F, false, false, 1.0F);

            String title = entryTitleFont.trimToWidth(entry.title, (int) Math.max(24.0F, listW - 56.0F - timeWidth), false);
            String status = (entry.purchased ? "\u041a\u0443\u043f\u043b\u0435\u043d\u043e" : "\u041d\u0435 \u043a\u0443\u043f\u043b\u0435\u043d\u043e") + " x" + entry.count;
            entryTitleFont.drawString(context.getMatrices(), title, drawX + 28.0F, drawY + 7.0F, ColorAssist.multAlpha(0xFFF4F7FB, alpha));
            entryMetaFont.drawString(context.getMatrices(), elapsed, timeX, drawY + 8.0F, ColorAssist.multAlpha(0xFFAEB8C2, alpha));
            entryMetaFont.drawString(context.getMatrices(), status, drawX + 28.0F, drawY + 18.0F, statusColor);
        }
        scissor.pop();

        float contentHeight = visibleEntries <= 0
                ? 0.0F
                : visibleEntries * (HISTORY_ENTRY_HEIGHT + HISTORY_ENTRY_GAP) - HISTORY_ENTRY_GAP;
        if (contentHeight > listH + 1.0F) {
            float trackX = panelX + HISTORY_PANEL_WIDTH - 4.0F;
            float thumbH = Math.max(24.0F, listH * (listH / contentHeight));
            float maxScroll = Math.max(1.0F, contentHeight - listH);
            float progress = MathHelper.clamp(-historyScrollAnimated / maxScroll, 0.0F, 1.0F);
            float thumbY = listY + progress * (listH - thumbH);
            rectangle.render(ShapeProperties.create(context.getMatrices(), trackX, listY, 2.0F, listH)
                    .round(2).color(ColorAssist.multAlpha(0xFF0D1015, 0.65F)).build());
            rectangle.render(ShapeProperties.create(context.getMatrices(), trackX, thumbY, 2.0F, thumbH)
                    .round(2).color(ColorAssist.multAlpha(0xFFD7E1E9, 0.88F)).build());
        }

        if (hoveredHistoryEntry != null) {
            context.drawItemTooltip(mc.textRenderer, hoveredHistoryEntry.stack, lastMouseX, lastMouseY);
        }
    }

    private void drawVanillaButton(DrawContext context, ButtonBounds bounds, boolean hovered) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Identifier texture = hovered ? BUTTON_HOVER_TEXTURE : BUTTON_TEXTURE;
        context.drawGuiTexture(RenderLayer::getGuiTextured, texture, bounds.x, bounds.y, bounds.w, bounds.h);
    }

    private void drawButtonText(DrawContext context, ButtonBounds bounds, String label, String status, int labelColor, int statusColor) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int labelWidth = mc.textRenderer.getWidth(label);
        int statusWidth = mc.textRenderer.getWidth(status);
        int totalWidth = labelWidth + 4 + statusWidth;
        int startX = bounds.x + (bounds.w - totalWidth) / 2;
        int textY = bounds.y + (bounds.h - 8) / 2;
        context.drawText(mc.textRenderer, label, startX, textY, labelColor, false);
        context.drawText(mc.textRenderer, status, startX + labelWidth + 4, textY, statusColor, false);
    }

    private void drawAutoSetupSlider(DrawContext context, ButtonBounds bounds, boolean hovered) {
        float min = autoSetupDiscount.getMin();
        float max = autoSetupDiscount.getMax();
        float value = MathHelper.clamp(autoSetupDiscount.getValue(), min, max);
        float progress = max <= min ? 0.0F : (value - min) / (max - min);
        progress = MathHelper.clamp(progress, 0.0F, 1.0F);

        int textColor = hovered ? 0xFFFFFFFF : 0xFFD7DDE4;
        String text = Math.round(value) + "% \u043e\u0442 \u0446\u0435\u043d\u044b";
        int textY = bounds.y + 2;
        int textW = mc.textRenderer.getWidth(text);
        context.drawText(mc.textRenderer, text, bounds.x + (bounds.w - textW) / 2, textY, textColor, false);

        int trackX = bounds.x + 8;
        int trackW = Math.max(24, bounds.w - 16);
        int trackY = bounds.y + bounds.h - 8;
        int trackH = 4;
        int fillW = Math.max(0, Math.round(trackW * progress));
        int knobX = trackX + Math.max(0, Math.min(trackW, fillW));

        rectangle.render(ShapeProperties.create(context.getMatrices(), trackX, trackY, trackW, trackH)
                .round(2).color(0x7A171C22).build());
        if (fillW > 0) {
            rectangle.render(ShapeProperties.create(context.getMatrices(), trackX, trackY, fillW, trackH)
                    .round(2).color(0xC84DFF4D).build());
        }
        rectangle.render(ShapeProperties.create(context.getMatrices(), knobX - 3.0F, trackY - 2.0F, 6.0F, 8.0F)
                .round(2).thickness(1.0F).outlineColor(hovered ? 0xFFF7FBFF : 0xFFD5DEE8).color(0xFF10161D).build());
    }

    private void updateAutoSetupPercentByMouse(int mouseX) {
        if (autoSetupSliderBounds == null) {
            return;
        }
        int sliderStart = autoSetupSliderBounds.x + 8;
        int sliderWidth = Math.max(24, autoSetupSliderBounds.w - 16);
        float progress = (mouseX - sliderStart) / (float) sliderWidth;
        progress = MathHelper.clamp(progress, 0.0F, 1.0F);
        float value = autoSetupDiscount.getMin() + (autoSetupDiscount.getMax() - autoSetupDiscount.getMin()) * progress;
        autoSetupDiscount.setValue(Math.round(value));
    }

    private String formatElapsedTime(long sinceMs, long nowMs) {
        long elapsedSeconds = Math.max(0L, (nowMs - sinceMs) / 1000L);
        long hours = elapsedSeconds / 3600L;
        long minutes = (elapsedSeconds % 3600L) / 60L;
        long seconds = elapsedSeconds % 60L;

        if (hours > 0L) {
            return hours + "\u0447 " + minutes + "\u043c";
        }
        if (minutes > 0L) {
            return minutes + "\u043c " + seconds + "\u0441";
        }
        return seconds + "\u0441";
    }

    private void openAuctionOnEnable() {
        if (!isHolyWorldMode() || mc.player == null || mc.player.networkHandler == null) {
            return;
        }
        if (mc.currentScreen instanceof AutoBuyScreen) {
            return;
        }
        if (mc.currentScreen instanceof GenericContainerScreen screen
                && (isAuctionScreen(screen) || isHolyWorldPurchaseConfirmScreen(screen))) {
            return;
        }
        queueVisibleChatCommand("/ah");
    }

    private void tickAutoSetup() {
        autoBuyScript.cleanup();
        long now = System.currentTimeMillis();
        tickScriptedLookRoutine(now);

        if (!isHolyWorldMode()) {
            setAutoSetupEnabled(false);
            return;
        }

        List<AutoBuyItem> queue = getAutoSetupQueue();
        if (queue.isEmpty()) {
            setAutoSetupEnabled(false);
            return;
        }

        if (autoSetupIndex >= queue.size()) {
            startScriptedLookRoutine(now, true);
            setAutoSetupEnabled(false);
            return;
        }

        if (autoSetupItem == null) {
            autoSetupItem = queue.get(autoSetupIndex);
            autoSetupStage = 0;
            autoSetupRefreshesLeft = 0;
            // Старт обработки предмета почти сразу.
            autoSetupNextActionMs = now + randomBetween(40L, 120L);
            autoSetupWatch.reset();
        }

        // Жёсткий лимит ~2с на один предмет: если зависли — ставим цену из того, что есть, и идём дальше.
        if (autoSetupWatch.finished(AUTOSETUP_ITEM_MAX_MS)) {
            GenericContainerScreen capScreen = mc.currentScreen instanceof GenericContainerScreen s && isAuctionScreen(s)
                    ? s
                    : null;
            if (capScreen != null) {
                applyAutoSetupCheapestPrice(capScreen, autoSetupItem);
            }
            advanceAutoSetupItem(now);
            return;
        }

        if (now < autoSetupNextActionMs) {
            return;
        }

        GenericContainerScreen auctionScreen = mc.currentScreen instanceof GenericContainerScreen screen && isAuctionScreen(screen)
                ? screen
                : null;
        switch (autoSetupStage) {
            case 0 -> {
                String searchName = autoSetupItem.getSearchName();
                if (searchName == null || searchName.isBlank()) {
                    advanceAutoSetupItem(now);
                    return;
                }
                queueVisibleChatCommand("/ah search " + searchName, true);
                autoSetupRefreshesLeft = (int) randomBetween(AUTOSETUP_REFRESH_CLICKS_MIN, AUTOSETUP_REFRESH_CLICKS_MAX);
                autoSetupStage = 1;
                // Ждём появления окна аукциона после поиска.
                autoSetupNextActionMs = now + randomBetween(220L, 380L);
            }
            case 1 -> {
                if (auctionScreen != null) {
                    autoSetupStage = 2;
                    autoSetupNextActionMs = now + randomBetween(60L, 140L);
                    return;
                }
                autoSetupNextActionMs = now + randomBetween(80L, 160L);
            }
            case 2 -> {
                if (auctionScreen == null) {
                    autoSetupStage = 1;
                    autoSetupNextActionMs = now + randomBetween(100L, 220L);
                    return;
                }
                if (autoSetupRefreshesLeft > 0) {
                    clickRefresh(auctionScreen);
                    autoSetupRefreshesLeft--;
                    autoSetupNextActionMs = now + pickAutoSetupRefreshDelayMs();
                    return;
                }
                autoSetupStage = 3;
                autoSetupNextActionMs = now + randomBetween(80L, 160L);
            }
            case 3 -> {
                if (auctionScreen == null) {
                    autoSetupStage = 1;
                    autoSetupNextActionMs = now + randomBetween(100L, 220L);
                    return;
                }
                // Почти мгновенная установка цены по самому дешёвому лоту.
                applyAutoSetupCheapestPrice(auctionScreen, autoSetupItem);
                autoSetupStage = 4;
                autoSetupNextActionMs = now + randomBetween(60L, 140L);
            }
            case 4 -> {
                advanceAutoSetupItem(now);
            }
            default -> {
                autoSetupStage = 0;
                autoSetupRefreshesLeft = 0;
                autoSetupNextActionMs = now + randomBetween(80L, 160L);
            }
        }
    }

    private void applyAutoSetupCheapestPrice(GenericContainerScreen auctionScreen, AutoBuyItem item) {
        int cheapest = findCheapestPrice(auctionScreen, item);
        if (cheapest > 0) {
            float pricePercent = MathHelper.clamp(autoSetupDiscount.getValue(), autoSetupDiscount.getMin(), autoSetupDiscount.getMax()) / 100.0F;
            long target = Math.max(1L, Math.round(cheapest * pricePercent));
            if (target != item.getPriceValue()) {
                item.setRawPrice(String.valueOf(target));
                autoSetupChangedAnyPrice = true;
            }
        }
    }

    private void advanceAutoSetupItem(long now) {
        autoSetupStage = 0;
        autoSetupRefreshesLeft = 0;
        autoSetupIndex++;
        autoSetupItem = null;
        autoSetupNextActionMs = now + randomBetween(80L, 200L);
        if (ThreadLocalRandom.current().nextDouble() < 0.28D) {
            startScriptedLookRoutine(now, false);
        }
    }

    private List<AutoBuyItem> getAutoSetupQueue() {
        return getItems().stream()
                .filter(item -> item.isSetupEnabled())
                .sorted(Comparator.comparingInt(this::sortBucket)
                        .thenComparing(AutoBuyItem::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private long pickAutoSetupRefreshDelayMs() {
        long delay = randomBetween(AUTOSETUP_REFRESH_MIN_DELAY_MS, AUTOSETUP_REFRESH_MAX_DELAY_MS);
        if (ThreadLocalRandom.current().nextDouble() < 0.36D) {
            delay += randomBetween(120L, 760L);
        }
        if (ThreadLocalRandom.current().nextDouble() < 0.13D) {
            delay += randomBetween(900L, 2_300L);
        }
        return delay;
    }

    private void persistAutoSetupConfigs() {
        if (zov4ik.getInstance().getFileController() == null) {
            return;
        }
        zov4ik.getInstance().getFileController().saveFile(ModuleFile.class);
        zov4ik.getInstance().getFileController().saveFile(AutoBuyConfigFile.class);
    }

    private int findCheapestPrice(GenericContainerScreen screen, AutoBuyItem item) {
        int cheapest = Integer.MAX_VALUE;
        List<Slot> slots = screen.getScreenHandler().slots;
        int endIndex = Math.min(slots.size() - 1, 44);
        for (int i = 0; i <= endIndex; i++) {
            Slot slot = slots.get(i);
            if (slot == null || !slot.hasStack()) {
                continue;
            }
            if (slot.inventory instanceof net.minecraft.entity.player.PlayerInventory) {
                continue;
            }
            ItemStack stack = slot.getStack();
            if (!item.matches(stack, List.of())) {
                continue;
            }
            int unitPrice = getUnitPrice(stack);
            if (unitPrice <= 0) {
                int totalPrice = getTotalPrice(stack);
                if (totalPrice > 0) {
                    unitPrice = totalPrice / Math.max(1, stack.getCount());
                }
            }
            if (unitPrice > 0 && unitPrice < cheapest) {
                cheapest = unitPrice;
            }
        }
        return cheapest == Integer.MAX_VALUE ? -1 : cheapest;
    }

    private boolean shouldShowAuctionOverlay(GenericContainerScreen screen) {
        long now = System.currentTimeMillis();
        if (isAuctionScreen(screen)) {
            lastAuctionSeenMs = now;
            return true;
        }
        return now - lastAuctionSeenMs < 3000L;
    }

    private void adjustAutoSetupDiscount(int delta) {
        float value = autoSetupDiscount.getValue();
        float next = value + delta;
        if (next < autoSetupDiscount.getMin()) {
            next = autoSetupDiscount.getMin();
        } else if (next > autoSetupDiscount.getMax()) {
            next = autoSetupDiscount.getMax();
        }
        autoSetupDiscount.setValue(Math.round(next));
    }

    private boolean isSneaking() {
        return mc.options != null && mc.options.sneakKey.isPressed();
    }

    private boolean isSpookyTimeMode() {
        return "SpookyTime".equals(serverMode.getSelected());
    }

    private boolean isHolyWorldMode() {
        return "HolyWorld".equals(serverMode.getSelected());
    }

    private boolean isFunTimeMode() {
        return "FunTime".equals(serverMode.getSelected());
    }

    private FunTimeAutoSell getFunTimeAutoSell() {
        return zov4ik.getInstance().getModuleProvider() == null
                ? null
                : zov4ik.getInstance().getModuleProvider().get(FunTimeAutoSell.class);
    }

    private FunTimeAutoParser getFunTimeAutoParser() {
        return zov4ik.getInstance().getModuleProvider() == null
                ? null
                : zov4ik.getInstance().getModuleProvider().get(FunTimeAutoParser.class);
    }

    private boolean isFunTimeAutoSellEnabled() {
        FunTimeAutoSell module = getFunTimeAutoSell();
        return module != null && module.isState();
    }

    private boolean isFunTimeAutoParserEnabled() {
        FunTimeAutoParser module = getFunTimeAutoParser();
        return module != null && module.isState();
    }

    private void toggleFunTimeAutoSell() {
        FunTimeAutoSell module = getFunTimeAutoSell();
        if (module == null) {
            im.zov4ik.utils.client.chat.ChatMessage.brandmessage("§c[AutoBuy] FunTimeAutoSell module not found in registry");
            return;
        }
        module.switchState();
    }

    private void toggleFunTimeAutoParser() {
        FunTimeAutoParser module = getFunTimeAutoParser();
        if (module == null) {
            im.zov4ik.utils.client.chat.ChatMessage.brandmessage("§c[AutoBuy] FunTimeAutoParser module not found in registry");
            return;
        }
        module.switchState();
    }

    private boolean isAutoBuyWorkTest() {
        return isHolyWorldMode() && "Test".equalsIgnoreCase(autoBuyWork.getSelected());
    }

    private boolean isAutoSellEnabled() {
        return isHolyWorldMode() && "On".equalsIgnoreCase(autoSellMode.getSelected());
    }

    private boolean isDebugAbEnabled() {
        return isHolyWorldMode() && debugAb.isValue();
    }

    private boolean isSafeModeEnabled() {
        return isHolyWorldMode() && safeMode.isValue();
    }

    private boolean isTimedAutoSellEnabled() {
        return isAutoSellEnabled() && !isAutoBuyWorkTest() && "Timer".equalsIgnoreCase(autoSellTriggerMode.getSelected());
    }

    private boolean isHolyWorldShakeOff() {
        return isSafeModeEnabled() || !isHolyWorldShakeWindowActive(System.currentTimeMillis());
    }

    // Тряска (обход) работает только первые SHAKE_WINDOW_MS мс после включения AutoBuy,
    // дальше отключается — чтобы бот не дёргался и не зависал.
    private boolean isHolyWorldShakeWindowActive(long now) {
        return autoBuyEnabled && autoBuyStartMs > 0L && (now - autoBuyStartMs) <= SHAKE_WINDOW_MS;
    }

    private int getTimedAutoSellIntervalMinutes() {
        String raw = AutoBuyItem.normalizeDigits(autoSellTimerMinutes.getText());
        int parsed = parseIntSafe(raw);
        return Math.max(1, parsed > 0 ? parsed : 5);
    }

    private long pickTimedAutoSellIntervalMs() {
        long base = getTimedAutoSellIntervalMinutes() * 60_000L;
        long jitter = Math.min(90_000L, Math.max(12_000L, Math.round(base * 0.22D)));
        return Math.max(60_000L, base + randomBetween(-jitter, jitter));
    }

    private int getTimedAutoSellTargetAnarchy() {
        String raw = AutoBuyItem.normalizeDigits(autoSellTimerLight.getText());
        int parsed = parseIntSafe(raw);
        if (parsed < HOLYWORLD_RANDOM_RCT_MIN || parsed > HOLYWORLD_RANDOM_RCT_HARD_MAX) {
            return -1;
        }
        if (HOLYWORLD_BLOCKED_RCT.contains(parsed)) {
            return -1;
        }
        return parsed;
    }

    private int getPlannedTimedSellTargetAnarchy(long now, int currentAnarchy) {
        if (holyWorldTimedSellPlannedAnarchy > 0) {
            return holyWorldTimedSellPlannedAnarchy;
        }

        int planned = chooseNextHolyWorldAnarchyAvoiding(now, currentAnarchy > 0 ? currentAnarchy : -1);
        if (planned <= 0) {
            planned = chooseNextHolyWorldAnarchy(now);
        }
        if (planned <= 0) {
            planned = getTimedAutoSellTargetAnarchy();
        }
        holyWorldTimedSellPlannedAnarchy = planned;
        return planned;
    }

    private int getCurrentHolyWorldAnarchyFromScoreboard() {
        if (mc.world == null) {
            return -1;
        }

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) {
            return -1;
        }

        for (ScoreboardEntry entry : scoreboard.getScoreboardEntries(objective)) {
            String text = Team.decorateName(scoreboard.getScoreHolderTeam(entry.owner()), entry.name()).getString();
            String normalized = AutoBuyItem.normalizeLine(text);
            Matcher matcher = HOLYWORLD_SCOREBOARD_ANARCHY_PATTERN.matcher(normalized);
            if (matcher.find()) {
                int parsed = parseIntSafe(AutoBuyItem.normalizeDigits(matcher.group(1)));
                if (parsed > 0) {
                    return parsed;
                }
            }
        }

        return -1;
    }

    private boolean isHolyWorldLightConfirmedByScoreboard(int anarchyFromScoreboard) {
        return anarchyFromScoreboard > 0 && parseCoinsFromScoreboard() >= 0L;
    }

    private boolean isStaffLeaveEnabled() {
        return isHolyWorldMode() && leaveFromStaff.isValue();
    }

    private boolean isHolyWorldStaffEscapeBlocking() {
        return holyWorldStaffHubCooldownUntilMs > 0L || holyWorldStaffHubReturnQueued;
    }

    private void tickHolyWorldStaffEscape() {
        long now = System.currentTimeMillis();
        if (!autoBuyEnabled || !isHolyWorldMode() || !isStaffLeaveEnabled()) {
            clearHolyWorldStaffEscapeState();
            return;
        }
        if (mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }

        int currentAnarchy = getCurrentHolyWorldAnarchyFromScoreboard();
        if (currentAnarchy != holyWorldLastObservedAnarchy) {
            holyWorldKnownOnlineStaff.clear();
            holyWorldLastObservedAnarchy = currentAnarchy;
        }
        Set<String> onlineStaff = collectOnlineTrackedStaffNames();
        if (!onlineStaff.isEmpty() && !isHolyWorldStaffEscapeBlocking()) {
            String detected = onlineStaff.iterator().next();
            holyWorldTrackedStaffName = detected;
            holyWorldTrackedStaffLastSeenMs = now;
            if (currentAnarchy > 0) {
                holyWorldTrackedStaffLastAnarchy = currentAnarchy;
            }
            if (now - holyWorldStaffLastActionMs >= HOLYWORLD_STAFF_ACTION_DELAY_MS) {
                leaveHolyWorldFromStaff(now, currentAnarchy);
                holyWorldStaffLastActionMs = now;
            }
        }

        for (String staffName : onlineStaff) {
            if (!holyWorldKnownOnlineStaff.contains(staffName)) {
                handleHolyWorldStaffJoin(staffName, currentAnarchy, now);
                break;
            }
        }

        holyWorldKnownOnlineStaff.clear();
        holyWorldKnownOnlineStaff.addAll(onlineStaff);
        handleHolyWorldStaffEscapeCooldown(currentAnarchy, now);
    }

    private void clearHolyWorldStaffEscapeState() {
        holyWorldKnownOnlineStaff.clear();
        holyWorldTrackedStaffName = "";
        holyWorldTrackedStaffHits = 0;
        holyWorldLastObservedAnarchy = -1;
        holyWorldTrackedStaffLastAnarchy = -1;
        holyWorldTrackedStaffLastSeenMs = 0L;
        holyWorldStaffHubCooldownUntilMs = 0L;
        holyWorldStaffHubReturnQueued = false;
        holyWorldStaffLastActionMs = 0L;
    }

    private Set<String> collectOnlineTrackedStaffNames() {
        Set<String> result = new HashSet<>();
        if (mc.getNetworkHandler() == null) {
            return result;
        }

        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            if (entry == null || entry.getProfile() == null) {
                continue;
            }
            String name = entry.getProfile().getName();
            if (name == null || name.isBlank()) {
                continue;
            }
            if (StaffRepository.isStaff(name)) {
                result.add(name.toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }

    private boolean hasOnlineTrackedStaff() {
        if (!isHolyWorldMode()) {
            return false;
        }
        if (mc == null || mc.getNetworkHandler() == null) {
            return !holyWorldKnownOnlineStaff.isEmpty();
        }
        return !collectOnlineTrackedStaffNames().isEmpty();
    }

    private void handleHolyWorldStaffJoin(String staffName, int currentAnarchy, long now) {
        if (staffName == null || staffName.isBlank()) {
            return;
        }
        if (isHolyWorldStaffEscapeBlocking()) {
            return;
        }

        boolean sameStaff = staffName.equals(holyWorldTrackedStaffName);
        boolean recent = now - holyWorldTrackedStaffLastSeenMs <= HOLYWORLD_STAFF_FOLLOW_WINDOW_MS;
        boolean changedAnarchy = currentAnarchy > 0
                && holyWorldTrackedStaffLastAnarchy > 0
                && currentAnarchy != holyWorldTrackedStaffLastAnarchy;

        if (sameStaff && recent && changedAnarchy) {
            holyWorldTrackedStaffHits++;
        } else {
            holyWorldTrackedStaffName = staffName;
            holyWorldTrackedStaffHits = 1;
        }

        holyWorldTrackedStaffLastSeenMs = now;
        if (currentAnarchy > 0) {
            holyWorldTrackedStaffLastAnarchy = currentAnarchy;
        }

        if (holyWorldTrackedStaffHits >= HOLYWORLD_STAFF_FOLLOW_TO_HUB_THRESHOLD) {
            startHolyWorldStaffHubCooldown(now);
            return;
        }

        leaveHolyWorldFromStaff(now, currentAnarchy);
    }

    private void handleHolyWorldStaffEscapeCooldown(int currentAnarchy, long now) {
        if (!isHolyWorldStaffEscapeBlocking()) {
            return;
        }

        holyWorldNeedAuctionReopen = false;
        holyWorldAuctionReopenWatch.reset();
        holyWorldWalkState = HolyWorldWalkState.IDLE;
        holyWorldWalkDeadlineMs = 0L;
        holyWorldStageStartedMs = 0L;
        holyWorldNextMoveSwitchMs = 0L;
        releaseHolyWorldMovementKeys();
        stopHolyWorldRotation();
        resetHolyWorldAutoSellState();
        resetHolyWorldTimedSellState();

        if (now < holyWorldStaffHubCooldownUntilMs) {
            holyWorldStaffHubReturnQueued = false;
            if (currentAnarchy > 0
                    && !isTypingChatNow()
                    && now - holyWorldStaffLastActionMs >= HOLYWORLD_STAFF_ACTION_DELAY_MS) {
                queueVisibleChatCommand("/hub");
                holyWorldStaffLastActionMs = now;
            }
            return;
        }

        if (!holyWorldStaffHubReturnQueued) {
            if (currentAnarchy > 0) {
                if (!isTypingChatNow() && now - holyWorldStaffLastActionMs >= HOLYWORLD_STAFF_ACTION_DELAY_MS) {
                    queueVisibleChatCommand("/hub");
                    holyWorldStaffLastActionMs = now;
                }
                return;
            }

            if (!isTypingChatNow() && now - holyWorldStaffLastActionMs >= HOLYWORLD_STAFF_ACTION_DELAY_MS) {
                int target = chooseNextHolyWorldAnarchyAvoiding(now, -1);
                if (target > 0 && beginHolyWorldRctSequence(target, now, "staff_cooldown_return")) {
                    holyWorldStaffHubReturnQueued = true;
                    holyWorldStaffLastActionMs = now;
                }
            }
            return;
        }

        if (currentAnarchy > 0) {
            holyWorldStaffHubCooldownUntilMs = 0L;
            holyWorldStaffHubReturnQueued = false;
            holyWorldTrackedStaffHits = 0;
            holyWorldTrackedStaffLastAnarchy = currentAnarchy;
            holyWorldTrackedStaffLastSeenMs = now;
            holyWorldNeedAuctionReopen = true;
            holyWorldAuctionReopenWatch.reset();
        }
    }

    private void startHolyWorldStaffHubCooldown(long now) {
        clearVisibleChatTypingState();
        clearHolyWorldPendingRctSequence();
        holyWorldStaffHubCooldownUntilMs = now + HOLYWORLD_STAFF_HUB_COOLDOWN_MS;
        holyWorldStaffHubReturnQueued = false;
        holyWorldStaffLastActionMs = 0L;
        queueVisibleChatCommand("/hub");
        holyWorldStaffLastActionMs = now;
    }

    private void leaveHolyWorldFromStaff(long now, int currentAnarchy) {
        int target = chooseNextHolyWorldAnarchyAvoiding(now, currentAnarchy);
        if (target <= 0) {
            return;
        }
        clearVisibleChatTypingState();
        clearHolyWorldPendingRctSequence();
        beginHolyWorldRctSequence(target, now, "staff_leave");
    }

    private int chooseNextHolyWorldAnarchyAvoiding(long now, int excludedAnarchy) {
        cleanExpiredHolyWorldAnarchyEntries(now);

        List<Integer> available = new ArrayList<>();
        int cappedMax = Math.min(HOLYWORLD_RANDOM_RCT_MAX, HOLYWORLD_RANDOM_RCT_HARD_MAX);
        for (int i = HOLYWORLD_RANDOM_RCT_MIN; i <= cappedMax; i++) {
            if (i == excludedAnarchy || HOLYWORLD_BLOCKED_RCT.contains(i)) {
                continue;
            }
            Long until = holyWorldRecentAnarchyEntries.get(i);
            if (until == null || until <= now) {
                available.add(i);
            }
        }

        if (available.isEmpty()) {
            for (int i = HOLYWORLD_RANDOM_RCT_MIN; i <= cappedMax; i++) {
                if (i == excludedAnarchy || HOLYWORLD_BLOCKED_RCT.contains(i)) {
                    continue;
                }
                available.add(i);
            }
        }

        if (available.isEmpty()) {
            return -1;
        }

        int selected = available.get(ThreadLocalRandom.current().nextInt(available.size()));
        holyWorldRecentAnarchyEntries.put(selected, now + HOLYWORLD_RCT_REUSE_COOLDOWN_MS);
        return selected;
    }

    private boolean beginHolyWorldRctSequence(int anarchy, long now) {
        return beginHolyWorldRctSequence(anarchy, now, "generic");
    }

    private boolean beginHolyWorldRctSequence(int anarchy, long now, String source) {
        if (anarchy <= 0 || mc.player == null || mc.player.networkHandler == null) {
            return false;
        }

        queueVisibleChatCommand(buildRctChatCommand(anarchy));
        logDebugAb("RCT_BEGIN",
                "target=" + anarchy
                        + ", attempts=1"
                        + ", source=" + (source == null || source.isBlank() ? "unknown" : source));
        holyWorldLastRctAnarchy = anarchy;
        holyWorldLastRctSentMs = now;
        holyWorldPendingRctAnarchy = anarchy;
        holyWorldPendingRctAttempts = 1;
        holyWorldPendingRctNeedsHubRetry = true;
        holyWorldPendingRctNeedCompassRecovery = false;
        holyWorldPendingRctCompassAttempts = 0;
        holyWorldNextRctCompassRecoveryMs = 0L;
        holyWorldPendingRctCreatedMs = now;
        holyWorldNextRctRetryMs = now + randomBetween(HOLYWORLD_RCT_RETRY_MIN_DELAY_MS, HOLYWORLD_RCT_RETRY_MAX_DELAY_MS);
        holyWorldNeedAuctionReopen = true;
        holyWorldAuctionReopenWatch.reset();
        return true;
    }

    private void clearHolyWorldPendingRctSequence() {
        holyWorldPendingRctAnarchy = -1;
        holyWorldPendingRctAttempts = 0;
        holyWorldPendingRctNeedsHubRetry = false;
        holyWorldPendingRctNeedCompassRecovery = false;
        holyWorldPendingRctCompassAttempts = 0;
        holyWorldNextRctCompassRecoveryMs = 0L;
        holyWorldPendingRctCreatedMs = 0L;
        holyWorldNextRctRetryMs = 0L;
    }

    private void queueVisibleChatCommand(String command) {
        queueVisibleChatCommand(command, false);
    }

    private void queueVisibleChatCommand(String command, boolean fastTyping) {
        if (command == null) {
            return;
        }
        String normalized = command.trim();
        if (normalized.isEmpty()) {
            return;
        }
        boolean resolvedFastTyping = fastTyping && !isSafeModeEnabled();
        int maxQueueSize = isSafeModeEnabled() ? 18 : 30;
        if (pendingVisibleChatCommands.size() > maxQueueSize) {
            pendingVisibleChatCommands.pollFirst();
            pendingVisibleChatFastCommands.pollFirst();
        }
        pendingVisibleChatCommands.addLast(normalized);
        pendingVisibleChatFastCommands.addLast(resolvedFastTyping);
        debugAbQueuedCommands++;
        logDebugAb("QUEUE_CMD",
                "fast=" + resolvedFastTyping
                        + ", queue=" + pendingVisibleChatCommands.size()
                        + ", cmd=" + normalized);
    }

    private void clearVisibleChatTypingState() {
        pendingVisibleChatCommands.clear();
        pendingVisibleChatFastCommands.clear();
        typingVisibleChatCommand = false;
        typingVisibleChatFastMode = false;
        typingVisibleChatUltraFastMode = false;
        typingVisibleChatText = "";
        typingVisibleChatIndex = 0;
        typingVisibleChatNextActionMs = 0L;
        typingVisibleChatTyposRemaining = 0;
        typingVisibleChatNeedsBackspace = false;
        typingVisibleChatCharsSincePause = 0;
        typingVisibleChatPauseEveryChars = 0;
        typingVisibleChatStartedMs = 0L;
    }

    private boolean isTypingChatNow() {
        return typingVisibleChatCommand || !pendingVisibleChatCommands.isEmpty();
    }

    private boolean isUltraFastAutoSellCommand(String command) {
        if (isSafeModeEnabled() || command == null || command.isBlank()) {
            return false;
        }
        String normalized = command.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("/ah sell auto");
    }

    private long pickTypingOpenDelay(boolean fast, boolean ultraFast) {
        if (ultraFast) {
            return randomBetween(CHAT_TYPE_ULTRA_FAST_OPEN_DELAY_MIN_MS, CHAT_TYPE_ULTRA_FAST_OPEN_DELAY_MAX_MS);
        }
        if (fast) {
            return randomBetween(CHAT_TYPE_FAST_OPEN_DELAY_MIN_MS, CHAT_TYPE_FAST_OPEN_DELAY_MAX_MS);
        }
        return randomBetween(CHAT_TYPE_OPEN_DELAY_MIN_MS, CHAT_TYPE_OPEN_DELAY_MAX_MS);
    }

    private long pickTypingCharDelay(boolean fast, boolean ultraFast) {
        if (ultraFast) {
            return randomBetween(CHAT_TYPE_ULTRA_FAST_CHAR_DELAY_MIN_MS, CHAT_TYPE_ULTRA_FAST_CHAR_DELAY_MAX_MS);
        }
        if (fast) {
            return randomBetween(CHAT_TYPE_FAST_CHAR_DELAY_MIN_MS, CHAT_TYPE_FAST_CHAR_DELAY_MAX_MS);
        }
        return randomBetween(CHAT_TYPE_CHAR_DELAY_MIN_MS, CHAT_TYPE_CHAR_DELAY_MAX_MS);
    }

    private long pickTypingBetweenCommandsDelay(boolean fast, boolean ultraFast) {
        if (ultraFast) {
            return randomBetween(CHAT_TYPE_ULTRA_FAST_BETWEEN_COMMANDS_MIN_MS, CHAT_TYPE_ULTRA_FAST_BETWEEN_COMMANDS_MAX_MS);
        }
        if (fast) {
            return randomBetween(CHAT_TYPE_FAST_BETWEEN_COMMANDS_MIN_MS, CHAT_TYPE_FAST_BETWEEN_COMMANDS_MAX_MS);
        }
        return randomBetween(CHAT_TYPE_BETWEEN_COMMANDS_MIN_MS, CHAT_TYPE_BETWEEN_COMMANDS_MAX_MS);
    }

    private boolean isTurboRefreshKeyHeld() {
        // Турбо-обновление аукциона, пока зажат левый Shift.
        if (mc.getWindow() == null) {
            return false;
        }
        long handle = mc.getWindow().getHandle();
        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private void sendChatCommandDirect(String command) {
        // Прямая отправка команды в чат без имитации печати.
        if (mc.player == null || mc.player.networkHandler == null || command == null) {
            return;
        }
        String trimmed = command.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        if (mc.currentScreen instanceof ChatScreen) {
            mc.currentScreen.close();
        }
        if (trimmed.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(trimmed.substring(1));
        } else {
            mc.player.networkHandler.sendChatMessage(trimmed);
        }
    }

    private void tickVisibleChatCommandTyping() {
        if (mc.player == null) {
            clearVisibleChatTypingState();
            return;
        }

        long now = System.currentTimeMillis();
        if (now < typingVisibleChatNextActionMs) {
            return;
        }

        if (pendingVisibleChatCommands.isEmpty()) {
            return;
        }

        // Соблюдаем лимит частоты команд, чтобы сервер не кикнул за спам.
        if (!canSendVisibleChatCommandNow(now)) {
            typingVisibleChatNextActionMs = now + randomBetween(60L, 160L);
            return;
        }

        String command = pendingVisibleChatCommands.pollFirst();
        Boolean fastFlag = pendingVisibleChatFastCommands.pollFirst();
        boolean fast = fastFlag != null && fastFlag;
        boolean ultraFast = isUltraFastAutoSellCommand(command);

        // Отправляем команду напрямую в чат (без побуквенной имитации).
        sendChatCommandDirect(command);

        debugAbSentCommands++;
        debugAbLastCommandSentMs = now;
        logDebugAb("SEND_CMD_DIRECT",
                "queue_left=" + pendingVisibleChatCommands.size()
                        + ", cmd=" + command);

        // Пауза между командами. Уменьши/увеличь, если надо быстрее/медленнее.
        typingVisibleChatNextActionMs = now + pickTypingBetweenCommandsDelay(fast, ultraFast);
    }

    private boolean canSendVisibleChatCommandNow(long now) {
        cleanupCommandRateWindow(now);
        long minInterval = isSafeModeEnabled() ? COMMAND_SAFE_MIN_INTERVAL_MS : COMMAND_MIN_INTERVAL_MS;
        int maxInWindow = isSafeModeEnabled() ? COMMAND_RATE_SAFE_MAX : COMMAND_RATE_MAX;

        if (commandLastSentMs > 0L && now - commandLastSentMs < minInterval) {
            logDebugAb("CMD_THROTTLE",
                    "reason=min_interval"
                            + ", delta=" + (now - commandLastSentMs)
                            + ", min=" + minInterval
                            + ", window=" + commandRateWindow.size());
            return false;
        }
        if (commandRateWindow.size() >= maxInWindow) {
            logDebugAb("CMD_THROTTLE",
                    "reason=window_limit"
                            + ", window=" + commandRateWindow.size()
                            + ", max=" + maxInWindow);
            return false;
        }

        commandRateWindow.addLast(now);
        commandLastSentMs = now;
        return true;
    }

    private void cleanupCommandRateWindow(long now) {
        long border = now - COMMAND_RATE_WINDOW_MS;
        while (!commandRateWindow.isEmpty()) {
            Long at = commandRateWindow.peekFirst();
            if (at == null || at < border) {
                commandRateWindow.pollFirst();
                continue;
            }
            break;
        }
    }

    private int getUnitPrice(ItemStack stack) {
        if (isHolyWorldMode()) {
            return holyWorldPriceParser.getUnitPrice(stack);
        }
        if (isFunTimeMode()) {
            int total = im.zov4ik.features.impl.misc.autobuy.catalog.util.AuctionUtils.getPrice(stack);
            if (total <= 0) return -1;
            return Math.max(1, total / Math.max(1, stack.getCount()));
        }
        return priceParser.getUnitPrice(stack);
    }

    private int getTotalPrice(ItemStack stack) {
        if (isHolyWorldMode()) {
            return holyWorldPriceParser.getPrice(stack);
        }
        if (isFunTimeMode()) {
            return im.zov4ik.features.impl.misc.autobuy.catalog.util.AuctionUtils.getPrice(stack);
        }
        return priceParser.getPrice(stack);
    }

    private int getAuctionRefreshSlot() {
        if (isHolyWorldMode()) {
            return HOLYWORLD_REFRESH_SLOT;
        }
        if (isFunTimeMode()) {
            return FUNTIME_REFRESH_SLOT;
        }
        if (isSpookyTimeMode()) {
            return SPOOKYTIME_REFRESH_SLOT;
        }
        return -1;
    }

    private Item getAuctionRefreshItem() {
        if (isHolyWorldMode()) {
            return Items.EMERALD;
        }
        if (isFunTimeMode()) {
            return Items.AIR;
        }
        if (isSpookyTimeMode()) {
            return Items.NETHER_STAR;
        }
        return Items.AIR;
    }

    private int getRefreshDelayMs() {
        if (isHolyWorldMode()) {
            // Турбо: пока зажат левый Shift — обновляем раз в SHIFT_TURBO_REFRESH_DELAY_MS мс.
            if (isTurboRefreshKeyHeld()) {
                return SHIFT_TURBO_REFRESH_DELAY_MS;
            }
            if (nextRefreshDelayMs <= 0L) {
                nextRefreshDelayMs = isAutoBuyWorkTest()
                        ? pickHolyWorldTestRefreshDelayMs()
                        : pickHolyWorldRefreshDelayMs();
            }
            return (int) nextRefreshDelayMs;
        }
        if (isSpookyTimeMode()) {
            return SPOOKYTIME_REFRESH_DELAY_MS;
        }
        return SPOOKYTIME_REFRESH_DELAY_MS;
    }

    private long pickAuctionScanDelayMs() {
        if (isSafeModeEnabled()) {
            long delay = weightedBetween(55L, 220L, 1.06D);
            if (ThreadLocalRandom.current().nextDouble() < 0.22D) {
                delay += randomBetween(110L, 380L);
            }
            if (ThreadLocalRandom.current().nextDouble() < 0.08D) {
                delay += randomBetween(320L, 960L);
            }
            return delay;
        }
        // Максимально быстрый скан: без случайных задержек — скан почти каждый тик.
        // Чтобы вернуть человекоподобные паузы, верни случайные прибавки и подними AUCTION_SCAN_*_DELAY_MS.
        return weightedBetween(AUCTION_SCAN_MIN_DELAY_MS, AUCTION_SCAN_MAX_DELAY_MS, 1.3D);
    }

    private long pickHolyWorldRefreshDelayMs() {
        if (isSafeModeEnabled()) {
            long delay = weightedBetween(HOLYWORLD_REFRESH_SAFE_MIN_DELAY_MS, HOLYWORLD_REFRESH_SAFE_MAX_DELAY_MS, 1.08D);
            if (ThreadLocalRandom.current().nextDouble() < 0.30D) {
                delay += randomBetween(450L, 1_400L);
            }
            if (ThreadLocalRandom.current().nextDouble() < 0.16D) {
                delay += randomBetween(2_000L, 6_500L);
            }
            if (ThreadLocalRandom.current().nextDouble() < 0.05D) {
                delay += randomBetween(8_000L, 18_000L);
            }
            return delay;
        }
        // Быстрое обновление без случайных всплесков: держим ~100 мс.
        // Хочешь человекоподобные паузы — верни случайные прибавки и подними HOLYWORLD_REFRESH_*_DELAY_MS.
        return weightedBetween(HOLYWORLD_REFRESH_MIN_DELAY_MS, HOLYWORLD_REFRESH_MAX_DELAY_MS, 1.10D);
    }

    private void trackNormalRefreshPattern(long now) {
        if (isSafeModeEnabled()) {
            return;
        }
        if (now < holyWorldNormalRefreshPauseUntilMs) {
            return;
        }

        if (holyWorldNormalRefreshBurstTarget <= 0) {
            holyWorldNormalRefreshBurstTarget = ThreadLocalRandom.current().nextInt(
                    HOLYWORLD_NORMAL_REFRESH_BURST_MIN,
                    HOLYWORLD_NORMAL_REFRESH_BURST_MAX + 1
            );
        }

        holyWorldNormalRefreshBurstClicks++;
        if (holyWorldNormalRefreshBurstClicks < holyWorldNormalRefreshBurstTarget) {
            return;
        }

        long pauseMs = weightedBetween(HOLYWORLD_NORMAL_REFRESH_PAUSE_MIN_MS, HOLYWORLD_NORMAL_REFRESH_PAUSE_MAX_MS, 1.02D);
        if (ThreadLocalRandom.current().nextDouble() < 0.14D) {
            pauseMs += randomBetween(500L, 1_600L);
        }
        holyWorldNormalRefreshPauseUntilMs = now + pauseMs;
        holyWorldNormalRefreshBurstClicks = 0;
        holyWorldNormalRefreshBurstTarget = ThreadLocalRandom.current().nextInt(
                HOLYWORLD_NORMAL_REFRESH_BURST_MIN,
                HOLYWORLD_NORMAL_REFRESH_BURST_MAX + 1
        );
        logDebugAb("REFRESH_MICRO_PAUSE",
                "pause_ms=" + pauseMs
                        + ", next_target=" + holyWorldNormalRefreshBurstTarget);
    }

    private void trackSafeRefreshPattern(long now) {
        if (!isSafeModeEnabled()) {
            return;
        }
        if (now < holyWorldSafeRefreshPauseUntilMs) {
            return;
        }

        if (holyWorldSafeRefreshBurstTarget <= 0) {
            holyWorldSafeRefreshBurstTarget = ThreadLocalRandom.current().nextInt(
                    HOLYWORLD_SAFE_REFRESH_BURST_MIN,
                    HOLYWORLD_SAFE_REFRESH_BURST_MAX + 1
            );
        }

        holyWorldSafeRefreshBurstClicks++;
        if (holyWorldSafeRefreshBurstClicks < holyWorldSafeRefreshBurstTarget) {
            return;
        }

        long pauseMs = weightedBetween(HOLYWORLD_SAFE_REFRESH_PAUSE_MIN_MS, HOLYWORLD_SAFE_REFRESH_PAUSE_MAX_MS, 1.12D);
        holyWorldSafeRefreshPauseUntilMs = now + pauseMs;
        holyWorldSafeRefreshBurstClicks = 0;
        holyWorldSafeRefreshBurstTarget = ThreadLocalRandom.current().nextInt(
                HOLYWORLD_SAFE_REFRESH_BURST_MIN,
                HOLYWORLD_SAFE_REFRESH_BURST_MAX + 1
        );
        logDebugAb("SAFE_REFRESH_PAUSE",
                "pause_ms=" + pauseMs
                        + ", next_target=" + holyWorldSafeRefreshBurstTarget);
    }

    private long getSafeRefreshPauseLeftMs(long now) {
        if (!isSafeModeEnabled()) {
            return 0L;
        }
        if (holyWorldSafeRefreshPauseUntilMs <= now) {
            return 0L;
        }
        return holyWorldSafeRefreshPauseUntilMs - now;
    }

    private long getNormalRefreshPauseLeftMs(long now) {
        if (isSafeModeEnabled()) {
            return 0L;
        }
        if (holyWorldNormalRefreshPauseUntilMs <= now) {
            return 0L;
        }
        return holyWorldNormalRefreshPauseUntilMs - now;
    }

    private long getRefreshPauseLeftMs(long now) {
        long safeLeft = getSafeRefreshPauseLeftMs(now);
        if (safeLeft > 0L) {
            return safeLeft;
        }
        return getNormalRefreshPauseLeftMs(now);
    }

    private boolean isHolyWorldPurchaseConfirmScreen(GenericContainerScreen screen) {
        if (!isHolyWorldMode() || screen == null) {
            return false;
        }

        String title = AutoBuyItem.normalizeLine(screen.getTitle().getString());
        List<Slot> slots = screen.getScreenHandler().slots;
        return title.contains("\u043f\u043e\u043a\u0443\u043f\u043a\u0430 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430")
                || (slots.size() >= HOLYWORLD_CONFIRM_TOP_SIZE
                && findHolyWorldConfirmButtonSlot(slots, Items.LIME_STAINED_GLASS_PANE, HOLYWORLD_CONFIRM_ACCEPT_PREFERRED_SLOT) != null
                && findHolyWorldConfirmButtonSlot(slots, Items.RED_STAINED_GLASS_PANE, HOLYWORLD_CONFIRM_DECLINE_PREFERRED_SLOT) != null);
    }

    private void handleHolyWorldPurchaseConfirm(GenericContainerScreen screen) {
        if (!autoBuyScript.isFinished()) {
            return;
        }

        List<Slot> slots = screen.getScreenHandler().slots;
        Slot centerSlot = slots.size() > HOLYWORLD_CONFIRM_CENTER_SLOT ? slots.get(HOLYWORLD_CONFIRM_CENTER_SLOT) : null;
        ItemStack centerStack = centerSlot != null ? centerSlot.getStack() : ItemStack.EMPTY;
        boolean matches = holyWorldPendingConfirmationItem != null
                && centerStack != null
                && !centerStack.isEmpty()
                && holyWorldPendingConfirmationItem.matches(centerStack, List.of());

        Slot actionSlot = findHolyWorldConfirmButtonSlot(
                slots,
                matches ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE,
                matches ? HOLYWORLD_CONFIRM_ACCEPT_PREFERRED_SLOT : HOLYWORLD_CONFIRM_DECLINE_PREFERRED_SLOT
        );
        if (actionSlot == null) {
            resolvePendingHistoryEntry(false, ItemStack.EMPTY);
            clearHolyWorldPendingConfirmation();
            return;
        }

        int syncId = screen.getScreenHandler().syncId;
        ItemStack confirmStack = centerStack.copy();
        int confirmDelayMs = (int) randomBetween(28L, 240L);
        int postConfirmDelayMs = (int) randomBetween(40L, 210L);
        autoBuyScript.cleanup()
                .addStep(confirmDelayMs, () -> {
                    boolean clicked = clickSlotWithDebug(syncId, actionSlot.id, 0, SlotActionType.PICKUP, "purchase_confirm");
                    boolean purchased = matches && clicked;
                    if (purchased) {
                        buyClicks++;
                        sendPurchasedItemToTelegram(confirmStack, holyWorldPendingUnitPrice);
                    }
                    resolvePendingHistoryEntry(purchased, purchased ? confirmStack : ItemStack.EMPTY);
                    clearHolyWorldPendingConfirmation();
                })
                .addStep(postConfirmDelayMs, () -> {});
    }

    private Slot findHolyWorldConfirmButtonSlot(List<Slot> slots, Item paneItem, int preferredIndex) {
        if (slots == null || paneItem == null || slots.isEmpty()) {
            return null;
        }

        if (preferredIndex >= 0 && preferredIndex < slots.size()) {
            Slot preferred = slots.get(preferredIndex);
            if (preferred != null && preferred.hasStack() && preferred.getStack().getItem() == paneItem) {
                return preferred;
            }
        }

        int endIndex = Math.min(HOLYWORLD_CONFIRM_TOP_SIZE, slots.size());
        for (int i = 0; i < endIndex; i++) {
            Slot slot = slots.get(i);
            if (slot != null && slot.hasStack() && slot.getStack().getItem() == paneItem) {
                return slot;
            }
        }
        return null;
    }

    private void expireHolyWorldPendingConfirmation() {
        if (holyWorldPendingConfirmationDeadlineMs != 0L
                && System.currentTimeMillis() > holyWorldPendingConfirmationDeadlineMs) {
            resolvePendingHistoryEntry(false, ItemStack.EMPTY);
            clearHolyWorldPendingConfirmation();
        }
    }

    private void clearHolyWorldPendingConfirmation() {
        holyWorldPendingConfirmationItem = null;
        holyWorldPendingUnitPrice = -1;
        holyWorldPendingConfirmationDeadlineMs = 0L;
        holyWorldPendingHistoryEntry = null;
    }

    private void tickHolyWorldAutoSell() {
        if (!autoBuyEnabled || !isHolyWorldMode()) {
            if (holyWorldAutoSellState != HolyWorldAutoSellState.IDLE) {
                resetHolyWorldAutoSellState();
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (isHolyWorldStaffEscapeBlocking()) {
            if (holyWorldAutoSellState != HolyWorldAutoSellState.IDLE) {
                resetHolyWorldAutoSellState();
            }
            return;
        }
        if (isHolyWorldPeriodicBreakActive()) {
            if (holyWorldAutoSellState != HolyWorldAutoSellState.IDLE) {
                resetHolyWorldAutoSellState();
            }
            return;
        }

        switch (holyWorldAutoSellState) {
            case OPEN_STORAGE -> tickHolyWorldOpenStorage(now);
            case LOOT_STORAGE -> tickHolyWorldLootStorage(now);
            case CLOSE_STORAGE -> tickHolyWorldCloseStorage(now);
            case REQUEST_PRICE -> tickHolyWorldAutoSellRequest(now);
            case WAIT_PRICE -> tickHolyWorldAutoSellWaitPrice(now);
            case CONFIRM_PRICE -> tickHolyWorldAutoSellConfirm(now);
            case WAIT_CONFIRM -> tickHolyWorldAutoSellWaitConfirm(now);
            case DONE -> {
                if (holyWorldWalkState == HolyWorldWalkState.PREPARE_STORAGE) {
                    holyWorldAutoSellState = HolyWorldAutoSellState.IDLE;
                }
            }
            case IDLE -> {
            }
        }
    }

    private void tickHolyWorldTimedSell() {
        if (!autoBuyEnabled || !isHolyWorldMode() || !isTimedAutoSellEnabled()) {
            if (holyWorldTimedSellState != HolyWorldTimedSellState.IDLE || holyWorldTimedSellNextRunMs != 0L) {
                resetHolyWorldTimedSellState();
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (isHolyWorldStaffEscapeBlocking()) {
            if (holyWorldTimedSellState != HolyWorldTimedSellState.IDLE) {
                resetHolyWorldTimedSellState();
            }
            return;
        }
        if (isHolyWorldPeriodicBreakActive()) {
            if (holyWorldTimedSellState != HolyWorldTimedSellState.IDLE) {
                resetHolyWorldTimedSellState();
            }
            return;
        }

        int currentAnarchy = getCurrentHolyWorldAnarchyFromScoreboard();

        switch (holyWorldTimedSellState) {
            case IDLE -> {
                if (holyWorldWalkState != HolyWorldWalkState.IDLE || isHolyWorldAutoSellActive()) {
                    return;
                }

                long intervalMs = pickTimedAutoSellIntervalMs();
                if (holyWorldTimedSellNextRunMs <= 0L) {
                    holyWorldTimedSellNextRunMs = now + intervalMs;
                    getPlannedTimedSellTargetAnarchy(now, currentAnarchy);
                    return;
                }
                if (now < holyWorldTimedSellNextRunMs) {
                    getPlannedTimedSellTargetAnarchy(now, currentAnarchy);
                    return;
                }

                int targetAnarchy = getPlannedTimedSellTargetAnarchy(now, currentAnarchy);
                if (targetAnarchy <= 0 || currentAnarchy <= 0) {
                    holyWorldTimedSellNextRunMs = now + pickTimedAutoSellIntervalMs();
                    holyWorldTimedSellPlannedAnarchy = -1;
                    return;
                }

                holyWorldTimedSellTargetAnarchy = targetAnarchy;
                holyWorldTimedSellPlannedAnarchy = -1;
                holyWorldTimedSellReturnAnarchy = currentAnarchy;
                holyWorldTimedSellStateStartedMs = now;
                holyWorldTimedSellLastActionMs = now;
                holyWorldTimedSellTransferAttempts = 0;
                holyWorldTimedSellSellStageStarted = false;
                holyWorldNeedAuctionReopen = true;
                holyWorldAuctionReopenWatch.reset();

                if (currentAnarchy != targetAnarchy) {
                    beginHolyWorldRctSequence(targetAnarchy, now, "timed_sell_to_target");
                    holyWorldTimedSellTransferAttempts = 1;
                    holyWorldTimedSellState = HolyWorldTimedSellState.TRANSFER_TO_SELL_LIGHT;
                } else {
                    holyWorldTimedSellState = HolyWorldTimedSellState.WAIT_AUCTION_ON_SELL_LIGHT;
                }
            }
            case TRANSFER_TO_SELL_LIGHT -> tickTimedSellTransferToSellLight(now, currentAnarchy);
            case WAIT_AUCTION_ON_SELL_LIGHT -> tickTimedSellWaitAuctionOnSellLight(now, currentAnarchy);
            case SELLING -> tickTimedSellSelling(now);
            case TRANSFER_BACK -> tickTimedSellTransferBack(now, currentAnarchy);
        }
    }

    private void tickTimedSellTransferToSellLight(long now, int currentAnarchy) {
        if (currentAnarchy > 0 && currentAnarchy == holyWorldTimedSellTargetAnarchy) {
            holyWorldTimedSellState = HolyWorldTimedSellState.WAIT_AUCTION_ON_SELL_LIGHT;
            holyWorldTimedSellStateStartedMs = now;
            holyWorldTimedSellLastActionMs = now;
            return;
        }

        if (now - holyWorldTimedSellStateStartedMs > delayFromActionStamp(
                holyWorldTimedSellStateStartedMs,
                HOLYWORLD_TIMED_SELL_TRANSFER_TIMEOUT_MIN_MS,
                HOLYWORLD_TIMED_SELL_TRANSFER_TIMEOUT_MAX_MS
        )) {
            finishTimedSellCycle(now);
            return;
        }

        holyWorldTimedSellTransferAttempts = Math.max(holyWorldTimedSellTransferAttempts, holyWorldPendingRctAttempts);
    }

    private void tickTimedSellWaitAuctionOnSellLight(long now, int currentAnarchy) {
        if (holyWorldTimedSellTargetAnarchy > 0
                && currentAnarchy > 0
                && currentAnarchy != holyWorldTimedSellTargetAnarchy) {
            holyWorldTimedSellState = HolyWorldTimedSellState.TRANSFER_TO_SELL_LIGHT;
            holyWorldTimedSellStateStartedMs = now;
            return;
        }

        if (mc.currentScreen instanceof GenericContainerScreen screen && isAuctionScreen(screen)) {
            holyWorldAutoSellPauseWalk = true;
            holyWorldWalkStartQueued = false;
            if (holyWorldAutoSellState == HolyWorldAutoSellState.IDLE || holyWorldAutoSellState == HolyWorldAutoSellState.DONE) {
                startHolyWorldAutoSellStorageFlow();
            }
            holyWorldTimedSellSellStageStarted = false;
            holyWorldTimedSellState = HolyWorldTimedSellState.SELLING;
            holyWorldTimedSellStateStartedMs = now;
            return;
        }

        if ((mc.currentScreen == null || mc.currentScreen instanceof ChatScreen)
                && !isTypingChatNow()
                && mc.player != null
                && mc.player.networkHandler != null
                && now - holyWorldTimedSellLastActionMs >= delayFromActionStamp(
                holyWorldTimedSellLastActionMs,
                HOLYWORLD_TIMED_SELL_AH_OPEN_RETRY_MIN_MS,
                HOLYWORLD_TIMED_SELL_AH_OPEN_RETRY_MAX_MS
        )) {
            if (mc.currentScreen instanceof ChatScreen) {
                mc.currentScreen.close();
            }
            queueVisibleChatCommand("/ah");
            holyWorldTimedSellLastActionMs = now;
        }

        if (now - holyWorldTimedSellStateStartedMs > delayFromActionStamp(
                holyWorldTimedSellStateStartedMs,
                HOLYWORLD_TIMED_SELL_TRANSFER_TIMEOUT_MIN_MS,
                HOLYWORLD_TIMED_SELL_TRANSFER_TIMEOUT_MAX_MS
        )) {
            finishTimedSellCycle(now);
        }
    }

    private void tickTimedSellSelling(long now) {
        if (!holyWorldTimedSellSellStageStarted) {
            boolean storageFinished = holyWorldAutoSellState == HolyWorldAutoSellState.DONE
                    || (holyWorldAutoSellState == HolyWorldAutoSellState.IDLE && !isHolyWorldAutoSellActive());
            if (!storageFinished) {
                return;
            }
            startHolyWorldAutoSellSellingFlow();
            holyWorldTimedSellSellStageStarted = true;
            holyWorldTimedSellStateStartedMs = now;
            holyWorldTimedSellLastActionMs = now;
            return;
        }

        boolean done = holyWorldAutoSellState == HolyWorldAutoSellState.DONE
                || (holyWorldAutoSellState == HolyWorldAutoSellState.IDLE && !isHolyWorldAutoSellActive());
        if (!done) {
            return;
        }

        holyWorldAutoSellPauseWalk = false;
        if (holyWorldTimedSellReturnAnarchy > 0
                && holyWorldTimedSellReturnAnarchy != holyWorldTimedSellTargetAnarchy) {
            beginHolyWorldRctSequence(holyWorldTimedSellReturnAnarchy, now, "timed_sell_return");
            holyWorldTimedSellTransferAttempts = 1;
            holyWorldTimedSellState = HolyWorldTimedSellState.TRANSFER_BACK;
            holyWorldTimedSellStateStartedMs = now;
            holyWorldTimedSellLastActionMs = now;
            holyWorldNeedAuctionReopen = true;
            holyWorldAuctionReopenWatch.reset();
            return;
        }

        finishTimedSellCycle(now);
    }

    private void tickTimedSellTransferBack(long now, int currentAnarchy) {
        if (currentAnarchy > 0 && currentAnarchy == holyWorldTimedSellReturnAnarchy) {
            finishTimedSellCycle(now);
            return;
        }

        if (now - holyWorldTimedSellStateStartedMs > delayFromActionStamp(
                holyWorldTimedSellStateStartedMs,
                HOLYWORLD_TIMED_SELL_TRANSFER_TIMEOUT_MIN_MS,
                HOLYWORLD_TIMED_SELL_TRANSFER_TIMEOUT_MAX_MS
        )) {
            finishTimedSellCycle(now);
            return;
        }

        holyWorldTimedSellTransferAttempts = Math.max(holyWorldTimedSellTransferAttempts, holyWorldPendingRctAttempts);
    }

    private void finishTimedSellCycle(long now) {
        long intervalMs = pickTimedAutoSellIntervalMs();
        holyWorldTimedSellState = HolyWorldTimedSellState.IDLE;
        holyWorldTimedSellStateStartedMs = 0L;
        holyWorldTimedSellLastActionMs = 0L;
        holyWorldTimedSellTransferAttempts = 0;
        holyWorldTimedSellTargetAnarchy = -1;
        holyWorldTimedSellReturnAnarchy = -1;
        holyWorldTimedSellPlannedAnarchy = -1;
        holyWorldTimedSellSellStageStarted = false;
        holyWorldTimedSellNextRunMs = now + intervalMs;
        holyWorldNeedAuctionReopen = true;
        holyWorldAuctionReopenWatch.reset();
    }

    private void resetHolyWorldTimedSellState() {
        holyWorldTimedSellState = HolyWorldTimedSellState.IDLE;
        holyWorldTimedSellNextRunMs = 0L;
        holyWorldTimedSellStateStartedMs = 0L;
        holyWorldTimedSellLastActionMs = 0L;
        holyWorldTimedSellTargetAnarchy = -1;
        holyWorldTimedSellReturnAnarchy = -1;
        holyWorldTimedSellPlannedAnarchy = -1;
        holyWorldTimedSellTransferAttempts = 0;
        holyWorldTimedSellSellStageStarted = false;
    }

    private boolean isHolyWorldPeriodicBreakActive() {
        return holyWorldPeriodicBreakState != HolyWorldPeriodicBreakState.IDLE;
    }

    private void tickHolyWorldPeriodicBreak() {
        if (!autoBuyEnabled || !isHolyWorldMode()) {
            resetHolyWorldPeriodicBreakState();
            return;
        }
        if (isSafeModeEnabled()) {
            if (holyWorldPeriodicBreakState != HolyWorldPeriodicBreakState.IDLE) {
                resetHolyWorldPeriodicBreakState();
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (holyWorldNextPeriodicBreakAtMs <= 0L) {
            holyWorldNextPeriodicBreakAtMs = now + pickHolyWorldPeriodicBreakIntervalMs();
        }

        switch (holyWorldPeriodicBreakState) {
            case IDLE -> {
                if (now < holyWorldNextPeriodicBreakAtMs) {
                    return;
                }
                if (holyWorldWalkState != HolyWorldWalkState.IDLE
                        || isHolyWorldAutoSellActive()
                        || holyWorldTimedSellState != HolyWorldTimedSellState.IDLE
                        || holyWorldPendingRctAnarchy > 0
                        || isTypingChatNow()) {
                    return;
                }
                startHolyWorldPeriodicBreak(now);
            }
            case WALK -> tickHolyWorldPeriodicBreakWalk(now);
            case HUB_WAIT -> tickHolyWorldPeriodicBreakHubWait(now);
            case REJOIN_WAIT -> tickHolyWorldPeriodicBreakRejoin(now);
        }
    }

    private void startHolyWorldPeriodicBreak(long now) {
        holyWorldPeriodicBreakState = HolyWorldPeriodicBreakState.WALK;
        holyWorldPeriodicBreakStateStartedMs = now;
        holyWorldPeriodicBreakWalkEndMs = now + pickHolyWorldPeriodicBreakPauseMs();
        holyWorldPeriodicBreakHubEndMs = 0L;
        holyWorldPeriodicBreakLastHubCommandMs = 0L;
        holyWorldPeriodicBreakHoldUntilMs = 0L;
        holyWorldPeriodicBreakTargetIndex = 0;
        holyWorldPeriodicBreakTargetAnarchy = -1;
        holyWorldRoutePointOneTarget = randomizeHolyWorldRoutePoint(HOLYWORLD_ROUTE_POINT_ONE, 2.6D, 2.1D, 0.35D);
        holyWorldRoutePointTwoTarget = randomizeHolyWorldRoutePoint(HOLYWORLD_ROUTE_POINT_TWO, 3.0D, 2.5D, 0.35D);
        holyWorldNeedAuctionReopen = false;
        clearHolyWorldPendingRctSequence();
        releaseHolyWorldMovementKeys();
        stopHolyWorldRotation();

        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen && isTypingChatNow())) {
            mc.currentScreen.close();
        }
    }

    private void tickHolyWorldPeriodicBreakWalk(long now) {
        if (mc.player == null) {
            return;
        }

        if (now >= holyWorldPeriodicBreakWalkEndMs) {
            releaseHolyWorldMovementKeys();
            holyWorldPeriodicBreakState = HolyWorldPeriodicBreakState.HUB_WAIT;
            holyWorldPeriodicBreakStateStartedMs = now;
            holyWorldPeriodicBreakHubEndMs = now + randomBetween(HOLYWORLD_PERIODIC_BREAK_HUB_MIN_MS, HOLYWORLD_PERIODIC_BREAK_HUB_MAX_MS);
            queueHolyWorldPeriodicHubCommand(now);
            return;
        }

        Vec3d target = holyWorldPeriodicBreakTargetIndex == 0
                ? resolveHolyWorldRoutePointOne()
                : resolveHolyWorldRoutePointTwo();
        if (now < holyWorldPeriodicBreakHoldUntilMs) {
            inspectHolyWorldRoutePoint(target, now);
            return;
        }

        if (moveToHolyWorldRouteTarget(target, now, true, 1.35D)) {
            inspectHolyWorldRoutePoint(target, now);
            holyWorldPeriodicBreakHoldUntilMs = now + randomBetween(1_400L, 3_000L);
            holyWorldPeriodicBreakTargetIndex = holyWorldPeriodicBreakTargetIndex == 0 ? 1 : 0;
        }
    }

    private void tickHolyWorldPeriodicBreakHubWait(long now) {
        queueHolyWorldPeriodicHubCommand(now);
        if (now < holyWorldPeriodicBreakHubEndMs || isTypingChatNow()) {
            return;
        }

        int currentAnarchy = getCurrentHolyWorldAnarchyFromScoreboard();
        int nextAnarchy = chooseNextHolyWorldAnarchyAvoiding(now, currentAnarchy > 0 ? currentAnarchy : -1);
        if (nextAnarchy <= 0) {
            finishHolyWorldPeriodicBreak(now);
            return;
        }
        holyWorldPeriodicBreakTargetAnarchy = nextAnarchy;
        beginHolyWorldRctSequence(nextAnarchy, now, "periodic_break_rejoin");
        holyWorldPeriodicBreakState = HolyWorldPeriodicBreakState.REJOIN_WAIT;
        holyWorldPeriodicBreakStateStartedMs = now;
    }

    private void tickHolyWorldPeriodicBreakRejoin(long now) {
        int currentAnarchy = getCurrentHolyWorldAnarchyFromScoreboard();
        if ((holyWorldPeriodicBreakTargetAnarchy > 0 && currentAnarchy == holyWorldPeriodicBreakTargetAnarchy)
                || (holyWorldPendingRctAnarchy <= 0 && currentAnarchy > 0)) {
            finishHolyWorldPeriodicBreak(now);
            return;
        }

        if (now - holyWorldPeriodicBreakStateStartedMs > HOLYWORLD_PERIODIC_BREAK_REJOIN_TIMEOUT_MS) {
            finishHolyWorldPeriodicBreak(now);
        }
    }

    private void queueHolyWorldPeriodicHubCommand(long now) {
        if (isTypingChatNow()) {
            return;
        }
        if (holyWorldPeriodicBreakLastHubCommandMs == 0L
                || now - holyWorldPeriodicBreakLastHubCommandMs >= delayFromActionStamp(
                holyWorldPeriodicBreakLastHubCommandMs,
                HOLYWORLD_PERIODIC_BREAK_HUB_RETRY_MIN_MS,
                HOLYWORLD_PERIODIC_BREAK_HUB_RETRY_MAX_MS
        )) {
            queueVisibleChatCommand("/hub");
            holyWorldPeriodicBreakLastHubCommandMs = now;
        }
    }

    private void finishHolyWorldPeriodicBreak(long now) {
        holyWorldPeriodicBreakState = HolyWorldPeriodicBreakState.IDLE;
        holyWorldPeriodicBreakStateStartedMs = 0L;
        holyWorldPeriodicBreakWalkEndMs = 0L;
        holyWorldPeriodicBreakHubEndMs = 0L;
        holyWorldPeriodicBreakLastHubCommandMs = 0L;
        holyWorldPeriodicBreakHoldUntilMs = 0L;
        holyWorldPeriodicBreakTargetIndex = 0;
        holyWorldPeriodicBreakTargetAnarchy = -1;
        holyWorldNextPeriodicBreakAtMs = now + pickHolyWorldPeriodicBreakIntervalMs();
        holyWorldNeedAuctionReopen = true;
        holyWorldAuctionReopenWatch.reset();
        releaseHolyWorldMovementKeys();
    }

    private void resetHolyWorldPeriodicBreakState() {
        boolean hadActiveBreak = holyWorldPeriodicBreakState != HolyWorldPeriodicBreakState.IDLE;
        holyWorldPeriodicBreakState = HolyWorldPeriodicBreakState.IDLE;
        holyWorldNextPeriodicBreakAtMs = 0L;
        holyWorldPeriodicBreakWalkEndMs = 0L;
        holyWorldPeriodicBreakHubEndMs = 0L;
        holyWorldPeriodicBreakStateStartedMs = 0L;
        holyWorldPeriodicBreakLastHubCommandMs = 0L;
        holyWorldPeriodicBreakHoldUntilMs = 0L;
        holyWorldPeriodicBreakTargetIndex = 0;
        holyWorldPeriodicBreakTargetAnarchy = -1;
        if (hadActiveBreak) {
            releaseHolyWorldMovementKeys();
            stopHolyWorldRotation();
        }
    }

    private void startHolyWorldAutoSellStorageFlow() {
        holyWorldAutoSellState = HolyWorldAutoSellState.OPEN_STORAGE;
        holyWorldAutoSellStateStartedMs = System.currentTimeMillis();
        holyWorldAutoSellLastActionMs = 0L;
        holyWorldAutoSellOfferReceived = false;
        holyWorldAutoSellConfirmReceived = false;
        holyWorldAutoSellNoItems = false;
        holyWorldStorageSlotMisses = 0;
    }

    private void startHolyWorldAutoSellSellingFlow() {
        long now = System.currentTimeMillis();
        if (now < holyWorldAutoSellNoFilterBackoffUntilMs) {
            logDebugAb("AUTOSELL_GUARD_BACKOFF",
                    "left_ms=" + (holyWorldAutoSellNoFilterBackoffUntilMs - now)
                            + ", safe=" + isSafeModeEnabled());
            holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
            holyWorldAutoSellStateStartedMs = now;
            return;
        }
        holyWorldAutoSellState = HolyWorldAutoSellState.REQUEST_PRICE;
        holyWorldAutoSellStateStartedMs = now;
        holyWorldAutoSellLastActionMs = 0L;
        holyWorldStorageSlotMisses = 0;
        holyWorldAutoSellAttempts = 0;
        holyWorldAutoSellConfirmAttempts = 0;
        holyWorldAutoSellNoFilterStreak = 0;
        holyWorldAutoSellNoFilterTotal = 0;
        holyWorldAutoSellOfferReceived = false;
        holyWorldAutoSellConfirmReceived = false;
        holyWorldAutoSellNoItems = false;
        holyWorldAutoSellNeedNextItem = false;
        holyWorldAutoSellAuctionSlotsFull = false;
        holyWorldAutoSellOfferedCount = 1;
        holyWorldAutoSellOfferedPrice = -1L;
        holyWorldAutoSellRequiredPrice = -1L;
        holyWorldAutoSellOfferedItem = "";
        holyWorldAutoSellCurrentInventoryIndex = -1;
        holyWorldAutoSellSkippedNamesThisCycle.clear();
        holyWorldAutoSellScanStartIndex = mc.player != null ? ThreadLocalRandom.current().nextInt(36) : 0;
    }

    private void tickHolyWorldOpenStorage(long now) {
        if (now - holyWorldAutoSellStateStartedMs > delayFromActionStamp(
                holyWorldAutoSellStateStartedMs,
                HOLYWORLD_STORAGE_PREPARE_TIMEOUT_MIN_MS,
                HOLYWORLD_STORAGE_PREPARE_TIMEOUT_MAX_MS
        )) {
            holyWorldAutoSellState = HolyWorldAutoSellState.CLOSE_STORAGE;
            holyWorldAutoSellStateStartedMs = now;
            return;
        }

        if (!(mc.currentScreen instanceof GenericContainerScreen screen) || !isAuctionScreen(screen)) {
            return;
        }

        if (now - holyWorldAutoSellLastActionMs < delayFromActionStamp(
                holyWorldAutoSellLastActionMs,
                HOLYWORLD_STORAGE_ACTION_DELAY_MIN_MS,
                HOLYWORLD_STORAGE_ACTION_DELAY_MAX_MS
        )) {
            return;
        }

        Slot storageSlot = findStorageSlotInAuction(screen);

        if (storageSlot == null || !storageSlot.hasStack()) {
            holyWorldStorageSlotMisses++;
            logDebugAb("STORAGE_SLOT_MISS",
                    "stage=open_storage, misses=" + holyWorldStorageSlotMisses + ", title=" + getCurrentScreenTitleSafe());
            if (holyWorldStorageSlotMisses < HOLYWORLD_STORAGE_SLOT_MISS_BEFORE_CLOSE) {
                holyWorldAutoSellLastActionMs = now;
                return;
            }
            holyWorldStorageSlotMisses = 0;
            holyWorldAutoSellState = HolyWorldAutoSellState.CLOSE_STORAGE;
            holyWorldAutoSellStateStartedMs = now;
            return;
        }
        holyWorldStorageSlotMisses = 0;

        if (clickSlotWithDebug(screen.getScreenHandler().syncId, storageSlot.id, 0, SlotActionType.PICKUP, "storage_open")) {
            holyWorldAutoSellLastActionMs = now;
            holyWorldAutoSellState = HolyWorldAutoSellState.LOOT_STORAGE;
            holyWorldAutoSellStateStartedMs = now;
        } else {
            holyWorldAutoSellLastActionMs = now;
        }
    }

    private void tickHolyWorldLootStorage(long now) {
        if (now - holyWorldAutoSellStateStartedMs > delayFromActionStamp(
                holyWorldAutoSellStateStartedMs,
                HOLYWORLD_STORAGE_PREPARE_TIMEOUT_MIN_MS,
                HOLYWORLD_STORAGE_PREPARE_TIMEOUT_MAX_MS
        )) {
            holyWorldAutoSellState = HolyWorldAutoSellState.CLOSE_STORAGE;
            holyWorldAutoSellStateStartedMs = now;
            return;
        }

        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            return;
        }

        if (isHolyWorldPurchaseConfirmScreen(screen)) {
            return;
        }

        if (isAuctionScreen(screen)) {
            if (now - holyWorldAutoSellLastActionMs < delayFromActionStamp(
                    holyWorldAutoSellLastActionMs,
                    HOLYWORLD_STORAGE_ACTION_DELAY_MIN_MS,
                    HOLYWORLD_STORAGE_ACTION_DELAY_MAX_MS
            )) {
                return;
            }
            Slot storageSlot = findStorageSlotInAuction(screen);
            if (storageSlot != null) {
                holyWorldStorageSlotMisses = 0;
                if (clickSlotWithDebug(screen.getScreenHandler().syncId, storageSlot.id, 0, SlotActionType.PICKUP, "storage_reopen")) {
                    holyWorldAutoSellLastActionMs = now;
                } else {
                    holyWorldAutoSellLastActionMs = now;
                }
                return;
            }
            holyWorldStorageSlotMisses++;
            logDebugAb("STORAGE_SLOT_MISS",
                    "stage=loot_reopen, misses=" + holyWorldStorageSlotMisses + ", title=" + getCurrentScreenTitleSafe());
            if (holyWorldStorageSlotMisses < HOLYWORLD_STORAGE_SLOT_MISS_BEFORE_CLOSE) {
                holyWorldAutoSellLastActionMs = now;
                return;
            }
            holyWorldStorageSlotMisses = 0;
            holyWorldAutoSellState = HolyWorldAutoSellState.CLOSE_STORAGE;
            holyWorldAutoSellStateStartedMs = now;
            return;
        }

        if (!isHolyWorldSaleStorageScreen(screen)) {
            holyWorldStorageSlotMisses++;
            logDebugAb("STORAGE_SCREEN_MISMATCH",
                    "misses=" + holyWorldStorageSlotMisses + ", title=" + getCurrentScreenTitleSafe());
            if (holyWorldStorageSlotMisses < HOLYWORLD_STORAGE_SLOT_MISS_BEFORE_CLOSE) {
                holyWorldAutoSellLastActionMs = now;
                return;
            }
            holyWorldStorageSlotMisses = 0;
            holyWorldAutoSellState = HolyWorldAutoSellState.CLOSE_STORAGE;
            holyWorldAutoSellStateStartedMs = now;
            return;
        }

        if (now - holyWorldAutoSellLastActionMs < delayFromActionStamp(
                holyWorldAutoSellLastActionMs,
                HOLYWORLD_STORAGE_ACTION_DELAY_MIN_MS,
                HOLYWORLD_STORAGE_ACTION_DELAY_MAX_MS
        )) {
            return;
        }

        Slot target = findStorageItemToLoot(screen);
        if (target == null) {
            holyWorldStorageSlotMisses++;
            logDebugAb("STORAGE_LOOT_EMPTY",
                    "misses=" + holyWorldStorageSlotMisses + ", title=" + getCurrentScreenTitleSafe());
            if (holyWorldStorageSlotMisses < HOLYWORLD_STORAGE_SLOT_MISS_BEFORE_CLOSE) {
                holyWorldAutoSellLastActionMs = now;
                return;
            }
            holyWorldStorageSlotMisses = 0;
            holyWorldAutoSellState = HolyWorldAutoSellState.CLOSE_STORAGE;
            holyWorldAutoSellStateStartedMs = now;
            return;
        }
        holyWorldStorageSlotMisses = 0;

        SlotActionType lootAction = shouldUsePickupStorageLoot(screen) ? SlotActionType.PICKUP : SlotActionType.QUICK_MOVE;
        if (clickSlotWithDebug(screen.getScreenHandler().syncId, target.id, 0, lootAction, "storage_loot")) {
            holyWorldAutoSellLastActionMs = now;
        } else {
            holyWorldAutoSellLastActionMs = now;
        }
    }

    private void tickHolyWorldCloseStorage(long now) {
        holyWorldStorageSlotMisses = 0;
        if (mc.currentScreen != null) {
            mc.currentScreen.close();
        }

        if (holyWorldWalkStartQueued) {
            holyWorldWalkStartQueued = false;
            startHolyWorldWalkRoutine(now);
            return;
        }

        holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
        holyWorldAutoSellStateStartedMs = now;
    }

    private void tickHolyWorldAutoSellRequest(long now) {
        if (!holyWorldAutoSellPauseWalk) {
            holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
            return;
        }
        if (isAutoSellNoFilterBackoffActive(now)) {
            logDebugAb("AUTOSELL_STOP",
                    "reason=no_filter_backoff, left_ms=" + (holyWorldAutoSellNoFilterBackoffUntilMs - now));
            holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
            return;
        }
        if (holyWorldAutoSellAttempts >= getAutoSellAttemptLimit()) {
            holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
            return;
        }
        if (mc.player == null || mc.player.networkHandler == null) {
            return;
        }
        if (isTypingChatNow()) {
            return;
        }
        if (now - holyWorldAutoSellLastActionMs < delayFromActionStamp(
                holyWorldAutoSellLastActionMs,
                HOLYWORLD_AUTOSELL_COMMAND_DELAY_MIN_MS,
                HOLYWORLD_AUTOSELL_COMMAND_DELAY_MAX_MS
        )) {
            return;
        }

        if (!prepareNextAutoSellItemInMainHand()) {
            holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
            return;
        }

        holyWorldAutoSellAttempts++;
        holyWorldAutoSellOfferReceived = false;
        holyWorldAutoSellNoItems = false;
        holyWorldAutoSellNeedNextItem = false;
        holyWorldAutoSellAuctionSlotsFull = false;
        holyWorldAutoSellOfferedPrice = -1L;
        queueVisibleChatCommand("/ah sell auto", shouldUseFastAutoSellTyping());
        holyWorldAutoSellLastActionMs = now;
        holyWorldAutoSellState = HolyWorldAutoSellState.WAIT_PRICE;
        holyWorldAutoSellStateStartedMs = now;
    }

    private void tickHolyWorldAutoSellWaitPrice(long now) {
        if (isAutoSellNoFilterBackoffActive(now)) {
            logDebugAb("AUTOSELL_STOP",
                    "reason=no_filter_backoff_wait_price, left_ms=" + (holyWorldAutoSellNoFilterBackoffUntilMs - now));
            holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
            return;
        }
        if (holyWorldAutoSellOfferReceived) {
            if (holyWorldAutoSellAuctionSlotsFull) {
                holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
                return;
            }
            if (holyWorldAutoSellNeedNextItem) {
                if (isAutoSellNoFilterExhausted()) {
                    logDebugAb("AUTOSELL_STOP",
                            "reason=no_filter_limit_wait_price, streak=" + holyWorldAutoSellNoFilterStreak
                                    + ", total=" + holyWorldAutoSellNoFilterTotal);
                    holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
                    return;
                }
                holyWorldAutoSellState = HolyWorldAutoSellState.REQUEST_PRICE;
                return;
            }
            if (holyWorldAutoSellNoItems) {
                holyWorldAutoSellState = HolyWorldAutoSellState.REQUEST_PRICE;
                return;
            }

            if (holyWorldAutoSellOfferedPrice > 0L) {
                long required = holyWorldAutoSellRequiredPrice;
                if (required < 0L) {
                    required = 0L;
                }
                if (holyWorldAutoSellOfferedPrice >= required) {
                    resetAutoSellNoFilterStreak();
                    holyWorldAutoSellState = HolyWorldAutoSellState.CONFIRM_PRICE;
                    holyWorldAutoSellStateStartedMs = now;
                } else {
                    holyWorldAutoSellState = HolyWorldAutoSellState.REQUEST_PRICE;
                }
                return;
            }
        }

        if (now - holyWorldAutoSellStateStartedMs > delayFromActionStamp(
                holyWorldAutoSellStateStartedMs,
                HOLYWORLD_AUTOSELL_WAIT_PRICE_TIMEOUT_MIN_MS,
                HOLYWORLD_AUTOSELL_WAIT_PRICE_TIMEOUT_MAX_MS
        )) {
            holyWorldAutoSellState = HolyWorldAutoSellState.REQUEST_PRICE;
        }
    }

    private void tickHolyWorldAutoSellConfirm(long now) {
        if (mc.player == null || mc.player.networkHandler == null) {
            return;
        }
        if (holyWorldAutoSellConfirmAttempts >= HOLYWORLD_AUTOSELL_MAX_CONFIRM_ATTEMPTS) {
            holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
            return;
        }
        if (isTypingChatNow()) {
            return;
        }
        if (now - holyWorldAutoSellLastActionMs < delayFromActionStamp(
                holyWorldAutoSellLastActionMs,
                HOLYWORLD_AUTOSELL_COMMAND_DELAY_MIN_MS,
                HOLYWORLD_AUTOSELL_COMMAND_DELAY_MAX_MS
        )) {
            return;
        }

        holyWorldAutoSellConfirmAttempts++;
        holyWorldAutoSellConfirmReceived = false;
        queueVisibleChatCommand("/ah sell auto confirm", shouldUseFastAutoSellTyping());
        holyWorldAutoSellLastActionMs = now;
        holyWorldAutoSellState = HolyWorldAutoSellState.WAIT_CONFIRM;
        holyWorldAutoSellStateStartedMs = now;
    }

    private void tickHolyWorldAutoSellWaitConfirm(long now) {
        if (isAutoSellNoFilterBackoffActive(now)) {
            logDebugAb("AUTOSELL_STOP",
                    "reason=no_filter_backoff_wait_confirm, left_ms=" + (holyWorldAutoSellNoFilterBackoffUntilMs - now));
            holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
            return;
        }
        if (holyWorldAutoSellAuctionSlotsFull) {
            holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
            return;
        }
        if (holyWorldAutoSellNeedNextItem) {
            if (isAutoSellNoFilterExhausted()) {
                logDebugAb("AUTOSELL_STOP",
                        "reason=no_filter_limit_wait_confirm, streak=" + holyWorldAutoSellNoFilterStreak
                                + ", total=" + holyWorldAutoSellNoFilterTotal);
                holyWorldAutoSellState = HolyWorldAutoSellState.DONE;
                return;
            }
            holyWorldAutoSellState = HolyWorldAutoSellState.REQUEST_PRICE;
            return;
        }
        if (holyWorldAutoSellConfirmReceived) {
            resetAutoSellNoFilterStreak();
            holyWorldAutoSellState = HolyWorldAutoSellState.REQUEST_PRICE;
            return;
        }
        if (now - holyWorldAutoSellStateStartedMs > delayFromActionStamp(
                holyWorldAutoSellStateStartedMs,
                HOLYWORLD_AUTOSELL_WAIT_CONFIRM_TIMEOUT_MIN_MS,
                HOLYWORLD_AUTOSELL_WAIT_CONFIRM_TIMEOUT_MAX_MS
        )) {
            holyWorldAutoSellState = HolyWorldAutoSellState.CONFIRM_PRICE;
        }
    }

    private int getAutoSellAttemptLimit() {
        return isSafeModeEnabled() ? HOLYWORLD_AUTOSELL_MAX_ATTEMPTS_SAFE : HOLYWORLD_AUTOSELL_MAX_ATTEMPTS;
    }

    private int getAutoSellNoFilterStreakLimit() {
        return isSafeModeEnabled() ? HOLYWORLD_AUTOSELL_NOFILTER_STREAK_SAFE_LIMIT : HOLYWORLD_AUTOSELL_NOFILTER_STREAK_LIMIT;
    }

    private int getAutoSellNoFilterTotalLimit() {
        return isSafeModeEnabled() ? HOLYWORLD_AUTOSELL_NOFILTER_TOTAL_SAFE_LIMIT : HOLYWORLD_AUTOSELL_NOFILTER_TOTAL_LIMIT;
    }

    private boolean isAutoSellNoFilterBackoffActive(long now) {
        return holyWorldAutoSellNoFilterBackoffUntilMs > now;
    }

    private boolean isAutoSellNoFilterExhausted() {
        return holyWorldAutoSellNoFilterStreak >= getAutoSellNoFilterStreakLimit()
                || holyWorldAutoSellNoFilterTotal >= getAutoSellNoFilterTotalLimit();
    }

    private void registerAutoSellNoFilter(long now) {
        holyWorldAutoSellNoFilterStreak++;
        holyWorldAutoSellNoFilterTotal++;

        if (isAutoSellNoFilterExhausted()) {
            if (holyWorldAutoSellNoFilterBackoffUntilMs <= now) {
                long waitMs = weightedBetween(
                        HOLYWORLD_AUTOSELL_NOFILTER_BACKOFF_MIN_MS,
                        HOLYWORLD_AUTOSELL_NOFILTER_BACKOFF_MAX_MS,
                        isSafeModeEnabled() ? 0.85D : 1.06D
                );
                holyWorldAutoSellNoFilterBackoffUntilMs = now + waitMs;
                logDebugAb("AUTOSELL_NOFILTER_GUARD",
                        "streak=" + holyWorldAutoSellNoFilterStreak
                                + ", total=" + holyWorldAutoSellNoFilterTotal
                                + ", streak_limit=" + getAutoSellNoFilterStreakLimit()
                                + ", total_limit=" + getAutoSellNoFilterTotalLimit()
                                + ", wait_ms=" + waitMs);
            }
        }
    }

    private void resetAutoSellNoFilterStreak() {
        holyWorldAutoSellNoFilterStreak = 0;
    }

    private boolean shouldUseFastAutoSellTyping() {
        if (isSafeModeEnabled()) {
            return false;
        }
        return isFastAutoSellItemName(holyWorldAutoSellOfferedItem);
    }

    private boolean isFastAutoSellItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return false;
        }

        String normalized = AutoBuyItem.normalizeLine(itemName)
                .toLowerCase(Locale.ROOT)
                .replace('\u0451', '\u0435');
        if (normalized.isBlank()) {
            return false;
        }

        boolean shardEgg = normalized.contains("\u043e\u0441\u043a\u043e\u043b\u043e\u0447\u043d\u043e\u0435 \u044f\u0439\u0446\u043e")
                || (normalized.contains("\u043e\u0441\u043a\u043e\u043b\u043e\u0447") && normalized.contains("\u044f\u0439\u0446"));
        boolean sphereShard = normalized.contains("\u043e\u0441\u043a\u043e\u043b\u043e\u043a \u0441\u0444\u0435\u0440\u044b")
                || (normalized.contains("\u043e\u0441\u043a\u043e\u043b\u043e\u043a") && normalized.contains("\u0441\u0444\u0435\u0440"));
        boolean gunpowder = normalized.contains("\u043f\u043e\u0440\u043e\u0445");

        return shardEgg || sphereShard || gunpowder;
    }

    private Slot findStorageItemToLoot(GenericContainerScreen screen) {
        if (screen == null) {
            return null;
        }

        int containerSlots = Math.min(screen.getScreenHandler().getInventory().size(), screen.getScreenHandler().slots.size());
        int endIndex = Math.min(containerSlots - 1, 8);
        for (int i = 0; i <= endIndex; i++) {
            Slot slot = screen.getScreenHandler().slots.get(i);
            if (slot == null || !slot.hasStack()) {
                continue;
            }
            if (slot.inventory instanceof net.minecraft.entity.player.PlayerInventory) {
                continue;
            }

            Item item = slot.getStack().getItem();
            if (item == Items.AIR
                    || item == Items.GRAY_STAINED_GLASS_PANE
                    || item == Items.BLACK_STAINED_GLASS_PANE
                    || item == Items.RED_STAINED_GLASS_PANE
                    || item == Items.LIME_STAINED_GLASS_PANE
                    || item == Items.ENDER_CHEST
                    || item == Items.COMPASS
                    || item == Items.BARRIER
                    || item == Items.EMERALD) {
                continue;
            }
            return slot;
        }
        return null;
    }

    private Slot findStorageSlotInAuction(GenericContainerScreen screen) {
        if (screen == null) {
            return null;
        }

        List<Slot> slots = screen.getScreenHandler().slots;
        Slot fromFirstRow = findStorageSlotInRange(slots, 0, 8);
        if (fromFirstRow != null) {
            return fromFirstRow;
        }
        if (slots.size() > HOLYWORLD_STORAGE_SLOT) {
            Slot byIndex = slots.get(HOLYWORLD_STORAGE_SLOT);
            if (isStorageAccessStack(byIndex == null ? ItemStack.EMPTY : byIndex.getStack())) {
                return byIndex;
            }
        }

        Slot anyTop = findStorageSlotInRange(slots, 0, 53);
        if (anyTop != null) {
            return anyTop;
        }

        return null;
    }

    private Slot findStorageSlotInRange(List<Slot> slots, int startIndex, int endIndex) {
        if (slots == null || slots.isEmpty()) {
            return null;
        }
        int from = Math.max(0, startIndex);
        int to = Math.min(endIndex, slots.size() - 1);
        for (int i = from; i <= to; i++) {
            Slot candidate = slots.get(i);
            if (isStorageAccessStack(candidate == null ? ItemStack.EMPTY : candidate.getStack())) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isStorageAccessStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() == Items.ENDER_CHEST) {
            return true;
        }
        String normalized = AutoBuyItem.normalizeLine(stack.getName().getString());
        return normalized.contains("\u0445\u0440\u0430\u043d\u0438\u043b") || normalized.contains("storage");
    }

    private boolean isHolyWorldSaleStorageScreen(GenericContainerScreen screen) {
        if (screen == null) {
            return false;
        }
        String title = AutoBuyItem.normalizeLine(screen.getTitle().getString());
        if (title.contains("товары на продаже")
                || title.contains("товары в хранилище")
                || title.contains("хранилище продаж")
                || title.contains("sale storage")
                || title.contains("storage")) {
            return true;
        }
        if (title.contains("товары") && (title.contains("продаж") || title.contains("хранил"))) {
            return true;
        }
        if (isAuctionScreen(screen) || isHolyWorldPurchaseConfirmScreen(screen)) {
            return false;
        }

        List<Slot> slots = screen.getScreenHandler().slots;
        int endIndex = Math.min(8, slots.size() - 1);
        for (int i = 0; i <= endIndex; i++) {
            Slot slot = slots.get(i);
            if (slot == null || !slot.hasStack()) {
                continue;
            }
            if (slot.inventory instanceof net.minecraft.entity.player.PlayerInventory) {
                continue;
            }
            Item item = slot.getStack().getItem();
            if (item == Items.AIR
                    || item == Items.GRAY_STAINED_GLASS_PANE
                    || item == Items.BLACK_STAINED_GLASS_PANE
                    || item == Items.RED_STAINED_GLASS_PANE
                    || item == Items.LIME_STAINED_GLASS_PANE
                    || item == Items.ENDER_CHEST
                    || item == Items.COMPASS
                    || item == Items.BARRIER
                    || item == Items.EMERALD) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean shouldUsePickupStorageLoot(GenericContainerScreen screen) {
        if (screen == null) {
            return false;
        }
        String title = AutoBuyItem.normalizeLine(screen.getTitle().getString());
        return title.contains("товары на продаже")
                || title.contains("товары в хранилище")
                || title.contains("хранилище продаж");
    }

    private boolean prepareNextAutoSellItemInMainHand() {
        if (mc.player == null || mc.interactionManager == null) {
            return false;
        }

        for (int scanAttempts = 0; scanAttempts < 36; scanAttempts++) {
            AutoSellCandidate candidate = findNextAutoSellCandidate();
            if (candidate == null) {
                return false;
            }

            holyWorldAutoSellScanStartIndex = Math.floorMod(
                    candidate.inventoryIndex() + ThreadLocalRandom.current().nextInt(1, 8),
                    36
            );
            if (!moveAutoSellCandidateToMainHand(candidate)) {
                continue;
            }

            ItemStack mainHand = mc.player.getMainHandStack();
            if (mainHand == null || mainHand.isEmpty()) {
                continue;
            }

            AutoBuyItem inHandConfigured = findConfiguredAutoSellItem(mainHand);
            if (inHandConfigured == null) {
                continue;
            }

            long requiredPrice = inHandConfigured.getPriceValue();
            if (requiredPrice <= 0L) {
                requiredPrice = candidate.requiredPrice();
            }

            holyWorldAutoSellCurrentInventoryIndex = candidate.inventoryIndex();
            holyWorldAutoSellRequiredPrice = Math.max(0L, requiredPrice);
            holyWorldAutoSellOfferedItem = mainHand.getName().getString();
            return true;
        }

        return false;
    }

    private boolean moveAutoSellCandidateToMainHand(AutoSellCandidate candidate) {
        if (candidate == null || mc.player == null || mc.interactionManager == null) {
            return false;
        }
        debugAbAutoSellMoveAttempts++;

        int inventoryIndex = candidate.inventoryIndex();
        if (inventoryIndex < 0 || inventoryIndex > 35) {
            debugAbAutoSellMoveFailures++;
            return false;
        }

        if (inventoryIndex <= 8) {
            InventoryTask.switchTo(inventoryIndex);
            boolean ok = isAutoSellCandidateInMainHand(candidate);
            if (!ok) {
                debugAbAutoSellMoveFailures++;
                logDebugAb("AUTOSELL_SWAP_FAIL",
                        "reason=hotbar_switch_failed, idx=" + inventoryIndex + ", item=" + candidate.itemName());
            } else {
                logDebugAb("AUTOSELL_SWAP_OK",
                        "mode=hotbar_switch, idx=" + inventoryIndex + ", item=" + candidate.itemName());
            }
            return ok;
        }

        int sourceSlotId = findPlayerInventorySlotId(inventoryIndex);
        if (sourceSlotId < 0) {
            debugAbAutoSellMoveFailures++;
            logDebugAb("AUTOSELL_SWAP_FAIL",
                    "reason=source_slot_not_found, idx=" + inventoryIndex + ", item=" + candidate.itemName());
            return false;
        }

        int targetHotbar = chooseAutoSellTargetHotbarSlot();
        boolean clicked = clickSlotWithDebug(
                mc.player.currentScreenHandler.syncId,
                sourceSlotId,
                targetHotbar,
                SlotActionType.SWAP,
                "autosell_swap_to_hotbar"
        );
        if (!clicked) {
            debugAbAutoSellMoveFailures++;
            logDebugAb("AUTOSELL_SWAP_FAIL",
                    "reason=swap_click_failed, idx=" + inventoryIndex + ", src_slot_id=" + sourceSlotId + ", target_hotbar=" + targetHotbar + ", item=" + candidate.itemName());
            return false;
        }
        InventoryTask.switchTo(targetHotbar);
        boolean ok = isAutoSellCandidateInMainHand(candidate);
        if (!ok) {
            ItemStack hotbarStack = mc.player.getInventory().getStack(targetHotbar);
            ok = isAutoSellStackMatchesCandidate(hotbarStack, candidate);
            if (ok) {
                InventoryTask.switchTo(targetHotbar);
            }
        }
        if (!ok) {
            debugAbAutoSellMoveFailures++;
            logDebugAb("AUTOSELL_SWAP_FAIL",
                    "reason=post_swap_verify_failed, idx=" + inventoryIndex + ", src_slot_id=" + sourceSlotId + ", target_hotbar=" + targetHotbar + ", item=" + candidate.itemName());
        } else {
            logDebugAb("AUTOSELL_SWAP_OK",
                    "mode=swap, idx=" + inventoryIndex + ", src_slot_id=" + sourceSlotId + ", target_hotbar=" + targetHotbar + ", item=" + candidate.itemName());
        }
        return ok;
    }

    private int chooseAutoSellTargetHotbarSlot() {
        if (mc.player == null) {
            return 0;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int selected = Math.max(0, Math.min(8, mc.player.getInventory().selectedSlot));
        ItemStack selectedStack = mc.player.getInventory().getStack(selected);
        if (selectedStack == null || selectedStack.isEmpty()) {
            return selected;
        }

        List<Integer> emptySlots = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack == null || stack.isEmpty()) {
                emptySlots.add(i);
            }
        }
        if (!emptySlots.isEmpty()) {
            return emptySlots.get(random.nextInt(emptySlots.size()));
        }

        if (random.nextDouble() < 0.55D) {
            return selected;
        }
        return (selected + random.nextInt(1, 9)) % 9;
    }

    private boolean isAutoSellCandidateInMainHand(AutoSellCandidate candidate) {
        if (mc.player == null) {
            return false;
        }
        return isAutoSellStackMatchesCandidate(mc.player.getMainHandStack(), candidate);
    }

    private boolean isAutoSellStackMatchesCandidate(ItemStack stack, AutoSellCandidate candidate) {
        if (stack == null || stack.isEmpty() || candidate == null) {
            return false;
        }
        AutoBuyItem configured = candidate.configuredItem();
        if (configured != null && configured.matches(stack, List.of())) {
            return true;
        }

        String stackName = AutoBuyItem.normalizeLine(stack.getName().getString());
        String candidateName = AutoBuyItem.normalizeLine(candidate.itemName());
        if (!stackName.isBlank() && !candidateName.isBlank() && (stackName.contains(candidateName) || candidateName.contains(stackName))) {
            return true;
        }

        ItemStack icon = configured != null ? configured.getIconStack() : ItemStack.EMPTY;
        return icon != null
                && !icon.isEmpty()
                && icon.getItem() == stack.getItem();
    }

    private AutoSellCandidate findNextAutoSellCandidate() {
        if (mc.player == null) {
            return null;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int startIndex = Math.max(0, Math.min(35, holyWorldAutoSellScanStartIndex));
        int step = AUTOSELL_SCAN_STEPS[random.nextInt(AUTOSELL_SCAN_STEPS.length)];
        int direction = random.nextBoolean() ? 1 : -1;
        for (int offset = 0; offset < 36; offset++) {
            int idx = Math.floorMod(startIndex + (offset * step * direction), 36);
            ItemStack stack = mc.player.getInventory().getStack(idx);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (isAutoSellItemSkippedForCurrentCycle(stack)) {
                continue;
            }

            AutoBuyItem configured = findConfiguredAutoSellItem(stack);
            if (configured == null) {
                continue;
            }

            long requiredPrice = configured.getPriceValue();
            if (requiredPrice <= 0L) {
                continue;
            }

            return new AutoSellCandidate(idx, requiredPrice, stack.getName().getString(), configured);
        }

        return null;
    }

    private AutoBuyItem findConfiguredAutoSellItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        for (AutoBuyItem configured : getConfiguredItems()) {
            if (configured != null
                    && configured.hasPrice()
                    && configured.isSellEnabled()
                    && configured.matches(stack, List.of())) {
                return configured;
            }
        }
        return null;
    }

    private int findPlayerInventorySlotId(int inventoryIndex) {
        if (mc.player == null || inventoryIndex < 0) {
            return -1;
        }

        for (Slot slot : mc.player.currentScreenHandler.slots) {
            if (slot != null
                    && slot.inventory == mc.player.getInventory()
                    && slot.getIndex() == inventoryIndex) {
                return slot.id;
            }
        }

        return -1;
    }

    private long resolveAutoSellRequiredPrice(String offeredItemName) {
        if (offeredItemName == null || offeredItemName.isBlank()) {
            return -1L;
        }

        String normalizedOffered = AutoBuyItem.normalizeLine(offeredItemName);
        if (normalizedOffered.isBlank()) {
            return -1L;
        }

        long result = -1L;
        for (AutoBuyItem configured : getConfiguredItems()) {
            if (!configured.hasPrice() || !configured.isSellEnabled()) {
                continue;
            }

            String display = AutoBuyItem.normalizeLine(configured.getDisplayName());
            String search = AutoBuyItem.normalizeLine(configured.getSearchName());
            boolean matches = (!display.isBlank() && (normalizedOffered.contains(display) || display.contains(normalizedOffered)))
                    || (!search.isBlank() && (normalizedOffered.contains(search) || search.contains(normalizedOffered)));
            if (matches) {
                if (result < 0L) {
                    result = configured.getPriceValue();
                } else {
                    result = Math.max(result, configured.getPriceValue());
                }
            }
        }

        return result;
    }

    private long extractLastPriceFromText(String rawMessage) {
        Matcher matcher = HOLYWORLD_AUTOSELL_PRICE_PATTERN.matcher(rawMessage);
        long parsed = -1L;
        while (matcher.find()) {
            parsed = parseLongSafe(AutoBuyItem.normalizeDigits(matcher.group(1)));
        }
        return parsed;
    }

    private String extractBracketItem(String rawMessage) {
        int left = rawMessage.indexOf('[');
        int right = rawMessage.indexOf(']', left + 1);
        if (left >= 0 && right > left + 1) {
            return rawMessage.substring(left + 1, right).trim();
        }
        return "";
    }

    private void markCurrentAutoSellItemSkippedForCycle() {
        if (mc.player == null) {
            return;
        }
        ItemStack inHand = mc.player.getMainHandStack();
        if (inHand == null || inHand.isEmpty()) {
            return;
        }
        String normalized = AutoBuyItem.normalizeLine(inHand.getName().getString());
        if (!normalized.isBlank()) {
            holyWorldAutoSellSkippedNamesThisCycle.add(normalized);
        }
    }

    private boolean isAutoSellItemSkippedForCurrentCycle(ItemStack stack) {
        if (stack == null || stack.isEmpty() || holyWorldAutoSellSkippedNamesThisCycle.isEmpty()) {
            return false;
        }
        String normalized = AutoBuyItem.normalizeLine(stack.getName().getString());
        return !normalized.isBlank() && holyWorldAutoSellSkippedNamesThisCycle.contains(normalized);
    }

    private void tickHolyWorldPrepareStorage(long now) {
        releaseHolyWorldMovementKeys();
        if (!isHolyWorldShakeOff() && holyWorldLookWatch.finished(holyWorldNextLookDelayMs)) {
            rotateHolyWorldHead(true);
        }

        if (!isAutoSellEnabled()) {
            holyWorldWalkState = HolyWorldWalkState.IDLE;
            holyWorldWalkStartQueued = false;
            resetHolyWorldAutoSellState();
            startHolyWorldWalkRoutine(now);
            return;
        }

        if (holyWorldAutoSellState == HolyWorldAutoSellState.IDLE || holyWorldAutoSellState == HolyWorldAutoSellState.DONE) {
            if (holyWorldWalkStartQueued) {
                holyWorldWalkStartQueued = false;
                startHolyWorldWalkRoutine(now);
            } else {
                holyWorldWalkState = HolyWorldWalkState.IDLE;
            }
        }
    }

    private boolean isHolyWorldAutoSellActive() {
        return holyWorldAutoSellState != HolyWorldAutoSellState.IDLE && holyWorldAutoSellState != HolyWorldAutoSellState.DONE;
    }

    private void resetHolyWorldAutoSellState() {
        long now = System.currentTimeMillis();
        holyWorldAutoSellState = HolyWorldAutoSellState.IDLE;
        holyWorldAutoSellStateStartedMs = 0L;
        holyWorldAutoSellLastActionMs = 0L;
        holyWorldAutoSellAttempts = 0;
        holyWorldAutoSellConfirmAttempts = 0;
        holyWorldAutoSellNoFilterStreak = 0;
        holyWorldAutoSellNoFilterTotal = 0;
        if (holyWorldAutoSellNoFilterBackoffUntilMs > 0L && now >= holyWorldAutoSellNoFilterBackoffUntilMs) {
            holyWorldAutoSellNoFilterBackoffUntilMs = 0L;
        }
        holyWorldAutoSellOfferedCount = 1;
        holyWorldAutoSellOfferedPrice = -1L;
        holyWorldAutoSellRequiredPrice = -1L;
        holyWorldAutoSellOfferReceived = false;
        holyWorldAutoSellConfirmReceived = false;
        holyWorldAutoSellNoItems = false;
        holyWorldAutoSellNeedNextItem = false;
        holyWorldAutoSellAuctionSlotsFull = false;
        holyWorldAutoSellPauseWalk = false;
        holyWorldTestAutoSellPendingSelling = false;
        holyWorldTestAuctionReopenAtMs = 0L;
        holyWorldTestAuctionReopenAttempts = 0;
        holyWorldNextAutoSellTryMs = 0L;
        holyWorldWalkStartQueued = false;
        holyWorldAutoSellOfferedItem = "";
        holyWorldAutoSellScanStartIndex = 0;
        holyWorldAutoSellCurrentInventoryIndex = -1;
        holyWorldAutoSellSkippedNamesThisCycle.clear();
        holyWorldStorageSlotMisses = 0;
    }

    private boolean tryStartHolyWorldRelogCycle(GenericContainerScreen screen) {
        if (!isHolyWorldMode() || screen == null || holyWorldWalkState != HolyWorldWalkState.IDLE) {
            return false;
        }
        if (refreshCount <= 0 || refreshCount - holyWorldLastRelogRefreshMark < holyWorldNextRelogRefreshTarget) {
            return false;
        }
        holyWorldLastRelogRefreshMark = refreshCount;
        holyWorldNextRelogRefreshTarget = randomRelogRefreshTarget();
        autoBuyScript.cleanup();
        long now = System.currentTimeMillis();

        if (isHolyWorldShakeOff()) {
            if (mc.currentScreen != null) {
                mc.currentScreen.close();
            }
            int targetAnarchy = chooseNextHolyWorldAnarchy(now);
            if (targetAnarchy > 0) {
                beginHolyWorldRctSequence(targetAnarchy, now, "safe_cycle_relog");
            }
            return true;
        }

        if (isAutoSellEnabled() && !isTimedAutoSellEnabled()) {
            holyWorldWalkState = HolyWorldWalkState.PREPARE_STORAGE;
            holyWorldWalkStartQueued = true;
            startHolyWorldAutoSellStorageFlow();
            return true;
        }

        if (mc.currentScreen != null) {
            mc.currentScreen.close();
        }
        startHolyWorldWalkRoutine(now);
        return true;
    }

    private void startHolyWorldWalkRoutine(long now) {
        if (mc.player == null) {
            return;
        }

        holyWorldWalkState = HolyWorldWalkState.RANDOM_WALK;
        holyWorldStageStartedMs = now;
        holyWorldWalkDeadlineMs = now + pickHolyWorldMainWalkDurationMs();
        holyWorldRouteDurationCycle++;
        holyWorldPostSellWalkUntilMs = 0L;
        holyWorldWalkStartPos = mc.player.getPos();
        holyWorldStepBackStartPos = holyWorldWalkStartPos;
        holyWorldCurrentWalkTarget = Vec3d.ZERO;
        holyWorldRoutePointOneTarget = randomizeHolyWorldRoutePoint(HOLYWORLD_ROUTE_POINT_ONE, 2.6D, 2.1D, 0.35D);
        holyWorldRoutePointTwoTarget = randomizeHolyWorldRoutePoint(HOLYWORLD_ROUTE_POINT_TWO, 3.0D, 2.5D, 0.35D);
        holyWorldRouteStep = 0;
        holyWorldRouteStepStartedMs = now;
        holyWorldRouteInspectUntilMs = 0L;
        holyWorldRouteReturnTarget = Vec3d.ZERO;
        holyWorldNextMoveSwitchMs = 0L;
        holyWorldMoveBack = false;
        holyWorldMoveStrafeLeft = false;
        holyWorldMoveStrafeRight = false;
        holyWorldMoveJump = false;
        holyWorldMoveSprint = true;
        holyWorldTestSellUntilDone = false;
        holyWorldNextLookDelayMs = 0L;
        holyWorldNextAutoSellTryMs = now + weightedBetween(HOLYWORLD_AUTOSELL_RETRY_MIN_MS, HOLYWORLD_AUTOSELL_RETRY_MAX_MS, 1.18D);
        holyWorldLookWatch.reset();
        holyWorldNeedAuctionReopen = false;
        releaseHolyWorldMovementKeys();
    }

    private void tickHolyWorldLook() {
        if (!autoBuyEnabled || !isHolyWorldMode() || mc.player == null) {
            if (holyWorldWalkState != HolyWorldWalkState.IDLE
                    || holyWorldTestSessionDeadlineMs != 0L
                    || holyWorldTestFrenzyDeadlineMs != 0L
                    || scriptedLookActive) {
                resetHolyWorldState();
            }
            holyWorldTestSellUntilDone = false;
            return;
        }

        long now = System.currentTimeMillis();
        boolean inAuction = mc.currentScreen instanceof GenericContainerScreen screen && isAuctionScreen(screen);
        if (isAutoBuyWorkTest()) {
            tickHolyWorldLookTest(now, inAuction);
            return;
        }
        if (scriptedLookActive && holyWorldWalkState == HolyWorldWalkState.IDLE) {
            tickScriptedLookRoutine(now);
        }
        if (inAuction) {
            holyWorldNeedAuctionReopen = false;
            holyWorldAuctionReopenWatch.reset();
        }
        if (isHolyWorldShakeOff()
                && holyWorldWalkState != HolyWorldWalkState.IDLE
                && holyWorldWalkState != HolyWorldWalkState.PREPARE_STORAGE) {
            clearHolyWorldWalkState();
        }

        if (isHolyWorldPeriodicBreakActive()) {
            return;
        }

        if (holyWorldWalkState == HolyWorldWalkState.IDLE) {
            tickHolyWorldRctRetry(now, inAuction);
            if (holyWorldNeedAuctionReopen
                    && holyWorldTimedSellState == HolyWorldTimedSellState.IDLE
                    && !isHolyWorldStaffEscapeBlocking()
                    && holyWorldPendingRctAnarchy <= 0
                    && !inAuction
                    && (mc.currentScreen == null || mc.currentScreen instanceof ChatScreen)
                    && !isTypingChatNow()
                    && mc.player.networkHandler != null
                    && holyWorldAuctionReopenWatch.finished(delayFromActionStamp(
                    Math.max(holyWorldLastRctSentMs, autoBuyStartMs),
                    HOLYWORLD_REOPEN_AH_DELAY_MIN_MS,
                    HOLYWORLD_REOPEN_AH_DELAY_MAX_MS
            ))) {
                if (mc.currentScreen instanceof ChatScreen) {
                    mc.currentScreen.close();
                }
                queueVisibleChatCommand("/ah");
                holyWorldAuctionReopenWatch.reset();
                refreshWatch.reset();
                scanWatch.reset();
                nextRefreshDelayMs = 0L;
            }
            return;
        }

        if (holyWorldWalkState != HolyWorldWalkState.PREPARE_STORAGE
                && mc.currentScreen != null
                && !(mc.currentScreen instanceof ChatScreen && isTypingChatNow())) {
            mc.currentScreen.close();
        }

        switch (holyWorldWalkState) {
            case PREPARE_STORAGE -> tickHolyWorldPrepareStorage(now);
            case RANDOM_WALK -> tickHolyWorldRandomWalk(now);
            case RETURN_TO_ORIGIN -> tickHolyWorldReturnToOrigin(now);
            case STEP_BACK -> tickHolyWorldStepBack(now);
            case IDLE -> {
            }
        }
    }

    private boolean handleHolyWorldTestSessionTimeout() {
        if (!isHolyWorldMode() || !isAutoBuyWorkTest()) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (holyWorldTestSessionDeadlineMs == 0L) {
            holyWorldTestSessionDeadlineMs = now + randomBetween(HOLYWORLD_TEST_SESSION_MIN_MS, HOLYWORLD_TEST_SESSION_MAX_MS);
        }
        if (now < holyWorldTestSessionDeadlineMs) {
            return false;
        }

        autoBuyScript.cleanup();
        if (mc.currentScreen != null) {
            mc.currentScreen.close();
        }
        holyWorldTestSessionDeadlineMs = 0L;
        if (isAutoSellEnabled()) {
            holyWorldTestSellUntilDone = true;
            holyWorldTestAutoSellPendingSelling = false;
            holyWorldTestAuctionReopenAtMs = now + randomBetween(220L, 880L);
            holyWorldTestAuctionReopenAttempts = 0;
            holyWorldNextAutoSellTryMs = 0L;
            holyWorldAutoSellPauseWalk = true;
            holyWorldTestFrenzyDeadlineMs = now + 120_000L;
        } else {
            holyWorldTestSellUntilDone = false;
            holyWorldTestFrenzyDeadlineMs = now + randomBetween(HOLYWORLD_TEST_FRENZY_MIN_MS, HOLYWORLD_TEST_FRENZY_MAX_MS);
        }
        holyWorldNextLookDelayMs = 0L;
        holyWorldLookWatch.reset();
        return true;
    }

    private void tickHolyWorldLookTest(long now, boolean inAuction) {
        if (holyWorldWalkState != HolyWorldWalkState.IDLE) {
            clearHolyWorldWalkState();
        }
        tickHolyWorldTestAutoSell(now, inAuction);
        boolean autoSellActiveInTest = isAutoSellEnabled() && (isHolyWorldAutoSellActive() || holyWorldTestAutoSellPendingSelling || holyWorldTestSellUntilDone);

        if (holyWorldTestSellUntilDone) {
            startScriptedLookRoutine(now, false);
            tickScriptedLookRoutine(now);
            if (!isAutoSellEnabled()) {
                holyWorldTestSellUntilDone = false;
            } else if (holyWorldAutoSellState == HolyWorldAutoSellState.DONE
                    && !isHolyWorldAutoSellActive()
                    && !holyWorldTestAutoSellPendingSelling) {
                holyWorldTestSellUntilDone = false;
                holyWorldTestFrenzyDeadlineMs = 0L;
                stopScriptedLookRoutine(now);
                if (!inAuction) {
                    queueVisibleChatCommand("/ah");
                }
                refreshWatch.reset();
                scanWatch.reset();
                nextRefreshDelayMs = 0L;
            }
            return;
        }

        if (!inAuction
                && !autoSellActiveInTest
                && holyWorldTestSessionDeadlineMs > 0L
                && holyWorldTestFrenzyDeadlineMs == 0L
                && now - lastAuctionSeenMs > 2_500L) {
            holyWorldTestSessionDeadlineMs = 0L;
        }

        if (inAuction) {
            if (holyWorldTestSessionDeadlineMs == 0L) {
                holyWorldTestSessionDeadlineMs = now + randomBetween(HOLYWORLD_TEST_SESSION_MIN_MS, HOLYWORLD_TEST_SESSION_MAX_MS);
            }
            stopHolyWorldTestAuctionWalk();
            startScriptedLookRoutine(now, false);
            tickScriptedLookRoutine(now);
            return;
        }

        stopHolyWorldTestAuctionWalk();

        if (autoSellActiveInTest) {
            startScriptedLookRoutine(now, false);
            tickScriptedLookRoutine(now);
            return;
        }

        if (holyWorldTestFrenzyDeadlineMs > now) {
            startScriptedLookRoutine(now, false);
            tickScriptedLookRoutine(now);
            return;
        }

        if (holyWorldTestFrenzyDeadlineMs != 0L) {
            holyWorldTestFrenzyDeadlineMs = 0L;
            holyWorldTestSellUntilDone = false;
            stopScriptedLookRoutine(now);
            stopHolyWorldRotation();
            if (mc.player != null && mc.player.networkHandler != null) {
                queueVisibleChatCommand("/ah");
            }
            refreshWatch.reset();
            scanWatch.reset();
            nextRefreshDelayMs = 0L;
        }
    }

    private void tickHolyWorldTestAutoSell(long now, boolean inAuction) {
        boolean forceSellNow = holyWorldTestSellUntilDone;
        if (!isAutoSellEnabled()) {
            if (holyWorldAutoSellState != HolyWorldAutoSellState.IDLE) {
                resetHolyWorldAutoSellState();
            }
            holyWorldTestAutoSellPendingSelling = false;
            holyWorldTestAuctionReopenAtMs = 0L;
            holyWorldTestAuctionReopenAttempts = 0;
            holyWorldTestSellUntilDone = false;
            return;
        }
        if (isHolyWorldStaffEscapeBlocking() || isHolyWorldPeriodicBreakActive()) {
            return;
        }

        if (isHolyWorldAutoSellActive()) {
            holyWorldAutoSellPauseWalk = true;
            holyWorldTestAuctionReopenAtMs = 0L;
            holyWorldTestAuctionReopenAttempts = 0;
            return;
        }

        if (holyWorldAutoSellState == HolyWorldAutoSellState.DONE) {
            if (holyWorldTestAutoSellPendingSelling) {
                holyWorldTestAutoSellPendingSelling = false;
                holyWorldAutoSellPauseWalk = true;
                startHolyWorldAutoSellSellingFlow();
                return;
            }
            if (!forceSellNow) {
                holyWorldAutoSellState = HolyWorldAutoSellState.IDLE;
                holyWorldAutoSellPauseWalk = false;
                holyWorldTestAuctionReopenAtMs = 0L;
                holyWorldTestAuctionReopenAttempts = 0;
                return;
            }
            if (!inAuction) {
                if (holyWorldTestAuctionReopenAtMs <= 0L) {
                    holyWorldTestAuctionReopenAtMs = now + (forceSellNow
                            ? randomBetween(180L, 680L)
                            : randomBetween(700L, 2_100L));
                }
                if (now >= holyWorldTestAuctionReopenAtMs) {
                    if (isTypingChatNow()) {
                        clearVisibleChatTypingState();
                    }
                    queueVisibleChatCommand("/ah", true);
                    holyWorldTestAuctionReopenAttempts++;
                    if (holyWorldTestAuctionReopenAttempts >= 2 && mc.player != null && mc.player.networkHandler != null) {
                        mc.player.networkHandler.sendChatCommand("ah");
                    }
                    holyWorldTestAuctionReopenAtMs = now + (forceSellNow
                            ? randomBetween(1_600L, 3_400L)
                            : randomBetween(2_900L, 5_800L));
                }
            } else {
                holyWorldTestAuctionReopenAtMs = 0L;
                holyWorldTestAuctionReopenAttempts = 0;
            }
            return;
        }

        if (!inAuction) {
            if (forceSellNow) {
                if (holyWorldTestAuctionReopenAtMs <= 0L) {
                    holyWorldTestAuctionReopenAtMs = now + randomBetween(220L, 900L);
                }
                if (now >= holyWorldTestAuctionReopenAtMs) {
                    if (isTypingChatNow()) {
                        clearVisibleChatTypingState();
                    }
                    queueVisibleChatCommand("/ah", true);
                    holyWorldTestAuctionReopenAttempts++;
                    if (holyWorldTestAuctionReopenAttempts >= 2 && mc.player != null && mc.player.networkHandler != null) {
                        mc.player.networkHandler.sendChatCommand("ah");
                    }
                    holyWorldTestAuctionReopenAtMs = now + randomBetween(1_500L, 3_200L);
                }
                return;
            }
            holyWorldAutoSellPauseWalk = false;
            return;
        }

        if (forceSellNow) {
            holyWorldAutoSellPauseWalk = true;
            holyWorldTestAutoSellPendingSelling = true;
            holyWorldTestAuctionReopenAtMs = 0L;
            holyWorldTestAuctionReopenAttempts = 0;
            startHolyWorldAutoSellStorageFlow();
            return;
        }
        holyWorldAutoSellPauseWalk = false;
    }

    private void tickHolyWorldTestAuctionWalk(long now) {
        if (mc.player == null) {
            return;
        }

        if (now < holyWorldTestWalkPauseUntilMs) {
            releaseHolyWorldMovementKeys();
            return;
        }

        if (holyWorldTestWalkMoveUntilMs <= 0L || holyWorldTestWalkRestUntilMs <= 0L) {
            startHolyWorldTestWalkBurst(now, false);
        } else if (now >= holyWorldTestWalkMoveUntilMs) {
            if (now < holyWorldTestWalkRestUntilMs) {
                releaseHolyWorldMovementKeys();
                return;
            }
            startHolyWorldTestWalkBurst(now, false);
        }

        if (holyWorldTestWalkSampleAtMs <= 0L || holyWorldTestWalkSamplePos == Vec3d.ZERO) {
            holyWorldTestWalkSampleAtMs = now;
            holyWorldTestWalkSamplePos = mc.player.getPos();
        } else if (now - holyWorldTestWalkSampleAtMs >= 560L) {
            double moved = horizontalDistance(mc.player.getPos(), holyWorldTestWalkSamplePos);
            if (moved < 0.07D) {
                holyWorldTestWalkStuckStrikes++;
            } else {
                holyWorldTestWalkStuckStrikes = 0;
            }
            holyWorldTestWalkSampleAtMs = now;
            holyWorldTestWalkSamplePos = mc.player.getPos();

            if (holyWorldTestWalkStuckStrikes >= 2) {
                holyWorldTestWalkStuckStrikes = 0;
                startHolyWorldTestWalkBurst(now, true);
            }
        }

        boolean forward = !holyWorldMoveBack;
        setMovementKeys(forward, holyWorldMoveBack, holyWorldMoveStrafeLeft, holyWorldMoveStrafeRight, false);
        setHolyWorldSprint(false);
    }

    private void stopHolyWorldTestAuctionWalk() {
        holyWorldTestWalkPauseUntilMs = 0L;
        holyWorldTestWalkMoveUntilMs = 0L;
        holyWorldTestWalkRestUntilMs = 0L;
        holyWorldTestWalkSampleAtMs = 0L;
        holyWorldTestWalkSamplePos = Vec3d.ZERO;
        holyWorldTestWalkStuckStrikes = 0;
        holyWorldMoveStrafeLeft = false;
        holyWorldMoveStrafeRight = false;
        holyWorldMoveBack = false;
        holyWorldMoveJump = false;
        releaseHolyWorldMovementKeys();
    }

    private void startHolyWorldTestWalkBurst(long now, boolean recovery) {
        if (mc.player == null) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        holyWorldMoveBack = recovery || random.nextDouble() < 0.08D;
        double strafeChance = recovery ? 0.72D : 0.24D;
        holyWorldMoveStrafeLeft = random.nextDouble() < strafeChance;
        holyWorldMoveStrafeRight = !holyWorldMoveStrafeLeft && random.nextDouble() < strafeChance;
        holyWorldMoveJump = false;

        long moveMin = recovery ? 420L : 850L;
        long moveMax = recovery ? 920L : 1_850L;
        holyWorldTestWalkMoveUntilMs = now + randomBetween(moveMin, moveMax);

        long restMin = recovery ? 280L : 260L;
        long restMax = recovery ? 620L : 760L;
        holyWorldTestWalkRestUntilMs = holyWorldTestWalkMoveUntilMs + randomBetween(restMin, restMax);
        holyWorldTestWalkSampleAtMs = now;
        holyWorldTestWalkSamplePos = mc.player.getPos();

        if (recovery && !isHolyWorldShakeOff() && holyWorldLookWatch.finished(90L)) {
            rotateHolyWorldHead(true);
        }
    }

    private void tickHolyWorldRandomWalk(long now) {
        if (isAutoSellEnabled()
                && !isTimedAutoSellEnabled()
                && !holyWorldAutoSellPauseWalk
                && !isHolyWorldAutoSellActive()
                && now >= holyWorldNextAutoSellTryMs) {
            holyWorldAutoSellPauseWalk = true;
            startHolyWorldAutoSellSellingFlow();
        }

        if (holyWorldAutoSellPauseWalk && isHolyWorldAutoSellActive()) {
            releaseHolyWorldMovementKeys();
            if (!isHolyWorldShakeOff() && holyWorldLookWatch.finished(holyWorldNextLookDelayMs)) {
                rotateHolyWorldHead(true);
            }
            return;
        }

        if (holyWorldAutoSellPauseWalk && !isHolyWorldAutoSellActive()) {
            holyWorldAutoSellPauseWalk = false;
            long postSellWalkMs = pickHolyWorldPostSellWalkDurationMs();
            holyWorldPostSellWalkUntilMs = Math.max(holyWorldPostSellWalkUntilMs, now + postSellWalkMs);
            long nextDelay = weightedBetween(HOLYWORLD_AUTOSELL_RETRY_MIN_MS, HOLYWORLD_AUTOSELL_RETRY_MAX_MS, 1.05D);
            if (ThreadLocalRandom.current().nextDouble() < 0.14D) {
                nextDelay += randomBetween(1_000L, 5_500L);
            }
            holyWorldNextAutoSellTryMs = now + nextDelay;
        }

        if (now - holyWorldRouteStepStartedMs > HOLYWORLD_ROUTE_STEP_TIMEOUT_MS) {
            finishHolyWorldRelogCycle(now);
            return;
        }

        switch (holyWorldRouteStep) {
            case 0 -> {
                Vec3d routePointOne = resolveHolyWorldRoutePointOne();
                if (moveToHolyWorldRouteTarget(routePointOne, now, true, 1.25D)) {
                    holyWorldRouteStep = 1;
                    holyWorldRouteStepStartedMs = now;
                    holyWorldRouteInspectUntilMs = now + randomBetween(HOLYWORLD_ROUTE_LOOK_MIN_MS, HOLYWORLD_ROUTE_LOOK_MAX_MS);
                }
            }
            case 1 -> {
                inspectHolyWorldRoutePoint(resolveHolyWorldRoutePointOne(), now);
                if (now >= holyWorldRouteInspectUntilMs) {
                    holyWorldRouteStep = 2;
                    holyWorldRouteStepStartedMs = now;
                }
            }
            case 2 -> {
                Vec3d routePointTwo = resolveHolyWorldRoutePointTwo();
                if (moveToHolyWorldRouteTarget(routePointTwo, now, true, 1.35D)) {
                    holyWorldRouteStep = 3;
                    holyWorldRouteStepStartedMs = now;
                    holyWorldRouteInspectUntilMs = now + randomBetween(HOLYWORLD_ROUTE_LOOK_MIN_MS, HOLYWORLD_ROUTE_LOOK_MAX_MS);
                }
            }
            case 3 -> {
                inspectHolyWorldRoutePoint(resolveHolyWorldRoutePointTwo(), now);
                if (now >= holyWorldRouteInspectUntilMs && now >= holyWorldWalkDeadlineMs) {
                    finishHolyWorldRelogCycle(now);
                    return;
                }
                if (now >= holyWorldRouteInspectUntilMs) {
                    holyWorldRouteInspectUntilMs = Math.min(
                            holyWorldWalkDeadlineMs,
                            now + randomBetween(1_500L, 3_200L)
                    );
                }
            }
            default -> {
                if (moveToHolyWorldRouteTarget(resolveHolyWorldRoutePointTwo(), now, false, 1.35D)) {
                    finishHolyWorldRelogCycle(now);
                }
            }
        }
    }

    private boolean moveToHolyWorldRouteTarget(Vec3d target, long now, boolean sprint, double reachDistance) {
        if (mc.player == null || target == null || target == Vec3d.ZERO) {
            return false;
        }
        double distance = horizontalDistance(mc.player.getPos(), target);
        if (distance <= reachDistance) {
            releaseHolyWorldMovementKeys();
            holyWorldNextMoveSwitchMs = 0L;
            return true;
        }

        rotateHolyWorldToward(target, sprint);
        updateHolyWorldMovementPattern(now, sprint);
        boolean forward = !holyWorldMoveBack;
        setMovementKeys(forward, holyWorldMoveBack, holyWorldMoveStrafeLeft, holyWorldMoveStrafeRight, holyWorldMoveJump);
        setHolyWorldSprint(holyWorldMoveSprint);
        holyWorldCurrentWalkTarget = target;
        return false;
    }

    private void inspectHolyWorldRoutePoint(Vec3d focus, long now) {
        releaseHolyWorldMovementKeys();
        if (focus == null || focus == Vec3d.ZERO) {
            return;
        }
        if (isHolyWorldShakeOff()) {
            rotateHolyWorldToward(focus, false);
            return;
        }
        if (holyWorldLookWatch.finished(Math.max(90L, holyWorldNextLookDelayMs))) {
            rotateHolyWorldHead(false);
        }
        if (now < holyWorldRouteInspectUntilMs) {
            Vec3d glance = randomizeHolyWorldRoutePoint(focus, 1.9D, 1.9D, 0.5D);
            rotateHolyWorldToward(glance, false);
        }
    }

    private Vec3d resolveHolyWorldRoutePointOne() {
        if (holyWorldRoutePointOneTarget == null || holyWorldRoutePointOneTarget == Vec3d.ZERO) {
            holyWorldRoutePointOneTarget = randomizeHolyWorldRoutePoint(HOLYWORLD_ROUTE_POINT_ONE, 2.6D, 2.1D, 0.35D);
        }
        return holyWorldRoutePointOneTarget;
    }

    private Vec3d resolveHolyWorldRoutePointTwo() {
        if (holyWorldRoutePointTwoTarget == null || holyWorldRoutePointTwoTarget == Vec3d.ZERO) {
            holyWorldRoutePointTwoTarget = randomizeHolyWorldRoutePoint(HOLYWORLD_ROUTE_POINT_TWO, 3.0D, 2.5D, 0.35D);
        }
        return holyWorldRoutePointTwoTarget;
    }

    private Vec3d randomizeHolyWorldRoutePoint(Vec3d base, double spreadX, double spreadZ, double spreadY) {
        if (base == null || base == Vec3d.ZERO) {
            return Vec3d.ZERO;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double x = base.x + random.nextDouble(-spreadX, spreadX);
        double y = base.y + random.nextDouble(-spreadY, spreadY);
        double z = base.z + random.nextDouble(-spreadZ, spreadZ);
        return new Vec3d(x, y, z);
    }

    private void updateHolyWorldMovementPattern(long now, boolean preferSprint) {
        if (mc.player == null) {
            return;
        }
        if (now < holyWorldNextMoveSwitchMs) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        boolean gentleWalk = holyWorldWalkState == HolyWorldWalkState.RANDOM_WALK;

        double backChance = gentleWalk
                ? (preferSprint ? 0.015D : 0.035D)
                : (preferSprint ? 0.05D : 0.10D);
        double strafeChance = gentleWalk ? 0.17D : 0.31D;
        double keepIdleStrafeChance = gentleWalk ? 0.08D : 0.14D;
        double pairedStrafeChance = gentleWalk ? 0.46D : 0.72D;
        double jumpChance = gentleWalk
                ? (preferSprint ? 0.04D : 0.02D)
                : (preferSprint ? 0.18D : 0.10D);
        double sprintChance = gentleWalk
                ? (preferSprint ? 0.985D : 0.82D)
                : (preferSprint ? 0.92D : 0.42D);

        holyWorldMoveBack = random.nextDouble() < backChance;
        holyWorldMoveStrafeLeft = !holyWorldMoveBack && random.nextDouble() < strafeChance;
        holyWorldMoveStrafeRight = !holyWorldMoveBack && !holyWorldMoveStrafeLeft && random.nextDouble() < strafeChance;
        if (!holyWorldMoveBack && !holyWorldMoveStrafeLeft && !holyWorldMoveStrafeRight && random.nextDouble() < keepIdleStrafeChance) {
            holyWorldMoveStrafeLeft = random.nextBoolean();
            holyWorldMoveStrafeRight = !holyWorldMoveStrafeLeft && random.nextDouble() < pairedStrafeChance;
        }
        holyWorldMoveJump = mc.player.isOnGround() && random.nextDouble() < jumpChance;
        holyWorldMoveSprint = random.nextDouble() < sprintChance;
        if (gentleWalk) {
            holyWorldNextMoveSwitchMs = now + weightedBetween(
                    HOLYWORLD_MOVE_SWITCH_GENTLE_MIN_MS,
                    HOLYWORLD_MOVE_SWITCH_GENTLE_MAX_MS,
                    1.12D
            );
        } else {
            holyWorldNextMoveSwitchMs = now + weightedBetween(
                    HOLYWORLD_MOVE_SWITCH_MIN_MS,
                    HOLYWORLD_MOVE_SWITCH_MAX_MS + 520L,
                    0.9D
            );
        }
    }

    private Vec3d pickHolyWorldReturnRangeTarget() {
        if (mc.player == null) {
            return Vec3d.ZERO;
        }
        double targetX = ThreadLocalRandom.current().nextDouble(0.0D, 5.0D);
        double targetZ = ThreadLocalRandom.current().nextDouble(0.0D, 3.0D);
        double y = holyWorldWalkStartPos != null && holyWorldWalkStartPos != Vec3d.ZERO
                ? holyWorldWalkStartPos.y
                : mc.player.getY();
        return new Vec3d(targetX, y, targetZ);
    }

    private void tickHolyWorldReturnToOrigin(long now) {
        if (mc.player == null) {
            return;
        }

        double distance = horizontalDistance(mc.player.getPos(), holyWorldWalkStartPos);
        if (distance <= HOLYWORLD_RETURN_DISTANCE || now - holyWorldStageStartedMs >= HOLYWORLD_RETURN_TIMEOUT_MS) {
            holyWorldWalkState = HolyWorldWalkState.STEP_BACK;
            holyWorldStageStartedMs = now;
            holyWorldStepBackStartPos = mc.player.getPos();
            releaseHolyWorldMovementKeys();
            return;
        }

        rotateHolyWorldToward(holyWorldWalkStartPos, true);
        updateHolyWorldMovementPattern(now, true);
        boolean forward = !holyWorldMoveBack;
        setMovementKeys(forward, holyWorldMoveBack, holyWorldMoveStrafeLeft, holyWorldMoveStrafeRight, holyWorldMoveJump);
        setHolyWorldSprint(holyWorldMoveSprint);
    }

    private void tickHolyWorldStepBack(long now) {
        if (mc.player == null) {
            return;
        }

        if (!isHolyWorldShakeOff() && holyWorldLookWatch.finished(holyWorldNextLookDelayMs)) {
            rotateHolyWorldHead(true);
        }
        setMovementKeys(false, true, false, false, false);

        double stepped = horizontalDistance(mc.player.getPos(), holyWorldStepBackStartPos);
        if (stepped >= HOLYWORLD_STEP_BACK_DISTANCE || now - holyWorldStageStartedMs >= HOLYWORLD_STEP_BACK_TIMEOUT_MS) {
            finishHolyWorldRelogCycle(now);
        }
    }

    private void finishHolyWorldRelogCycle(long now) {
        clearHolyWorldWalkState();

        int anarchy = chooseNextHolyWorldAnarchy(now);
        if (anarchy > 0) {
            beginHolyWorldRctSequence(anarchy, now, "walk_cycle_relog");
        }
        resetHolyWorldAutoSellState();
    }

    private void clearHolyWorldWalkState() {
        releaseHolyWorldMovementKeys();
        stopHolyWorldRotation();
        holyWorldWalkState = HolyWorldWalkState.IDLE;
        holyWorldWalkDeadlineMs = 0L;
        holyWorldTestWalkPauseUntilMs = 0L;
        holyWorldTestWalkMoveUntilMs = 0L;
        holyWorldTestWalkRestUntilMs = 0L;
        holyWorldTestWalkSampleAtMs = 0L;
        holyWorldTestWalkSamplePos = Vec3d.ZERO;
        holyWorldTestWalkStuckStrikes = 0;
        holyWorldTestSessionDeadlineMs = 0L;
        holyWorldTestFrenzyDeadlineMs = 0L;
        holyWorldPostSellWalkUntilMs = 0L;
        holyWorldStageStartedMs = 0L;
        holyWorldNextMoveSwitchMs = 0L;
        holyWorldMoveBack = false;
        holyWorldMoveStrafeLeft = false;
        holyWorldMoveStrafeRight = false;
        holyWorldMoveJump = false;
        holyWorldMoveSprint = true;
        holyWorldCurrentWalkTarget = Vec3d.ZERO;
        holyWorldRoutePointOneTarget = HOLYWORLD_ROUTE_POINT_ONE;
        holyWorldRoutePointTwoTarget = HOLYWORLD_ROUTE_POINT_TWO;
        holyWorldRouteStep = 0;
        holyWorldRouteStepStartedMs = 0L;
        holyWorldRouteInspectUntilMs = 0L;
        holyWorldRouteReturnTarget = Vec3d.ZERO;
    }

    private int chooseNextHolyWorldAnarchy(long now) {
        cleanExpiredHolyWorldAnarchyEntries(now);

        List<Integer> available = new ArrayList<>();
        int cappedMax = Math.min(HOLYWORLD_RANDOM_RCT_MAX, HOLYWORLD_RANDOM_RCT_HARD_MAX);
        for (int i = HOLYWORLD_RANDOM_RCT_MIN; i <= cappedMax; i++) {
            if (HOLYWORLD_BLOCKED_RCT.contains(i)) {
                continue;
            }
            Long until = holyWorldRecentAnarchyEntries.get(i);
            if (until == null || until <= now) {
                available.add(i);
            }
        }

        if (available.isEmpty()) {
            for (int i = HOLYWORLD_RANDOM_RCT_MIN; i <= cappedMax; i++) {
                if (!HOLYWORLD_BLOCKED_RCT.contains(i)) {
                    available.add(i);
                }
            }
        }

        if (available.isEmpty()) {
            return -1;
        }

        int selected = available.get(ThreadLocalRandom.current().nextInt(available.size()));
        holyWorldRecentAnarchyEntries.put(selected, now + HOLYWORLD_RCT_REUSE_COOLDOWN_MS);
        return selected;
    }

    private String buildRctChatCommand(int anarchy) {
        if (anarchy > 0) {
            return ".rct " + anarchy;
        }
        return ".rct";
    }

    private void tickHolyWorldRctRetry(long now, boolean inAuction) {
        if (!isHolyWorldMode()) {
            return;
        }

        if (inAuction) {
            clearHolyWorldPendingRctSequence();
            return;
        }

        if (holyWorldPendingRctAnarchy <= 0) {
            return;
        }
        int currentAnarchy = getCurrentHolyWorldAnarchyFromScoreboard();
        if (currentAnarchy > 0 && currentAnarchy == holyWorldPendingRctAnarchy) {
            clearHolyWorldPendingRctSequence();
            return;
        }
        if (isHolyWorldLightConfirmedByScoreboard(currentAnarchy)) {
            clearHolyWorldPendingRctSequence();
            return;
        }
        if (tryHolyWorldRctCompassRecovery(now)) {
            return;
        }
        if (holyWorldPendingRctAttempts >= HOLYWORLD_RCT_RETRY_MAX_ATTEMPTS) {
            if (now - holyWorldLastRctSentMs < HOLYWORLD_RCT_RESULT_WAIT_MS) {
                return;
            }
            clearHolyWorldPendingRctSequence();
            return;
        }
        if (now < holyWorldNextRctRetryMs) {
            return;
        }
        if (isTypingChatNow()) {
            return;
        }
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen)) {
            return;
        }

        if (holyWorldPendingRctNeedsHubRetry && holyWorldPendingRctAttempts == 1) {
            if (currentAnarchy > 0 && now - holyWorldPendingRctCreatedMs < HOLYWORLD_RCT_HUB_WAIT_TIMEOUT_MS) {
                holyWorldNextRctRetryMs = now + randomBetween(HOLYWORLD_RCT_RETRY_MIN_DELAY_MS, HOLYWORLD_RCT_RETRY_MAX_DELAY_MS);
                return;
            }
        }

        queueVisibleChatCommand(buildRctChatCommand(holyWorldPendingRctAnarchy));
        holyWorldPendingRctAttempts++;
        holyWorldPendingRctNeedsHubRetry = false;
        holyWorldLastRctAnarchy = holyWorldPendingRctAnarchy;
        holyWorldLastRctSentMs = now;
        holyWorldNextRctRetryMs = now + randomBetween(HOLYWORLD_RCT_RETRY_MIN_DELAY_MS, HOLYWORLD_RCT_RETRY_MAX_DELAY_MS);
    }

    private boolean tryHolyWorldRctCompassRecovery(long now) {
        if (!holyWorldPendingRctNeedCompassRecovery) {
            return false;
        }

        if (holyWorldPendingRctCompassAttempts >= HOLYWORLD_RCT_COMPASS_RECOVERY_MAX_ATTEMPTS) {
            holyWorldPendingRctNeedCompassRecovery = false;
            return false;
        }
        if (now < holyWorldNextRctCompassRecoveryMs) {
            return true;
        }
        if (isTypingChatNow()) {
            return true;
        }
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen)) {
            return true;
        }
        if (mc.player == null || mc.interactionManager == null) {
            return true;
        }

        int compassSlot = findCompassHotbarSlot();
        if (compassSlot >= 0) {
            mc.player.getInventory().selectedSlot = compassSlot;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        } else if (holyWorldPendingRctAnarchy > 0) {
            queueVisibleChatCommand(buildRctChatCommand(holyWorldPendingRctAnarchy));
        }

        holyWorldPendingRctCompassAttempts++;
        holyWorldNextRctCompassRecoveryMs = now + HOLYWORLD_RCT_COMPASS_RECOVERY_DELAY_MS;
        return true;
    }

    private int findCompassHotbarSlot() {
        if (mc.player == null) {
            return -1;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && !stack.isEmpty() && stack.getItem() == Items.COMPASS) {
                return i;
            }
        }
        return -1;
    }

    private void cleanExpiredHolyWorldAnarchyEntries(long now) {
        holyWorldRecentAnarchyEntries.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() <= now);
    }

    private void releaseHolyWorldMovementKeys() {
        if (mc.options == null) {
            return;
        }
        setMovementKeys(false, false, false, false, false);
        setHolyWorldSprint(false);
    }

    private void setMovementKeys(boolean forward, boolean back, boolean left, boolean right, boolean jump) {
        if (mc.options == null) {
            return;
        }
        mc.options.forwardKey.setPressed(forward);
        mc.options.backKey.setPressed(back);
        mc.options.leftKey.setPressed(left);
        mc.options.rightKey.setPressed(right);
        mc.options.jumpKey.setPressed(jump);
        if (mc.player != null && mc.player.input != null) {
            mc.player.input.movementForward = forward ? 1.0F : back ? -1.0F : 0.0F;
            mc.player.input.movementSideways = left ? 1.0F : right ? -1.0F : 0.0F;
            if (!forward && !back && !left && !right) {
                mc.player.input.movementForward = 0.0F;
                mc.player.input.movementSideways = 0.0F;
            }
        }
    }

    private void setHolyWorldSprint(boolean sprint) {
        if (mc.options == null) {
            return;
        }
        mc.options.sprintKey.setPressed(sprint);
    }

    private void rotateHolyWorldToward(Vec3d targetPos, boolean aggressive) {
        if (mc.player == null || targetPos == null) {
            return;
        }

        Vec3d eyes = mc.player.getEyePos();
        double dx = targetPos.x - eyes.x;
        double dy = targetPos.y - eyes.y;
        double dz = targetPos.z - eyes.z;
        double xz = Math.sqrt(dx * dx + dz * dz);

        float desiredYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
        float desiredPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(0.001D, xz)));

        boolean gentleWalk = holyWorldWalkState == HolyWorldWalkState.RANDOM_WALK;
        float yawJitter = (float) ThreadLocalRandom.current().nextDouble(
                aggressive ? -1.4D : (gentleWalk ? -2.2D : -4.2D),
                aggressive ? 1.4D : (gentleWalk ? 2.2D : 4.2D)
        );
        float pitchJitter = (float) ThreadLocalRandom.current().nextDouble(
                aggressive ? -1.0D : (gentleWalk ? -1.5D : -3.0D),
                aggressive ? 1.0D : (gentleWalk ? 1.5D : 3.0D)
        );
        desiredYaw += yawJitter;
        desiredPitch += pitchJitter;
        desiredPitch = MathHelper.clamp(desiredPitch, -89.5F, 89.5F);

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float yawStepLimit = aggressive ? HOLYWORLD_ROTATE_STEP_YAW_AGGRESSIVE : HOLYWORLD_ROTATE_STEP_YAW_CALM;
        float pitchStepLimit = aggressive ? HOLYWORLD_ROTATE_STEP_PITCH_AGGRESSIVE : HOLYWORLD_ROTATE_STEP_PITCH_CALM;
        if (gentleWalk) {
            yawStepLimit = Math.min(yawStepLimit, 7.5F);
            pitchStepLimit = Math.min(pitchStepLimit, 4.5F);
        }

        float yaw = limitAngleStep(currentYaw, desiredYaw, yawStepLimit);
        float pitch = limitAngleStep(currentPitch, desiredPitch, pitchStepLimit);
        pitch = MathHelper.clamp(pitch, -89.5F, 89.5F);

        Angle target = new Angle(yaw, pitch);
        RotationController.INSTANCE.rotateTo(
                new Angle.VecRotation(target, target.toVector()),
                mc.player,
                aggressive ? 320 : 360,
                HOLYWORLD_ROTATION_CONFIG,
                TaskPriority.HIGH_IMPORTANCE_3,
                this
        );

        holyWorldNextLookDelayMs = aggressive
                ? weightedBetween(8L, 52L, 0.9D)
                : weightedBetween(24L, 125L, 1.25D);
        holyWorldLookWatch.reset();
    }

    private double horizontalDistance(Vec3d a, Vec3d b) {
        if (a == null || b == null) {
            return 0.0D;
        }
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private float limitAngleStep(float current, float target, float maxStep) {
        float delta = MathHelper.wrapDegrees(target - current);
        return current + MathHelper.clamp(delta, -maxStep, maxStep);
    }

    private void rotateHolyWorldHead(boolean frenzy) {
        if (isHolyWorldShakeOff() || mc.player == null) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (isAutoBuyWorkTest()) {
            float speedMod = 0.85f + random.nextFloat() * 1.65f;
            if (frenzy) {
                speedMod *= 3.0f;
            }

            float yaw = random.nextFloat() * 360.0F - 180.0F;
            float pitch = frenzy
                    ? random.nextFloat() * 179.0F - 89.5F
                    : random.nextFloat() * 132.0F - 60.0F;
            Angle target = new Angle(yaw, pitch);

            RotationController.INSTANCE.rotateTo(
                    new Angle.VecRotation(target, target.toVector()),
                    mc.player,
                    500,
                    HOLYWORLD_ROTATION_CONFIG,
                    TaskPriority.HIGH_IMPORTANCE_3,
                    this
            );

            long minD = frenzy ? 5L : 25L;
            long maxD = frenzy ? 35L : 85L;
            holyWorldNextLookDelayMs = (long) (randomBetween(minD, maxD) / speedMod);
            holyWorldLookWatch.reset();
            return;
        }

        boolean gentleWalk = holyWorldWalkState == HolyWorldWalkState.RANDOM_WALK;
        float speedMod = 0.85f + random.nextFloat() * 1.65f;
        if (frenzy) {
            speedMod *= 3.0f;
        }

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float yawDelta = frenzy
                ? (float) random.nextDouble(gentleWalk ? -34.0D : -52.0D, gentleWalk ? 34.0D : 52.0D)
                : (float) random.nextDouble(gentleWalk ? -22.0D : -36.0D, gentleWalk ? 22.0D : 36.0D);
        float pitchDelta = frenzy
                ? (float) random.nextDouble(-15.0D, 15.0D)
                : (float) random.nextDouble(-9.0D, 9.0D);

        float desiredYaw = currentYaw + yawDelta;
        float desiredPitch = MathHelper.clamp(currentPitch + pitchDelta, -89.5F, 89.5F);

        float yawStep = frenzy ? 18.0F : 12.0F;
        float pitchStep = frenzy ? 10.0F : 7.0F;
        if (gentleWalk) {
            yawStep = Math.min(yawStep, 8.5F);
            pitchStep = Math.min(pitchStep, 5.0F);
        }
        float yaw = limitAngleStep(currentYaw, desiredYaw, yawStep);
        float pitch = limitAngleStep(currentPitch, desiredPitch, pitchStep);
        pitch = MathHelper.clamp(pitch, -89.5F, 89.5F);

        mc.player.prevHeadYaw = mc.player.headYaw;
        mc.player.headYaw = yaw;

        Angle target = new Angle(yaw, pitch);
        RotationController.INSTANCE.rotateTo(
                new Angle.VecRotation(target, target.toVector()),
                mc.player,
                500,
                HOLYWORLD_ROTATION_CONFIG,
                TaskPriority.HIGH_IMPORTANCE_3,
                this
        );

        long minD = frenzy ? 22L : 54L;
        long maxD = frenzy ? 84L : 170L;
        if (gentleWalk) {
            minD += 36L;
            maxD += 92L;
        }

        holyWorldNextLookDelayMs = (long) (randomBetween(minD, maxD) / speedMod);
        holyWorldLookWatch.reset();
    }

    private void startScriptedLookRoutine(long now, boolean force) {
        if (mc.player == null || isHolyWorldShakeOff()) {
            return;
        }
        if (!force) {
            if (scriptedLookActive || now < scriptedLookCooldownUntilMs) {
                return;
            }
        }
        scriptedLookActive = true;
        scriptedLookPhase = 0;
        scriptedLookUntilMs = now + randomBetween(SCRIPTED_LOOK_MIN_MS, SCRIPTED_LOOK_MAX_MS);
        scriptedLookNextStepMs = now;
        int spins = (int) randomBetween(1L, 3L);
        scriptedLookRemainingSpinDeg = spins * 360.0F + randomBetween(18L, 120L);
    }

    private void tickScriptedLookRoutine(long now) {
        if (!scriptedLookActive || mc.player == null) {
            return;
        }
        if (isHolyWorldShakeOff() || now >= scriptedLookUntilMs) {
            stopScriptedLookRoutine(now);
            return;
        }
        if (now < scriptedLookNextStepMs) {
            return;
        }

        switch (scriptedLookPhase) {
            case 0 -> {
                float desiredYaw = mc.player.getYaw() + (float) ThreadLocalRandom.current().nextDouble(-10.0D, 10.0D);
                float desiredPitch = (float) ThreadLocalRandom.current().nextDouble(-87.0D, -74.0D);
                applyScriptedLookRotation(desiredYaw, desiredPitch, 18.0F, 12.0F);
                scriptedLookPhase = 1;
                scriptedLookNextStepMs = now + randomBetween(220L, 520L);
            }
            case 1 -> {
                float step = (float) ThreadLocalRandom.current().nextDouble(26.0D, 58.0D);
                float desiredYaw = mc.player.getYaw() + step + (float) ThreadLocalRandom.current().nextDouble(-6.0D, 6.0D);
                float desiredPitch = (float) ThreadLocalRandom.current().nextDouble(-85.0D, -66.0D);
                applyScriptedLookRotation(desiredYaw, desiredPitch, 22.0F, 15.0F);
                scriptedLookRemainingSpinDeg -= step;
                if (scriptedLookRemainingSpinDeg <= 0.0F) {
                    scriptedLookPhase = 2;
                    scriptedLookNextStepMs = now + randomBetween(160L, 540L);
                } else {
                    scriptedLookNextStepMs = now + randomBetween(72L, 180L);
                }
            }
            default -> {
                float desiredYaw = mc.player.getYaw() + (float) ThreadLocalRandom.current().nextDouble(-95.0D, 95.0D);
                float desiredPitch = (float) ThreadLocalRandom.current().nextDouble(-86.0D, 43.0D);
                applyScriptedLookRotation(desiredYaw, desiredPitch, 26.0F, 18.0F);
                scriptedLookNextStepMs = now + randomBetween(45L, 140L);
            }
        }
    }

    private void stopScriptedLookRoutine(long now) {
        scriptedLookActive = false;
        scriptedLookPhase = 0;
        scriptedLookUntilMs = 0L;
        scriptedLookNextStepMs = 0L;
        scriptedLookRemainingSpinDeg = 0.0F;
        scriptedLookCooldownUntilMs = now + randomBetween(3_000L, 9_000L);
    }

    private void applyScriptedLookRotation(float desiredYaw, float desiredPitch, float yawStep, float pitchStep) {
        if (mc.player == null) {
            return;
        }
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float yaw = limitAngleStep(currentYaw, desiredYaw, yawStep);
        float pitch = MathHelper.clamp(limitAngleStep(currentPitch, desiredPitch, pitchStep), -89.5F, 89.5F);

        Angle target = new Angle(yaw, pitch);
        RotationController.INSTANCE.rotateTo(
                new Angle.VecRotation(target, target.toVector()),
                mc.player,
                450,
                HOLYWORLD_ROTATION_CONFIG,
                TaskPriority.HIGH_IMPORTANCE_3,
                this
        );
    }

    private void resetHolyWorldState() {
        holyWorldWalkState = HolyWorldWalkState.IDLE;
        holyWorldWalkDeadlineMs = 0L;
        holyWorldTestSessionDeadlineMs = 0L;
        holyWorldTestFrenzyDeadlineMs = 0L;
        holyWorldPostSellWalkUntilMs = 0L;
        holyWorldStageStartedMs = 0L;
        holyWorldNextMoveSwitchMs = 0L;
        holyWorldMoveBack = false;
        holyWorldMoveStrafeLeft = false;
        holyWorldMoveStrafeRight = false;
        holyWorldMoveJump = false;
        holyWorldMoveSprint = true;
        holyWorldNextLookDelayMs = 0L;
        holyWorldLastRelogRefreshMark = 0L;
        holyWorldNextRelogRefreshTarget = randomRelogRefreshTarget();
        holyWorldSafeRefreshPauseUntilMs = 0L;
        holyWorldSafeRefreshBurstClicks = 0;
        holyWorldSafeRefreshBurstTarget = 0;
        holyWorldNormalRefreshPauseUntilMs = 0L;
        holyWorldNormalRefreshBurstClicks = 0;
        holyWorldNormalRefreshBurstTarget = 0;
        holyWorldNeedAuctionReopen = false;
        clearHolyWorldPendingRctSequence();
        holyWorldWalkStartPos = Vec3d.ZERO;
        holyWorldStepBackStartPos = Vec3d.ZERO;
        holyWorldCurrentWalkTarget = Vec3d.ZERO;
        holyWorldRoutePointOneTarget = HOLYWORLD_ROUTE_POINT_ONE;
        holyWorldRoutePointTwoTarget = HOLYWORLD_ROUTE_POINT_TWO;
        holyWorldRouteStep = 0;
        holyWorldRouteStepStartedMs = 0L;
        holyWorldRouteInspectUntilMs = 0L;
        holyWorldRouteReturnTarget = Vec3d.ZERO;
        holyWorldAuctionReopenWatch.reset();
        holyWorldLookWatch.reset();
        resetHolyWorldAutoSellState();
        holyWorldAutoSellNoFilterBackoffUntilMs = 0L;
        resetHolyWorldTimedSellState();
        resetHolyWorldPeriodicBreakState();
        stopScriptedLookRoutine(System.currentTimeMillis());
        clearHolyWorldStaffEscapeState();
        clearVisibleChatTypingState();
        commandRateWindow.clear();
        commandLastSentMs = 0L;
        slotRateWindow.clear();
        slotLastClickMs = 0L;
        releaseHolyWorldMovementKeys();
        stopHolyWorldRotation();
        resolvePendingHistoryEntry(false, ItemStack.EMPTY);
        clearHolyWorldPendingConfirmation();
    }

    private void ensureDebugAbSession(long now) {
        if (!isDebugAbEnabled() || debugAbLogPath != null) {
            return;
        }

        try {
            Path baseDir;
            if (mc != null && mc.runDirectory != null) {
                baseDir = Path.of(mc.runDirectory.getAbsolutePath(), "zov4ik", "Files", "AutoBuyDebug");
            } else {
                baseDir = Path.of("run", "zov4ik", "Files", "AutoBuyDebug");
            }
            Files.createDirectories(baseDir);
            String fileName = "ab-debug-" + LocalDateTime.now(ZoneId.systemDefault()).format(DEBUG_AB_FILE_FORMAT) + ".log";
            debugAbLogPath = baseDir.resolve(fileName);
            debugAbStartedMs = now;
            debugAbLastHeartbeatMs = 0L;
            debugAbBanLogged = false;
            debugAbLastAutoSellState = holyWorldAutoSellState;
            debugAbLastTimedSellState = holyWorldTimedSellState;
            debugAbLastWalkState = holyWorldWalkState;
            debugAbLastBreakState = holyWorldPeriodicBreakState;
            debugAbLastAnarchy = getCurrentHolyWorldAnarchyFromScoreboard();
            debugAbQueuedCommands = 0L;
            debugAbSentCommands = 0L;
            debugAbLastCommandSentMs = 0L;
            debugAbCommandBurstWindowStartedMs = 0L;
            debugAbCommandBurstCount = 0L;
            debugAbLastTypedCommandDurationMs = 0L;
            debugAbSlotClickAttempts = 0L;
            debugAbSlotClickSuccess = 0L;
            debugAbSlotClickFailures = 0L;
            debugAbAutoSellMoveAttempts = 0L;
            debugAbAutoSellMoveFailures = 0L;
            debugAbRefreshDeltaMinMs = 0L;
            debugAbRefreshDeltaMaxMs = 0L;
            debugAbRefreshDeltaTotalMs = 0L;
            debugAbRefreshDeltaSamples = 0L;
            debugAbLastYaw = Float.NaN;
            debugAbLastPitch = Float.NaN;
            debugAbMaxYawJump = 0.0F;
            debugAbMaxPitchJump = 0.0F;
            debugAbYawJumpTotalScaled = 0L;
            debugAbPitchJumpTotalScaled = 0L;
            debugAbRotationSamples = 0L;
            debugAbMovementMask = -1;
            debugAbMovementMaskChangedMs = 0L;
            debugAbMovementMaskChanges = 0L;
            debugAbRecentEvents.clear();

            logDebugAb("SESSION_START",
                    "version=ab_debug_v3"
                            + ", mode=" + serverMode.getSelected()
                            + ", autosell=" + autoSellMode.getSelected()
                            + ", trigger=" + autoSellTriggerMode.getSelected()
                            + ", safe=" + isSafeModeEnabled()
                            + ", refresh_target=" + holyWorldNextRelogRefreshTarget);
        } catch (Exception ignored) {
            debugAbLogPath = null;
        }
    }

    private void closeDebugAbSession(String reason, long now) {
        if (debugAbLogPath == null) {
            return;
        }
        logDebugAb("SESSION_END",
                "reason=" + reason
                        + ", duration=" + formatDuration(Math.max(0L, now - debugAbStartedMs))
                        + ", refresh=" + refreshCount
                        + ", buys=" + buyClicks
                        + ", cmd_queued=" + debugAbQueuedCommands
                        + ", cmd_sent=" + debugAbSentCommands
                        + ", slot_click_ok=" + debugAbSlotClickSuccess
                        + ", slot_click_fail=" + debugAbSlotClickFailures
                        + ", autosell_swap_fail=" + debugAbAutoSellMoveFailures
                        + ", ban_marker=" + debugAbBanLogged);
        debugAbLogPath = null;
        debugAbStartedMs = 0L;
        debugAbLastHeartbeatMs = 0L;
        debugAbBanLogged = false;
        debugAbLastAutoSellState = null;
        debugAbLastTimedSellState = null;
        debugAbLastWalkState = null;
        debugAbLastBreakState = null;
        debugAbLastAnarchy = -1;
        debugAbQueuedCommands = 0L;
        debugAbSentCommands = 0L;
        debugAbLastCommandSentMs = 0L;
        debugAbCommandBurstWindowStartedMs = 0L;
        debugAbCommandBurstCount = 0L;
        debugAbLastTypedCommandDurationMs = 0L;
        debugAbSlotClickAttempts = 0L;
        debugAbSlotClickSuccess = 0L;
        debugAbSlotClickFailures = 0L;
        debugAbAutoSellMoveAttempts = 0L;
        debugAbAutoSellMoveFailures = 0L;
        debugAbRefreshDeltaMinMs = 0L;
        debugAbRefreshDeltaMaxMs = 0L;
        debugAbRefreshDeltaTotalMs = 0L;
        debugAbRefreshDeltaSamples = 0L;
        debugAbLastYaw = Float.NaN;
        debugAbLastPitch = Float.NaN;
        debugAbMaxYawJump = 0.0F;
        debugAbMaxPitchJump = 0.0F;
        debugAbYawJumpTotalScaled = 0L;
        debugAbPitchJumpTotalScaled = 0L;
        debugAbRotationSamples = 0L;
        debugAbMovementMask = -1;
        debugAbMovementMaskChangedMs = 0L;
        debugAbMovementMaskChanges = 0L;
        debugAbRecentEvents.clear();
    }

    private void recordDebugRefreshDelta(long deltaMs) {
        if (deltaMs <= 0L) {
            return;
        }
        if (debugAbRefreshDeltaSamples <= 0L) {
            debugAbRefreshDeltaMinMs = deltaMs;
            debugAbRefreshDeltaMaxMs = deltaMs;
            debugAbRefreshDeltaTotalMs = deltaMs;
            debugAbRefreshDeltaSamples = 1L;
            return;
        }
        debugAbRefreshDeltaMinMs = Math.min(debugAbRefreshDeltaMinMs, deltaMs);
        debugAbRefreshDeltaMaxMs = Math.max(debugAbRefreshDeltaMaxMs, deltaMs);
        debugAbRefreshDeltaTotalMs += deltaMs;
        debugAbRefreshDeltaSamples++;
    }

    private void trackDebugAbMovementAndRotation(long now) {
        if (mc == null || mc.player == null) {
            return;
        }

        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        if (!Float.isNaN(debugAbLastYaw)) {
            float yawDelta = Math.abs(MathHelper.wrapDegrees(yaw - debugAbLastYaw));
            float pitchDelta = Math.abs(pitch - debugAbLastPitch);
            debugAbMaxYawJump = Math.max(debugAbMaxYawJump, yawDelta);
            debugAbMaxPitchJump = Math.max(debugAbMaxPitchJump, pitchDelta);
            debugAbYawJumpTotalScaled += Math.round(yawDelta * 1000.0F);
            debugAbPitchJumpTotalScaled += Math.round(pitchDelta * 1000.0F);
            debugAbRotationSamples++;
            if (yawDelta >= 45.0F || pitchDelta >= 30.0F) {
                logDebugAb("ROT_JUMP",
                        "yaw_delta=" + String.format(Locale.ROOT, "%.2f", yawDelta)
                                + ", pitch_delta=" + String.format(Locale.ROOT, "%.2f", pitchDelta)
                                + ", yaw=" + String.format(Locale.ROOT, "%.2f", yaw)
                                + ", pitch=" + String.format(Locale.ROOT, "%.2f", pitch));
            }
        }
        debugAbLastYaw = yaw;
        debugAbLastPitch = pitch;

        int currentMask = getCurrentMovementMask();
        if (debugAbMovementMask != currentMask) {
            long heldForMs = debugAbMovementMaskChangedMs > 0L
                    ? Math.max(0L, now - debugAbMovementMaskChangedMs)
                    : 0L;
            logDebugAb("MOVE_KEYS",
                    "from=" + formatDebugMovementMask(debugAbMovementMask)
                            + ", to=" + formatDebugMovementMask(currentMask)
                            + ", held_ms=" + heldForMs);
            debugAbMovementMask = currentMask;
            debugAbMovementMaskChangedMs = now;
            debugAbMovementMaskChanges++;
        }
    }

    private int getCurrentMovementMask() {
        if (mc == null || mc.options == null) {
            return 0;
        }
        int mask = 0;
        if (mc.options.forwardKey.isPressed()) {
            mask |= 1;
        }
        if (mc.options.backKey.isPressed()) {
            mask |= 1 << 1;
        }
        if (mc.options.leftKey.isPressed()) {
            mask |= 1 << 2;
        }
        if (mc.options.rightKey.isPressed()) {
            mask |= 1 << 3;
        }
        if (mc.options.jumpKey.isPressed()) {
            mask |= 1 << 4;
        }
        if (mc.options.sprintKey.isPressed()) {
            mask |= 1 << 5;
        }
        return mask;
    }

    private String formatDebugMovementMask(int mask) {
        if (mask < 0) {
            return "-";
        }
        return (isMaskBitSet(mask, 0) ? "F" : "-")
                + (isMaskBitSet(mask, 1) ? "B" : "-")
                + (isMaskBitSet(mask, 2) ? "L" : "-")
                + (isMaskBitSet(mask, 3) ? "R" : "-")
                + (isMaskBitSet(mask, 4) ? "J" : "-")
                + (isMaskBitSet(mask, 5) ? "S" : "-");
    }

    private boolean isMaskBitSet(int mask, int bit) {
        return (mask & (1 << bit)) != 0;
    }

    private boolean clickSlotWithDebug(int syncId, int slotId, int button, SlotActionType action, String source) {
        if (mc == null || mc.player == null || mc.interactionManager == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        pauseHolyWorldTestAuctionWalkForClick(now, source);
        debugAbSlotClickAttempts++;
        if (!canClickSlotNow(now, source)) {
            logDebugAb("SLOT_THROTTLE",
                    "source=" + source
                            + ", window=" + slotRateWindow.size()
                            + ", safe=" + isSafeModeEnabled());
            return false;
        }
        try {
            mc.interactionManager.clickSlot(syncId, slotId, button, action, mc.player);
            debugAbSlotClickSuccess++;
            if (!"auction_refresh".equals(source)) {
                logDebugAb("SLOT_CLICK",
                        "source=" + source
                                + ", sync=" + syncId
                                + ", slot=" + slotId
                                + ", button=" + button
                                + ", action=" + action
                                + ", screen=" + getCurrentScreenTitleSafe());
            }
            return true;
        } catch (Exception exception) {
            debugAbSlotClickFailures++;
            logDebugAb("SLOT_CLICK_FAIL",
                    "source=" + source
                            + ", sync=" + syncId
                            + ", slot=" + slotId
                            + ", button=" + button
                            + ", action=" + action
                            + ", screen=" + getCurrentScreenTitleSafe()
                            + ", error=" + exception.getClass().getSimpleName());
            return false;
        }
    }

    private void pauseHolyWorldTestAuctionWalkForClick(long now, String source) {
        if (!isHolyWorldMode() || !isAutoBuyWorkTest() || !(mc.currentScreen instanceof GenericContainerScreen screen) || !isAuctionScreen(screen)) {
            return;
        }

        long pauseMs = "auction_refresh".equals(source)
                ? randomBetween(360L, 820L)
                : randomBetween(190L, 540L);
        holyWorldTestWalkPauseUntilMs = Math.max(holyWorldTestWalkPauseUntilMs, now + pauseMs);
        holyWorldTestWalkRestUntilMs = Math.max(
                holyWorldTestWalkRestUntilMs,
                holyWorldTestWalkPauseUntilMs + randomBetween(140L, 420L)
        );
        releaseHolyWorldMovementKeys();
    }

    private boolean canClickSlotNow(long now, String source) {
        cleanupSlotRateWindow(now);
        long minInterval = isSafeModeEnabled() ? SLOT_SAFE_MIN_INTERVAL_MS : SLOT_MIN_INTERVAL_MS;
        int maxInWindow = isSafeModeEnabled() ? SLOT_RATE_SAFE_MAX : SLOT_RATE_MAX;

        if ("auction_refresh".equals(source)) {
            // Минимальный интервал между кликами обновления (мс). Ниже — быстрее.
            minInterval = Math.max(minInterval, isSafeModeEnabled() ? 220L : 90L);
        }

        if (slotLastClickMs > 0L && now - slotLastClickMs < minInterval) {
            return false;
        }
        if (slotRateWindow.size() >= maxInWindow) {
            return false;
        }

        slotRateWindow.addLast(now);
        slotLastClickMs = now;
        return true;
    }

    private void cleanupSlotRateWindow(long now) {
        long border = now - SLOT_RATE_WINDOW_MS;
        while (!slotRateWindow.isEmpty()) {
            Long at = slotRateWindow.peekFirst();
            if (at == null || at < border) {
                slotRateWindow.pollFirst();
                continue;
            }
            break;
        }
    }

    private String getCurrentScreenTitleSafe() {
        if (mc == null || !(mc.currentScreen instanceof GenericContainerScreen screen)) {
            return "none";
        }
        String title = AutoBuyItem.normalizeLine(screen.getTitle().getString());
        return title.isBlank() ? "container" : title;
    }

    private void logDebugAbRecentWindow(long now, String reason) {
        if (debugAbLogPath == null || debugAbRecentEvents.isEmpty()) {
            return;
        }
        long border = now - DEBUG_AB_PREBAN_WINDOW_MS;
        String stamp = LocalDateTime.now(ZoneId.systemDefault()).format(DEBUG_AB_TIME_FORMAT);
        appendDebugAbRawLine("[" + stamp + "] PREBAN_BUFFER_BEGIN | reason="
                + (reason == null || reason.isBlank() ? "unknown" : reason)
                + ", window_ms=" + DEBUG_AB_PREBAN_WINDOW_MS
                + ", cached=" + debugAbRecentEvents.size());
        int added = 0;
        for (DebugAbRecentEvent event : debugAbRecentEvents) {
            if (event == null || event.line == null || event.line.isBlank()) {
                continue;
            }
            if (event.atMs < border) {
                continue;
            }
            appendDebugAbRawLine(event.line);
            added++;
        }
        appendDebugAbRawLine("[" + stamp + "] PREBAN_BUFFER_END | captured=" + added);
    }

    private void logDebugAb(String event, String details) {
        if (debugAbLogPath == null) {
            return;
        }
        long atMs = System.currentTimeMillis();
        String safeDetails = details == null ? "" : details.replace('\n', ' ').replace('\r', ' ').trim();
        String line = "[" + LocalDateTime.now(ZoneId.systemDefault()).format(DEBUG_AB_TIME_FORMAT) + "] "
                + event
                + " | t=" + formatDuration(Math.max(0L, atMs - debugAbStartedMs))
                + (safeDetails.isEmpty() ? "" : " | " + safeDetails);
        appendDebugAbRawLine(line);
        cacheDebugAbRecentEvent(atMs, line);
    }

    private void appendDebugAbRawLine(String line) {
        if (debugAbLogPath == null || line == null || line.isBlank()) {
            return;
        }
        try {
            Files.writeString(
                    debugAbLogPath,
                    line + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
        }
    }

    private void cacheDebugAbRecentEvent(long atMs, String line) {
        if (line == null || line.isBlank()) {
            return;
        }
        debugAbRecentEvents.addLast(new DebugAbRecentEvent(atMs, line));
        while (debugAbRecentEvents.size() > DEBUG_AB_RECENT_EVENTS_LIMIT) {
            debugAbRecentEvents.pollFirst();
        }
        long border = atMs - (DEBUG_AB_PREBAN_WINDOW_MS * 2L);
        while (!debugAbRecentEvents.isEmpty()) {
            DebugAbRecentEvent head = debugAbRecentEvents.peekFirst();
            if (head == null || head.atMs < border) {
                debugAbRecentEvents.pollFirst();
                continue;
            }
            break;
        }
    }

    private long randomBetween(long minInclusive, long maxInclusive) {
        return ThreadLocalRandom.current().nextLong(minInclusive, maxInclusive + 1L);
    }

    private long pickHolyWorldTestScanDelayMs() {
        long delay = randomBetween(14L, 120L);
        if (ThreadLocalRandom.current().nextDouble() < 0.22D) {
            delay += randomBetween(40L, 240L);
        }
        if (ThreadLocalRandom.current().nextDouble() < 0.06D) {
            delay += randomBetween(260L, 760L);
        }
        return Math.max(10L, delay);
    }

    private long pickHolyWorldTestRefreshDelayMs() {
        long delay = randomBetween(HOLYWORLD_TEST_REFRESH_MIN_DELAY_MS, HOLYWORLD_TEST_REFRESH_MAX_DELAY_MS);
        if (ThreadLocalRandom.current().nextDouble() < 0.34D) {
            delay += randomBetween(80L, 420L);
        }
        if (ThreadLocalRandom.current().nextDouble() < 0.08D) {
            delay += randomBetween(520L, 1_800L);
        }
        return delay;
    }

    private long weightedBetween(long minInclusive, long maxInclusive, double exponent) {
        if (maxInclusive <= minInclusive) {
            return minInclusive;
        }
        double u = ThreadLocalRandom.current().nextDouble();
        double scaled = Math.pow(u, exponent);
        long span = maxInclusive - minInclusive;
        return minInclusive + Math.round(span * scaled);
    }

    private long delayFromActionStamp(long stampMs, long minInclusive, long maxInclusive) {
        if (maxInclusive <= minInclusive) {
            return minInclusive;
        }
        long key = stampMs > 0L ? stampMs : (System.nanoTime() ^ ThreadLocalRandom.current().nextLong());
        long mixed = mix64(key ^ 0x9E3779B97F4A7C15L);
        long span = maxInclusive - minInclusive + 1L;
        return minInclusive + Math.floorMod(mixed, span);
    }

    private long mix64(long value) {
        long z = value + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private long pickHolyWorldMainWalkDurationMs() {
        int cycle = Math.max(0, holyWorldRouteDurationCycle);
        if (cycle == 0) {
            return HOLYWORLD_RANDOM_WALK_MAX_MS;
        }
        long drop = Math.min(14_000L, cycle * 850L);
        long adaptiveMax = Math.max(HOLYWORLD_RANDOM_WALK_MIN_MS + 2_000L, HOLYWORLD_RANDOM_WALK_MAX_MS - drop);
        long adaptiveMin = Math.max(HOLYWORLD_RANDOM_WALK_MIN_MS, adaptiveMax - 8_000L);
        return weightedBetween(adaptiveMin, adaptiveMax, 0.72D);
    }

    private long pickHolyWorldPostSellWalkDurationMs() {
        if (ThreadLocalRandom.current().nextDouble() < 0.74D) {
            return randomBetween(HOLYWORLD_POST_SELL_WALK_MIN_MS, 10_000L);
        }
        return weightedBetween(HOLYWORLD_POST_SELL_WALK_MIN_MS, HOLYWORLD_POST_SELL_WALK_MAX_MS, 1.8D);
    }

    private long pickHolyWorldPeriodicBreakIntervalMs() {
        long value = weightedBetween(HOLYWORLD_PERIODIC_BREAK_INTERVAL_MIN_MS, HOLYWORLD_PERIODIC_BREAK_INTERVAL_MAX_MS, 1.12D);
        if (ThreadLocalRandom.current().nextDouble() < 0.09D) {
            value = randomBetween(HOLYWORLD_PERIODIC_BREAK_INTERVAL_MIN_MS, HOLYWORLD_PERIODIC_BREAK_INTERVAL_MAX_MS);
        }
        return value;
    }

    private long pickHolyWorldPeriodicBreakPauseMs() {
        long value = weightedBetween(HOLYWORLD_PERIODIC_BREAK_PAUSE_MIN_MS, HOLYWORLD_PERIODIC_BREAK_PAUSE_MAX_MS, 0.92D);
        if (ThreadLocalRandom.current().nextDouble() < 0.12D) {
            value += randomBetween(8_000L, 32_000L);
        }
        return Math.max(HOLYWORLD_PERIODIC_BREAK_PAUSE_MIN_MS, Math.min(value, HOLYWORLD_PERIODIC_BREAK_PAUSE_MAX_MS + 32_000L));
    }

    private int randomRelogRefreshTarget() {
        if (isSafeModeEnabled()) {
            int value = (int) weightedBetween(520L, 760L, 0.96D);
            if (ThreadLocalRandom.current().nextDouble() < 0.12D) {
                value = ThreadLocalRandom.current().nextInt(520, 761);
            }
            return Math.max(520, Math.min(value, 760));
        }
        int value = (int) weightedBetween(HOLYWORLD_REFRESHES_BEFORE_RELOG_MIN, HOLYWORLD_REFRESHES_BEFORE_RELOG_MAX, 0.82D);
        if (ThreadLocalRandom.current().nextDouble() < 0.10D) {
            value = ThreadLocalRandom.current().nextInt(HOLYWORLD_REFRESHES_BEFORE_RELOG_MIN, HOLYWORLD_REFRESHES_BEFORE_RELOG_MAX + 1);
        }
        return MathHelper.clamp(value, HOLYWORLD_REFRESHES_BEFORE_RELOG_MIN, HOLYWORLD_REFRESHES_BEFORE_RELOG_MAX);
    }

    private void stopHolyWorldRotation() {
        holyWorldNextLookDelayMs = 0L;
        holyWorldLookWatch.reset();
        RotationController.INSTANCE.reset();
    }
}



