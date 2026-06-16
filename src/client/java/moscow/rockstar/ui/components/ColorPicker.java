/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gui.screen.Screen
 */
package moscow.rockstar.ui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.objects.gradient.Gradient;
import moscow.rockstar.framework.objects.gradient.impl.HorizontalGradient;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.animation.types.ColorAnimation;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.interfaces.IWindow;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.obj.Rect;
import net.minecraft.client.gui.screen.Screen;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class ColorPicker
extends CustomComponent
implements IScaledResolution,
IWindow {
    private final Animation animation = new Animation(300L, 0.0f, Easing.BAKEK_SIZE);
    private final Animation blurAnim = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    protected final Animation dragAnim = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation pickAnim = new Animation(300L, 0.0f, Easing.BAKEK_SIZE);
    private final ColorAnimation huePreviewColorAnimation = new ColorAnimation(300L);
    private final ColorAnimation activePreviewColorAnimation = new ColorAnimation(200L);
    private final String title;
    private boolean showing;
    private float offsetFactor;
    private boolean drag;
    private boolean pick;
    private float dragX;
    private float dragY;
    private boolean dragHue;
    private boolean dragBS;
    private boolean dragAlpha;
    private final boolean enableAlpha;
    private final Animation hueAnim = new Animation(500L, Easing.BAKEK_PAGES);
    private final Animation brightnessAnim = new Animation(500L, Easing.BAKEK_PAGES);
    private final Animation saturationAnim = new Animation(500L, Easing.BAKEK_PAGES);
    private final Animation alphaAnim = new Animation(500L, Easing.BAKEK_PAGES);
    private float hue;
    private float brightness;
    private float saturation;
    private float alpha;
    public static final List<Preset> COLOR_PRESETS = new ArrayList<Preset>(List.of(new Preset(new ColorRGBA(0.0f, 122.0f, 255.0f)), new Preset(new ColorRGBA(52.0f, 199.0f, 89.0f)), new Preset(new ColorRGBA(255.0f, 204.0f, 0.0f)), new Preset(new ColorRGBA(255.0f, 59.0f, 48.0f)), new Preset(new ColorRGBA(151.0f, 71.0f, 255.0f))));

    public ColorPicker(float x, float y, float offsetFactor, boolean enableAlpha, ColorRGBA color, String title) {
        super(x, y, 143.0f, enableAlpha ? 160.0f : 136.0f);
        this.offsetFactor = offsetFactor;
        this.enableAlpha = enableAlpha;
        this.showing = true;
        this.activePreviewColorAnimation.setColor(color);
        this.title = title;
        this.update(color);
    }

    public static void setColorPresets(List<Preset> newPresets) {
        COLOR_PRESETS.clear();
        COLOR_PRESETS.addAll(newPresets);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (this.dragHue) {
            this.hue = GuiUtility.getSliderValue(0.0f, 1.0f, this.y + 22.0f, 66.0f, context.getMouseY());
        }
        if (this.dragBS) {
            this.brightness = 1.0f - GuiUtility.getSliderValue(0.0f, 1.0f, this.x + 6.0f, 114.0f, context.getMouseX());
            this.saturation = 1.0f - GuiUtility.getSliderValue(0.0f, 1.0f, this.y + 20.0f, 70.0f, context.getMouseY());
        }
        if (this.dragAlpha) {
            this.alpha = GuiUtility.getSliderValue(0.0f, 1.0f, this.x + 7.0f, 88.0f, context.getMouseX());
        }
        if (this.drag) {
            this.x = (float)context.getMouseX() - this.dragX;
            this.y = (float)context.getMouseY() - this.dragY;
        }
        COLOR_PRESETS.removeIf(preset -> preset.animation.getValue() == 0.0f && !preset.showing);
        this.pickAnim.setEasing(this.pick ? Easing.BAKEK : Easing.BAKEK_BACK);
        this.pickAnim.update(this.pick);
        this.animation.setEasing(this.showing ? Easing.BAKEK : Easing.BAKEK_BACK);
        this.animation.update(this.showing);
        this.blurAnim.update(this.animation.getValue() >= 0.6f);
        this.dragAnim.update(this.drag);
        this.hueAnim.update(this.hue);
        this.brightnessAnim.update(1.0f - this.brightness);
        this.saturationAnim.update(1.0f - this.saturation);
        this.alphaAnim.update(this.alpha);
        this.huePreviewColorAnimation.update(ColorRGBA.fromHSB(this.hue, 1.0f, 1.0f));
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        ColorRGBA bgColor = Colors.getBackgroundColor().mulAlpha(dark ? 0.9f - 0.6f * Interface.glass() : 0.7f);
        ColorRGBA withoutAlpha = ColorRGBA.fromHSB(this.hue, this.brightness, this.saturation);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)Math.min(1.0f, this.animation.getValue()));
        RenderUtility.scale(context.getMatrices(), this.x + this.width / this.offsetFactor, this.y + this.height / this.offsetFactor, 0.5f + this.animation.getValue() * 0.5f);
        ScissorUtility.push(context.getMatrices(), this.x + 1.0f, this.y + 1.0f, this.width - 2.0f, this.height - 2.0f);
        context.drawShadow(this.x - 5.0f, this.y - 5.0f, this.width + 10.0f, this.height + 10.0f, 15.0f, BorderRadius.all(6.0f), ColorRGBA.BLACK.withAlpha(255.0f * (0.1f + 0.15f * this.dragAnim.getValue())));
        ScissorUtility.pop();
        if (Interface.showMinimalizm() && Interface.blurPopupsEnabled()) {
            context.drawBlurredRect(this.x, this.y, this.width, this.height, 45.0f, 7.0f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue() * Interface.minimalizm()));
        }
        if (Interface.showGlass()) {
            context.drawLiquidGlass(this.x, this.y, this.width, this.height, 7.0f, 0.05f - 0.03f * this.dragAnim.getValue(), BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue() * Interface.glass()));
        }
        context.drawSquircle(this.x, this.y, this.width, this.height, 7.0f, BorderRadius.all(6.0f), bgColor);
        ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
        context.drawCenteredText(Fonts.MEDIUM.getFont(7.0f), this.title, this.x + this.width / 2.0f, this.y + 7.0f, Colors.getTextColor());
        context.drawTexture(Rockstar.id("icons/colorpicker/pipette.png"), this.x + 7.0f, this.y + 6.0f, 8.0f, 8.0f);
        if (GuiUtility.isHovered((double)(this.x + 7.0f), (double)(this.y + 6.0f), 8.0, 8.0, context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
        context.drawRoundedRect(this.x + this.width - 15.0f, this.y + 5.0f, 10.0f, 10.0f, BorderRadius.all(5.0f), Colors.getAdditionalColor());
        context.drawTexture(Rockstar.id("icons/colorpicker/xmark.png"), this.x + this.width - 15.0f, this.y + 5.0f, 10.0f, 10.0f);
        if (GuiUtility.isHovered((double)(this.x + this.width - 15.0f), (double)(this.y + 5.0f), 10.0, 10.0, context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
        context.drawRoundedTexture(Rockstar.id("textures/hue.png"), this.x + this.width - 18.0f, this.y + 20.0f, 12.0f, 70.0f, BorderRadius.all(4.0f));
        context.drawRoundedRect(this.x + this.width - 16.0f, this.y + 22.0f + 64.0f * this.hueAnim.getValue(), 8.0f, 2.0f, BorderRadius.all(0.2f), Colors.WHITE);
        if (GuiUtility.isHovered(this.x + this.width - 18.0f, this.y + 20.0f, 12.0, 70.0, context) || this.dragHue) {
            CursorUtility.set(CursorType.ARROW_VERTICAL);
        }
        context.drawRoundedRect(this.x + 6.0f, this.y + 20.0f, 114.0f, 70.0f, BorderRadius.all(4.0f), Gradient.of(this.huePreviewColorAnimation.getColor(), Colors.BLACK, Colors.WHITE, Colors.BLACK));
        context.drawRoundedRect(this.x + 6.0f + 114.0f * this.brightnessAnim.getValue() - 3.5f, this.y + 20.0f + 70.0f * this.saturationAnim.getValue() - 3.5f, 7.0f, 7.0f, BorderRadius.all(2.5f), Colors.WHITE);
        context.drawRoundedRect(this.x + 7.0f + 114.0f * this.brightnessAnim.getValue() - 3.5f, this.y + 21.0f + 70.0f * this.saturationAnim.getValue() - 3.5f, 5.0f, 5.0f, BorderRadius.all(1.5f), withoutAlpha);
        if (GuiUtility.isHovered(this.x + 6.0f, this.y + 20.0f, 114.0, 70.0, context) || this.dragBS) {
            CursorUtility.set(CursorType.CROSSHAIR);
        }
        if (this.enableAlpha) {
            context.drawText(Fonts.MEDIUM.getFont(5.0f), Localizator.translate("colorpicker.opacity").toUpperCase(), this.x + 6.0f, this.y + 95.0f, Colors.getTextColor().withAlpha(191.25f));
            context.drawRoundedTexture(Rockstar.id("textures/empty.png"), this.x + 6.0f, this.y + 102.0f, 100.0f, 12.0f, BorderRadius.all(5.0f));
            context.drawRoundedRect(this.x + 6.0f - 0.5f, this.y + 102.0f - 0.5f, 101.0f, 13.0f, BorderRadius.all(5.0f), new HorizontalGradient(withoutAlpha.withAlpha(0.0f), withoutAlpha));
            context.drawRoundedRect(this.x + this.width - 32.0f, this.y + 102.0f, 26.0f, 12.0f, BorderRadius.all(2.0f), Colors.getAdditionalColor().withAlpha(255.0f));
            context.drawCenteredText(Fonts.MEDIUM.getFont(6.0f), (int)(this.alpha * 100.0f) + "%", this.x + this.width - 32.0f + 13.0f, this.y + 106.0f, Colors.getTextColor());
            context.drawRoundedBorder(this.x + 7.0f + 88.0f * this.alphaAnim.getValue(), this.y + 103.0f, 10.0f, 10.0f, 0.5f, BorderRadius.all(4.0f), Colors.WHITE);
            context.drawRoundedRect(this.x + 8.0f + 88.0f * this.alphaAnim.getValue(), this.y + 104.0f, 8.0f, 8.0f, BorderRadius.all(3.0f), this.built());
            if (GuiUtility.isHovered(this.x + 6.0f, this.y + 102.0f, 100.0, 12.0, context) || this.dragAlpha) {
                CursorUtility.set(CursorType.ARROW_HORIZONTAL);
            }
        }
        context.drawRoundedRect(this.x + 6.0f, this.y + this.height - 36.0f, 29.0f, 29.0f, BorderRadius.all(5.0f), this.built());
        float xOffset = 0.0f;
        float yOffset = 0.0f;
        for (Preset preset2 : COLOR_PRESETS) {
            preset2.animation.update(preset2.showing);
            preset2.selected.update(preset2.color.getHue() == this.hue && preset2.color.getSaturation() == this.saturation && preset2.color.getBrightness() == this.brightness);
            if (preset2.selected.getValue() > 0.0f) {
                float anim = preset2.selected.getValue();
                context.drawRoundedRect(this.x + 45.0f + xOffset, this.y + this.height - 36.0f + yOffset, 11.0f, 11.0f, BorderRadius.all(4.5f), preset2.color.withAlpha(255.0f * preset2.animation.getValue()));
                context.drawRoundedBorder(this.x + 45.0f + xOffset - 1.0f + 2.0f * anim, this.y + this.height - 36.0f + yOffset - 1.0f + 2.0f * anim, 13.0f - 4.0f * anim, 13.0f - 4.0f * anim, 0.5f, BorderRadius.all(6.5f - 2.0f * anim), Colors.WHITE.withAlpha(255.0f * preset2.animation.getValue() * preset2.selected.getValue()));
            } else {
                context.drawRoundedRect(this.x + 45.0f + xOffset, this.y + this.height - 36.0f + yOffset, 11.0f, 11.0f, BorderRadius.all(4.5f), preset2.color.withAlpha(255.0f * preset2.animation.getValue()));
            }
            if (GuiUtility.isHovered(this.x + 45.0f + xOffset, this.y + this.height - 36.0f + yOffset, 11.0, 11.0, context)) {
                CursorUtility.set(CursorType.HAND);
            }
            if (!(45.0f + (xOffset += 20.0f * preset2.animation.getValue()) > this.width)) continue;
            xOffset = 0.0f;
            yOffset += 18.0f * preset2.animation.getValue();
        }
        if (COLOR_PRESETS.size() < 10) {
            context.drawRoundedRect(this.x + 45.0f + xOffset, this.y + this.height - 36.0f + yOffset, 11.0f, 11.0f, BorderRadius.all(4.5f), Colors.getAdditionalColor());
            context.drawTexture(Rockstar.id("icons/colorpicker/plus.png"), this.x + 45.0f + xOffset, this.y + this.height - 36.0f + yOffset, 11.0f, 11.0f);
            if (GuiUtility.isHovered(this.x + 45.0f + xOffset, this.y + this.height - 36.0f + yOffset, 11.0, 11.0, context)) {
                CursorUtility.set(CursorType.HAND);
            }
        }
        ScissorUtility.pop();
        RenderUtility.end(context.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        if (this.pickAnim.getValue() > 0.0f) {
            Rect pickRect = new Rect(context.getMouseX(), context.getMouseY() + 10, 45.0f + Fonts.REGULAR.getFont(6.0f).width(Localizator.translate("colorpicker.click_to_sample")), 30.0f);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)Math.min(1.0f, this.pickAnim.getValue()));
            RenderUtility.scale(context.getMatrices(), pickRect.getX() + pickRect.getWidth() / 2.0f, pickRect.getY() + pickRect.getHeight() / 2.0f, 0.5f + this.pickAnim.getValue() * 0.5f);
            if (Interface.blurPopupsEnabled()) {
                context.drawBlurredRect(pickRect.getX(), pickRect.getY(), pickRect.getWidth(), pickRect.getHeight(), 45.0f, 7.0f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.pickAnim.getValue()));
            }
            context.drawSquircle(pickRect.getX(), pickRect.getY(), pickRect.getWidth(), pickRect.getHeight(), 7.0f, BorderRadius.all(6.0f), Colors.getBackgroundColor().mulAlpha(dark ? 0.8f : 0.7f));
            ColorRGBA mouseColor = ColorRGBA.fromPixel((float)((double)context.getMouseX() * sr.getScaleFactor()), (float)((double)mw.getHeight() - (double)context.getMouseY() * sr.getScaleFactor()));
            context.drawRoundedRect(pickRect.getX() + 5.0f, pickRect.getY() + 5.0f, pickRect.getHeight() - 10.0f, pickRect.getHeight() - 10.0f, BorderRadius.all(5.0f), mouseColor);
            context.drawTexture(Rockstar.id("icons/colorpicker/click.png"), pickRect.getX() + pickRect.getHeight(), pickRect.getY() + 16.0f, 6.0f, 6.0f);
            context.drawText(Fonts.REGULAR.getFont(6.0f), String.format("RGB %s %s %s", (int)mouseColor.getRed(), (int)mouseColor.getGreen(), (int)mouseColor.getBlue()), pickRect.getX() + pickRect.getHeight(), pickRect.getY() + 8.0f, Colors.getTextColor());
            context.drawText(Fonts.REGULAR.getFont(6.0f), Localizator.translate("colorpicker.click_to_sample"), pickRect.getX() + pickRect.getHeight() + 8.0f, pickRect.getY() + 17.0f, Colors.getTextColor().withAlpha(200.0f));
            RenderUtility.end(context.getMatrices());
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    public ColorRGBA built() {
        this.activePreviewColorAnimation.update(ColorRGBA.fromHSB(this.hue, this.brightness, this.saturation).withAlpha(this.enableAlpha ? 255.0f * this.alpha : 255.0f));
        return this.activePreviewColorAnimation.getColor();
    }

    @Override
    @Compile
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.isCopy((int)keyCode)) {
            ColorPicker.mc.keyboard.setClipboard(this.built().toHex());
        } else if (Screen.isPaste((int)keyCode)) {
            String clipboard = ColorPicker.mc.keyboard.getClipboard();
            try {
                this.update(ColorRGBA.fromHex(clipboard));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        super.onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    @Compile
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        boolean canAppend = COLOR_PRESETS.size() < 10;
        float xOffset = 0.0f;
        float yOffset = 0.0f;
        for (Preset preset : COLOR_PRESETS) {
            if (GuiUtility.isHovered((double)(this.x + 45.0f + xOffset), (double)(this.y + this.height - 36.0f + yOffset), 11.0, 11.0, mouseX, mouseY)) {
                if (button.getButtonIndex() != 0) {
                    preset.showing = false;
                    Rockstar.getInstance().getFileManager().writeFile("client");
                } else {
                    this.update(preset.color);
                }
                return;
            }
            if (preset.color.getHue() == this.hue && preset.color.getSaturation() == this.saturation && preset.color.getBrightness() == this.brightness) {
                canAppend = false;
            }
            if (!(45.0f + (xOffset += 20.0f) > this.width)) continue;
            xOffset = 0.0f;
            yOffset += 18.0f;
        }
        if (GuiUtility.isHovered((double)(this.x + 45.0f + xOffset), (double)(this.y + this.height - 36.0f + yOffset), 11.0, 11.0, mouseX, mouseY) && canAppend) {
            COLOR_PRESETS.add(new Preset(this.built()));
            Rockstar.getInstance().getFileManager().writeFile("client");
            return;
        }
        if (button.getButtonIndex() != 0) {
            this.pick = false;
            return;
        }
        if (this.pick) {
            ColorRGBA color = ColorRGBA.fromPixel((float)(mouseX * sr.getScaleFactor()), (float)((double)mw.getHeight() - mouseY * sr.getScaleFactor()));
            this.update(color);
            this.pick = false;
        }
        if (GuiUtility.isHovered((double)(this.x + 7.0f), (double)(this.y + 6.0f), 8.0, 8.0, mouseX, mouseY)) {
            this.pick = true;
            return;
        }
        if (GuiUtility.isHovered((double)(this.x + this.width - 15.0f), (double)(this.y + 5.0f), 10.0, 10.0, mouseX, mouseY)) {
            this.showing = false;
            this.offsetFactor = 2.0f;
            return;
        }
        if (GuiUtility.isHovered((double)(this.x + this.width - 18.0f), (double)(this.y + 20.0f), 12.0, 70.0, mouseX, mouseY)) {
            this.dragHue = true;
            return;
        }
        if (GuiUtility.isHovered((double)(this.x + 6.0f), (double)(this.y + 20.0f), 114.0, 70.0, mouseX, mouseY)) {
            this.dragBS = true;
            return;
        }
        if (GuiUtility.isHovered((double)(this.x + 6.0f), (double)(this.y + 102.0f), 100.0, 12.0, mouseX, mouseY)) {
            this.dragAlpha = true;
            return;
        }
        if (this.isHovered(mouseX, mouseY)) {
            this.drag = true;
            this.dragX = (float)(mouseX - (double)this.x);
            this.dragY = (float)(mouseY - (double)this.y);
        }
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.drag = false;
        this.dragBS = false;
        this.dragHue = false;
        this.dragAlpha = false;
    }

    public void update(ColorRGBA color) {
        this.hue = color.getHue();
        this.brightness = color.getBrightness();
        this.saturation = color.getSaturation();
        this.alpha = color.getAlpha() / 255.0f;
        this.activePreviewColorAnimation.update(color);
    }

    @Generated
    public Animation getAnimation() {
        return this.animation;
    }

    @Generated
    public boolean isShowing() {
        return this.showing;
    }

    @Generated
    public void setShowing(boolean showing) {
        this.showing = showing;
    }

    @Generated
    public boolean isDrag() {
        return this.drag;
    }

    @Generated
    public boolean isPick() {
        return this.pick;
    }

    public static class Preset {
        private final ColorRGBA color;
        private final Animation selected = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
        private final Animation animation = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
        private boolean showing = true;

        @Generated
        public Preset(ColorRGBA color) {
            this.color = color;
        }

        @Generated
        public ColorRGBA getColor() {
            return this.color;
        }

        @Generated
        public Animation getSelected() {
            return this.selected;
        }

        @Generated
        public Animation getAnimation() {
            return this.animation;
        }

        @Generated
        public boolean isShowing() {
            return this.showing;
        }
    }
}

