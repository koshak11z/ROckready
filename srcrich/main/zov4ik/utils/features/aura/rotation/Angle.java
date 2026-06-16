package im.zov4ik.utils.features.aura.rotation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.Vec3d;
import im.zov4ik.utils.features.aura.warp.Turns;

public class Angle extends Turns {
    public Angle(float yaw, float pitch) {
        super(yaw, pitch);
    }

    @Getter
    @RequiredArgsConstructor
    public static class VecRotation {
        private final Angle angle;
        private final Vec3d vec;
    }
}
