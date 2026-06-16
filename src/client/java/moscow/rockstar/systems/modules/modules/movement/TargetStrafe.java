package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.rotations.RotationHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Target Strafe", category = ModuleCategory.MOVEMENT, desc = "Стрейф вокруг текущей цели")
public class TargetStrafe extends BaseModule {
    private final ModeSetting mode = new ModeSetting(this, "Режим");
    private final ModeSetting.Value matrix = new ModeSetting.Value(this.mode, "Matrix").select();
    private final ModeSetting.Value grim = new ModeSetting.Value(this.mode, "Grim");

    private final ModeSetting type = new ModeSetting(this, "Точка ходьбы", () -> !this.grim.isSelected());
    private final ModeSetting.Value grimCube = new ModeSetting.Value(this.type, "Cube").select();
    private final ModeSetting.Value grimCenter = new ModeSetting.Value(this.type, "Center");
    private final ModeSetting.Value grimCircle = new ModeSetting.Value(this.type, "Circle");

    private final ModeSetting typeMatrix = new ModeSetting(this, "Точка для обхода", () -> !this.matrix.isSelected());
    private final ModeSetting.Value matrixCube = new ModeSetting.Value(this.typeMatrix, "Cube");
    private final ModeSetting.Value matrixCircle = new ModeSetting.Value(this.typeMatrix, "Circle").select();

    private final SliderSetting grimRadius = new SliderSetting(this, "Радиус обхода", () -> !this.grim.isSelected()).min(0.1f).max(1.5f).step(0.01f).currentValue(0.87f);
    private final SliderSetting radius = new SliderSetting(this, "Радиус", () -> !this.matrix.isSelected()).min(0.1f).max(7.0f).step(0.1f).currentValue(2.5f);
    private final SliderSetting speed = new SliderSetting(this, "Скорость", () -> !this.matrix.isSelected()).min(0.1f).max(1.0f).step(0.01f).currentValue(0.3f);

    private final SelectSetting settings = new SelectSetting(this, "Настройки");
    private final SelectSetting.Value autoJump = new SelectSetting.Value(this.settings, "Auto Jump").select();
    private final SelectSetting.Value onlyKeyPressed = new SelectSetting.Value(this.settings, "Only Key Pressed");
    private final SelectSetting.Value inFront = new SelectSetting.Value(this.settings, "In front of the target");
    private final SelectSetting.Value directionModeEnabled = new SelectSetting.Value(this.settings, "Direction Mode");

    private final ModeSetting directionMode = new ModeSetting(this, "Направление", () -> !this.directionModeEnabled.isSelected());
    private final ModeSetting.Value clockwise = new ModeSetting.Value(this.directionMode, "Clockwise").select();
    private final ModeSetting.Value counterclockwise = new ModeSetting.Value(this.directionMode, "Counterclockwise");
    private final ModeSetting.Value random = new ModeSetting.Value(this.directionMode, "Random");

    private int pointIndex;

