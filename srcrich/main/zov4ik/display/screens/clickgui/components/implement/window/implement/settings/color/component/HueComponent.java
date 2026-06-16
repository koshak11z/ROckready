package im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.component;

import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

import static net.minecraft.util.math.MathHelper.clamp;

@RequiredArgsConstructor
public class HueComponent extends AbstractComponent {
    private final ColorSetting setting;
    private boolean hueDragging;

    private float X, Y, W, H;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        X = x + 8F;
        Y = y + 8F;
        W = 104F;
        H = 66F;

        int[] color = {
                0xFF000000,
                0xFFFFFFFF,
                0xFF000000,
                Color.HSBtoRGB(setting.getHue(), 1, 1)
        };

        rectangle.render(ShapeProperties.create(matrix, X, Y, W, H)
                .round(4F)
                .color(color)
                .build());

        float pointerSize = 6F;
        float clampedX = clamp(X + W * setting.getSaturation(), X, X + W - pointerSize);
        float clampedY = clamp(Y + H * (1 - setting.getBrightness()), Y, Y + H - pointerSize);

        rectangle.render(ShapeProperties.create(matrix, clampedX, clampedY, pointerSize, pointerSize)
                .round(pointerSize / 2F)
                .softness(1F)
                .thickness(2.4F)
                .color(0x00FFFFFF)
                .outlineColor(0xFFFFFFFF)
                .build());

        if (hueDragging) {
            float saturation = clamp((mouseX - X) / W, 0F, 1F);
            float brightness = clamp(1F - ((mouseY - Y) / H), 0F, 1F);
            setting.setSaturation(saturation);
            setting.setBrightness(brightness);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        hueDragging = button == 0 && Calculate.isHovered(mouseX, mouseY, X, Y, W, H);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        hueDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
