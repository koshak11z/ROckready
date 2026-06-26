/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.option.SimpleOption
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.option;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.FullBright;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SimpleOption.class})
public class SimpleOptionMixin<T> {
    @Shadow
    @Final
    Text field_38280;
    @Shadow
    T field_37868;

    @Inject(method={"getValue"}, at={@At(value="HEAD")}, cancellable=true)
    public void getGammaValue(CallbackInfoReturnable<Double> cir) {
        if (Rockstar.getInstance().getModuleManager() == null) {
            return;
        }
        FullBright fullBright = Rockstar.getInstance().getModuleManager().getModule(FullBright.class);
        if (fullBright != null && fullBright.isEnabled() && this.field_38280.equals((Object)Text.translatable((String)"options.gamma"))) {
            cir.setReturnValue(fullBright.getGamma());
        }
    }

    @Inject(method={"setValue"}, at={@At(value="HEAD")}, cancellable=true)
    public void setGammaValue(T value, CallbackInfo ci) {
        if (this.field_38280.equals((Object)Text.translatable((String)"options.gamma"))) {
            this.field_37868 = value;
            ci.cancel();
        }
    }
}


