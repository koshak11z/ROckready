package im.zov4ik.utils.features.aura.rotation;

import im.zov4ik.utils.features.aura.rotations.constructor.RotateConstructor;
import im.zov4ik.utils.features.aura.warp.TurnsConfig;

public class RotationConfig extends TurnsConfig {
    public RotationConfig(RotateConstructor angleSmooth, boolean moveCorrection, boolean freeCorrection) {
        super(angleSmooth, moveCorrection, freeCorrection);
    }

    public RotationConfig(RotateConstructor angleSmooth, boolean moveCorrection, boolean freeCorrection, boolean changeLook) {
        super(angleSmooth, moveCorrection, freeCorrection, changeLook);
    }
}
