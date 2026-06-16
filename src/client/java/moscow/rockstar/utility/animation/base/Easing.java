/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.utility.animation.base;

import lombok.Generated;
import moscow.rockstar.utility.math.MathUtility;

public interface Easing {
    public static final Easing BAKEK = Easing.generate(0.45f, 1.45f, 0.49f, 1.15f);
    public static final Easing BAKEK_SMALLER = Easing.generate(0.45f, 1.45f, 0.43f, 0.91f);
    public static final Easing BAKEK_PAGES = Easing.generate(0.1f, 1.07f, 0.34f, 1.04f);
    public static final Easing BAKEK_SIZE = Easing.generate(0.27f, 1.09f, 0.49f, 1.06f);
    public static final Easing BAKEK_BACK = Easing.generate(0.62, -0.16, 0.8, 0.37);
    public static final Easing BAKEK_MANY = Easing.generate(0.25, 1.07, 0.11, 1.1);
    public static final Easing FIGMA_EASE_IN_OUT = Easing.generate(0.42, 0.0, 0.58, 1.0);
    public static final Easing SMOOTH_STEP = (t, b, c, d) -> {
        float x = c * t / d + b;
        return (float)(-2.0 * Math.pow(x, 3.0) + 3.0 * Math.pow(x, 2.0));
    };
    public static final Easing BOTH_CUBIC = (t, b, c, d) -> {
        float x = c * t / d + b;
        return (double)x < 0.5 ? 4.0f * x * x * x : (float)(1.0 - Math.pow(-2.0f * x + 2.0f, 3.0) / 2.0);
    };
    public static final Easing LINEAR = (t, b, c, d) -> c * t / d + b;
    public static final Easing QUAD_IN = (t, b, c, d) -> c * (t /= d) * t + b;
    public static final Easing QUAD_OUT = (t, b, c, d) -> -c * (t /= d) * (t - 2.0f) + b;
    public static final Easing QUAD_IN_OUT = (t, b, c, d) -> {
        t /= d / 2.0f;
        if (t < 1.0f) {
            return c / 2.0f * t * t + b;
        }
        return -c / 2.0f * ((t -= 1.0f) * (t - 2.0f) - 1.0f) + b;
    };
    public static final Easing CUBIC_IN = (t, b, c, d) -> c * (t /= d) * t * t + b;
    public static final Easing CUBIC_OUT = (t, b, c, d) -> {
        t = t / d - 1.0f;
        return c * (t * t * t + 1.0f) + b;
    };
    public static final Easing CUBIC_IN_OUT = (t, b, c, d) -> {
        t /= d / 2.0f;
        if (t < 1.0f) {
            return c / 2.0f * t * t * t + b;
        }
        return c / 2.0f * ((t -= 2.0f) * t * t + 2.0f) + b;
    };
    public static final Easing QUARTIC_IN = (t, b, c, d) -> c * (t /= d) * t * t * t + b;
    public static final Easing QUARTIC_OUT = (t, b, c, d) -> {
        t = t / d - 1.0f;
        return -c * (t * t * t * t - 1.0f) + b;
    };
    public static final Easing QUARTIC_IN_OUT = (t, b, c, d) -> {
        t /= d / 2.0f;
        if (t < 1.0f) {
            return c / 2.0f * t * t * t * t + b;
        }
        return -c / 2.0f * ((t -= 2.0f) * t * t * t - 2.0f) + b;
    };
    public static final Easing QUINTIC_IN = (t, b, c, d) -> c * (t /= d) * t * t * t * t + b;
    public static final Easing QUINTIC_OUT = (t, b, c, d) -> {
        t = t / d - 1.0f;
        return c * (t * t * t * t * t + 1.0f) + b;
    };
    public static final Easing QUINTIC_IN_OUT = (t, b, c, d) -> {
        t /= d / 2.0f;
        if (t < 1.0f) {
            return c / 2.0f * t * t * t * t * t + b;
        }
        return c / 2.0f * ((t -= 2.0f) * t * t * t * t + 2.0f) + b;
    };
    public static final Easing SINE_IN = (t, b, c, d) -> -c * (float)MathUtility.cos((double)(t / d) * 1.5707963267948966) + c + b;
    public static final Easing SINE_OUT = (t, b, c, d) -> c * (float)MathUtility.sin((double)(t / d) * 1.5707963267948966) + b;
    public static final Easing SINE_IN_OUT = (t, b, c, d) -> -c / 2.0f * ((float)MathUtility.cos(Math.PI * (double)t / (double)d) - 1.0f) + b;
    public static final Easing EXPO_IN = (t, b, c, d) -> t == 0.0f ? b : c * (float)Math.pow(2.0, 10.0f * (t / d - 1.0f)) + b;
    public static final Easing EXPO_OUT = (t, b, c, d) -> t == d ? b + c : c * (-((float)Math.pow(2.0, -10.0f * t / d)) + 1.0f) + b;
    public static final Easing EXPO_IN_OUT = (t, b, c, d) -> {
        if (t == 0.0f) {
            return b;
        }
        if (t == d) {
            return b + c;
        }
        t /= d / 2.0f;
        if (t < 1.0f) {
            return c / 2.0f * (float)Math.pow(2.0, 10.0f * (t - 1.0f)) + b;
        }
        return c / 2.0f * (-((float)Math.pow(2.0, -10.0f * (t -= 1.0f))) + 2.0f) + b;
    };
    public static final Easing CIRC_IN = (t, b, c, d) -> -c * ((float)Math.sqrt(1.0f - (t /= d) * t) - 1.0f) + b;
    public static final Easing CIRC_OUT = (t, b, c, d) -> {
        t = t / d - 1.0f;
        return c * (float)Math.sqrt(1.0f - t * t) + b;
    };
    public static final Easing CIRC_IN_OUT = (t, b, c, d) -> {
        t /= d / 2.0f;
        if (t < 1.0f) {
            return -c / 2.0f * ((float)Math.sqrt(1.0f - t * t) - 1.0f) + b;
        }
        return c / 2.0f * ((float)Math.sqrt(1.0f - (t -= 2.0f) * t) + 1.0f) + b;
    };
    public static final Elastic ELASTIC_IN = new ElasticIn();
    public static final Elastic ELASTIC_OUT = new ElasticOut();
    public static final Elastic ELASTIC_IN_OUT = new ElasticInOut();
    public static final Back BACK_IN = new BackIn();
    public static final Back BACK_OUT = new BackOut();
    public static final Back BACK_IN_OUT = new BackInOut();
    public static final Easing BOUNCE_OUT = (t, b, c, d) -> {
        t /= d;
        if (t < 0.36363637f) {
            return c * (7.5625f * t * t) + b;
        }
        if (t < 0.72727275f) {
            return c * (7.5625f * (t -= 0.54545456f) * t + 0.75f) + b;
        }
        if (t < 0.90909094f) {
            return c * (7.5625f * (t -= 0.8181818f) * t + 0.9375f) + b;
        }
        return c * (7.5625f * (t -= 0.95454544f) * t + 0.984375f) + b;
    };
    public static final Easing BOUNCE_IN = (t, b, c, d) -> c - BOUNCE_OUT.ease(d - t, 0.0f, c, d) + b;
    public static final Easing BOUNCE_IN_OUT = (t, b, c, d) -> {
        if (t < d / 2.0f) {
            return BOUNCE_IN.ease(t * 2.0f, 0.0f, c, d) * 0.5f + b;
        }
        return BOUNCE_OUT.ease(t * 2.0f - d, 0.0f, c, d) * 0.5f + c * 0.5f + b;
    };

