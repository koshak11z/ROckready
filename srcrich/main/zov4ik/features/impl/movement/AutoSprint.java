package im.zov4ik.features.impl.movement;

import antidaunleak.api.annotation.Native;
import im.zov4ik.main.listener.impl.EventListener;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.math.time.TimerUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.effect.StatusEffects;
import im.zov4ik.features.module.setting.implement.MultiSelectSetting;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.text.Text;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoSprint extends Module {
    public static AutoSprint getInstance() {
        return Instance.get(AutoSprint.class);
    }

    public static int tickStop;

    MultiSelectSetting settings = new MultiSelectSetting("Игнорировать", "Не дает спринтиться при эффектах")
            .value("Slowness", "Blindness");

    public AutoSprint() {
        super("AutoSprint", "Auto Sprint", ModuleCategory.MOVEMENT);
        setup(settings);
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginMutation)
    public void onTick(TickEvent e) {
        boolean hasSlowness = mc.player.hasStatusEffect(StatusEffects.SLOWNESS);
        boolean hasBlindness = mc.player.hasStatusEffect(StatusEffects.BLINDNESS);

        boolean shouldCancelSprintDueToSlowness = hasSlowness && !settings.isSelected("Slowness");
        boolean shouldCancelSprintDueToBlindness = hasBlindness && !settings.isSelected("Blindness");

        boolean horizontal = mc.player.horizontalCollision && !mc.player.collidedSoftly;
        boolean sneaking = mc.player.isSneaking() && !mc.player.isSwimming();

        if (tickStop > 0 || sneaking || shouldCancelSprintDueToSlowness || shouldCancelSprintDueToBlindness) {
            mc.player.setSprinting(false);
        } else if (!horizontal && mc.player.forwardSpeed > 0 && !mc.options.sprintKey.isPressed()) {
            mc.player.setSprinting(true);
        }

        tickStop--;
    }


}