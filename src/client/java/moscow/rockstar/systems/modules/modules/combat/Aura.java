package moscow.rockstar.systems.modules.modules.combat;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

@ModuleInfo(name = "Aura", category = ModuleCategory.COMBAT, desc = "Бьёт женщин и детей")
public class Aura extends BaseModule implements IMinecraft {
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
    private ModeSetting.Value funTimeRotation;
    private ModeSetting.Value spookyTimeRotation;
    private ModeSetting.Value holyWorldRotation;
    private ModeSetting.Value intaveRotation;
    private ModeSetting.Value neuroRotation;
    private ModeSetting.Value customRotation;

    // ─── Custom mode tunables (visible only when Custom is selected) ───
    private SliderSetting customSmoothing;     // Сглаживание 0.00-1.5
    private SliderSetting customShake;         // Тряска 0.00-1.5
    private SliderSetting customSticky;        // Прилипание 0.00-1.5
    private SliderSetting customSmoothness;    // Плавность 0.00-1.5 (только если сглаживание == 0)
    private SliderSetting customRotationSpeed; // Скорость ротации 0.1-2.0
    private SliderSetting customHitAngle;      // Угол удара 0-360
    private ModeSetting customHitAngleMode;    // Плавная / Снап
    private ModeSetting.Value customAngleSmooth;
    private ModeSetting.Value customAngleSnap;
    private SliderSetting customYBlend;        // вертикальный вес точки наведения
    private SliderSetting customNoiseScale;    // вес перлин-шума
    private SliderSetting customIdleYaw;       // отвод yaw в простое
    private SliderSetting customIdlePitch;     // отвод pitch в простое
    private SliderSetting customMaxYawStep;    // потолок шага yaw (град/тик)
    private SliderSetting customMaxPitchStep;  // потолок шага pitch (град/тик)
    private SliderSetting customAccel;         // ускорение доводки (доля от разницы углов)
    private SliderSetting customRandomization; // гуманизация: случайный разброс скорости 0-1
    private SliderSetting customReturnSpeed;   // скорость возврата головы
    private SliderSetting customPitchClamp;    // лимит pitch (наклон головы)
    private BooleanSetting customPredict;       // упреждение по скорости цели
    private SliderSetting customPredictStrength; // сила упреждения 0-1
    private BooleanSetting customKeepRotation;  // держать ротацию на цели между ударами

    // ─── Sprint reset ───
    private ModeSetting sprintReset;
    private ModeSetting.Value sprintReset1;    // как сейчас (crit-based)
    private ModeSetting.Value sprintReset2;    // таймер сброса при ударе
    private SliderSetting sprintResetTime;     // 0.0-1.0 сек
    private long lastAttackForSprintMs;

    private ModeSetting moveCorrectionMode;
    private ModeSetting.Value noMoveCorrection;
    private ModeSetting.Value directMoveCorrection;
    private ModeSetting.Value silentMoveCorrection;
    private ModeSetting styleAttack;
    private ModeSetting.Value fastPvp;
    private ModeSetting.Value slowPvp;
    private BooleanSetting onlyCriticals;
    private BooleanSetting walls;
    private BooleanSetting rayTrace;
    private BooleanSetting onlyWeapon;
    private BooleanSetting targeting;


    private final Animation nononoYaw = new Animation(300L, Easing.LINEAR);
    private final Animation nononoPitch = new Animation(1000L, Easing.LINEAR);
    private Timer attackTimer;
    boolean shield;
    private PerlinNoise noise = new PerlinNoise();
    private long rotationStartTime = 0L;
    private float noiseFactor = 0.0F;
    private int attacks;
    private Rotation additional;
    private final Timer collideTimer = new Timer();

    private final EventListener<ClientPlayerTickEvent> onPlayerTick = event -> {
        if (mc.player != null) {
            // Sprint reset mode 2: гасим спринт в окне после удара (настраиваемое время).
            if (this.sprintReset != null && this.sprintReset.is(this.sprintReset2) && this.lastAttackForSprintMs > 0L) {
                long window = (long) (this.sprintResetTime.getCurrentValue() * 1000.0F);
                if (System.currentTimeMillis() - this.lastAttackForSprintMs <= window) {
                    mc.player.setSprinting(false);
                } else {
                    this.lastAttackForSprintMs = 0L;
                }
            }
            float requiredAimDistance = Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isEnabled()
                    ? 50.0F
                    : this.aimDistance.getCurrentValue();

            TargetSettings.Builder builder = new TargetSettings.Builder()
                    .targetPlayers(this.players.isSelected())
                    .targetAnimals(this.animals.isSelected())
                    .targetMobs(this.mobs.isSelected())
                    .targetInvisibles(this.invisibles.isSelected())
                    .targetNakedPlayers(this.nakedPlayers.isSelected())
                    .targetFriends(this.friends.isSelected())
                    .requiredRange(requiredAimDistance);

            if (this.sortingMode.is(this.distanceSorting)) {
                builder.sortBy(TargetComparators.DISTANCE);
            } else if (this.sortingMode.is(this.healthSorting)) {
                builder.sortBy(TargetComparators.HEALTH);
            } else if (this.sortingMode.is(this.fovSorting)) {
                builder.sortBy(TargetComparators.FOV);
            }

            TargetSettings settings = builder.build();
            LivingEntity target = Rockstar.getInstance().getTargetManager().getCurrentTarget() instanceof LivingEntity living ? living : null;

            if (!this.targeting.isEnabled()
                    || target == null
                    || MathHelper.sqrt((float)mc.player.squaredDistanceTo(RotationMath.getNearestPoint(target))) > requiredAimDistance
                    || !mc.world.hasEntity(target)
                    || !target.isAlive()) {
                Rockstar.getInstance().getTargetManager().update(settings);
            }

            if (target != null) {
                this.rotateHead(target);
                if (this.shouldAttackEntity(target)) {
                    this.attack(target);
                }

                NeyroManager manager = NeyroManager.INSTANCE;
                if (manager.isRecording()) {
                    manager.performTrainingAttack();
                    manager.recordFrame();
                }
            } else {
                this.rotationStartTime = System.currentTimeMillis();
                this.noise = new PerlinNoise();
                this.noiseFactor = 1.0F;
            }
        }
    };

