/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.entity.LivingEntityRenderer
 *  net.minecraft.client.render.entity.model.BipedEntityModel
 *  net.minecraft.client.render.entity.model.EntityModel
 *  net.minecraft.client.render.entity.state.EntityRenderState
 *  net.minecraft.client.render.entity.state.LivingEntityRenderState
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.decoration.ArmorStandEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.Identifier
 *  org.joml.Vector3f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package moscow.rockstar.mixin.minecraft.client.render.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.Rockstar;
import moscow.rockstar.mixin.accessors.BipedEntityModelAccessor;
import moscow.rockstar.systems.modules.modules.visuals.AntiInvisible;
import moscow.rockstar.systems.modules.modules.visuals.FriendMarkers;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.countermine.AntiAim;
import moscow.rockstar.utility.mixins.EntityRenderStateAddition;
import moscow.rockstar.utility.rotations.RotationHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Unique
    private static final AntiInvisible ANTI_INVISIBLE_MODULE = Rockstar.getInstance().getModuleManager().getModule(AntiInvisible.class);

    @Shadow
    public abstract Identifier method_3885(S var1);

    @ModifyExpressionValue(method={"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/LivingEntityRenderer;clampBodyYaw(Lnet/minecraft/entity/LivingEntity;FF)F")})
    public float changeYaw(float oldValue, LivingEntity entity) {
        if (!(entity instanceof ClientPlayerEntity) || AntiAim.FORCE) {
            return oldValue;
        }
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        float yaw = rotationHandler.isIdling() ? oldValue : rotationHandler.getRenderRotation().getYaw();
        rotationHandler.getServerRotation().setYaw(yaw);
        return yaw;
    }

    @ModifyExpressionValue(method={"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F")})
    public float changeHeadYaw(float oldValue, LivingEntity entity) {
        if (!(entity instanceof ClientPlayerEntity) || AntiAim.FORCE) {
            return oldValue;
        }
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        float yaw = rotationHandler.isIdling() ? oldValue : rotationHandler.getRenderRotation().getYaw();
        rotationHandler.getServerRotation().setYaw(yaw);
        return yaw;
    }

    @ModifyExpressionValue(method={"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F")})
    public float changePitch(float oldValue, LivingEntity entity) {
        if (!(entity instanceof ClientPlayerEntity) || AntiAim.FORCE) {
            return oldValue;
        }
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        float pitch = rotationHandler.isIdling() ? oldValue : rotationHandler.getRenderRotation().getPitch();
        rotationHandler.getServerRotation().setYaw(pitch);
        return pitch;
    }

    @WrapOperation(method={"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V")})
    private void changeModelColor(EntityModel<?> instance, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, int color, Operation<Void> original, @Local(argsOnly=true) S livingEntityRenderState) {
        Entity entity;
        if (ANTI_INVISIBLE_MODULE.isEnabled() && ANTI_INVISIBLE_MODULE.shouldModifyOpacity((EntityRenderState)livingEntityRenderState)) {
            entity = ((EntityRenderStateAddition)livingEntityRenderState).rockstar$getEntity();
            color = entity instanceof ArmorStandEntity ? Colors.WHITE.withAlpha(0.0f).getRGB() : Colors.WHITE.withAlpha(ANTI_INVISIBLE_MODULE.getOpacity().getCurrentValue() / 100.0f * 255.0f).getRGB();
        }
        entity = ((EntityRenderStateAddition)livingEntityRenderState).rockstar$getEntity();
        FriendMarkers markers = Rockstar.getInstance().getModuleManager().getModule(FriendMarkers.class);
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (instance instanceof BipedEntityModel) {
                BipedEntityModel model = (BipedEntityModel)instance;
                if (markers.isEnabled() && markers.getHeads().isSelected() && Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())) {
                    BipedEntityModelAccessor accessor = (BipedEntityModelAccessor)model;
                    float scale = 1.09f;
                    accessor.rockstar$getHead().scale(new Vector3f(scale, scale, scale));
                    original.call(new Object[]{instance, matrixStack, vertexConsumer, light, overlay, color});
                }
            }
        }
        original.call(new Object[]{instance, matrixStack, vertexConsumer, light, overlay, color});
    }

    @ModifyReturnValue(method={"getRenderLayer"}, at={@At(value="RETURN")})
    private RenderLayer changeRenderLayer(RenderLayer original, S state, boolean showBody, boolean translucent, boolean showOutline) {
        if (ANTI_INVISIBLE_MODULE.isEnabled() && !showBody && !translucent && !showOutline) {
            ((LivingEntityRenderState)state).invisible = false;
            return RenderLayer.getItemEntityTranslucentCull((Identifier)this.method_3885(state));
        }
        return original;
    }
}

