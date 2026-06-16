/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.hud.impl.island.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.IslandStatus;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;

public class DefaultStatus
extends IslandStatus {
    public DefaultStatus(SelectSetting setting) {
        super(setting, "default");
    }

    @Override
    public void draw(CustomDrawContext context) {
        DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
        float x = sr.getScaledWidth() / 2.0f - island.getSize().width / 2.0f;
        float y = 7.0f;
        float width = this.size.width = 20.0f + Fonts.MEDIUM.getFont(7.0f).width("Rockstar");
        this.size.height = 15.0f;
        float height = 15.0f;
        context.drawRoundedRect(x - 6.0f + 10.0f * this.animation.getValue(), y + 4.0f, 7.0f, 7.0f, BorderRadius.all(3.0f), Colors.getLogoBackgroundColor());
        context.drawText(Fonts.MEDIUM.getFont(7.0f), "Enfurix", x + 25.0f - 10.0f * this.animation.getValue(), y + 5.0f, Colors.getLogoTextColor());
    }

    @Override
    public boolean canShow() {
        return true;
    }
}

