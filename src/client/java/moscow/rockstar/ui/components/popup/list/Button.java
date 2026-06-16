/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 */
package moscow.rockstar.ui.components.popup.list;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.components.popup.PopupAction;
import moscow.rockstar.ui.components.popup.PopupComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;

public class Button
extends PopupComponent {
    private final Popup popup;
    private final String text;
    private final String icon;
    private PopupAction action;
    private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

    @Override
    protected void renderComponent(UIContext context) {
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        float nameLeftPadding = 8.0f;
        float nameHeight = nameFont.height();
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        if (this.isHovered(context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
        ColorRGBA color = !this.text.equals(Localizator.translate("remove")) ? Colors.getTextColor() : ColorRGBA.RED.mix(ColorRGBA.WHITE, 0.3f);
        context.drawFadeoutText(nameFont, this.text, this.x + nameLeftPadding, this.y + GuiUtility.getMiddleOfBox(nameHeight, this.height), color.withAlpha(RenderSystem.getShaderColor()[3] * 255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())), 0.8f, 1.0f, this.width - 24.0f);
        context.drawTexture(Rockstar.id(this.icon), this.x + this.width - 16.0f, this.y + 6.0f, 8.0f, 8.0f, color.withAlpha(RenderSystem.getShaderColor()[3] * 255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())));
        if (this.isHovered(context)) {
            CursorUtility.set(CursorType.HAND);
        }
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.isHovered(mouseX, mouseY) && button == MouseButton.LEFT) {
            this.action.run(this.popup);
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public float getHeight() {
        this.height = 19.0f;
        return 19.0f;
    }

    @Generated
    public Button(Popup popup, String text, String icon, PopupAction action) {
        this.popup = popup;
        this.text = text;
        this.icon = icon;
        this.action = action;
    }
}

