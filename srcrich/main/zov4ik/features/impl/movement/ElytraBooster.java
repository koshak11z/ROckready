package im.zov4ik.features.impl.movement;

import antidaunleak.api.annotation.Native;
import im.zov4ik.events.player.FireworkEvent;
import im.zov4ik.features.impl.combat.Aura;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.Setting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.managers.event.EventHandler;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ElytraBooster extends Module {
    SliderSettings[] yawBoost = new SliderSettings[9];
    SliderSettings[] pitchBoost = new SliderSettings[9];

    public ElytraBooster() {
        super("ElytraBooster", "Elytra Booster", ModuleCategory.MOVEMENT);

        String[] ranges = {
                "0-5", "5-10", "10-15", "15-20", "20-25",
                "25-30", "30-35", "35-40", "40-45"
        };

        Setting[] settings = new Setting[yawBoost.length + pitchBoost.length];

        for (int i = 0; i < yawBoost.length; i++) {
            yawBoost[i] = new SliderSettings("Yaw " + ranges[i], "Horizontal firework boost in this yaw range")
                    .range(1.5f, 2.5f)
                    .setValue(1.5f);
            settings[i] = yawBoost[i];
        }

        for (int i = 0; i < pitchBoost.length; i++) {
            pitchBoost[i] = new SliderSettings("Pitch " + ranges[i], "Vertical firework boost in this pitch range")
                    .range(1.5f, 2.5f)
                    .setValue(1.5f);
            settings[i + yawBoost.length] = pitchBoost[i];
        }

        setup(settings);
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onFirework(FireworkEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        float yawRange = normalizeToBoosterRange(MathHelper.wrapDegrees(mc.player.getYaw()));
        float pitchRange = normalizeToBoosterRange(Math.abs(mc.player.getPitch()));

        float horizontalBoost = getYawBoost(yawRange);
        float verticalBoost = getPitchBoost(pitchRange);
        if (verticalBoost > horizontalBoost) {
            horizontalBoost = verticalBoost;
        }

        Aura aura = Instance.get(Aura.class);
        ElytraTarget elytraTarget = Instance.get(ElytraTarget.class);
        LivingEntity target = aura.getTarget();

        if (aura.isState() && elytraTarget.isState() && target != null && !target.isGliding()) {
            horizontalBoost = 1.52f;
            verticalBoost = 1.52f;

            float pitch = mc.player.getPitch();
            if (pitch > 89.0f && pitch < 90.0f) {
                verticalBoost = 2.3f;
            }
        }

        Vec3d vector = event.getVector();
        event.setVector(new Vec3d(
                vector.x * horizontalBoost,
                vector.y * verticalBoost,
                vector.z * horizontalBoost
        ));
    }

    private float getYawBoost(float angle) {
        int index = (int) (angle / 5.0f);
        if (index >= yawBoost.length) {
            index = yawBoost.length - 1;
        }
        return yawBoost[index].getValue();
    }

    private float getPitchBoost(float angle) {
        int index = (int) (angle / 5.0f);
        if (index >= pitchBoost.length) {
            index = pitchBoost.length - 1;
        }
        return pitchBoost[index].getValue();
    }

    private float normalizeToBoosterRange(float angle) {
        float normalized = Math.abs(angle);
        if (normalized > 90.0f) {
            normalized = 180.0f - normalized;
        }
        if (normalized > 45.0f) {
            normalized = 90.0f - normalized;
        }
        return normalized;
    }
}
