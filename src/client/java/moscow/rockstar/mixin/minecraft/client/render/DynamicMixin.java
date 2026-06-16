/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.RenderTickCounter$Dynamic
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.render;

import moscow.rockstar.utility.game.EntityUtility;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={RenderTickCounter.Dynamic.class})
public class DynamicMixin {
    @Shadow
    private float field_51958;
    @Shadow
    private float field_51959;
    @Shadow
    private long field_51962;
    @Final
    @Shadow
    private float field_51964;

    @Inject(at={@At(value="FIELD", target="Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J", opcode=181, ordinal=0)}, method={"beginRenderTick(J)I"}, cancellable=true)
    public void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        if (EntityUtility.getTimer() == 1.0f) {
            return;
        }
        this.field_51958 = (float)(timeMillis - this.field_51962) / this.field_51964 * EntityUtility.getTimer();
        this.field_51962 = timeMillis;
        this.field_51959 += this.field_51958;
        int i = (int)this.field_51959;
        this.field_51959 -= (float)i;
        cir.setReturnValue(i);
    }
}


