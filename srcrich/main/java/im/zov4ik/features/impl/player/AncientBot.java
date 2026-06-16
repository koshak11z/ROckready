package im.zov4ik.features.impl.player;

import im.zov4ik.events.player.TickEvent;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.features.impl.render.AncientXrayV2;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.interactions.item.ItemToolkit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AncientBot extends Module {
    private static final int BLAST_SEARCH_RADIUS = 80;
    private static final int BLAST_STEP = 8;
    private static final long COMMAND_DELAY_MS = 900L;
    private static final long PLACE_DELAY_MS = 900L;
    private static final long EXPLOSION_WAIT_MS = 6500L;
    private static final long MINE_TIMEOUT_MS = 8500L;
    private static final int MAX_MINE_ATTEMPTS = 3;
    private static final long STUCK_CHECK_MS = 3500L;
    private static final long NO_SAFE_SPOT_MESSAGE_MS = 7000L;
    private static final int EXPLORATION_DISTANCE = 48;
    private static final int MAX_TERRAIN_SEARCH_FAILURES = 3;
    private static final int MIN_BLAST_Y = 35;
    private static final int MAX_TNT_PLACE_ATTEMPTS = 8;
    private static final int FIRE_RESISTANCE_REFRESH_TICKS = 20 * 20;
    private static final long POTION_DRINK_MS = 3200L;
    private static final long TNT_CONFIRM_FALLBACK_MS = 2400L;
    private static final long ITEM_USE_RETRY_MS = 650L;
    private static final long MOVEMENT_NUDGE_MS = 6500L;
    private static final float MAX_YAW_STEP = 6.0F;
    private static final float MAX_PITCH_STEP = 4.0F;
    private static final float ROTATION_DEAD_ZONE = 1.5F;
    private static final double DEBRIS_INTERRUPT_RANGE_SQ = 6400.0D;
    private static final double DEBRIS_SWAP_IMPROVEMENT_SQ = 64.0D;
    private static final long LAVA_STUCK_RTP_MS = 60_000L;
    private static final long LAVA_RTP_COOLDOWN_MS = 30_000L;
    private static final long BALANCE_CHECK_TIMEOUT_MS = 6_000L;
    private static final long REQUIRED_BALANCE = 250_000L;
    private static final long GOTO_REPEAT_MS = 2500L;
    private static final long SAME_COMMAND_THROTTLE_MS = 800L;
    private static final long ANTI_AFK_HOLD_MS = 500L;
    private static final long ANTI_AFK_COOLDOWN_MS = 3000L;
    private static final java.util.regex.Pattern MONEY_PATTERN = java.util.regex.Pattern.compile("([0-9][0-9.,\\s]{2,})");

    private final Set<BlockPos> usedBlastPositions = new HashSet<>();
    private final Set<BlockPos> finishedDebris = new HashSet<>();
    private final Set<BlockPos> failedDebris = new HashSet<>();

    private Phase phase = Phase.STOPPED;
    private BlockPos blastPos;
    private BlockPos targetDebris;
    private BlockPos explorationPos;
    private BlockPos navigationTarget;
    private BlockPos pendingTntPos;
    private BlockPos serverConfirmedTntPos;
    private long phaseStarted;
    private long lastCommand;
    private long potionStarted;
    private long eatingStarted;
    private long lastUseRetry;
    private long lastMovementNudge;
    private long tntPlacedAt;
    private long serverConfirmedTntAt;
    private long lastGotoSentAt;
    private long lastCommandSentAt;
    private String lastCommandText = "";
    private long antiAfkUntil;
    private long lastAntiAfkAt;
    private long pendingRtpRetryAt;
    private String pendingRtpCommand;
    private int tierWhiteSlot = -1;
    private int flintSlot = -1;
    private int blockSlot = -1;
    private int foodSlot = -1;
    private int pickaxeSlot = -1;
    private int fireResistanceSlot = -1;
    private int previousSlot = -1;
    private String lastPlacementFailure = "";
    private long lastStuckCheck;
    private long lastNoSafeSpotMessage;
    private double lastGoalDistance = Double.MAX_VALUE;
    private int stuckTicks;
    private int explorationAttempts;
    private int failedTerrainSearches;
    private int mineAttempts;
    private int tntPlaceAttempts;
    private int stableTntTicks;
    private boolean forcedBlastPosition;
    private boolean pausedForEating;
    private boolean pausedForPotion;
    private boolean drinkingPotion;
    private boolean directMiningStarted;
    private boolean rotatingForAction;
    private long lavaSinceMs;
    private long lastRtpAt;
    private long balanceRequestedAt;
    private long detectedBalance = -1L;
    private boolean balanceChecked;
    private int rtpStage;

    public AncientBot() {
        super("AncientBot", "Ancient Bot", ModuleCategory.PLAYER);
    }

    public BlockPos getTargetDebris() {
        return targetDebris;
    }

    public BlockPos getVisualTarget() {
        if (navigationTarget != null) {
            return navigationTarget;
        }
        return switch (phase) {
            case FINDING_BLAST, GOING_BLAST, PLACING_TNT, IGNITING_TNT, RETREATING, WAITING_EXPLOSION -> blastPos;
            case EXPLORING_TERRAIN -> explorationPos;
            case FINDING_DEBRIS, GOING_DEBRIS, MINING_DEBRIS -> targetDebris;
            default -> null;
        };
    }

    @Override
    public void activate() {
        reset();
        phase = Phase.CHECKING;
        phaseStarted = System.currentTimeMillis();
        sendStatus("Запуск проверки условий", Formatting.AQUA);
    }

    @Override
    public void deactivate() {
        stopEating();
        stopPotion();
        sendBaritone("#stop", true);
        reset();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.getNetworkHandler() == null) {
            return;
        }

        tickAntiAfk();

        autoEat();
        if (pausedForEating) {
            return;
        }

        autoFireResistance();
        if (pausedForPotion) {
            return;
        }

        if (handleLavaEscape()) {
            return;
        }

        if (handleBasaltBiome()) {
            return;
        }

        if (tryInterruptForVisibleDebris()) {
            return;
        }

        switch (phase) {
            case CHECKING -> tickChecking();
            case FINDING_BLAST -> tickFindingBlast();
            case EXPLORING_TERRAIN -> tickExploringTerrain();
            case GOING_BLAST -> tickGoingBlast();
            case PLACING_TNT -> tickPlacingTnt();
            case IGNITING_TNT -> tickIgnitingTnt();
            case RETREATING -> tickRetreating();
            case WAITING_EXPLOSION -> tickWaitingExplosion();
            case FINDING_DEBRIS -> tickFindingDebris();
            case GOING_DEBRIS -> tickGoingDebris();
            case MINING_DEBRIS -> tickMiningDebris();
            case STOPPED -> {
                stopEating();
                stopPotion();
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (event.getType() != PacketEvent.Type.RECEIVE) {
            return;
        }

        if (event.getPacket() instanceof GameMessageS2CPacket msg) {
            handleChatBalance(msg);
        }

        if (pendingTntPos == null) {
            return;
        }

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            handleServerTntState(packet.getPos(), packet.getState());
        } else if (event.getPacket() instanceof ChunkDeltaUpdateS2CPacket packet) {
            packet.visitUpdates(this::handleServerTntState);
        }
    }

    private void handleChatBalance(GameMessageS2CPacket msg) {
        String text = msg.content().getString();
        if (text == null || text.isEmpty()) return;

        String lowered = text.toLowerCase();
        if (lowered.contains("афк") || lowered.contains("afk")) {
            triggerAntiAfk();
        }

        if (!balanceChecked && balanceRequestedAt != 0L
                && System.currentTimeMillis() - balanceRequestedAt <= BALANCE_CHECK_TIMEOUT_MS + 2000L) {
            java.util.regex.Matcher m = MONEY_PATTERN.matcher(text);
            long best = -1L;
            while (m.find()) {
                String raw = m.group(1).replaceAll("[\\s,.]", "");
                if (raw.isEmpty()) continue;
                try {
                    long value = Long.parseLong(raw);
                    if (value >= REQUIRED_BALANCE / 10 && value > best) best = value;
                } catch (NumberFormatException ignored) {}
            }
            if (best > 0L) detectedBalance = best;
        }
    }

    private void triggerAntiAfk() {
        long now = System.currentTimeMillis();
        if (now - lastAntiAfkAt < ANTI_AFK_COOLDOWN_MS) return;
        lastAntiAfkAt = now;
        antiAfkUntil = now + ANTI_AFK_HOLD_MS;
        if (mc.options != null) {
            mc.options.jumpKey.setPressed(true);
        }
        sendStatus("Сервер считает что я АФК, прыгаю и повторю команду", Formatting.YELLOW);
        if (pendingRtpCommand != null) {
            pendingRtpRetryAt = now + ANTI_AFK_HOLD_MS + 300L;
        }
    }

    private void tickAntiAfk() {
        if (antiAfkUntil > 0L && System.currentTimeMillis() >= antiAfkUntil) {
            antiAfkUntil = 0L;
            if (mc.options != null) {
                mc.options.jumpKey.setPressed(false);
            }
        }
        if (pendingRtpCommand != null && pendingRtpRetryAt > 0L
                && System.currentTimeMillis() >= pendingRtpRetryAt
                && mc.player != null && mc.player.networkHandler != null) {
            String cmd = pendingRtpCommand;
            mc.player.networkHandler.sendChatCommand(cmd);
            mc.player.networkHandler.sendChatCommand(cmd);
            pendingRtpCommand = null;
            pendingRtpRetryAt = 0L;
        }
    }

    private void sendRtpCommand(String command) {
        if (mc.player == null || mc.player.networkHandler == null) return;
        mc.player.networkHandler.sendChatCommand(command);
        mc.player.networkHandler.sendChatCommand(command);
        pendingRtpCommand = command;
        pendingRtpRetryAt = 0L;
    }

    private boolean isInNether() {
        return mc.world != null && mc.world.getRegistryKey() == World.NETHER;
    }

    private void tickChecking() {
        AncientXrayV2 xray = Instance.get(AncientXrayV2.class);
        if (xray != null && !xray.isState()) {
            xray.setState(true);
        }

        refreshSlots();
        long now = System.currentTimeMillis();

        if (!balanceChecked) {
            if (balanceRequestedAt == 0L) {
                balanceRequestedAt = now;
                detectedBalance = -1L;
                mc.player.networkHandler.sendChatCommand("money");
                sendStatus("Проверяю баланс через /money...", Formatting.AQUA);
                return;
            }
            if (detectedBalance < 0L && now - balanceRequestedAt < BALANCE_CHECK_TIMEOUT_MS) {
                return;
            }
            balanceChecked = true;
        }

        boolean balanceOk = detectedBalance >= REQUIRED_BALANCE;
        boolean xrayOk = xray != null && xray.isState();
        printChecklist(xrayOk, balanceOk);

        boolean requiredOk = xrayOk && balanceOk
                && tierWhiteSlot != -1 && flintSlot != -1
                && foodSlot != -1 && pickaxeSlot != -1 && fireResistanceSlot != -1;
        if (!requiredOk) {
            phase = Phase.STOPPED;
            String reason = !balanceOk
                    ? ("Нужно минимум " + REQUIRED_BALANCE + " на балансе (сейчас " + (detectedBalance < 0 ? "?" : detectedBalance) + ")")
                    : "Не хватает обязательных условий, бот остановлен";
            sendStatus(reason, Formatting.RED);
            return;
        }

        if (!isInNether()) {
            if (now - lastRtpAt >= LAVA_RTP_COOLDOWN_MS) {
                sendStatus("Не в аду, делаю /rtp nether x2", Formatting.YELLOW);
                sendRtpCommand("rtp nether");
                lastRtpAt = now;
            }
            return;
        }

        phase = Phase.FINDING_BLAST;
        phaseStarted = System.currentTimeMillis();
        sendStatus("Условия выполнены, ищу место для взрыва", Formatting.GREEN);
    }

    private boolean handleLavaEscape() {
        if (phase == Phase.STOPPED || phase == Phase.CHECKING) {
            lavaSinceMs = 0L;
            return false;
        }

        long now = System.currentTimeMillis();
        if (mc.player.isInLava()) {
            if (lavaSinceMs == 0L) lavaSinceMs = now;
        } else {
            lavaSinceMs = 0L;
        }

        if (lavaSinceMs == 0L) return false;
        if (now - lavaSinceMs < LAVA_STUCK_RTP_MS) return false;
        if (now - lastRtpAt < LAVA_RTP_COOLDOWN_MS) return false;

        sendBaritone("#stop", true);
        sendStatus("Застрял в лаве более минуты, делаю /rtp nether x2 и заново ищу территорию", Formatting.RED);
        sendRtpCommand("rtp nether");
        lastRtpAt = now;
        lavaSinceMs = 0L;
        clearTntConfirmation();
        navigationTarget = null;
        directMiningStarted = false;
        targetDebris = null;
        blastPos = null;
        explorationPos = null;
        usedBlastPositions.clear();
        failedTerrainSearches = 0;
        phase = Phase.FINDING_BLAST;
        phaseStarted = now;
        resetStuckCheck();
        return true;
    }

    private boolean handleBasaltBiome() {
        if (mc.world == null || mc.player == null) return false;
        if (phase != Phase.FINDING_BLAST && phase != Phase.EXPLORING_TERRAIN && phase != Phase.GOING_BLAST) {
            return false;
        }
        if (!mc.world.getBiome(mc.player.getBlockPos()).matchesKey(BiomeKeys.BASALT_DELTAS)) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - lastRtpAt < LAVA_RTP_COOLDOWN_MS) {
            return false;
        }

        sendBaritone("#stop", true);
        sendStatus("Базальтовый биом, не взрываю. Делаю /rtp nether x2 и заново ищу территорию", Formatting.RED);
        sendRtpCommand("rtp nether");
        lastRtpAt = now;
        clearTntConfirmation();
        navigationTarget = null;
        directMiningStarted = false;
        targetDebris = null;
        blastPos = null;
        explorationPos = null;
        usedBlastPositions.clear();
        failedTerrainSearches = 0;
        phase = Phase.FINDING_BLAST;
        phaseStarted = now;
        resetStuckCheck();
        return true;
    }

    private void tickFindingBlast() {
        if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
            startForcedBlast();
            return;
        }

        blastPos = findBestBlastPosition();
        if (blastPos == null) {
            failedTerrainSearches++;
            if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
                startForcedBlast();
                return;
            }
            startExploringTerrain();
            return;
        }

        forcedBlastPosition = false;
        usedBlastPositions.add(blastPos);
        sendStatus("Точка взрыва: " + format(blastPos), Formatting.GREEN);
        goTo(blastPos, true);
        phase = Phase.GOING_BLAST;
        phaseStarted = System.currentTimeMillis();
        resetStuckCheck();
    }

    private void startExploringTerrain() {
        if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
            startForcedBlast();
            return;
        }

        explorationPos = findExplorationPosition();
        long now = System.currentTimeMillis();
        if (now - lastNoSafeSpotMessage > NO_SAFE_SPOT_MESSAGE_MS) {
            sendStatus("Безопасной точки рядом нет, копаюсь в новую территорию: " + format(explorationPos), Formatting.YELLOW);
            lastNoSafeSpotMessage = now;
        }
        goTo(explorationPos, true);
        phase = Phase.EXPLORING_TERRAIN;
        phaseStarted = now;
        resetStuckCheck();
    }

    private void startForcedBlast() {
        sendBaritone("#stop", true);
        forcedBlastPosition = true;
        blastPos = findFallbackBlastPosition();

        if (blastPos == null) {
            blastPos = currentBlastHeightPos();
            sendStatus("Третий поиск провален, поднимаюсь на Y>=35 и ставлю таер вайт", Formatting.YELLOW);
            goTo(blastPos, true);
            phase = Phase.GOING_BLAST;
            phaseStarted = System.currentTimeMillis();
            resetStuckCheck();
            return;
        }

        usedBlastPositions.add(blastPos);
        sendStatus("Третий поиск провален, взрываю forced-точку: " + format(blastPos), Formatting.YELLOW);
        goTo(blastPos, true);
        phase = Phase.GOING_BLAST;
        phaseStarted = System.currentTimeMillis();
        resetStuckCheck();
    }

    private void tickExploringTerrain() {
        if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
            sendBaritone("#stop", true);
            startForcedBlast();
            return;
        }

        blastPos = findBestBlastPosition();
        if (blastPos != null) {
            forcedBlastPosition = false;
            sendBaritone("#stop", true);
            usedBlastPositions.add(blastPos);
            sendStatus("Нашел нормальную территорию, иду к точке: " + format(blastPos), Formatting.GREEN);
            goTo(blastPos, true);
            phase = Phase.GOING_BLAST;
            phaseStarted = System.currentTimeMillis();
            resetStuckCheck();
            return;
        }

        if (explorationPos == null || mc.player.getBlockPos().getSquaredDistance(explorationPos) <= 9.0D) {
            failedTerrainSearches++;
            if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
                sendBaritone("#stop", true);
                startForcedBlast();
                return;
            }
            phase = Phase.FINDING_BLAST;
            phaseStarted = System.currentTimeMillis();
            return;
        }

        if (isStuckTowards(explorationPos) || System.currentTimeMillis() - phaseStarted > 45000L) {
            sendBaritone("#stop", true);
            explorationPos = null;
            failedTerrainSearches++;
            if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
                startForcedBlast();
                return;
            }
            phase = Phase.FINDING_BLAST;
            phaseStarted = System.currentTimeMillis();
        }
    }

    private void tickGoingBlast() {
        if (blastPos == null) {
            phase = Phase.FINDING_BLAST;
            return;
        }

        if (mc.player.getBlockPos().getSquaredDistance(blastPos) <= 9.0D) {
            sendBaritone("#stop", true);
            phase = Phase.PLACING_TNT;
            phaseStarted = System.currentTimeMillis();
        } else if (isStuckTowards(blastPos)) {
            if (forcedBlastPosition) {
                if (mc.player.getBlockY() >= MIN_BLAST_Y) {
                    sendStatus("Не дошел до forced-точки, ставлю таер вайт там, где стою", Formatting.YELLOW);
                    blastPos = mc.player.getBlockPos();
                    sendBaritone("#stop", true);
                    phase = Phase.PLACING_TNT;
                } else {
                    blastPos = currentBlastHeightPos();
                    sendStatus("Forced-точка недоступна, сначала поднимаюсь на Y>=35", Formatting.YELLOW);
                    goTo(blastPos, true);
                }
                phaseStarted = System.currentTimeMillis();
                return;
            }
            sendStatus("Застрял по пути к точке, ищу следующую", Formatting.YELLOW);
            sendBaritone("#stop", true);
            usedBlastPositions.add(blastPos);
            failedTerrainSearches++;
            if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
                startForcedBlast();
                return;
            }
            phase = Phase.FINDING_BLAST;
            phaseStarted = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - phaseStarted > 55000L) {
            if (forcedBlastPosition) {
                if (mc.player.getBlockY() >= MIN_BLAST_Y) {
                    sendStatus("Forced-точка долго не достигается, ставлю таер вайт здесь", Formatting.YELLOW);
                    blastPos = mc.player.getBlockPos();
                    sendBaritone("#stop", true);
                    phase = Phase.PLACING_TNT;
                } else {
                    blastPos = currentBlastHeightPos();
                    sendStatus("Forced-точка долго не достигается, поднимаюсь на Y>=35", Formatting.YELLOW);
                    goTo(blastPos, true);
                }
                phaseStarted = System.currentTimeMillis();
                return;
            }
            sendStatus("Точка не достигнута, ищу следующую", Formatting.YELLOW);
            sendBaritone("#stop", true);
            usedBlastPositions.add(blastPos);
            failedTerrainSearches++;
            if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
                startForcedBlast();
                return;
            }
            phase = Phase.FINDING_BLAST;
            phaseStarted = System.currentTimeMillis();
        }
    }

    private void tickPlacingTnt() {
        if (System.currentTimeMillis() - phaseStarted < PLACE_DELAY_MS) return;
        if (!refreshSlots()) {
            phase = Phase.STOPPED;
            sendStatus("Предметы пропали из хотбара", Formatting.RED);
            return;
        }

        if (isTntConfirmed(blastPos)) {
            sendStatus("Таер вайт подтвержден сервером", Formatting.GREEN);
            failedTerrainSearches = 0;
            tntPlaceAttempts = 0;
            clearTntConfirmation();
            phase = Phase.IGNITING_TNT;
            phaseStarted = System.currentTimeMillis();
            return;
        }

        if (isPendingTntVisible(blastPos)) {
            phaseStarted = System.currentTimeMillis();
            return;
        }

        if (tntPlaceAttempts >= MAX_TNT_PLACE_ATTEMPTS) {
            sendStatus("TNT не подтвердился сервером после " + MAX_TNT_PLACE_ATTEMPTS + " попыток: " + lastPlacementFailure, Formatting.YELLOW);
            tntPlaceAttempts = 0;
            clearTntConfirmation();
            if (forcedBlastPosition) {
                blastPos = currentBlastHeightPos();
                phaseStarted = System.currentTimeMillis();
                return;
            }

            failedTerrainSearches++;
            if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
                startForcedBlast();
                return;
            }
            phase = Phase.FINDING_BLAST;
            phaseStarted = System.currentTimeMillis();
            return;
        }

        if (placeTierWhite() || rotatingForAction) {
            if (rotatingForAction) {
                rotatingForAction = false;
                return;
            }
            tntPlaceAttempts++;
            sendStatus("Поставил таер вайт, жду подтверждение сервера (" + tntPlaceAttempts + "/" + MAX_TNT_PLACE_ATTEMPTS + ")", Formatting.GREEN);
            phaseStarted = System.currentTimeMillis();
        } else {
            sendStatus("Не смог поставить таер вайт: " + lastPlacementFailure, Formatting.YELLOW);
            tntPlaceAttempts++;
            if (forcedBlastPosition) {
                blastPos = currentBlastHeightPos();
                phaseStarted = System.currentTimeMillis();
                return;
            }

            failedTerrainSearches++;
            if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
                startForcedBlast();
                return;
            }
            phase = Phase.FINDING_BLAST;
            phaseStarted = System.currentTimeMillis();
        }
    }

    private void tickIgnitingTnt() {
        if (System.currentTimeMillis() - phaseStarted < PLACE_DELAY_MS) return;
        selectSlot(flintSlot);
        mc.interactionManager.syncSelectedSlot();
        if (blastPos == null || !mc.world.getBlockState(blastPos).isOf(Blocks.TNT)) {
            if (forcedBlastPosition) {
                sendStatus("Forced TNT не найден, повторяю установку рядом", Formatting.YELLOW);
                blastPos = currentBlastHeightPos();
                phase = Phase.PLACING_TNT;
            } else {
                sendStatus("TNT не найден после установки, ищу новую точку", Formatting.YELLOW);
                failedTerrainSearches++;
                if (failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) {
                    startForcedBlast();
                    return;
                }
                phase = Phase.FINDING_BLAST;
            }
            phaseStarted = System.currentTimeMillis();
            return;
        }
        Direction side = bestClickSide(blastPos);
        Vec3d hitVec = Vec3d.ofCenter(blastPos).add(
                side.getOffsetX() * 0.5D,
                side.getOffsetY() * 0.5D,
                side.getOffsetZ() * 0.5D
        );
        if (!lookAt(hitVec)) {
            return;
        }
        BlockHitResult hit = new BlockHitResult(hitVec, side, blastPos, false);
        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        if (result.isAccepted()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            sendStatus("ПКМ огнивом отклонен, все равно отхожу", Formatting.YELLOW);
        }
        AncientXrayV2 xray = Instance.get(AncientXrayV2.class);
        if (xray != null) {
            xray.trustExplosionAround(blastPos);
        }
        sendStatus("Поджег TNT, отхожу", Formatting.GREEN);
        BlockPos retreat = findRetreatPosition();
        goTo(retreat, false);
        phase = Phase.RETREATING;
        phaseStarted = System.currentTimeMillis();
    }

    private void handleServerTntState(BlockPos pos, BlockState state) {
        if (pendingTntPos == null || !pendingTntPos.equals(pos)) {
            return;
        }

        if (state.isOf(Blocks.TNT)) {
            serverConfirmedTntPos = pos.toImmutable();
            serverConfirmedTntAt = System.currentTimeMillis();
        } else if (serverConfirmedTntPos != null && serverConfirmedTntPos.equals(pos)) {
            serverConfirmedTntPos = null;
            serverConfirmedTntAt = 0L;
        }
    }

    private boolean isTntConfirmed(BlockPos pos) {
        if (pos == null || !mc.world.getBlockState(pos).isOf(Blocks.TNT)) {
            stableTntTicks = 0;
            return false;
        }

        if (serverConfirmedTntPos != null && serverConfirmedTntPos.equals(pos)) {
            return true;
        }

        if (pendingTntPos != null && pendingTntPos.equals(pos) && System.currentTimeMillis() - tntPlacedAt >= TNT_CONFIRM_FALLBACK_MS) {
            stableTntTicks++;
            return stableTntTicks >= 4;
        }

        return false;
    }

    private boolean isPendingTntVisible(BlockPos pos) {
        return pos != null
                && pendingTntPos != null
                && pendingTntPos.equals(pos)
                && mc.world.getBlockState(pos).isOf(Blocks.TNT);
    }

    private void clearTntConfirmation() {
        pendingTntPos = null;
        serverConfirmedTntPos = null;
        serverConfirmedTntAt = 0L;
        tntPlacedAt = 0L;
        stableTntTicks = 0;
    }

    private void tickRetreating() {
        if (System.currentTimeMillis() - phaseStarted > 2600L) {
            sendBaritone("#stop", true);
            phase = Phase.WAITING_EXPLOSION;
            phaseStarted = System.currentTimeMillis();
        }
    }

    private void tickWaitingExplosion() {
        if (System.currentTimeMillis() - phaseStarted < EXPLOSION_WAIT_MS) return;
        phase = Phase.FINDING_DEBRIS;
        phaseStarted = System.currentTimeMillis();
    }

    private void tickFindingDebris() {
        targetDebris = nearestDebris();
        if (targetDebris == null) {
            sendStatus("Обломки рядом закончились, ищу новую местность", Formatting.AQUA);
            phase = Phase.FINDING_BLAST;
            phaseStarted = System.currentTimeMillis();
            return;
        }

        sendStatus("Иду к ближайшему обломку: " + format(targetDebris), Formatting.GREEN);
        goTo(targetDebris, true);
        phase = Phase.GOING_DEBRIS;
        phaseStarted = System.currentTimeMillis();
        resetStuckCheck();
    }

    private void tickGoingDebris() {
        if (targetDebris == null) {
            phase = Phase.FINDING_DEBRIS;
            return;
        }

        if (!isTrustedDebrisTarget(targetDebris)) {
            finishedDebris.add(targetDebris);
            sendBaritone("#stop", true);
            phase = Phase.FINDING_DEBRIS;
            return;
        }

        if (isCloseEnoughToMine(targetDebris) && isTrustedDebrisTarget(targetDebris)) {
            sendBaritone("#stop", true);
            directMiningStarted = false;
            phase = Phase.MINING_DEBRIS;
            phaseStarted = System.currentTimeMillis();
            mineAttempts = 1;
        } else if (isStuckTowards(targetDebris)) {
            markDebrisFailed(targetDebris);
            sendStatus("Застрял по пути к обломку, беру следующий", Formatting.YELLOW);
            sendBaritone("#stop", true);
            phase = Phase.FINDING_DEBRIS;
            phaseStarted = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - phaseStarted > 45000L) {
            markDebrisFailed(targetDebris);
            sendBaritone("#stop", true);
            phase = Phase.FINDING_DEBRIS;
            phaseStarted = System.currentTimeMillis();
        }
    }

    private void tickMiningDebris() {
        if (targetDebris == null || !isTrustedDebrisTarget(targetDebris)) {
            if (targetDebris != null) finishedDebris.add(targetDebris);
            sendBaritone("#stop", true);
            phase = Phase.FINDING_DEBRIS;
            phaseStarted = System.currentTimeMillis();
            mineAttempts = 0;
            directMiningStarted = false;
            return;
        }

        mineTargetDebris();

        if (System.currentTimeMillis() - phaseStarted > MINE_TIMEOUT_MS) {
            if (mineAttempts < MAX_MINE_ATTEMPTS) {
                mineAttempts++;
                sendStatus("Обломок не вскопался за 5 сек, пробую еще раз", Formatting.YELLOW);
                sendBaritone("#stop", true);
                directMiningStarted = false;
                phaseStarted = System.currentTimeMillis();
                return;
            }

            markDebrisFailed(targetDebris);
            sendStatus("Обломок не вскопался после 2 попыток, иду дальше", Formatting.YELLOW);
            sendBaritone("#stop", true);
            phase = Phase.FINDING_DEBRIS;
            phaseStarted = System.currentTimeMillis();
            mineAttempts = 0;
            directMiningStarted = false;
        }
    }

    private void mineTargetDebris() {
        if (targetDebris == null || mc.interactionManager == null || mc.player == null || mc.world == null) return;
        if (!isCloseEnoughToMine(targetDebris)) {
            sendBaritone("#stop", true);
            goTo(targetDebris, true);
            phase = Phase.GOING_DEBRIS;
            phaseStarted = System.currentTimeMillis();
            directMiningStarted = false;
            return;
        }

        if (pickaxeSlot != -1) {
            selectSlot(pickaxeSlot);
            mc.interactionManager.syncSelectedSlot();
        }

        Direction side = bestClickSide(targetDebris);
        if (!lookAt(Vec3d.ofCenter(targetDebris).add(
                side.getOffsetX() * 0.45D,
                side.getOffsetY() * 0.45D,
                side.getOffsetZ() * 0.45D
        ))) {
            return;
        }

        if (!directMiningStarted) {
            mc.interactionManager.attackBlock(targetDebris, side);
            directMiningStarted = true;
        } else {
            mc.interactionManager.updateBlockBreakingProgress(targetDebris, side);
        }
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean refreshSlots() {
        tierWhiteSlot = findHotbar(this::isTierWhiteTnt);
        flintSlot = findHotbar(stack -> stack.isOf(Items.FLINT_AND_STEEL));
        blockSlot = findHotbar(stack -> stack.getItem() instanceof BlockItem && !stack.isOf(Items.TNT) && stack.getCount() >= 64);
        foodSlot = findHotbar(stack -> stack.get(DataComponentTypes.FOOD) != null);
        pickaxeSlot = findHotbar(stack -> stack.getItem() instanceof PickaxeItem);
        fireResistanceSlot = findHotbar(this::isFireResistance);
        return tierWhiteSlot != -1 && flintSlot != -1 && foodSlot != -1 && pickaxeSlot != -1 && fireResistanceSlot != -1;
    }

    private int findHotbar(StackPredicate predicate) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && predicate.test(stack)) return i;
        }
        return -1;
    }

    private boolean isTierWhiteTnt(ItemStack stack) {
        if (!stack.isOf(Items.TNT)) return false;
        String name = stack.getName().getString().toLowerCase();
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        String custom = customData == null ? "" : customData.toString().toLowerCase();
        return custom.contains("tnt-tier-white") || custom.contains("twhite") || name.contains("tier white");
    }

    private boolean isFireResistance(ItemStack stack) {
        PotionContentsComponent component = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (component == null) return false;
        for (var effect : component.getEffects()) {
            if (effect.getEffectType().equals(StatusEffects.FIRE_RESISTANCE)) return true;
        }
        return false;
    }

    private void printChecklist(boolean xrayEnabled, boolean balanceOk) {
        sendCheck("AncientXrayV2", xrayEnabled, true);
        sendCheck("Таер вайт в хотбаре", tierWhiteSlot != -1, true);
        sendCheck("Огниво в хотбаре", flintSlot != -1, true);
        sendCheck("Любая еда в хотбаре", foodSlot != -1, true);
        sendCheck("Кирка в хотбаре", pickaxeSlot != -1, true);
        sendCheck("Огнестойкость в хотбаре", fireResistanceSlot != -1, true);
        String balanceText = "Баланс >= " + REQUIRED_BALANCE + " (сейчас " + (detectedBalance < 0 ? "?" : detectedBalance) + ")";
        sendCheck(balanceText, balanceOk, true);
        sendCheck("Любые блоки 64 шт в хотбаре", blockSlot != -1, false);
        sendCheck("Тотем бессмертия", hasItem(Items.TOTEM_OF_UNDYING), false);
        sendCheck("Золотая броня", hasGoldenArmor(), false);
    }

    private void sendCheck(String name, boolean ok, boolean required) {
        MutableText text = Text.literal("AncientBot -> ").formatted(Formatting.DARK_PURPLE)
                .append(Text.literal(ok ? "[OK] " : required ? "[NO] " : "[REC] ").formatted(ok ? Formatting.GREEN : required ? Formatting.RED : Formatting.YELLOW))
                .append(Text.literal(name).formatted(ok ? Formatting.GREEN : required ? Formatting.RED : Formatting.YELLOW));
        mc.player.sendMessage(text, false);
    }

    private void sendStatus(String message, Formatting color) {
        mc.player.sendMessage(Text.literal("AncientBot -> ").formatted(Formatting.DARK_PURPLE).append(Text.literal(message).formatted(color)), false);
    }

    private boolean hasItem(net.minecraft.item.Item item) {
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return true;
        }
        return false;
    }

    private boolean hasGoldenArmor() {
        return hasItem(Items.GOLDEN_HELMET) || hasItem(Items.GOLDEN_CHESTPLATE) || hasItem(Items.GOLDEN_LEGGINGS) || hasItem(Items.GOLDEN_BOOTS);
    }

    private BlockPos findBestBlastPosition() {
        BlockPos player = mc.player.getBlockPos();
        int y = getBlastSearchY();
        BlockPos best = null;
        int bestScore = Integer.MIN_VALUE;

        for (int r = BLAST_STEP; r <= BLAST_SEARCH_RADIUS; r += BLAST_STEP) {
            for (int dx = -r; dx <= r; dx += BLAST_STEP) {
                for (int dz = -r; dz <= r; dz += BLAST_STEP) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    BlockPos pos = new BlockPos(player.getX() + dx, y, player.getZ() + dz);
                    if (usedBlastPositions.stream().anyMatch(old -> old.getSquaredDistance(pos) < 225.0D)) continue;
                    if (!isBlastAreaSafe(pos)) continue;
                    int score = scoreBlastPosition(pos);
                    if (score > bestScore) {
                        bestScore = score;
                        best = pos;
                    }
                }
            }
        }

        return best;
    }

    private BlockPos findFallbackBlastPosition() {
        BlockPos player = mc.player.getBlockPos();
        int y = getBlastSearchY();
        BlockPos best = null;
        int bestScore = Integer.MIN_VALUE;

        for (int r = BLAST_STEP; r <= BLAST_SEARCH_RADIUS; r += BLAST_STEP) {
            for (int dx = -r; dx <= r; dx += BLAST_STEP) {
                for (int dz = -r; dz <= r; dz += BLAST_STEP) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    BlockPos pos = new BlockPos(player.getX() + dx, y, player.getZ() + dz);
                    if (findPlacementNear(pos, false, true, true) == null) continue;

                    int score = scoreBlastPosition(pos);
                    if (score > bestScore) {
                        bestScore = score;
                        best = pos;
                    }
                }
            }
        }

        return best;
    }

    private BlockPos findExplorationPosition() {
        BlockPos player = mc.player.getBlockPos();
        int y = getBlastSearchY();
        float yaw = mc.player.getYaw() + explorationAttempts * 90.0F;
        explorationAttempts++;
        double radians = Math.toRadians(yaw);
        int x = player.getX() + MathHelper.floor(-MathHelper.sin((float) radians) * EXPLORATION_DISTANCE);
        int z = player.getZ() + MathHelper.floor(MathHelper.cos((float) radians) * EXPLORATION_DISTANCE);
        return new BlockPos(x, y, z);
    }

    private int getBlastSearchY() {
        return MathHelper.clamp(Math.max(mc.player.getBlockY(), MIN_BLAST_Y), MIN_BLAST_Y, mc.world.getTopYInclusive() - 4);
    }

    private BlockPos currentBlastHeightPos() {
        BlockPos player = mc.player.getBlockPos();
        return new BlockPos(player.getX(), getBlastSearchY(), player.getZ());
    }

    private int scoreBlastPosition(BlockPos pos) {
        int score = 0;
        for (BlockPos scan : BlockPos.iterate(pos.add(-7, -4, -7), pos.add(7, 4, 7))) {
            BlockState state = mc.world.getBlockState(scan);
            if (state.isOf(Blocks.LAVA)) score -= scan.getSquaredDistance(pos) <= 16.0D ? 120 : 45;
            else if (state.isOf(Blocks.FIRE) || state.isOf(Blocks.SOUL_FIRE)) score -= 35;
            else if (state.isAir()) score -= 5;
            else if (state.isOf(Blocks.NETHERRACK) || state.isOf(Blocks.BLACKSTONE) || state.isOf(Blocks.BASALT)) score += 3;
            else score += 1;
        }
        if (!mc.world.getBlockState(pos).isAir()) score += 15;
        return score;
    }

    private boolean isBlastAreaSafe(BlockPos pos) {
        if (mc.world == null) return false;
        return findPlacementNear(pos, false, false, true) != null;
    }

    private boolean placeTierWhite() {
        if (blastPos == null) return false;
        Placement placement = findPlacementNear(blastPos, true, forcedBlastPosition);
        if (placement == null) {
            if (lastPlacementFailure.isEmpty()) lastPlacementFailure = "рядом нет свободной клетки с твердой опорой";
            return false;
        }

        selectSlot(tierWhiteSlot);
        mc.interactionManager.syncSelectedSlot();
        Vec3d hitVec = Vec3d.ofCenter(placement.support()).add(
                placement.side().getOffsetX() * 0.5D,
                placement.side().getOffsetY() * 0.5D,
                placement.side().getOffsetZ() * 0.5D
        );
        if (!lookAt(hitVec)) {
            lastPlacementFailure = "rotating";
            rotatingForAction = true;
            return false;
        }
        BlockHitResult hit = new BlockHitResult(hitVec, placement.side(), placement.support(), false);
        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        if (result.isAccepted()) {
            mc.player.swingHand(Hand.MAIN_HAND);
            pendingTntPos = placement.target().toImmutable();
            serverConfirmedTntPos = null;
            serverConfirmedTntAt = 0L;
            tntPlacedAt = System.currentTimeMillis();
            stableTntTicks = 0;
            blastPos = placement.target();
            return true;
        }
        lastPlacementFailure = "сервер отклонил ПКМ по " + format(placement.support()) + " side=" + placement.side();
        return false;
    }

    private Placement findPlacementNear(BlockPos center, boolean updateFailure) {
        return findPlacementNear(center, updateFailure, false, false);
    }

    private Placement findPlacementNear(BlockPos center, boolean updateFailure, boolean allowImmediateDanger) {
        return findPlacementNear(center, updateFailure, allowImmediateDanger, false);
    }

    private Placement findPlacementNear(BlockPos center, boolean updateFailure, boolean allowImmediateDanger, boolean ignoreReachDistance) {
        if (updateFailure) lastPlacementFailure = "";
        Placement best = null;
        double bestDistance = Double.MAX_VALUE;
        int blockedTargets = 0;
        int unsafeSupports = 0;
        int tooFarTargets = 0;

        for (BlockPos target : BlockPos.iterate(center.add(-2, -1, -2), center.add(2, 2, 2))) {
            if (target.getY() < MIN_BLAST_Y) {
                blockedTargets++;
                continue;
            }

            if (!canPlaceTntAt(target)) {
                blockedTargets++;
                continue;
            }

            if (!allowImmediateDanger && hasImmediateDanger(target)) {
                blockedTargets++;
                continue;
            }

            double distance = mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(target));
            if (!ignoreReachDistance && distance > 25.0D) {
                tooFarTargets++;
                continue;
            }

            for (Direction side : Direction.values()) {
                BlockPos support = target.offset(side.getOpposite());
                if (!isValidSupport(support, side)) {
                    unsafeSupports++;
                    continue;
                }

                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new Placement(support, side, target.toImmutable());
                }
            }
        }

        if (best == null && updateFailure) {
            lastPlacementFailure = "занято/небезопасно=" + blockedTargets + ", плохих опор=" + unsafeSupports + ", далеко=" + tooFarTargets;
        }
        return best;
    }

    private boolean canPlaceTntAt(BlockPos target) {
        BlockState state = mc.world.getBlockState(target);
        if (!state.isAir() && !state.isReplaceable()) return false;
        if (state.isOf(Blocks.LAVA) || state.isOf(Blocks.WATER)) return false;
        return !mc.player.getBoundingBox().intersects(new Box(target));
    }

    private boolean hasImmediateDanger(BlockPos target) {
        for (BlockPos scan : BlockPos.iterate(target.add(-1, -1, -1), target.add(1, 1, 1))) {
            BlockState state = mc.world.getBlockState(scan);
            if (state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE) || state.isOf(Blocks.SOUL_FIRE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidSupport(BlockPos support, Direction side) {
        BlockState state = mc.world.getBlockState(support);
        if (state.isAir() || state.isReplaceable() || state.isOf(Blocks.LAVA) || state.isOf(Blocks.WATER)) {
            return false;
        }
        return state.isSideSolidFullSquare(mc.world, support, side);
    }

    private Direction bestClickSide(BlockPos pos) {
        Vec3d delta = mc.player.getEyePos().subtract(Vec3d.ofCenter(pos));
        Direction side = Direction.getFacing(delta.x, delta.y, delta.z);
        return side == null ? Direction.UP : side;
    }

    private boolean lookAt(Vec3d point) {
        Vec3d delta = point.subtract(mc.player.getEyePos());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D);
        float targetPitch = (float) MathHelper.clamp(-Math.toDegrees(Math.atan2(delta.y, horizontal)), -90.0D, 90.0D);
        float yawDelta = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDelta = targetPitch - mc.player.getPitch();
        if (Math.abs(yawDelta) <= ROTATION_DEAD_ZONE && Math.abs(pitchDelta) <= ROTATION_DEAD_ZONE) {
            return true;
        }
        float yaw = mc.player.getYaw() + MathHelper.clamp(yawDelta, -MAX_YAW_STEP, MAX_YAW_STEP);
        float pitch = mc.player.getPitch() + MathHelper.clamp(pitchDelta, -MAX_PITCH_STEP, MAX_PITCH_STEP);
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
        return Math.abs(yawDelta) <= MAX_YAW_STEP && Math.abs(pitchDelta) <= MAX_PITCH_STEP;
    }

    private BlockPos findRetreatPosition() {
        Vec3d away = mc.player.getPos().subtract(Vec3d.ofCenter(blastPos));
        if (away.horizontalLengthSquared() < 0.1D) away = new Vec3d(1.0D, 0.0D, 0.0D);
        away = away.normalize().multiply(5.0D);
        return BlockPos.ofFloored(mc.player.getX() + away.x, mc.player.getY(), mc.player.getZ() + away.z);
    }

    private BlockPos nearestDebris() {
        AncientXrayV2 xray = Instance.get(AncientXrayV2.class);
        if (xray == null) return null;
        List<BlockPos> positions = xray.getDebrisPositionsSnapshot();
        return positions.stream()
                .filter(this::isAncientDebris)
                .filter(xray::isTrustedDebrisPosition)
                .filter(pos -> !finishedDebris.contains(pos))
                .filter(pos -> !failedDebris.contains(pos))
                .min(Comparator.comparingDouble(pos -> mc.player.getBlockPos().getSquaredDistance(pos)))
                .orElse(null);
    }

    private void markDebrisFailed(BlockPos pos) {
        if (pos == null) return;
        BlockPos immutable = pos.toImmutable();
        failedDebris.add(immutable);
        finishedDebris.add(immutable);
    }

    private boolean tryInterruptForVisibleDebris() {
        if (phase != Phase.FINDING_BLAST && phase != Phase.EXPLORING_TERRAIN
                && phase != Phase.GOING_BLAST && phase != Phase.FINDING_DEBRIS
                && phase != Phase.GOING_DEBRIS) {
            return false;
        }

        BlockPos debris = nearestDebris();
        if (debris == null) return false;

        BlockPos playerPos = mc.player.getBlockPos();
        double distSq = playerPos.getSquaredDistance(debris);
        if (distSq > DEBRIS_INTERRUPT_RANGE_SQ) {
            return false;
        }

        if (phase == Phase.GOING_DEBRIS) {
            if (targetDebris != null && targetDebris.equals(debris)) return false;
            double currentSq = targetDebris == null ? Double.MAX_VALUE : playerPos.getSquaredDistance(targetDebris);
            if (currentSq - distSq < DEBRIS_SWAP_IMPROVEMENT_SQ) return false;
            targetDebris = debris;
            sendStatus("Ближе нашелся обломок, переключаюсь: " + format(targetDebris), Formatting.GREEN);
            sendBaritone("#stop", true);
            goTo(targetDebris, true);
            phaseStarted = System.currentTimeMillis();
            resetStuckCheck();
            return true;
        }

        targetDebris = debris;
        sendStatus("Нашел обломок по пути, сначала добываю: " + format(targetDebris), Formatting.GREEN);
        sendBaritone("#stop", true);
        goTo(targetDebris, true);
        phase = Phase.GOING_DEBRIS;
        phaseStarted = System.currentTimeMillis();
        resetStuckCheck();
        return true;
    }

    private boolean isAncientDebris(BlockPos pos) {
        return mc.world != null && mc.world.getBlockState(pos).isOf(Blocks.ANCIENT_DEBRIS);
    }

    private boolean isTrustedDebrisTarget(BlockPos pos) {
        AncientXrayV2 xray = Instance.get(AncientXrayV2.class);
        return pos != null && isAncientDebris(pos) && xray != null && xray.isTrustedDebrisPosition(pos);
    }

    private boolean isCloseEnoughToMine(BlockPos pos) {
        if (mc.player == null) return false;
        BlockPos playerPos = mc.player.getBlockPos();
        int dx = Math.abs(playerPos.getX() - pos.getX());
        int dy = Math.abs(playerPos.getY() - pos.getY());
        int dz = Math.abs(playerPos.getZ() - pos.getZ());
        return dx + dy + dz == 1 || dx == 0 && dz == 0 && dy <= 2;
    }

    private boolean isStuckTowards(BlockPos goal) {
        long now = System.currentTimeMillis();
        if (now - lastStuckCheck < STUCK_CHECK_MS) return false;

        double distance = mc.player.getBlockPos().getSquaredDistance(goal);
        if (lastGoalDistance - distance < 4.0D) {
            stuckTicks++;
            nudgeMovement(goal, distance);
        } else {
            stuckTicks = 0;
        }

        lastGoalDistance = distance;
        lastStuckCheck = now;
        return stuckTicks >= 3;
    }

    private void resetStuckCheck() {
        lastStuckCheck = System.currentTimeMillis();
        lastGoalDistance = Double.MAX_VALUE;
        stuckTicks = 0;
    }

    private void nudgeMovement(BlockPos goal, double distance) {
        long now = System.currentTimeMillis();
        if (goal == null || distance <= 9.0D || pausedForEating || pausedForPotion || now - lastMovementNudge < MOVEMENT_NUDGE_MS) {
            return;
        }

        lastMovementNudge = now;
        goTo(goal, true);
    }

    private void autoEat() {
        refreshConsumableSlots();
        if (mc.player.getHungerManager().getFoodLevel() > 14 || foodSlot == -1) {
            stopEating();
            return;
        }

        if (!pausedForEating) {
            sendBaritone("#pause", true);
            pausedForEating = true;
            eatingStarted = System.currentTimeMillis();
            lastUseRetry = 0L;
        }
        if (previousSlot == -1) previousSlot = mc.player.getInventory().selectedSlot;
        selectSlot(foodSlot);
        mc.interactionManager.syncSelectedSlot();
        holdUseItem();
    }

    private void stopEating() {
        if (!pausedForEating) return;
        releaseUseItem();
        eatingStarted = 0L;
        if (previousSlot != -1 && mc.player != null) {
            selectSlot(previousSlot);
            previousSlot = -1;
        }
        if (pausedForEating && mc.getNetworkHandler() != null) {
            sendBaritone("#resume", true);
            pausedForEating = false;
            resumeNavigationAfterPause();
        }
    }

    private void autoFireResistance() {
        if (drinkingPotion) {
            holdUseItem();
            if (!needsFireResistanceRefresh() || System.currentTimeMillis() - potionStarted >= POTION_DRINK_MS) {
                stopPotion();
                return;
            }
            return;
        }

        if (!needsFireResistanceRefresh()) {
            stopPotion();
            return;
        }

        refreshConsumableSlots();
        if (fireResistanceSlot == -1) return;

        if (!pausedForPotion) {
            sendBaritone("#pause", true);
            pausedForPotion = true;
            lastUseRetry = 0L;
        }
        if (previousSlot == -1) previousSlot = mc.player.getInventory().selectedSlot;
        selectSlot(fireResistanceSlot);
        mc.interactionManager.syncSelectedSlot();
        potionStarted = System.currentTimeMillis();
        drinkingPotion = true;
        holdUseItem();
    }

    private void stopPotion() {
        if (!pausedForPotion && !drinkingPotion) return;
        releaseUseItem();
        drinkingPotion = false;
        potionStarted = 0L;
        if (previousSlot != -1 && mc.player != null) {
            selectSlot(previousSlot);
            previousSlot = -1;
        }
        if (pausedForPotion && mc.getNetworkHandler() != null) {
            sendBaritone("#resume", true);
            pausedForPotion = false;
            resumeNavigationAfterPause();
        }
    }

    private void refreshConsumableSlots() {
        foodSlot = findHotbar(stack -> stack.get(DataComponentTypes.FOOD) != null);
        fireResistanceSlot = findHotbar(this::isFireResistance);
    }

    private void holdUseItem() {
        if (mc.player == null || mc.interactionManager == null) return;

        mc.options.useKey.setPressed(true);
        long now = System.currentTimeMillis();
        if (!mc.player.isUsingItem()) {
            ItemToolkit.INSTANCE.setReleaseItem(true);
        }

        if (!mc.player.isUsingItem() || now - lastUseRetry >= ITEM_USE_RETRY_MS) {
            ItemToolkit.INSTANCE.useHand(Hand.MAIN_HAND);
            lastUseRetry = now;
        } else {
            ItemToolkit.INSTANCE.setUseItem(true);
        }
    }

    private void releaseUseItem() {
        mc.options.useKey.setPressed(false);
        ItemToolkit.INSTANCE.setUseItem(false);
        ItemToolkit.INSTANCE.setReleaseItem(true);
        lastUseRetry = 0L;
    }

    private boolean needsFireResistanceRefresh() {
        StatusEffectInstance effect = mc.player.getStatusEffect(StatusEffects.FIRE_RESISTANCE);
        return effect == null || effect.getDuration() <= FIRE_RESISTANCE_REFRESH_TICKS;
    }

    private void selectSlot(int slot) {
        if (slot < 0 || slot > 8 || mc.player == null) return;
        mc.player.getInventory().selectedSlot = slot;
    }

    private void goTo(BlockPos pos, boolean force) {
        if (pos == null) return;
        BlockPos immutable = pos.toImmutable();
        boolean sameTarget = immutable.equals(navigationTarget);
        navigationTarget = immutable;
        if (sameTarget && System.currentTimeMillis() - lastGotoSentAt < GOTO_REPEAT_MS) {
            return;
        }
        if (sendBaritone("#goto " + pos.getX() + " " + pos.getY() + " " + pos.getZ(), force)) {
            lastGotoSentAt = System.currentTimeMillis();
        }
    }

    private void resumeNavigationAfterPause() {
        if (navigationTarget != null && (phase == Phase.EXPLORING_TERRAIN || phase == Phase.GOING_BLAST || phase == Phase.GOING_DEBRIS || phase == Phase.RETREATING)) {
            goTo(navigationTarget, true);
        }
    }

    private boolean sendBaritone(String command, boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastCommand < COMMAND_DELAY_MS) return false;
        if (command.equals(lastCommandText) && now - lastCommandSentAt < SAME_COMMAND_THROTTLE_MS) {
            return false;
        }
        if (!command.equals("#stop")) {
            lastCommand = now;
        }
        lastCommandText = command;
        lastCommandSentAt = now;
        mc.getNetworkHandler().sendChatMessage(command);
        return true;
    }

    private String format(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private void reset() {
        stopEating();
        stopPotion();
        phase = Phase.STOPPED;
        blastPos = null;
        targetDebris = null;
        explorationPos = null;
        navigationTarget = null;
        clearTntConfirmation();
        phaseStarted = 0L;
        lastCommand = 0L;
        eatingStarted = 0L;
        lastMovementNudge = 0L;
        previousSlot = -1;
        lastNoSafeSpotMessage = 0L;
        explorationAttempts = 0;
        failedTerrainSearches = 0;
        mineAttempts = 0;
        tntPlaceAttempts = 0;
        directMiningStarted = false;
        rotatingForAction = false;
        forcedBlastPosition = false;
        pausedForEating = false;
        pausedForPotion = false;
        drinkingPotion = false;
        failedDebris.clear();
        finishedDebris.clear();
        lavaSinceMs = 0L;
        lastRtpAt = 0L;
        balanceRequestedAt = 0L;
        detectedBalance = -1L;
        balanceChecked = false;
        rtpStage = 0;
        lastGotoSentAt = 0L;
        lastCommandSentAt = 0L;
        lastCommandText = "";
        antiAfkUntil = 0L;
        lastAntiAfkAt = 0L;
        pendingRtpCommand = null;
        pendingRtpRetryAt = 0L;
        if (mc.options != null) {
            mc.options.jumpKey.setPressed(false);
        }
        resetStuckCheck();
    }

    private enum Phase {
        STOPPED,
        CHECKING,
        FINDING_BLAST,
        EXPLORING_TERRAIN,
        GOING_BLAST,
        PLACING_TNT,
        IGNITING_TNT,
        RETREATING,
        WAITING_EXPLOSION,
        FINDING_DEBRIS,
        GOING_DEBRIS,
        MINING_DEBRIS
    }

    private interface StackPredicate {
        boolean test(ItemStack stack);
    }

    private record Placement(BlockPos support, Direction side, BlockPos target) {}
}
