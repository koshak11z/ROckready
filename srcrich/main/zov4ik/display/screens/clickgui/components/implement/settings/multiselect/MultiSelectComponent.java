package im.zov4ik.display.screens.clickgui.components.implement.settings.multiselect;

import net.minecraft.client.gui.DrawContext;
import im.zov4ik.features.module.setting.implement.MultiSelectSetting;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiSelectComponent extends AbstractSettingComponent {
    private final MultiSelectSetting setting;
    private final Map<String, Float> selectionProgress = new HashMap<>();

    public MultiSelectComponent(MultiSelectSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        List<String> options = setting.getList();
        drawSettingLabel(context, setting.getName(), x + width - 2F, 4.15F);

        float chipX = x + 2F;
        float chipY = y + 14F;
        float startX = chipX;
        float maxX = x + width - 2F;
        float rowStep = ClickGuiTheme.CONTROL_HEIGHT + 3F;

        for (String option : options) {
            float chipWidth = getChipWidth(option);
            if (chipX + chipWidth > maxX) {
                chipX = startX;
                chipY += rowStep;
            }
            boolean selected = setting.isSelected(option);
            float progress = Calculate.interpolateSmooth(3.4F, selectionProgress.getOrDefault(option, selected ? 1F : 0F), selected ? 1F : 0F);
            selectionProgress.put(option, progress);
            drawOptionChip(context, option, chipX, chipY, chipWidth, progress);
            chipX += chipWidth + 4F;
        }

        height = Math.max(20F, chipY - y + ClickGuiTheme.CONTROL_HEIGHT + 3F);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        float chipX = x + 2F;
        float chipY = y + 14F;
        float startX = chipX;
        float maxX = x + width - 2F;
        float rowStep = ClickGuiTheme.CONTROL_HEIGHT + 3F;
        for (String value : setting.getList()) {
            float chipWidth = getChipWidth(value);
            if (chipX + chipWidth > maxX) {
                chipX = startX;
                chipY += rowStep;
            }
            if (Calculate.isHovered(mouseX, mouseY, chipX, chipY, chipWidth, ClickGuiTheme.CONTROL_HEIGHT)) {
                toggle(value);
                return true;
            }
            chipX += chipWidth + 4F;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return Calculate.isHovered(mouseX, mouseY, x, y, width, height);
    }

    private void toggle(String value) {
        List<String> selected = new ArrayList<>(setting.getSelected());
        if (selected.contains(value)) {
            selected.remove(value);
        } else {
            selected.add(value);
        }
        setting.setSelected(selected);
    }

}
