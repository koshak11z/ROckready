package im.zov4ik.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ValueComponent extends AbstractSettingComponent {
    private final SliderSettings setting;
    private boolean dragging;
    private float displayedValue = Float.NaN;
    private float dragProgress;

    public ValueComponent(SliderSettings setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        height = 19;

        if (Float.isNaN(displayedValue)) {
            displayedValue = setting.getValue();
        }
        if (dragging) {
            updateValue(mouseX);
        }
        dragProgress = Calculate.interpolateSmooth(3.2F, dragProgress, dragging ? 1F : 0F);
        displayedValue = Calculate.interpolateSmooth(dragging ? 2.15F : 3.1F, displayedValue, setting.getValue());

        String valueText = getAnimatedValueText();
        float valueWidth = getValueWidth(getTargetValueText());
        float valueAreaX = x + width - valueWidth - 2F;
        float trackX = x + 2F;
        float trackY = y + 13.6F;
        float trackWidth = getTrackWidth(valueWidth);

        float range = Math.max(0.0001F, setting.getMax() - setting.getMin());
        float percent = MathHelper.clamp((displayedValue - setting.getMin()) / range, 0F, 1F);
        float filled = trackWidth * percent;

        drawSettingLabel(context, setting.getName(), valueAreaX - 4F, 3.15F);
        drawControlSurface(context, valueAreaX, y + 1.0F, valueWidth, dragProgress);
        drawControlText(context, valueText, valueAreaX, y + 1.0F, valueWidth);

        rectangle.render(ShapeProperties.create(context.getMatrices(), trackX, trackY, trackWidth, 2.0F)
                .round(1.0F)
                .color(ClickGuiTheme.CONTROL_OUTLINE, ClickGuiTheme.CONTROL_OUTLINE, ClickGuiTheme.CONTROL_BG_DARK, ClickGuiTheme.CONTROL_BG_DARK)
                .build());
        if (filled > 0.25F) {
            blur.render(ShapeProperties.create(context.getMatrices(), trackX - 0.25F, trackY - 0.4F, filled + 0.5F, 3.0F)
                    .round(1.5F)
                    .softness(5F)
                    .color(ColorAssist.multAlpha(ClickGuiTheme.settingAccent(), 0.05F + dragProgress * 0.02F))
                    .build());
        }
        rectangle.render(ShapeProperties.create(context.getMatrices(), trackX, trackY, filled, 2.0F)
                .round(1.0F)
                .color(ClickGuiTheme.settingAccent(), ClickGuiTheme.settingAccent(), ColorAssist.multDark(ClickGuiTheme.settingAccent(), 0.92F), ColorAssist.multDark(ClickGuiTheme.settingAccent(), 0.92F))
                .build());

        float knobX = trackX + Math.max(0F, filled - 2.3F);
        blur.render(ShapeProperties.create(context.getMatrices(), knobX - 0.35F, trackY - 1.45F, 5.2F, 5.2F)
                .round(2.6F)
                .softness(6F)
                .color(ColorAssist.multAlpha(0xFFFDFEFF, 0.06F + dragProgress * 0.03F))
                .build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), knobX, trackY - 0.8F, 4.0F, 4.0F)
                .round(2.0F)
                .color(0xFFFDFEFF, 0xFFFDFEFF, 0xFFE6EAF3, 0xFFE6EAF3)
                .build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float valueWidth = getValueWidth(getTargetValueText());
        float trackWidth = getTrackWidth(valueWidth);
        float trackY = y + 13.6F;
        if (button == 0 && Calculate.isHovered(mouseX, mouseY, x + 2F, trackY - 3F, trackWidth, 8F)) {
            dragging = true;
            updateValue(mouseX);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            updateValue(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private float getValueWidth(String valueText) {
        return Math.max(20F, Fonts.getSize(ClickGuiTheme.CONTROL_FONT_SIZE, Fonts.Type.SEMI).getStringWidth(valueText) + 9F);
    }

    private float getTrackWidth(float valueWidth) {
        return Math.max(14F, (x + width - valueWidth - 2F) - (x + 2F) - 6F);
    }

    private void updateValue(double mouseX) {
        float trackX = x + 2F;
        float trackWidth = getTrackWidth(getValueWidth(getTargetValueText()));
        float range = Math.max(0.0001F, setting.getMax() - setting.getMin());
        float diff = MathHelper.clamp((float) mouseX - trackX, 0F, trackWidth);
        float raw = (diff / trackWidth) * range + setting.getMin();
        BigDecimal bd = BigDecimal.valueOf(raw).setScale(2, RoundingMode.HALF_UP);
        float value = setting.isInteger() ? bd.intValue() : bd.floatValue();
        setting.setValue(MathHelper.clamp(value, setting.getMin(), setting.getMax()));
    }

    private String getTargetValueText() {
        return setting.isInteger()
                ? String.valueOf((int) setting.getValue())
                : String.format("%.1f", setting.getValue());
    }

    private String getAnimatedValueText() {
        return setting.isInteger()
                ? String.valueOf(Math.round(displayedValue))
                : String.format("%.1f", displayedValue);
    }
}
