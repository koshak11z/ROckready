/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.notifications;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.time.Timer;

public class NotificationOther {
    private final NotificationType type;
    private final String title;
    private final String desc;
    private final Timer timer = new Timer();
    private final long duration;
    private final Animation animation = new Animation(400L, Easing.BAKEK);
    private final Animation showing = new Animation(300L, Easing.BAKEK_SIZE);
    private final Animation animY = new Animation(300L, Easing.BAKEK_SMALLER);

    public NotificationOther(NotificationType type, String title, String desc) {
        this.type = type;
        this.title = title;
        this.desc = desc;
        this.duration = 2000L;
    }

    public void draw(CustomDrawContext context, float off) {
        float textWidth = Math.max(Fonts.BOLD.getFont(7.0f).width(this.title), Fonts.MEDIUM.getFont(6.0f).width(this.desc));
        float width = textWidth + 32.0f;
        this.animY.setEasing(Easing.BAKEK_SIZE);
        this.animY.setDuration(300L);
        float x = (float)context.getScaledWindowWidth() / 2.0f - width / 2.0f;
        float y = (float)context.getScaledWindowHeight() - 90.0f - this.animY.update(off);
        float height = 26.0f;
        int alpha = (int)(255.0f * this.animation.getValue());
        RenderUtility.scale(context.getMatrices(), x + width / 2.0f, y + 12.0f + height / 2.0f, 0.5f + 0.5f * this.animation.getValue());
        if (Interface.showGlass()) {
            context.drawLiquidGlass(x, y, width, height, 7.0f, 0.08f, BorderRadius.all(7.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue() * Interface.glass()));
            context.drawSquircle(x, y, width, height, 7.0f, BorderRadius.all(7.0f), Colors.getBackgroundColor().mulAlpha((0.8f - 0.6f * Interface.glass()) * this.animation.getValue()));
        } else {
            if (Interface.blurNotificationsEnabled()) {
                context.drawBlurredRect(x, y, width, height, 45.0f, 7.0f, BorderRadius.all(7.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue() * Interface.minimalizm()));
            }
            context.drawSquircle(x, y, width, height, 7.0f, BorderRadius.all(7.0f), new ColorRGBA(0.0f, 0.0f, 0.0f).withAlpha((int)(140.25f * this.animation.getValue())));
            context.drawRoundedRect(x + height / 2.0f - 9.0f, y + height / 2.0f - 9.0f, 18.0f, 18.0f, BorderRadius.all(4.0f), new ColorRGBA(0.0f, 0.0f, 0.0f).withAlpha((int)(51.0f * this.animation.getValue())));
        }
        context.drawTexture(Rockstar.id("icons/" + this.type.getName() + ".png"), x + height / 2.0f - 4.5f, y + height / 2.0f - 4.5f, 10.0f, 10.0f, this.type.getColor().withAlpha((float)alpha * 0.8f));
        context.drawText(Fonts.BOLD.getFont(7.0f), this.title, x + 27.0f, y + 7.0f, ColorRGBA.WHITE.withAlpha(alpha));
        context.drawText(Fonts.MEDIUM.getFont(6.0f), this.desc, x + 27.0f, y + 15.0f, ColorRGBA.WHITE.withAlpha(alpha));
        RenderUtility.end(context.getMatrices());
    }

    public void update() {
        this.animation.setDuration(400L);
        this.animation.setEasing(this.timer.finished(this.duration) ? Easing.BAKEK_BACK : Easing.BAKEK);
        this.animation.update(this.timer.finished(this.duration) ? 0.0f : 1.0f);
    }

    public boolean isFinished() {
        return this.animation.getValue() == 0.0f && this.timer.finished(this.duration);
    }

    @Generated
    public NotificationType getType() {
        return this.type;
    }

    @Generated
    public String getTitle() {
        return this.title;
    }

    @Generated
    public String getDesc() {
        return this.desc;
    }

    @Generated
    public Timer getTimer() {
        return this.timer;
    }

    @Generated
    public long getDuration() {
        return this.duration;
    }

    @Generated
    public Animation getAnimation() {
        return this.animation;
    }

    @Generated
    public Animation getShowing() {
        return this.showing;
    }

    @Generated
    public Animation getAnimY() {
        return this.animY;
    }
}

