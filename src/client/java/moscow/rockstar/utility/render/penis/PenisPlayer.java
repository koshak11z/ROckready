/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.Identifier
 */
package moscow.rockstar.utility.render.penis;

import lombok.Generated;
import moscow.rockstar.utility.render.penis.PenisAtlas;
import moscow.rockstar.utility.render.penis.PenisMeta;
import moscow.rockstar.utility.render.penis.PenisSprite;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.util.Identifier;

public class PenisPlayer {
    private final PenisAtlas.AnimationRegion region;
    private final Timer frameTimer;
    private int currentFrame = 0;
    private boolean isPlaying = true;
    private boolean hasFinished = false;
    private boolean playOnceMode = false;

    public PenisPlayer(Identifier animationId) {
        this.region = PenisAtlas.getAnimationRegion(animationId);
        if (this.region == null) {
            throw new RuntimeException("\u0410\u043d\u0438\u043c\u0430\u0446\u0438\u044f \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430 \u0432 \u0433\u043b\u043e\u0431\u0430\u043b\u044c\u043d\u043e\u043c \u0430\u0442\u043b\u0430\u0441\u0435: " + String.valueOf(animationId));
        }
        this.frameTimer = new Timer();
    }

    public void playOnce() {
        this.currentFrame = 0;
        this.isPlaying = true;
        this.hasFinished = false;
        this.playOnceMode = true;
        this.frameTimer.reset();
    }

    public PenisSprite getCurrentSprite() {
        if (!this.isPlaying || this.hasFinished) {
            return this.region.getFrameSprite(this.currentFrame);
        }
        this.update();
        return this.region.getFrameSprite(this.currentFrame);
    }

    public void update() {
        if (!this.isPlaying || this.hasFinished) {
            return;
        }
        long frameDuration = this.region.meta.getFrameDuration();
        if (this.frameTimer.finished(frameDuration)) {
            this.nextFrame();
            this.frameTimer.reset();
        }
    }

    private void nextFrame() {
        ++this.currentFrame;
        if (this.currentFrame >= this.region.frameCount) {
            if (this.playOnceMode) {
                this.currentFrame = this.region.frameCount - 1;
                this.hasFinished = true;
                this.playOnceMode = false;
            } else if (this.region.meta.isLoop()) {
                this.currentFrame = 0;
            } else {
                this.currentFrame = this.region.frameCount - 1;
                this.hasFinished = true;
            }
        }
    }

    public void play() {
        this.isPlaying = true;
        this.hasFinished = false;
        this.playOnceMode = false;
    }

    public void pause() {
        this.isPlaying = false;
    }

    public void stop() {
        this.isPlaying = false;
        this.currentFrame = 0;
        this.hasFinished = false;
        this.playOnceMode = false;
        this.frameTimer.reset();
    }

    public void setFrame(int frame) {
        if (frame >= 0 && frame < this.region.frameCount) {
            this.currentFrame = frame;
            this.hasFinished = false;
        }
    }

    public boolean isFinished() {
        return this.hasFinished;
    }

    public PenisMeta getMeta() {
        return this.region.meta;
    }

    public Identifier getGlobalAtlasTexture() {
        return this.region.atlasTexture;
    }

    @Generated
    public int getCurrentFrame() {
        return this.currentFrame;
    }

    @Generated
    public boolean isPlaying() {
        return this.isPlaying;
    }
}

