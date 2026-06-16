/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 */
package moscow.rockstar.ui.components.popup.list;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.ui.components.popup.CheckBoxAction;
import moscow.rockstar.ui.components.popup.PopupComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;

public class CheckBox
extends PopupComponent {
    private boolean enabled;
    private final String text;
    private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation enableAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private CheckBoxAction action;

    public CheckBox(String text) {
        this.text = text;
    }

    @Override
    protected void renderComponent(UIContext context) {
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        float nameLeftPadding = 8.0f;
        float nameHeight = nameFont.height();
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        this.enableAnimation.update(this.enabled ? 1.0f : 0.0f);
        if (this.isHovered(context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
        float shaderAlpha = RenderSystem.getShaderColor()[3] * 255.0f;
        float textAlpha = shaderAlpha * (0.75f + 0.25f * this.enableAnimation.getValue() + 0.25f * this.hoverAnimation.getValue());
        context.drawFadeoutText(nameFont, this.text, this.x + nameLeftPadding, this.y + GuiUtility.getMiddleOfBox(nameHeight, this.height), Colors.getTextColor().mix(Colors.getAccentColor(), this.enableAnimation.getValue()).withAlpha(textAlpha), 0.8f, 1.0f, this.width - 12.0f - 12.0f * this.enableAnimation.getValue());
        float alpha = this.enableAnimation.getValue() * shaderAlpha;
        if (this.enableAnimation.getValue() >= 0.0f) {
            context.drawTexture(Rockstar.id("icons/check.png"), this.x + this.width - 13.0f - this.enableAnimation.getValue() * 2.0f, this.y + 7.0f, 6.0f, 6.0f, Colors.getAccentColor().withAlpha(alpha));
        }
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.isHovered(mouseX, mouseY) && button == MouseButton.LEFT) {
            boolean bl = this.enabled = !this.enabled;
            if (this.action != null) {
                this.action.handleAction(this.enabled);
            }
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public float getHeight() {
        this.height = 19.0f;
        return 19.0f;
    }

    public CheckBox enabled(boolean value) {
        this.enabled = value;
        return this;
    }

    public CheckBox action(CheckBoxAction action) {
        this.action = action;
        return this;
    }
}

