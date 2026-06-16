package im.zov4ik.display.screens.clickgui.components.implement.window.implement.settings.color.component;

import im.zov4ik.display.screens.clickgui.ClickGuiPainter;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

@RequiredArgsConstructor
public class ColorEditorComponent extends AbstractComponent {
    private final ColorSetting setting;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float previewX = x + 8F;
        float previewY = y + 103F;
        float previewWidth = 74F;
        float previewHeight = 13F;
        float alphaBoxX = x + 86F;
        float alphaBoxWidth = 26F;
        int displayValue = (int) (setting.getAlpha() * 100F);

        ClickGuiPainter.drawControlSurface(context, previewX, previewY, previewWidth, previewHeight, 4F, false);
        rectangle.render(ShapeProperties.create(context.getMatrices(), previewX + 3F, previewY + 3F, previewWidth - 6F, previewHeight - 6F)
                .round(2.8F)
                .color(setting.getColor(), setting.getColor(), ColorAssist.multDark(setting.getColor(), 0.92F), ColorAssist.multDark(setting.getColor(), 0.92F))
                .build());

        ClickGuiPainter.drawControlSurface(context, alphaBoxX, previewY, alphaBoxWidth, previewHeight, 4F, false);
        Fonts.getSize(10, Fonts.Type.MANROPEBOLD)
                .drawCenteredString(context.getMatrices(), displayValue + "%", alphaBoxX + alphaBoxWidth / 2F, previewY + 4.95F, ClickGuiTheme.TEXT_PRIMARY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (Calculate.isHovered(mouseX, mouseY, x + 86F, y + 103F, 26F, 13F)) {
            setting.setAlpha(MathHelper.clamp((float) (setting.getAlpha() - amount * 0.04F), 0F, 1F));
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
