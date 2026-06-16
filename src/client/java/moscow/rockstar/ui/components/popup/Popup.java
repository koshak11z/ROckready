/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.render.VertexFormats
 */
package moscow.rockstar.ui.components.popup;

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
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.popup.CheckBoxAction;
import moscow.rockstar.ui.components.popup.PopupAction;
import moscow.rockstar.ui.components.popup.list.Button;
import moscow.rockstar.ui.components.popup.list.CheckBox;
import moscow.rockstar.ui.components.popup.list.Separator;
import moscow.rockstar.ui.components.popup.list.Text;
import moscow.rockstar.ui.components.popup.list.Title;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import net.minecraft.client.render.VertexFormats;

public class Popup
extends CustomComponent {
    private final Animation animation = new Animation(300L, 0.0f, Easing.BAKEK_SIZE);
    private final Animation blurAnim = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final List<CustomComponent> components = new ArrayList<CustomComponent>();
    private boolean showing;
    private final float offsetFactor;
    private Runnable onClose = () -> {};
    private boolean closed;

    public Popup(float x, float y) {
        this(x, y, 90.0f);
    }

    public Popup(float x, float y, float width) {
        this(x, y, width, 2.0f);
    }

    public Popup(float x, float y, float width, float offsetFactor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.offsetFactor = offsetFactor;
        this.showing = true;
    }

    @Override
    protected void renderComponent(UIContext context) {
        this.animation.setEasing(this.showing ? Easing.BAKEK : Easing.BAKEK_BACK);
        this.animation.update(this.showing);
        this.blurAnim.update(this.animation.getValue() >= 0.6f);
        this.height = 0.0f;
        for (CustomComponent component : this.components) {
            float f;
            if (component instanceof MenuSettingComponent) {
                component.set(this.x - 2.0f, this.y + this.height, this.width + 4.0f, 0.0f);
            } else {
                component.set(this.x, this.y + this.height, this.width, 0.0f);
            }
            float f2 = component.getHeight() + 0.5f;
            if (component instanceof MenuSettingComponent) {
                MenuSettingComponent settingComponent = (MenuSettingComponent)component;
                f = settingComponent.getOpacity();
            } else {
                f = 1.0f;
            }
            this.height += f2 * f;
        }
        this.height += 2.0f;
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)Math.min(1.0f, this.animation.getValue()));
        RenderUtility.scale(context.getMatrices(), this.x + this.width / this.offsetFactor, this.y + this.height / this.offsetFactor, 0.5f + this.animation.getValue() * 0.5f);
        context.drawShadow(this.x, this.y, this.width, this.height, 15.0f, BorderRadius.all(6.0f), ColorRGBA.BLACK.withAlpha(127.5f));
        if (Interface.showMinimalizm() && Interface.blurPopupsEnabled()) {
            context.drawBlurredRect(this.x, this.y, this.width, this.height, 45.0f, 7.0f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue() * Interface.minimalizm()));
        }
        if (Interface.showGlass()) {
            context.drawLiquidGlass(this.x, this.y, this.width, this.height, 7.0f, 0.08f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue() * Interface.glass()));
        }
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        context.drawSquircle(this.x, this.y, this.width, this.height, 7.0f, BorderRadius.all(6.0f), Colors.getBackgroundColor().mulAlpha(dark ? 0.8f - 0.6f * Interface.glass() : 0.7f));
        for (CustomComponent component : this.components) {
            int index;
            if (component instanceof MenuSettingComponent) {
                MenuSettingComponent settingComponent = (MenuSettingComponent)component;
                settingComponent.getVisibilityAnimation().update(settingComponent.getSetting().isVisible() ? 1.0f : 0.0f);
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(settingComponent.getOpacity() * this.animation.getValue()));
            }
            if ((index = this.components.indexOf(component)) != 0 && !(component instanceof Separator) && !(this.components.get(index - 1) instanceof Separator)) {
                float separatorHeight = 0.5f;
                context.drawRect(this.x, component.getY() - 1.0f, this.width, separatorHeight, Colors.getTextColor().withAlpha(5.1f));
            }
            component.render(context);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)this.animation.getValue());
        }
        for (CustomComponent component : this.components) {
            if (!(component instanceof MenuSettingComponent)) continue;
            MenuSettingComponent comp = (MenuSettingComponent)component;
            FontBatching font = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.REGULAR);
            comp.drawRegular8(context);
            ((Batching)font).draw();
        }
        RenderUtility.end(context.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public Popup setting(Setting setting) {
        MenuSettingComponent settingComponent = GuiUtility.settinge(setting, this);
        if (settingComponent != null) {
            this.components.add(settingComponent);
        }
        return this;
    }

    public Popup add(CustomComponent component) {
        this.components.add(component);
        return this;
    }

    public Popup text(String text) {
        this.components.add(new Text(text));
        return this;
    }

    public Popup title(String text) {
        this.components.add(new Title(text));
        return this;
    }

    public Popup separator() {
        this.components.add(new Separator());
        return this;
    }

    public Popup checkbox(String text, boolean enabled) {
        this.components.add(new CheckBox(text).enabled(enabled));
        return this;
    }

    public Popup checkbox(String text, boolean enabled, CheckBoxAction action) {
        this.components.add(new CheckBox(text).enabled(enabled).action(action));
        return this;
    }

    public Popup button(String text, String icon, PopupAction runnable) {
        this.components.add(new Button(this, text, icon, runnable));
        return this;
    }

    public Popup onClose(Runnable onClose) {
        this.onClose = onClose;
        return this;
    }

    public void setShowing(boolean showing) {
        this.showing = showing;
        if (!showing && !this.closed) {
            this.onClose.run();
            this.closed = true;
        }
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        for (CustomComponent component : this.components) {
            MenuSettingComponent settingComponent;
            if (component instanceof MenuSettingComponent && (settingComponent = (MenuSettingComponent)component).getOpacity() == 0.0f) continue;
            component.onMouseClicked(mouseX, mouseY, button);
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        for (CustomComponent component : this.components) {
            MenuSettingComponent settingComponent;
            if (component instanceof MenuSettingComponent && (settingComponent = (MenuSettingComponent)component).getOpacity() == 0.0f) continue;
            component.onMouseReleased(mouseX, mouseY, button);
        }
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CustomComponent component : this.components) {
            MenuSettingComponent settingComponent;
            if (component instanceof MenuSettingComponent && (settingComponent = (MenuSettingComponent)component).getOpacity() == 0.0f) continue;
            component.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        super.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        for (CustomComponent component : this.components) {
            MenuSettingComponent settingComponent;
            if (component instanceof MenuSettingComponent && (settingComponent = (MenuSettingComponent)component).getOpacity() == 0.0f) continue;
            component.onKeyPressed(keyCode, scanCode, modifiers);
        }
        super.onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (CustomComponent component : this.components) {
            MenuSettingComponent settingComponent;
            if (component instanceof MenuSettingComponent && (settingComponent = (MenuSettingComponent)component).getOpacity() == 0.0f) continue;
            component.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
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

