/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.modules.modules.other;

import lombok.Generated;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.StringSetting;
import moscow.rockstar.utility.game.EntityUtility;

@ModuleInfo(name="Name Protect", category=ModuleCategory.OTHER, desc="\u0412\u0438\u0437\u0443\u0430\u043b\u044c\u043d\u043e \u0441\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u043d\u0438\u043a \u0438\u0433\u0440\u043e\u043a\u0430")
public class NameProtect
extends BaseModule {
    private final StringSetting fakeName = new StringSetting(this, "modules.settings.name_protect.fake_name").text("Player");

    public String patchName(String text) {
        String clientUsername = mc.getSession().getUsername();
        if (EntityUtility.isInGame()) {
            text = text.replace(NameProtect.mc.player.getDisplayName().getString(), this.fakeName.getText());
        }
        return text.replace(clientUsername, this.fakeName.getText());
    }

    @Generated
    public StringSetting getFakeName() {
        return this.fakeName;
    }
}

