package im.zov4ik.features.impl.combat;

import antidaunleak.api.annotation.Native;
import im.zov4ik.events.player.MotionEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.events.render.DrawEvent;
import im.zov4ik.events.render.WorldRenderEvent;
import im.zov4ik.features.impl.movement.AutoSprint;
import im.zov4ik.features.impl.movement.ElytraTarget;
import im.zov4ik.features.impl.movement.Strafe;
import im.zov4ik.features.impl.movement.TargetStrafe;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.geometry.Render3D;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.display.shape.implement.Arc;
import im.zov4ik.utils.features.aura.point.MultiPoint;
import im.zov4ik.utils.features.aura.rotations.constructor.LinearConstructor;
import im.zov4ik.utils.features.aura.rotations.constructor.RotateConstructor;
import im.zov4ik.utils.features.aura.rotations.impl.*;
import im.zov4ik.utils.features.aura.rotations.neyro.NeyroManager;
import im.zov4ik.utils.features.aura.rotations.neyro.NeyroSmoothMode;
import im.zov4ik.utils.features.aura.utils.MathAngle;
import im.zov4ik.utils.features.aura.warp.TurnsConfig;
import im.zov4ik.utils.features.aura.warp.Turns;
import im.zov4ik.utils.features.aura.warp.TurnsConnection;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.interactions.simulate.PlayerSimulation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.client.managers.event.types.EventType;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.*;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.math.task.TaskPriority;
import im.zov4ik.zov4ik;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.player.RotationUpdateEvent;
import im.zov4ik.display.hud.Notifications;
import im.zov4ik.utils.features.aura.striking.StrikeManager;
import im.zov4ik.utils.features.aura.striking.StrikerConstructor;
import im.zov4ik.utils.features.aura.target.TargetFinder;
import im.zov4ik.features.impl.render.Hud;
import im.zov4ik.utils.math.calc.Calculate;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Aura extends Module {

    private static final float RANGE_MARGIN = 0.253F;

    private static final Map<String, Integer> LEGIT_SPRINT_MAP = Map.of(
            "FunTime", 1,
            "Matrix", 1,
            "Snap", 1,
            "SpookyTime", 2,
            "HolyWorld", 2
    );

    public static Aura getInstance() {
        return Instance.get(Aura.class);
    }

    TargetFinder targetSelector = new TargetFinder();
    MultiPoint pointFinder = new MultiPoint();

    @NonFinal
    LivingEntity target, lastTarget;

    @NonFinal
    long shiftTapEndTime = 0;

    @NonFinal
    boolean waterTargetSprintLocked = false;

    public static boolean fakeRotate;

    @NonFinal
    @Getter
    public static float legitSprintNeed;

    @NonFinal
    private FovCircleRenderer fovCircleRenderer;

    SelectSetting aimMode = new SelectSetting("Наводка", "Выберите тип наводки")
            .value("FunTime", "Legit Snap", "ReallyWorld", "HolyWorld", "SpookyTime", "CakeWorld", "Neyro")
            .selected("FunTime");

    MultiSelectSetting targetType = new MultiSelectSetting("Тип таргета", "Фильтрует весь список целей по типу")
            .value("Players", "Mobs", "Animals", "Friends", "Armor Stand")
            .selected("Players", "Mobs", "Animals");

    SliderSettings attackRange = new SliderSettings("Дистанция удара", "Дальность атаки до цели")
            .setValue(3).range(1F, 6F);

    SliderSettings lookRange = new SliderSettings("Дополнительная дистанция поиска", "Диапазон поиска до цели")
            .setValue(1.5f).range(0F, 2F);

    MultiSelectSetting attackSetting = new MultiSelectSetting("Настройки", "Позволяет настроить работу функции")
            .value("Only Critical", "Break Shield", "UnPress Shield", "No Attack When Eat", "Ignore The Walls", "Fake Lag", "Hit Chance")
            .selected("Only Critical", "Break Shield");

    SliderSettings hitChance = new SliderSettings("Шанс удара в %", "Шанс удара по цели")
            .setValue(100).range(1F, 100F).visible(() -> attackSetting.isSelected("Hit Chance"));

    SelectSetting correctionType = new SelectSetting("Коррекции движения", "Выбор коррекции движения игрока")
            .value("Free", "Focused", "Not visible").selected("Free");

    SelectSetting sprintReset = new SelectSetting("Сброс спринта", "Выбор сброса спринта перед ударом")
            .value("Legit", "Packet").selected("Legit");

    BooleanSetting smartCrits = new BooleanSetting("Удары на земле", "Криты только при нажатии пробела")
            .setValue(true).visible(() -> attackSetting.isSelected("Only Critical"));

    public Aura() {
        super("Aura", ModuleCategory.COMBAT);
        setup(
                aimMode,
                correctionType,
                sprintReset,

                targetType,

                attackRange,
                lookRange,
                hitChance,

                attackSetting,
                smartCrits
        );
        fovCircleRenderer = new FovCircleRenderer();
        zov4ik.getInstance().getEventManager().register(fovCircleRenderer);
    }

    @Override
    public void activate() {
        super.activate();
        if (aimMode.isSelected("Neyro")) {
            NeyroManager.INSTANCE.startPlayback();
        }
    }

    @Override
    public void deactivate() {
        targetSelector.releaseTarget();
        target = null;
        waterTargetSprintLocked = false;
        packets.forEach(PlayerInteractionHelper::sendPacketWithOutEvent);
        packets.clear();
        NeyroManager.INSTANCE.stopPlayback();
        super.deactivate();
    }

    private final List<Packet<?>> packets = new CopyOnWriteArrayList<>();
    private Box box;
    public static int tickStop = -1;

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof EntityStatusS2CPacket status && status.getStatus() == 30) {
            Entity entity = status.getEntity(mc.world);
            if (entity != null && entity.equals(target) && Hud.getInstance().notificationSettings.isSelected("Break Shield")) {
                Notifications.getInstance().addList(Text.literal("Сломали щит игроку - ").append(entity.getDisplayName()), 5000);
            }
        }

        if (attackSetting.isSelected("Fake Lag") && target !=null) {
            if (PlayerInteractionHelper.nullCheck()) return;
            switch (e.getPacket()) {
                case PlayerRespawnS2CPacket respawn -> setState(false);
                case GameJoinS2CPacket join -> setState(false);
                case ClientStatusC2SPacket status when status.getMode().equals(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN) ->
                        setState(false);
                default -> {
                    if (e.isSend() && tickStop < 0) {
                        packets.add(e.getPacket());
                        e.cancel();
                    }
                }
            }
        }
    }

    public class FovCircleRenderer implements QuickImports {
        private float currentScale = 1.0f;
        private float targetScale = 1.0f;
        private float cachedDynamicFov = 33;
        private float lastFinalRadius = 0;

        @EventHandler
        public void drawEvent(DrawEvent e){
            if (mc.player == null || !aimMode.isSelected("Legit Snap") || !isState()) return;

            if (mc.options.getPerspective().isFirstPerson()) {
                MatrixStack matrix = e.getDrawContext().getMatrices();
                float middleW = mc.getWindow().getScaledWidth() / 2f;
                float middleH = mc.getWindow().getScaledHeight() / 2f;

                double fov = mc.options.getFov().getValue();
                fov = MathHelper.clamp(fov, 30, 110);

                float baseRadius = (float) MathHelper.lerp((fov - 30.0) / 80.0, 106.5, 65.0);
                float fovScale = (float) (450.0 / fov);
                float dynamicRadius = baseRadius * fovScale;

                targetScale = mc.player.isSprinting() ? 0.9f : 1f;
                currentScale = Calculate.interpolateSmooth(2.5, currentScale, targetScale);

                float finalRadius = dynamicRadius * currentScale;
                lastFinalRadius = finalRadius;

                float baseThickness = (float) MathHelper.lerp((fov - 30.0) / 80.0, 0.003, 0.015);

                arc.render(ShapeProperties.create(matrix, middleW - finalRadius / 2f, middleH - finalRadius / 2f, finalRadius, finalRadius)
                        .round(0.3F)
                        .thickness(baseThickness)
                        .end(360)
                        .color(ColorAssist.getColor(255, 255, 255, 255))
                        .build());

                cachedDynamicFov = calculateDynamicFov();
            }
        }

        private float calculateDynamicFov() {
            if (mc.player == null || mc.getWindow() == null) return 33;

            double fov = mc.options.getFov().getValue();
            fov = MathHelper.clamp(fov, 30, 110);

            float screenWidth = mc.getWindow().getScaledWidth();
            float screenHeight = mc.getWindow().getScaledHeight();

            float circleRadiusInPixels = lastFinalRadius / 2f;

            double horizontalFovRadians = Math.toRadians(fov);

            double pixelsPerRadian = screenWidth / horizontalFovRadians;

            double circleFovRadians = circleRadiusInPixels / pixelsPerRadian;

            float circleFovDegrees = (float) Math.toDegrees(circleFovRadians);

            circleFovDegrees *= 2.0f;

            return MathHelper.clamp(circleFovDegrees, 36, 360);
        }

        public float getCachedDynamicFov() {
            return cachedDynamicFov;
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (box != null && attackSetting.isSelected("Fake Lag") && target !=null) {
            Render3D.drawBox(box, ColorAssist.getClientColor(), 1);
        }
    }

    @EventHandler
    public void tick(TickEvent e) {
        if (PlayerInteractionHelper.nullCheck()) return;
        updateWaterTargetSprintLock();
        if (waterTargetSprintLocked) {
            disableSprintForWaterTarget();
        }
        if (target == null) return;

        tickStop--;
        if (tickStop >= 0 && !packets.isEmpty() && attackSetting.isSelected("Fake Lag")) {
            box = mc.player.getBoundingBox();
            packets.forEach(PlayerInteractionHelper::sendPacketWithOutEvent);
            packets.clear();
        }
        if (mc.player.distanceTo(target) > attackRange.getValue() && attackSetting.isSelected("Fake Lag")) {
            packets.forEach(PlayerInteractionHelper::sendPacketWithOutEvent);
            packets.clear();
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        try {
            if (aimMode.isSelected("FunTime") && zov4ik.getInstance().getFtCheckClient() != null) {
                zov4ik.getInstance().getFtCheckClient().checkAndWarnFunTime();
            }
        } catch (Exception ex) {
        }

        switch (e.getType()) {
            case EventType.PRE -> {
                target = updateTarget();
                updateWaterTargetSprintLock();
                if (waterTargetSprintLocked) {
                    disableSprintForWaterTarget();
                }
                if (target != null) {
                    rotateToTarget(getConfig());
                    lastTarget = target;
                }
            }
            case EventType.POST -> {
                if (target != null) {
                    zov4ik.getInstance().getAttackPerpetrator().performAttack(getConfig());
                }
            }
        }
    }

    public static boolean shouldRotate;

    private LivingEntity updateTarget() {
        TargetFinder.EntityFilter filter = new TargetFinder.EntityFilter(targetType.getSelected());
        float range = attackRange.getValue() + RANGE_MARGIN + (mc.player.isGliding() && ElytraTarget.getInstance().isState() ? ElytraTarget.getInstance().elytraFindRange.getValue() : lookRange.getValue());

        float dynamicFov = 360;
        if (aimMode.isSelected("Legit Snap") && fovCircleRenderer != null) {
            dynamicFov = fovCircleRenderer.getCachedDynamicFov();
        }

        targetSelector.searchTargets(mc.world.getEntities(), range, dynamicFov, attackSetting.isSelected("Ignore The Walls"));
        targetSelector.validateTarget(filter::isValid);
        return targetSelector.getCurrentTarget();
    }

    @Native(type = Native.Type.VMProtectBeginMutation)
    private void rotateToTarget(StrikerConstructor.AttackPerpetratorConfigurable config) {
        StrikeManager attackHandler = zov4ik.getInstance().getAttackPerpetrator().getAttackHandler();
        TurnsConnection controller = TurnsConnection.INSTANCE;
        Turns.VecRotation rotation = new Turns.VecRotation(config.getAngle(), config.getAngle().toVector());
        TurnsConfig rotationConfig = getRotationConfig();

        boolean elytraMode = mc.player.isGliding() && attackSetting.isSelected("Elytra possibilities");

        if (fakeRotate && target != null) {
            FakeAngle fake = new FakeAngle();
            Turns fakeRot = fake.limitAngleChange(controller.getRotation(), rotation.getAngle(), rotation.getVec(), target);
            controller.setFakeRotation(fakeRot);
        }
        fakeRotate = false;
        switch (aimMode.getSelected()) {


            case "FunTime" -> {
                if (attackHandler.canAttack(config, 5)) {
                    controller.clear();
                    controller.rotateTo(rotation, target, 40, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                }
            }

            case "HolyWorld" -> {
                if (attackHandler.canAttack(config, 10) || !attackHandler.getAttackTimer().finished(150)) {
                    controller.rotateTo(rotation, target, 10, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                }
            }

            case "Legit Snap" -> {
                if (attackHandler.canAttack(config, 1) || !attackHandler.getAttackTimer().finished(40)) {
                    controller.rotateTo(rotation, target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                }
            }

            case "ReallyWorld", "SpookyTime", "CakeWorld" -> {
                controller.rotateTo(rotation, target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
            }

            case "Neyro" -> {
                if (!NeyroManager.INSTANCE.isPlaying()) {
                    NeyroManager.INSTANCE.startPlayback();
                }
                controller.rotateTo(rotation, target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
            }

        };

        if (shouldRotate && !aimMode.isSelected("TriggerBot")) {
            controller.rotateTo(rotation, target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
        }

        if (elytraMode && !aimMode.isSelected("TriggerBot")) {
            controller.rotateTo(rotation, target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
        }
    }

    @NonFinal
    public boolean elytraStateForward = false;
    private boolean wasForwardPressed = false;

    public StrikerConstructor.AttackPerpetratorConfigurable getConfig() {
        float baseRange = attackRange.getValue() + RANGE_MARGIN;

        Pair<Vec3d, Box> pointData = pointFinder.computeVector(
                target,
                baseRange,
                TurnsConnection.INSTANCE.getRotation(),
                getSmoothMode().randomValue(),
                attackSetting.isSelected("Ignore The Walls")
        );

        Vec3d computedPoint = pointData.getLeft();
        Box hitbox = pointData.getRight();

        if (mc.player.isGliding() && ElytraTarget.getInstance().isState()) {
            Vec3d resolverPoint = ElytraTarget.getInstance().getResolverPosition();
            if (resolverPoint != null) {
                computedPoint = resolverPoint;
                hitbox = new Box(
                        resolverPoint.x - 0.3,
                        resolverPoint.y - 0.3,
                        resolverPoint.z - 0.3,
                        resolverPoint.x + 0.3,
                        resolverPoint.y + 0.3,
                        resolverPoint.z + 0.3
                );
            }
        }

        if (mc.player.isGliding() && target.isGliding()) {
            Vec3d targetVelocity = target.getVelocity();
            double targetSpeed = targetVelocity.horizontalLength();

            float leadTicks = 0;
            if (ElytraTarget.shouldElytraTarget) {
                leadTicks = ElytraTarget.getInstance().elytraForward.getValue();
            }

            if (targetSpeed > 0.35) {

                Vec3d predictedPos = target.getPos().add(targetVelocity.multiply(leadTicks));
                computedPoint = predictedPos.add(0, target.getHeight() / 2, 0);

                hitbox = new Box(
                        predictedPos.x - target.getWidth() / 2,
                        predictedPos.y,
                        predictedPos.z - target.getWidth() / 2,
                        predictedPos.x + target.getWidth() / 2,
                        predictedPos.y + target.getHeight(),
                        predictedPos.z + target.getWidth() / 2
                );
            }
        }

        Turns angle = MathAngle.fromVec3d(computedPoint.subtract(Objects.requireNonNull(mc.player).getEyePos()));

        return new StrikerConstructor.AttackPerpetratorConfigurable(
                target,
                angle,
                baseRange,
                attackSetting.getSelected(),
                aimMode,
                hitbox
        );
    }

    @Native(type = Native.Type.VMProtectBeginMutation)
    public TurnsConfig getRotationConfig() {
        boolean visibleCorrection = !correctionType.isSelected("Not visible");
        boolean freeCorrection = !aimMode.isSelected("Legit") && correctionType.isSelected("Free");
        if (TargetStrafe.getInstance().isState() && TargetStrafe.getInstance().mode.isSelected("Grim") && target !=null) { freeCorrection = false; }
        return new TurnsConfig(getSmoothMode(), visibleCorrection, freeCorrection);
    }

    @EventHandler
    public void onmotion(MotionEvent event) {

    }

    public RotateConstructor getSmoothMode() {
        if (mc.player.isGliding() && attackSetting.isSelected("Elytra possibilities") && !aimMode.isSelected("Trigger Bot")) {
            return new LinearConstructor();
        }
        return switch (aimMode.getSelected()) {
            case "FunTime" -> new FTAngle();
            case "HolyWorld" -> new HWAngle();
            case "HvH" -> new HAngle();
            case "CakeWorld" -> new LGAngle();
            case "SpookyTime" -> new SPAngle();
            case "ReallyWorld" -> new RWAngle();
            case "Snap" -> new SnapAngle();
            case "Legit Snap" -> new SnapAngle();
            case "Matrix" -> new MatrixAngle();
            case "Neyro" -> new NeyroSmoothMode();
            default -> new LinearConstructor();
        };
    }

    private void updateWaterTargetSprintLock() {
        waterTargetSprintLocked = shouldLockSprintInWater();
    }

    private boolean shouldLockSprintInWater() {
        return isState()
                && mc.player != null
                && target instanceof PlayerEntity
                && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater());
    }

    private void disableSprintForWaterTarget() {
        AutoSprint.tickStop = Math.max(AutoSprint.tickStop, 2);
        mc.options.sprintKey.setPressed(false);
        mc.player.setSprinting(false);
    }
}
