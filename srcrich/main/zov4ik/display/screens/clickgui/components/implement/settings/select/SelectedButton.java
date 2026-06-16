package im.zov4ik.display.screens.clickgui.components.implement.settings.select;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector4f;

import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;

import java.util.List;

import static im.zov4ik.utils.display.font.Fonts.Type.BOLD;
import static im.zov4ik.utils.math.calc.Calculate.*;

public class SelectedButton extends AbstractComponent {
    private final SelectSetting setting;
    private final String text;

    @Setter
    @Accessors(chain = true)
    private float alpha;

    private final Animation alphaAnimation = new Decelerate()
            .setMs(300).setValue(0.5F);

    public SelectedButton(SelectSetting setting, String text) {
        this.setting = setting;
        this.text = text;

        alphaAnimation.setDirection(Direction.BACKWARDS);
    }

    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();

        alphaAnimation.setDirection(setting.getSelected().contains(text) ? Direction.FORWARDS : Direction.BACKWARDS);

        float opacity = alphaAnimation.getOutput().floatValue();
        int selectedOpacity = ColorAssist.multAlpha(ColorAssist.multAlpha(ColorAssist.getClientColor(), opacity), alpha);

        if (!alphaAnimation.isFinished(Direction.BACKWARDS)) {
            rectangle.render(ShapeProperties.create(matrices, x, y, width, height + 0.15F).round(getRound(setting.getList(), text)).color(selectedOpacity).build());
        }
        Fonts.getSize(12, BOLD).drawString(matrices, text, x + 4, y + 5, ColorAssist.multAlpha(0xFFD4D6E1, alpha));
    }

    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            setting.setSelected(text);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    
    public static Vector4f getRound(List<String> list, String text) {
        if (list.size() == 1) return new Vector4f(4);
        if (list.getLast().contains(text)) return new Vector4f(0, 4, 0, 4);
        if (list.getFirst().contains(text)) return new Vector4f(4, 0, 4, 0);
        return new Vector4f(0);
    }
}
