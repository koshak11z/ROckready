package moscow.rockstar.systems.modules.modules.visuals;

import lombok.Generated;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.SliderSetting;

/**
 * Full Bright — раньше это была галочка «Включить яркость» внутри Ambience,
 * вынесена в отдельный модуль. Сила берётся из ползунка и читается миксином гаммы
 * ({@code SimpleOptionMixin}).
 */
@ModuleInfo(name = "Full Bright", category = ModuleCategory.VISUALS, desc = "modules.descriptions.full_bright")
public class FullBright extends BaseModule {
    private final SliderSetting level = new SliderSetting((SettingsContainer) this, "modules.settings.full_bright.level")
            .min(1.0f).max(100.0f).step(1.0f).currentValue(16.0f);

    /** Значение гаммы, которое миксин подставляет вместо ванильного. */
    public double getGamma() {
        return this.level.getCurrentValue();
    }

    @Generated
    public SliderSetting getLevel() {
        return this.level;
    }
}
