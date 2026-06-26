package moscow.rockstar.ui.menu.modern.components;

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
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.ui.menu.modern.ModernCategory;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.animation.types.ColorAnimation;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.sounds.ClientSounds;

/**
 * A module card in the Expensive-style ClickGUI. Right-clicking expands the module's settings
 * INLINE underneath the card header (pushing the rest of the column down) instead of a popup.
 */
public class ModernModule extends CustomComponent {
    public static final float HEADER = 28.0f;

    private final Animation visible = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation offset = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation enableAnimation = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation expandAnimation = new Animation(350L, 0.0f, Easing.QUARTIC_OUT);
    private final Module module;
    private final ModernCategory category;
    private boolean bindingMode;
    private boolean expanded;
    private final Animation shakeAnimation = new Animation(100L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation blockingAnimation = new Animation(500L, Easing.FIGMA_EASE_IN_OUT);
    private final ColorAnimation blockingColorAnimation = new ColorAnimation(500L, ColorRGBA.WHITE, Easing.FIGMA_EASE_IN_OUT);
    private boolean blocking;
    private boolean shakeValue;
    private final List<MenuSettingComponent> components = new ArrayList<>();

    @Generated
    public ModernModule(Module module, ModernCategory category) {
        this.module = module;
        this.category = category;
        if (module != null) {
            for (Setting setting : module.getSettings()) {
                MenuSettingComponent c = GuiUtility.settinge(setting, this);
                if (c != null) {
                    this.components.add(c);
                }
            }
        }
    }

    /** Total height of the card = header + (animated) settings area. */
    public float fullHeight() {
        return HEADER + this.settingsHeight() * this.expandAnimation.getValue();
    }

    private float settingsHeight() {
        float h = 0.0f;
        for (MenuSettingComponent c : this.components) {
            h += c.getHeight() * c.getOpacity();
        }
        return h + 6.0f;
    }

    @Override
    protected void renderComponent(UIContext context) {
        this.enableAnimation.setEasing(Easing.QUARTIC_OUT);
        this.enableAnimation.update(this.module.isEnabled());
        this.hoverAnimation.update(this.isHoveredHeader(context.getMouseX(), context.getMouseY()));
        this.blockingAnimation.update(this.blocking);
        this.expandAnimation.update(this.expanded ? 1.0f : 0.0f);
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
        this.height = this.fullHeight();
        context.drawSquircle(this.x, this.y, this.width, this.height, 5.0f, BorderRadius.all(6.0f),
                (!dark ? Colors.getAdditionalColor().mulAlpha(0.3f) : Colors.getBackgroundColor().mulAlpha(0.3f)).mulAlpha(this.visible.getValue()));
    }

    public void renderRounds(UIContext context) {
        context.drawRoundedRect(this.x + this.width - 25.0f, this.y + (HEADER - 7.0f) / 2.0f, 14.5f, 7.0f, BorderRadius.all(2.75f),
                Colors.getAdditionalColor().mix(Colors.ACCENT, this.enableAnimation.getValue()).mulAlpha(this.visible.getValue()));
    }

    public void renderInto(UIContext context) {
        context.drawRoundedRect(this.x + this.width - 25.0f + 1.0f + 5.0f * this.enableAnimation.getValue(), this.y + (HEADER - 5.0f) / 2.0f, 7.5f, 5.0f, BorderRadius.all(1.75f),
                Colors.WHITE.mulAlpha(this.visible.getValue()));
    }

    public void renderMedium(UIContext context) {
        int key = this.module.getKey();
        Object bindingText = key == -1 ? Localizator.translate("menu.binding") : Localizator.translate("key") + ": " + TextUtility.getKeyName(key);
        context.drawText(Fonts.MEDIUM.getFont(7.0f), (String) (this.bindingMode ? bindingText : this.module.getName()),
                this.x + 9.0f + this.shakeAnimation.getValue(), this.y + (HEADER - Fonts.MEDIUM.getFont(7.0f).height()) / 2.0f - 0.5f,
                this.blockingColorAnimation.getColor().mulAlpha(RenderSystem.getShaderColor()[3] * 0.75f + 0.25f * this.enableAnimation.getValue() + 0.25f * this.hoverAnimation.getValue()).mulAlpha(this.visible.getValue()));
    }

    /** Draws the inline settings area (called by ModernScreen after the card background). */
    public void renderSettings(UIContext context) {
        if (this.expandAnimation.getValue() <= 0.001f) {
            return;
        }
        float areaTop = this.y + HEADER;
        float areaH = this.settingsHeight() * this.expandAnimation.getValue();
        context.drawRect(this.x + 6.0f, areaTop - 0.5f, this.width - 12.0f, 0.5f, Colors.getTextColor().withAlpha(12.0f));
        ScissorUtility.push(context.getMatrices(), this.x, areaTop, this.width, areaH);
        float off = 4.0f;
        for (MenuSettingComponent c : this.components) {
            c.getVisibilityAnimation().update(c.getSetting().isVisible() ? 1.0f : 0.0f);
            c.setX(this.x + 2.0f);
            c.setY(areaTop + off);
            c.setWidth(this.width - 4.0f);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, c.getOpacity());
            context.pushMatrix();
            context.getMatrices().translate(0.0f, (-c.getHeight() + c.getHeight() * c.getOpacity()) / 2.0f, 0.0f);
            c.render(context);
            context.popMatrix();
            off += c.getHeight() * c.getOpacity();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        ScissorUtility.pop();
    }

    private boolean isHoveredHeader(double mx, double my) {
        return GuiUtility.isHovered((double) this.x, (double) this.y, (double) this.width, (double) HEADER, mx, my);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        // forward to settings first when expanded
        if (this.expanded) {
            for (MenuSettingComponent c : this.components) {
                if (c.getOpacity() == 0.0f) {
                    continue;
                }
                // СКМ по настройке — сброс к рекомендованному значению.
                if (button == MouseButton.MIDDLE && c.isHovered(mouseX, mouseY)) {
                    c.getSetting().resetDefault();
                    return;
                }
                c.onMouseClicked(mouseX, mouseY, button);
            }
        }
        if (!this.isHoveredHeader(mouseX, mouseY)) {
            return;
        }
        if (this.bindingMode && button != MouseButton.LEFT && button != MouseButton.RIGHT) {
            this.module.setKey(button.getButtonIndex());
            this.bindingMode = false;
            return;
        }
        switch (button) {
            case LEFT:
                this.module.toggle();
                break;
            case MIDDLE:
                for (ModernModule comp : this.category.getModules()) {
                    comp.setBindingMode(false);
                }
                this.bindingMode = true;
                break;
            case RIGHT:
                this.toggleExpand();
        }
    }

    private void toggleExpand() {
        if (this.module.getSettings().isEmpty()) {
            if (Rockstar.getInstance().getModuleManager().getModule(Sounds.class).isEnabled() && !this.blocking) {
                ClientSounds.CRITICAL.play(1.0f, 1.0f);
            }
            this.blocking = true;
            this.shakeValue = true;
            return;
        }
        this.expanded = !this.expanded;
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        if (this.expanded) {
            for (MenuSettingComponent c : this.components) {
                if (c.getOpacity() == 0.0f) {
                    continue;
                }
                c.onMouseReleased(mouseX, mouseY, button);
            }
        }
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.expanded) {
            for (MenuSettingComponent c : this.components) {
                if (c.getOpacity() == 0.0f) {
                    continue;
                }
                c.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
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
            if (menuScreen instanceof DropDownScreen dropDownScreen) {
                dropDownScreen.getSearchField().setFocused(false);
            }
        }
        if (this.expanded) {
            for (MenuSettingComponent c : this.components) {
                if (c.getOpacity() == 0.0f) {
                    continue;
                }
                c.onKeyPressed(keyCode, scanCode, modifiers);
            }
        }
        super.onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.expanded) {
            for (MenuSettingComponent c : this.components) {
                if (c.getOpacity() == 0.0f) {
                    continue;
                }
                c.charTyped(chr, modifiers);
            }
        }
        return super.charTyped(chr, modifiers);
    }

    public boolean isBinding() {
        return false;
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
    public boolean isExpanded() {
        return this.expanded;
    }

    @Generated
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Generated
    public List<MenuSettingComponent> getComponents() {
        return this.components;
    }

    @Generated
    public void setBindingMode(boolean bindingMode) {
        this.bindingMode = bindingMode;
    }
}
