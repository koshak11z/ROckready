/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.SplashOverlay
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.resource.ResourceReload
 *  net.minecraft.util.Util
 *  net.minecraft.util.math.MathHelper
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import java.util.Optional;
import java.util.function.Consumer;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.ui.components.gif.Gif;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SplashOverlay.class})
public class SplashOverlayMixin
implements IScaledResolution,
IMinecraft {
    @Unique
    private Gif daunGif;
    @Unique
    private Animation fadeOutAnimation;
    @Shadow
    private long field_17771 = -1L;
    @Final
    @Shadow
    private Consumer<Optional<Throwable>> field_18218;
    @Shadow
    @Final
    private ResourceReload field_17767;
    @Shadow
    @Final
    private boolean field_18219;
    @Shadow
    private long field_18220;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void init(MinecraftClient client, ResourceReload monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading, CallbackInfo ci) {
        this.daunGif = new Gif(Rockstar.id("gifs/loading.gif"), 100.0f, 100.0f, 100.0f, 100.0f);
        this.fadeOutAnimation = new Animation(3000L, 1.0f, Easing.CUBIC_IN_OUT);
    }

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void replaceRendering(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        float g;
        if (Rockstar.getInstance().isPanic()) {
            return;
        }
        ci.cancel();
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        UIContext uiContext = UIContext.of(context, 0, 0, delta);
        long currentTime = Util.getMeasuringTimeMs();
        if (this.field_18219 && this.field_18220 == -1L) {
            this.field_18220 = currentTime;
        }
        float f = this.field_17771 > -1L ? (float)(currentTime - this.field_17771) / 1000.0f : -1.0f;
        float f2 = g = this.field_18220 > -1L ? (float)(currentTime - this.field_18220) / 500.0f : -1.0f;
        if (f >= 1.0f) {
            if (SplashOverlayMixin.mc.currentScreen != null) {
                SplashOverlayMixin.mc.currentScreen.render(context, 0, 0, delta);
            }
            int k = MathHelper.ceil((float)((1.0f - MathHelper.clamp((float)(f - 1.0f), (float)0.0f, (float)1.0f)) * 255.0f));
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, width, height, Colors.BLACK.withAlpha(k).getRGB());
        } else if (this.field_18219 && SplashOverlayMixin.mc.currentScreen != null && g < 1.0f) {
            SplashOverlayMixin.mc.currentScreen.render(context, mouseX, mouseY, delta);
            int k = MathHelper.ceil((double)(MathHelper.clamp((double)g, (double)0.15, (double)1.0) * 255.0));
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, width, height, Colors.BLACK.withAlpha(k).getRGB());
        }
        if (f < 1.0f) {
            this.daunGif.set(0.0f, sr.getScaledHeight() / 2.0f - sr.getScaledWidth() / 1920.0f * 1080.0f / 2.0f, sr.getScaledWidth(), sr.getScaledWidth() / 1920.0f * 1080.0f);
            this.daunGif.setAlpha(1.0f);
            this.daunGif.render(uiContext);
        }
        if (f >= 2.0f) {
            mc.setOverlay(null);
            this.daunGif.dispose();
        }
        if (this.field_17771 == -1L && this.field_17767.isComplete() && (!this.field_18219 || g >= 2.0f)) {
            try {
                this.field_17767.throwException();
                this.field_18218.accept(Optional.empty());
            }
            catch (Throwable throwable) {
                this.field_18218.accept(Optional.of(throwable));
            }
            this.field_17771 = currentTime;
            if (SplashOverlayMixin.mc.currentScreen != null) {
                SplashOverlayMixin.mc.currentScreen.init(mc, context.getScaledWindowWidth(), context.getScaledWindowHeight());
            }
        }
    }
}

