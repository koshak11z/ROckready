/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.modules;

import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.interfaces.Toggleable;

public interface Module
extends Toggleable,
IMinecraft,
IScaledResolution,
SettingsContainer {
    public void disable();

    public void enable();

    public void tick();

    public ModuleInfo getInfo();

    public String getName();

    default public String getDescription() {
        String translationKey = "modules.descriptions.%s".formatted(this.getName().toLowerCase().replace(" ", "_"));
        return Localizator.translate(translationKey);
    }

    public int getKey();

    public ModuleCategory getCategory();

    public boolean isEnabled();

    public boolean isHidden();

    public Animation getKeybindsAnimation();

    public void setKey(int var1);

    public void setEnabled(boolean var1, boolean var2);
}

