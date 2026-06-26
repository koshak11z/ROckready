/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 */
package moscow.rockstar.ui.menu.modern.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.ui.menu.modern.ModernScreen;
import moscow.rockstar.ui.menu.modern.components.ModernModule;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.animation.types.ColorAnimation;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;

public class ModernSettings
extends CustomComponent {
    private final ModernModule module;
    private List<MenuSettingComponent> components = new ArrayList<MenuSettingComponent>();
    private final Animation animation = new Animation(300L, 0.0f, Easing.BAKEK_SIZE);
    private boolean showing;
    private final ScrollHandler scrollHandler = new ScrollHandler();
    private float dragX;
    private float dragY;
    private boolean drag;
    private final Animation hoverAnimation = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation circleOpacityAnimation = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation enableAnimation = new Animation(300L, Easing.BAKEK);
    private final ColorAnimation backgroundColorAnimation = new ColorAnimation(300L, new ColorRGBA(24.0f, 24.0f, 27.0f), Easing.FIGMA_EASE_IN_OUT);

    public ModernSettings(ModernModule module, float x, float y, float width) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.showing = true;
        for (Setting setting : module.getModule().getSettings()) {
            MenuSettingComponent settingComponent = GuiUtility.settinge(setting, this);
            if (settingComponent == null) continue;
            this.components.add(settingComponent);
        }
    }

    @Override
    protected void renderComponent(UIContext context) {
        this.animation.setDuration(this.showing ? 500L : 300L);
        this.animation.update(this.showing && ModernSettings.mc.currentScreen instanceof ModernScreen);
        this.scrollHandler.update();
        if (this.drag) {
            this.x = (float)context.getMouseX() - this.dragX;
            this.y = (float)context.getMouseY() - this.dragY;
        }
        float alpha = Math.min(1.0f, this.animation.getValue());
        boolean check = Rockstar.getInstance().getMenuScreen().getMenuAnimation().getValue() == Rockstar.getInstance().getMenuScreen().getMenuAnimation().getTargetValue();
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        this.animation.setEasing(this.showing ? Easing.QUARTIC_OUT : Easing.BAKEK_BACK);
        float x = MathUtility.interpolate(this.module.getX(), this.x, alpha);
        float y = MathUtility.interpolate(this.module.getY(), this.y, alpha);
        float width = MathUtility.interpolate(this.module.getWidth(), this.width, alpha);
        float height = MathUtility.interpolate(this.module.getHeight(), this.height, alpha);
        if (!(this.showing && check && ModernSettings.mc.currentScreen instanceof ModernScreen)) {
            x = this.x;
            y = this.y;
            width = this.width;
            height = this.height;
        }
        if (!this.showing || !check) {
            RenderUtility.scale(context.getMatrices(), x + width / 2.0f, y + height / 2.0f, 0.5f + 0.5f * this.animation.getValue());
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        }
        if (Interface.blurPopupsEnabled()) {
            context.drawBlurredRect(x, y, width, height, 45.0f, 5.0f, BorderRadius.all(11.0f), Colors.WHITE);
        }
        context.drawSquircle(x, y, width, height, 5.0f, BorderRadius.all(6.0f + 5.0f * alpha), (!dark ? Colors.getBackgroundColor().mix(Colors.getAdditionalColor(), 0.3f) : Colors.getAdditionalColor().mix(Colors.getBackgroundColor(), 0.3f)).mix(dark ? Colors.getAdditionalColor().mulAlpha(0.98f) : Colors.getBackgroundColor().mulAlpha(0.75f), alpha));
        if (this.showing && check) {
            context.drawRoundedRect(x + width - 25.0f, y + 10.5f + 20.0f * alpha, 14.5f, 7.0f, BorderRadius.all(3.0f), Colors.getAdditionalColor().mix(Colors.ACCENT, this.module.getModule().isEnabled() ? 1.0f : 0.0f).mulAlpha(1.0f - alpha));
            context.drawRoundedRect(x + width - 25.0f + 1.0f + (float)(5 * (this.module.getModule().isEnabled() ? 1 : 0)), y + 11.5f + 20.0f * alpha, 7.5f, 5.0f, BorderRadius.all(1.75f), Colors.WHITE.mulAlpha(1.0f - alpha));
            context.drawFadeoutText(Fonts.REGULAR.getFont(6.0f), this.module.getModule().getDescription(), x + 7.0f, y + 16.0f + 5.0f * alpha, Colors.getTextColor().mulAlpha(0.5f).mulAlpha(1.0f - alpha), 0.9f, 1.0f, width - 30.0f);
        }
        if (this.showing) {
            context.drawText(Fonts.MEDIUM.getFont(7.0f + 2.0f * alpha), this.module.getModule().getName(), x + 7.0f + 2.0f * alpha, y + 8.0f + 2.0f * alpha, Colors.getTextColor());
        } else {
            context.drawText(Fonts.MEDIUM.getFont(9.0f), this.module.getModule().getName(), x + 9.0f, y + 10.0f, Colors.getTextColor());
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        context.drawTexture(Rockstar.id("icons/close.png"), x + width - 17.0f, y + 9.0f, 8.0f, 8.0f, Colors.getTextColor());
        if (GuiUtility.isHovered(x + width - 17.0f, y + 9.0f, 8.0, 8.0, context)) {
            CursorUtility.set(CursorType.HAND);
        }
        if (Interface.showMinimalizm()) {
            context.drawRect(x, y + 24.0f, width, 4.0f, Colors.getSeparatorColor().withAlpha(Colors.getSeparatorColor().getAlpha() * Interface.minimalizm()));
        }
        float settingsY = 28.0f;
        float offset = 0.0f;
        ScissorUtility.push(context.getMatrices(), x, y + 28.0f, width, height - 28.0f - 5.0f);
        this.circleOpacityAnimation.update(this.module.getModule().isEnabled() ? 1.0f : 0.75f);
        this.enableAnimation.update(this.module.getModule().isEnabled() ? 1.0f : 0.0f);
        this.backgroundColorAnimation.update(this.module.getModule().isEnabled() ? new ColorRGBA(151.0f, 71.0f, 255.0f) : Rockstar.getInstance().getThemeManager().getCurrentTheme().getAdditionalColor());
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        if (this.isHovered(context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
        float settingY = (float)((double)(y + 28.0f) - this.scrollHandler.getValue());
        float checkWidth = 13.0f;
        float checkHeight = 8.0f;
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        float leftPadding = 10.0f;
        float headerHeight = 19.0f;
        String name = Localizator.translate("enabled");
        context.drawFadeoutText(nameFont, name.substring(0, 1).toUpperCase() + name.substring(1), x + leftPadding, settingY + GuiUtility.getMiddleOfBox(nameFont.height(), headerHeight) - 0.5f, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.enableAnimation.getValue() + 0.25f * this.hoverAnimation.getValue())), 0.7f, 0.99f, width - checkWidth - 20.0f);
        context.drawRoundedRect(x + width - checkWidth - 9.0f, settingY + 5.0f, checkWidth, checkHeight, BorderRadius.all(3.0f), this.backgroundColorAnimation.getColor().withAlpha(!this.module.getModule().isEnabled() ? 255.0f - 100.0f * Interface.glass() : 255.0f));
        context.drawRoundedRect(x + width - checkWidth - 8.0f + 5.0f * this.enableAnimation.getValue(), settingY + 6.0f, 6.0f, 6.0f, BorderRadius.all(4.0f), new ColorRGBA(255.0f, 255.0f, 255.0f).withAlpha(this.circleOpacityAnimation.getValue() * 255.0f));
        float separatorHeight = 0.5f;
        context.drawRect(x, settingY + 18.0f, width, separatorHeight, Colors.getTextColor().withAlpha(5.1f));
        offset += 18.0f;
        for (MenuSettingComponent settingComponent : this.components) {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(settingComponent.getOpacity() * alpha));
            settingComponent.getVisibilityAnimation().update(settingComponent.getSetting().isVisible() ? 1.0f : 0.0f);
            settingComponent.setX(x);
            settingComponent.setY((float)((double)(y + settingsY + offset) - this.scrollHandler.getValue()));
            settingComponent.setWidth(width);
            if (GuiUtility.isHovered((double)x, (double)(y - settingComponent.getHeight()), (double)width, (double)(height + settingComponent.getHeight()), settingComponent.getX(), settingComponent.getY())) {
                context.pushMatrix();
                context.getMatrices().translate(0.0f, (-settingComponent.getHeight() + settingComponent.getHeight() * settingComponent.getOpacity()) / 2.0f, 0.0f);
                settingComponent.render(context);
                context.popMatrix();
            }
            offset += settingComponent.getHeight() * settingComponent.getOpacity();
        }
        ScissorUtility.pop();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        this.height = Math.min(200.0f, offset + 28.0f + 5.0f);
        this.scrollHandler.setMax(-offset + height - 24.0f - 4.0f - 5.0f);
        if (!this.showing || !check) {
            RenderUtility.end(context.getMatrices());
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        for (MenuSettingComponent component : this.components) {
            if (component.getOpacity() == 0.0f || !this.isHovered(mouseX, mouseY) && button == MouseButton.LEFT) continue;
            // СКМ по настройке — сброс к рекомендованному значению.
            if (button == MouseButton.MIDDLE && component.isHovered(mouseX, mouseY)) {
                component.getSetting().resetDefault();
                continue;
            }
            component.onMouseClicked(mouseX, mouseY, button);
        }
        if (GuiUtility.isHovered((double)this.x, (double)(this.y + 24.0f) - this.scrollHandler.getValue(), (double)this.width, 18.0, mouseX, mouseY) && button == MouseButton.LEFT) {
            this.module.getModule().toggle();
        }
        if (GuiUtility.isHovered((double)this.x, (double)this.y, (double)this.width, 24.0, mouseX, mouseY)) {
            this.drag = true;
            this.dragX = (float)(mouseX - (double)this.x);
            this.dragY = (float)(mouseY - (double)this.y);
        }
        if (GuiUtility.isHovered((double)(this.x + this.width - 17.0f), (double)(this.y + 9.0f), 8.0, 8.0, mouseX, mouseY)) {
            this.showing = false;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        for (MenuSettingComponent component : this.components) {
            if (component.getOpacity() == 0.0f) continue;
            component.onMouseReleased(mouseX, mouseY, button);
        }
        this.drag = false;
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (MenuSettingComponent component : this.components) {
            if (component.getOpacity() == 0.0f) continue;
            component.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (this.isHovered(mouseX, mouseY)) {
            this.scrollHandler.scroll(verticalAmount);
        }
        super.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        for (MenuSettingComponent component : this.components) {
            if (component.getOpacity() == 0.0f) continue;
            component.onKeyPressed(keyCode, scanCode, modifiers);
        }
        super.onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (MenuSettingComponent component : this.components) {
            if (component.getOpacity() == 0.0f) continue;
            component.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public float getHeight() {
        float offset = 18.0f;
        for (MenuSettingComponent settingComponent : this.components) {
            offset += settingComponent.getHeight() * settingComponent.getOpacity();
        }
        this.height = Math.min(200.0f, offset + 28.0f + 5.0f);
        return this.height;
    }

    @Generated
    public ModernModule getModule() {
        return this.module;
    }

    @Generated
    public List<MenuSettingComponent> getComponents() {
        return this.components;
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
    public ScrollHandler getScrollHandler() {
        return this.scrollHandler;
    }

    @Generated
    public float getDragX() {
        return this.dragX;
    }

    @Generated
    public float getDragY() {
        return this.dragY;
    }

    @Generated
    public boolean isDrag() {
        return this.drag;
    }

    @Generated
    public Animation getHoverAnimation() {
        return this.hoverAnimation;
    }

    @Generated
    public Animation getCircleOpacityAnimation() {
        return this.circleOpacityAnimation;
    }

    @Generated
    public Animation getEnableAnimation() {
        return this.enableAnimation;
    }

    @Generated
    public ColorAnimation getBackgroundColorAnimation() {
        return this.backgroundColorAnimation;
    }

    @Generated
    public void setShowing(boolean showing) {
        this.showing = showing;
    }
}

