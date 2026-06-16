package im.zov4ik.utils.features.aura.rotation;

import net.minecraft.entity.LivingEntity;
import im.zov4ik.features.module.Module;
import im.zov4ik.utils.features.aura.warp.Turns;
import im.zov4ik.utils.features.aura.warp.TurnsConnection;
import im.zov4ik.utils.math.task.TaskPriority;

public class RotationController {
    public static final RotationController INSTANCE = new RotationController();

    public void rotateTo(Angle.VecRotation vecRotation, LivingEntity entity, int reset, RotationConfig config, TaskPriority priority, Module provider) {
        Turns angle = new Turns(vecRotation.getAngle().getYaw(), vecRotation.getAngle().getPitch());
        Turns.VecRotation turnsVecRotation = new Turns.VecRotation(angle, vecRotation.getVec());
        TurnsConnection.INSTANCE.rotateTo(turnsVecRotation, entity, reset, config, priority, provider);
    }

    public void reset() {
        TurnsConnection.INSTANCE.reset();
    }
}
