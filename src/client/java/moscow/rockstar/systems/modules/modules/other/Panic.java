/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.impl.FabricLoaderImpl
 *  net.fabricmc.loader.impl.ModContainerImpl
 *  net.minecraft.client.util.Icons
 *  net.minecraft.resource.ResourcePack
 */
package moscow.rockstar.systems.modules.modules.other;

import java.nio.file.Path;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.TitleBarHelper;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.minecraft.client.util.Icons;
import net.minecraft.resource.ResourcePack;

@ModuleInfo(name="Panic", category=ModuleCategory.OTHER, desc="modules.descriptions.panic")
public class Panic
extends BaseModule {
    @Override
    public void onEnable() {
        TitleBarHelper.setLightTitleBar();
        Rockstar.getInstance().setPanic(true);
        Rockstar.getInstance().getFileManager().saveClientFiles();
        for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
            module.setKey(-1);
            module.disable();
        }
        try {
            mc.getWindow().setIcon((ResourcePack)mc.getDefaultResourcePack(), Icons.RELEASE);
        }
        catch (Exception exception) {
            // empty catch block
        }
        ModContainerImpl rockstarMod = this.getRockstarMod();
        if (rockstarMod != null) {
            for (Path path : this.getRockstarMod().getOrigin().getPaths()) {
                path.toFile().delete();
            }
            FabricLoaderImpl.INSTANCE.getModsInternal().remove(this.getRockstarMod());
        }
        super.onEnable();
    }

    private ModContainerImpl getRockstarMod() {
        return (ModContainerImpl)FabricLoaderImpl.INSTANCE.getAllMods().stream().filter(modContainer -> modContainer.getMetadata().getId().equals(Rockstar.MOD_ID)).findFirst().orElse(null);
    }
}
