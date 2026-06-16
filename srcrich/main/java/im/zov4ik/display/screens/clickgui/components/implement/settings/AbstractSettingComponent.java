package im.zov4ik.display.screens.clickgui.components.implement.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import im.zov4ik.features.module.setting.Setting;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.scissor.ScissorAssist;
import im.zov4ik.zov4ik;
import im.zov4ik.display.screens.clickgui.ClickGuiPainter;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;

@Getter
@RequiredArgsConstructor
public abstract class AbstractSettingComponent extends AbstractComponent {
    private final Setting setting;

    protected void drawSettingLabel(DrawContext context, String text, float rightStartX) {
        drawSettingLabel(context, text, rightStartX, 3.1F);
    }

    protected void drawSettingLabel(DrawContext context, String text, float rightStartX, float labelOffsetY) {
        FontRenderer font = Fonts.getSize(ClickGuiTheme.SETTING_FONT_SIZE, Fonts.Type.SEMI);
        float labelX = x + 2F;
        float labelWidth = Math.max(10F, rightStartX - labelX - 4F);
        drawScrollableText(context, font, text, labelX, y + labelOffsetY, labelWidth, ClickGuiTheme.TEXT_SECONDARY);
    }

    protected void drawControlText(DrawContext context, String text, float boxX, float boxY, float boxWidth) {
        FontRenderer font = Fonts.getSize(ClickGuiTheme.CONTROL_FONT_SIZE, Fonts.Type.SEMI);
        float contentWidth = boxWidth - 8F;
        if (font.getStringWidth(text) <= contentWidth) {
            font.drawCenteredString(context.getMatrices(), text, boxX + boxWidth / 2F, boxY + 4.15F, ClickGuiTheme.TEXT_PRIMARY);
            return;
        }

        Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();
        ScissorAssist scissorManager = zov4ik.getInstance().getScissorManager();
        scissorManager.push(positionMatrix, boxX + 3F, boxY + 1F, contentWidth, 14F);
        font.drawStringWithScroll(context.getMatrices(), text, boxX + 3F, boxY + 4.15F, contentWidth, ClickGuiTheme.TEXT_PRIMARY);
        scissorManager.pop();
    }

    protected float getAdaptiveControlWidth(String text) {
        float minWidth = ClickGuiTheme.CONTROL_WIDTH;
        float maxWidth = Math.max(minWidth, Math.min(width - 28F, 112F));
        float desired = Fonts.getSize(ClickGuiTheme.CONTROL_FONT_SIZE, Fonts.Type.SEMI).getStringWidth(text) + 11F;
        return Math.max(minWidth, Math.min(maxWidth, desired));
    }

    private void drawScrollableText(DrawContext context, FontRenderer font, String text, float drawX, float drawY, float width, int color) {
        Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();
        ScissorAssist scissorManager = zov4ik.getInstance().getScissorManager();
        scissorManager.push(positionMatrix, drawX, y, width, 22F);
        font.drawStringWithScroll(context.getMatrices(), text, drawX, drawY, width, color);
        scissorManager.pop();
    }

    protected void drawControlSurface(DrawContext context, float boxX, float boxY, float boxWidth, boolean active) {
        ClickGuiPainter.drawControlSurface(context, boxX, boxY, boxWidth, ClickGuiTheme.CONTROL_HEIGHT, active);
    }

    protected void drawControlSurface(DrawContext context, float boxX, float boxY, float boxWidth, float activeProgress) {
        ClickGuiPainter.drawControlSurface(context, boxX, boxY, boxWidth, ClickGuiTheme.CONTROL_HEIGHT, activeProgress);
    }

    protected void drawControlSurface(DrawContext context, float boxX, float boxY, float boxWidth, float boxHeight, float radius, boolean active) {
        ClickGuiPainter.drawControlSurface(context, boxX, boxY, boxWidth, boxHeight, radius, active);
    }

    protected void drawControlSurface(DrawContext context, float boxX, float boxY, float boxWidth, float boxHeight, float radius, float activeProgress) {
        ClickGuiPainter.drawControlSurface(context, boxX, boxY, boxWidth, boxHeight, radius, activeProgress);
    }

    protected void drawToggle(DrawContext context, float toggleX, float toggleY, float progress) {
        ClickGuiPainter.drawToggle(context, toggleX, toggleY, progress);
    }

    protected float getChipWidth(String text) {
        return Math.max(20F, Fonts.getSize(ClickGuiTheme.CONTROL_FONT_SIZE, Fonts.Type.SEMI).getStringWidth(text) + 8F);
    }

    protected void drawOptionChip(DrawContext context, String text, float chipX, float chipY, float chipWidth, boolean active) {
        drawOptionChip(context, text, chipX, chipY, chipWidth, active ? 1F : 0F);
    }

    protected void drawOptionChip(DrawContext context, String text, float chipX, float chipY, float chipWidth, float activeProgress) {
        float progress = MathHelper.clamp(activeProgress, 0F, 1F);
        if (progress > 0.001F) {
            rectangle.render(ShapeProperties.create(context.getMatrices(), chipX, chipY, chipWidth, ClickGuiTheme.CONTROL_HEIGHT)
                    .round(3.0F)
                    .thickness(1.25F)
                    .outlineColor(ColorAssist.overCol(ClickGuiTheme.CONTROL_OUTLINE,
                            ColorAssist.overCol(ClickGuiTheme.CONTROL_OUTLINE, ClickGuiTheme.accentOutline(), 0.82F), progress))
                    .color(ColorAssist.overCol(ClickGuiTheme.CONTROL_BG, ColorAssist.overCol(ClickGuiTheme.CONTROL_BG_ACTIVE, ClickGuiTheme.settingAccent(), 0.26F), progress),
                            ColorAssist.overCol(ClickGuiTheme.CONTROL_BG, ColorAssist.overCol(ClickGuiTheme.CONTROL_BG_ACTIVE, ClickGuiTheme.settingAccent(), 0.26F), progress),
                            ColorAssist.overCol(ClickGuiTheme.CONTROL_BG_DARK, ColorAssist.overCol(ClickGuiTheme.CONTROL_BG_ACTIVE_DARK, ClickGuiTheme.settingAccent(), 0.2F), progress),
                            ColorAssist.overCol(ClickGuiTheme.CONTROL_BG_DARK, ColorAssist.overCol(ClickGuiTheme.CONTROL_BG_ACTIVE_DARK, ClickGuiTheme.settingAccent(), 0.2F), progress))
                    .build());
        } else {
            drawControlSurface(context, chipX, chipY, chipWidth, ClickGuiTheme.CONTROL_HEIGHT, 3.0F, 0F);
        }
        int color = ColorAssist.overCol(ClickGuiTheme.TEXT_SECONDARY, ClickGuiTheme.TEXT_PRIMARY, progress);
        Fonts.getSize(ClickGuiTheme.CONTROL_FONT_SIZE, Fonts.Type.SEMI)
                .drawCenteredString(context.getMatrices(), text, chipX + chipWidth / 2F, chipY + 5.05F, color);
    }
}
