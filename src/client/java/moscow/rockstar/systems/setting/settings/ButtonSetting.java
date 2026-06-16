/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonPrimitive
 *  lombok.Generated
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.systems.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.impl.AbstractSetting;
import org.jetbrains.annotations.NotNull;

public class ButtonSetting
extends AbstractSetting {
    private Runnable action = System.out::println;

    public ButtonSetting(@NotNull SettingsContainer parent, String name, String description, @NotNull BooleanSupplier hideCondition) {
        super(parent, name, hideCondition);
    }

    public ButtonSetting(@NotNull SettingsContainer parent, String name, @NotNull BooleanSupplier hideCondition) {
        super(parent, name, hideCondition);
    }

    public ButtonSetting(@NotNull SettingsContainer parent, String name, String description) {
        super(parent, name);
    }

    public ButtonSetting(@NotNull SettingsContainer parent, String name) {
        super(parent, name);
    }

    public ButtonSetting action(Runnable action) {
        this.action = action;
        return this;
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive("\u0441\u0443\u043a\u0430 \u043a\u0430\u043a \u0441\u0434\u0435\u043b\u0430\u0442\u044c \u0442\u0430\u043a \u0447\u0442\u043e\u0431\u044b \u0434\u043b\u044f \u043d\u0435\u0433\u043e \u043d\u0435 \u0431\u044b\u043b\u043e \u043a\u0444\u0433");
    }

    @Override
    public void load(JsonElement element) {
    }

    @Generated
    public Runnable getAction() {
        return this.action;
    }

    @Generated
    public void setAction(Runnable action) {
        this.action = action;
    }
}

