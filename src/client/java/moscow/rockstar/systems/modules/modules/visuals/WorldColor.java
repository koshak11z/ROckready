package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IScaledResolution;

/**
 * WorldColor — заливает мир выбранным цветом (полноэкранный полупрозрачный оверлей).
 * Цвет синхронизируется с темой, если включена кастомная тема.
 */
@ModuleInfo(name = "WorldColor", category = ModuleCategory.VISUALS, desc = "modules.descriptions.worldcolor")
public class WorldColor extends BaseModule {
    private final ColorSetting color = new ColorSetting(this, "color").color(Colors.ACCENT);
    private final SliderSetting opacity = new SliderSetting(this, "modules.settings.worldcolor.opacity")
            .min(0.0f).max(1.0f).step(0.01f).currentValue(0.22f);
    private final BooleanSetting ignoreScreens = new BooleanSetting(this, "modules.settings.worldcolor.ignore_screens").enable();

    private final EventListener<HudRenderEvent> onHud = event -> {
        if (mc.world == null || mc.player == null) {
            return;
        }
        // По умолчанию не заливаем поверх открытых меню (инвентарь, чат и т.д.).
        if (this.ignoreScreens.isEnabled() && mc.currentScreen != null) {
            return;
        }
        UIContext context = UIContext.of(event.getContext(), -1, -1, event.getTickDelta());
        float w = IScaledResolution.sr.getScaledWidth();
        float h = IScaledResolution.sr.getScaledHeight();
        ColorRGBA drawColor = Rockstar.getInstance().getThemeManager().isCustomTheme()
                ? Colors.getWorldColor()
                : this.color.getColor();
        context.drawRect(0.0f, 0.0f, w, h, drawColor.withAlpha(255.0f * this.opacity.getCurrentValue()));
    };
}