    public Aura() {
        this.initialize();
    }

    @VMProtect(type = VMProtectType.VIRTUALIZATION)
    private void initialize() {
        this.rotationMode = new ModeSetting(this, "modules.settings.aura.rotationMode");
        this.noRotation = new ModeSetting.Value(this.rotationMode, "modules.settings.aura.noRotation");
        this.simpleRotation = new ModeSetting.Value(this.rotationMode, "modules.settings.aura.simpleRotation").select();
        this.customRotation = new ModeSetting.Value(this.rotationMode, "modules.settings.aura.customRotation");
        this.funTimeRotation = new ModeSetting.Value(this.rotationMode, "FunTime");
        this.spookyTimeRotation = new ModeSetting.Value(this.rotationMode, "SpookyTime");
        this.holyWorldRotation = new ModeSetting.Value(this.rotationMode, "HolyWorld");
        this.intaveRotation = new ModeSetting.Value(this.rotationMode, "Intave");
        this.neuroRotation = new ModeSetting.Value(this.rotationMode, "Neyro");

        this.attackDistance = new SliderSetting(this, "modules.settings.aura.attackDistance")
                .min(0.1F)
                .max(6.0F)
                .step(0.1F)
                .currentValue(3.0F)
                .suffix(number -> " %s".formatted(Localizator.translate("block")) + TextUtility.makeCountTranslated(number));
        this.aimDistance = new SliderSetting(this, "modules.settings.aura.aimDistance")
                .min(0.1F)
                .max(6.0F)
                .step(0.1F)
                .currentValue(3.0F)
                .suffix(number -> " %s".formatted(Localizator.translate("block")) + TextUtility.makeCountTranslated(number));
        this.onlyCriticals = new BooleanSetting(this, "only_crits");
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

        // ─── Custom mode settings (hidden unless Custom rotation selected) ───
        // Дефолты подобраны под SpookyTime и СПЕЦИАЛЬНО быстрые: угол удара 360°,
        // лёгкое сглаживание и высокая скорость — чтобы килка не «фотографировала»
        // (смотрела на цель, но не била). Угол 360 убирает FOV-гейт удара.
        java.util.function.BooleanSupplier custom = () -> !this.customRotation.isSelected();
        this.customSmoothing = new SliderSetting(this, "modules.settings.aura.custom.smoothing", custom)
                .min(0.0F).max(1.5F).step(0.01F).currentValue(0.15F);
        this.customSmoothness = new SliderSetting(this, "modules.settings.aura.custom.smoothness",
                () -> !this.customRotation.isSelected() || this.customSmoothing.getCurrentValue() > 0.0F)
                .min(0.0F).max(1.5F).step(0.01F).currentValue(0.3F);
        this.customShake = new SliderSetting(this, "modules.settings.aura.custom.shake", custom)
                .min(0.0F).max(1.5F).step(0.01F).currentValue(0.15F);
        this.customSticky = new SliderSetting(this, "modules.settings.aura.custom.sticky", custom)
                .min(0.0F).max(1.5F).step(0.01F).currentValue(0.6F);
        this.customRotationSpeed = new SliderSetting(this, "modules.settings.aura.custom.rotationSpeed", custom)
                .min(0.1F).max(2.0F).step(0.01F).currentValue(1.5F);
        this.customHitAngle = new SliderSetting(this, "modules.settings.aura.custom.hitAngle", custom)
                .min(0.0F).max(360.0F).step(1.0F).currentValue(360.0F).suffix("°");
        this.customHitAngleMode = new ModeSetting(this, "modules.settings.aura.custom.hitAngleMode", custom);
        this.customAngleSmooth = new ModeSetting.Value(this.customHitAngleMode, "modules.settings.aura.custom.angleSmooth").select();
        this.customAngleSnap = new ModeSetting.Value(this.customHitAngleMode, "modules.settings.aura.custom.angleSnap");
        this.customYBlend = new SliderSetting(this, "modules.settings.aura.custom.yBlend", custom)
                .min(0.0F).max(1.0F).step(0.01F).currentValue(0.5F);
        this.customNoiseScale = new SliderSetting(this, "modules.settings.aura.custom.noiseScale", custom)
                .min(0.0F).max(1.0F).step(0.01F).currentValue(0.4F);
        this.customIdleYaw = new SliderSetting(this, "modules.settings.aura.custom.idleYaw", custom)
                .min(0.0F).max(30.0F).step(0.5F).currentValue(6.0F).suffix("°");
        this.customIdlePitch = new SliderSetting(this, "modules.settings.aura.custom.idlePitch", custom)
                .min(0.0F).max(30.0F).step(0.5F).currentValue(12.0F).suffix("°");
        this.customMaxYawStep = new SliderSetting(this, "modules.settings.aura.custom.maxYawStep", custom)
                .min(1.0F).max(180.0F).step(1.0F).currentValue(60.0F).suffix("°");
        this.customMaxPitchStep = new SliderSetting(this, "modules.settings.aura.custom.maxPitchStep", custom)
                .min(1.0F).max(180.0F).step(1.0F).currentValue(40.0F).suffix("°");
        this.customAccel = new SliderSetting(this, "modules.settings.aura.custom.accel", custom)
                .min(0.1F).max(1.0F).step(0.01F).currentValue(0.55F);
        this.customRandomization = new SliderSetting(this, "modules.settings.aura.custom.randomization", custom)
                .min(0.0F).max(1.0F).step(0.01F).currentValue(0.1F);
        this.customReturnSpeed = new SliderSetting(this, "modules.settings.aura.custom.returnSpeed", custom)
                .min(1.0F).max(90.0F).step(1.0F).currentValue(45.0F).suffix("°");
        this.customPitchClamp = new SliderSetting(this, "modules.settings.aura.custom.pitchClamp", custom)
                .min(30.0F).max(90.0F).step(1.0F).currentValue(89.0F).suffix("°");
        this.customPredict = new BooleanSetting(this, "modules.settings.aura.custom.predict", custom);
        this.customPredictStrength = new SliderSetting(this, "modules.settings.aura.custom.predictStrength",
                () -> !this.customRotation.isSelected() || !this.customPredict.isEnabled())
                .min(0.0F).max(1.0F).step(0.01F).currentValue(0.5F);
        this.customKeepRotation = new BooleanSetting(this, "modules.settings.aura.custom.keepRotation", custom);

        // ─── Sprint reset (2 modes) ───
        this.sprintReset = new ModeSetting(this, "modules.settings.aura.sprintReset");
        this.sprintReset1 = new ModeSetting.Value(this.sprintReset, "modules.settings.aura.sprintReset.mode1").select();
        this.sprintReset2 = new ModeSetting.Value(this.sprintReset, "modules.settings.aura.sprintReset.mode2");
        this.sprintResetTime = new SliderSetting(this, "modules.settings.aura.sprintReset.time",
                () -> !this.sprintReset2.isSelected())
                .min(0.0F).max(1.0F).step(0.01F).currentValue(0.3F).suffix("s");

        this.attackTimer = new Timer();
    }

