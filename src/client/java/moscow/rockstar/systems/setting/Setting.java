/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 */
package moscow.rockstar.systems.setting;

import com.google.gson.JsonElement;
import java.util.function.BooleanSupplier;
import moscow.rockstar.systems.setting.SettingsContainer;

public interface Setting {
    public String getName();

    public String getDescription();

    public BooleanSupplier getHideCondition();

    public void register(SettingsContainer var1);

    default public boolean isVisible() {
        return !this.getHideCondition().getAsBoolean();
    }

    public JsonElement save();

    public void load(JsonElement var1);
}

