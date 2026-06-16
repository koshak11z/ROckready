package im.zov4ik.display.screens.clickgui;

import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.features.impl.render.Hud;

public final class ClickGuiTheme {
    public static final int PANEL_BG = 0xEE07090D;
    public static final int PANEL_BG_DARK = 0xF2030406;
    public static final int PANEL_HEADER_BG = 0xF007090D;
    public static final int PANEL_HEADER_BG_DARK = 0xF0030406;
    public static final int PANEL_OUTLINE = 0x70313A4A;
    public static final int PANEL_INNER = 0x30080D14;
    public static final int PANEL_INNER_DARK = 0x26060A11;
    public static final int PANEL_DIVIDER = 0x29465D89;
    public static final int ITEM_BG = 0xB7080B10;
    public static final int ITEM_BG_HOVER = 0xC90C1017;
    public static final int ITEM_BG_ACTIVE = 0xCA101420;
    public static final int ITEM_BG_ACTIVE_DARK = 0xC706090F;
    public static final int ITEM_OUTLINE = 0x3C303846;
    public static final int SETTINGS_BG = 0x22090D16;
    public static final int SETTINGS_BG_DARK = 0x1A060A11;
    public static final int SEPARATOR = 0x12000000;
    public static final int TEXT_PRIMARY = 0xFFF7FAFF;
    public static final int TEXT_SECONDARY = 0xFFE0E7F3;
    public static final int TEXT_MUTED = 0xFFB7C1D1;
    public static final int CONTROL_BG = 0xB80A0D14;
    public static final int CONTROL_BG_DARK = 0xB005070B;
    public static final int CONTROL_BG_ACTIVE = 0xC6111723;
    public static final int CONTROL_BG_ACTIVE_DARK = 0xC0080C14;
    public static final int CONTROL_OUTLINE = 0x8C384664;
    public static final int TOGGLE_BG = 0xFF0E1522;
    public static final int TOGGLE_KNOB_OFF = 0xFF9DA9BC;
    public static final int TOGGLE_KNOB_ON = 0xFFFDFEFF;
    public static final float CONTROL_WIDTH = 48F;
    public static final float CONTROL_HEIGHT = 11.0F;
    public static final float CONTROL_MARGIN = 3.0F;
    public static final float PANEL_RADIUS = 4.5F;
    public static final float ITEM_RADIUS = 3.8F;
    public static final float CONTROL_RADIUS = 3.0F;
    public static final float HEADER_HEIGHT = 21F;
    public static final float STATE_BADGE_SIZE = 8.8F;
    public static final float TOGGLE_WIDTH = 10.5F;
    public static final float TOGGLE_HEIGHT = 5.8F;
    public static final float TOGGLE_KNOB_SIZE = 3.8F;
    public static final int TITLE_FONT_SIZE = 14;
    public static final int MODULE_FONT_SIZE = 12;
    public static final int SETTING_FONT_SIZE = 10;
    public static final int CONTROL_FONT_SIZE = 9;

    private ClickGuiTheme() {
    }

    public static int accent() {
        try {
            return Hud.getInstance().colorSetting.getColor();
        } catch (Throwable ignored) {
            return 0xFF8A68FF;
        }
    }

    public static int accentSoft() {
        return ColorAssist.multAlpha(accent(), 0.30F);
    }

    public static int accentGlow() {
        return ColorAssist.multAlpha(accent(), 0.06F);
    }

    public static int accentOutline() {
        return ColorAssist.multAlpha(accent(), 0.82F);
    }

    public static int moduleEnabledFill() {
        return ITEM_BG_ACTIVE;
    }

    public static int moduleEnabledFillBottom() {
        return ITEM_BG_ACTIVE_DARK;
    }

    public static int moduleRowFill(boolean enabled, boolean hovered) {
        return enabled ? ITEM_BG_ACTIVE : (hovered ? ITEM_BG_HOVER : ITEM_BG);
    }

    public static int moduleRowFillBottom(boolean enabled, boolean hovered) {
        return enabled ? ITEM_BG_ACTIVE_DARK : (hovered ? ColorAssist.multDark(ITEM_BG_HOVER, 0.92F) : ColorAssist.multDark(ITEM_BG, 0.9F));
    }

    public static int moduleRowOutline(boolean enabled, boolean hovered) {
        if (enabled) {
            return ColorAssist.overCol(ITEM_OUTLINE, accentSoft(), 0.30F);
        }
        if (hovered) {
            return ColorAssist.overCol(ITEM_OUTLINE, CONTROL_OUTLINE, 0.68F);
        }
        return ITEM_OUTLINE;
    }

    public static int moduleEnabledText() {
        return TEXT_PRIMARY;
    }

    public static int moduleRowFill(float enabledProgress, boolean hovered) {
        int base = hovered ? ITEM_BG_HOVER : ITEM_BG;
        return ColorAssist.overCol(base, ITEM_BG_ACTIVE, enabledProgress);
    }

    public static int moduleRowFillBottom(float enabledProgress, boolean hovered) {
        int base = hovered ? ColorAssist.multDark(ITEM_BG_HOVER, 0.92F) : ColorAssist.multDark(ITEM_BG, 0.9F);
        return ColorAssist.overCol(base, ITEM_BG_ACTIVE_DARK, enabledProgress);
    }

    public static int moduleRowOutline(float enabledProgress, boolean hovered) {
        int base = hovered ? ColorAssist.overCol(ITEM_OUTLINE, CONTROL_OUTLINE, 0.68F) : ITEM_OUTLINE;
        int active = ColorAssist.overCol(ITEM_OUTLINE, accentSoft(), 0.30F);
        return ColorAssist.overCol(base, active, enabledProgress);
    }

    public static int moduleEnabledText(float enabledProgress) {
        return ColorAssist.overCol(TEXT_SECONDARY, TEXT_PRIMARY, enabledProgress);
    }

    public static int settingAccent() {
        return accent();
    }

    public static int categoryAccent(ModuleCategory category) {
        return switch (category) {
            case COMBAT -> 0xFF7591FF;
            case MOVEMENT -> 0xFF69DEFF;
            case RENDER -> 0xFF8164FF;
            case PLAYER -> 0xFFC47CFF;
            case MISC -> 0xFF9F88FF;
            case CONFIGS, AUTOBUY -> 0xFF8FA2C5;
        };
    }

    public static int categoryAccentSoft(ModuleCategory category) {
        return ColorAssist.multAlpha(categoryAccent(category), 0.22F);
    }

    public static String panelTitle(ModuleCategory category) {
        return switch (category) {
            case RENDER -> "Visuals";
            case MISC -> "Misc";
            default -> category.getReadableName();
        };
    }

    public static String categoryIcon(ModuleCategory category) {
        return switch (category) {
            case COMBAT -> "A";
            case MOVEMENT -> "B";
            case RENDER -> "C";
            case PLAYER -> "D";
            case MISC -> "E";
            case CONFIGS -> "F";
            case AUTOBUY -> "H";
        };
    }

    public static String iconPath(ModuleCategory category) {
        return switch (category) {
            case RENDER -> "textures/hudicons/visuals.png";
            case MISC -> "textures/hudicons/other.png";
            case CONFIGS, AUTOBUY -> "textures/hudicons/other.png";
            default -> "textures/hudicons/" + category.getReadableName().toLowerCase() + ".png";
        };
    }
}
