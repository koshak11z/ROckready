/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.modules.modules.movement;

import lombok.Generated;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;

@ModuleInfo(name="Timer", category=ModuleCategory.MOVEMENT, desc="modules.descriptions.timer")
public class Timer
extends BaseModule {
    private final SliderSetting speed = new SliderSetting(this, "modules.settings.timer.speed").step(0.1f).min(0.1f).max(15.0f).currentValue(1.0f);

    @Override
    public void tick() {
        EntityUtility.setTimer(this.speed.getCurrentValue());
        super.tick();
    }

    @Override
    public void onDisable() {
        EntityUtility.resetTimer();
        super.onDisable();
    }

    @Generated
    public SliderSetting getSpeed() {
        return this.speed;
    }
}

