/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 */
package moscow.rockstar.ui.menu.modern.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.ui.menu.modern.ModernCategory;
import moscow.rockstar.ui.menu.modern.ModernScreen;
import moscow.rockstar.ui.menu.modern.components.ModernSettings;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.animation.types.ColorAnimation;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.obj.Rect;
import moscow.rockstar.utility.sounds.ClientSounds;

public class ModernModule
extends CustomComponent {
    private final Animation visible = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation offset = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation enableAnimation = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Module module;
    private final ModernCategory category;
    private boolean bindingMode;
    private final Animation shakeAnimation = new Animation(100L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation blockingAnimation = new Animation(500L, Easing.FIGMA_EASE_IN_OUT);
    private final ColorAnimation blockingColorAnimation = new ColorAnimation(500L, ColorRGBA.WHITE, Easing.FIGMA_EASE_IN_OUT);
    private boolean blocking;
    private boolean shakeValue;

    @Override
    protected void renderComponent(UIContext context) {
        this.enableAnimation.setEasing(Easing.QUARTIC_OUT);
        this.enableAnimation.update(this.module.isEnabled());
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        this.blockingAnimation.update(this.blocking);
        ColorRGBA baseTextColor = this.module.isEnabled() ? Colors.getGuiTextActiveColor() : Colors.getGuiTextInactiveColor();
        this.blockingColorAnimation.update(this.blocking ? new ColorRGBA(255.0f, 150.0f, 150.0f) : baseTextColor);
        this.shakeAnimation.update(this.blocking ? (this.shakeValue ? 1.0f : -1.0f) : 0.0f);
        if (this.blockingAnimation.getValue() == 1.0f) {
            this.blocking = false;
        }
        if (this.shakeAnimation.getValue() == 1.0f) {
            this.shakeValue = false;
        }
        if (this.shakeAnimation.getValue() == -1.0f) {
            this.shakeValue = true;
        }
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        context.drawSquircle(this.x, this.y, this.width, this.height, 5.0f, BorderRadius.all(6.0f), (!dark ? Colors.getAdditionalColor().mulAlpha(0.3f) : Colors.getBackgroundColor().mulAlpha(0.3f)).mulAlpha(this.visible.getValue()));
    }

    public void renderRounds(UIContext context) {
        context.drawRoundedRect(this.x + this.width - 25.0f, this.y + 10.5f, 14.5f, 7.0f, BorderRadius.all(2.75f), Colors.getAdditionalColor().mix(Colors.ACCENT, this.enableAnimation.getValue()).mulAlpha(this.visible.getValue()));
    }

    public void renderInto(UIContext context) {
        context.drawRoundedRect(this.x + this.width - 25.0f + 1.0f + 5.0f * this.enableAnimation.getValue(), this.y + 11.5f, 7.5f, 5.0f, BorderRadius.all(1.75f), Colors.WHITE.mulAlpha(this.visible.getValue()));
    }

    public void renderMedium(UIContext context) {
        int key = this.module.getKey();
        Object bindingText = key == -1 ? Localizator.translate("menu.binding") : Localizator.translate("key") + ": " + TextUtility.getKeyName(key);
        context.drawText(Fonts.MEDIUM.getFont(7.0f), (String)(this.bindingMode ? bindingText : this.module.getName()), this.x + 7.0f + this.shakeAnimation.getValue(), this.y + 8.0f, this.blockingColorAnimation.getColor().mulAlpha(RenderSystem.getShaderColor()[3] * 0.75f + 0.25f * this.enableAnimation.getValue() + 0.25f * this.hoverAnimation.getValue()).mulAlpha(this.visible.getValue()));
    }

    public void renderRegular(UIContext context) {
        context.drawText(Fonts.REGULAR.getFont(6.0f), this.module.getDescription(), this.x + 7.0f, this.y + 16.0f, Colors.getTextColor().mulAlpha(0.5f * this.visible.getValue()));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.bindingMode && button != MouseButton.LEFT && button != MouseButton.RIGHT) {
            this.module.setKey(button.getButtonIndex());
            this.bindingMode = false;
            return;
        }
        switch (button) {
            case LEFT: {
                this.module.toggle();
                break;
            }
            case MIDDLE: {
                for (ModernModule comp : this.category.getModules()) {
                    comp.setBindingMode(false);
                }
                this.bindingMode = true;
                break;
            }
            case RIGHT: {
                this.open();
            }
        }
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.bindingMode) {
            if (keyCode == 256 || keyCode == 261) {
                this.module.setKey(-1);
            } else {
                this.module.setKey(keyCode);
            }
            this.bindingMode = false;
            MenuScreen menuScreen = Rockstar.getInstance().getMenuScreen();
            if (menuScreen instanceof DropDownScreen) {
                DropDownScreen dropDownScreen = (DropDownScreen)menuScreen;
                dropDownScreen.getSearchField().setFocused(false);
            }
        }
        super.onKeyPressed(keyCode, scanCode, modifiers);
    }

    public void open() {
        if (this.module.getSettings().isEmpty()) {
            if (Rockstar.getInstance().getModuleManager().getModule(Sounds.class).isEnabled() && !this.blocking) {
                ClientSounds.CRITICAL.play(1.0f, 1.0f);
            }
            this.blocking = true;
            this.shakeValue = true;
        } else {
            ModernScreen modernScreen = (ModernScreen)Rockstar.getInstance().getMenuScreen();
            Rect win = modernScreen.getMenuWindow();
            List<ModernSettings> windows = modernScreen.getWindows();
            float x = win.getX() + win.getWidth() + 10.0f;
            float y = win.getY();
            float width = 152.0f;
            if (!windows.isEmpty()) {
                float h = windows.getLast().getY() + windows.getLast().getHeight();
                if (h < win.getY() + win.getHeight()) {
                    y = h + 10.0f;
                    x = windows.getLast().getX();
                } else {
                    x = windows.getLast().getX() + windows.getLast().getWidth() + 10.0f;
                }
            }
            for (ModernSettings window : windows) {
                if (window.getModule() != this) continue;
                return;
            }
            if (!windows.isEmpty() && x + width > IScaledResolution.sr.getScaledWidth() || windows.size() > 4) {
                x = windows.getFirst().getX();
                y = windows.getFirst().getY();
                windows.getFirst().setShowing(false);
                ModernSettings newWindow = new ModernSettings(this, x, y, width);
                windows.addFirst(newWindow);
                float offset = y + newWindow.getHeight() + 10.0f;
                for (ModernSettings window : windows) {
                    if (window.getX() != x || window.getModule() == this) continue;
                    window.setY(offset);
                    offset += window.getHeight() + 10.0f;
                }
            } else {
                windows.add(new ModernSettings(this, x, y, width));
            }
            this.visible.setValue(0.0f);
        }
    }

    public boolean isBinding() {
        return false;
    }

    @Generated
    public ModernModule(Module module, ModernCategory category) {
        this.module = module;
        this.category = category;
    }

    @Generated
    public Animation getVisible() {
        return this.visible;
    }

    @Generated
    public Animation getOffset() {
        return this.offset;
    }

    @Generated
    public Module getModule() {
        return this.module;
    }

    @Generated
    public void setBindingMode(boolean bindingMode) {
        this.bindingMode = bindingMode;
    }
}

