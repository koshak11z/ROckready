/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.VertexFormats
 */
package moscow.rockstar.ui.hud.inline;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.hud.inline.InlineValue;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import net.minecraft.client.render.VertexFormats;

public class InlineElement
extends HudElement {
    protected final SelectSetting elements = new SelectSetting(this, "elements").draggable().min(1);

    public InlineElement(String name, String icon) {
        super(name, icon);
        this.height = 18.0f;
    }

    @Override
    protected void renderComponent(UIContext context) {
        float textWidth;
        InlineValue elmt;
        context.drawClientRect(this.x, this.y, this.width, this.height, this.animation.getValue(), this.dragAnim.getValue(), 3.0f);
        ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
        context.drawTexture(Rockstar.id(this.icon), this.x + 5.0f, this.y + 5.0f, 8.0f, 8.0f, Colors.getTextColor());
        float xOffset = 0.0f;
        FontBatching fontBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);
        for (SelectSetting.Value value : this.elements.getValues()) {
            boolean hover;
            elmt = (InlineValue)value;
            if (!elmt.isSelected()) continue;
            textWidth = Fonts.MEDIUM.getFont(8.0f).width(elmt.text()) + 8.0f * elmt.copyAnim().getValue();
            boolean bl = hover = GuiUtility.isHovered(this.x + 19.0f + xOffset, this.y + 6.0f, Fonts.MEDIUM.getFont(8.0f).width(elmt.text()), 8.0, context) && !elmt.copy().isEmpty();
            if (!hover || elmt.copyTimer().finished(1000L)) {
                elmt.copied(false);
            }
            elmt.copyAnim().update(hover);
            elmt.successAnim().update(elmt.copied());
            context.drawText(Fonts.MEDIUM.getFont(8.0f), elmt.text(), this.x + 19.0f + xOffset + 8.0f * elmt.copyAnim().getValue(), this.y + 6.0f, Colors.getTextColor());
            if (!elmt.suffix().isEmpty()) {
                context.drawText(Fonts.MEDIUM.getFont(7.0f), elmt.suffix(), this.x + 19.0f + xOffset + textWidth, this.y + 7.0f, Colors.getTextColor().mulAlpha(0.5f));
            }
            xOffset += textWidth + Fonts.MEDIUM.getFont(7.0f).width(elmt.suffix()) + 10.0f;
        }
        ((Batching)fontBatching).draw();
        xOffset = 0.0f;
        for (SelectSetting.Value value : this.elements.getValues()) {
            elmt = (InlineValue)value;
            if (!elmt.isSelected()) continue;
            textWidth = Fonts.MEDIUM.getFont(8.0f).width(elmt.text()) + 8.0f * elmt.copyAnim().getValue();
            if (xOffset != 0.0f) {
                context.drawRoundedRect(this.x + 19.0f + xOffset - 7.0f, this.y + this.height / 2.0f - 1.0f, 2.0f, 2.0f, BorderRadius.all(1.0f), Colors.getTextColor().mulAlpha(0.5f));
            }
            RenderUtility.rotate(context.getMatrices(), this.x + 19.0f + xOffset + 3.0f, this.y + 6.0f + 3.0f, 90.0f * elmt.successAnim().getValue());
            context.drawTexture(Rockstar.id("icons/hud/copy.png"), this.x + 19.0f + xOffset, this.y + 6.0f, 6.0f, 6.0f, Colors.getTextColor().mulAlpha(elmt.copyAnim().getValue() * (1.0f - elmt.successAnim().getValue())));
            RenderUtility.end(context.getMatrices());
            RenderUtility.rotate(context.getMatrices(), this.x + 19.0f + xOffset + 3.0f, this.y + 6.0f + 3.0f, -90.0f + 90.0f * elmt.successAnim().getValue());
            context.drawTexture(Rockstar.id("icons/check.png"), this.x + 19.0f + xOffset, this.y + 6.0f, 6.0f, 6.0f, Colors.GREEN.mulAlpha(elmt.copyAnim().getValue() * elmt.successAnim().getValue()));
            RenderUtility.end(context.getMatrices());
            xOffset += textWidth + Fonts.MEDIUM.getFont(7.0f).width(elmt.suffix()) + 10.0f;
        }
        ScissorUtility.pop();
        this.width = 15.0f + xOffset;
        this.getWidthAnim().update(this.width);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        float xOffset = 0.0f;
        for (SelectSetting.Value value : this.elements.getValues()) {
            boolean hover;
            InlineValue elmt = (InlineValue)value;
            if (!elmt.isSelected()) continue;
            float textWidth = Fonts.MEDIUM.getFont(8.0f).width(elmt.text());
            boolean bl = hover = GuiUtility.isHovered((double)(this.x + 19.0f + xOffset), (double)(this.y + 6.0f), (double)Fonts.MEDIUM.getFont(8.0f).width(elmt.text()), 8.0, mouseX, mouseY) && !elmt.copy().isEmpty();
            if (hover && button == MouseButton.LEFT) {
                TextUtility.copyText(elmt.copy());
                elmt.copyTimer().reset();
                elmt.copied(true);
                return;
            }
            xOffset += textWidth + Fonts.MEDIUM.getFont(7.0f).width(elmt.suffix()) + 10.0f;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }
}

