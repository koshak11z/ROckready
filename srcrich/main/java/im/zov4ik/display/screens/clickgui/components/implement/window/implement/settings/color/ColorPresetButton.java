package im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color;

import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;

@RequiredArgsConstructor
public class ColorPresetButton extends AbstractComponent {
    private final ColorSetting setting;
    private final int color;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, 10, 10)
                .round(3F)
                .thickness(1F)
                .outlineColor(ClickGuiTheme.CONTROL_OUTLINE)
                .color(color)
                .build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, 10, 10) && button == 0) {
            setting.setColor(color);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
