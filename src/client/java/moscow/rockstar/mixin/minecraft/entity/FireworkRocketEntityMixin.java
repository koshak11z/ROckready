/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.projectile.FireworkRocketEntity
 *  net.minecraft.util.math.Vec3d
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.FireworkEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={FireworkRocketEntity.class})
public abstract class FireworkRocketEntityMixin
implements IMinecraft {
    @Redirect(method={"tick"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void redirectSetVelocity(LivingEntity shooter, Vec3d velocity) {
        FireworkRocketEntity rocketEntity = (FireworkRocketEntity)(Object)this;
        FireworkEvent event = new FireworkEvent(shooter, velocity, rocketEntity);
        Rockstar.getInstance().getEventManager().triggerEvent(event);
        shooter.setVelocity(event.getVelocity());
    }

    @Redirect(method={"tick"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d redirectGetRotationVector(LivingEntity instance) {
        RotationHandler rotationHandler;
        if (instance == FireworkRocketEntityMixin.mc.player && (rotationHandler = Rockstar.getInstance().getRotationHandler()) != null && rotationHandler.getState() != RotationState.IDLE) {
            Rotation currentRotation = rotationHandler.getCurrentRotation();
            return Vec3d.fromPolar((float)currentRotation.getPitch(), (float)currentRotation.getYaw());
        }
        return instance.getRotationVector();
    }
}
