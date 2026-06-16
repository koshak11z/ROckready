/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.entity.Entity$RemovalReason
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.damage.DamageSource
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.EntityDeathEvent;
import moscow.rockstar.systems.event.impl.game.EntityJumpEvent;
import moscow.rockstar.systems.modules.modules.player.NoDelay;
import moscow.rockstar.systems.modules.modules.player.NoPush;
import moscow.rockstar.systems.modules.modules.visuals.SwingAnimation;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationTask;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin {
    @Shadow
    private int field_6228;

    @Shadow
    public abstract void method_5650(Entity.RemovalReason var1);

    @Shadow
    public abstract ItemStack method_6047();

    @ModifyReturnValue(method={"getHandSwingDuration"}, at={@At(value="RETURN")})
    public int replaceSwingSpeed(int original) {
        SwingAnimation swingAnimationModule = Rockstar.getInstance().getModuleManager().getModule(SwingAnimation.class);
        if (!swingAnimationModule.isEnabled() || !swingAnimationModule.shouldApplyAnimation(this.method_6047())) {
            return original;
        }
        return (int)((float)original * Rockstar.getInstance().getSwingManager().getSpeed().getCurrentValue());
    }

    @Inject(method={"jump"}, at={@At(value="HEAD")}, cancellable=true)
    public void triggerJumpEvent(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        EntityJumpEvent event = new EntityJumpEvent(livingEntity);
        Rockstar.getInstance().getEventManager().triggerEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method={"jump"}, at={@At(value="NEW", target="(DDD)Lnet/minecraft/util/math/Vec3d;")})
    public Vec3d movementCorrection(Vec3d original) {
        RotationHandler rotationHandler = Rockstar.INSTANCE.getRotationHandler();
        RotationTask currentTask = rotationHandler.getCurrentTask();
        if ((LivingEntity)(Object)this != MinecraftClient.getInstance().player) {
            return original;
        }
        if (currentTask != null && currentTask.getMoveCorrection() != MoveCorrection.NONE) {
            float yaw = rotationHandler.getCurrentRotation().getYaw() * ((float)Math.PI / 180);
            return new Vec3d((double)(-MathHelper.sin((float)yaw) * 0.2f), 0.0, (double)(MathHelper.cos((float)yaw) * 0.2f));
        }
        return original;
    }

    @Inject(method={"tickMovement"}, at={@At(value="HEAD")})
    public void removeJumpDelay(CallbackInfo ci) {
        NoDelay noDelay = Rockstar.getInstance().getModuleManager().getModule(NoDelay.class);
        if (noDelay.isEnabled() && noDelay.getJump().isEnabled()) {
            this.field_6228 = 0;
        }
    }

    @Inject(method={"isPushable"}, at={@At(value="HEAD")}, cancellable=true)
    private void removePushFromEntity(CallbackInfoReturnable<Boolean> cir) {
        NoPush noPush = Rockstar.getInstance().getModuleManager().getModule(NoPush.class);
        LivingEntity entity = (LivingEntity)(Object)this;
        if (entity instanceof ClientPlayerEntity && noPush.isEnabled() && noPush.getEntities().isSelected()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"onDeath"}, at={@At(value="TAIL")})
    public void triggerEntityDeathEvent(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        Rockstar.getInstance().getEventManager().triggerEvent(new EntityDeathEvent(entity, damageSource));
    }

    @Redirect(method={"calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getPitch()F"))
    private float redirectGetPitch(LivingEntity instance) {
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        return rotationHandler.isIdling() ? instance.getPitch() : rotationHandler.getCurrentRotation().getPitch();
    }

    @Redirect(method={"calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d redirectGetRotationVector(LivingEntity instance) {
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        return rotationHandler.isIdling() ? instance.getRotationVector() : rotationHandler.getCurrentRotation().getRotationVector();
    }
}


