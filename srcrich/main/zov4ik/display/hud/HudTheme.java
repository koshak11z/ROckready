package im.zov4ik.display.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.geometry.Render2D;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.utils.display.shape.ShapeProperties;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.*;

public final class HudTheme {
    public static final int TEXT = RefRender.TEXT;
    public static final int TEXT_DIM = RefRender.TEXT_DIM;
    public static final int TEXT_MUTED = RefRender.TEXT_MUTED;
    public static final int MUTED_TEXT = TEXT_DIM;
    public static final int PANEL_TOP = RefRender.PANEL_SOFT;
    public static final int PANEL_BOTTOM = RefRender.PANEL;
    public static final int PANEL_BORDER = RefRender.OUTLINE;
    public static final int LINE = RefRender.LINE;
    public static final int ACCENT = RefRender.ACCENT;
    public static final int ACCENT_SOFT = new Color(138, 104, 255, 85).getRGB();
    public static final int DANGER = RefRender.DANGER;
    public static final int GOOD = RefRender.GOOD;
    public static final int WARN = RefRender.WARN;

    public static final float PANEL_RADIUS = 4.0F;
    public static final float SMALL_RADIUS = 3.0F;

    public static final Identifier ICON_USER = iconId("user");
    public static final Identifier ICON_HEART = iconId("heart");
    public static final Identifier ICON_SHIELD = iconId("shield");
    public static final Identifier ICON_SWORD = iconId("sword");
    public static final Identifier ICON_HAND = iconId("hand");
    public static final Identifier ICON_ORBIT = iconId("orbit");
    public static final Identifier ICON_BELL = iconId("bell");
    public static final Identifier ICON_TARGET = iconId("target");
    public static final Identifier ICON_FLASK = iconId("flask");
    public static final Identifier ICON_MAP_PIN = iconId("map_pin");
    public static final Identifier ICON_KEYBOARD = iconId("keyboard");
    public static final Identifier ICON_CLOCK = iconId("clock");
    public static final Identifier ICON_WIFI = iconId("wifi");
    public static final Identifier ICON_SERVER = iconId("server");
    public static final Identifier ICON_MONITOR = iconId("monitor");
    public static final Identifier ICON_EYE = iconId("eye");
    public static final Identifier ICON_SETTINGS = iconId("settings");
    public static final Identifier ICON_ZAP = iconId("zap");
    public static final Identifier ICON_CROSSHAIR = iconId("crosshair");
    public static final Identifier ICON_DIAMOND = iconId("diamond");
    public static final Identifier ICON_ACTIVITY = iconId("activity");
    public static final Identifier ICON_PACKAGE = iconId("package");
    public static final Identifier ICON_CHEVRON_UP = iconId("chevron_up");

    private HudTheme() {
    }

    private static Identifier iconId(String name) {
        return Identifier.of("textures/zovui/icons/" + name + ".png");
    }

    public static int text(float alpha) {
        return ColorAssist.multAlpha(TEXT, alpha);
    }

    public static int muted(float alpha) {
        return ColorAssist.multAlpha(TEXT_DIM, alpha);
    }

    public static int accent(float alpha) {
        return ColorAssist.multAlpha(ACCENT, alpha);
    }

    public static void panel(MatrixStack matrix, float x, float y, float width, float height, float radius) {
        panel(matrix, x, y, width, height, radius, 1.0F);
    }

    public static void panel(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha) {
        RefRender.panel(matrix, x, y, width, height, radius, alpha);
    }

    public static void dangerPanel(MatrixStack matrix, float x, float y, float width, float height, float radius) {
        panel(matrix, x, y, width, height, radius);
        QuickImports.rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(radius)
                .color(ColorAssist.rgba(88, 8, 18, 72),
                        ColorAssist.rgba(66, 5, 13, 70),
                        ColorAssist.rgba(20, 2, 7, 64),
                        ColorAssist.rgba(38, 3, 10, 66))
                .build());
    }

    public static void hairline(MatrixStack matrix, float x, float y, float width) {
        RefRender.line(matrix, x, y, width);
    }

    public static void track(MatrixStack matrix, float x, float y, float width, float height) {
        RefRender.track(matrix, x, y, width, height);
    }

    public static void accentBar(MatrixStack matrix, float x, float y, float width, float height, float progress) {
        RefRender.progress(matrix, x, y, width, height, progress, ACCENT);
    }

    public static void dot(MatrixStack matrix, float centerX, float centerY, float size, int color) {
        float radius = size / 2.0F;
        QuickImports.rectangle.render(ShapeProperties.create(matrix, centerX - radius - 1.2F, centerY - radius - 1.2F, size + 2.4F, size + 2.4F)
                .round(radius + 1.0F)
                .softness(4.0F)
                .color(ColorAssist.multAlpha(color, 0.42F))
                .build());
        QuickImports.rectangle.render(ShapeProperties.create(matrix, centerX - radius, centerY - radius, size, size)
                .round(radius)
                .color(color)
                .build());
    }

    public static void icon(DrawContext context, Identifier id, float x, float y, float size, int color) {
        RefRender.icon(context, id, x, y, size, color);
    }

    public static void iconSlot(DrawContext context, Identifier id, float x, float y, float slot, float size, int color) {
        RefRender.iconSlot(context, id, x, y, slot, size, color);
    }

    public static void iconBox(DrawContext context, Identifier id, float x, float y, float size, int color) {
        RefRender.iconSlot(context, id, x, y, size, Math.max(1.0F, size - 5.0F), color);
    }

    public static void screenShade(MatrixStack matrix, float width, float height) {
    }
}
