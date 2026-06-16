package im.zov4ik.features.impl.render;

import im.zov4ik.events.render.WorldRenderEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.display.geometry.Render3D;

public class SelfOrbit extends Module {
    private final SliderSettings speed = new SliderSettings("Speed", "Orbit speed").setValue(1.3F).range(1.0F, 3.0F);

    public SelfOrbit() {
        super("SelfOrbit", ModuleCategory.RENDER);
        setup(speed);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        Render3D.drawGhosts(mc.player, 0.45F, 0.0F, speed.getValue());
    }
}
