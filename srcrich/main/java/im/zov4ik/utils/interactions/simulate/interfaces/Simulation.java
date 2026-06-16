package im.zov4ik.utils.interactions.simulate.interfaces;

import net.minecraft.util.math.Vec3d;

public interface Simulation {
    Vec3d pos();

    void tick();
}