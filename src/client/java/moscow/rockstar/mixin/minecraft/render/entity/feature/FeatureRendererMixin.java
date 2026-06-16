/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.entity.feature.FeatureRenderer
 *  net.minecraft.client.render.entity.model.EntityModel
 *  net.minecraft.client.render.entity.state.EntityRenderState
 *  net.minecraft.client.render.entity.state.LivingEntityRenderState
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.ArmorStandEntity
 *  net.minecraft.util.Identifier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package moscow.rockstar.mixin.minecraft.render.entity.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.AntiInvisible;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.mixins.EntityRenderStateAddition;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={FeatureRenderer.class})
public abstract class FeatureRendererMixin {
    @Unique
    private static final AntiInvisible ANTI_INVISIBLE_MODULE = Rockstar.getInstance().getModuleManager().getModule(AntiInvisible.class);

    @WrapOperation(method={"renderModel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V")})
    private static void changeModelColor(EntityModel<?> instance, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, int color, Operation<Void> original, @Local(argsOnly=true) LivingEntityRenderState state) {
        if (ANTI_INVISIBLE_MODULE.isEnabled() && ANTI_INVISIBLE_MODULE.shouldModifyOpacity((EntityRenderState)state)) {
            Entity entity = ((EntityRenderStateAddition)state).rockstar$getEntity();
            color = entity instanceof ArmorStandEntity ? Colors.WHITE.withAlpha(0.0f).getRGB() : Colors.WHITE.withAlpha(ANTI_INVISIBLE_MODULE.getOpacity().getCurrentValue() / 100.0f * 255.0f).getRGB();
        }
        original.call(new Object[]{instance, matrixStack, vertexConsumer, light, overlay, color});
    }

    @WrapOperation(method={"renderModel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/RenderLayer;getEntityCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;")})
    private static RenderLayer changeModelRenderLayer(Identifier texture, Operation<RenderLayer> original, @Local(argsOnly=true) LivingEntityRenderState state) {
        if (ANTI_INVISIBLE_MODULE.isEnabled() && ANTI_INVISIBLE_MODULE.shouldModifyOpacity((EntityRenderState)state)) {
            return RenderLayer.getItemEntityTranslucentCull((Identifier)texture);
        }
        return (RenderLayer)original.call(new Object[]{texture});
    }
}

