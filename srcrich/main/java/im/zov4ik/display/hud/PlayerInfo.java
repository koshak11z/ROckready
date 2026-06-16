package im.zov4ik.display.hud;

import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class PlayerInfo extends AbstractDraggable {
    private float animatedWidth = 88.0F;

    private static final float HEIGHT = 13.5F;
    private static final float PAD_X = 8.25F;
    private static final float ICON_SLOT = 7.0F;
    private static final float ICON_SIZE = 5.4F;
    private static final float GAP = 3.5F;

    public PlayerInfo() {
        super("Player Info", 4, 120, 88, 14, true);
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(14, Fonts.Type.SEMI);
        BlockPos pos = Objects.requireNonNull(mc.player).getBlockPos();
        String coords = "X " + pos.getX() + " Y " + pos.getY() + " Z " + pos.getZ();

        float targetWidth = PAD_X + ICON_SLOT + GAP + font.getStringWidth(coords) + PAD_X;
        animatedWidth += (targetWidth - animatedWidth) * 0.22F;
        if (Math.abs(targetWidth - animatedWidth) < 0.3F) {
            animatedWidth = targetWidth;
        }

        setWidth((int) Math.ceil(animatedWidth));
        setHeight((int) HEIGHT);

        HudTheme.panel(matrix, getX(), getY(), getWidth(), HEIGHT, 3.2F);
        HudTheme.iconSlot(context, HudTheme.ICON_MAP_PIN, getX() + PAD_X, getY() + (HEIGHT - ICON_SLOT) / 2.0F, ICON_SLOT, ICON_SIZE, HudTheme.ACCENT);
        font.drawString(matrix, coords, getX() + PAD_X + ICON_SLOT + GAP, getY() + 4.10F, HudTheme.TEXT);
    }
}
