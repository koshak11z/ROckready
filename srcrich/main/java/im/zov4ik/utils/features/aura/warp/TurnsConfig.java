package im.zov4ik.utils.features.aura.warp;

import im.zov4ik.features.impl.movement.Strafe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import im.zov4ik.utils.features.aura.rotations.constructor.RotateConstructor;
import im.zov4ik.utils.features.aura.rotations.constructor.LinearConstructor;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TurnsConfig {
    public static TurnsConfig DEFAULT = new TurnsConfig(new LinearConstructor(), true, true);
    public static boolean moveCorrection, freeCorrection;
    RotateConstructor angleSmooth;
    int resetThreshold = 1;
    boolean changeLook;

    public TurnsConfig(boolean moveCorrection, boolean freeCorrection) {
        this(new LinearConstructor(), moveCorrection, freeCorrection);
    }

    public TurnsConfig(boolean moveCorrection) {
        this(new LinearConstructor(), moveCorrection, true);
    }

    public TurnsConfig(RotateConstructor angleSmooth, boolean moveCorrection, boolean freeCorrection) {
        this(angleSmooth, moveCorrection, freeCorrection, false);
    }

    public TurnsConfig(RotateConstructor angleSmooth, boolean moveCorrection, boolean freeCorrection, boolean changeLook) {
        this.angleSmooth = angleSmooth;
        this.moveCorrection = moveCorrection;
        this.freeCorrection = freeCorrection;
        this.changeLook = changeLook;
    }

    public TurnsConstructor createRotationPlan(Turns angle, Vec3d vec, Entity entity, int reset) {
        return new TurnsConstructor(angle, vec, entity, angleSmooth, reset, resetThreshold, moveCorrection, freeCorrection, changeLook);
    }

    public TurnsConstructor createRotationPlan(Turns angle, Vec3d vec, Entity entity, boolean moveCorrection, boolean freeCorrection) {
        return new TurnsConstructor(angle, vec, entity, angleSmooth, 1, resetThreshold, moveCorrection, freeCorrection, changeLook);
    }
}
