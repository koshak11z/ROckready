/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.modules.constructions.swinganim;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.setting.SettingsContainer;

public class SwingSettings
implements SettingsContainer {
    protected final List<Setting> settings = new ArrayList<Setting>();

    @Override
    public List<Setting> getSettings() {
        return this.settings;
    }
}

