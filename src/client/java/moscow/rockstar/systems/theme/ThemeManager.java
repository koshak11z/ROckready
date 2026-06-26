/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.theme;

import lombok.Generated;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.colors.ColorRGBA;

public class ThemeManager {
    private Theme currentTheme = Theme.DARK;
    private ColorRGBA customAccentColor = new ColorRGBA(151.0f, 71.0f, 255.0f);
    private ColorRGBA customTextColor = Theme.DARK.getTextColor();
    private ColorRGBA customGuiTextActiveColor = Theme.DARK.getTextColor();
    private ColorRGBA customGuiTextInactiveColor = Theme.DARK.getTextColor().withAlpha(160.0f);
    private ColorRGBA customHeaderTextColor = Theme.DARK.getTextColor();
    private ColorRGBA customLogoBackgroundColor = new ColorRGBA(151.0f, 71.0f, 255.0f);
    private ColorRGBA customLogoTextColor = Theme.DARK.getTextColor();
    private ColorRGBA customVisualsColor = new ColorRGBA(151.0f, 71.0f, 255.0f);
    private ColorRGBA customBackgroundColor = Theme.DARK.getBackgroundColor();
    private ColorRGBA customAdditionalColor = Theme.DARK.getAdditionalColor();
    private ColorRGBA customOutlineColor = Theme.DARK.getOutlineColor();
    private ColorRGBA customFlatColor = Theme.DARK.getFlatColor();
    private ColorRGBA customSliderTrackColor = new ColorRGBA(151.0f, 71.0f, 255.0f);
    private ColorRGBA customSliderCircleColor = new ColorRGBA(255.0f, 255.0f, 255.0f);
    private ColorRGBA customSliderWindowColor = Theme.DARK.getAdditionalColor();
    private ColorRGBA customTooltipTextColor = Theme.DARK.getTextColor();

    public void switchTheme() {
        this.currentTheme = this.currentTheme == Theme.DARK ? Theme.LIGHT : Theme.DARK;
    }

    /** Сброс темы к стандартной (тёмная + фиолетовый акцент) — используется в {@code .cfg reset}. */
    public void reset() {
        this.currentTheme = Theme.DARK;
        this.customAccentColor = new ColorRGBA(151.0f, 71.0f, 255.0f);
        this.customTextColor = Theme.DARK.getTextColor();
        this.customGuiTextActiveColor = Theme.DARK.getTextColor();
        this.customGuiTextInactiveColor = Theme.DARK.getTextColor().withAlpha(160.0f);
        this.customHeaderTextColor = Theme.DARK.getTextColor();
        this.customLogoBackgroundColor = new ColorRGBA(151.0f, 71.0f, 255.0f);
        this.customLogoTextColor = Theme.DARK.getTextColor();
        this.customVisualsColor = new ColorRGBA(151.0f, 71.0f, 255.0f);
        this.customBackgroundColor = Theme.DARK.getBackgroundColor();
        this.customAdditionalColor = Theme.DARK.getAdditionalColor();
        this.customOutlineColor = Theme.DARK.getOutlineColor();
        this.customFlatColor = Theme.DARK.getFlatColor();
        this.customSliderTrackColor = new ColorRGBA(151.0f, 71.0f, 255.0f);
        this.customSliderCircleColor = new ColorRGBA(255.0f, 255.0f, 255.0f);
        this.customSliderWindowColor = Theme.DARK.getAdditionalColor();
        this.customTooltipTextColor = Theme.DARK.getTextColor();
    }

    public Theme getCurrentTheme() {
        if (Interface.glassSelected()) {
            return Theme.DARK;
        }
        return this.currentTheme;
    }

    public boolean isCustomTheme() {
        return Interface.customSelected();
    }

    @Generated
    public ColorRGBA getCustomAccentColor() {
        return this.customAccentColor;
    }

    @Generated
    public ColorRGBA getCustomTextColor() {
        return this.customTextColor;
    }

    @Generated
    public ColorRGBA getCustomGuiTextActiveColor() {
        return this.customGuiTextActiveColor;
    }

    @Generated
    public ColorRGBA getCustomGuiTextInactiveColor() {
        return this.customGuiTextInactiveColor;
    }

    @Generated
    public ColorRGBA getCustomHeaderTextColor() {
        return this.customHeaderTextColor;
    }

    @Generated
    public ColorRGBA getCustomLogoBackgroundColor() {
        return this.customLogoBackgroundColor;
    }

    @Generated
    public ColorRGBA getCustomLogoTextColor() {
        return this.customLogoTextColor;
    }

    @Generated
    public ColorRGBA getCustomTargetESPColor() {
        return this.customVisualsColor;
    }

    @Generated
    public ColorRGBA getCustomWorldColor() {
        return this.customVisualsColor;
    }

    @Generated
    public ColorRGBA getCustomBackgroundColor() {
        return this.customBackgroundColor;
    }

    @Generated
    public ColorRGBA getCustomAdditionalColor() {
        return this.customAdditionalColor;
    }

    @Generated
    public ColorRGBA getCustomOutlineColor() {
        return this.customOutlineColor;
    }

