package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.visuals.AncientXRayV2;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ModuleInfo(name = "AncientBot", category = ModuleCategory.PLAYER, desc = "srcrich AncientBot state-machine port")
public class AncientBot extends BaseModule {
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
    private static final Pattern MONEY_PATTERN = Pattern.compile("([0-9][0-9.,\\s]{2,})");

    private final SliderSetting mineRange = new SliderSetting(this, "Mine range").min(2.0f).max(6.0f).step(0.5f).currentValue(4.5f);
    private final SliderSetting safeRange = new SliderSetting(this, "Blast Search").min(24.0f).max(96.0f).step(4.0f).currentValue(80.0f);
    private final BooleanSetting autoXray = new BooleanSetting(this, "Auto enable AncientXRayV2").enable();
    private final BooleanSetting useBaritone = new BooleanSetting(this, "Use Baritone commands").enable();
    private final BooleanSetting useTnt = new BooleanSetting(this, "TNT blast cycle").enable();
    private final BooleanSetting autoEat = new BooleanSetting(this, "Auto Eat").enable();
    private final BooleanSetting fireResistance = new BooleanSetting(this, "Fire Resistance").enable();
    private final BooleanSetting chatDebug = new BooleanSetting(this, "Chat Debug").enable();

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
    private float serverYaw;
    private float serverPitch;
    private boolean rotInit;

    private enum Phase { CHECKING, FINDING_BLAST, EXPLORING_TERRAIN, GOING_BLAST, PLACING_TNT, IGNITING_TNT, RETREATING, WAITING_EXPLOSION, FINDING_DEBRIS, GOING_DEBRIS, MINING_DEBRIS, STOPPED }

