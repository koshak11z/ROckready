/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 */
package moscow.rockstar.ui.menu.dropdown.components.module;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.ui.menu.dropdown.components.MenuPanel;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.animation.types.ColorAnimation;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.penis.PenisPlayer;
import moscow.rockstar.utility.sounds.ClientSounds;

public class ModuleComponent
extends CustomComponent {
    private final Module module;
    private final MenuPanel parent;
    private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation enableAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation shakeAnimation = new Animation(100L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation blockingAnimation = new Animation(500L, Easing.FIGMA_EASE_IN_OUT);
    private final ColorAnimation blockingColorAnimation = new ColorAnimation(500L, ColorRGBA.WHITE, Easing.FIGMA_EASE_IN_OUT);
    private boolean blocking;
    private boolean shakeValue;
    private final PenisPlayer enablePenis;
    private final PenisPlayer disablePenis;
    private PenisPlayer currentPenis;
    private boolean lastModuleState;
    private Font nameFont;
    private float headerHeight;
    private final List<MenuSettingComponent<?>> settingComponents = new ArrayList();
    private boolean bindingMode;

    public ModuleComponent(Module module, MenuPanel parent) {
        this.module = module;
        this.parent = parent;
        this.enablePenis = new PenisPlayer(Rockstar.id("penises/check_enable.penis"));
        this.disablePenis = new PenisPlayer(Rockstar.id("penises/check_disable.penis"));
        this.lastModuleState = module.isEnabled();
        PenisPlayer penisPlayer = this.currentPenis = this.lastModuleState ? this.enablePenis : this.disablePenis;
        if (this.lastModuleState) {
            this.enablePenis.playOnce();
            this.currentPenis = this.enablePenis;
        } else {
            this.disablePenis.setFrame(0);
            this.disablePenis.stop();
            this.currentPenis = this.disablePenis;
        }
    }

    @Override
    public void onInit() {
        this.nameFont = Fonts.REGULAR.getFont(8.0f);
        this.headerHeight = 20.0f;
        this.settingComponents.clear();
        for (Setting setting : this.module.getSettings()) {
            MenuSettingComponent settingComponent = GuiUtility.settinge(setting, this);
            if (settingComponent == null) continue;
            this.settingComponents.add(settingComponent);
        }
        this.settingComponents.forEach(MenuSettingComponent::onInit);
        for (MenuSettingComponent menuSettingComponent : this.settingComponents) {
            menuSettingComponent.getVisibilityAnimation().setValue(menuSettingComponent.getSetting().isVisible() ? 1.0f : 0.0f);
        }
        super.onInit();
    }

    @Override
    protected void renderComponent(UIContext context) {
        this.enableAnimation.update(this.module.isEnabled() ? 1.0f : 0.0f);
        boolean currentState = this.module.isEnabled();
        if (currentState != this.lastModuleState) {
            this.currentPenis = currentState ? this.enablePenis : this.disablePenis;
            this.currentPenis.playOnce();
            this.lastModuleState = currentState;
        }
        this.currentPenis.update();
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
        if (this.parent.isHovered(context) && this.isHovered(context)) {
            CursorUtility.set(CursorType.HAND);
            MenuScreen menuScreen = Rockstar.getInstance().getMenuScreen();
            if (menuScreen instanceof DropDownScreen) {
                DropDownScreen dropDownScreen = (DropDownScreen)menuScreen;
                dropDownScreen.setDesc(this.module.getDescription());
            }
        }
    }

    public void drawRegular8(UIContext context) {
        float nameLeftPadding = 10.0f + 2.0f * this.enableAnimation.getValue();
        float nameHeight = this.nameFont.height();
        int key = this.module.getKey();
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        Object bindingText = key == -1 ? Localizator.translate("menu.binding") : Localizator.translate("key") + ": " + TextUtility.getKeyName(key);
        context.drawText(this.nameFont, (String)(this.bindingMode && this.parent.getSelectedModuleComponent() == null ? bindingText : this.module.getName()), this.x + nameLeftPadding + this.shakeAnimation.getValue(), this.y + GuiUtility.getMiddleOfBox(nameHeight, this.headerHeight) - 0.5f, this.blockingColorAnimation.getColor().mulAlpha(RenderSystem.getShaderColor()[3] * (0.75f + 0.25f * this.enableAnimation.getValue() + 0.25f * this.hoverAnimation.getValue())));
    }

    public void drawIcons(UIContext context) {
        float alpha = this.enableAnimation.getValue() * RenderSystem.getShaderColor()[3];
        if (this.enableAnimation.getValue() > 0.0f || this.currentPenis.isPlaying()) {
            DrawUtility.drawAnimationSprite(context.getMatrices(), this.currentPenis.getCurrentSprite(), this.x + this.width - 15.0f - this.enableAnimation.getValue() * 2.0f, this.y + 7.0f, 6.0f, 6.0f, Colors.getTextColor().mulAlpha(0.1f + 0.9f * alpha));
        }
    }

    public void drawSplit(UIContext context) {
        float separatorHeight = 0.5f;
        context.drawRect(this.x, this.y + this.height, this.width, separatorHeight, Colors.getTextColor().withAlpha(RenderSystem.getShaderColor()[3] * 255.0f * 0.02f));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (!this.isHovered(mouseX, mouseY)) {
            return;
        }
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
                for (ModuleComponent comp : this.parent.getModuleComponents()) {
                    comp.setBindingMode(false);
                }
                this.bindingMode = true;
                break;
            }
            case RIGHT: {
                this.open();
            }
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    public void open() {
        if (this.module.getSettings().isEmpty()) {
            if (Rockstar.getInstance().getModuleManager().getModule(Sounds.class).isEnabled() && !this.blocking) {
                ClientSounds.CRITICAL.play(1.0f, 1.0f);
            }
            this.blocking = true;
            this.shakeValue = true;
        } else {
            this.parent.setSelectedModuleComponent(this);
            this.onInit();
            if (Rockstar.getInstance().getModuleManager().getModule(Sounds.class).isEnabled()) {
                ClientSounds.CLICKGUI_OPEN.play(0.8f, 1.3f);
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

    @Generated
    public Module getModule() {
        return this.module;
    }

    @Generated
    public MenuPanel getParent() {
        return this.parent;
    }

    @Generated
    public Animation getHoverAnimation() {
        return this.hoverAnimation;
    }

    @Generated
    public Animation getEnableAnimation() {
        return this.enableAnimation;
    }

    @Generated
    public Animation getShakeAnimation() {
        return this.shakeAnimation;
    }

    @Generated
    public Animation getBlockingAnimation() {
        return this.blockingAnimation;
    }

    @Generated
    public ColorAnimation getBlockingColorAnimation() {
        return this.blockingColorAnimation;
    }

    @Generated
    public boolean isBlocking() {
        return this.blocking;
    }

    @Generated
    public boolean isShakeValue() {
        return this.shakeValue;
    }

    @Generated
    public PenisPlayer getEnablePenis() {
        return this.enablePenis;
    }

    @Generated
    public PenisPlayer getDisablePenis() {
        return this.disablePenis;
    }

    @Generated
    public PenisPlayer getCurrentPenis() {
        return this.currentPenis;
    }

    @Generated
    public boolean isLastModuleState() {
        return this.lastModuleState;
    }

    @Generated
    public Font getNameFont() {
        return this.nameFont;
    }

    @Generated
    public float getHeaderHeight() {
        return this.headerHeight;
    }

    @Generated
    public List<MenuSettingComponent<?>> getSettingComponents() {
        return this.settingComponents;
    }

    @Generated
    public boolean isBindingMode() {
        return this.bindingMode;
    }

    @Generated
    public void setBindingMode(boolean bindingMode) {
        this.bindingMode = bindingMode;
    }
}

