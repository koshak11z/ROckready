/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.Vec2f
 */
package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.settings.BezierSetting;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.util.math.Vec2f;

public class BezierSettingComponent
extends MenuSettingComponent<BezierSetting> {
    private final Animation startX = new Animation(500L, Easing.BAKEK_PAGES);
    private final Animation startY = new Animation(500L, Easing.BAKEK_PAGES);
    private final Animation endX = new Animation(500L, Easing.BAKEK_PAGES);
    private final Animation endY = new Animation(500L, Easing.BAKEK_PAGES);
    private boolean dragStart;
    private boolean dragEnd;

    public BezierSettingComponent(BezierSetting setting, CustomComponent parent) {
        super(setting, parent);
    }

    @Override
    protected void renderComponent(UIContext context) {
        float x = this.x + 9.0f;
        float y = this.y + 2.0f;
        float width = this.width - 18.0f;
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        float leftPadding = 10.0f;
        float nameHeight = Fonts.REGULAR.getFont(7.0f).height();
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        float offset = 3.0f;
        float boxX = x - 1.0f + offset;
        float boxY = y + 17.0f + offset;
        float boxWidth = width + 2.0f - offset * 2.0f;
        float boxHeight = this.height - 10.0f - 17.0f - offset * 2.0f;
        context.drawRoundedRect(boxX - offset, boxY - offset, boxWidth + offset * 2.0f, boxHeight + offset * 2.0f, BorderRadius.all(6.0f), Colors.getBackgroundColor().mulAlpha(0.3f));
        context.drawRoundedRect(boxX + this.startX.getValue() * boxWidth - 3.0f, boxY + this.startY.getValue() * boxHeight - 3.0f, 6.0f, 6.0f, BorderRadius.all(6.0f), Colors.WHITE.withAlpha(255.0f));
        context.drawRoundedRect(boxX + this.endX.getValue() * boxWidth - 3.0f, boxY + this.endY.getValue() * boxHeight - 3.0f, 6.0f, 6.0f, BorderRadius.all(6.0f), Colors.WHITE.withAlpha(255.0f));
        Vec2f anchorStart = new Vec2f(boxX, boxY + boxHeight);
        Vec2f controlStart = new Vec2f(boxX + this.startX.getValue() * boxWidth, boxY + this.startY.getValue() * boxHeight);
        Vec2f controlEnd = new Vec2f(boxX + this.endX.getValue() * boxWidth, boxY + this.endY.getValue() * boxHeight);
        Vec2f anchorEnd = new Vec2f(boxX + boxWidth, boxY);
        context.drawBezier(anchorStart, controlStart, controlEnd, anchorEnd, ColorRGBA.WHITE, 50);
        context.drawLine(anchorStart, controlStart, Colors.WHITE.mulAlpha(0.5f));
        context.drawLine(anchorEnd, controlEnd, Colors.WHITE.mulAlpha(0.5f));
        context.drawFadeoutText(nameFont, Localizator.translate(((BezierSetting)this.setting).getName()), this.x + leftPadding, y + 11.0f - nameFont.height(), Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())), 0.8f, 1.0f, this.getParent().getWidth() - leftPadding - 10.0f);
        if (this.isHovered(context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
        if (this.dragStart) {
            float xValue = GuiUtility.getSliderValue(0.0f, 1.0f, boxX, boxWidth, context.getMouseX());
            float yValue = GuiUtility.getSliderValueWithoutClamp(0.0f, 1.0f, boxY, boxHeight, context.getMouseY());
            ((BezierSetting)this.setting).start(new Vec2f(xValue, Math.clamp(yValue, -0.5f, 1.5f)));
            CursorUtility.set(CursorType.CROSSHAIR);
        } else if (this.dragEnd) {
            float xValue = GuiUtility.getSliderValue(0.0f, 1.0f, boxX, boxWidth, context.getMouseX());
            float yValue = GuiUtility.getSliderValueWithoutClamp(0.0f, 1.0f, boxY, boxHeight, context.getMouseY());
            ((BezierSetting)this.setting).end(new Vec2f(xValue, Math.clamp(yValue, -0.5f, 1.5f)));
            CursorUtility.set(CursorType.CROSSHAIR);
        }
        this.startX.setValue(((BezierSetting)this.setting).start().x);
        this.startY.setValue(((BezierSetting)this.setting).start().y);
        this.endX.setValue(((BezierSetting)this.setting).end().x);
        this.endY.setValue(((BezierSetting)this.setting).end().y);
    }

    @Override
    public void drawSplit(UIContext context) {
        float separatorHeight = 0.5f;
        context.drawRect(this.x, this.y + this.height, this.width, separatorHeight, Colors.getTextColor().withAlpha(5.1f));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        float x = this.x + 9.0f;
        float y = this.y + 2.0f;
        float width = this.width - 18.0f;
        if (this.isHovered(mouseX, mouseY)) {
            float endDist;
            float boxX = x - 1.0f;
            float boxY = y + 17.0f;
            float boxWidth = width + 2.0f;
            float boxHeight = this.height - 10.0f - 17.0f;
            Vec2f mouse = new Vec2f(GuiUtility.getPercent((float)mouseX, boxX, boxX + boxWidth), GuiUtility.getPercent((float)mouseY, boxY, boxY + boxHeight));
            float startDist = this.distance(((BezierSetting)this.setting).start(), mouse);
            if (startDist < (endDist = this.distance(((BezierSetting)this.setting).end(), mouse))) {
                this.dragStart = true;
            } else {
                this.dragEnd = true;
            }
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    public float distance(Vec2f vec, Vec2f vec2) {
        float f = vec.x - vec2.x;
        float g = vec.y - vec2.y;
        return (float)Math.sqrt(f * f + g * g);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.dragStart = false;
        this.dragEnd = false;
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public float getHeight() {
        this.height = this.width - 14.0f;
        return this.height;
    }
}

