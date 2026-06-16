package im.zov4ik.features.impl.player;

import im.zov4ik.utils.features.aura.warp.Turns;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BindSetting;
import im.zov4ik.events.keyboard.KeyEvent;
import im.zov4ik.events.keyboard.MouseRotationEvent;
import im.zov4ik.events.render.CameraEvent;
import im.zov4ik.events.render.FovEvent;
import im.zov4ik.utils.features.aura.utils.MathAngle;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class FreeLook extends Module {

    Perspective perspective;
    Turns angle;
    public static BindSetting freeLookSetting = new BindSetting("Свободный обзор", "Клавиша для свободного обзора");

    public FreeLook() {
        super("FreeLook", "Free Look", ModuleCategory.RENDER);
        setup(freeLookSetting);
        angle = null;
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (e.isKeyDown(freeLookSetting.getKey())) {
            perspective = mc.options.getPerspective();
            if (angle == null) {
                angle = MathAngle.cameraAngle();
            }
        }
    }

    @EventHandler
    public void onFov(FovEvent e) {
        if (PlayerInteractionHelper.isKey(freeLookSetting)) {
            if (mc.options.getPerspective().isFirstPerson()) mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            if (angle == null) {
                angle = MathAngle.cameraAngle();
            }
        } else if (perspective != null) {
            mc.options.setPerspective(perspective);
            perspective = null;
            angle = null;
        }
    }

    @EventHandler
    public void onMouseRotation(MouseRotationEvent e) {
        if (PlayerInteractionHelper.isKey(freeLookSetting)) {
            if (angle == null) {
                angle = MathAngle.cameraAngle();
            }
            angle.setYaw(angle.getYaw() + e.getCursorDeltaX() * 0.15F);
            angle.setPitch(MathHelper.clamp(angle.getPitch() + e.getCursorDeltaY() * 0.15F, -90F, 90F));
            e.cancel();
        } else {
            angle = null;
        }
    }

    @EventHandler
    public void onCamera(CameraEvent e) {
        if (PlayerInteractionHelper.isKey(freeLookSetting) && angle != null) {
            e.setAngle(angle);
            e.cancel();
        }
    }
}