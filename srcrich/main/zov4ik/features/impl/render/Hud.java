package im.zov4ik.features.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.features.module.setting.implement.MultiSelectSetting;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.utils.client.Instance;

import java.awt.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Hud extends Module {
    public static Hud getInstance() {
        return Instance.get(Hud.class);
    }

    public MultiSelectSetting interfaceSettings = new MultiSelectSetting("Элементы", "Настройка элементов интерфейса").value("Watermark", "Hot Keys", "Potions", "Target Hud", "Player Info", "Notifications")
            .selected("Watermark", "Hot Keys", "Potions", "Target Hud", "Player Info", "Notifications");

    public MultiSelectSetting notificationSettings = new MultiSelectSetting("Уведомления", "Выберите, когда будут появляться уведомления")
            .value("Module Switch", "Staff Join", "Staff Leave", "Item Pick Up", "Auto Armor", "Break Shield").selected("Module Switch", "Item Pick Up", "Auto Armor", "Break Shield").visible(()-> interfaceSettings.isSelected("Notifications"));

    public ColorSetting colorSetting = new ColorSetting("Изменяет цвет некоторых модулей", "Выберите цвет клиента")
            .setColor(new Color(124, 92, 255, 255).getRGB()).presets(0xFF7C5CFF, 0xFF6C9AFD, 0xFFFF2436, 0xFFFFA576, 0xFFFF7B7B);

    public SliderSettings soundVolumeSetting = new SliderSettings("Громкость Звука", "Volume for module switch sounds")
            .range(0.0f, 1.0f)
            .setValue(1.0f)
            .visible(() -> interfaceSettings.isSelected("Notifications"));

    public float getModuleVolume() {
        return soundVolumeSetting.getValue();
    }

    public Hud() {
        super("Hud", "Интерфейс", ModuleCategory.RENDER);
        setup(colorSetting, interfaceSettings, notificationSettings, soundVolumeSetting);
    }
}
