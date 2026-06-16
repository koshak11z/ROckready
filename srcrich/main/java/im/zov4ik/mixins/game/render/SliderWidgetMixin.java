package im.zov4ik.mixins.game.render;

import im.zov4ik.features.impl.misc.SelfDestruct;
import im.zov4ik.features.impl.render.BetterMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin extends ClickableWidget {

    @Shadow protected double value;

    public SliderWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void onRenderWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;
        if (BetterMinecraft.getInstance().isState() && BetterMinecraft.getInstance().getBetterButton().isValue()) {

            ci.cancel();
            renderSlider(
                    context,
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight(),
                    this.value,
                    this.active,
                    getMessage() != null ? getMessage().getString() : ""
            );
        }
    }

    private void renderSlider(DrawContext context, int x, int y, int width, int height, double value, boolean active, String text) {
        context.fill(x, y, x + width, y + height, new Color(18, 19, 20, 175).getRGB());
        int knobWidth = 7;
        int knobX = x + (int) Math.round(value * Math.max(0, width - knobWidth));
        context.fill(knobX, y + 1, knobX + knobWidth, y + height - 1, new Color(124, 124, 124, active ? 180 : 90).getRGB());
        if (text != null && !text.isEmpty()) {
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x + width / 2, y + height / 2 - 3, Color.WHITE.getRGB());
        }
    }
}
