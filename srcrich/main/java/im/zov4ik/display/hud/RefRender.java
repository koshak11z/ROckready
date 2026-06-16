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

public final class RefRender {
    public static final int TEXT = new Color(250, 251, 255, 255).getRGB();
    public static final int TEXT_DIM = new Color(185, 190, 202, 255).getRGB();
    public static final int TEXT_MUTED = new Color(105, 111, 124, 255).getRGB();
    public static final int PANEL = new Color(2, 3, 6, 248).getRGB();
    public static final int PANEL_SOFT = new Color(5, 6, 10, 245).getRGB();
    public static final int OUTLINE = new Color(28, 31, 40, 210).getRGB();
    public static final int LINE = new Color(28, 31, 39, 150).getRGB();
    public static final int ACCENT = new Color(128, 93, 255, 255).getRGB();
    public static final int DANGER = new Color(240, 62, 82, 255).getRGB();
    public static final int GOOD = new Color(80, 235, 138, 255).getRGB();
    public static final int WARN = new Color(255, 204, 72, 255).getRGB();

    private static final int SHADOW = new Color(0, 0, 0, 125).getRGB();

    private RefRender() {
    }

    public static void panel(MatrixStack matrix, float x, float y, float width, float height, float radius) {
        panel(matrix, x, y, width, height, radius, 1.0F);
    }

    public static void panel(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha) {
        if (width <= 0.0F || height <= 0.0F || alpha <= 0.0F) {
            return;
        }

        x = Math.round(x);
        y = Math.round(y);
        width = Math.round(width);
        height = Math.round(height);

        QuickImports.rectangle.render(ShapeProperties.create(matrix, x - 0.8F, y + 1.0F, width + 1.6F, height + 1.6F)
                .round(radius + 0.9F)
                .softness(5.0F)
                .color(ColorAssist.multAlpha(SHADOW, alpha * 0.78F))
                .build());

        QuickImports.rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(radius)
                .thickness(0.85F)
                .outlineColor(ColorAssist.multAlpha(OUTLINE, alpha))
                .color(ColorAssist.multAlpha(PANEL_SOFT, alpha),
                        ColorAssist.multAlpha(PANEL, alpha),
                        ColorAssist.multAlpha(PANEL, alpha),
                        ColorAssist.multAlpha(PANEL, alpha))
                .build());

        float railH = Math.max(5.0F, height - 7.0F);
        QuickImports.rectangle.render(ShapeProperties.create(matrix, x + 2.6F, y + (height - railH) / 2.0F, 1.25F, railH)
                .round(0.7F)
                .color(ColorAssist.multAlpha(ACCENT, alpha * 0.92F))
                .build());

        QuickImports.rectangle.render(ShapeProperties.create(matrix, x + 5.5F, y + 0.75F, Math.max(0.0F, width - 8.5F), 0.45F)
                .color(ColorAssist.multAlpha(new Color(255, 255, 255, 255).getRGB(), alpha * 0.045F))
                .build());
    }

    public static void line(MatrixStack matrix, float x, float y, float width) {
        QuickImports.rectangle.render(ShapeProperties.create(matrix, Math.round(x), Math.round(y), Math.round(width), 0.55F)
                .color(LINE)
                .build());
    }

    public static void track(MatrixStack matrix, float x, float y, float width, float height) {
        QuickImports.rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(height / 2.0F)
                .color(new Color(18, 21, 29, 210).getRGB())
                .build());
    }

    public static void progress(MatrixStack matrix, float x, float y, float width, float height, float progress, int color) {
        float clamped = Math.max(0.0F, Math.min(1.0F, progress));
        if (width <= 0.0F || height <= 0.0F || clamped <= 0.0F) {
            return;
        }
        float progressWidth = width * clamped;
        QuickImports.rectangle.render(ShapeProperties.create(matrix, x, y, progressWidth, height)
                .round(height / 2.0F)
                .color(ColorAssist.multDark(color, 0.78F), color, color, ColorAssist.multDark(color, 0.86F))
                .build());
    }

    public static void icon(DrawContext context, Identifier id, float x, float y, float size, int color) {
        MatrixStack matrix = context.getMatrices();
        matrix.push();
        matrix.translate(Math.round(x * 2.0F) / 2.0F, Math.round(y * 2.0F) / 2.0F, 0.0F);
        matrix.scale(size, size, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Render2D.drawTexture(matrix, id, 0, 0, 1.0F, 1.0F, 0.0F, 0.0F, 32, 32, 32, 32, color);
        RenderSystem.disableBlend();
        matrix.pop();
    }

    public static void iconSlot(DrawContext context, Identifier id, float x, float y, float slot, float size, int color) {
        icon(context, id, x + (slot - size) / 2.0F, y + (slot - size) / 2.0F, size, color);
    }

    public static void control(MatrixStack matrix, float x, float y, float width, float height, float radius, float activeProgress) {
        float progress = Math.max(0.0F, Math.min(1.0F, activeProgress));
        int top = ColorAssist.overCol(new Color(5, 7, 11, 235).getRGB(), ColorAssist.multAlpha(ACCENT, 0.24F), progress);
        int bottom = ColorAssist.overCol(new Color(2, 3, 6, 240).getRGB(), ColorAssist.multAlpha(ACCENT, 0.15F), progress);
        int outline = ColorAssist.overCol(OUTLINE, ColorAssist.multAlpha(ACCENT, 0.72F), progress);
        QuickImports.rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(radius)
                .thickness(0.8F)
                .outlineColor(outline)
                .color(top, top, bottom, bottom)
                .build());
    }

    public static void toggle(MatrixStack matrix, float x, float y, float width, float height, float progress) {
        float clamped = Math.max(0.0F, Math.min(1.0F, progress));
        int fill = ColorAssist.overCol(new Color(14, 18, 27, 255).getRGB(), ColorAssist.multDark(ACCENT, 0.72F), clamped);
        int outline = ColorAssist.overCol(OUTLINE, ColorAssist.multAlpha(ACCENT, 0.85F), clamped);
        QuickImports.rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(height / 2.0F)
                .thickness(0.75F)
                .outlineColor(outline)
                .color(fill)
                .build());

        float knob = Math.max(3.2F, height - 2.4F);
        float knobX = x + 1.2F + (width - knob - 2.4F) * clamped;
        float knobY = y + (height - knob) / 2.0F;
        QuickImports.rectangle.render(ShapeProperties.create(matrix, knobX, knobY, knob, knob)
                .round(knob / 2.0F)
                .color(ColorAssist.overCol(new Color(158, 166, 182, 255).getRGB(), Color.WHITE.getRGB(), clamped))
                .build());
    }
}
