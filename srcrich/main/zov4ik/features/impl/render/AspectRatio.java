package im.zov4ik.features.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.events.render.AspectRatioEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AspectRatio extends Module {

    SliderSettings ratioSetting = new SliderSettings("Соотношение", "Настройка значения соотношения сторон")
            .setValue(1.0F).range(0.1F, 2.0F);


    public AspectRatio() {
        super("AspectRatio", "Aspect Ratio", ModuleCategory.RENDER);
        setup(ratioSetting);
    }

    @EventHandler
    public void onAspectRatio(AspectRatioEvent e) {
            e.setRatio(ratioSetting.getValue());
            e.cancel();
    }
}