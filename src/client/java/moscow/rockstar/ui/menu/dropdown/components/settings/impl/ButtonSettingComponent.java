/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.settings.ButtonSetting;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;

public class ButtonSettingComponent
extends MenuSettingComponent<ButtonSetting> {
    public ButtonSettingComponent(ButtonSetting setting, CustomComponent parent) {
        super(setting, parent);
    }

    @Override
    public void onInit() {
        this.width = 13.0f;
        this.height = 8.0f;
        super.onInit();
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        if (this.isHovered(context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        context.drawRoundedRect(this.x + 7.0f, this.y + 4.0f, this.width - 14.0f, this.height - 7.0f, BorderRadius.all(6.0f), Colors.getBackgroundColor().mulAlpha(0.3f + 0.2f * this.hoverAnimation.getValue()));
        context.drawCenteredText(nameFont, Localizator.translate(((ButtonSetting)this.setting).getName()), this.x + this.width / 2.0f, this.y + GuiUtility.getMiddleOfBox(nameFont.height(), this.height) - 0.5f, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())));
    }

    @Override
    public void drawRegular8(UIContext context) {
    }

    @Override
    public void drawSplit(UIContext context) {
        float separatorHeight = 0.5f;
        context.drawRect(this.x, this.y + this.height, this.width, separatorHeight, Colors.getTextColor().withAlpha(5.1f));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.isHovered(mouseX, mouseY) && button == MouseButton.LEFT) {
            ((ButtonSetting)this.setting).getAction().run();
        }
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public float getHeight() {
        this.height = 24.0f;
        return 24.0f;
    }
}

