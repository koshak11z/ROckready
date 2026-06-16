/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.hud.impl.island.impl;

import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.TimerStatus;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.server.ServerUtility;

public class PVPStatus
extends TimerStatus {
    public PVPStatus(SelectSetting setting) {
        super(setting, "pvp");
    }

    @Override
    public void draw(CustomDrawContext context) {
        this.update("s", ServerUtility.ctTime, "\u0412\u044b \u0432 PVP \u0440\u0435\u0436\u0438\u043c\u0435", new ColorRGBA(185.0f, 28.0f, 28.0f));
        super.draw(context);
    }

    @Override
    public boolean canShow() {
        return ServerUtility.hasCT;
    }
}

