package moscow.rockstar.ui.hud.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.server.ServerUtility;

/**
 * Expensive-style watermark: a black rounded bar at the top-left showing
 * {@code ◇ logo · nick · ping ms · FPS · clock · server}, each with its own mini-icon.
 */
public class Watermark extends HudElement {
    private interface Glyph {
        void draw(UIContext c, float x, float y, float s, ColorRGBA color);
    }

    private static final class Seg {
        final Glyph icon;
        final ColorRGBA iconColor;
        final String text;

        Seg(Glyph icon, ColorRGBA iconColor, String text) {
            this.icon = icon;
            this.iconColor = iconColor;
            this.text = text;
        }
    }

    private final BooleanSetting showNick = new BooleanSetting(this, "hud.watermark.nick").enable();
    private final BooleanSetting showPing = new BooleanSetting(this, "hud.watermark.ping").enable();
    private final BooleanSetting showFps = new BooleanSetting(this, "hud.watermark.fps").enable();
    private final BooleanSetting showTime = new BooleanSetting(this, "hud.watermark.time").enable();
    private final BooleanSetting showServer = new BooleanSetting(this, "hud.watermark.server").enable();

    private static final float HEIGHT = 16.0f;
    private static final float PAD = 8.0f;
    private static final float ICON = 7.5f;
    private static final float ICON_GAP = 3.5f;
    private static final float SEG_GAP = 9.0f;

    public Watermark() {
        super("hud.watermark", "icons/hud/island.png");
        this.x = 4.0f;
        this.y = 4.0f;
        this.showing = true;
    }

    private Font font() {
        return Fonts.MEDIUM.getFont(7.0f);
    }

    private int ping() {
        if (mc.player == null || mc.player.networkHandler == null) {
            return -1;
        }
        var entry = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
        return entry == null ? -1 : entry.getLatency();
    }

    private List<Seg> segments() {
        List<Seg> segs = new ArrayList<>();
        ColorRGBA icon = Colors.getTextColor().mulAlpha(0.85f);
        if (this.showNick.isEnabled() && mc.player != null) {
            segs.add(new Seg(Glyphs::person, icon, mc.player.getName().getString()));
        }
        int ping = this.ping();
        if (this.showPing.isEnabled() && ping >= 0) {
            int bars = ping < 60 ? 4 : ping < 130 ? 3 : ping < 250 ? 2 : 1;
            final int fb = bars;
            segs.add(new Seg((c, x, y, s, col) -> Glyphs.wifi(c, x, y, s, fb, col), icon, ping + " ms"));
        }
        if (this.showFps.isEnabled()) {
            segs.add(new Seg(Glyphs::gauge, icon, mc.getCurrentFps() + " FPS"));
        }
        if (this.showTime.isEnabled()) {
            segs.add(new Seg(Glyphs::clock, icon, TextUtility.getCurrentTime()));
        }
        if (this.showServer.isEnabled()) {
            String server = mc.isInSingleplayer() ? "SinglePlayer" : ServerUtility.getIP();
            segs.add(new Seg(Glyphs::cloud, icon, server));
        }
        return segs;
    }

    private float widthOf(List<Seg> segs) {
        Font font = this.font();
        float w = PAD;
        // brand diamond
        w += ICON + SEG_GAP;
        for (int i = 0; i < segs.size(); i++) {
            Seg s = segs.get(i);
            w += ICON + ICON_GAP + font.width(s.text);
            if (i < segs.size() - 1) {
                w += SEG_GAP;
            }
        }
        w += PAD;
        return w;
    }

    @Override
    public void update(UIContext context) {
        this.height = HEIGHT;
        this.width = this.widthOf(this.segments());
        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (mc.player == null) {
            return;
        }
        List<Seg> segs = this.segments();
        Font font = this.font();
        float h = HEIGHT;

        context.drawShadow(this.x, this.y, this.width, h, 10.0f, BorderRadius.all(6.0f), ColorRGBA.BLACK.withAlpha(70.0f));
        Glyphs.background(context, this.x, this.y, this.width, h, 6.0f, this.animation.getValue());
        context.drawRoundedBorder(this.x, this.y, this.width, h, 1.0f, BorderRadius.all(6.0f), Colors.getAccentColor().mulAlpha(0.20f));

        float cx = this.x + PAD;
        float iconY = this.y + (h - ICON) / 2.0f;
        float textY = this.y + (h - font.height()) / 2.0f - 0.5f;

        // brand Z logo
        Glyphs.zLogo(context, cx, iconY, ICON, Colors.getAccentColor());
        cx += ICON + SEG_GAP;

        for (int i = 0; i < segs.size(); i++) {
            Seg s = segs.get(i);
            s.icon.draw(context, cx, iconY, ICON, s.iconColor);
            cx += ICON + ICON_GAP;
            context.drawText(font, s.text, cx, textY, Colors.getTextColor());
            cx += font.width(s.text);
            if (i < segs.size() - 1) {
                cx += SEG_GAP;
            }
        }
    }

    @Override
    public boolean show() {
        return mc.player != null && mc.world != null;
    }
}
