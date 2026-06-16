/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.hud.impl.island.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.modules.player.Blink;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.IslandStatus;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;

public class BlinkStatus
extends IslandStatus {
    public BlinkStatus(SelectSetting setting) {
        super(setting, "blink");
    }

    @Override
    public void draw(CustomDrawContext context) {
        DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
        Blink blink = this.blink();
        Font font = Fonts.MEDIUM.getFont(7.0f);
        float x = sr.getScaledWidth() / 2.0f - island.getSize().width / 2.0f;
        float y = 7.0f;
        this.size.width = 80.0f;
        float width = 80.0f;
        this.size.height = 15.0f;
        float height = 15.0f;
        context.drawText(font, "Blink", x - 4.0f + 10.0f * this.animation.getValue(), y + 5.0f, Colors.getTextColor());
        if (!blink.getPulse().isEnabled()) {
            context.drawRightText(font, TextUtility.formatNumber((float)blink.getTimer().getElapsedTime() / 1000.0f) + " \u0441\u0435\u043a", x + width + 4.0f - 10.0f * this.animation.getValue(), y + 5.0f, Colors.getTextColor());
        } else {
            float blinkWidth = width - font.width("Blink") - 14.0f;
            float progress = blinkWidth * ((blink.getTime().getCurrentValue() * 50.0f - (float)blink.getTimer().getElapsedTime()) / (blink.getTime().getCurrentValue() * 50.0f));
            context.drawRoundedRect(x + width - 5.0f - blinkWidth, y + 4.5f, blinkWidth, 6.0f, BorderRadius.all(2.5f), Colors.getAdditionalColor());
            context.drawRoundedRect(x + width - 5.0f - progress, y + 4.5f, progress, 6.0f, BorderRadius.all(2.5f), Colors.ACCENT);
        }
    }

    @Override
    public boolean canShow() {
        return this.blink().isEnabled();
    }

    private Blink blink() {
        return Rockstar.getInstance().getModuleManager().getModule(Blink.class);
    }
}

