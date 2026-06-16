/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.modules.modules.player;

import lombok.Generated;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SelectSetting;

@ModuleInfo(name="No Push", category=ModuleCategory.PLAYER, desc="\u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u043a\u043e\u043b\u043b\u0438\u0437\u0438\u044e \u043e\u0442 \u0432\u043d\u0435\u0448\u043d\u0438\u0445 \u0444\u0430\u043a\u0442\u043e\u0440\u043e\u0432")
public class NoPush
extends BaseModule {
    private final SelectSetting removePushFrom = new SelectSetting(this, "\u041e\u0442\u043a\u043b\u044e\u0447\u0430\u0442\u044c \u0434\u043b\u044f");
    private final SelectSetting.Value entities = new SelectSetting.Value(this.removePushFrom, "\u042d\u043d\u0442\u0438\u0442\u0438", "\u041f\u0440\u0435\u0434\u043e\u0442\u0432\u0440\u0430\u0449\u0430\u0435\u0442 \u043e\u0442\u0442\u0430\u043b\u043a\u0438\u0432\u0430\u043d\u0438\u0435 \u043e\u0442 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0435\u0439").select();
    private final SelectSetting.Value fluids = new SelectSetting.Value(this.removePushFrom, "\u0412\u043e\u0434\u044b \u0438 \u043b\u0430\u0432\u044b", "\u041f\u0440\u0435\u0434\u043e\u0442\u0432\u0440\u0430\u0449\u0430\u0435\u0442 \u0432\u044b\u0442\u0430\u043b\u043a\u0438\u0432\u0430\u043d\u0438\u0435 \u0438\u0437 \u0432\u043e\u0434\u044b \u0438 \u043b\u0430\u0432\u044b");
    private final SelectSetting.Value blocks = new SelectSetting.Value(this.removePushFrom, "\u0411\u043b\u043e\u043a\u043e\u0432", "\u041f\u0440\u0435\u0434\u043e\u0442\u0432\u0440\u0430\u0449\u0430\u0435\u0442 \u043e\u0442\u0442\u0430\u043b\u043a\u0438\u0432\u0430\u043d\u0438\u0435 \u0438\u0437 \u0431\u043b\u043e\u043a\u043e\u0432").select();

    @Generated
    public SelectSetting getRemovePushFrom() {
        return this.removePushFrom;
    }

    @Generated
    public SelectSetting.Value getEntities() {
        return this.entities;
    }

    @Generated
    public SelectSetting.Value getFluids() {
        return this.fluids;
    }

    @Generated
    public SelectSetting.Value getBlocks() {
        return this.blocks;
    }
}

