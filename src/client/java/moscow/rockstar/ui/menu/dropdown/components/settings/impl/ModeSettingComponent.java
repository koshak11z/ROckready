/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.penis.PenisPlayer;

public class ModeSettingComponent
extends MenuSettingComponent<ModeSetting> {
    private boolean initialized;

    public ModeSettingComponent(ModeSetting setting, CustomComponent parent) {
        super(setting, parent);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (!this.initialized) {
            for (ModeSetting.Value value : ((ModeSetting)this.setting).getValues()) {
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
        float headerHeight = 19.0f;
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        context.drawFadeoutText(nameFont, Localizator.translate(((ModeSetting)this.getSetting()).getName()), this.x + leftPadding, y - 1.0f + GuiUtility.getMiddleOfBox(nameFont.height(), headerHeight), Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getValue())), 0.8f, 1.0f, this.getParent().getWidth() - leftPadding);
        context.drawRoundedRect(x - 1.0f, y + 17.0f, width + 2.0f, (float)(8 + ((ModeSetting)this.setting).getValues().size() * 12), BorderRadius.all(6.0f), Colors.getBackgroundColor().mulAlpha(0.3f));
        float offset = 0.0f;
        for (ModeSetting.Value value : ((ModeSetting)this.setting).getValues()) {
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
            boolean hover = GuiUtility.isHovered((double)(x - 1.0f), (double)(y + 20.0f + offset), (double)(width + 2.0f), 12.0, context.getMouseX(), context.getMouseY());
            if (hover) {
                CursorUtility.set(CursorType.HAND);
            }
            value.getHoverAnimation().update(hover);
            value.getActiveAnimation().update(value.isSelected());
            context.drawFadeoutText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(value.getName()), x + 7.0f, y + 24.5f + offset, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * value.getHoverAnimation().getValue() + 0.25f * value.getActiveAnimation().getValue())), 0.8f, 1.0f, width - 12.0f - value.getActiveAnimation().getValue() * 10.0f);
            if (value.getActiveAnimation().getValue() > 0.0f || value.getCurrentPenis().isPlaying()) {
                DrawUtility.drawAnimationSprite(context.getMatrices(), value.getCurrentPenis().getCurrentSprite(), x + width - 11.0f - value.getActiveAnimation().getValue() * 2.0f, y + 24.0f + offset, 6.0f, 6.0f, Colors.getTextColor().mulAlpha(0.1f + 0.9f * value.getActiveAnimation().getValue()));
            }
            offset += 12.0f;
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
        float offset = 0.0f;
        for (ModeSetting.Value value : ((ModeSetting)this.setting).getValues()) {
            if (value.isHidden()) continue;
            boolean hover = GuiUtility.isHovered((double)(this.x - 1.0f), (double)(this.y + 20.0f + offset), (double)(this.width - 2.0f), 12.0, mouseX, mouseY);
            if (hover) {
                value.select();
            }
            offset += 12.0f;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public float getHeight() {
        this.height = 31 + ((ModeSetting)this.setting).getValues().size() * 12;
        return this.height;
    }
}

