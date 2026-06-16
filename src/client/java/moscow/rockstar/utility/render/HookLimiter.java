/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.render;

import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.IHook;

public class HookLimiter
implements IMinecraft {
    private long lastHookTime = System.nanoTime();
    private int accumulatedCalls;
    private final boolean useMCFrameRate;
    private int currentFps = 0;
    private long hookIntervalNS = 0L;

    public HookLimiter(boolean useMCFrameRate) {
        this.useMCFrameRate = useMCFrameRate;
        this.accumulatedCalls = 0;
    }

    public void execute(int fps, IHook ... calls) {
        if (this.currentFps != fps) {
            this.hookIntervalNS = 1000000000L / (long)fps;
            this.currentFps = fps;
        }
        long nanoTime = System.nanoTime();
        long elapsed = nanoTime - this.lastHookTime;
        this.accumulatedCalls += (int)(elapsed / this.hookIntervalNS);
        this.lastHookTime += (long)this.accumulatedCalls * this.hookIntervalNS;
        this.accumulatedCalls = Math.min(this.accumulatedCalls, this.useMCFrameRate ? Math.min(this.currentFps, mc.getCurrentFps()) : this.currentFps);
        while (this.accumulatedCalls > 0) {
            for (IHook call : calls) {
                call.execute();
            }
            --this.accumulatedCalls;
        }
    }
}

