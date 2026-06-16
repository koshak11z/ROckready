/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import lombok.Generated;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.settings.BindSetting;
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
import moscow.rockstar.utility.render.ScissorUtility;

public class BindSettingComponent
extends MenuSettingComponent<BindSetting> {
    private final ColorAnimation bindColorAnimation = new ColorAnimation(300L, new ColorRGBA(24.0f, 24.0f, 27.0f), Easing.FIGMA_EASE_IN_OUT);
    private final Animation widthAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private Animation changeAnimation = new Animation(300L, 1.0f, Easing.FIGMA_EASE_IN_OUT);
    private int prevKey;
    private boolean bindingMode;

    public BindSettingComponent(BindSetting setting, CustomComponent parent) {
        super(setting, parent);
    }

    @Override
    protected void renderComponent(UIContext context) {
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        Font keyFont = Fonts.REGULAR.getFont(7.0f);
        float leftPadding = 10.0f;
        float headerHeight = 19.0f;
        this.bindColorAnimation.update(this.bindingMode ? Colors.ACCENT : Colors.getTextColor());
        this.changeAnimation.setDuration(500L);
        this.changeAnimation.update(1.0f);
        String key = TextUtility.getKeyName(((BindSetting)this.setting).getKey());
        String prev = TextUtility.getKeyName(this.prevKey);
        float keyWidth = keyFont.width(key) + 7.0f;
        this.widthAnimation.update(keyWidth);
        context.drawRoundedRect(this.x + this.width - 9.0f - this.widthAnimation.getValue(), this.y + 4.0f, this.widthAnimation.getValue(), 11.0f, BorderRadius.all(3.0f), Colors.getAdditionalColor());
        ScissorUtility.push(context.getMatrices(), this.x + this.width - 9.0f - this.widthAnimation.getValue(), this.y + 4.0f, this.widthAnimation.getValue(), 11.0f);
        context.drawText(keyFont, prev, this.x + this.width - 9.0f - this.widthAnimation.getValue() + 4.0f + 4.0f * this.changeAnimation.getValue(), this.y + 7.0f, this.bindColorAnimation.getColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue()) * (1.0f - this.changeAnimation.getValue())));
        context.drawText(keyFont, key, this.x + this.width - 9.0f - this.widthAnimation.getValue() + 4.0f - 4.0f + 4.0f * this.changeAnimation.getValue(), this.y + 7.0f, this.bindColorAnimation.getColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue()) * this.changeAnimation.getValue()));
        ScissorUtility.pop();
        context.drawFadeoutText(nameFont, Localizator.translate(((BindSetting)this.setting).getName()), this.x + leftPadding, this.y + GuiUtility.getMiddleOfBox(nameFont.height(), headerHeight), Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())), 0.7f, 0.99f, this.width - this.widthAnimation.getValue() - 20.0f);
        if (this.isHovered(context)) {
            CursorUtility.set(CursorType.HAND);
        }
    }

    @Override
    public void drawSplit(UIContext context) {
        float separatorHeight = 0.5f;
        context.drawRect(this.x, this.y + this.height, this.width, separatorHeight, Colors.getTextColor().withAlpha(5.1f));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.isHovered(mouseX, mouseY) && button == MouseButton.LEFT) {
            boolean bl = this.bindingMode = !this.bindingMode;
        }
        if (this.bindingMode && button != MouseButton.LEFT) {
            int buttonIndex = button.getButtonIndex();
            ((BindSetting)this.setting).setKey(buttonIndex);
            this.bindingMode = false;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.bindingMode) {
            this.prevKey = ((BindSetting)this.setting).getKey();
            if (keyCode == 256 || keyCode == 261) {
                ((BindSetting)this.setting).setKey(-1);
            } else {
                ((BindSetting)this.setting).setKey(keyCode);
            }
            this.changeAnimation = new Animation(500L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
            this.bindingMode = false;
            return;
        }
        super.onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public float getHeight() {
        this.height = 19.0f;
        return 19.0f;
    }

    @Generated
    public void setBindingMode(boolean bindingMode) {
        this.bindingMode = bindingMode;
    }
}

