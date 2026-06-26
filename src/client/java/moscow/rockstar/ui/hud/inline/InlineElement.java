package moscow.rockstar.ui.hud.inline;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import net.minecraft.client.render.VertexFormats;

/**
 * Redesigned inline pill (black rounded panel, accent leading icon, dot-separated values with
 * hover-to-copy). Base for {@code WorldElement} (coords/server) and {@code PlayerElement}.
 */
public class InlineElement extends HudElement {
    protected final SelectSetting elements = new SelectSetting(this, "elements").draggable().min(1);

    private static final float HEIGHT = 16.0f;
    private static final float ICON = 8.0f;
    private static final float TEXT_X = 19.0f;

    public InlineElement(String name, String icon) {
        super(name, icon);
        this.height = HEIGHT;
    }

    protected Font font() {
        return Fonts.MEDIUM.getFont(7.5f);
    }

    /** Leading icon; override to draw a procedural glyph instead of the PNG. */
    protected void drawLeadingIcon(UIContext context, float x, float y, float size, ColorRGBA color) {
        context.drawTexture(Rockstar.id(this.icon), x, y, size, size, color);
    }

    @Override
    protected void renderComponent(UIContext context) {
        Font font = this.font();
        Font suffixFont = Fonts.MEDIUM.getFont(6.5f);
        float h = HEIGHT;

        Glyphs.background(context, this.x, this.y, this.width, h, 6.0f, this.animation.getValue());

        ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, h);
        this.drawLeadingIcon(context, this.x + 6.0f, this.y + (h - ICON) / 2.0f, ICON, Colors.getAccentColor());

        float textY = this.y + (h - font.height()) / 2.0f - 0.5f;
        float xOffset = 0.0f;
        FontBatching fontBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);
        for (SelectSetting.Value value : this.elements.getValues()) {
            InlineValue elmt = (InlineValue) value;
            if (!elmt.isSelected()) continue;
            float textWidth = font.width(elmt.text()) + 8.0f * elmt.copyAnim().getValue();
            boolean hover = GuiUtility.isHovered(this.x + TEXT_X + xOffset, this.y + 4.0f, font.width(elmt.text()), 8.0, context) && !elmt.copy().isEmpty();
            if (!hover || elmt.copyTimer().finished(1000L)) {
                elmt.copied(false);
            }
            elmt.copyAnim().update(hover);
            elmt.successAnim().update(elmt.copied());
            context.drawText(font, elmt.text(), this.x + TEXT_X + xOffset + 8.0f * elmt.copyAnim().getValue(), textY, Colors.getTextColor());
            if (!elmt.suffix().isEmpty()) {
                context.drawText(suffixFont, elmt.suffix(), this.x + TEXT_X + xOffset + textWidth, textY + 0.5f, Colors.getTextColor().mulAlpha(0.5f));
            }
            xOffset += textWidth + suffixFont.width(elmt.suffix()) + 10.0f;
        }
        ((Batching) fontBatching).draw();

        xOffset = 0.0f;
        for (SelectSetting.Value value : this.elements.getValues()) {
            InlineValue elmt = (InlineValue) value;
            if (!elmt.isSelected()) continue;
            float textWidth = font.width(elmt.text()) + 8.0f * elmt.copyAnim().getValue();
            if (xOffset != 0.0f) {
                context.drawRoundedRect(this.x + TEXT_X + xOffset - 7.0f, this.y + h / 2.0f - 1.0f, 2.0f, 2.0f, BorderRadius.all(1.0f), Colors.getTextColor().mulAlpha(0.4f));
            }
            RenderUtility.rotate(context.getMatrices(), this.x + TEXT_X + xOffset + 3.0f, textY + 3.0f, 90.0f * elmt.successAnim().getValue());
            context.drawTexture(Rockstar.id("icons/hud/copy.png"), this.x + TEXT_X + xOffset, textY, 6.0f, 6.0f, Colors.getTextColor().mulAlpha(elmt.copyAnim().getValue() * (1.0f - elmt.successAnim().getValue())));
            RenderUtility.end(context.getMatrices());
            RenderUtility.rotate(context.getMatrices(), this.x + TEXT_X + xOffset + 3.0f, textY + 3.0f, -90.0f + 90.0f * elmt.successAnim().getValue());
            context.drawTexture(Rockstar.id("icons/check.png"), this.x + TEXT_X + xOffset, textY, 6.0f, 6.0f, Colors.GREEN.mulAlpha(elmt.copyAnim().getValue() * elmt.successAnim().getValue()));
            RenderUtility.end(context.getMatrices());
            xOffset += textWidth + suffixFont.width(elmt.suffix()) + 10.0f;
        }
        ScissorUtility.pop();
        this.width = 15.0f + xOffset;
        this.getWidthAnim().update(this.width);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        float xOffset = 0.0f;
        Font font = this.font();
        for (SelectSetting.Value value : this.elements.getValues()) {
            InlineValue elmt = (InlineValue) value;
            if (!elmt.isSelected()) continue;
            float textWidth = font.width(elmt.text());
            boolean hover = GuiUtility.isHovered((double) (this.x + TEXT_X + xOffset), (double) (this.y + 4.0f), (double) font.width(elmt.text()), 8.0, mouseX, mouseY) && !elmt.copy().isEmpty();
            if (hover && button == MouseButton.LEFT) {
                TextUtility.copyText(elmt.copy());
                elmt.copyTimer().reset();
                elmt.copied(true);
                return;
            }
            xOffset += textWidth + Fonts.MEDIUM.getFont(6.5f).width(elmt.suffix()) + 10.0f;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }
}
