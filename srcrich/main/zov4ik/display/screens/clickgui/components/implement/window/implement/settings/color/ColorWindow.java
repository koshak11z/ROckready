package im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color;

import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.display.screens.clickgui.components.implement.window.AbstractWindow;
import im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.component.AlphaComponent;
import im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.component.ColorEditorComponent;
import im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.component.ColorPresetComponent;
import im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.component.HueComponent;
import im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.component.SaturationComponent;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorWindow extends AbstractWindow {
    private final List<AbstractComponent> components = new ArrayList<>();
    private final ColorPresetComponent colorPresetComponent;

    public ColorWindow(ColorSetting setting) {
        components.addAll(Arrays.asList(
                new HueComponent(setting),
                new SaturationComponent(setting),
                new AlphaComponent(setting),
                new ColorEditorComponent(setting),
                colorPresetComponent = new ColorPresetComponent(setting)
        ));
    }

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        width = 124F;
        height = Math.max(121F, colorPresetComponent.getWindowHeight());

        blur.render(ShapeProperties.create(context.getMatrices(), x - 1F, y - 1F, width + 2F, height + 2F)
                .round(7.5F)
                .softness(20F)
                .color(0x44000000)
                .build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, height)
                .round(7F)
                .thickness(1.1F)
                .outlineColor(ColorAssist.overCol(ClickGuiTheme.CONTROL_OUTLINE, ClickGuiTheme.accentSoft(), 0.28F))
                .color(0xEC12161F, 0xEC12161F, 0xF00D1017, 0xF00D1017)
                .build());

        for (AbstractComponent component : components) {
            component.position(x, y);
            component.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        draggable(Calculate.isHovered(mouseX, mouseY, x, y, width, 16));
        components.forEach(component -> component.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        components.forEach(component -> component.mouseScrolled(mouseX, mouseY, amount));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(component -> component.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
