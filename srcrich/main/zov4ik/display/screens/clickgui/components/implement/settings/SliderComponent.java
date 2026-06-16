package im.zov4ik.display.screens.clickgui.components.implement.settings;

import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SliderComponent extends AbstractSettingComponent {
    private final SliderSettings setting;
    private boolean dragging;
    private double animation;

    public SliderComponent(SliderSettings setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        height = 18;

        String value = String.valueOf(setting.getValue());
        float valueWidth = Fonts.getSize(ClickGuiTheme.CONTROL_FONT_SIZE, Fonts.Type.SEMI).getStringWidth(value) + 9.0F;
        float valueX = x + width - valueWidth - 2.0F;
        float trackWidth = Math.max(42.0F, width - 6.0F);

        drawSettingLabel(context, setting.getName(), valueX - 3.0F, 3.15F);
        drawControlSurface(context, valueX, y + 1.0F, valueWidth, false);
        Fonts.getSize(ClickGuiTheme.CONTROL_FONT_SIZE, Fonts.Type.SEMI)
                .drawCenteredString(matrix, value, valueX + valueWidth / 2.0F, y + 5.55F, ClickGuiTheme.TEXT_PRIMARY);

        changeValue(drawTrack(mouseX, matrix, trackWidth), trackWidth);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        dragging = Calculate.isHovered(mouseX, mouseY, x + 3.0F, y + 11.4F, Math.max(42.0F, width - 6.0F), 7.0F) && button == 0;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private float drawTrack(int mouseX, MatrixStack matrix, float trackWidth) {
        float trackX = x + 3.0F;
        float trackY = y + 13.4F;
        float percentValue = trackWidth * (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        float difference = MathHelper.clamp(mouseX - trackX, 0, trackWidth);
        animation = Calculate.interpolate(animation, percentValue);

        int accent = ClickGuiTheme.accent();
        rectangle.render(ShapeProperties.create(matrix, trackX, trackY, trackWidth, 2.0F)
                .round(1.0F)
                .color(0x70343A48)
                .build());
        rectangle.render(ShapeProperties.create(matrix, trackX, trackY, (float) animation, 2.0F)
                .round(1.0F)
                .color(ColorAssist.multDark(accent, 0.62F),
                        ColorAssist.multAlpha(accent, 0.86F),
                        accent,
                        accent)
                .build());

        float knobX = MathHelper.clamp((float) (trackX + animation), trackX, trackX + trackWidth);
        rectangle.render(ShapeProperties.create(matrix, knobX - 2.5F, trackY - 1.55F, 5.0F, 5.0F)
                .round(2.5F)
                .color(accent)
                .build());
        rectangle.render(ShapeProperties.create(matrix, knobX - 2.1F, trackY - 1.1F, 4.2F, 4.2F)
                .round(2.1F)
                .thickness(1.1F)
                .color(ColorAssist.multDark(accent, 0.5F),
                        ColorAssist.multDark(accent, 0.55F),
                        ColorAssist.multDark(accent, 0.6F),
                        ColorAssist.multDark(accent, 0.65F))
                .build());

        return difference;
    }

    private void changeValue(float difference, float trackWidth) {
        BigDecimal bd = BigDecimal.valueOf((difference / trackWidth) * (setting.getMax() - setting.getMin()) + setting.getMin())
                .setScale(2, RoundingMode.HALF_UP);

        if (dragging) {
            float value = difference == 0 ? setting.getMin() : bd.floatValue();
            if (setting.isInteger()) value = (int) value;
            setting.setValue(value);
        }
    }
}
