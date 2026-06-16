/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.ChunkPos
 */
package moscow.rockstar.utility.chunkanimator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.math.ChunkPos;

public class ChunkAnimator {
    private static final Map<ChunkPos, ChunkAnimation> animatingChunks = new ConcurrentHashMap<ChunkPos, ChunkAnimation>();
    private static final long ANIMATION_DURATION = 1000L;

    public static void startAnimation(ChunkPos pos, float worldY) {
        float startY = worldY - 64.0f;
        animatingChunks.put(pos, new ChunkAnimation(startY, worldY));
    }

    public static Float getAnimationOffset(ChunkPos pos) {
        ChunkAnimation anim = animatingChunks.get(pos);
        if (anim == null) {
            return null;
        }
        if (anim.isFinished()) {
            animatingChunks.remove(pos);
            return null;
        }
        return Float.valueOf(anim.getCurrentY() - anim.targetY);
    }

    public static class ChunkAnimation {
        public final long startTime = System.currentTimeMillis();
        public final float startY;
        public final float targetY;

        public ChunkAnimation(float startY, float targetY) {
            this.startY = startY;
            this.targetY = targetY;
        }

        public float getCurrentY() {
            long elapsed = System.currentTimeMillis() - this.startTime;
            if (elapsed >= 1000L) {
                return this.targetY;
            }
            float progress = (float)elapsed / 1000.0f;
            return this.startY + (this.targetY - this.startY) * progress;
        }

        public boolean isFinished() {
            return System.currentTimeMillis() - this.startTime >= 1000L;
        }
    }
}

