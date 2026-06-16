package im.zov4ik.utils.features.aura.rotations.neyro;

import im.zov4ik.events.player.TickEvent;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.zov4ik;

public class NeyroRecordHandler implements QuickImports {
    public static final NeyroRecordHandler INSTANCE = new NeyroRecordHandler();

    public void init() {
        zov4ik.getInstance().getEventManager().register(this);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        NeyroManager manager = NeyroManager.INSTANCE;
        if (manager.isRecording()) {
            manager.performTrainingAttack();
            manager.recordFrame();
        }
    }
}
