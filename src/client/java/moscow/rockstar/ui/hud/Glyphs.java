package moscow.rockstar.ui.hud;

import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.render.RenderUtility;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

/**
 * Procedural vector mini-icons + shared style constants for the redesigned HUD.
 *
 * <p>The reference uses thin Tabler/Feather-style line icons that do not ship as PNG assets, so
 * they are drawn here from the {@link CustomDrawContext} primitives (rounded rects, borders, lines).
 * Everything is drawn inside a {@code size x size} box anchored at {@code (x, y)} top-left so callers
 * can lay them out like any other glyph.
 */
public final class Glyphs {
    /** Near-black panel fill used by every redesigned HUD element (the "full black" look). */
    public static ColorRGBA panel(float alpha) {
        return new ColorRGBA(9.0f, 9.0f, 13.0f, alpha);
    }

    /** Slightly lighter surface used for inner chips / inactive bars. */
    public static ColorRGBA surface(float alpha) {
        return new ColorRGBA(26.0f, 26.0f, 32.0f, alpha);
    }

    public static ColorRGBA divider(float alpha) {
        return new ColorRGBA(255.0f, 255.0f, 255.0f, alpha * 0.06f);
    }

    private Glyphs() {
    }

    // ───────────────────────────── basic shapes ─────────────────────────────

    private static void line(CustomDrawContext c, float x1, float y1, float x2, float y2, ColorRGBA color) {
        c.drawLine(new Vec2f(x1, y1), new Vec2f(x2, y2), color);
    }

    private static void dot(CustomDrawContext c, float cx, float cy, float r, ColorRGBA color) {
        c.drawRoundedRect(cx - r, cy - r, r * 2.0f, r * 2.0f, BorderRadius.all(r), color);
    }

    private static void ringOutline(CustomDrawContext c, float cx, float cy, float r, float thickness, ColorRGBA color) {
        c.drawRoundedBorder(cx - r, cy - r, r * 2.0f, r * 2.0f, thickness, BorderRadius.all(r), color);
    }

    // ───────────────────────────── icons ─────────────────────────────

    /** Diamond / rhombus brand logo (the ◇ in the reference watermark). */
    public static void diamond(CustomDrawContext c, float x, float y, float s, ColorRGBA color) {
        float cx = x + s / 2.0f;
        float cy = y + s / 2.0f;
        float side = s * 0.62f;
        RenderUtility.rotate(c.getMatrices(), cx, cy, 45.0f);
        c.drawRoundedRect(cx - side / 2.0f, cy - side / 2.0f, side, side, BorderRadius.all(s * 0.16f), color);
        RenderUtility.end(c.getMatrices());
    }

    /** Simple person silhouette (head + shoulders). */
    public static void person(CustomDrawContext c, float x, float y, float s, ColorRGBA color) {
        float cx = x + s / 2.0f;
        float headR = s * 0.20f;
        dot(c, cx, y + headR + s * 0.04f, headR, color);
        float bw = s * 0.62f;
        float bh = s * 0.42f;
        c.drawRoundedRect(cx - bw / 2.0f, y + s - bh, bw, bh, new BorderRadius(bw / 2.0f, bw / 2.0f, 0.0f, 0.0f), color);
    }

    /** Wifi/ping signal bars (left-to-right ascending). {@code filled} of 4 use {@code color}, rest dim. */
    public static void wifi(CustomDrawContext c, float x, float y, float s, int filled, ColorRGBA color) {
        float bw = s * 0.18f;
        float gap = s * 0.10f;
        for (int i = 0; i < 4; i++) {
            float bh = s * (0.30f + 0.22f * i);
            float bx = x + i * (bw + gap);
            float by = y + s - bh;
            c.drawRoundedRect(bx, by, bw, bh, BorderRadius.all(bw * 0.5f), i < filled ? color : color.mulAlpha(0.25f));
        }
    }

    /** Clock face with two hands. */
    public static void clock(CustomDrawContext c, float x, float y, float s, ColorRGBA color) {
        float cx = x + s / 2.0f;
        float cy = y + s / 2.0f;
        float r = s * 0.46f;
        ringOutline(c, cx, cy, r, Math.max(0.7f, s * 0.10f), color);
        line(c, cx, cy, cx, cy - r * 0.55f, color);
        line(c, cx, cy, cx + r * 0.45f, cy, color);
    }

    /** Small "performance" chip (monitor outline + base) used for the FPS segment. */
    public static void gauge(CustomDrawContext c, float x, float y, float s, ColorRGBA color) {
        float cx = x + s / 2.0f;
        float cy = y + s / 2.0f;
        float r = s * 0.46f;
        ringOutline(c, cx, cy, r, Math.max(0.7f, s * 0.10f), color);
        // needle pointing up-right like a speedometer
        line(c, cx, cy, cx + r * 0.55f, cy - r * 0.45f, color);
        dot(c, cx, cy, s * 0.07f, color);
    }

    /** Cloud / server glyph (three lobes + base). */
    public static void cloud(CustomDrawContext c, float x, float y, float s, ColorRGBA color) {
        float baseY = y + s * 0.66f;
        dot(c, x + s * 0.34f, baseY - s * 0.06f, s * 0.20f, color);
        dot(c, x + s * 0.62f, baseY - s * 0.12f, s * 0.24f, color);
        c.drawRoundedRect(x + s * 0.16f, baseY, s * 0.66f, s * 0.20f, BorderRadius.all(s * 0.10f), color);
    }

