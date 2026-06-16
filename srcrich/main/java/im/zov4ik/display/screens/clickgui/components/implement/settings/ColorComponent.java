package im.zov4ik.display.screens.clickgui.components.implement.settings;

import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.display.screens.clickgui.components.implement.window.AbstractWindow;
import im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.ColorWindow;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ColorComponent extends AbstractSettingComponent {
    private final ColorSetting setting;

    public ColorComponent(ColorSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        height = 20;
        float boxWidth = 42F;
        float boxX = x + width - boxWidth - ClickGuiTheme.CONTROL_MARGIN;
        drawSettingLabel(context, setting.getName(), boxX - 1F, 5.2F);
        drawControlSurface(context, boxX, y + 1.7F, boxWidth, false);
        rectangle.render(ShapeProperties.create(context.getMatrices(), boxX + 4.6F, y + 3.65F, boxWidth - 9.2F, 9.2F)
                .round(4.0F)
                .thickness(1.05F)
                .outlineColor(ClickGuiTheme.CONTROL_OUTLINE)
                .color(setting.getColor(), setting.getColor(), ColorAssist.multDark(setting.getColor(), 0.92F), ColorAssist.multDark(setting.getColor(), 0.92F))
                .build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float boxWidth = 42F;
        float boxX = x + width - boxWidth - ClickGuiTheme.CONTROL_MARGIN;
        if (button == 0 && Calculate.isHovered(mouseX, mouseY, boxX, y + 1.7F, boxWidth, ClickGuiTheme.CONTROL_HEIGHT)) {
            AbstractWindow existingWindow = null;

            for (AbstractWindow window : windowManager.getWindows()) {
                if (window instanceof ColorWindow) {
                    existingWindow = window;
                    break;
                }
            }

            if (existingWindow != null) {
                windowManager.delete(existingWindow);
            } else {
                float windowWidth = 124F;
                float windowHeight = 146F;
                float spawnX = boxX + boxWidth + 8F;
                float spawnY = y + ClickGuiTheme.CONTROL_HEIGHT + 6F;
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null) {
                    float maxX = client.getWindow().getScaledWidth() - windowWidth - 6F;
                    float maxY = client.getWindow().getScaledHeight() - windowHeight - 6F;
                    spawnX = Math.max(6F, Math.min(spawnX, maxX));
                    spawnY = Math.max(6F, Math.min(spawnY, maxY));
                }

                AbstractWindow colorWindow = new ColorWindow(setting)
                        .position(spawnX, spawnY)
                        .size(windowWidth, windowHeight)
                        .draggable(true);
                windowManager.add(colorWindow);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