    private final EventListener<InputEvent> onInput = event -> {
        if (mc.player == null || mc.world == null || !this.grim.isSelected()) return;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        if (this.onlyKeyPressed.isSelected() && !hasMovementKey()) return;

        Vec3d playerPos = mc.player.getPos();
        Vec3d targetPos = target.getPos();
        double r = this.grimRadius.getCurrentValue();
        int direction = getDirectionMultiplier();
        Vec3d nextPoint = getGrimPoint(playerPos, targetPos, target, r, direction);
        Vec3d dir = nextPoint.subtract(playerPos).normalize();
        float yaw = getServerYaw();
        float movementAngle = (float)Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0f;
        float angleDiff = MathHelper.wrapDegrees(movementAngle - yaw);

        float forward = 0.0f;
        float strafe = 0.0f;
        if (angleDiff >= -22.5f && angleDiff < 22.5f) { forward = 1.0f; }
        else if (angleDiff >= 22.5f && angleDiff < 67.5f) { forward = 1.0f; strafe = -1.0f; }
        else if (angleDiff >= 67.5f && angleDiff < 112.5f) { strafe = -1.0f; }
        else if (angleDiff >= 112.5f && angleDiff < 157.5f) { forward = -1.0f; strafe = -1.0f; }
        else if (angleDiff >= -67.5f && angleDiff < -22.5f) { forward = 1.0f; strafe = 1.0f; }
        else if (angleDiff >= -112.5f && angleDiff < -67.5f) { strafe = 1.0f; }
        else if (angleDiff >= -157.5f && angleDiff < -112.5f) { forward = -1.0f; strafe = 1.0f; }
        else { forward = -1.0f; }
        event.setForward(forward);
        event.setStrafe(strafe);
        if (this.autoJump.isSelected() && mc.player.isOnGround()) event.setJump(true);
    };

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null || mc.world == null || !this.matrix.isSelected()) return;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        if (this.onlyKeyPressed.isSelected() && !hasMovementKey()) return;
        if (this.autoJump.isSelected() && mc.player.isOnGround()) mc.player.jump();

        Vec3d playerPos = mc.player.getPos();
        Vec3d targetPos = target.getPos();
        double r = this.radius.getCurrentValue();
        int direction = getDirectionMultiplier();
        Vec3d nextPoint;
        if (this.inFront.isSelected()) {
            float targetYaw = target.getYaw();
            double x = targetPos.x - Math.sin(Math.toRadians(targetYaw)) * r * direction;
            double z = targetPos.z + Math.cos(Math.toRadians(targetYaw)) * r * direction;
            nextPoint = new Vec3d(x, playerPos.y, z);
        } else if (this.matrixCube.isSelected()) {
            nextPoint = getCubePoint(playerPos, targetPos, r, direction);
        } else {
            double angle = Math.atan2(playerPos.z - targetPos.z, playerPos.x - targetPos.x);
            angle += direction * this.speed.getCurrentValue() / Math.max(playerPos.distanceTo(targetPos), r);
            nextPoint = new Vec3d(targetPos.x + r * Math.cos(angle), playerPos.y, targetPos.z + r * Math.sin(angle));
        }
        Vec3d dir = nextPoint.subtract(playerPos).normalize();
        float yaw = (float)Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0f;
        double motionSpeed = this.speed.getCurrentValue();
        mc.player.setVelocity(-Math.sin(Math.toRadians(yaw)) * motionSpeed, mc.player.getVelocity().y, Math.cos(Math.toRadians(yaw)) * motionSpeed);
    };

    private LivingEntity getTarget() {
        return Rockstar.getInstance().getTargetManager().getCurrentTarget() instanceof LivingEntity living ? living : null;
    }

    private boolean hasMovementKey() {
        return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
    }

    private int getDirectionMultiplier() {
        if (!this.directionModeEnabled.isSelected()) return 1;
        if (this.counterclockwise.isSelected()) return -1;
        if (this.random.isSelected()) return (System.currentTimeMillis() / 3000L) % 2L == 0L ? 1 : -1;
        return 1;
    }

    private float getServerYaw() {
        RotationHandler handler = Rockstar.getInstance().getRotationHandler();
        return handler.isIdling() ? mc.player.getYaw() : handler.getCurrentRotation().getYaw();
    }

    private Vec3d getGrimPoint(Vec3d playerPos, Vec3d targetPos, LivingEntity target, double r, int direction) {
        if (this.inFront.isSelected()) {
            float targetYaw = target.getYaw();
            if (this.grimCenter.isSelected()) {
                return targetPos.add(-Math.sin(Math.toRadians(targetYaw)) * r * direction, 0.0, Math.cos(Math.toRadians(targetYaw)) * r * direction);
            }
            double offset = Math.cos(System.currentTimeMillis() / 500.0) * r * direction;
            return targetPos.add(-Math.sin(Math.toRadians(targetYaw)) * r + Math.cos(Math.toRadians(targetYaw)) * offset, 0.0, Math.cos(Math.toRadians(targetYaw)) * r + Math.sin(Math.toRadians(targetYaw)) * offset);
        }
        if (this.grimCircle.isSelected()) {
            double baseAngle = (System.currentTimeMillis() % 3600L) / 3600.0 * 4.0 * Math.PI;
            double angle = direction > 0 ? baseAngle : 2.0 * Math.PI - baseAngle;
            return new Vec3d(targetPos.x + Math.cos(angle) * r, playerPos.y, targetPos.z + Math.sin(angle) * r);
        }
        if (this.grimCube.isSelected()) return getCubePoint(playerPos, targetPos, r, direction);
        return new Vec3d(targetPos.x, playerPos.y, targetPos.z);
    }

    private Vec3d getCubePoint(Vec3d playerPos, Vec3d targetPos, double r, int direction) {
        Vec3d[] points = new Vec3d[]{
                new Vec3d(targetPos.x - r, playerPos.y, targetPos.z - r),
                new Vec3d(targetPos.x - r, playerPos.y, targetPos.z + r),
                new Vec3d(targetPos.x + r, playerPos.y, targetPos.z + r),
                new Vec3d(targetPos.x + r, playerPos.y, targetPos.z - r)
        };
        if (playerPos.distanceTo(points[this.pointIndex]) < 0.5) {
            this.pointIndex = (this.pointIndex + direction + points.length) % points.length;
        }
        return points[this.pointIndex];
    }
}