    public static Easing generate(final double x1, final double y1, final double x2, final double y2) {
        return new Easing(){

            @Override
            public float ease(float t, float b, float c, float d) {
                if (d <= 0.0f || t <= 0.0f) {
                    return b;
                }
                if (t >= d) {
                    return b + c;
                }
                float progress = t / d;
                float tBez = this.solveTBez((float)x1, (float)x2, progress);
                float y = this.bezierY(tBez, (float)y1, (float)y2);
                return b + c * y;
            }

            private float solveTBez(float x12, float x22, float progress) {
                float t = progress;
                int MAX_ITERATIONS = 8;
                float EPSILON = 1.0E-5f;
                for (int i = 0; i < 8; ++i) {
                    float x = this.bezierX(t, x12, x22);
                    float dx = this.bezierDX(t, x12, x22);
                    if (Math.abs(x - progress) < 1.0E-5f || Math.abs(dx) < 1.0E-6f) break;
                    t -= (x - progress) / dx;
                    t = Math.max(0.0f, Math.min(1.0f, t));
                }
                return t;
            }

            private float bezierX(float t, float x12, float x22) {
                return 3.0f * (1.0f - t) * (1.0f - t) * t * x12 + 3.0f * (1.0f - t) * t * t * x22 + t * t * t;
            }

            private float bezierDX(float t, float x12, float x22) {
                return 3.0f * ((1.0f - t) * (1.0f - 3.0f * t) * x12 + (2.0f * t - 3.0f * t * t) * x22) + 3.0f * t * t;
            }

            private float bezierY(float t, float y12, float y22) {
                return 3.0f * (1.0f - t) * (1.0f - t) * t * y12 + 3.0f * (1.0f - t) * t * t * y22 + t * t * t;
            }
        };
    }

    public float ease(float var1, float var2, float var3, float var4);

