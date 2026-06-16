/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.theme;

import lombok.Generated;
import moscow.rockstar.utility.colors.ColorRGBA;

public enum Theme {
    DARK(new ColorRGBA(255.0f, 255.0f, 255.0f), new ColorRGBA(12.0f, 12.0f, 12.0f), new ColorRGBA(24.0f, 24.0f, 27.0f), new ColorRGBA(32.0f, 32.0f, 32.0f), ColorRGBA.BLACK),
    LIGHT(new ColorRGBA(54.0f, 49.0f, 55.0f), new ColorRGBA(255.0f, 255.0f, 255.0f), new ColorRGBA(189.0f, 189.0f, 189.0f), new ColorRGBA(32.0f, 32.0f, 32.0f), ColorRGBA.WHITE);

    private final ColorRGBA textColor;
    private final ColorRGBA backgroundColor;
    private final ColorRGBA additionalColor;
    private final ColorRGBA outlineColor;
    private final ColorRGBA flatColor;

    @Generated
    public ColorRGBA getTextColor() {
        return this.textColor;
    }

    @Generated
    public ColorRGBA getBackgroundColor() {
        return this.backgroundColor;
    }

    @Generated
    public ColorRGBA getAdditionalColor() {
        return this.additionalColor;
    }

    @Generated
    public ColorRGBA getOutlineColor() {
        return this.outlineColor;
    }

    @Generated
    public ColorRGBA getFlatColor() {
        return this.flatColor;
    }

    @Generated
    private Theme(ColorRGBA textColor, ColorRGBA backgroundColor, ColorRGBA additionalColor, ColorRGBA outlineColor, ColorRGBA flatColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.additionalColor = additionalColor;
        this.outlineColor = outlineColor;
        this.flatColor = flatColor;
    }
}

