package im.zov4ik.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;

import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.display.screens.clickgui.ClickGuiPainter;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.features.module.setting.implement.BooleanSetting;

public class CheckboxComponent extends AbstractSettingComponent {
    private final BooleanSetting setting;
    private float toggleProgress;

    public CheckboxComponent(BooleanSetting setting) {
        super(setting);
        this.setting = setting;
        toggleProgress = setting.isValue() ? 1F : 0F;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        height = 12;
        float badgeSize = ClickGuiTheme.STATE_BADGE_SIZE;
        float badgeX = x + width - badgeSize - 4.0F;
        toggleProgress = Calculate.interpolateSmooth(3.5F, toggleProgress, setting.isValue() ? 1F : 0F);
        drawSettingLabel(context, setting.getName(), badgeX - 5F, 3.95F);

        ClickGuiPainter.drawStateBadge(context, badgeX, y + height / 2F - badgeSize / 2F + 0.2F, badgeSize, toggleProgress);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && Calculate.isHovered(mouseX, mouseY, x, y, width, height)) {
            setting.setValue(!setting.isValue());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