    /** Location pin (teardrop) used for the coordinates element. */
    public static void pin(CustomDrawContext c, float x, float y, float s, ColorRGBA color) {
        float cx = x + s / 2.0f;
        float topCy = y + s * 0.36f;
        float r = s * 0.30f;
        ringOutline(c, cx, topCy, r, Math.max(0.8f, s * 0.12f), color);
        dot(c, cx, topCy, s * 0.11f, color);
        // tip
        RenderUtility.rotate(c.getMatrices(), cx, y + s * 0.78f, 45.0f);
        float t = s * 0.22f;
        c.drawRoundedRect(cx - t / 2.0f, y + s * 0.78f - t / 2.0f, t, t, BorderRadius.all(s * 0.05f), color);
        RenderUtility.end(c.getMatrices());
    }

    /** Pencil / edit glyph for the Potions header. */
    public static void pencil(CustomDrawContext c, float x, float y, float s, ColorRGBA color) {
        float cx = x + s / 2.0f;
        float cy = y + s / 2.0f;
        RenderUtility.rotate(c.getMatrices(), cx, cy, 45.0f);
        float w = s * 0.30f;
        float h = s * 0.74f;
        c.drawRoundedRect(cx - w / 2.0f, cy - h / 2.0f, w, h, BorderRadius.all(s * 0.06f), color);
        // tip
        c.drawRoundedRect(cx - w / 2.0f, cy + h / 2.0f - s * 0.12f, w, s * 0.12f, BorderRadius.all(s * 0.04f), color.mulAlpha(0.7f));
        RenderUtility.end(c.getMatrices());
    }

    /** Thick rounded line segment between two points (a capsule). */
    public static void thickLine(CustomDrawContext c, float x1, float y1, float x2, float y2, float thick, ColorRGBA color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.01f) {
            return;
        }
        float ang = (float) (Math.atan2(dy, dx) * 180.0 / Math.PI);
        float mx = (x1 + x2) / 2.0f;
        float my = (y1 + y2) / 2.0f;
        RenderUtility.rotate(c.getMatrices(), mx, my, ang);
        c.drawRoundedRect(mx - len / 2.0f - thick / 2.0f, my - thick / 2.0f, len + thick, thick, BorderRadius.all(thick / 2.0f), color);
        RenderUtility.end(c.getMatrices());
    }

    /** Stylized brand "Z" logo (top bar + diagonal + bottom bar of rounded strokes). */
    public static void zLogo(CustomDrawContext c, float x, float y, float s, ColorRGBA color) {
        float t = Math.max(1.2f, s * 0.17f);
        float l = x + s * 0.17f;
        float r = x + s * 0.83f;
        float top = y + s * 0.2f;
        float bot = y + s * 0.8f;
        thickLine(c, l, top, r, top, t, color);
        thickLine(c, r + t * 0.2f, top, l - t * 0.2f, bot, t, color);
        thickLine(c, l, bot, r, bot, t, color);
    }

    /**
     * Themed HUD-panel background that stays in sync with the rest of the HUD's blur/liquid-glass
     * mode (mirrors {@code drawClientRect}) but keeps the near-black look.
     */
    public static void background(CustomDrawContext c, float x, float y, float w, float h, float radius, float alpha) {
        if (Interface.showMinimalizm() && Interface.blurHudEnabled()) {
            c.drawBlurredRect(x, y, w, h, 45.0f, radius, BorderRadius.all(radius), ColorRGBA.WHITE.withAlpha(255.0f * alpha * Interface.minimalizm()));
        }
        if (Interface.showGlass()) {
            c.drawLiquidGlass(x, y, w, h, radius, 0.08f, BorderRadius.all(radius), ColorRGBA.WHITE.withAlpha(255.0f * alpha * Interface.glass()));
        }
        c.drawSquircle(x, y, w, h, radius, BorderRadius.all(radius), panel(238.0f * alpha).mulAlpha(1.0f - 0.45f * Interface.glass()));
    }

    /** Rounded chevron arrow head pointing toward +Y (downward / outward), centered at (cx, cy). */
    public static void arrow(CustomDrawContext c, float cx, float cy, float w, float h, float thick, ColorRGBA color) {
        float tipX = cx;
        float tipY = cy + h / 2.0f;
        thickLine(c, tipX, tipY, cx - w / 2.0f, cy - h / 2.0f, thick, color);
        thickLine(c, tipX, tipY, cx + w / 2.0f, cy - h / 2.0f, thick, color);
    }

    // ───────────────────────────── progress ring ─────────────────────────────

    /**
     * Circular countdown ring (the "кружок где заканчивается зелье"): a faint full ring with a bright
     * accent arc covering {@code progress} (0..1) of the circle, drawn clockwise from the top.
     */
    public static void ring(CustomDrawContext c, float cx, float cy, float radius, float thickness,
                            float progress, ColorRGBA bg, ColorRGBA fg) {
        progress = MathHelper.clamp(progress, 0.0f, 1.0f);
        int segments = 40;
        float inner = radius - thickness;
        float mid = radius - thickness / 2.0f;
        // faint full ring
        ringOutline(c, cx, cy, mid, thickness, bg);
        if (progress <= 0.0f) {
            return;
        }
        int lit = Math.max(1, Math.round(segments * progress));
        for (int i = 0; i < lit; i++) {
            float a0 = (float) (-Math.PI / 2.0 + (Math.PI * 2.0) * i / segments);
            float a1 = (float) (-Math.PI / 2.0 + (Math.PI * 2.0) * (i + 1) / segments);
            float x0 = cx + MathHelper.cos(a0) * mid;
            float y0 = cy + MathHelper.sin(a0) * mid;
            float x1 = cx + MathHelper.cos(a1) * mid;
            float y1 = cy + MathHelper.sin(a1) * mid;
            line(c, x0, y0, x1, y1, fg);
        }
        // dummy reference so inner is not flagged unused on some toolchains
        if (inner < 0.0f) {
            line(c, cx, cy, cx, cy, fg);
        }
    }
}