    private MoveCorrection getMoveCorrection() {
        if (this.moveCorrectionMode.is(this.silentMoveCorrection)) {
            return MoveCorrection.SILENT;
        } else if (this.moveCorrectionMode.is(this.directMoveCorrection)) {
            return MoveCorrection.DIRECT;
        }
        return MoveCorrection.NONE;
    }

    private void rotateHead(LivingEntity targetedEntity) {
        if (!this.onlyWeapon.isEnabled() || EntityUtility.isHoldingWeapon()) {


            if (!this.rotationMode.is(this.noRotation)) {

                MoveCorrection moveCorrection = getMoveCorrection();
                RotationHandler handler = Rockstar.getInstance().getRotationHandler();

                if (this.rotationMode.is(this.simpleRotation)) {
                    Rotation rot = RotationMath.getRotationTo(
                            RotationMath.getNearestPoint(
                                    targetedEntity,
                                    Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isEnabled() && targetedEntity instanceof PlayerEntity player
                                            ? ElytraPredictionSystem.predictPlayerPosition(player)
                                            : targetedEntity.getPos()
                            )
                    );
                    if (mc.player.getEyePos().distanceTo(targetedEntity.getEyePos()) > 3.0) {
                        rot.setYaw(
                                RotationMath.getRotationTo(
                                                (Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isEnabled()
                                                        && targetedEntity instanceof PlayerEntity playerx
                                                        ? ElytraPredictionSystem.predictPlayerPosition(playerx)
                                                        : targetedEntity.getPos())
                                                        .add(0.0, targetedEntity.getEyeHeight(targetedEntity.getPose()), 0.0)
                                        )
                                        .getYaw()
                        );
                    }
                    handler.rotate(rot, moveCorrection, 180.0F, 180.0F, 180.0F, RotationPriority.TO_TARGET);
                }

                if (this.rotationMode.is(this.holyWorldRotation)) {
                    Rotation current = Rockstar.getInstance().getRotationHandler().getCurrentRotation();
                    Box box = targetedEntity.getBoundingBox();
                    double offsetX = this.getSensitivity((float)Math.cos(System.currentTimeMillis() / 1000.0)) * 0.15;
                    double offsetY = this.getSensitivity((float)Math.cos(System.currentTimeMillis() / 10000.0)) * 0.15;
                    double offsetZ = this.getSensitivity((float)Math.cos(System.currentTimeMillis() / 1000.0)) * 0.15;
                    Vec3d nearY = RotationMath.getNearestPoint(targetedEntity);
                    Vec3d targetPos = new Vec3d(
                            nearY.x,
                            MathHelper.clamp(
                                    MathUtility.interpolate(mc.player.getY(), targetedEntity.getEyeY(), 0.5),
                                    targetedEntity.getBoundingBox().minY,
                                    targetedEntity.getBoundingBox().maxY
                            ),
                            nearY.z
                    );
                    double clampedX = MathHelper.clamp(targetPos.getX() + offsetX, box.minX, box.maxX);
                    double clampedY = targetPos.getY() + targetedEntity.getHeight() / 2.0F + offsetY;
                    double clampedZ = MathHelper.clamp(targetPos.getZ() + offsetZ, box.minZ, box.maxZ);
                    Vec3d vec = new Vec3d(clampedX, clampedY, clampedZ).subtract(mc.player.getEyePos());
                    float yawToTarget = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
                    float yawDelta = MathHelper.wrapDegrees(yawToTarget - current.getYaw());
                    float yaw = current.getYaw() + yawDelta;
                    float pitch = Math.clamp(current.getPitch(), -90.0F, 90.0F);
                    if (!MathUtility.canTraceWithBlock(this.attackDistance.getCurrentValue(), yaw, pitch, mc.player, targetedEntity, !this.walls.isEnabled())
                            && this.rayTrace.isEnabled()) {
                        pitch = RotationMath.getRotationTo(targetPos).getPitch();
                    }
                    handler.rotate(new Rotation(yaw, pitch), moveCorrection, 180.0F, 180.0F, 180.0F, RotationPriority.TO_TARGET);
                }
                if (this.rotationMode.is(this.neuroRotation)) {
                    Rotation targetRot = RotationMath.getRotationTo(RotationMath.getNearestPoint(targetedEntity));
                    Rotation currentRot = handler.getCurrentRotation();
                    Rotation neyroRot = NeyroSmoothMode.limitAngleChange(currentRot, targetRot, targetedEntity);
                    float yawDiff = Math.abs(RotationMath.getAngleDifference(currentRot.getYaw(), neyroRot.getYaw()));
                    float pitchDiff = Math.abs(neyroRot.getPitch() - currentRot.getPitch());
                    handler.rotate(neyroRot, moveCorrection, Math.max(8.0F, yawDiff), Math.max(6.0F, pitchDiff), 80.0F, RotationPriority.TO_TARGET);
                    return;
                }
                if (this.rotationMode.is(this.funTimeRotation) || this.rotationMode.is(this.intaveRotation)) {
                    if (mc.player.age % 500 == 0) {
                        this.noise = new PerlinNoise();
                        this.noiseFactor = 1.0F;
                    }

                    Vec3d nearY = RotationMath.getNearestPoint(targetedEntity);
                    Rotation targetRot = RotationMath.getRotationTo(
                            new Vec3d(
                                    nearY.x,
                                    MathHelper.clamp(
                                            MathUtility.interpolate(mc.player.getY(), targetedEntity.getEyeY(), 0.5),
                                            targetedEntity.getBoundingBox().minY,
                                            targetedEntity.getBoundingBox().maxY
                                    ),
                                    nearY.z
                            )
                    );
                    Rotation multipoint = RotationMath.getRotationTo(RotationMath.getNearestPoint(targetedEntity));
                    boolean idle = this.attackTimer.finished(300L);
                    if (this.additional == null) {
                        this.additional = new Rotation(0.0F, 0.0F);
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
                            targetYaw += 5.0F;
                            targetPitch -= 10.0F;
                        } else {
                            targetYaw -= 5.0F;
                        }
                    }

                    if (!this.rotationMode.is(this.intaveRotation) && !idle) {
                        targetYaw += this.additional.getYaw();
                        targetPitch += this.additional.getPitch();
                    }

                    float yawSpeed = Math.max(
                            (90.0F - Math.abs(yawDiff)) / (idle ? (mc.player.fallDistance > 0.0F ? 20.0F : 60.0F) : 40.0F), MathUtility.random(1.0, 5.0)
                    )
                            * MathUtility.random(0.9, 1.1);
                    float pitchSpeed = Math.abs(pitchDiff) / (idle ? (mc.player.fallDistance > 0.0F ? 60.0F : 100.0F) : 30.0F) * MathUtility.random(0.9, 1.1);
                    long timeElapsed = System.currentTimeMillis() - this.rotationStartTime;
                    float yawNoise = (float)this.noise.noise(timeElapsed * 5.0E-4);
                    float pitchNoise = (float)this.noise.noise(timeElapsed * 5.0E-4, 10.0);
                    float yawOffset = yawNoise * 25.0F * this.noiseFactor;
                    float pitchOffset = pitchNoise * 25.0F * this.noiseFactor;
                    float finalTargetYaw = targetYaw + yawOffset;
                    float finalTargetPitch = targetPitch + pitchOffset;
                    float totalDiff = Math.abs(yawDiff) + Math.abs(pitchDiff);
                    if (totalDiff < 10.0F) {
                        this.noiseFactor = Math.max(0.0F, this.noiseFactor - 0.05F);
                    }

                    handler.rotate(
                            new Rotation(targetYaw, Math.clamp(targetPitch, -90.0F, 90.0F)),
                            moveCorrection,
                            yawSpeed * 25.0F,
                            pitchSpeed * 25.0F,
                            MathUtility.random(5.0, 50.0),
                            RotationPriority.TO_TARGET
                    );
                }

                // Если в настройках Aura выбран режим ротации SpookyTime, запускаем его отдельную обработку.
                if (this.rotationMode.is(this.spookyTimeRotation)) {
                    // Передаем текущую цель, общий обработчик ротаций и выбранную коррекцию движения.
                    this.rotateSpookyTime(targetedEntity, handler, moveCorrection);
                }

                // Кастомная ротация: обход с плавной киллауры + гибкие настройки.
                if (this.rotationMode.is(this.customRotation)) {
                    this.rotateCustom(targetedEntity, handler, moveCorrection);
                }
            }
        }
    }

    /**
     * Custom-режим ротации: база — плавная (simple) аура, поверх неё накладываются
     * сглаживание/плавность, тряска (Perlin), прилипание и ограничение угла удара (Плавная/Снап).
     * Все значения берутся из слайдеров Custom-секции.
     */
    private void rotateCustom(LivingEntity targetedEntity, RotationHandler handler, MoveCorrection moveCorrection) {
        if (mc.player.age % 350 == 0 || this.noise == null) {
            this.noise = new PerlinNoise();
            this.noiseFactor = 1.0F;
        }

        boolean collide = EntityUtility.collideWith(targetedEntity, 1.0F);
        double eyeDistance = mc.player.getEyePos().distanceTo(targetedEntity.getEyePos());

        // ── точка наведения (как в плавной, с настраиваемым вертикальным весом) ──
        Vec3d nearestPoint = RotationMath.getNearestPoint(targetedEntity);
        float yBlend = this.customYBlend.getCurrentValue();
        double targetY = MathHelper.clamp(
                MathUtility.interpolate(mc.player.getY(), targetedEntity.getEyeY(), yBlend),
                targetedEntity.getBoundingBox().minY + 0.05,
                targetedEntity.getBoundingBox().maxY - 0.05
        );
        Vec3d aimPoint = new Vec3d(nearestPoint.x, targetY, nearestPoint.z);
        // Упреждение: добавляем смещение по горизонтальной скорости цели (относительно нашей).
        if (this.customPredict.isEnabled()) {
            Vec3d rel = targetedEntity.getVelocity().subtract(mc.player.getVelocity());
            double strength = this.customPredictStrength.getCurrentValue();
            aimPoint = aimPoint.add(rel.x * strength * 2.0, 0.0, rel.z * strength * 2.0);
        }
        // Прилипание: в клинче целимся ближе к корпусу, вес растёт со sticky.
        float sticky = this.customSticky.getCurrentValue();
        Vec3d collisionPoint = targetedEntity.getPos().add(0.0,
                MathHelper.clamp(targetedEntity.getHeight() * 0.36F, 0.35F, 0.75F), 0.0);
        Vec3d finalAim = collide ? aimPoint.lerp(collisionPoint, Math.min(1.0F, sticky)) : aimPoint;
        Rotation targetRot = RotationMath.getRotationTo(finalAim);

        Rotation currentRot = handler.getCurrentRotation();
        float curYaw = currentRot.getYaw();
        float curPitch = currentRot.getPitch();
        float targetYaw = targetRot.getYaw();
        float targetPitch = targetRot.getPitch();

        // ── тряска (Perlin jitter) ──
        float shake = this.customShake.getCurrentValue();
        if (shake > 0.0F && this.noise != null) {
            long t = System.currentTimeMillis() - this.rotationStartTime;
            float yawNoise = (float) this.noise.noise(t * 5.0E-4);
            float pitchNoise = (float) this.noise.noise(t * 5.0E-4, 10.0);
            float noiseScale = this.customNoiseScale.getCurrentValue();
            targetYaw += yawNoise * 12.0F * shake * noiseScale * this.noiseFactor;
            targetPitch += pitchNoise * 8.0F * shake * noiseScale * this.noiseFactor;
            float totalDiff = Math.abs(RotationMath.getAngleDifference(curYaw, targetYaw))
                    + Math.abs(RotationMath.getAngleDifference(curPitch, targetPitch));
            if (totalDiff < 12.0F) {
                this.noiseFactor = Math.max(0.0F, this.noiseFactor - 0.04F);
            }
        }

        // ── отвод в простое ──
        // Keep rotation: держим голову на цели между ударами (без отвода в простое).
        boolean idle = this.attackTimer.finished(collide ? 450L : 220L);
        boolean hasHeadRoom = EntityUtility.getBlock(0.0, 2.0, 0.0) == Blocks.AIR;
        if (idle && hasHeadRoom && !this.customKeepRotation.isEnabled() && !this.shouldPreventSprinting()) {
            targetYaw += this.customIdleYaw.getCurrentValue() * (0.6F + 0.4F * (float) Math.sin(mc.player.age * 0.2));
            targetPitch -= this.customIdlePitch.getCurrentValue();
        }

        // ── ограничение угла удара (FOV-конус) ──
        float hitAngle = this.customHitAngle.getCurrentValue();
        float yawDiff = RotationMath.getAngleDifference(curYaw, targetYaw);
        float pitchDiff = RotationMath.getAngleDifference(curPitch, targetPitch);
        boolean snap = this.customHitAngleMode.is(this.customAngleSnap);
        // Конус меряем от РЕАЛЬНОГО взгляда игрока (легит-FOV), а не от текущей серверной ротации,
        // иначе при silent-коррекции цель никогда не входит в конус и аура «залипает».
        float lookYaw = mc.player.getYaw();
        boolean withinAngle = hitAngle >= 360.0F || Math.abs(RotationMath.getAngleDifference(lookYaw, targetYaw)) <= hitAngle / 2.0F;
        // СНАП = легит-снап: голову двигаем только когда игрок САМ навёлся в конус.
        // ПЛАВНАЯ всегда плавно доводит к цели (конус ограничивает только удар — см. shouldAttackEntity).
        if (snap && !withinAngle) {
            return;
        }

        // ── скорость доводки ──
        float speedMul = this.customRotationSpeed.getCurrentValue();
        float maxYaw = this.customMaxYawStep.getCurrentValue();
        float maxPitch = this.customMaxPitchStep.getCurrentValue();
        float accel = this.customAccel.getCurrentValue();

        float yawSpeed;
        float pitchSpeed;
        if (snap) {
            // Снап: попали в конус — резко доводим (полный шаг, без сглаживания).
            yawSpeed = maxYaw * speedMul;
            pitchSpeed = maxPitch * speedMul;
        } else {
            // Плавная: мягко доводим в пределах конуса (наклон кривой = customAccel).
            yawSpeed = MathHelper.clamp(Math.abs(yawDiff) * accel + (idle ? 8.0F : 5.0F), 4.0F, maxYaw) * speedMul;
            pitchSpeed = MathHelper.clamp(Math.abs(pitchDiff) * (accel * 0.85F) + (idle ? 4.0F : 2.5F), 3.0F, maxPitch) * speedMul;

            // сглаживание / плавность (только для плавного режима)
            float smoothing = this.customSmoothing.getCurrentValue();
            if (smoothing > 0.0F) {
                float factor = MathHelper.clamp(1.0F - smoothing / 1.5F * 0.85F, 0.1F, 1.0F);
                yawSpeed *= factor;
                pitchSpeed *= factor;
            } else {
                float smoothness = this.customSmoothness.getCurrentValue();
                if (smoothness > 0.0F) {
                    float factor = MathHelper.clamp(1.0F - smoothness / 1.5F * 0.6F, 0.2F, 1.0F);
                    yawSpeed *= factor;
                    pitchSpeed *= factor;
                }
            }
        }

        // ── гуманизация: случайный разброс скорости ──
        float rnd = this.customRandomization.getCurrentValue();
        if (rnd > 0.0F) {
            yawSpeed *= (float) MathUtility.random(1.0 - rnd * 0.35, 1.0 + rnd * 0.35);
            pitchSpeed *= (float) MathUtility.random(1.0 - rnd * 0.35, 1.0 + rnd * 0.35);
        }

        float pitchLimit = this.customPitchClamp.getCurrentValue();
        handler.rotate(
                new Rotation(targetYaw, Math.clamp(targetPitch, -pitchLimit, pitchLimit)),
                moveCorrection,
                yawSpeed,
                pitchSpeed,
                this.customReturnSpeed.getCurrentValue(),
                RotationPriority.TO_TARGET
        );
    }

    // Вся логика SpookyTime-ротации вынесена сюда, чтобы режим было проще настраивать и читать.
    private void rotateSpookyTime(LivingEntity targetedEntity, RotationHandler handler, MoveCorrection moveCorrection) {
        if (mc.player.age % 350 == 0 || this.noise == null) {
            this.noise = new PerlinNoise();
            this.noiseFactor = 1.0F;
        }

        boolean collide = EntityUtility.collideWith(targetedEntity, 1.0F);
        boolean hardCollide = EntityUtility.collideWith(targetedEntity);
        boolean hasHeadRoom = EntityUtility.getBlock(0.0, 2.0, 0.0) == Blocks.AIR;
        boolean sprintLocked = this.shouldPreventSprinting();

        Vec3d nearestPoint = RotationMath.getNearestPoint(targetedEntity);
        double eyeDistance = mc.player.getEyePos().distanceTo(targetedEntity.getEyePos());

        float yBlend = collide ? 0.38F : (eyeDistance > 3.0 ? 0.55F : 0.48F);
        double targetY = MathHelper.clamp(
                MathUtility.interpolate(mc.player.getY(), targetedEntity.getEyeY(), yBlend),
                targetedEntity.getBoundingBox().minY + 0.05,
                targetedEntity.getBoundingBox().maxY - 0.05
        );

        Vec3d aimPoint = new Vec3d(nearestPoint.x, targetY, nearestPoint.z);
        Vec3d collisionPoint = targetedEntity.getPos().add(0.0,
                MathHelper.clamp(targetedEntity.getHeight() * 0.36F, 0.35F, 0.75F), 0.0);

        Rotation targetRot = RotationMath.getRotationTo(collide ? collisionPoint : aimPoint);
        Rotation multipoint = RotationMath.getRotationTo(RotationMath.getNearestPoint(targetedEntity));

        boolean canTrace = MathUtility.canTraceWithBlock(
                this.attackDistance.getCurrentValue(),
                targetRot.getYaw(),
                targetRot.getPitch(),
                mc.player,
                targetedEntity,
                !this.walls.isEnabled()
        );

        if (!canTrace && eyeDistance > 3.0) {
            targetRot.setPitch(MathUtility.interpolate(targetRot.getPitch(), multipoint.getPitch() + 9.0F, 0.55F));
        }

        boolean idle = this.attackTimer.finished(collide ? 450L : 220L);
        if (this.additional == null) {
            this.additional = new Rotation(0.0F, 0.0F);
        }

        float targetYaw = targetRot.getYaw();
        float targetPitch = targetRot.getPitch();

        // Spooky-шум
        if (this.noise != null) {
            double noiseVal = this.noise.noise(mc.player.age * 0.02, mc.player.getX() * 0.1, mc.player.getZ() * 0.1);
            targetYaw += (float)(noiseVal * 2.5 * this.noiseFactor);
            targetPitch += (float)(noiseVal * 1.8 * this.noiseFactor);
            this.noiseFactor = (float)Math.max(0.3, this.noiseFactor - 0.001);
        }

        Rotation currentRot = handler.getCurrentRotation();
        float currentYaw = currentRot.getYaw();
        float currentPitch = currentRot.getPitch();
        float yawDiff = RotationMath.getAngleDifference(currentYaw, targetYaw);
        float pitchDiff = RotationMath.getAngleDifference(currentPitch, targetPitch);

        if (!sprintLocked && idle && hasHeadRoom) {
            double phase1 = Math.sin(mc.player.age * 0.21);
            double phase2 = Math.cos(mc.player.age * 0.17);
            targetYaw += 7.0F + (float)phase1 * 3.0F;
            targetPitch -= 14.0F + (float)phase2 * 3.0F;
        }

        float yawSpeed = MathHelper.clamp(Math.abs(yawDiff) * 0.45F + (idle ? 10.0F : 6.0F), 6.0F, idle ? 52.0F : 36.0F);
        float pitchSpeed = MathHelper.clamp(Math.abs(pitchDiff) * 0.38F + (idle ? 4.5F : 3.0F), 4.0F, idle ? 32.0F : 22.0F);

        yawSpeed *= MathUtility.random(0.85, 1.15);
        pitchSpeed *= MathUtility.random(0.85, 1.15);

        // Замена randomInt(2,5): случайное целое от 2 до 5 через MathUtility.random или Math.random
        int randomTickInterval = 2 + (int)(Math.random() * 4); // 2,3,4,5
        if (mc.player.age % randomTickInterval == 0) {
            yawSpeed *= 1.0 + MathUtility.random(-0.15, 0.25);
            pitchSpeed *= 1.0 + MathUtility.random(-0.15, 0.2);
        }

        if (!hardCollide) {
            this.collideTimer.reset();
        }

        if (this.collideTimer.finished(450L) && CombatUtility.stalin(targetedEntity)) {
            float stalinPitch = MathHelper.clamp(
                    40.0F + (float)(mc.player.getY() - targetedEntity.getY()) * 10.0F
                            + (float)Math.sin(mc.player.age * 0.7) * 8.0F,
                    -25.0F, 85.0F);
            targetPitch = MathUtility.interpolate(targetPitch, stalinPitch, 0.6F);
            yawSpeed *= 0.4F;
        }

        if (!idle && hasHeadRoom && !collide) {
            targetYaw += this.additional.getYaw() * 0.7F;
            targetPitch += this.additional.getPitch() * 0.5F;
            this.additional.setYaw(this.additional.getYaw() * 0.98F);
            this.additional.setPitch(this.additional.getPitch() * 0.98F);
        }

        // Плавный доворот без rotateTowards – используем getAngleDifference
        float newYaw = smoothRotate(currentYaw, targetYaw, yawSpeed);
        float newPitch = smoothRotate(currentPitch, targetPitch, pitchSpeed);

        // Установка новой ротации: предположим, что у handler есть setCurrentRotation(Rotation)
        // или setYaw/setPitch по отдельности. Адаптируйте под свой RotationHandler.
        handler.setCurrentRotation(new Rotation(newYaw, newPitch));
        // Альтернатива: handler.setYaw(newYaw); handler.setPitch(newPitch);

        // Коррекция движения – если метода update нет, оставьте вызов существующего метода,
        // или закомментируйте эту строку, если moveCorrection не требуется.
        // Например, moveCorrection.update(handler, targetedEntity, collide);
        // Если у MoveCorrection есть метод apply или adjust, используйте его.
        if (moveCorrection != null) {
            // Пример: moveCorrection.apply(handler, targetedEntity, collide);
            // Если никакого подходящего метода нет, просто удалите этот блок.
        }
    }

    /**
     * Плавно доворачивает current к target с ограничением скорости speed.
     * Использует уже имеющуюся getAngleDifference для корректного перехода через 360°.
     */
    private float smoothRotate(float current, float target, float speed) {
        float diff = RotationMath.getAngleDifference(current, target);
        if (Math.abs(diff) <= speed) {
            return target;
        }
        return current + Math.signum(diff) * speed;
    }

    private boolean shouldAttackEntity(LivingEntity targetedEntity) {
        // Custom: бить только когда цель в выбранном угле удара (FOV от реального взгляда игрока).
        if (this.rotationMode.is(this.customRotation)) {
            float hitAngle = this.customHitAngle.getCurrentValue();
            if (hitAngle < 360.0F) {
                float targetYaw = RotationMath.getRotationTo(RotationMath.getNearestPoint(targetedEntity)).getYaw();
                if (Math.abs(RotationMath.getAngleDifference(mc.player.getYaw(), targetYaw)) > hitAngle / 2.0F) {
                    return false;
                }
            }
        }
        if (!this.isCooledDown()) {
            return false;
        } else if (this.onlyWeapon.isEnabled() && !EntityUtility.isHoldingWeapon()) {
            return false;
        } else if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return false;
        } else if (this.inRange(targetedEntity)) {
            return false;
        } else if (this.walls.isEnabled()
                && this.spookyTimeRotation.isSelected()
                && mc.world
                .raycast(
                        new RaycastContext(
                                mc.player.getEyePos(),
                                mc.player
                                        .getEyePos()
                                        .add(
                                                mc.player
                                                        .getRotationVector(-90.0F, Rockstar.getInstance().getRotationHandler().getCurrentRotation().getYaw())
                                                        .multiply(this.attackDistance.getCurrentValue())
                                        ),
                                ShapeType.COLLIDER,
                                FluidHandling.NONE,
                                mc.player
                        )
                )
                .getType()
                == Type.BLOCK) {
            return false;
        } else {
            return !MathUtility.canTraceWithBlock(
                    this.attackDistance.getCurrentValue(),
                    Rockstar.getInstance().getRotationHandler().getCurrentRotation().getYaw(),
                    Rockstar.getInstance().getRotationHandler().getCurrentRotation().getPitch(),
                    mc.player,
                    targetedEntity,
                    !this.walls.isEnabled()
            )
                    && this.rayTrace.isEnabled()
                    ? false
                    : !this.onlyCriticals.isEnabled() || !this.isCriticalRequired(targetedEntity) || CombatUtility.canPerformCriticalHit(targetedEntity, true);
        }
    }

    private boolean isCriticalRequired(LivingEntity targetedEntity) {
        float damage = this.calculateDamage(targetedEntity);
        return damage <= targetedEntity.getHealth();
    }

    public boolean isCooledDown() {
        if (mc.player == null) {
            return false;
        } else {
            return CombatUtility.getMace() != null
                    ? this.attackTimer.finished(500L)
                    : mc.player.getAttackCooldownProgress(1.5F) > 0.93F && this.attackTimer.finished(500L)
                    || this.fastPvp.isSelected() && this.attackTimer.finished(50L);
        }
    }

    public float calculateDamage(LivingEntity targetedEntity) {
        return 0.0F;
    }

    private void attack(LivingEntity targetedEntity) {
        if (mc.interactionManager != null && mc.player != null) {

            this.shield = mc.player.isUsingItem() && mc.player.getActiveItem().getItem().getUseAction(mc.player.getActiveItem()) == UseAction.BLOCK;
            if (this.shield) {
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            }

            if (CombatUtility.shouldBreakShield(targetedEntity) && CombatUtility.canBreakShield(targetedEntity)) {
                CombatUtility.tryBreakShield(targetedEntity);
            }

            HotbarSlot slot = CombatUtility.getMace();
            if (slot != null) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
                CombatUtility.tryBreakShield(targetedEntity);
            }

            mc.interactionManager.attackEntity(mc.player, targetedEntity);
            mc.player.swingHand(Hand.MAIN_HAND);

            if (slot != null) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }

            if (this.shield) {
                mc.interactionManager
                        .sendSequencedPacket(
                                mc.world,
                                sequence -> new PlayerInteractItemC2SPacket(
                                        mc.player.getActiveHand(),
                                        sequence,
                                        Rockstar.getInstance().getRotationHandler().getCurrentRotation().getYaw(),
                                        Rockstar.getInstance().getRotationHandler().getCurrentRotation().getPitch()
                                )
                        );
            }

            this.additional = new Rotation(MathUtility.random(5.0, 20.0), MathUtility.random(5.0, 10.0));
            this.attackTimer.reset();
            this.lastAttackForSprintMs = System.currentTimeMillis();
            this.attacks++;
        }
    }

    public float getGCDValue() {
        double sensitivity = (Double)mc.options.getMouseSensitivity().getValue();
        double value = sensitivity * 0.6 + 0.2;
        double result = Math.pow(value, 3.0) * 0.8;
        return (float)result * 0.15F;
    }

    public float getSensitivity(float rot) {
        return this.getDeltaMouse(rot) * this.getGCDValue();
    }

    public float getDeltaMouse(float delta) {
        return Math.round(delta / this.getGCDValue());
    }

    public boolean shouldPreventSprinting() {
        LivingEntity target = Rockstar.getInstance().getTargetManager().getCurrentTarget() instanceof LivingEntity living ? living : null;
        if (target == null || mc.player == null) {
            return false;
        } else if (this.sprintReset != null && this.sprintReset.is(this.sprintReset2)) {
            // Mode 2 управляет спринтом по таймеру в onPlayerTick — здесь ничего не делаем.
            return false;
        } else if (this.styleAttack.is(this.fastPvp)) {
            return false;
        } else {
            Criticals criticals = Rockstar.getInstance().getModuleManager().getModule(Criticals.class);
            boolean predict = criticals.isEnabled() && (criticals.canCritical() || mc.player.isOnGround())
                    || !mc.player.isOnGround() && FallingPlayer.fromPlayer(mc.player).findFall(CombatUtility.getFallDistance(target));
            return this.onlyCriticals.isEnabled()
                    && this.isCriticalRequired(target)
                    && (
                    predict
                            || CombatUtility.canPerformCriticalHit(target, true)
                            || !this.attackTimer.finished(!ServerUtility.isHW() && !ServerUtility.isST() ? 50L : (long)MathUtility.random(50.0, 150.0))
            );
        }
    }

    private boolean inRange(LivingEntity target) {
        return MathHelper.sqrt((float)mc.player.squaredDistanceTo(RotationMath.getNearestPoint(target))) > this.attackDistance.getCurrentValue();
    }

    @Override
    public void onEnable() {        this.rotationStartTime = System.currentTimeMillis();
        this.noise = new PerlinNoise();
        this.noiseFactor = 1.0F;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        NeyroManager.INSTANCE.stopPlayback();
        Rockstar.getInstance().getTargetManager().reset();
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
