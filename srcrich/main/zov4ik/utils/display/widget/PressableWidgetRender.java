package im.zov4ik.utils.display.widget;

import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.display.systemrender.builders.Builder;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class PressableWidgetRender implements QuickImports {

    private final Animation animation = new Decelerate().setMs(200).setValue(8);

    public static void render(DrawContext context, int x, int y, int width, int height, boolean active, String text) {

//        blur.render(ShapeProperties.create(
//                        context.getMatrices(),
//                        x + (width - width ) / 2f,
//                        y + (height - height ) / 2f,
//                        width ,
//                        height
//                )
//                .round(5f).quality(24)
//                .color( new Color(18, 19, 20, 135).getRGB(),
//                        new Color(0, 2, 5, 135).getRGB(),
//                        new Color(0, 2, 5, 135).getRGB(),
//                        new Color(18, 19, 20, 135).getRGB())
//                .build());

        ShapeProperties animRect = ShapeProperties.create(
                        context.getMatrices(),
                        x + (width - width ) / 2f,
                        y + (height - height ) / 2f,
                        width ,
                        height
                )
                .round(4f)
                .thickness(3)
                .outlineColor(new Color(150,150,150,150).getRGB())
                .color( new Color(18, 19, 20, 125).getRGB(),
                        new Color(0, 2, 5, 125).getRGB(),
                        new Color(0, 2, 5, 125).getRGB(),
                        new Color(18, 19, 20, 125).getRGB())
                .build();
        rectangle.render(animRect);

        if (text != null && !text.isEmpty() && Fonts.getSize(18, Fonts.Type.DEFAULT).getStringWidth(text) <= width) {
            Fonts.getSize(18, Fonts.Type.DEFAULT)
                    .drawString(context.getMatrices(),
                            text,
                            x - Fonts.getSize(18, Fonts.Type.DEFAULT).getStringWidth(text) / 2f + width / 2f,
                            y + 7f,
                            new Color(255, 255, 255, 255).getRGB());
        }
    }
}
