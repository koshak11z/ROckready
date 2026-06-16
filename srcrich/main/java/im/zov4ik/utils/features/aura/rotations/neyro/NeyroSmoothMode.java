package im.zov4ik.utils.features.aura.rotations.neyro;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import im.zov4ik.utils.features.aura.rotations.constructor.RotateConstructor;
import im.zov4ik.utils.features.aura.utils.MathAngle;
import im.zov4ik.utils.features.aura.warp.Turns;

public class NeyroSmoothMode extends RotateConstructor {

    public NeyroSmoothMode() {
        super("Neyro");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d aimVector, Entity targetEntity) {
        NeyroManager manager = NeyroManager.INSTANCE;
        if (!manager.isPlaying() || manager.getActiveRecording() == null || targetEntity == null) {
            Turns delta = MathAngle.calculateDelta(currentAngle, targetAngle);
            float stepYaw = MathHelper.clamp(delta.getYaw(), -25.0F, 25.0F);
            float stepPitch = MathHelper.clamp(delta.getPitch(), -15.0F, 15.0F);
            return new Turns(currentAngle.getYaw() + stepYaw, currentAngle.getPitch() + stepPitch).adjustSensitivity();
        }

        NeyroFrame frame = manager.getNextFrame();
        if (frame == null) {
            return currentAngle;
        }

        Turns delta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawError = delta.getYaw();
        float pitchError = delta.getPitch();
        float recordedDeltaYaw = frame.getDeltaYaw();
        float recordedDeltaPitch = frame.getDeltaPitch();

        float appliedYaw;
        float appliedPitch;

        if (Math.abs(yawError) < 2.0F && Math.abs(pitchError) < 1.0F) {
            appliedYaw = recordedDeltaYaw * 0.6F;
            appliedPitch = recordedDeltaPitch * 0.6F;
        } else {
            float yawInfluence = MathHelper.clamp(Math.abs(yawError) / 20.0F, 0.45F, 1.0F);
            float pitchInfluence = MathHelper.clamp(Math.abs(pitchError) / 15.0F, 0.45F, 1.0F);
            appliedYaw = Math.signum(yawError) * Math.abs(recordedDeltaYaw) * yawInfluence;
            appliedPitch = Math.signum(pitchError) * Math.abs(recordedDeltaPitch) * pitchInfluence;

            if (Math.abs(recordedDeltaYaw) < 0.3F) {
                appliedYaw = yawError * 0.25F;
            }
            if (Math.abs(recordedDeltaPitch) < 0.2F) {
                appliedPitch = pitchError * 0.22F;
            }
        }

        appliedYaw *= MathHelper.clamp(1.0F + Math.abs(frame.getYawVelocity()) * 12.0F, 0.7F, 3.5F);
        appliedPitch *= MathHelper.clamp(1.0F + Math.abs(frame.getPitchVelocity()) * 10.0F, 0.7F, 3.0F);

        appliedYaw = MathHelper.clamp(appliedYaw, -Math.abs(yawError), Math.abs(yawError));
        appliedPitch = MathHelper.clamp(appliedPitch, -Math.abs(pitchError), Math.abs(pitchError));

        return new Turns(
                currentAngle.getYaw() + appliedYaw,
                MathHelper.clamp(currentAngle.getPitch() + appliedPitch, -89.6F, 89.6F)
        ).adjustSensitivity();
    }

    @Override
    public Vec3d randomValue() {
        NeyroManager manager = NeyroManager.INSTANCE;
        if (manager.isPlaying() && manager.getActiveRecording() != null) {
            double timer = System.currentTimeMillis() * 0.001D;
            return new Vec3d(
                    0.04D + Math.sin(timer * 1.5D) * 0.10D,
                    0.06D + Math.sin(timer * 1.2D + 1.0D) * 0.08D,
                    0.03D + Math.sin(timer + 2.0D) * 0.06D
            );
        }
        return new Vec3d(0.06D, 0.08D, 0.05D);
    }
}
