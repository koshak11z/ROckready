package im.zov4ik.features.module.setting.implement;

import java.util.function.Supplier;

public class ValueSetting extends SliderSettings {
    public ValueSetting(String name, String description) {
        super(name, description);
    }

    public ValueSetting setValue(float value) {
        super.setValue(value);
        return this;
    }

    @Override
    public ValueSetting range(float min, float max) {
        super.range(min, max);
        return this;
    }

    @Override
    public ValueSetting range(int min, int max) {
        super.range(min, max);
        return this;
    }

    @Override
    public ValueSetting visible(Supplier<Boolean> visible) {
        super.visible(visible);
        return this;
    }
}
