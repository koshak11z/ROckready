package moscow.rockstar.systems.modules.modules.combat.neyro;

import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public final class NeyroSmoothMode {
    private NeyroSmoothMode() {}

    public static Rotation limitAngleChange(Rotation currentAngle, Rotation targetAngle, Entity targetEntity) {
        NeyroManager manager = NeyroManager.INSTANCE;
        if (!manager.isPlaying() || manager.getActiveRecording() == null || targetEntity == null) {
            float yawDelta = RotationMath.getAngleDifference(currentAngle.getYaw(), targetAngle.getYaw());
            float pitchDelta = targetAngle.getPitch() - currentAngle.getPitch();
            float stepYaw = MathHelper.clamp(yawDelta, -25.0f, 25.0f);
            float stepPitch = MathHelper.clamp(pitchDelta, -15.0f, 15.0f);
            return RotationMath.correctRotation(new Rotation(currentAngle.getYaw() + stepYaw, MathHelper.clamp(currentAngle.getPitch() + stepPitch, -89.6f, 89.6f)));
        }
        NeyroFrame frame = manager.getNextFrame();
        if (frame == null) return currentAngle;
        float yawError = RotationMath.getAngleDifference(currentAngle.getYaw(), targetAngle.getYaw());
        float pitchError = targetAngle.getPitch() - currentAngle.getPitch();
        float recordedDeltaYaw = frame.getDeltaYaw();
        float recordedDeltaPitch = frame.getDeltaPitch();
        float appliedYaw;
        float appliedPitch;
        if (Math.abs(yawError) < 2.0f && Math.abs(pitchError) < 1.0f) {
            appliedYaw = recordedDeltaYaw * 0.6f;
            appliedPitch = recordedDeltaPitch * 0.6f;
        } else {
            float yawInfluence = MathHelper.clamp(Math.abs(yawError) / 20.0f, 0.45f, 1.0f);
            float pitchInfluence = MathHelper.clamp(Math.abs(pitchError) / 15.0f, 0.45f, 1.0f);
            appliedYaw = Math.signum(yawError) * Math.abs(recordedDeltaYaw) * yawInfluence;
            appliedPitch = Math.signum(pitchError) * Math.abs(recordedDeltaPitch) * pitchInfluence;
            if (Math.abs(recordedDeltaYaw) < 0.3f) appliedYaw = yawError * 0.25f;
            if (Math.abs(recordedDeltaPitch) < 0.2f) appliedPitch = pitchError * 0.22f;
        }
        appliedYaw *= MathHelper.clamp(1.0f + Math.abs(frame.getYawVelocity()) * 12.0f, 0.7f, 3.5f);
        appliedPitch *= MathHelper.clamp(1.0f + Math.abs(frame.getPitchVelocity()) * 10.0f, 0.7f, 3.0f);
        appliedYaw = MathHelper.clamp(appliedYaw, -Math.abs(yawError), Math.abs(yawError));
        appliedPitch = MathHelper.clamp(appliedPitch, -Math.abs(pitchError), Math.abs(pitchError));
        return RotationMath.correctRotation(new Rotation(currentAngle.getYaw() + appliedYaw, MathHelper.clamp(currentAngle.getPitch() + appliedPitch, -89.6f, 89.6f)));
    }
}
