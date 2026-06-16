package im.zov4ik.display.screens.clickgui.components.implement.settings.multiselect;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.features.module.setting.implement.MultiSelectSetting;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.display.screens.clickgui.components.implement.settings.select.SelectedButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static im.zov4ik.utils.display.font.Fonts.Type.BOLD;

public class MultiSelectedButton extends AbstractComponent {
    private final MultiSelectSetting setting;
    private final String text;
    @Setter
    @Accessors(chain = true)
    private float alpha;
    private final Animation alphaAnimation = new Decelerate().setMs(300).setValue(0.5);

    public MultiSelectedButton(MultiSelectSetting setting, String text) {
        this.setting = setting;
        this.text = text;

        alphaAnimation.setDirection(Direction.BACKWARDS);
    }

    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        alphaAnimation.setDirection(setting.getSelected().contains(text) ? Direction.FORWARDS : Direction.BACKWARDS);

        float opacity = alphaAnimation.getOutput().floatValue();
        int selectedOpacity = ColorAssist.multAlpha(ColorAssist.getClientColor(), opacity * alpha);

        if (!alphaAnimation.isFinished(Direction.BACKWARDS)) {
            rectangle.render(ShapeProperties.create(matrix, x, y, width, height + 0.15F).round(SelectedButton.getRound(setting.getList(), text)).color(selectedOpacity).build());
        }
        Fonts.getSize(12, BOLD).drawString(matrix, text, x + 4, y + 5, ColorAssist.multAlpha(0xFFD4D6E1, alpha));
    }

    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            List<String> selected = new ArrayList<>(setting.getSelected());
            if (selected.contains(text)) {
                selected.remove(text);
            } else {
                selected.add(text);
                sortSelectedAccordingToList(selected, setting.getList());
            }
            setting.setSelected(selected);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    
    private void sortSelectedAccordingToList(List<String> selected, List<String> list) {
        selected.sort(Comparator.comparingInt(list::indexOf));
    }
}
