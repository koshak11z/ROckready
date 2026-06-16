/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.Frustum
 *  net.minecraft.client.render.entity.EntityRenderer
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.DisplayEntity$ItemDisplayEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.render.entity;

import moscow.rockstar.systems.modules.modules.other.CounterMine;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityRenderer.class})
public class EntityRendererMixin<T extends Entity> {
    @Inject(method={"shouldRender"}, at={@At(value="HEAD")}, cancellable=true)
    private void onShouldRender(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        DisplayEntity.ItemDisplayEntity itemDisplay;
        if (entity instanceof DisplayEntity.ItemDisplayEntity && CounterMine.shouldHideEntity(itemDisplay = (DisplayEntity.ItemDisplayEntity)entity)) {
            cir.setReturnValue(false);
        }
    }
}


