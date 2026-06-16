/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.math.Vec2f
 */
package moscow.rockstar.systems.modules.constructions.swinganim.presets;

import lombok.Generated;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingTransformations;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import net.minecraft.util.math.Vec2f;

public final class SwingPreset {
    private final String name;
    private final Vec2f bezierStart;
    private final Vec2f bezierEnd;
    private final boolean swingBack;
    private final float speed;
    private final SwingTransformations from;
    private final SwingTransformations to;
    private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation activeAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

    @Generated
    public SwingPreset(String name, Vec2f bezierStart, Vec2f bezierEnd, boolean swingBack, float speed, SwingTransformations from, SwingTransformations to) {
        this.name = name;
        this.bezierStart = bezierStart;
        this.bezierEnd = bezierEnd;
        this.swingBack = swingBack;
        this.speed = speed;
        this.from = from;
        this.to = to;
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public Vec2f getBezierStart() {
        return this.bezierStart;
    }

    @Generated
    public Vec2f getBezierEnd() {
        return this.bezierEnd;
    }

    @Generated
    public boolean isSwingBack() {
        return this.swingBack;
    }

    @Generated
    public float getSpeed() {
        return this.speed;
    }

    @Generated
    public SwingTransformations getFrom() {
        return this.from;
    }

    @Generated
    public SwingTransformations getTo() {
        return this.to;
    }

    @Generated
    public Animation getHoverAnimation() {
        return this.hoverAnimation;
    }

    @Generated
    public Animation getActiveAnimation() {
        return this.activeAnimation;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SwingPreset)) {
            return false;
        }
        SwingPreset other = (SwingPreset)o;
        if (this.isSwingBack() != other.isSwingBack()) {
            return false;
        }
        if (Float.compare(this.getSpeed(), other.getSpeed()) != 0) {
            return false;
        }
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        Vec2f this$bezierStart = this.getBezierStart();
        Vec2f other$bezierStart = other.getBezierStart();
        if (this$bezierStart == null ? other$bezierStart != null : !this$bezierStart.equals(other$bezierStart)) {
            return false;
        }
        Vec2f this$bezierEnd = this.getBezierEnd();
        Vec2f other$bezierEnd = other.getBezierEnd();
        if (this$bezierEnd == null ? other$bezierEnd != null : !this$bezierEnd.equals(other$bezierEnd)) {
            return false;
        }
        SwingTransformations this$from = this.getFrom();
        SwingTransformations other$from = other.getFrom();
        if (this$from == null ? other$from != null : !((Object)this$from).equals(other$from)) {
            return false;
        }
        SwingTransformations this$to = this.getTo();
        SwingTransformations other$to = other.getTo();
        if (this$to == null ? other$to != null : !((Object)this$to).equals(other$to)) {
            return false;
        }
        Animation this$hoverAnimation = this.getHoverAnimation();
        Animation other$hoverAnimation = other.getHoverAnimation();
        if (this$hoverAnimation == null ? other$hoverAnimation != null : !this$hoverAnimation.equals(other$hoverAnimation)) {
            return false;
        }
        Animation this$activeAnimation = this.getActiveAnimation();
        Animation other$activeAnimation = other.getActiveAnimation();
        return !(this$activeAnimation == null ? other$activeAnimation != null : !this$activeAnimation.equals(other$activeAnimation));
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isSwingBack() ? 79 : 97);
        result = result * 59 + Float.floatToIntBits(this.getSpeed());
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        Vec2f $bezierStart = this.getBezierStart();
        result = result * 59 + ($bezierStart == null ? 43 : $bezierStart.hashCode());
        Vec2f $bezierEnd = this.getBezierEnd();
        result = result * 59 + ($bezierEnd == null ? 43 : $bezierEnd.hashCode());
        SwingTransformations $from = this.getFrom();
        result = result * 59 + ($from == null ? 43 : ((Object)$from).hashCode());
        SwingTransformations $to = this.getTo();
        result = result * 59 + ($to == null ? 43 : ((Object)$to).hashCode());
        Animation $hoverAnimation = this.getHoverAnimation();
        result = result * 59 + ($hoverAnimation == null ? 43 : $hoverAnimation.hashCode());
        Animation $activeAnimation = this.getActiveAnimation();
        result = result * 59 + ($activeAnimation == null ? 43 : $activeAnimation.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "SwingPreset(name=" + this.getName() + ", bezierStart=" + String.valueOf(this.getBezierStart()) + ", bezierEnd=" + String.valueOf(this.getBezierEnd()) + ", swingBack=" + this.isSwingBack() + ", speed=" + this.getSpeed() + ", from=" + String.valueOf(this.getFrom()) + ", to=" + String.valueOf(this.getTo()) + ", hoverAnimation=" + String.valueOf(this.getHoverAnimation()) + ", activeAnimation=" + String.valueOf(this.getActiveAnimation()) + ")";
    }
}

