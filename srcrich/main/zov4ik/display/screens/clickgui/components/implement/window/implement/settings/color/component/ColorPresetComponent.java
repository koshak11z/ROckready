package im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.component;

import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.ColorPresetButton;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.utils.display.font.Fonts;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ColorPresetComponent extends AbstractComponent {
    private final List<ColorPresetButton> colorPresetButtonList = new ArrayList<>();
    private final ColorSetting setting;
    private float windowHeight;

    public ColorPresetComponent(ColorSetting setting) {
        this.setting = setting;
        for (int preset : setting.getPresets()) {
            colorPresetButtonList.add(new ColorPresetButton(setting, preset));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float titleY = y + 123F;
        float startX = x + 8F;
        float startY = y + 136F;
        int buttonSize = 10;
        int gap = 6;
        int perRow = 6;

        if (!colorPresetButtonList.isEmpty()) {
            Fonts.getSize(11, Fonts.Type.MANROPEBOLD)
                    .drawString(context.getMatrices(), "\u0413\u043e\u0442\u043e\u0432\u044b\u0435 \u0446\u0432\u0435\u0442\u0430", startX, titleY, ClickGuiTheme.TEXT_PRIMARY);
        }

        int xOffset = 0;
        int yOffset = 0;
        int colorIndex = 0;

        for (ColorPresetButton button : colorPresetButtonList) {
            button.x = startX + xOffset;
            button.y = startY + yOffset;
            button.render(context, mouseX, mouseY, delta);

            xOffset += buttonSize + gap;
            colorIndex++;

            if (colorIndex >= perRow) {
                colorIndex = 0;
                xOffset = 0;
                yOffset += buttonSize + gap;
            }
        }

        if (colorPresetButtonList.isEmpty()) {
            windowHeight = 121F;
            return;
        }

        int rows = (int) Math.ceil(colorPresetButtonList.size() / (float) perRow);
        windowHeight = startY - y + rows * buttonSize + Math.max(0, rows - 1) * gap + 10F;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        colorPresetButtonList.forEach(colorPresetButton -> colorPresetButton.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
