/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.menu.dropdown.components.settings;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.ui.menu.dropdown.components.module.ModuleComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;

public abstract class MenuSettingComponent<T extends Setting>
extends CustomComponent {
    private final CustomComponent parent;
    protected final T setting;
    private final Animation visibilityAnimation = new Animation(300L, Easing.BAKEK_PAGES);
    protected final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

    public MenuSettingComponent(T setting, CustomComponent parent) {
        this.parent = parent;
        this.setting = setting;
    }

    @Override
    public void update(UIContext context) {
        ModuleComponent component;
        String translatedDescription = Localizator.translateOrEmpty(this.setting.getDescription());
        CustomComponent customComponent = this.parent;
        if (customComponent instanceof ModuleComponent && ((component = (ModuleComponent)customComponent).getParent().isHovered(context) && this.isHovered(context) || Rockstar.getInstance().getMenuScreen() instanceof DropDownScreen)) {
            ((DropDownScreen)Rockstar.getInstance().getMenuScreen()).setDesc(Localizator.translate(translatedDescription));
        }
        if (this.parent instanceof Popup && this.isHovered(context)) {
            Rockstar.getInstance().getHud().setDesc(Localizator.translate(translatedDescription));
        }
        super.update(context);
    }

    @Override
    public void onInit() {
        super.onInit();
    }

    public float getOpacity() {
        return this.visibilityAnimation.getValue();
    }

    public void drawRegular8(UIContext context) {
    }

    public void drawSplit(UIContext context) {
    }

    @Generated
    public CustomComponent getParent() {
        return this.parent;
    }

    @Generated
    public T getSetting() {
        return this.setting;
    }

    @Generated
    public Animation getVisibilityAnimation() {
        return this.visibilityAnimation;
    }

    @Generated
    public Animation getHoverAnimation() {
        return this.hoverAnimation;
    }
}

