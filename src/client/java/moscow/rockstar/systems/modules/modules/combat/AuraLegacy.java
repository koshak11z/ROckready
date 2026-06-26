/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.consume.UseAction
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package moscow.rockstar.systems.modules.modules.combat;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.combat.neyro.NeyroManager;
import moscow.rockstar.systems.modules.modules.combat.neyro.NeyroSmoothMode;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.target.TargetComparators;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.game.CombatUtility;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.prediction.ElytraPredictionSystem;
import moscow.rockstar.utility.game.prediction.FallingPlayer;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.math.PerlinNoise;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

@ModuleInfo(name="AuraLegacy", category=ModuleCategory.COMBAT, desc="\u0411\u044c\u0451\u0442 \u0436\u0435\u043d\u0449\u0438\u043d \u0438 \u0434\u0435\u0442\u0435\u0439")
public class AuraLegacy
        extends BaseModule implements IMinecraft {
    private SliderSetting attackDistance;
    private SliderSetting aimDistance;
    private SelectSetting targets;
    private SelectSetting.Value players;
    private SelectSetting.Value animals;
    private SelectSetting.Value mobs;
    private SelectSetting.Value invisibles;
    private SelectSetting.Value nakedPlayers;
    private SelectSetting.Value friends;
    private ModeSetting sortingMode;
    private ModeSetting.Value distanceSorting;
    private ModeSetting.Value healthSorting;
    private ModeSetting.Value fovSorting;
    private ModeSetting rotationMode;
    private ModeSetting.Value noRotation;
    private ModeSetting.Value simpleRotation;
    private ModeSetting.Value neuroRotation;
    private ModeSetting.Value funTimeRotation;
    private ModeSetting.Value funTimeTestRotation;
    private ModeSetting.Value spookyTimeRotation;
    private ModeSetting.Value holyWorldRotation;
    private ModeSetting.Value intaveRotation;
    private ModeSetting moveCorrectionMode;
    private ModeSetting.Value noMoveCorrection;
    private ModeSetting.Value directMoveCorrection;
    private ModeSetting.Value silentMoveCorrection;
    private ModeSetting styleAttack;
    private ModeSetting.Value fastPvp;
    private ModeSetting.Value slowPvp;
    private BooleanSetting onlyCriticals;
    private BooleanSetting smartCrits;
    private BooleanSetting tpsSync;
    private BooleanSetting walls;
    private BooleanSetting rayTrace;
    private BooleanSetting onlyWeapon;
    private BooleanSetting targeting;
    private final Animation nononoYaw = new Animation(300L, Easing.LINEAR);
    private final Animation nononoPitch = new Animation(1000L, Easing.LINEAR);
    private Timer attackTimer;
    private final Timer smartCritTimer = new Timer();
    boolean shield;
    private PerlinNoise noise = new PerlinNoise();
    private long rotationStartTime = 0L;
    private float noiseFactor = 0.0f;
    private int attacks;
    private Rotation additional;
    private final Timer collideTimer = new Timer();
    private long lastUpdateMs;
    private long lastSnapAttackMs;
    private Rotation lastSnapRotation;
    private final EventListener<ClientPlayerTickEvent> onPlayerTick = event -> {
        this.processAuraUpdates();
    };
    private final EventListener<PreHudRenderEvent> onPreHudRender = event -> this.processAuraUpdates();

    public AuraLegacy() {
        this.initialize();
    }

    private void processAuraUpdates() {
        if (mc.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.lastUpdateMs == 0L) {
            this.lastUpdateMs = now;
        }
        long elapsed = now - this.lastUpdateMs;
        if (elapsed <= 0L) {
            return;
        }
        int steps = (int)Math.min(50L, elapsed);
        for (int i = 0; i < steps; ++i) {
            this.updateAuraStep();
        }
        this.lastUpdateMs += (long)steps;
    }

    private void updateAuraStep() {
        LivingEntity living;
        LivingEntity target;
        float requiredAimDistance = Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isEnabled() ? 50.0f : this.aimDistance.getCurrentValue();
        TargetSettings.Builder builder = new TargetSettings.Builder().targetPlayers(this.players.isSelected()).targetAnimals(this.animals.isSelected()).targetMobs(this.mobs.isSelected()).targetInvisibles(this.invisibles.isSelected()).targetNakedPlayers(this.nakedPlayers.isSelected()).targetFriends(this.friends.isSelected()).requiredRange(requiredAimDistance);
        if (this.sortingMode.is(this.distanceSorting)) {
            builder.sortBy(TargetComparators.DISTANCE);
        } else if (this.sortingMode.is(this.healthSorting)) {
            builder.sortBy(TargetComparators.HEALTH);
        } else if (this.sortingMode.is(this.fovSorting)) {
            builder.sortBy(TargetComparators.FOV);
        }
        TargetSettings settings = builder.build();
        Entity target1 = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        LivingEntity livingEntity = target = target1 instanceof LivingEntity ? (living = (LivingEntity)target1) : null;
        if (!this.targeting.isEnabled() || target == null || MathHelper.sqrt((float)((float)mc.player.squaredDistanceTo(RotationMath.getNearestPoint(target)))) > requiredAimDistance || !mc.world.hasEntity((Entity)target) || !target.isAlive()) {
            Rockstar.getInstance().getTargetManager().update(settings);
        }
        if (target != null) {
            if (this.rotationMode.is(this.funTimeTestRotation)) {
                this.handleSnapAura(target);
            } else {
                this.rotateHead(target);
                if (this.shouldAttackEntity(target)) {
                    this.attack(target);
                }
            }
            NeyroManager manager = NeyroManager.INSTANCE;
            if (manager.isRecording()) {
                manager.performTrainingAttack();
                manager.recordFrame();
            }
        } else {
            this.rotationStartTime = System.currentTimeMillis();
            this.noise = new PerlinNoise();
            this.noiseFactor = 1.0f;
        }
    }

    private void handleSnapAura(LivingEntity targetedEntity) {
        long now = System.currentTimeMillis();
        if (this.isSnapShaking(now)) {
            this.applySnapShake(now);
            return;
        }
        if (!this.canAttemptSnapAttack(targetedEntity)) {
            return;
        }
        RotationHandler handler = Rockstar.getInstance().getRotationHandler();
        MoveCorrection moveCorrection = this.getMoveCorrection();
        Rotation snapRotation = this.getEdgeSnapRotation(targetedEntity);
        this.lastSnapRotation = snapRotation;
        handler.rotate(snapRotation, moveCorrection, 180.0f, 180.0f, 180.0f, RotationPriority.TO_TARGET);
        if (this.shouldAttackEntity(targetedEntity)) {
            this.attack(targetedEntity);
            this.lastSnapAttackMs = now;
        }
    }

    private boolean isSnapShaking(long now) {
        return this.lastSnapAttackMs > 0L && now - this.lastSnapAttackMs <= 175L;
    }

    private void applySnapShake(long now) {
        RotationHandler handler = Rockstar.getInstance().getRotationHandler();
        MoveCorrection moveCorrection = this.getMoveCorrection();
        Rotation base = this.lastSnapRotation != null ? this.lastSnapRotation : handler.getCurrentRotation();
        float progress = (float)(now - this.lastSnapAttackMs) / 175.0f;
        float amplitude = Math.max(0.5f, 2.0f + progress * 14.0f);
        float yawOffset = (float)MathUtility.random((double)(-amplitude), (double)amplitude);
        float pitchOffset = (float)MathUtility.random((double)(-amplitude * 0.6f), (double)(amplitude * 0.6f));
        handler.rotate(new Rotation(base.getYaw() + yawOffset, Math.clamp(base.getPitch() + pitchOffset, -90.0f, 90.0f)), moveCorrection, 180.0f, 180.0f, 180.0f, RotationPriority.TO_TARGET);
    }

    private MoveCorrection getMoveCorrection() {
        return this.moveCorrectionMode.is(this.silentMoveCorrection) ? MoveCorrection.SILENT : (this.moveCorrectionMode.is(this.directMoveCorrection) ? MoveCorrection.DIRECT : MoveCorrection.NONE);
    }

    private Rotation getEdgeSnapRotation(LivingEntity targetedEntity) {
        Box box = targetedEntity.getBoundingBox();
        double x = MathUtility.random(box.minX, box.maxX);
        double y = MathUtility.random(box.minY, box.maxY);
        double z = MathUtility.random(box.minZ, box.maxZ);
        int axis = (int)MathUtility.random(0.0, 3.0);
        boolean useMax = MathUtility.random(0.0, 1.0) > 0.5;
        if (axis == 0) {
            x = useMax ? box.maxX : box.minX;
        } else if (axis == 1) {
            y = useMax ? box.maxY : box.minY;
        } else {
            z = useMax ? box.maxZ : box.minZ;
        }
        return RotationMath.getRotationTo(new Vec3d(x, y, z));
    }

    private boolean canAttemptSnapAttack(LivingEntity targetedEntity) {
        if (!this.isCooledDown()) {
            return false;
        }
        if (this.onlyWeapon.isEnabled() && !EntityUtility.isHoldingWeapon()) {
            return false;
        }
        if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return false;
        }
        if (this.inRange(targetedEntity)) {
            return false;
        }
        if (this.smartCrits.isEnabled()) {
            if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                return this.smartCritTimer.finished(300L);
            }
            this.smartCritTimer.reset();
            return CombatUtility.canPerformCriticalHit(targetedEntity, true);
        }
        return !this.onlyCriticals.isEnabled() || !this.isCriticalRequired(targetedEntity) || CombatUtility.canPerformCriticalHit(targetedEntity, true);
    }

    @VMProtect(type=VMProtectType.VIRTUALIZATION)
    private void initialize() {
        this.rotationMode = new ModeSetting(this, "modules.settings.aura.rotationMode");
        this.noRotation = new ModeSetting.Value(this.rotationMode, "modules.settings.aura.noRotation");
        this.simpleRotation = new ModeSetting.Value(this.rotationMode, "modules.settings.aura.simpleRotation").select();
        this.neuroRotation = new ModeSetting.Value(this.rotationMode, "Neyro");
        this.funTimeRotation = new ModeSetting.Value(this.rotationMode, "FunTime");
        this.funTimeTestRotation = new ModeSetting.Value(this.rotationMode, "modules.settings.aura.funtimeTest");
        this.spookyTimeRotation = new ModeSetting.Value(this.rotationMode, "SpookyTime");
        this.holyWorldRotation = new ModeSetting.Value(this.rotationMode, "HolyWorld");
        this.intaveRotation = new ModeSetting.Value(this.rotationMode, "Intave");
        this.attackDistance = new SliderSetting(this, "modules.settings.aura.attackDistance").min(0.1f).max(6.5f).step(0.1f).currentValue(3.0f).suffix(number -> " %s".formatted(Localizator.translate("block")) + TextUtility.makeCountTranslated(number));
        this.aimDistance = new SliderSetting(this, "modules.settings.aura.aimDistance").min(0.1f).max(6.5f).step(0.1f).currentValue(3.0f).suffix(number -> " %s".formatted(Localizator.translate("block")) + TextUtility.makeCountTranslated(number));
        this.onlyCriticals = new BooleanSetting(this, "only_crits");
        this.smartCrits = new BooleanSetting(this, "smart_crits");
        this.tpsSync = new BooleanSetting(this, "modules.settings.aura.tpsSync").enable();
        this.walls = new BooleanSetting(this, "modules.settings.aura.walls").enable();
        this.rayTrace = new BooleanSetting(this, "modules.settings.aura.rayTrace").enable();
        this.targeting = new BooleanSetting(this, "modules.settings.aura.targeting").enable();
        this.onlyWeapon = new BooleanSetting(this, "modules.settings.aura.onlyWeapon");
        this.targets = new SelectSetting(this, "targets");
        this.players = new SelectSetting.Value(this.targets, "players").select();
        this.animals = new SelectSetting.Value(this.targets, "animals").select();
        this.mobs = new SelectSetting.Value(this.targets, "mobs").select();
        this.invisibles = new SelectSetting.Value(this.targets, "invisibles").select();
        this.nakedPlayers = new SelectSetting.Value(this.targets, "nakedPlayers").select();
        this.friends = new SelectSetting.Value(this.targets, "friends");
        this.sortingMode = new ModeSetting(this, "sorting");
        this.distanceSorting = new ModeSetting.Value(this.sortingMode, "modules.settings.aura.distanceSorting").select();
        this.healthSorting = new ModeSetting.Value(this.sortingMode, "modules.settings.aura.healthSorting");
        this.fovSorting = new ModeSetting.Value(this.sortingMode, "modules.settings.aura.fovSorting");
        this.moveCorrectionMode = new ModeSetting(this, "modules.settings.aura.moveCorrectionMode");
        this.noMoveCorrection = new ModeSetting.Value(this.moveCorrectionMode, "modules.settings.aura.noMoveCorrection");
        this.directMoveCorrection = new ModeSetting.Value(this.moveCorrectionMode, "modules.settings.aura.directMoveCorrection");
        this.silentMoveCorrection = new ModeSetting.Value(this.moveCorrectionMode, "modules.settings.aura.silentMoveCorrection").select();
        this.styleAttack = new ModeSetting(this, "modules.settings.aura.styleAttack");
        this.fastPvp = new ModeSetting.Value(this.styleAttack, "1.8");
        this.slowPvp = new ModeSetting.Value(this.styleAttack, "1.9").select();
        this.attackTimer = new Timer();
    }

    private boolean shouldAttackEntity(LivingEntity targetedEntity) {
        if (!this.isCooledDown()) {
            return false;
        }
        if (this.onlyWeapon.isEnabled() && !EntityUtility.isHoldingWeapon()) {
            return false;
        }
        if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return false;
        }
        if (this.inRange(targetedEntity)) {
            return false;
        }
        if (this.walls.isEnabled() && this.spookyTimeRotation.isSelected() && mc.world.raycast(new RaycastContext(mc.player.getEyePos(), mc.player.getEyePos().add(mc.player.getRotationVector(-90.0f, Rockstar.getInstance().getRotationHandler().getCurrentRotation().getYaw()).multiply((double)this.attackDistance.getCurrentValue())), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)mc.player)).getType() == HitResult.Type.BLOCK) {
            return false;
        }
        if (!MathUtility.canTraceWithBlock(this.attackDistance.getCurrentValue(), Rockstar.getInstance().getRotationHandler().getCurrentRotation().getYaw(), Rockstar.getInstance().getRotationHandler().getCurrentRotation().getPitch(), (Entity)mc.player, (Entity)targetedEntity, !this.walls.isEnabled()) && this.rayTrace.isEnabled()) {
            return false;
        }
        if (this.smartCrits.isEnabled()) {
            if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                return this.smartCritTimer.finished(300L);
            }
            this.smartCritTimer.reset();
            return CombatUtility.canPerformCriticalHit(targetedEntity, true);
        }
        return !this.onlyCriticals.isEnabled() || !this.isCriticalRequired(targetedEntity) || CombatUtility.canPerformCriticalHit(targetedEntity, true);
    }

    private long getTpsDelay(long delay) {
        if (this.tpsSync == null || !this.tpsSync.isEnabled()) {
            return delay;
        }
        float tps = Rockstar.getInstance().getTpsHandler().getTPS();
        if (!(tps > 0.0f)) {
            return delay;
        }
        float factor = MathHelper.clamp(20.0f / tps, 1.0f, 5.0f);
        return (long)((float)delay * factor);
    }

    private boolean isCriticalRequired(LivingEntity targetedEntity) {
        float damage = this.calculateDamage(targetedEntity);
        return damage <= targetedEntity.getHealth();
    }

    public boolean isCooledDown() {
        if (mc.player == null) {
            return false;
        }
        if (CombatUtility.getMace() != null) {
            return this.attackTimer.finished(this.getTpsDelay(500L));
        }
        return mc.player.getAttackCooldownProgress(1.5f) > 0.93f && this.attackTimer.finished(this.getTpsDelay(500L)) || this.fastPvp.isSelected() && this.attackTimer.finished(this.getTpsDelay(50L));
    }

    public float calculateDamage(LivingEntity targetedEntity) {
        return 0.0f;
    }

    private void attack(LivingEntity targetedEntity) {
        HotbarSlot slot;
        if (mc.interactionManager == null || mc.player == null) {
            return;
        }
        boolean bl = this.shield = mc.player.isUsingItem() && mc.player.getActiveItem().getItem().getUseAction(mc.player.getActiveItem()) == UseAction.BLOCK;
        if (this.shield) {
            mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
        }
        if (CombatUtility.shouldBreakShield(targetedEntity) && CombatUtility.canBreakShield(targetedEntity)) {
            CombatUtility.tryBreakShield(targetedEntity);
        }
        if ((slot = CombatUtility.getMace()) != null) {
            mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
            CombatUtility.tryBreakShield(targetedEntity);
        }
        mc.interactionManager.attackEntity((PlayerEntity)mc.player, (Entity)targetedEntity);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (slot != null) {
            mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
        if (this.shield) {
            mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), sequence, Rockstar.getInstance().getRotationHandler().getCurrentRotation().getYaw(), Rockstar.getInstance().getRotationHandler().getCurrentRotation().getPitch()));
        }
        this.additional = new Rotation(MathUtility.random(5.0, 20.0), MathUtility.random(5.0, 10.0));
        this.attackTimer.reset();
        if (this.smartCrits.isEnabled() && mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
            this.smartCritTimer.reset();
        }
        ++this.attacks;
    }

    private void rotateHead(LivingEntity targetedEntity) {
        if (this.onlyWeapon.isEnabled() && !EntityUtility.isHoldingWeapon()) {
            return;
        }
        if (this.rotationMode.is(this.noRotation)) {
            return;
        }
        MoveCorrection moveCorrection = this.moveCorrectionMode.is(this.silentMoveCorrection) ? MoveCorrection.SILENT : (this.moveCorrectionMode.is(this.directMoveCorrection) ? MoveCorrection.DIRECT : MoveCorrection.NONE);
        RotationHandler handler = Rockstar.getInstance().getRotationHandler();
        if (this.rotationMode.is(this.simpleRotation)) {
            Vec3d vec3d;
            PlayerEntity player;
            if (Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isEnabled() && targetedEntity instanceof PlayerEntity) {
                player = (PlayerEntity)targetedEntity;
                vec3d = ElytraPredictionSystem.predictPlayerPosition(player);
            } else {
                vec3d = targetedEntity.getPos();
            }
            Rotation rot = RotationMath.getRotationTo(RotationMath.getNearestPoint(targetedEntity, vec3d));
            if (mc.player.getEyePos().distanceTo(targetedEntity.getEyePos()) > 3.0) {
                Vec3d vec3d2;
                if (Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isEnabled() && targetedEntity instanceof PlayerEntity) {
                    player = (PlayerEntity)targetedEntity;
                    vec3d2 = ElytraPredictionSystem.predictPlayerPosition(player);
                } else {
                    vec3d2 = targetedEntity.getPos();
                }
                rot.setYaw(RotationMath.getRotationTo(vec3d2.add(0.0, (double)targetedEntity.getEyeHeight(targetedEntity.getPose()), 0.0)).getYaw());
            }
            handler.rotate(rot, moveCorrection, 180.0f, 180.0f, 180.0f, RotationPriority.TO_TARGET);
        }
        if (this.rotationMode.is(this.neuroRotation)) {
            Rotation targetRot = RotationMath.getRotationTo(RotationMath.getNearestPoint(targetedEntity));
            Rotation currentRot = handler.getCurrentRotation();
            Rotation neyroRot = NeyroSmoothMode.limitAngleChange(currentRot, targetRot, targetedEntity);
            float yawDiff = Math.abs(RotationMath.getAngleDifference(currentRot.getYaw(), neyroRot.getYaw()));
            float pitchDiff = Math.abs(neyroRot.getPitch() - currentRot.getPitch());
            handler.rotate(neyroRot, moveCorrection, Math.max(8.0f, yawDiff), Math.max(6.0f, pitchDiff), 80.0f, RotationPriority.TO_TARGET);
            return;
        }
        if (this.rotationMode.is(this.holyWorldRotation)) {
            Rotation current = Rockstar.getInstance().getRotationHandler().getCurrentRotation();
            Box box = targetedEntity.getBoundingBox();
            double offsetX = (double)this.getSensitivity((float)Math.cos((double)System.currentTimeMillis() / 1000.0)) * 0.15;
            double offsetY = (double)this.getSensitivity((float)Math.cos((double)System.currentTimeMillis() / 10000.0)) * 0.15;
            double offsetZ = (double)this.getSensitivity((float)Math.cos((double)System.currentTimeMillis() / 1000.0)) * 0.15;
            Vec3d nearY = RotationMath.getNearestPoint(targetedEntity);
            Vec3d targetPos = new Vec3d(nearY.x, MathHelper.clamp((double)MathUtility.interpolate(mc.player.getY(), targetedEntity.getEyeY(), 0.5), (double)targetedEntity.getBoundingBox().minY, (double)targetedEntity.getBoundingBox().maxY), nearY.z);
            double clampedX = MathHelper.clamp((double)(targetPos.getX() + offsetX), (double)box.minX, (double)box.maxX);
            double clampedY = targetPos.getY() + (double)(targetedEntity.getHeight() / 2.0f) + offsetY;
            double clampedZ = MathHelper.clamp((double)(targetPos.getZ() + offsetZ), (double)box.minZ, (double)box.maxZ);
            Vec3d vec = new Vec3d(clampedX, clampedY, clampedZ).subtract(mc.player.getEyePos());
            float yawToTarget = (float)MathHelper.wrapDegrees((double)(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0));
            float yawDelta = MathHelper.wrapDegrees((float)(yawToTarget - current.getYaw()));
            float yaw = current.getYaw() + yawDelta;
            float pitch = Math.clamp(current.getPitch(), -90.0f, 90.0f);
            if (!MathUtility.canTraceWithBlock(this.attackDistance.getCurrentValue(), yaw, pitch, (Entity)mc.player, (Entity)targetedEntity, !this.walls.isEnabled()) && this.rayTrace.isEnabled()) {
                pitch = RotationMath.getRotationTo(targetPos).getPitch();
            }
            handler.rotate(new Rotation(yaw, pitch), moveCorrection, 180.0f, 180.0f, 180.0f, RotationPriority.TO_TARGET);
        }
        if (this.rotationMode.is(this.funTimeRotation) || this.rotationMode.is(this.intaveRotation)) {
            if (mc.player.age % 500 == 0) {
                this.noise = new PerlinNoise();
                this.noiseFactor = 1.0f;
            }
            Vec3d nearY = RotationMath.getNearestPoint(targetedEntity);
            Rotation targetRot = RotationMath.getRotationTo(new Vec3d(nearY.x, MathHelper.clamp((double)MathUtility.interpolate(mc.player.getY(), targetedEntity.getEyeY(), 0.4), (double)targetedEntity.getBoundingBox().minY, (double)targetedEntity.getBoundingBox().maxY), nearY.z));
            Rotation multipoint = RotationMath.getRotationTo(RotationMath.getNearestPoint(targetedEntity));
            boolean idle = this.attackTimer.finished(this.getTpsDelay(200L));
            if (this.additional == null) {
                this.additional = new Rotation(0.0f, 0.0f);
            }
            float targetYaw = targetRot.getYaw();
            float targetPitch = targetRot.getPitch();
            Rotation currentRot = handler.getCurrentRotation();
            float currentYaw = currentRot.getYaw();
            float currentPitch = currentRot.getPitch();
            float yawDiff = RotationMath.getAngleDifference(currentYaw, targetYaw);
            float pitchDiff = RotationMath.getAngleDifference(currentPitch, targetPitch);
            if (idle) {
                if (this.shouldPreventSprinting()) {
                    targetYaw += 0.0f;
                    targetPitch -= 10.0f;
                } else {
                    targetYaw -= 15.0f;
                }
            }
            if (!this.rotationMode.is(this.intaveRotation) && !idle) {
                targetYaw += this.additional.getYaw();
                targetPitch += this.additional.getPitch();
            }
            float yawSpeed = Math.max((180.0f - Math.abs(yawDiff)) / (idle ? (mc.player.fallDistance > 0.0f ? 20.0f : 60.0f) : 40.0f), MathUtility.random(21.2, 21.5)) * MathUtility.random(21.9, 30.1);
            float pitchSpeed = Math.abs(pitchDiff) / (idle ? (mc.player.fallDistance > 0.0f ? 60.0f : 100.0f) : 30.0f) * MathUtility.random(10.2, 10.5);
            long timeElapsed = System.currentTimeMillis() - this.rotationStartTime;
            float yawNoise = (float)this.noise.noise((double)timeElapsed * 5.0E-4);
            float pitchNoise = (float)this.noise.noise((double)timeElapsed * 5.0E-4, 10.0);
            float yawOffset = yawNoise * 90.0f * this.noiseFactor;
            float pitchOffset = pitchNoise * 90.0f * this.noiseFactor;
            float finalTargetYaw = targetYaw + yawOffset;
            float finalTargetPitch = targetPitch + pitchOffset;
            float totalDiff = Math.abs(yawDiff) + Math.abs(pitchDiff);
            if (totalDiff < 10.0f) {
                this.noiseFactor = Math.max(0.0f, this.noiseFactor - 0.05f);
            }
            handler.rotate(new Rotation(targetYaw, Math.clamp(targetPitch, -90.0f, 90.0f)), moveCorrection, yawSpeed * 25.0f, pitchSpeed * 25.0f, MathUtility.random(5.0, 50.0), RotationPriority.TO_TARGET);
        }
        if (this.rotationMode.is(this.funTimeTestRotation)) {
            Vec3d nearY = RotationMath.getNearestPoint(targetedEntity);
            Rotation targetRot = RotationMath.getRotationTo(new Vec3d(nearY.x, MathHelper.clamp((double)MathUtility.interpolate(mc.player.getY(), targetedEntity.getEyeY(), 0.45), (double)targetedEntity.getBoundingBox().minY, (double)targetedEntity.getBoundingBox().maxY), nearY.z));
            Rotation currentRot = handler.getCurrentRotation();
            float targetYaw = targetRot.getYaw();
            float targetPitch = targetRot.getPitch();
            float yawDiff = RotationMath.getAngleDifference(currentRot.getYaw(), targetYaw);
            float pitchDiff = RotationMath.getAngleDifference(currentRot.getPitch(), targetPitch);
            double time = (double)(System.currentTimeMillis() % 10000L) / 1000.0;
            float swayYaw = (float)Math.sin(time * 1.7) * 6.0f;
            float swayPitch = (float)Math.cos(time * 1.3) * 4.0f;
            if (mc.player.getEyePos().distanceTo(targetedEntity.getEyePos()) > 3.0) {
                targetYaw += swayYaw;
                targetPitch += swayPitch;
            } else {
                targetYaw += swayYaw * 0.6f;
                targetPitch += swayPitch * 0.6f;
            }
            float yawSpeed = Math.max((120.0f - Math.abs(yawDiff)) / 35.0f, MathUtility.random(6.0, 8.0)) * MathUtility.random(7.0, 9.0);
            float pitchSpeed = Math.max(Math.abs(pitchDiff) / 25.0f, MathUtility.random(4.0, 6.0)) * MathUtility.random(6.0, 8.0);
            handler.rotate(new Rotation(targetYaw, Math.clamp(targetPitch, -90.0f, 90.0f)), moveCorrection, yawSpeed * 20.0f, pitchSpeed * 20.0f, MathUtility.random(12.0, 30.0), RotationPriority.TO_TARGET);
        }
        if (this.rotationMode.is(this.spookyTimeRotation)) {
            // Если в AuraLegacy выбран SpookyTime, используем обновленную отдельную обработку.
            this.rotateSpookyTime(targetedEntity, handler, moveCorrection);
        }
    }

    // Вся логика SpookyTime-ротации вынесена сюда, чтобы режим было проще настраивать и читать.
    private void rotateSpookyTime(LivingEntity targetedEntity, RotationHandler handler, MoveCorrection moveCorrection) {
        // 500 тиков - примерно 25 секунд при 20 TPS; обновляем шум, чтобы рисунок ротации не застывал.
        if (mc.player.age % 500 == 0) {
            // Создаем новый PerlinNoise, который дает плавное, а не резкое случайное смещение.
            this.noise = new PerlinNoise();
            // 1.0f - полный вес шума после обновления или потери цели.
            this.noiseFactor = 1.0f;
        }

        // 1.0f расширяет хитбокс цели по X/Z и помогает понять, что игрок уже в ближнем контакте.
        boolean collide = EntityUtility.collideWith(targetedEntity, 1.0f);
        // Повторно сохраняем проверку без расширения, чтобы таймер коллизии не дергался на краю хитбокса.
        boolean hardCollide = EntityUtility.collideWith(targetedEntity);
        // Проверяем блок над головой: AIR значит можно делать небольшой верхний оффсет без упора в потолок.
        boolean hasHeadRoom = EntityUtility.getBlock(0.0, 2.0, 0.0) == Blocks.AIR;
        // Сохраняем критическое состояние один раз, чтобы sprint-логика не пересчитывалась разными случайными задержками.
        boolean sprintLocked = this.shouldPreventSprinting();
        // Берем ближайшую точку хитбокса, потому что она стабильнее центра на маленькой дистанции.
        Vec3d nearestPoint = RotationMath.getNearestPoint(targetedEntity);
        // Дистанция до глаз цели помогает ослаблять оффсеты, когда цель уже рядом.
        double eyeDistance = mc.player.getEyePos().distanceTo(targetedEntity.getEyePos());
        // 0.42/0.55/0.48 - вертикальный вес: ниже в клинче, выше на дальней дистанции, середина в обычной ситуации.
        float yBlend = collide ? 0.42f : (eyeDistance > 3.0 ? 0.55f : 0.48f);
        // minY + 0.05 и maxY - 0.05 не дают целиться ровно в грань хитбокса, где трассировка часто шумит.
        double targetY = MathHelper.clamp(
                MathUtility.interpolate(mc.player.getY(), targetedEntity.getEyeY(), yBlend),
                targetedEntity.getBoundingBox().minY + 0.05,
                targetedEntity.getBoundingBox().maxY - 0.05
        );
        // Собираем основную точку наведения из ближайших X/Z и сглаженной Y.
        Vec3d aimPoint = new Vec3d(nearestPoint.x, targetY, nearestPoint.z);
        // В клинче целимся ближе к корпусу: 0.38 высоты выглядит стабильнее, чем старый жесткий Y=0.5.
        Vec3d collisionPoint = targetedEntity.getPos().add(0.0, MathHelper.clamp(targetedEntity.getHeight() * 0.38f, 0.35f, 0.75f), 0.0);
        // Если есть контакт, берем корпус; иначе работаем по ближайшей точке хитбокса.
        Rotation targetRot = RotationMath.getRotationTo(collide ? collisionPoint : aimPoint);
        // Мультипоинт нужен как запасной pitch, когда основной луч упирается в блок или край хитбокса.
        Rotation multipoint = RotationMath.getRotationTo(RotationMath.getNearestPoint(targetedEntity));
        // rayTrace включаем с текущей дистанцией атаки, чтобы ротация не уходила в точку, которую нельзя ударить.
        boolean canTrace = MathUtility.canTraceWithBlock(
                this.attackDistance.getCurrentValue(),
                targetRot.getYaw(),
                targetRot.getPitch(),
                (Entity)mc.player,
                (Entity)targetedEntity,
                !this.walls.isEnabled()
        );
        // Если дальняя цель не трассируется, мягко поднимаем pitch к мультипоинту вместо старого резкого +10.
        if (!canTrace && eyeDistance > 3.0) {
            // 0.55 - вес запасного pitch; 8 градусов дают небольшой запас над краем хитбокса.
            targetRot.setPitch(MathUtility.interpolate(targetRot.getPitch(), multipoint.getPitch() + 8.0f, 0.55f));
        }

        // 450 мс в контакте и 220 мс без контакта задают паузу; getTpsDelay сохраняет legacy TPS-sync.
        boolean idle = this.attackTimer.finished(this.getTpsDelay(collide ? 450L : 220L));
        // Если после атаки дополнительный оффсет еще не создан, начинаем с нулевого значения.
        if (this.additional == null) {
            // 0/0 означает отсутствие пост-атачного сдвига yaw/pitch.
            this.additional = new Rotation(0.0f, 0.0f);
        }

        // Целевой yaw из рассчитанной ротации.
        float targetYaw = targetRot.getYaw();
        // Целевой pitch из рассчитанной ротации.
        float targetPitch = targetRot.getPitch();
        // Текущая серверная/тихая ротация из общего RotationHandler.
        Rotation currentRot = handler.getCurrentRotation();
        // Текущий yaw нужен для расчета кратчайшей дельты.
        float currentYaw = currentRot.getYaw();
        // Текущий pitch нужен для расчета вертикальной дельты.
        float currentPitch = currentRot.getPitch();
        // yawDiff хранит кратчайшую разницу в градусах с учетом перехода через 180/-180.
        float yawDiff = RotationMath.getAngleDifference(currentYaw, targetYaw);
        // pitchDiff хранит вертикальную разницу в градусах.
        float pitchDiff = RotationMath.getAngleDifference(currentPitch, targetPitch);
        // Если крит не удерживает спринт, idle активен и над головой пусто, добавляем небольшой человеческий отвод.
        if (!sprintLocked && idle && hasHeadRoom) {
            // Синус дает плавный отвод yaw в пределах примерно 4-8 градусов вместо постоянных старых 10.
            targetYaw += 6.0f + (float)Math.sin(mc.player.age * 0.18f) * 2.0f;
            // Pitch уводим вверх на 10-14 градусов, меньше старых 20, чтобы не было резкого рывка.
            targetPitch -= 12.0f + (float)Math.cos(mc.player.age * 0.16f) * 2.0f;
        }

        // Базовая скорость yaw растет от ошибки: чем дальше цель от прицела, тем быстрее доводка.
        float yawSpeed = MathHelper.clamp(Math.abs(yawDiff) * 0.42f + (idle ? 8.0f : 4.0f), 5.0f, idle ? 48.0f : 32.0f);
        // Базовая скорость pitch мягче yaw, чтобы вертикальная ротация не прыгала.
        float pitchSpeed = MathHelper.clamp(Math.abs(pitchDiff) * 0.35f + (idle ? 3.5f : 2.0f), 3.0f, idle ? 28.0f : 18.0f);
        // 0.92-1.08 добавляет маленький разброс скорости без старого сильного дерганья.
        yawSpeed *= MathUtility.random(0.92, 1.08);
        // 0.92-1.08 добавляет такой же маленький разброс для pitch.
        pitchSpeed *= MathUtility.random(0.92, 1.08);
        // Если настоящего пересечения больше нет, сбрасываем таймер клинча.
        if (!hardCollide) {
            // Сброс нужен, чтобы stalin-ветка не включалась после выхода из хитбокса.
            this.collideTimer.reset();
        }

        // Через 450 мс плотного контакта включаем защитный режим против застревания на цели.
        if (this.collideTimer.finished(this.getTpsDelay(450L)) && CombatUtility.stalin(targetedEntity)) {
            // stalinPitch держит взгляд в безопасном диапазоне -20..82, без старого почти вертикального снапа.
            float stalinPitch = MathHelper.clamp(35.0f + (float)(mc.player.getY() - targetedEntity.getY()) * 8.0f + (float)Math.sin(mc.player.age * 0.45f) * 6.0f, -20.0f, 82.0f);
            // 0.55 смешивает текущий pitch с защитным, чтобы переход был заметным, но не мгновенным.
            targetPitch = MathUtility.interpolate(targetPitch, stalinPitch, 0.55f);
            // 0.45 замедляет yaw в клинче, чтобы не пилить головой при тесном столкновении.
            yawSpeed *= 0.45f;
        }

        // После удара добавляем сохраненный дополнительный оффсет, но только когда нет idle и клинча.
        if (!idle && hasHeadRoom && !collide) {
            // 0.65 оставляет часть пост-атачного yaw-сдвига, не давая ему увести прицел слишком далеко.
            targetYaw += this.additional.getYaw() * 0.65f;
            // 0.45 ослабляет pitch-сдвиг, потому что вертикальные оффсеты заметнее античиту и игроку.
            targetPitch += this.additional.getPitch() * 0.45f;
        }

        // Если стены включены, точка не видна и падение еще не достаточно для крита, уводим pitch вниз мягче.
        if (this.walls.isEnabled() && !MathUtility.canSeen(nearestPoint) && mc.player.fallDistance <= CombatUtility.getFallDistance(targetedEntity)) {
            // -70 заменяет старые -90, чтобы камера не падала строго вертикально.
            targetPitch = MathUtility.interpolate(targetPitch, -70.0f, 0.45f);
            // 0.75 дополнительно замедляет yaw при wall-сценарии, чтобы возврат был плавнее.
            yawSpeed *= 0.75f;
        }

        // Считаем, сколько миллисекунд прошло с начала текущего паттерна ротации.
        long timeElapsed = System.currentTimeMillis() - this.rotationStartTime;
        // 5.0E-4 делает шум медленным: значение меняется плавно, а не каждый тик как Random.
        float yawNoise = (float)this.noise.noise((double)timeElapsed * 5.0E-4);
        // Второй аргумент 10.0 сдвигает канал pitch, чтобы yaw и pitch не повторяли одну волну.
        float pitchNoise = (float)this.noise.noise((double)timeElapsed * 5.0E-4, 10.0);
        // Чем меньше суммарная ошибка, тем сильнее режем шум, чтобы на цели прицел не плавал.
        float noiseScale = MathHelper.clamp((Math.abs(yawDiff) + Math.abs(pitchDiff)) / 60.0f, 0.25f, 1.0f);
        // 10 градусов - максимум yaw-шума до умножения на noiseFactor и noiseScale.
        float yawOffset = yawNoise * 10.0f * this.noiseFactor * noiseScale;
        // 6 градусов - максимум pitch-шума, меньше yaw, чтобы вертикаль оставалась спокойной.
        float pitchOffset = pitchNoise * 6.0f * this.noiseFactor * noiseScale;
        // Финальный yaw состоит из цели и плавного шумового смещения.
        float finalTargetYaw = targetYaw + yawOffset;
        // Финальный pitch состоит из цели и плавного шумового смещения.
        float finalTargetPitch = targetPitch + pitchOffset;
        // totalDiff нужен, чтобы постепенно выключать шум, когда ротация уже почти на цели.
        float totalDiff = Math.abs(yawDiff) + Math.abs(pitchDiff);
        // 12 градусов - зона стабилизации, в которой шум начинает затухать.
        if (totalDiff < 12.0f) {
            // 0.04 за тик гасит noiseFactor плавнее старых 0.05 и меньше дергает при входе в цель.
            this.noiseFactor = Math.max(0.0f, this.noiseFactor - 0.04f);
        }

        // Отправляем готовую ротацию в общий обработчик.
        handler.rotate(
                // Pitch ограничиваем -89..89, чтобы не ловить крайние вертикальные значения камеры.
                new Rotation(finalTargetYaw, Math.clamp(finalTargetPitch, -89.0f, 89.0f)),
                // moveCorrection сохраняет выбранный режим коррекции движения.
                moveCorrection,
                // yawSpeed уже в градусах за тик, без старого множителя 25.
                yawSpeed,
                // pitchSpeed уже в градусах за тик, без старого множителя 25.
                pitchSpeed,
                // 35-60 градусов задают умеренный возврат после завершения задачи ротации.
                MathUtility.random(35.0, 60.0),
                // TO_TARGET оставляет приоритет цели выше обычных ротаций, но ниже override-задач.
                RotationPriority.TO_TARGET
        );
    }

    public float getGCDValue() {
        double sensitivity = (Double)mc.options.getMouseSensitivity().getValue();
        double value = sensitivity * 0.6 + 0.2;
        double result = Math.pow(value, 3.0) * 0.8;
        return (float)result * 0.15f;
    }

    public float getSensitivity(float rot) {
        return this.getDeltaMouse(rot) * this.getGCDValue();
    }

    public float getDeltaMouse(float delta) {
        return Math.round(delta / this.getGCDValue());
    }

    public boolean shouldPreventSprinting() {
        boolean predict;
        LivingEntity living;
        LivingEntity target;
        Entity target1 = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        LivingEntity livingEntity = target = target1 instanceof LivingEntity ? (living = (LivingEntity)target1) : null;
        if (target == null || mc.player == null) {
            return false;
        }
        if (this.styleAttack.is(this.fastPvp)) {
            return false;
        }
        Criticals criticals = Rockstar.getInstance().getModuleManager().getModule(Criticals.class);
        boolean bl = predict = criticals.isEnabled() && (criticals.canCritical() || mc.player.isOnGround()) || !mc.player.isOnGround() && FallingPlayer.fromPlayer(mc.player).findFall(CombatUtility.getFallDistance(target));
        long delay = ServerUtility.isHW() || ServerUtility.isST() ? (long)MathUtility.random(50.0, 150.0) : 50L;
        return this.onlyCriticals.isEnabled() && this.isCriticalRequired(target) && (predict || CombatUtility.canPerformCriticalHit(target, true) || !this.attackTimer.finished(this.getTpsDelay(delay)));
    }

    private boolean inRange(LivingEntity target) {
        return MathHelper.sqrt((float)((float)mc.player.squaredDistanceTo(RotationMath.getNearestPoint(target)))) > this.attackDistance.getCurrentValue();
    }

    @Override
    public void onEnable() {
        this.rotationStartTime = System.currentTimeMillis();
        this.noise = new PerlinNoise();
        this.noiseFactor = 1.0f;
        this.lastUpdateMs = System.currentTimeMillis();
        if (this.rotationMode.is(this.neuroRotation)) {
            NeyroManager.INSTANCE.startPlayback();
        }
    }

    @Override
    public void onDisable() {
        NeyroManager.INSTANCE.stopPlayback();
        super.onDisable();
    }

    @Generated
    public ModeSetting.Value getFastPvp() {
        return this.fastPvp;
    }

    @Generated
    public ModeSetting.Value getSlowPvp() {
        return this.slowPvp;
    }

    @Generated
    public Timer getAttackTimer() {
        return this.attackTimer;
    }

    @Generated
    public int getAttacks() {
        return this.attacks;
    }

}
