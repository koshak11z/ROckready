package im.zov4ik.features.impl.movement;

import antidaunleak.api.annotation.Native;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.impl.combat.Aura;
import im.zov4ik.features.impl.misc.ElytraHelper;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.math.time.StopWatch;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ElytraMotion extends Module {

    SliderSettings distanceSetting = new SliderSettings("Distance", "Distance to freeze near aura target")
            .range(1f, 3f)
            .setValue(1.5f);

    BooleanSetting autoFirework = new BooleanSetting("Auto Firework", "Automatically launches a firework while holding target")
            .setValue(true);

    StopWatch fireworkTimer = new StopWatch();

    public ElytraMotion() {
        super("ElytraMotion", "Elytra Motion", ModuleCategory.MOVEMENT);
        setup(distanceSetting, autoFirework);
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null || !mc.player.isGliding()) {
            return;
        }

        Aura aura = Instance.get(Aura.class);
        LivingEntity target = aura.getTarget();
        if (!aura.isState() || target == null) {
            return;
        }

        double eyeDistance = mc.player.getEyePos().distanceTo(target.getEyePos());
        if (eyeDistance > distanceSetting.getValue()) {
            return;
        }

        if (autoFirework.isValue() && fireworkTimer.finished(1000)) {
            Instance.get(ElytraHelper.class).fireWorkMethod();
            fireworkTimer.reset();
        }

        mc.player.setVelocity(Vec3d.ZERO);
    }
}
