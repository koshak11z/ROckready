package im.zov4ik.utils.features.aura.warp;

import im.zov4ik.features.impl.combat.Aura;
import im.zov4ik.features.impl.movement.Strafe;
import im.zov4ik.features.impl.movement.TargetStrafe;
import im.zov4ik.features.impl.player.AutoPilot;
import im.zov4ik.utils.features.aura.utils.MathAngle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.utils.features.aura.rotations.constructor.RotateConstructor;

@Setter
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TurnsConstructor implements QuickImports {
    Turns angle;
    Vec3d vec3d;
    Entity entity;
    RotateConstructor angleSmooth;
    int ticksUntilReset;
    float resetThreshold;
    public boolean moveCorrection;
    @Getter(AccessLevel.PUBLIC)
    public boolean freeCorrection;
    public boolean changeLook;

    public boolean isChangeLook() {
        return changeLook
                || (AutoPilot.getInstance().isState() && AutoPilot.getInstance().target != null)
                || (Aura.getInstance().isState()
                && Aura.getInstance().getTarget() != null
                && Aura.getInstance().getCorrectionType().isSelected("Change Look"));
    }


    public Turns nextRotation(Turns fromAngle, boolean isResetting) {
        if (isResetting) {
            return angleSmooth.limitAngleChange(fromAngle, MathAngle.fromVec2f(mc.player.getRotationClient()));
        }
        return angleSmooth.limitAngleChange(fromAngle, angle, vec3d, entity);
    }
}
