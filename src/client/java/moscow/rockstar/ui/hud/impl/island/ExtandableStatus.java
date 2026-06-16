/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.hud.impl.island;

import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.IslandStatus;

public class ExtandableStatus
extends IslandStatus {
    public ExtandableStatus(SelectSetting setting, String name) {
        super(setting, name);
    }

    @Override
    public boolean canShow() {
        return false;
    }
}

