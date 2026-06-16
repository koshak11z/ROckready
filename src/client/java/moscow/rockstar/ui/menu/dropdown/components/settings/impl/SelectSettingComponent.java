/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import java.util.ArrayList;
import java.util.Comparator;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.components.animated.AnimatedNumber;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.penis.PenisPlayer;
import moscow.rockstar.utility.time.Timer;

public class SelectSettingComponent
extends MenuSettingComponent<SelectSetting> {
    private AnimatedNumber numberAnim;
    private SelectSetting.Value dragging;
    private final Timer sortTimer = new Timer();
    private boolean initialized;

    public SelectSettingComponent(SelectSetting setting, CustomComponent parent) {
        super(setting, parent);
        ArrayList enabled = new ArrayList();
        setting.getValues().forEach(sel -> {
            if (sel.isSelected()) {
                enabled.add(sel);
            }
        });
        setting.getSelectedValues().clear();
        setting.getSelectedValues().addAll(enabled);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (!this.initialized) {
            for (SelectSetting.Value value : ((SelectSetting)this.setting).getValues()) {
                value.setEnablePenis(new PenisPlayer(Rockstar.id("penises/check_enable.penis")));
                value.setDisablePenis(new PenisPlayer(Rockstar.id("penises/check_disable.penis")));
                value.setLastState(value.isSelected());
                value.setCurrentPenis(value.isLastState() ? value.getEnablePenis() : value.getDisablePenis());
                if (value.isLastState()) {
                    value.getEnablePenis().playOnce();
                    continue;
                }
                value.getDisablePenis().setFrame(0);
                value.getDisablePenis().stop();
            }
            this.initialized = true;
        }
        float x = this.x + 9.0f;
        float y = this.y + 1.0f;
        float width = this.width - 18.0f;
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        float leftPadding = 10.0f;
        float nameHeight = Fonts.REGULAR.getFont(7.0f).height();
        float headerHeight = 19.0f;
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        String rightText = String.format(" %s", Localizator.translate("setting_of") + " " + ((SelectSetting)this.setting).getValues().size());
        if (this.numberAnim == null) {
            this.numberAnim = new AnimatedNumber(Fonts.MEDIUM.getFont(7.0f), 5.0f, 500L, Easing.BAKEK);
        }
        context.drawFadeoutText(nameFont, Localizator.translate(((SelectSetting)this.setting).getName()), this.x + leftPadding, y - 1.0f + GuiUtility.getMiddleOfBox(nameFont.height(), headerHeight), Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())), 0.8f, 1.0f, this.getParent().getWidth() - leftPadding - Fonts.REGULAR.getFont(7.0f).width(rightText) - this.numberAnim.getWidth() - 10.0f);
        this.numberAnim.settings(false, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())));
        this.numberAnim.update(((SelectSetting)this.setting).getSelectedValues().size());
        this.numberAnim.pos(x + width - Fonts.REGULAR.getFont(7.0f).width(rightText) - this.numberAnim.getWidth(), y - 1.0f + GuiUtility.getMiddleOfBox(nameHeight, headerHeight));
        this.numberAnim.render(context);
        context.drawRightText(Fonts.REGULAR.getFont(7.0f), rightText, x + width, y - 1.0f + GuiUtility.getMiddleOfBox(nameHeight, headerHeight), Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())));
        context.drawRoundedRect(x - 1.0f, y + 17.0f, width + 2.0f, (float)(8 + ((SelectSetting)this.setting).getValues().size() * 12), BorderRadius.all(6.0f), Colors.getBackgroundColor().mulAlpha(0.3f));
        float offset = 0.0f;
        for (SelectSetting.Value value : ((SelectSetting)this.setting).getValues()) {
            if (value.isHidden()) continue;
            boolean currentState = value.isSelected();
            if (currentState != value.isLastState()) {
                if (currentState) {
                    value.setCurrentPenis(value.getEnablePenis());
                } else {
                    value.setCurrentPenis(value.getDisablePenis());
                }
                value.getCurrentPenis().playOnce();
                value.setLastState(currentState);
            }
            value.getCurrentPenis().update();
            float elmtY = this.dragging == value ? Math.clamp((float)(context.getMouseY() - 2), y + 18.0f, y + 20.0f + (float)(((SelectSetting)this.setting).getValues().size() * 12)) : y + 24.0f + offset;
            boolean hover = GuiUtility.isHovered((double)(x - 1.0f), (double)(elmtY - 4.0f), (double)(width + 2.0f), 12.0, context.getMouseX(), context.getMouseY());
            value.getYAnim().setEasing(Easing.BAKEK_SMALLER);
            value.getYAnim().update(elmtY - y);
            value.setYFactor(elmtY);
            if (hover && this.dragging != value && !value.isAlwaysEnabled()) {
                CursorUtility.set(CursorType.HAND);
            }
            value.getHoverAnimation().update(hover);
            value.getActiveAnimation().update(value.isSelected());
            if (((SelectSetting)this.setting).isDraggable()) {
                context.drawTexture(Rockstar.id("icons/hud/drag.png"), x + 7.0f, y + value.getYAnim().getValue(), 6.0f, 6.0f, Colors.getTextColor());
            }
            if (GuiUtility.isHovered(x, elmtY - 2.0f, 17.0, 10.0, context) || value == this.dragging) {
                CursorUtility.set(CursorType.ARROW_VERTICAL);
            }
            context.drawFadeoutText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(value.getName()), x + (float)(((SelectSetting)this.setting).isDraggable() ? 18 : 7), y + value.getYAnim().getValue() + 0.5f, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * value.getHoverAnimation().getValue() + 0.25f * value.getActiveAnimation().getValue())), 0.8f, 1.0f, width - 12.0f - value.getActiveAnimation().getValue() * 10.0f);
            if (value.getActiveAnimation().getValue() > 0.0f || value.getCurrentPenis().isPlaying()) {
                DrawUtility.drawAnimationSprite(context.getMatrices(), value.getCurrentPenis().getCurrentSprite(), x + width - 11.0f - value.getActiveAnimation().getValue() * 2.0f, y + value.getYAnim().getValue(), 6.0f, 6.0f, Colors.getTextColor().mulAlpha(0.1f + 0.9f * value.getActiveAnimation().getValue()));
            }
            offset += 12.0f;
        }
        if (this.sortTimer.finished(100L)) {
            ((SelectSetting)this.setting).getValues().sort(Comparator.comparingDouble(SelectSetting.Value::getYFactor));
            this.sortTimer.reset();
        }
    }

    @Override
    public void drawSplit(UIContext context) {
        float separatorHeight = 0.5f;
        context.drawRect(this.x, this.y + this.height, this.width, separatorHeight, Colors.getTextColor().withAlpha(5.1f));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (button != MouseButton.LEFT) {
            return;
        }
        float x = this.x + 9.0f;
        float y = this.y + 1.0f;
        float offset = 0.0f;
        for (SelectSetting.Value value : ((SelectSetting)this.setting).getValues()) {
            if (value.isHidden()) continue;
            boolean hover = GuiUtility.isHovered((double)(x - 1.0f), (double)(y + 20.0f + offset), (double)(this.width - 2.0f), 12.0, mouseX, mouseY);
            if (GuiUtility.isHovered((double)x, (double)(y + 22.0f + offset), 17.0, 10.0, mouseX, mouseY) && ((SelectSetting)this.setting).isDraggable()) {
                this.dragging = value;
            } else if (hover) {
                value.toggle();
            }
            offset += 12.0f;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.dragging = null;
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public float getHeight() {
        this.height = 31 + ((SelectSetting)this.setting).getValues().size() * 12;
        return this.height;
    }
}

