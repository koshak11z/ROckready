package im.zov4ik.display.screens.clickgui.components.implement.settings.select;

import net.minecraft.client.gui.DrawContext;

import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectComponent extends AbstractSettingComponent {
    private final SelectSetting setting;
    private final Map<String, Float> selectionProgress = new HashMap<>();

    public SelectComponent(SelectSetting setting) {
        super(setting);
        this.setting = setting;
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        List<String> options = setting.getList();
        drawSettingLabel(context, setting.getName(), x + width - 2F, 3.6F);

        float chipX = x + 2F;
        float chipY = y + 12.5F;
        float startX = chipX;
        float maxX = x + width - 2F;
        float rowStep = ClickGuiTheme.CONTROL_HEIGHT + 2.5F;

        for (String value : options) {
            float chipWidth = getChipWidth(value);
            if (chipX + chipWidth > maxX) {
                chipX = startX;
                chipY += rowStep;
            }
            boolean selected = setting.isSelected(value);
            float progress = Calculate.interpolateSmooth(3.4F, selectionProgress.getOrDefault(value, selected ? 1F : 0F), selected ? 1F : 0F);
            selectionProgress.put(value, progress);
            drawOptionChip(context, value, chipX, chipY, chipWidth, progress);
            chipX += chipWidth + 3F;
        }

        height = Math.max(18F, chipY - y + ClickGuiTheme.CONTROL_HEIGHT + 2.5F);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        float chipX = x + 2F;
        float chipY = y + 12.5F;
        float startX = chipX;
        float maxX = x + width - 2F;
        float rowStep = ClickGuiTheme.CONTROL_HEIGHT + 2.5F;
        for (String value : setting.getList()) {
            float chipWidth = getChipWidth(value);
            if (chipX + chipWidth > maxX) {
                chipX = startX;
                chipY += rowStep;
            }
            if (Calculate.isHovered(mouseX, mouseY, chipX, chipY, chipWidth, ClickGuiTheme.CONTROL_HEIGHT)) {
                setting.setSelected(value);
                return true;
            }
            chipX += chipWidth + 3F;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return Calculate.isHovered(mouseX, mouseY, x, y, width, height);
    }
}
