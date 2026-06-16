/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.render.BackgroundRenderer
 *  net.minecraft.client.render.BackgroundRenderer$FogType
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.Fog
 *  net.minecraft.client.render.FogShape
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.MathHelper
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.CustomFog;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import moscow.rockstar.utility.colors.ColorRGBA;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BackgroundRenderer.class})
public class BackgroundRendererMixin {
    @Inject(method={"getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<Object> info) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && removals.getBlindness().isSelected()) {
            info.setReturnValue(null);
        }
    }

    @ModifyReturnValue(method={"applyFog"}, at={@At(value="RETURN")})
    private static Fog modifyFogProperties(Fog original, @Local(argsOnly=true) Camera camera, @Local(argsOnly=true) BackgroundRenderer.FogType fogType, @Local(argsOnly=true, ordinal=0) float viewDistance) {
        CustomFog customFogModule = Rockstar.getInstance().getModuleManager().getModule(CustomFog.class);
        if (customFogModule.shouldModifyFog(camera) && fogType == BackgroundRenderer.FogType.FOG_TERRAIN) {
            float start = MathHelper.clamp((float)customFogModule.getDistance().getFirstValue(), (float)-8.0f, (float)viewDistance);
            float end = MathHelper.clamp((float)customFogModule.getDistance().getSecondValue(), (float)0.0f, (float)viewDistance);
            ColorRGBA color = customFogModule.getFogColor().getColor();
            FogShape shape = FogShape.SPHERE;
            float r = color.getRed() / 255.0f;
            float g = color.getGreen() / 255.0f;
            float b = color.getBlue() / 255.0f;
            float a = color.getAlpha() / 255.0f;
            return new Fog(start, end, shape, r, g, b, a);
        }
        return original;
    }
}

