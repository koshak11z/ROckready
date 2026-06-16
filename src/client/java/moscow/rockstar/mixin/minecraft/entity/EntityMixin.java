/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.math.Box
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.combat.BackTrack;
import moscow.rockstar.systems.modules.modules.combat.Hitboxes;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.mixins.BacktrackableEntity;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationTask;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public class EntityMixin
implements IMinecraft,
BacktrackableEntity {
    @Shadow
    private Box field_6005;
    @Unique
    private final List<BackTrack.Position> backTracks = new ArrayList<BackTrack.Position>();

    @ModifyExpressionValue(method={"move"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;isControlledByPlayer()Z")})
    public boolean fixFalldistanceValue(boolean original) {
        if ((Entity)(Object)this == EntityMixin.mc.player) {
            return false;
        }
        return original;
    }

    @Inject(method={"getBoundingBox"}, at={@At(value="HEAD")}, cancellable=true)
    public final void getBoundingBox(CallbackInfoReturnable<Box> cir) {
        Hitboxes hitbox = Rockstar.getInstance().getModuleManager().getModule(Hitboxes.class);
        Entity entity = (Entity)(Object)this;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (hitbox.isEnabled() && hitbox.shouldModifyHitbox(livingEntity) && entity.getId() != EntityMixin.mc.player.getId()) {
                cir.setReturnValue(new Box(this.field_6005.minX - (double)hitbox.getScale().getCurrentValue(), this.field_6005.minY, this.field_6005.minZ - (double)hitbox.getScale().getCurrentValue(), this.field_6005.maxX + (double)hitbox.getScale().getCurrentValue(), this.field_6005.maxY, this.field_6005.maxZ + (double)hitbox.getScale().getCurrentValue()));
            }
        }
    }

    @Redirect(method={"updateVelocity"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;getYaw()F"))
    public float movementCorrection(Entity instance) {
        RotationHandler rotationHandler = Rockstar.INSTANCE.getRotationHandler();
        RotationTask currentTask = rotationHandler.getCurrentTask();
        if (currentTask != null && currentTask.getMoveCorrection() != MoveCorrection.NONE && instance instanceof ClientPlayerEntity) {
            return rotationHandler.getCurrentRotation().getYaw();
        }
        return instance.getYaw();
    }

    @Override
    public List<BackTrack.Position> rockstar2_0$getBackTracks() {
        return this.backTracks;
    }
}


