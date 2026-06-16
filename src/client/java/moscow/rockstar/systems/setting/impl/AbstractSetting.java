/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.systems.setting.impl;

import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.setting.SettingsContainer;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSetting
implements Setting {
    private final String name;
    @NotNull
    private final BooleanSupplier hideCondition;

    public AbstractSetting(@NotNull SettingsContainer parent, String name, @NotNull BooleanSupplier hideCondition) {
        this.name = name;
        this.hideCondition = hideCondition;
        this.register(parent);
    }

    public AbstractSetting(@NotNull SettingsContainer parent, String name) {
        this(parent, name, () -> false);
    }

    @Override
    public void register(SettingsContainer parent) {
        parent.getSettings().add(this);
    }

    @Override
    public String getDescription() {
        return this.getName() + ".description";
    }

    @Override
    @Generated
    public String getName() {
        return this.name;
    }

    @Override
    @NotNull
    @Generated
    public BooleanSupplier getHideCondition() {
        return this.hideCondition;
    }
}