    @Generated
    public ColorRGBA getCustomFlatColor() {
        return this.customFlatColor;
    }

    @Generated
    public ColorRGBA getCustomSliderTrackColor() {
        return this.customSliderTrackColor;
    }

    @Generated
    public ColorRGBA getCustomSliderCircleColor() {
        return this.customSliderCircleColor;
    }

    @Generated
    public ColorRGBA getCustomSliderWindowColor() {
        return this.customSliderWindowColor;
    }

    @Generated
    public ColorRGBA getCustomTooltipTextColor() {
        return this.customTooltipTextColor;
    }

    @Generated
    public void setCustomAccentColor(ColorRGBA customAccentColor) {
        this.customAccentColor = customAccentColor;
    }

    @Generated
    public void setCustomTextColor(ColorRGBA customTextColor) {
        this.customTextColor = customTextColor;
    }

    @Generated
    public void setCustomGuiTextActiveColor(ColorRGBA customGuiTextActiveColor) {
        this.customGuiTextActiveColor = customGuiTextActiveColor;
    }

    @Generated
    public void setCustomGuiTextInactiveColor(ColorRGBA customGuiTextInactiveColor) {
        this.customGuiTextInactiveColor = customGuiTextInactiveColor;
    }

    @Generated
    public void setCustomHeaderTextColor(ColorRGBA customHeaderTextColor) {
        this.customHeaderTextColor = customHeaderTextColor;
    }

    @Generated
    public void setCustomLogoBackgroundColor(ColorRGBA customLogoBackgroundColor) {
        this.customLogoBackgroundColor = customLogoBackgroundColor;
    }

    @Generated
    public void setCustomLogoTextColor(ColorRGBA customLogoTextColor) {
        this.customLogoTextColor = customLogoTextColor;
    }

    @Generated
    public void setCustomTargetESPColor(ColorRGBA customTargetESPColor) {
        this.customVisualsColor = customTargetESPColor;
    }

    @Generated
    public void setCustomWorldColor(ColorRGBA customWorldColor) {
        this.customVisualsColor = customWorldColor;
    }

    @Generated
    public void setCustomBackgroundColor(ColorRGBA customBackgroundColor) {
        this.customBackgroundColor = customBackgroundColor;
    }

    @Generated
    public void setCustomAdditionalColor(ColorRGBA customAdditionalColor) {
        this.customAdditionalColor = customAdditionalColor;
    }

    @Generated
    public void setCustomOutlineColor(ColorRGBA customOutlineColor) {
        this.customOutlineColor = customOutlineColor;
    }

    @Generated
    public void setCustomFlatColor(ColorRGBA customFlatColor) {
        this.customFlatColor = customFlatColor;
    }

    @Generated
    public void setCustomSliderTrackColor(ColorRGBA customSliderTrackColor) {
        this.customSliderTrackColor = customSliderTrackColor;
    }

    @Generated
    public void setCustomSliderCircleColor(ColorRGBA customSliderCircleColor) {
        this.customSliderCircleColor = customSliderCircleColor;
    }

    @Generated
    public void setCustomSliderWindowColor(ColorRGBA customSliderWindowColor) {
        this.customSliderWindowColor = customSliderWindowColor;
    }

    @Generated
    public void setCustomTooltipTextColor(ColorRGBA customTooltipTextColor) {
        this.customTooltipTextColor = customTooltipTextColor;
    }

    @Generated
    public void setCurrentTheme(Theme currentTheme) {
        this.currentTheme = currentTheme;
    }

    /** Derive a full dark custom theme from a single base/accent color. */
    public void applyAutoTheme(ColorRGBA base) {
        float h = base.getHue();
        float s = base.getSaturation();
        ColorRGBA bg = ColorRGBA.fromHSB(h, Math.min(s, 0.6f) * 0.5f, 0.07f);
        ColorRGBA add = ColorRGBA.fromHSB(h, Math.min(s, 0.6f) * 0.4f, 0.12f);
        ColorRGBA text = ColorRGBA.fromHSB(h, 0.04f, 0.95f);
        ColorRGBA white = new ColorRGBA(255.0f, 255.0f, 255.0f);
        this.setCustomAccentColor(base);
        this.setCustomBackgroundColor(bg);
        this.setCustomAdditionalColor(add);
        this.setCustomOutlineColor(ColorRGBA.fromHSB(h, 0.3f, 0.22f));
        this.setCustomFlatColor(add);
        this.setCustomTextColor(text);
        this.setCustomGuiTextActiveColor(text);
        this.setCustomGuiTextInactiveColor(text.withAlpha(160.0f));
        this.setCustomHeaderTextColor(text);
        this.setCustomTooltipTextColor(text);
        this.setCustomLogoBackgroundColor(base);
        this.setCustomLogoTextColor(white);
        this.setCustomTargetESPColor(base);
        this.setCustomWorldColor(base);
        this.setCustomSliderTrackColor(base);
        this.setCustomSliderCircleColor(white);
        this.setCustomSliderWindowColor(add);
    }
}

