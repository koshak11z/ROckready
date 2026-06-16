/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package moscow.rockstar.utility.game.prediction;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class EntityPredictor
implements IMinecraft {
    public static float predictDamage(Entity crystal, PlayerEntity target) {
        Vec3d crystalPos = new Vec3d(crystal.getX(), crystal.getY(), crystal.getZ());
        Vec3d targetPos = target.getBoundingBox().getCenter();
        double dist = targetPos.distanceTo(crystalPos);
        if (dist < 0.5) {
            dist = 0.0;
        }
        double scaledImpact = 1.0 - MathHelper.clamp((double)(dist / 6.0), (double)0.0, (double)1.0);
        boolean blocked = target.getWorld().raycast(new RaycastContext(crystalPos, targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)target)).getType() != HitResult.Type.MISS;
        float exposure = blocked ? 0.7f : 1.0f;
        return (float)((double)exposure * (scaledImpact * 24.0 + 1.0));
    }

    @Generated
    private EntityPredictor() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

