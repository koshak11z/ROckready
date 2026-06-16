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
import moscow.rockstar.systems.setting.settings.StringSetting;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;

public class StringSettingComponent
extends MenuSettingComponent<StringSetting> {
    private TextField textField;

    public StringSettingComponent(StringSetting setting, CustomComponent parent) {
        super(setting, parent);
    }

    @Override
    public void onInit() {
        this.width = 13.0f;
        this.height = 8.0f;
        this.textField = new TextField(Fonts.REGULAR.getFont(8.0f));
        this.textField.paste(((StringSetting)this.setting).getText());
        this.textField.setPreview(Localizator.translate("type_text"));
        super.onInit();
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        float x = this.x + 8.0f;
        float y = this.y + 15.0f;
        float width = this.width - 16.0f;
        float height = this.height - 20.0f;
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        if (this.isHovered(context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
        float checkWidth = 13.0f;
        float checkHeight = 8.0f;
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        float leftPadding = 10.0f;
        float nameHeight = nameFont.height();
        float headerHeight = 19.0f;
        context.drawFadeoutText(nameFont, Localizator.translate(((StringSetting)this.setting).getName()), this.x + leftPadding, this.y + GuiUtility.getMiddleOfBox(nameFont.height(), headerHeight) - 0.5f, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())), 0.7f, 0.99f, width - checkWidth - 20.0f);
        context.drawRoundedRect(x, y, width, height, BorderRadius.all(4.0f), Colors.getBackgroundColor().mulAlpha(0.3f));
        this.textField.set(x, y, width, height);
        this.textField.setAlpha(1.0f);
        this.textField.setTextColor(Colors.getTextColor());
        this.textField.render(context);
        ((StringSetting)this.setting).text(this.textField.getBuiltText());
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
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        this.textField.onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.textField.charTyped(chr, modifiers);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        this.textField.onMouseClicked(mouseX, mouseY, button);
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.textField.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public float getHeight() {
        this.height = 35.0f;
        return 35.0f;
    }
}

