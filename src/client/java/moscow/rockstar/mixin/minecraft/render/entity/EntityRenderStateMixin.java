/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.entity.state.EntityRenderState
 *  net.minecraft.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package moscow.rockstar.mixin.minecraft.render.entity;

import moscow.rockstar.utility.mixins.EntityRenderStateAddition;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={EntityRenderState.class})
public abstract class EntityRenderStateMixin
implements EntityRenderStateAddition {
    @Unique
    private Entity rockstar$entity;

    @Override
    @Unique
    public void rockstar$setEntity(Entity entity) {
        this.rockstar$entity = entity;
    }

    @Override
    @Unique
    public Entity rockstar$getEntity() {
        return this.rockstar$entity;
    }
}

