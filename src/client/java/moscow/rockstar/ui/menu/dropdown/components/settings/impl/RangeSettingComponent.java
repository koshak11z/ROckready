/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.settings.RangeSetting;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;

public class RangeSettingComponent
extends MenuSettingComponent<RangeSetting> {
    private final Animation xAnim = new Animation(500L, Easing.BAKEK_PAGES);
    private final Animation widthAnim = new Animation(500L, Easing.BAKEK_PAGES);
    private boolean dragFirst;
    private boolean dragSecond;

    public RangeSettingComponent(RangeSetting setting, CustomComponent parent) {
        super(setting, parent);
    }

    @Override
    protected void renderComponent(UIContext context) {
        float second;
        float x = this.x + 9.0f;
        float y = this.y + 2.0f;
        float width = this.width - 18.0f;
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        float leftPadding = 10.0f;
        float nameHeight = Fonts.REGULAR.getFont(7.0f).height();
        float first = ((RangeSetting)this.setting).getFirstValue();
        if (first >= (second = ((RangeSetting)this.setting).getSecondValue())) {
            first = ((RangeSetting)this.setting).getSecondValue();
            second = ((RangeSetting)this.setting).getFirstValue();
        }
        this.xAnim.update(first);
        this.widthAnim.update(second);
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        context.drawRoundedRect(x, y + this.height - 12.0f, width, 2.0f, BorderRadius.all(0.25f), Colors.getSliderWindowColor().mulAlpha(0.7f));
        context.drawRoundedRect(x + width * GuiUtility.getPercent(this.xAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()), y + this.height - 12.0f, width * GuiUtility.getPercent(this.widthAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()) - width * GuiUtility.getPercent(this.xAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()), 2.0f, BorderRadius.all(0.25f), Colors.getSliderTrackColor());
        context.drawShadow(x + width * GuiUtility.getPercent(this.xAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()) - 3.0f, y + this.height - 14.0f, 6.0f, 6.0f, 10.0f, BorderRadius.all(3.0f), ColorRGBA.BLACK.withAlpha(63.75f));
        context.drawRoundedRect(x + width * GuiUtility.getPercent(this.xAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()) - 3.0f, y + this.height - 14.0f, 6.0f, 6.0f, BorderRadius.all(3.0f), Colors.getSliderCircleColor());
        context.drawShadow(x + width * GuiUtility.getPercent(this.xAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()) + width * GuiUtility.getPercent(this.widthAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()) - width * GuiUtility.getPercent(this.xAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()) - 3.0f, y + this.height - 14.0f, 6.0f, 6.0f, 10.0f, BorderRadius.all(3.0f), ColorRGBA.BLACK.withAlpha(63.75f));
        context.drawRoundedRect(x + width * GuiUtility.getPercent(this.xAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()) + width * GuiUtility.getPercent(this.widthAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()) - width * GuiUtility.getPercent(this.xAnim.getValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()) - 3.0f, y + this.height - 14.0f, 6.0f, 6.0f, BorderRadius.all(3.0f), Colors.getSliderCircleColor());
        String value = String.format("\u043e\u0442 %s \u0434\u043e %s", TextUtility.formatNumber(this.xAnim.getValue()), TextUtility.formatNumber(this.widthAnim.getValue()));
        context.drawFadeoutText(nameFont, Localizator.translate(((RangeSetting)this.setting).getName()), this.x + leftPadding, y + 11.0f - nameFont.height(), Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())), 0.8f, 1.0f, this.getParent().getWidth() - leftPadding - Fonts.REGULAR.getFont(7.0f).width(value) - 10.0f);
        context.drawRightText(Fonts.REGULAR.getFont(7.0f), value, x + width, y + 11.0f - nameHeight, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())));
        if (this.isHovered(context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
        if (this.dragFirst) {
            float xValue = GuiUtility.getSliderValue(((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax(), x, width, context.getMouseX());
            ((RangeSetting)this.setting).setFirstValue(xValue);
            CursorUtility.set(CursorType.ARROW_HORIZONTAL);
        } else if (this.dragSecond) {
            float xValue = GuiUtility.getSliderValue(((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax(), x, width, context.getMouseX());
            ((RangeSetting)this.setting).setSecondValue(xValue);
            CursorUtility.set(CursorType.ARROW_HORIZONTAL);
        }
    }

    @Override
    public void drawSplit(UIContext context) {
        float separatorHeight = 0.5f;
        context.drawRect(this.x, this.y + this.height, this.width, separatorHeight, Colors.getTextColor().withAlpha(5.1f));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        float x = this.x + 9.0f;
        float width = this.width - 18.0f;
        if (this.isHovered(mouseX, mouseY)) {
            float secondDist;
            float firstDist = (float)Math.abs(mouseX - (double)(x + width * GuiUtility.getPercent(((RangeSetting)this.setting).getFirstValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax())));
            if (firstDist < (secondDist = (float)Math.abs(mouseX - (double)(x + width * GuiUtility.getPercent(((RangeSetting)this.setting).getSecondValue(), ((RangeSetting)this.setting).getMin(), ((RangeSetting)this.setting).getMax()))))) {
                this.dragFirst = true;
            } else {
                this.dragSecond = true;
            }
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.dragFirst = false;
        this.dragSecond = false;
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public float getHeight() {
        this.height = 29.0f;
        return 29.0f;
    }
}

