/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.util.math.Vector2f
 */
package moscow.rockstar.utility.gui;

import lombok.Generated;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.setting.settings.BezierSetting;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ButtonSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.RangeSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.setting.settings.StringSetting;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BezierSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BindSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BooleanSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ButtonSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ColorSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ModeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.RangeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.SelectSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.SliderSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.StringSettingComponent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.obj.Rect;
import net.minecraft.client.util.math.Vector2f;

public final class GuiUtility {
    public static float getMiddleOfBox(float objectHeight, float boxHeight) {
        return (float)Math.ceil(boxHeight / 2.0f - objectHeight / 2.0f);
    }

    public static double getMiddleOfBox(double objectHeight, double boxHeight) {
        return Math.ceil(boxHeight / 2.0 - objectHeight / 2.0);
    }

    public static boolean isHovered(double x, double y, double width, double height, int mouseX, int mouseY) {
        return (double)mouseX >= x && (double)mouseX < x + width && (double)mouseY >= y && (double)mouseY < y + height;
    }

    public static boolean isHovered(double x, double y, double width, double height, UIContext context) {
        return GuiUtility.isHovered(x, y, width, height, context.getMouseX(), context.getMouseY());
    }

    public static boolean isHovered(Rect rect, double mouseX, double mouseY) {
        return GuiUtility.isHovered((double)rect.getX(), (double)rect.getY(), (double)rect.getWidth(), (double)rect.getHeight(), mouseX, mouseY);
    }

    public static boolean isHovered(CustomComponent rect, double mouseX, double mouseY) {
        return GuiUtility.isHovered((double)rect.getX(), (double)rect.getY(), (double)rect.getWidth(), (double)rect.getHeight(), mouseX, mouseY);
    }

    public static boolean isHovered(double x, double y, double width, double height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static float getSliderValue(float min, float max, float start, float size, double mouse) {
        return (float)(Math.min(1.0, Math.max(0.0, (mouse - (double)start) / (double)size)) * (double)(max - min)) + min;
    }

    public static float getSliderValueWithoutClamp(float min, float max, float start, float size, double mouse) {
        return (float)((mouse - (double)start) / (double)size * (double)(max - min)) + min;
    }

    public static float getPercent(float value, float min, float max) {
        return (value - min) / (max - min);
    }

    public static Vector2f getMouse() {
        return new Vector2f((float)(IMinecraft.mc.mouse.getX() / IScaledResolution.sr.getScaleFactor()), (float)(IMinecraft.mc.mouse.getY() / IScaledResolution.sr.getScaleFactor()));
    }

    public static MenuSettingComponent settinge(Setting setting, CustomComponent parent) {
        MenuSettingComponent settingComponent = null;
        if (setting instanceof BooleanSetting) {
            BooleanSetting s = (BooleanSetting)setting;
            settingComponent = new BooleanSettingComponent(s, parent);
        } else if (setting instanceof BindSetting) {
            BindSetting s = (BindSetting)setting;
            settingComponent = new BindSettingComponent(s, parent);
        } else if (setting instanceof ColorSetting) {
            ColorSetting s = (ColorSetting)setting;
            settingComponent = new ColorSettingComponent(s, parent);
        } else if (setting instanceof ModeSetting) {
            ModeSetting s = (ModeSetting)setting;
            settingComponent = new ModeSettingComponent(s, parent);
        } else if (setting instanceof RangeSetting) {
            RangeSetting s = (RangeSetting)setting;
            settingComponent = new RangeSettingComponent(s, parent);
        } else if (setting instanceof BezierSetting) {
            BezierSetting s = (BezierSetting)setting;
            settingComponent = new BezierSettingComponent(s, parent);
        } else if (setting instanceof ButtonSetting) {
            ButtonSetting s = (ButtonSetting)setting;
            settingComponent = new ButtonSettingComponent(s, parent);
        } else if (setting instanceof SelectSetting) {
            SelectSetting s = (SelectSetting)setting;
            settingComponent = new SelectSettingComponent(s, parent);
        } else if (setting instanceof SliderSetting) {
            SliderSetting s = (SliderSetting)setting;
            settingComponent = new SliderSettingComponent(s, parent);
        } else if (setting instanceof StringSetting) {
            StringSetting s = (StringSetting)setting;
            settingComponent = new StringSettingComponent(s, parent);
        }
        if (settingComponent != null) {
            settingComponent.onInit();
        }
        return settingComponent;
    }

    @Generated
    private GuiUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

