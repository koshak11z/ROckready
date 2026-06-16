package im.zov4ik.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;

import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.features.module.setting.implement.ButtonSetting;

public class SButtonComponent extends AbstractSettingComponent {
    private final ButtonSetting setting;

    public SButtonComponent(ButtonSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        height = 22;
        float buttonWidth = getAdaptiveControlWidth("Run");
        float buttonX = x + width - buttonWidth - ClickGuiTheme.CONTROL_MARGIN;
        drawSettingLabel(context, setting.getName(), buttonX);
        drawControlSurface(context, buttonX, y + 2.4F, buttonWidth, false);
        drawControlText(context, "Run", buttonX, y + 2.4F, buttonWidth);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float buttonWidth = getAdaptiveControlWidth("Run");
        float buttonX = x + width - buttonWidth - ClickGuiTheme.CONTROL_MARGIN;
        if (button == 0 && Calculate.isHovered(mouseX, mouseY, buttonX, y + 2.4F, buttonWidth, ClickGuiTheme.CONTROL_HEIGHT)) {
            setting.getRunnable().run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
