/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.utility.animation.base;

import lombok.Generated;
import moscow.rockstar.utility.animation.base.Easing;

public class Animation {
    private long duration;
    private float value;
    private Easing easing;
    private long startTime;
    private float startValue;
    private float targetValue;
    private boolean done;
    private boolean direction;

    public Animation(long duration, float initialValue, Easing easing) {
        this.duration = duration;
        this.easing = easing;
        this.value = initialValue;
        this.startValue = initialValue;
        this.targetValue = initialValue;
        this.done = true;
    }

    public Animation(long duration, Easing easing) {
        this(duration, 0.0f, easing);
    }

    public void update(boolean bool) {
        this.update(bool ? 1.0f : 0.0f);
    }

    public float update(float newValue) {
        long elapsed;
        long currentTime = System.currentTimeMillis();
        if (newValue != this.targetValue) {
            this.startValue = this.value;
            this.targetValue = newValue;
            this.startTime = currentTime;
            this.done = false;
        }
        if ((elapsed = currentTime - this.startTime) >= this.duration) {
            this.value = this.targetValue;
            this.done = true;
            return this.value;
        }
        float progress = (float)elapsed / (float)this.duration;
        float easedProgress = this.easing.ease(progress, 0.0f, 1.0f, 1.0f);
        this.value = this.startValue + (this.targetValue - this.startValue) * easedProgress;
        return this.value;
    }

    public void setValue(float newValue) {
        this.value = newValue;
        this.startValue = newValue;
        this.targetValue = newValue;
        this.done = true;
    }

    public void reset(float initialValue) {
        this.value = initialValue;
        this.startValue = initialValue;
        this.targetValue = initialValue;
        this.done = true;
    }

    public void reset() {
        this.reset(0.0f);
    }

    public void nonono() {
        if (this.direction) {
            this.update(1.0f);
        } else {
            this.update(0.0f);
        }
        if (this.value == 1.0f) {
            this.direction = false;
        } else if (this.value == 0.0f) {
            this.direction = true;
        }
    }

    @Generated
    public long getDuration() {
        return this.duration;
    }

    @Generated
    public float getValue() {
        return this.value;
    }

    @Generated
    public Easing getEasing() {
        return this.easing;
    }

    @Generated
    public long getStartTime() {
        return this.startTime;
    }

    @Generated
    public float getStartValue() {
        return this.startValue;
    }

    @Generated
    public float getTargetValue() {
        return this.targetValue;
    }

    @Generated
    public boolean isDone() {
        return this.done;
    }

    @Generated
    public boolean isDirection() {
        return this.direction;
    }

    @Generated
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Generated
    public void setEasing(Easing easing) {
        this.easing = easing;
    }

    @Generated
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Generated
    public void setStartValue(float startValue) {
        this.startValue = startValue;
    }

    @Generated
    public void setTargetValue(float targetValue) {
        this.targetValue = targetValue;
    }

    @Generated
    public void setDone(boolean done) {
        this.done = done;
    }

    @Generated
    public void setDirection(boolean direction) {
        this.direction = direction;
    }
}

