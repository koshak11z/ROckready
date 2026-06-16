/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Identifier
 */
package moscow.rockstar.ui.hud.impl.island.impl;

import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.impl.win.WindowsMediaSession;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.window.MouseScrollEvent;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.ExtandableStatus;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.obj.Rect;
import moscow.rockstar.utility.sounds.MusicTracker;
import net.minecraft.util.Identifier;

public class MusicStatus
extends ExtandableStatus
implements IMinecraft {
    private final Animation[] waveAnims = new Animation[4];
    private final Animation pausingAnim = new Animation(300L, 0.0f, Easing.BAKEK_SIZE);
    private final Animation hoverPrevious = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation hoverPause = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation hoverNext = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation hoverLyrics = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private boolean showLyrics = false;
    private int lyricsOffset = 0;
    private final EventListener<MouseScrollEvent> onMouseScroll = event -> {
        if (!this.showLyrics) {
            return;
        }
        DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
        if (island.active() != this || !island.isExtended()) {
            return;
        }
        if (event.getVerticalAmount() < 0.0) {
            ++this.lyricsOffset;
        } else if (event.getVerticalAmount() > 0.0) {
            --this.lyricsOffset;
        } else {
            return;
        }
        String[] lines = Rockstar.getInstance().getMusicTracker().getLyrics().split("\\n");
        int maxOffset = Math.max(0, lines.length - 6);
        this.lyricsOffset = Math.min(Math.max(0, this.lyricsOffset), maxOffset);
    };

    public MusicStatus(SelectSetting setting) {
        super(setting, "music");
        for (int i = 0; i < this.waveAnims.length; ++i) {
            this.waveAnims[i] = new Animation(400L, 0.0f, Easing.LINEAR);
        }
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    @Override
    public void draw(CustomDrawContext context) {
        DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
        float x = sr.getScaledWidth() / 2.0f - island.getSize().width / 2.0f;
        float y = 7.0f;
        MusicTracker tracker = Rockstar.getInstance().getMusicTracker();
        ColorRGBA textColor = Colors.getTextColor();
        if (!tracker.haveActiveSession() || tracker.getSession() == null) {
            return;
        }
        MediaInfo media = tracker.getSession().getMedia();
        float expWidth = 164.0f;
        float expHeight = this.showLyrics ? 125.0f : 80.0f;
        float maxWidth = 100.0f;
        float defaultWidth = 32.0f + Fonts.MEDIUM.getFont(7.0f).width(media.getTitle());
        this.size.width = island.isExtended() ? expWidth : Math.min(defaultWidth, maxWidth);
        float width = this.size.width;
        this.size.height = island.isExtended() ? expHeight : 15.0f;
        float height = this.size.height;
        float extending = island.getExtendingAnim().getValue();
        float imageMargin = 4.0f + 6.0f * extending;
        float imageSize = 7.0f + 19.0f * extending;
        Identifier trackImage = tracker.getImage() != null ? tracker.getImage() : Rockstar.id("icons/music/no_image.png");
        float imageY = y + (island.isExtended() ? imageMargin : GuiUtility.getMiddleOfBox(imageSize, island.getSize().height));
        context.drawRoundedTexture(trackImage, x + imageMargin - 10.0f + 10.0f * this.animation.getValue(), imageY, imageSize, imageSize, BorderRadius.all(1.0f + 5.0f * extending));
        context.drawFadeoutText(Fonts.MEDIUM.getFont(7.0f), tracker.getSession().getMedia().getTitle(), x - 5.0f + 10.0f * this.animation.getValue() + 10.0f * this.animation.getValue() + 29.0f * extending, y + 5.0f + 11.0f * extending, textColor, 0.3f, 0.7f, island.isExtended() ? expWidth - 30.0f : maxWidth + 5.0f);
        if (extending != 0.0f && tracker.getSession() != null) {
            context.drawFadeoutText(Fonts.REGULAR.getFont(7.0f), tracker.getSession().getMedia().getArtist(), x + 20.0f + 24.0f * extending, y + 5.0f + 19.0f * extending, textColor.withAlpha(178.5f * extending), 0.3f, 0.7f, island.isExtended() ? expWidth - 30.0f : maxWidth + 5.0f);
            context.drawText(Fonts.REGULAR.getFont(5.0f), MusicStatus.formatTime(media.getPosition()), sr.getScaledWidth() / 2.0f - expWidth / 2.0f + 11.0f * extending, y + 43.0f * extending, textColor.withAlpha(255.0f));
            context.drawText(Fonts.REGULAR.getFont(5.0f), MusicStatus.formatTime(media.getDuration()), sr.getScaledWidth() / 2.0f + expWidth / 2.0f - (9.5f + Fonts.REGULAR.getFont(5.0f).width(MusicStatus.formatTime(media.getDuration())) * extending), y + 43.0f * extending, textColor.withAlpha(255.0f));
            float barWidth = 116.0f;
            float barX = sr.getScaledWidth() / 2.0f - barWidth / 2.0f;
            context.drawRoundedRect(barX, y + expHeight - (float)(this.showLyrics ? 45 : 0) - 36.5f * extending, barWidth, 3.0f, BorderRadius.all(0.5f), textColor.withAlpha(63.75f));
            float progressWidth = barWidth * Math.min(1.0f, (float)media.getPosition() / (float)media.getDuration());
            context.drawRoundedRect(barX, y + expHeight - (float)(this.showLyrics ? 45 : 0) - 36.5f * extending, progressWidth, 3.0f, BorderRadius.all(0.5f), textColor.withAlpha(150.0f));
            this.pausingAnim.setDuration(600L);
            this.pausingAnim.update(media.isPlaying() ? 1.0f : 0.0f);
            if (extending > 0.7f) {
                float controlY = y + expHeight - 25.0f * extending;
                double mouseX = GuiUtility.getMouse().getX();
                double mouseY = GuiUtility.getMouse().getY();
                Rect previous = new Rect(sr.getScaledWidth() / 2.0f - 40.0f, controlY, 16.0f, 16.0f);
                Rect pause = new Rect(sr.getScaledWidth() / 2.0f - 8.0f, controlY, 16.0f, 16.0f);
                Rect next = new Rect(sr.getScaledWidth() / 2.0f + 24.0f, controlY, 16.0f, 16.0f);
                if (previous.hovered(mouseX, mouseY) || pause.hovered(mouseX, mouseY) || next.hovered(mouseX, mouseY)) {
                    CursorUtility.set(CursorType.HAND);
                }
                this.hoverPrevious.update(previous.hovered(mouseX, mouseY));
                this.hoverPause.update(pause.hovered(mouseX, mouseY));
                this.hoverNext.update(next.hovered(mouseX, mouseY));
                context.drawTexture(Rockstar.id("icons/music/previous.png"), previous, textColor.withAlpha(255.0f - 100.0f * this.hoverPrevious.getValue()));
                float anim = this.pausingAnim.getValue();
                float centerX = pause.getX() + pause.getWidth() / 2.0f;
                float centerY = pause.getY() + pause.getHeight() / 2.0f;
                RenderUtility.rotate(context.getMatrices(), centerX, centerY, 90.0f * anim);
                RenderUtility.scale(context.getMatrices(), centerX, centerY, 1.0f - anim);
                context.drawTexture(Rockstar.id("icons/music/play.png"), pause, textColor.withAlpha(255.0f * (1.0f - anim) - 100.0f * this.hoverPause.getValue()));
                RenderUtility.end(context.getMatrices());
                RenderUtility.end(context.getMatrices());
                RenderUtility.rotate(context.getMatrices(), centerX, centerY, -90.0f + 90.0f * anim);
                RenderUtility.scale(context.getMatrices(), centerX, centerY, anim);
                context.drawTexture(Rockstar.id("icons/music/pause.png"), pause, textColor.withAlpha(255.0f * anim - 100.0f * this.hoverPause.getValue()));
                RenderUtility.end(context.getMatrices());
                RenderUtility.end(context.getMatrices());
                context.drawTexture(Rockstar.id("icons/music/next.png"), next, textColor.withAlpha(255.0f - 100.0f * this.hoverNext.getValue()));
            }
            String owner = null;
            if (tracker.getSession() == null) {
                return;
            }
            if (tracker.getSession().getOwner().toLowerCase().contains("yandex") || tracker.getSession().getOwner().toLowerCase().contains("\u044f\u043d\u0434\u0435\u043a\u0441")) {
                owner = "yandex_music";
            } else if (tracker.getSession().getOwner().toLowerCase().contains("edge")) {
                owner = "edge";
            } else if (tracker.getSession().getOwner().toLowerCase().contains("spotify")) {
                owner = "spotify";
            }
            if (owner != null) {
                context.drawTexture(Rockstar.id("icons/media/" + owner + ".png"), x + expWidth - 22.0f, y + expHeight - 21.0f, 8.0f, 8.0f, ColorRGBA.WHITE);
            }
            switch (WindowsMediaSession.getCycle()) {
                case 0: {
                    context.drawTexture(Rockstar.id("icons/music/repeat.png"), x + 14.0f, y + expHeight - 21.0f, 8.0f, 8.0f, textColor.withAlpha(150.0f));
                    break;
                }
                case 1: {
                    context.drawTexture(Rockstar.id("icons/music/repeat.png"), x + 14.0f, y + expHeight - 21.0f, 8.0f, 8.0f, textColor);
                    break;
                }
                case 2: {
                    context.drawTexture(Rockstar.id("icons/music/repeat1.png"), x + 14.0f, y + expHeight - 21.0f, 8.0f, 8.0f, textColor);
                }
            }
            if (tracker.getLyrics().isEmpty()) {
                this.showLyrics = false;
            }
            Rect lyricsRect = new Rect(x + 14.0f, y + expHeight - 21.0f, 8.0f, 8.0f);
            if (!tracker.getLyrics().isEmpty() && tracker.getLyrics().split("butors\\n\\n").length > 1) {
                if (lyricsRect.hovered(GuiUtility.getMouse().getX(), GuiUtility.getMouse().getY())) {
                    CursorUtility.set(CursorType.HAND);
                }
                this.hoverLyrics.update(lyricsRect.hovered(GuiUtility.getMouse().getX(), GuiUtility.getMouse().getY()));
                context.drawTexture(Rockstar.id("icons/music/text.png"), lyricsRect, textColor.withAlpha(255.0f - 100.0f * this.hoverLyrics.getValue()));
            }
            if (this.showLyrics && tracker.getLyrics().split("butors\\n\\n").length > 1) {
                int maxLines;
                String[] lines = tracker.getLyrics().split("butors\\n\\n")[1].split("\\n");
                if (this.lyricsOffset > lines.length - (maxLines = Math.min(6, lines.length))) {
                    this.lyricsOffset = Math.max(lines.length - maxLines, 0);
                }
                for (int i = 0; i < maxLines && i + this.lyricsOffset < lines.length; ++i) {
                    context.drawFadeoutText(Fonts.REGULAR.getFont(6.0f), lines[i + this.lyricsOffset], x + 10.0f, y + 55.0f + (float)(i * 7), textColor.withAlpha(255.0f * extending), 0.91f, 1.0f, expWidth - 20.0f);
                }
            }
        }
        for (int i = 0; i < this.waveAnims.length; ++i) {
            float phase = (float)media.getPosition() * 8.0f + (float)i * 0.7f;
            float size = media.isPlaying() ? (float)(2.0 + Math.abs(MathUtility.sin(phase)) * 8.0) : 3.0f;
            this.waveAnims[i].update(size);
            this.waveAnims[i].setDuration(1000L);
            context.drawRoundedRect(x + MathUtility.interpolate(Math.min(defaultWidth, maxWidth), expWidth - 10.0f, extending) - 2.0f - 10.0f * this.animation.getValue() + (float)i * (2.0f + extending), y + MathUtility.interpolate(4.25, 14.0, extending) + (7.0f - this.waveAnims[i].getValue()) / 2.0f, 1.0f + extending, this.waveAnims[i].getValue(), BorderRadius.all(0.5f), tracker.getMediaColor());
        }
    }

    @Override
    public void click(float mouseX, float mouseY, int button) {
        DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
        float x = sr.getScaledWidth() / 2.0f - island.getSize().width / 2.0f;
        float y = 7.0f;
        float width = this.size.width;
        float height = this.size.height;
        MusicTracker tracker = Rockstar.getInstance().getMusicTracker();
        if (!tracker.haveActiveSession()) {
            return;
        }
        if (GuiUtility.isHovered((double)(x + width / 2.0f - 40.0f), (double)(y + height - 9.0f - 16.0f), 16.0, 16.0, mouseX, mouseY)) {
            tracker.getSession().previous();
        }
        if (GuiUtility.isHovered((double)(x + width / 2.0f - 8.0f), (double)(y + height - 9.0f - 16.0f), 16.0, 16.0, mouseX, mouseY)) {
            tracker.getSession().playPause();
        }
        if (GuiUtility.isHovered((double)(x + width / 2.0f + 24.0f), (double)(y + height - 9.0f - 16.0f), 16.0, 16.0, mouseX, mouseY)) {
            tracker.getSession().next();
        }
        if (GuiUtility.isHovered((double)(x + 14.0f), (double)(y + height - 21.0f), 8.0, 8.0, mouseX, mouseY)) {
            tracker.getSession().swapCycle();
            WindowsMediaSession.setCycle(tracker.getSession().getCycleType());
        }
        Rect lyricsRect = new Rect(x + 14.0f, y + height - 21.0f, 8.0f, 8.0f);
        if (!tracker.getLyrics().isEmpty() && GuiUtility.isHovered((double)lyricsRect.getX(), (double)lyricsRect.getY(), (double)lyricsRect.getWidth(), (double)lyricsRect.getHeight(), mouseX, mouseY) && tracker.getLyrics().split("butors\\n\\n").length > 1) {
            boolean bl = this.showLyrics = !this.showLyrics;
            if (this.showLyrics) {
                this.lyricsOffset = 0;
            }
        }
    }

    @Override
    public boolean canShow() {
        return Rockstar.getInstance().getMusicTracker().getSession() != null && Rockstar.getInstance().getMusicTracker().haveActiveSession() && !Rockstar.getInstance().getMusicTracker().getSession().getOwner().toLowerCase().contains("gram");
    }

    public static String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public ColorRGBA getColor() {
        return super.getColor().mix(Rockstar.getInstance().getMusicTracker().getMediaColor(), 0.2f);
    }
}

