package im.zov4ik.display.hud;

import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Watermark extends AbstractDraggable {
    private float animatedFps;
    private float animatedWidth = 160.0F;

    private static final float HEIGHT = 14.0F;
    private static final float PADDING_X = 8.5F;
    private static final float ICON_SLOT = 7.0F;
    private static final float ICON_SIZE = 5.6F;
    private static final float ICON_GAP = 3.0F;
    private static final float GROUP_GAP = 5.5F;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public Watermark() {
        super("Watermark", 6, 6, 160, 14, true);
    }

    @Override
    public void tick() {
        int fps = mc.getCurrentFps();
        if (animatedFps <= 0.0F) {
            animatedFps = fps;
        }
        animatedFps += (fps - animatedFps) * 0.12F;
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(14, Fonts.Type.REGULAR);
        FontRenderer bold = Fonts.getSize(14, Fonts.Type.SEMI);

        String name = mc.player != null ? mc.player.getName().getString() : mc.getSession().getUsername();
        String ping = getPing() + "ms";
        String fps = Math.round(animatedFps) + " FPS";
        String time = LocalTime.now().format(TIME_FORMAT);
        String server = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer";

        float targetWidth = PADDING_X
                + ICON_SLOT
                + GROUP_GAP
                + widthOf(font, name)
                + widthOf(font, ping)
                + widthOf(font, fps)
                + widthOf(font, time)
                + widthOf(font, server)
                + PADDING_X;

        animatedWidth += (targetWidth - animatedWidth) * 0.20F;
        if (Math.abs(targetWidth - animatedWidth) < 0.35F) {
            animatedWidth = targetWidth;
        }
        setWidth((int) Math.ceil(animatedWidth));
        setHeight((int) HEIGHT);

        HudTheme.panel(matrix, getX(), getY(), getWidth(), HEIGHT, 3.2F);

        float x = getX() + PADDING_X;
        float iconY = getY() + (HEIGHT - ICON_SLOT) / 2.0F;
        float textY = getY() + 4.35F;

        HudTheme.iconSlot(context, HudTheme.ICON_DIAMOND, x, iconY, ICON_SLOT, ICON_SIZE, HudTheme.ACCENT);
        x += ICON_SLOT + GROUP_GAP;

        x = drawGroup(context, font, HudTheme.ICON_USER, name, x, iconY, textY, HudTheme.TEXT);
        x = drawGroup(context, font, HudTheme.ICON_WIFI, ping, x, iconY, textY, HudTheme.TEXT_DIM);
        x = drawGroup(context, bold, HudTheme.ICON_MONITOR, fps, x, iconY, textY, HudTheme.TEXT);
        x = drawGroup(context, font, HudTheme.ICON_CLOCK, time, x, iconY, textY, HudTheme.TEXT_DIM);
        drawGroup(context, font, HudTheme.ICON_SERVER, server, x, iconY, textY, HudTheme.TEXT);
    }

    private float widthOf(FontRenderer font, String text) {
        return ICON_SLOT + ICON_GAP + font.getStringWidth(text) + GROUP_GAP;
    }

    private float drawGroup(DrawContext context, FontRenderer font, Identifier icon, String text,
                            float x, float iconY, float textY, int color) {
        HudTheme.iconSlot(context, icon, x, iconY, ICON_SLOT, ICON_SIZE, color);
        font.drawString(context.getMatrices(), text, x + ICON_SLOT + ICON_GAP, textY, color);
        return x + ICON_SLOT + ICON_GAP + font.getStringWidth(text) + GROUP_GAP;
    }

    private int getPing() {
        if (mc.player == null || mc.getNetworkHandler() == null) {
            return 0;
        }
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }
}
