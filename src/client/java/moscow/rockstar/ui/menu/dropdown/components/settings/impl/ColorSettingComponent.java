/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.ui.menu.modern.ModernScreen;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class ColorSettingComponent
extends MenuSettingComponent<ColorSetting> {
    private ColorPicker picker;

    public ColorSettingComponent(ColorSetting setting, CustomComponent parent) {
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
        float checkWidth = 13.0f;
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        float leftPadding = 10.0f;
        float headerHeight = 19.0f;
        context.drawFadeoutText(nameFont, Localizator.translate(((ColorSetting)this.setting).getName()), this.x + leftPadding, this.y + GuiUtility.getMiddleOfBox(nameFont.height(), headerHeight) - 0.5f, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())), 0.7f, 0.99f, this.width - checkWidth - 20.0f);
        context.drawRoundedRect(this.x + this.width - leftPadding - 8.0f, this.y + 5.0f, 8.0f, 8.0f, BorderRadius.all(4.5f), Colors.getOutlineColor());
        context.drawRoundedRect(this.x + this.width - leftPadding - 7.0f, this.y + 6.0f, 6.0f, 6.0f, BorderRadius.all(4.5f), ((ColorSetting)this.setting).getColor());
        if (this.picker != null) {
            ((ColorSetting)this.setting).color(this.picker.built());
        }
    }

    @Override
    public void drawSplit(UIContext context) {
        float separatorHeight = 0.5f;
        context.drawRect(this.x, this.y + this.height, this.width, separatorHeight, Colors.getTextColor().withAlpha(5.1f));
    }

    @Override
    @Compile
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.isHovered(mouseX, mouseY) && button == MouseButton.LEFT) {
            MenuScreen menuScreen = Rockstar.getInstance().getMenuScreen();
            if (menuScreen instanceof DropDownScreen) {
                DropDownScreen dropDownScreen = (DropDownScreen)menuScreen;
                this.picker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, ((ColorSetting)this.setting).isAlpha(), ((ColorSetting)this.setting).getColor(), Localizator.translate(((ColorSetting)this.setting).getName()));
                dropDownScreen.getColorPickers().add(this.picker);
            } else {
                menuScreen = Rockstar.getInstance().getMenuScreen();
                if (menuScreen instanceof ModernScreen) {
                    ModernScreen modernScreen = (ModernScreen)menuScreen;
                    this.picker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, ((ColorSetting)this.setting).isAlpha(), ((ColorSetting)this.setting).getColor(), Localizator.translate(((ColorSetting)this.setting).getName()));
                    modernScreen.getColorPickers().add(this.picker);
                }
            }
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public float getHeight() {
        this.height = 18.0f;
        return 18.0f;
    }
}

