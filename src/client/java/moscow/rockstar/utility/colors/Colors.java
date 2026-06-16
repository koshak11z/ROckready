/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.utility.colors;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.systems.theme.ThemeManager;
import moscow.rockstar.utility.animation.types.ColorAnimation;
import moscow.rockstar.utility.colors.ColorRGBA;

public final class Colors {
    public static final ColorRGBA RED = new ColorRGBA(255.0f, 0.0f, 0.0f);
    public static final ColorRGBA GREEN = new ColorRGBA(0.0f, 255.0f, 0.0f);
    public static final ColorRGBA BLUE = new ColorRGBA(0.0f, 0.0f, 255.0f);
    public static final ColorRGBA WHITE = new ColorRGBA(255.0f, 255.0f, 255.0f);
    public static final ColorRGBA BLACK = new ColorRGBA(0.0f, 0.0f, 0.0f);
    public static final ColorRGBA ACCENT = new ColorRGBA(151.0f, 71.0f, 255.0f);
    private static final long ANIMATION_DURATION = 500L;
    private static final ColorAnimation ACCENT_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation BACKGROUND_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation ADDITIONAL_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation TEXT_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation GUI_TEXT_ACTIVE_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation GUI_TEXT_INACTIVE_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation HEADER_TEXT_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation LOGO_BACKGROUND_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation LOGO_TEXT_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation VISUALS_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation OUTLINE_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation FLAT_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation SLIDER_TRACK_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation SLIDER_CIRCLE_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation SLIDER_WINDOW_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation TOOLTIP_TEXT_COLOR_ANIMATION = new ColorAnimation(500L);

    private static ThemeManager getThemeManager() {
        return Rockstar.getInstance().getThemeManager();
    }

    private static Theme getTheme() {
        return Rockstar.getInstance().getThemeManager().getCurrentTheme();
    }

    public static ColorRGBA getAccentColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomAccentColor() : ACCENT;
        return Colors.getAnimatedColor(ACCENT_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getBackgroundColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomBackgroundColor() : Colors.getTheme().getBackgroundColor();
        return Colors.getAnimatedColor(BACKGROUND_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getAdditionalColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomAdditionalColor() : Colors.getTheme().getAdditionalColor();
        return Colors.getAnimatedColor(ADDITIONAL_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getTextColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomTextColor() : Colors.getTheme().getTextColor();
        return Colors.getAnimatedColor(TEXT_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getGuiTextActiveColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomGuiTextActiveColor() : Colors.getTheme().getTextColor();
        return Colors.getAnimatedColor(GUI_TEXT_ACTIVE_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getGuiTextInactiveColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomGuiTextInactiveColor() : Colors.getTheme().getTextColor().withAlpha(160.0f);
        return Colors.getAnimatedColor(GUI_TEXT_INACTIVE_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getHeaderTextColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomHeaderTextColor() : Colors.getTheme().getTextColor();
        return Colors.getAnimatedColor(HEADER_TEXT_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getLogoBackgroundColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomLogoBackgroundColor() : ACCENT;
        return Colors.getAnimatedColor(LOGO_BACKGROUND_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getLogoTextColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomLogoTextColor() : Colors.getTheme().getTextColor();
        return Colors.getAnimatedColor(LOGO_TEXT_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getTargetESPColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomTargetESPColor() : ACCENT;
        return Colors.getAnimatedColor(VISUALS_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getWorldColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomWorldColor() : ACCENT;
        return Colors.getAnimatedColor(VISUALS_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getOutlineColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomOutlineColor() : Colors.getTheme().getOutlineColor();
        return Colors.getAnimatedColor(OUTLINE_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getFlatColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomFlatColor() : Colors.getTheme().getFlatColor();
        return Colors.getAnimatedColor(FLAT_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getSliderTrackColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomSliderTrackColor() : ACCENT;
        return Colors.getAnimatedColor(SLIDER_TRACK_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getSliderCircleColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomSliderCircleColor() : Colors.WHITE;
        return Colors.getAnimatedColor(SLIDER_CIRCLE_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getSliderWindowColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomSliderWindowColor() : Colors.getAdditionalColor();
        return Colors.getAnimatedColor(SLIDER_WINDOW_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getTooltipTextColor() {
        ThemeManager themeManager = Colors.getThemeManager();
        ColorRGBA color = themeManager.isCustomTheme() ? themeManager.getCustomTooltipTextColor() : Colors.getTextColor();
        return Colors.getAnimatedColor(TOOLTIP_TEXT_COLOR_ANIMATION, color);
    }

    public static ColorRGBA getSeparatorColor() {
        return ColorRGBA.BLACK.withAlpha(255.0f * (Colors.getTheme() == Theme.DARK ? 0.08f : 0.05f));
    }

    private static ColorRGBA getAnimatedColor(ColorAnimation animation, ColorRGBA color) {
        animation.update(color);
        return animation.getColor();
    }

    @Generated
    private Colors() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

