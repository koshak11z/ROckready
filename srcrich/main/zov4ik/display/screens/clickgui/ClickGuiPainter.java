package im.zov4ik.display.screens.clickgui;

import im.zov4ik.display.hud.RefRender;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.utils.display.shape.ShapeProperties;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public final class ClickGuiPainter {
    private ClickGuiPainter() {
    }

    public static void drawControlSurface(DrawContext context, float x, float y, float width, float height, boolean active) {
        drawControlSurface(context, x, y, width, height, ClickGuiTheme.CONTROL_RADIUS, active ? 1F : 0F);
    }

    public static void drawControlSurface(DrawContext context, float x, float y, float width, float height, float radius, boolean active) {
        drawControlSurface(context, x, y, width, height, radius, active ? 1F : 0F);
    }

    public static void drawControlSurface(DrawContext context, float x, float y, float width, float height, float activeProgress) {
        drawControlSurface(context, x, y, width, height, ClickGuiTheme.CONTROL_RADIUS, activeProgress);
    }

    public static void drawControlSurface(DrawContext context, float x, float y, float width, float height, float radius, float activeProgress) {
        RefRender.control(context.getMatrices(), x, y, width, height, radius, activeProgress);
    }

    public static void drawToggle(DrawContext context, float x, float y, float progress) {
        RefRender.toggle(context.getMatrices(), x, y, ClickGuiTheme.TOGGLE_WIDTH, ClickGuiTheme.TOGGLE_HEIGHT, progress);
    }

    public static void drawStateBadge(DrawContext context, float x, float y, float size, float activeProgress) {
        float progress = MathHelper.clamp(activeProgress, 0F, 1F);
        int idle = ClickGuiTheme.CONTROL_OUTLINE;
        int active = ColorAssist.overCol(ClickGuiTheme.CONTROL_OUTLINE, ClickGuiTheme.accentSoft(), 0.55F);
        int outline = ColorAssist.overCol(idle, active, progress);
        int top = ColorAssist.multAlpha(ColorAssist.overCol(ClickGuiTheme.CONTROL_BG, ClickGuiTheme.CONTROL_BG_ACTIVE, progress), 0.9F);
        int bottom = ColorAssist.multAlpha(ColorAssist.overCol(ClickGuiTheme.CONTROL_BG_DARK, ClickGuiTheme.CONTROL_BG_ACTIVE_DARK, progress), 0.9F);

        QuickImports.blur.render(ShapeProperties.create(context.getMatrices(), x - 0.4F, y - 0.4F, size + 0.8F, size + 0.8F)
                .round(4.0F)
                .softness(10F)
                .color(ColorAssist.multAlpha(0xFF060A11, 0.05F + progress * 0.015F))
                .build());

        QuickImports.rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, size, size)
                .round(3.35F)
                .thickness(1.1F)
                .outlineColor(outline)
                .color(top, top, bottom, bottom)
                .build());

        int markColor = ColorAssist.overCol(0xFFB0B8C8, ClickGuiTheme.accent(), progress);
        if (progress >= 0.5F) {
            QuickImports.rectangle.render(ShapeProperties.create(context.getMatrices(), x + size / 2F - 2.2F, y + size / 2F - 0.35F, 1.8F, 1.0F)
                    .round(0.5F)
                    .color(markColor)
                    .build());
            QuickImports.rectangle.render(ShapeProperties.create(context.getMatrices(), x + size / 2F - 0.9F, y + size / 2F - 1.5F, 3.4F, 1.0F)
                    .round(0.5F)
                    .color(markColor)
                    .build());
        } else {
            QuickImports.rectangle.render(ShapeProperties.create(context.getMatrices(), x + size / 2F - 1.25F, y + size / 2F - 1.25F, 2.5F, 2.5F)
                    .round(1.25F)
                    .color(markColor)
                    .build());
        }
    }
}
