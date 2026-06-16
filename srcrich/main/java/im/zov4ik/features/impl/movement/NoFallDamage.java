package im.zov4ik.features.impl.movement;

import antidaunleak.api.annotation.Native;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.utils.math.projection.Projection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.utils.client.managers.event.EventHandler;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoFallDamage extends Module {

    SelectSetting mode = new SelectSetting("Режим", "Выберите тип")
            .value("SpookyTime")
            .selected("SpookyTime");

    public NoFallDamage() {
        super("NoFallDamage", "No Fall Damage", ModuleCategory.MOVEMENT);
        setup(mode);
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onPacket(PacketEvent e) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.fallDistance > 0 && Projection.getDistanceToGround() >4) {
            mc.player.setVelocity(0, 0, 0);
        }
    }
}