    private final EventListener<ClientPlayerTickEvent> onTick = event -> this.tickBot();
    private final EventListener<ReceivePacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof GameMessageS2CPacket msg) this.handleChat(msg.content().getString());
        if (this.pendingTntPos == null) return;
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet) this.handleServerTntState(packet.getPos(), packet.getState());
        else if (event.getPacket() instanceof ChunkDeltaUpdateS2CPacket packet) packet.visitUpdates(this::handleServerTntState);
    };

    @Override
    public void onEnable() {
        this.reset();
        this.phase = Phase.CHECKING;
        this.phaseStarted = System.currentTimeMillis();
        this.sendStatus("Запуск проверки условий", Formatting.AQUA);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.stopEating();
        this.stopPotion();
        this.sendBaritone("#stop", true);
        this.releaseKeys();
        this.reset();
        super.onDisable();
    }

    public BlockPos getTargetDebris() { return this.targetDebris; }
    public BlockPos getVisualTarget() {
        if (this.navigationTarget != null) return this.navigationTarget;
        return switch (this.phase) {
            case FINDING_BLAST, GOING_BLAST, PLACING_TNT, IGNITING_TNT, RETREATING, WAITING_EXPLOSION -> this.blastPos;
            case EXPLORING_TERRAIN -> this.explorationPos;
            case FINDING_DEBRIS, GOING_DEBRIS, MINING_DEBRIS -> this.targetDebris;
            default -> null;
        };
    }

    private void tickBot() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.getNetworkHandler() == null) return;
        this.tickAntiAfk();
        if (this.autoEat.isEnabled()) this.autoEat();
        if (this.pausedForEating) return;
        if (this.fireResistance.isEnabled()) this.autoFireResistance();
        if (this.pausedForPotion) return;
        if (this.handleLavaEscape()) return;
        if (this.handleBasaltBiome()) return;
        if (this.tryInterruptForVisibleDebris()) return;

        switch (this.phase) {
            case CHECKING -> this.tickChecking();
            case FINDING_BLAST -> this.tickFindingBlast();
            case EXPLORING_TERRAIN -> this.tickExploringTerrain();
            case GOING_BLAST -> this.tickGoingBlast();
            case PLACING_TNT -> this.tickPlacingTnt();
            case IGNITING_TNT -> this.tickIgnitingTnt();
            case RETREATING -> this.tickRetreating();
            case WAITING_EXPLOSION -> this.tickWaitingExplosion();
            case FINDING_DEBRIS -> this.tickFindingDebris();
            case GOING_DEBRIS -> this.tickGoingDebris();
            case MINING_DEBRIS -> this.tickMiningDebris();
            case STOPPED -> { this.stopEating(); this.stopPotion(); this.releaseKeys(); }
        }
    }

    private void handleChat(String text) {
        if (text == null || text.isEmpty()) return;
        String low = text.toLowerCase(Locale.ROOT);
        if (low.contains("афк") || low.contains("afk")) this.triggerAntiAfk();
        if (!this.balanceChecked && this.balanceRequestedAt != 0L && System.currentTimeMillis() - this.balanceRequestedAt <= BALANCE_CHECK_TIMEOUT_MS + 2000L) {
            Matcher m = MONEY_PATTERN.matcher(text);
            long best = -1L;
            while (m.find()) {
                String raw = m.group(1).replaceAll("[\\s,.]", "");
                if (raw.isEmpty()) continue;
                try { long value = Long.parseLong(raw); if (value >= REQUIRED_BALANCE / 10 && value > best) best = value; } catch (NumberFormatException ignored) {}
            }
            if (best > 0L) this.detectedBalance = best;
        }
    }

    private void tickChecking() {
        AncientXRayV2 xray = this.getXray();
        if (xray != null && this.autoXray.isEnabled() && !xray.isEnabled()) xray.enable();
        this.refreshSlots();
        long now = System.currentTimeMillis();
        if (!this.balanceChecked) {
            if (this.balanceRequestedAt == 0L) { this.balanceRequestedAt = now; this.sendCommand("bal", true); return; }
            if (now - this.balanceRequestedAt < BALANCE_CHECK_TIMEOUT_MS) return;
            this.balanceChecked = true;
            if (this.detectedBalance >= 0 && this.detectedBalance < REQUIRED_BALANCE) this.sendStatus("Баланс ниже " + REQUIRED_BALANCE + ", TNT может не купиться", Formatting.YELLOW);
        }
        if (!this.isInNether()) { this.sendRtpCommand("rtp nether"); this.phaseStarted = now; return; }
        this.phase = Phase.FINDING_DEBRIS;
        this.phaseStarted = now;
    }

    private void tickFindingBlast() {
        this.blastPos = this.findBlastPosition(false);
        if (this.blastPos == null) {
            this.failedTerrainSearches++;
            if (this.failedTerrainSearches >= MAX_TERRAIN_SEARCH_FAILURES) { this.phase = Phase.EXPLORING_TERRAIN; this.phaseStarted = System.currentTimeMillis(); this.explorationPos = this.findExplorationPosition(); }
            else if (System.currentTimeMillis() - this.lastNoSafeSpotMessage > NO_SAFE_SPOT_MESSAGE_MS) { this.lastNoSafeSpotMessage = System.currentTimeMillis(); this.sendStatus("Не нашел безопасное место под TNT", Formatting.YELLOW); }
            return;
        }
        this.failedTerrainSearches = 0;
        this.usedBlastPositions.add(this.blastPos);
        this.phase = Phase.GOING_BLAST;
        this.phaseStarted = System.currentTimeMillis();
        this.gotoBlock(this.blastPos);
    }

    private void tickExploringTerrain() {
        if (this.explorationPos == null) this.explorationPos = this.findExplorationPosition();
        if (this.explorationPos == null) { this.sendRtpCommand("rtp nether"); this.phase = Phase.CHECKING; return; }
        if (mc.player.squaredDistanceTo(Vec3d.ofCenter(this.explorationPos)) <= 9.0D) { this.explorationAttempts++; this.explorationPos = null; this.phase = Phase.FINDING_BLAST; return; }
        this.gotoBlock(this.explorationPos);
    }

    private void tickGoingBlast() {
        if (this.blastPos == null) { this.phase = Phase.FINDING_BLAST; return; }
        this.gotoBlock(this.blastPos);
        if (mc.player.squaredDistanceTo(Vec3d.ofCenter(this.blastPos)) <= 9.0D) { this.sendBaritone("#stop", false); this.phase = Phase.PLACING_TNT; this.tntPlaceAttempts = 0; this.phaseStarted = System.currentTimeMillis(); }
    }

    private void tickPlacingTnt() {
        if (this.blastPos == null) { this.phase = Phase.FINDING_BLAST; return; }
        if (this.tierWhiteSlot < 0) { this.buyTnt(); return; }
        if (System.currentTimeMillis() - this.phaseStarted < PLACE_DELAY_MS) return;
        if (this.tntPlaceAttempts++ > MAX_TNT_PLACE_ATTEMPTS) { this.failedTerrainSearches++; this.phase = Phase.FINDING_BLAST; return; }
        if (!this.selectHotbarOrSwap(this.tierWhiteSlot, 7)) return;
        BlockHitResult hit = this.findPlacementHit(this.blastPos);
        if (hit == null) { this.lastPlacementFailure = "no hit"; this.phaseStarted = System.currentTimeMillis(); return; }
        this.rotateTo(hit.getPos());
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        this.pendingTntPos = hit.getBlockPos().offset(hit.getSide());
        this.tntPlacedAt = System.currentTimeMillis();
        this.phase = Phase.IGNITING_TNT;
    }

    private void tickIgnitingTnt() {
        BlockPos tnt = this.serverConfirmedTntPos != null ? this.serverConfirmedTntPos : this.pendingTntPos;
        if (tnt == null && System.currentTimeMillis() - this.tntPlacedAt > TNT_CONFIRM_FALLBACK_MS) tnt = this.blastPos;
        if (tnt == null) return;
        if (this.flintSlot < 0) { this.refreshSlots(); if (this.flintSlot < 0) { this.sendStatus("Нет огнива", Formatting.RED); this.phase = Phase.STOPPED; return; } }
        if (!this.selectHotbarOrSwap(this.flintSlot, 6)) return;
        if (System.currentTimeMillis() - this.lastUseRetry < ITEM_USE_RETRY_MS) return;
        this.lastUseRetry = System.currentTimeMillis();
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(tnt), Direction.UP, tnt, false);
        this.rotateTo(Vec3d.ofCenter(tnt));
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        this.phase = Phase.RETREATING;
        this.phaseStarted = System.currentTimeMillis();
    }

    private void tickRetreating() {
        if (this.blastPos != null) this.gotoBlock(this.blastPos.add(8, 0, 8));
        if (System.currentTimeMillis() - this.phaseStarted > 1600L) { this.sendBaritone("#stop", false); this.phase = Phase.WAITING_EXPLOSION; this.phaseStarted = System.currentTimeMillis(); }
    }

    private void tickWaitingExplosion() {
        if (System.currentTimeMillis() - this.phaseStarted < EXPLOSION_WAIT_MS) return;
        AncientXRayV2 xray = this.getXray();
        if (xray != null && this.blastPos != null) xray.trustExplosionAround(this.blastPos);
        this.pendingTntPos = null; this.serverConfirmedTntPos = null; this.phase = Phase.FINDING_DEBRIS; this.phaseStarted = System.currentTimeMillis();
    }

    private void tickFindingDebris() {
        BlockPos nearest = this.findBestDebris();
        if (nearest == null) {
            this.targetDebris = null;
            if (this.useTnt.isEnabled()) this.phase = Phase.FINDING_BLAST;
            else this.phase = Phase.EXPLORING_TERRAIN;
            this.phaseStarted = System.currentTimeMillis();
            return;
        }
        this.targetDebris = nearest;
        this.mineAttempts = 0;
        this.phase = Phase.GOING_DEBRIS;
        this.phaseStarted = System.currentTimeMillis();
        this.gotoBlock(nearest);
    }

    private void tickGoingDebris() {
        if (!this.validDebris(this.targetDebris)) { this.phase = Phase.FINDING_DEBRIS; return; }
        if (this.canReach(this.targetDebris)) { this.sendBaritone("#stop", false); this.directMiningStarted = false; this.phase = Phase.MINING_DEBRIS; this.phaseStarted = System.currentTimeMillis(); return; }
        this.gotoBlock(this.targetDebris);
        this.checkStuckTo(this.targetDebris);
    }

    private void tickMiningDebris() {
        if (!this.validDebris(this.targetDebris)) { this.finishDebris(this.targetDebris, true); this.phase = Phase.FINDING_DEBRIS; return; }
        if (!this.canReach(this.targetDebris)) { this.phase = Phase.GOING_DEBRIS; return; }
        if (System.currentTimeMillis() - this.phaseStarted > MINE_TIMEOUT_MS) {
            if (++this.mineAttempts >= MAX_MINE_ATTEMPTS) { this.failedDebris.add(this.targetDebris); this.phase = Phase.FINDING_DEBRIS; }
            else { this.phaseStarted = System.currentTimeMillis(); }
            return;
        }
        this.equipPickaxe();
        this.rotateTo(Vec3d.ofCenter(this.targetDebris));
        Direction side = Direction.getFacing(this.targetDebris.getX() - mc.player.getBlockX(), this.targetDebris.getY() - mc.player.getBlockY(), this.targetDebris.getZ() - mc.player.getBlockZ());
        mc.interactionManager.updateBlockBreakingProgress(this.targetDebris, side);
        mc.player.swingHand(Hand.MAIN_HAND);
        this.directMiningStarted = true;
    }

    private void refreshSlots() {
        this.tierWhiteSlot = this.findByName("tier");
        if (this.tierWhiteSlot < 0) this.tierWhiteSlot = this.findByItem(Items.TNT);
        this.flintSlot = this.findByItem(Items.FLINT_AND_STEEL);
        this.blockSlot = this.findBuildBlock();
        this.foodSlot = this.findFood();
        this.pickaxeSlot = this.findPickaxe();
        this.fireResistanceSlot = this.findFireResistancePotion();
    }

    private boolean tryInterruptForVisibleDebris() {
        if (this.phase == Phase.MINING_DEBRIS || this.phase == Phase.GOING_DEBRIS) return false;
        BlockPos best = this.findBestDebris();
        if (best == null) return false;
        if (this.targetDebris == null || mc.player.squaredDistanceTo(Vec3d.ofCenter(best)) + DEBRIS_SWAP_IMPROVEMENT_SQ < mc.player.squaredDistanceTo(Vec3d.ofCenter(this.targetDebris))) {
            this.targetDebris = best; this.phase = Phase.GOING_DEBRIS; this.phaseStarted = System.currentTimeMillis(); return true;
        }
        return false;
    }

    private BlockPos findBestDebris() {
        AncientXRayV2 xray = this.getXray();
        if (xray == null) return null;
        List<BlockPos> list = xray.getDebrisPositionsSnapshot();
        return list.stream().filter(this::validDebris).filter(pos -> !this.finishedDebris.contains(pos) && !this.failedDebris.contains(pos)).filter(pos -> mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) <= DEBRIS_INTERRUPT_RANGE_SQ).min(Comparator.comparingDouble(pos -> mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)))).orElse(null);
    }

    private boolean validDebris(BlockPos pos) { return pos != null && mc.world != null && mc.world.getBlockState(pos).getBlock() == Blocks.ANCIENT_DEBRIS; }
    private boolean canReach(BlockPos pos) { return pos != null && mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) <= this.mineRange.getCurrentValue() * this.mineRange.getCurrentValue(); }
    private void finishDebris(BlockPos pos, boolean success) { if (pos == null) return; if (success) this.finishedDebris.add(pos); else this.failedDebris.add(pos); AncientXRayV2 xray = this.getXray(); if (xray != null) xray.markFinished(pos); this.targetDebris = null; }
    private AncientXRayV2 getXray() { return Rockstar.getInstance().getModuleManager().getModule(AncientXRayV2.class); }

    private BlockPos findBlastPosition(boolean force) {
        BlockPos base = mc.player.getBlockPos();
        int radius = (int)this.safeRange.getCurrentValue();
        BlockPos best = null; double bestScore = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; x += BLAST_STEP) for (int z = -radius; z <= radius; z += BLAST_STEP) for (int y = Math.max(MIN_BLAST_Y, base.getY() - 24); y <= Math.min(118, base.getY() + 24); y += 2) {
            BlockPos pos = base.add(x, y - base.getY(), z);
            if (this.usedBlastPositions.contains(pos)) continue;
            if (!this.isSafeBlastSpot(pos, force)) continue;
            double d = base.getSquaredDistance(pos);
            if (d < bestScore) { bestScore = d; best = pos; }
        }
        return best;
    }

    private boolean isSafeBlastSpot(BlockPos pos, boolean force) {
        if (mc.world == null || pos.getY() < MIN_BLAST_Y) return false;
        if (!force && !mc.world.getBlockState(pos).isAir()) return false;
        return mc.world.getBlockState(pos.down()).isSolidBlock(mc.world, pos.down())
                && mc.world.getBlockState(pos.up()).isAir()
                && mc.world.getBlockState(pos.up(2)).isAir()
                && mc.world.getBlockState(pos).getFluidState().isEmpty();
    }

    private BlockPos findExplorationPosition() {
        Vec3d dir = mc.player.getRotationVector().normalize().multiply(EXPLORATION_DISTANCE);
        BlockPos target = BlockPos.ofFloored(mc.player.getX() + dir.x, Math.max(MIN_BLAST_Y, mc.player.getY()), mc.player.getZ() + dir.z);
        return target;
    }

    private void buyTnt() {
        if (System.currentTimeMillis() - this.lastCommand < COMMAND_DELAY_MS) return;
        this.sendCommand("shop", true);
        this.lastCommand = System.currentTimeMillis();
        this.refreshSlots();
    }

    private void handleServerTntState(BlockPos pos, BlockState state) {
        if (this.pendingTntPos == null || !pos.equals(this.pendingTntPos)) return;
        if (state.getBlock() == Blocks.TNT) { this.serverConfirmedTntPos = pos.toImmutable(); this.serverConfirmedTntAt = System.currentTimeMillis(); this.stableTntTicks++; }
    }

    private BlockHitResult findPlacementHit(BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos base = pos.offset(side.getOpposite());
            if (mc.world.getBlockState(base).isSolidBlock(mc.world, base) && mc.world.getBlockState(base.offset(side)).isAir()) {
                return new BlockHitResult(Vec3d.ofCenter(base).add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5), side, base, false);
            }
        }
        return null;
    }

    private void gotoBlock(BlockPos pos) {
        if (pos == null) return;
        this.navigationTarget = pos;
        if (this.useBaritone.isEnabled()) {
            long now = System.currentTimeMillis();
            if (now - this.lastGotoSentAt > GOTO_REPEAT_MS) {
                this.sendBaritone("#goto " + pos.getX() + " " + pos.getY() + " " + pos.getZ(), false);
                this.lastGotoSentAt = now;
            }
            return;
        }
        this.walkTo(pos);
    }

    private void walkTo(BlockPos pos) {
        Vec3d center = Vec3d.ofCenter(pos);
        this.rotateTo(center);
        if (mc.options != null) {
            mc.options.forwardKey.setPressed(true);
            mc.options.sprintKey.setPressed(true);
            mc.options.jumpKey.setPressed(mc.player.horizontalCollision);
        }
    }

    private void checkStuckTo(BlockPos goal) {
        long now = System.currentTimeMillis();
        if (now - this.lastStuckCheck < STUCK_CHECK_MS) return;
        double dist = mc.player.squaredDistanceTo(Vec3d.ofCenter(goal));
        if (dist >= this.lastGoalDistance - 1.0D) this.stuckTicks++; else this.stuckTicks = 0;
        this.lastGoalDistance = dist;
        this.lastStuckCheck = now;
        if (this.stuckTicks >= 3) { this.stuckTicks = 0; this.sendBaritone("#stop", false); this.lastGotoSentAt = 0L; }
    }

    private void autoEat() {
        if (mc.player.getHungerManager().getFoodLevel() > 14) { this.stopEating(); return; }
        if (this.pausedForPotion) return;
        if (this.foodSlot < 0) this.foodSlot = this.findFood();
        if (this.foodSlot < 0) return;
        if (!this.pausedForEating) { this.previousSlot = mc.player.getInventory().selectedSlot; this.pausedForEating = true; this.eatingStarted = System.currentTimeMillis(); this.selectHotbarOrSwap(this.foodSlot, 5); }
        mc.options.useKey.setPressed(true);
        if (System.currentTimeMillis() - this.eatingStarted > 1800L) this.stopEating();
    }

    private void stopEating() { if (!this.pausedForEating) return; this.pausedForEating = false; if (mc.options != null) mc.options.useKey.setPressed(false); if (this.previousSlot >= 0 && this.previousSlot < 9) mc.player.getInventory().selectedSlot = this.previousSlot; this.previousSlot = -1; }

    private void autoFireResistance() {
        boolean has = mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && mc.player.getStatusEffect(StatusEffects.FIRE_RESISTANCE).getDuration() > FIRE_RESISTANCE_REFRESH_TICKS;
        if (has) { this.stopPotion(); return; }
        if (this.fireResistanceSlot < 0) this.fireResistanceSlot = this.findFireResistancePotion();
        if (this.fireResistanceSlot < 0) return;
        if (!this.pausedForPotion) { this.previousSlot = mc.player.getInventory().selectedSlot; this.pausedForPotion = true; this.drinkingPotion = true; this.potionStarted = System.currentTimeMillis(); this.selectHotbarOrSwap(this.fireResistanceSlot, 4); }
        mc.options.useKey.setPressed(true);
        if (System.currentTimeMillis() - this.potionStarted > POTION_DRINK_MS) this.stopPotion();
    }

    private void stopPotion() { if (!this.pausedForPotion) return; this.pausedForPotion = false; this.drinkingPotion = false; if (mc.options != null) mc.options.useKey.setPressed(false); if (this.previousSlot >= 0 && this.previousSlot < 9) mc.player.getInventory().selectedSlot = this.previousSlot; this.previousSlot = -1; }

    private boolean handleLavaEscape() {
        boolean lava = mc.player.isInLava();
        if (!lava) { this.lavaSinceMs = 0L; return false; }
        if (this.lavaSinceMs == 0L) this.lavaSinceMs = System.currentTimeMillis();
        if (System.currentTimeMillis() - this.lavaSinceMs > LAVA_STUCK_RTP_MS && System.currentTimeMillis() - this.lastRtpAt > LAVA_RTP_COOLDOWN_MS) {
            this.sendRtpCommand("rtp nether"); this.lastRtpAt = System.currentTimeMillis(); this.phase = Phase.CHECKING; return true;
        }
        if (mc.options != null) { mc.options.jumpKey.setPressed(true); mc.options.forwardKey.setPressed(true); }
        return false;
    }

    private boolean handleBasaltBiome() {
        if (mc.world == null || mc.player == null) return false;
        try {
            if (mc.world.getBiome(mc.player.getBlockPos()).matchesKey(BiomeKeys.BASALT_DELTAS)) {
                if (System.currentTimeMillis() - this.lastRtpAt > LAVA_RTP_COOLDOWN_MS) { this.sendRtpCommand("rtp nether"); this.lastRtpAt = System.currentTimeMillis(); this.phase = Phase.CHECKING; return true; }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private void tickAntiAfk() { if (this.antiAfkUntil > 0L && System.currentTimeMillis() >= this.antiAfkUntil) { this.antiAfkUntil = 0L; if (mc.options != null) mc.options.jumpKey.setPressed(false); } if (this.pendingRtpCommand != null && this.pendingRtpRetryAt > 0L && System.currentTimeMillis() >= this.pendingRtpRetryAt) { String cmd = this.pendingRtpCommand; mc.player.networkHandler.sendChatCommand(cmd); mc.player.networkHandler.sendChatCommand(cmd); this.pendingRtpCommand = null; this.pendingRtpRetryAt = 0L; } }
    private void triggerAntiAfk() { long now = System.currentTimeMillis(); if (now - this.lastAntiAfkAt < ANTI_AFK_COOLDOWN_MS) return; this.lastAntiAfkAt = now; this.antiAfkUntil = now + ANTI_AFK_HOLD_MS; if (mc.options != null) mc.options.jumpKey.setPressed(true); if (this.pendingRtpCommand != null) this.pendingRtpRetryAt = now + ANTI_AFK_HOLD_MS + 300L; }
    private void sendRtpCommand(String command) { if (mc.player == null || mc.player.networkHandler == null) return; mc.player.networkHandler.sendChatCommand(command); mc.player.networkHandler.sendChatCommand(command); this.pendingRtpCommand = command; this.pendingRtpRetryAt = 0L; }
    private boolean isInNether() { return mc.world != null && mc.world.getRegistryKey() == World.NETHER; }

    private void rotateTo(Vec3d point) {
        Rotation target = RotationMath.getRotationTo(point);
        if (!this.rotInit) { this.serverYaw = mc.player.getYaw(); this.serverPitch = mc.player.getPitch(); this.rotInit = true; }
        float dy = wrap(target.getYaw() - this.serverYaw);
        float dp = target.getPitch() - this.serverPitch;
        if (Math.abs(dy) > ROTATION_DEAD_ZONE) this.serverYaw = wrap(this.serverYaw + clamp(dy, -MAX_YAW_STEP, MAX_YAW_STEP));
        if (Math.abs(dp) > ROTATION_DEAD_ZONE) this.serverPitch = clamp(this.serverPitch + clamp(dp, -MAX_PITCH_STEP, MAX_PITCH_STEP), -90.0F, 90.0F);
        Rockstar.getInstance().getRotationHandler().rotate(new Rotation(this.serverYaw, this.serverPitch), MoveCorrection.SILENT, 180.0f, 180.0f, 80.0f, RotationPriority.TO_TARGET);
    }

    private int findByItem(net.minecraft.item.Item item) { for (int i = 0; i < 36; i++) if (mc.player.getInventory().getStack(i).isOf(item)) return i; return -1; }
    private int findByName(String part) { String p = part.toLowerCase(Locale.ROOT); for (int i = 0; i < 36; i++) { ItemStack s = mc.player.getInventory().getStack(i); if (!s.isEmpty() && s.getName().getString().toLowerCase(Locale.ROOT).contains(p)) return i; } return -1; }
    private int findPickaxe() { for (int i = 0; i < 36; i++) if (mc.player.getInventory().getStack(i).getItem() instanceof PickaxeItem) return i; return -1; }
    private int findBuildBlock() { for (int i = 0; i < 36; i++) { ItemStack s = mc.player.getInventory().getStack(i); if (s.getItem() != Items.TNT && s.getItem() instanceof net.minecraft.item.BlockItem) return i; } return -1; }
    private int findFood() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.get(DataComponentTypes.FOOD) != null) return i;
        }
        return -1;
    }

    private int findFireResistancePotion() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            String name = stack.getName().getString().toLowerCase(Locale.ROOT);
            if ((stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION))
                    && (name.contains("fire") || name.contains("огне") || name.contains("resistance"))) return i;
        }
        return -1;
    }
    private boolean selectHotbarOrSwap(int invSlot, int preferredHotbar) { if (invSlot < 0) return false; PlayerInventory inv = mc.player.getInventory(); if (invSlot < 9) { inv.selectedSlot = invSlot; return true; } mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invSlot, preferredHotbar, SlotActionType.SWAP, mc.player); inv.selectedSlot = preferredHotbar; this.refreshSlots(); return true; }
    private void equipPickaxe() { if (this.pickaxeSlot < 0) this.pickaxeSlot = this.findPickaxe(); this.selectHotbarOrSwap(this.pickaxeSlot, 8); }
    private void releaseKeys() { if (mc.options == null) return; mc.options.forwardKey.setPressed(false); mc.options.sprintKey.setPressed(false); mc.options.jumpKey.setPressed(false); mc.options.useKey.setPressed(false); }
    private void sendCommand(String command, boolean throttle) { long now = System.currentTimeMillis(); if (throttle && command.equals(this.lastCommandText) && now - this.lastCommandSentAt < SAME_COMMAND_THROTTLE_MS) return; mc.player.networkHandler.sendChatCommand(command); this.lastCommandText = command; this.lastCommandSentAt = now; }
    private void sendBaritone(String command, boolean force) { if (!force && System.currentTimeMillis() - this.lastCommandSentAt < SAME_COMMAND_THROTTLE_MS) return; if (mc.player != null && mc.player.networkHandler != null) mc.player.networkHandler.sendChatMessage(command); this.lastCommandSentAt = System.currentTimeMillis(); }
    private void sendStatus(String msg, Formatting color) { if (this.chatDebug.isEnabled()) MessageUtility.info(Text.literal("[AncientBot] ").formatted(Formatting.GOLD).append(Text.literal(msg).formatted(color))); }
    private void reset() { this.usedBlastPositions.clear(); this.finishedDebris.clear(); this.failedDebris.clear(); this.phase = Phase.STOPPED; this.blastPos = null; this.targetDebris = null; this.explorationPos = null; this.navigationTarget = null; this.pendingTntPos = null; this.serverConfirmedTntPos = null; this.lastCommand = 0L; this.lastGotoSentAt = 0L; this.balanceRequestedAt = 0L; this.detectedBalance = -1L; this.balanceChecked = false; this.pausedForEating = false; this.pausedForPotion = false; this.rotInit = false; }
    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
    private static float wrap(float degrees) { degrees %= 360.0F; if (degrees >= 180.0F) degrees -= 360.0F; if (degrees < -180.0F) degrees += 360.0F; return degrees; }
}