    public static class ElasticIn
    extends Elastic {
        public ElasticIn(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticIn() {
        }

        @Override
        public float ease(float t, float b, float c, float d) {
            float a = this.getAmplitude();
            float p = this.getPeriod();
            if (t == 0.0f) {
                return b;
            }
            if ((t /= d) == 1.0f) {
                return b + c;
            }
            if (p == 0.0f) {
                p = d * 0.3f;
            }
            float s = 0.0f;
            if (a < Math.abs(c)) {
                a = c;
                s = p / 4.0f;
            } else {
                s = p / ((float)Math.PI * 2) * (float)Math.asin(c / a);
            }
            return -(a * (float)Math.pow(2.0, 10.0f * (t -= 1.0f)) * (float)MathUtility.sin((double)(t * d - s) * (Math.PI * 2) / (double)p)) + b;
        }
    }

    public static abstract class Elastic
    implements Easing {
        private float amplitude;
        private float period;

        public Elastic(float amplitude, float period) {
            this.amplitude = amplitude;
            this.period = period;
        }

        public Elastic() {
            this(-1.0f, 0.0f);
        }

        @Generated
        public void setAmplitude(float amplitude) {
            this.amplitude = amplitude;
        }

        @Generated
        public void setPeriod(float period) {
            this.period = period;
        }

        @Generated
        public float getAmplitude() {
            return this.amplitude;
        }

        @Generated
        public float getPeriod() {
            return this.period;
        }
    }

    public static class ElasticOut
    extends Elastic {
        public ElasticOut(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticOut() {
        }

        @Override
        public float ease(float t, float b, float c, float d) {
            float a = this.getAmplitude();
            float p = this.getPeriod();
            if (t == 0.0f) {
                return b;
            }
            if ((t /= d) == 1.0f) {
                return b + c;
            }
            if (p == 0.0f) {
                p = d * 0.3f;
            }
            float s = 0.0f;
            if (a < Math.abs(c)) {
                a = c;
                s = p / 4.0f;
            } else {
                s = p / ((float)Math.PI * 2) * (float)Math.asin(c / a);
            }
            return a * (float)Math.pow(2.0, -10.0f * t) * (float)MathUtility.sin((double)(t * d - s) * (Math.PI * 2) / (double)p) + c + b;
        }
    }

    public static class ElasticInOut
    extends Elastic {
        public ElasticInOut(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticInOut() {
        }

        @Override
        public float ease(float t, float b, float c, float d) {
            float a = this.getAmplitude();
            float p = this.getPeriod();
            if (t == 0.0f) {
                return b;
            }
            if ((t /= d / 2.0f) == 2.0f) {
                return b + c;
            }
            if (p == 0.0f) {
                p = d * 0.45000002f;
            }
            float s = 0.0f;
            if (a < Math.abs(c)) {
                a = c;
                s = p / 4.0f;
            } else {
                s = p / ((float)Math.PI * 2) * (float)Math.asin(c / a);
            }
            if (t < 1.0f) {
                return -0.5f * (a * (float)Math.pow(2.0, 10.0f * (t -= 1.0f)) * (float)MathUtility.sin((double)(t * d - s) * (Math.PI * 2) / (double)p)) + b;
            }
            return a * (float)Math.pow(2.0, -10.0f * (t -= 1.0f)) * (float)MathUtility.sin((double)(t * d - s) * (Math.PI * 2) / (double)p) * 0.5f + c + b;
        }
    }

    public static class BackIn
    extends Back {
        public BackIn() {
        }

        public BackIn(float overshoot) {
            super(overshoot);
        }

        @Override
        public float ease(float t, float b, float c, float d) {
            float s = this.getOvershoot();
            return c * (t /= d) * t * ((s + 1.0f) * t - s) + b;
        }
    }

    public static abstract class Back
    implements Easing {
        public static final float DEFAULT_OVERSHOOT = 1.70158f;
        private float overshoot;

        public Back() {
            this(1.70158f);
        }

        public Back(float overshoot) {
            this.overshoot = overshoot;
        }

        @Generated
        public void setOvershoot(float overshoot) {
            this.overshoot = overshoot;
        }

        @Generated
        public float getOvershoot() {
            return this.overshoot;
        }
    }

    public static class BackOut
    extends Back {
        public BackOut() {
        }

        public BackOut(float overshoot) {
            super(overshoot);
        }

        @Override
        public float ease(float t, float b, float c, float d) {
            float s = this.getOvershoot();
            t = t / d - 1.0f;
            return c * (t * t * ((s + 1.0f) * t + s) + 1.0f) + b;
        }
    }

    public static class BackInOut
    extends Back {
        public BackInOut() {
        }

        public BackInOut(float overshoot) {
            super(overshoot);
        }

        @Override
        public float ease(float t, float b, float c, float d) {
            float s = this.getOvershoot();
            t /= d / 2.0f;
            if (t < 1.0f) {
                return c / 2.0f * (t * t * (((s *= 1.525f) + 1.0f) * t - s)) + b;
            }
            return c / 2.0f * ((t -= 2.0f) * t * (((s *= 1.525f) + 1.0f) * t + s) + 2.0f) + b;
        }
    }
}
