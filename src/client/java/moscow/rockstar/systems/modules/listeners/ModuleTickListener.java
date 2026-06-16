/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.modules.listeners;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.Module;

public class ModuleTickListener
implements EventListener<ClientPlayerTickEvent> {
    @Override
    public void onEvent(ClientPlayerTickEvent event) {
        for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
            if (!module.isEnabled()) continue;
            module.tick();
        }
    }
}

