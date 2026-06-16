package im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.component;

import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static net.minecraft.util.math.MathHelper.clamp;

@RequiredArgsConstructor
public class AlphaComponent extends AbstractComponent {
    private final ColorSetting setting;
    private boolean alphaDragging;

    private float X, Y, W, H;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        X = x + 8F;
        Y = y + 92F;
        W = 104F;
        H = 6F;

        float clampedX = clamp(X + W * setting.getAlpha(), X, X + W - H);
        float min = clamp((mouseX - X) / W, 0F, 1F);

        image.setTexture("textures/gui/alphabar.png").render(ShapeProperties.create(matrix, X, Y, W, H).round(H / 2F).build());
        rectangle.render(ShapeProperties.create(matrix, X, Y - 0.2F, W + 0.5F, H)
                .round(H / 2F)
                .color(0x80000000, 0x80000000, setting.getColorWithAlpha(), setting.getColorWithAlpha())
                .build());
        rectangle.render(ShapeProperties.create(matrix, clampedX, Y, H, H)
                .round(H / 2F)
                .thickness(2.4F)
                .color(0x00FFFFFF)
                .outlineColor(0xFFFFFFFF)
                .build());

        if (alphaDragging) {
            setting.setAlpha(min);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        alphaDragging = button == 0 && Calculate.isHovered(mouseX, mouseY, X, Y, W, H);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        alphaDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
