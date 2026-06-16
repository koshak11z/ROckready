package im.zov4ik.utils.features.aura.rotations.impl;

import im.zov4ik.zov4ik;
import im.zov4ik.features.impl.combat.Aura;
import im.zov4ik.utils.features.aura.point.Vector;
import im.zov4ik.utils.features.aura.rotations.constructor.RotateConstructor;
import im.zov4ik.utils.features.aura.striking.StrikeManager;
import im.zov4ik.utils.features.aura.utils.MathAngle;
import im.zov4ik.utils.features.aura.warp.Turns;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.utils.math.time.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.system.MathUtil;

import java.security.SecureRandom;

public class LGAngle extends RotateConstructor {
    public LGAngle() {
        super("CakeWorld");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        StrikeManager attackHandler = zov4ik.getInstance().getAttackPerpetrator().getAttackHandler();
        Aura aura = Aura.getInstance();
        StopWatch attackTimer = attackHandler.getAttackTimer();

        Turns angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw(), pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        boolean canAttack = entity != null && aura.getTarget() != null && attackHandler.canAttack(aura.getConfig(), 0);

        float distanceToTarget = 0;
        if (entity != null) {
            distanceToTarget = (float) mc.player.distanceTo(entity);
        }

        float baseSpeed = canAttack ? 0.93F : 0.56F;

        float speed = baseSpeed;
        if (distanceToTarget > 0 && distanceToTarget < 0.66F) {
            float closeRangeSpeed = MathHelper.clamp(distanceToTarget / 1.5F * 0.35F, 0.1F, 0.6F);
            speed = canAttack ? 0.85f : Math.min(speed, closeRangeSpeed);
        }
        float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
        float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);
        float jitterYaw = canAttack ? 0 : (float) (randomLerp(20, 26) * Math.sin(System.currentTimeMillis() / 25D));
        float jitterPitch = canAttack ? 0 : (float) (randomLerp(8, 23) * Math.sin(System.currentTimeMillis() / 27D));

        if ((!aura.isState() || aura.getTarget() == null) && attackHandler.getAttackTimer().finished(1000)) {
            baseSpeed = 0.35F;
            jitterYaw = 0;
            jitterPitch = 0;
        }
        float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);
        Turns moveAngle = new Turns(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(MathHelper.lerp(baseSpeed, currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + jitterYaw);
        moveAngle.setPitch(MathHelper.lerp(baseSpeed, currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + jitterPitch);

        return moveAngle;
    }

    public static float lerp(float delta, float start, float end) {
        return end;
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.01, 0.07, 0.02);
    }
}
