/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
 *  net.minecraft.util.math.MathHelper
 */
package moscow.rockstar.utility.game.server;

import java.util.Arrays;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;

public class TPSHandler {
    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long timeLastTimeUpdate = -1L;
    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        if (!(event.getPacket() instanceof WorldTimeUpdateS2CPacket)) {
            return;
        }
        if (this.timeLastTimeUpdate != -1L) {
            float timeElapsed = (float)(System.nanoTime() - this.timeLastTimeUpdate) / 1.0E9f;
            this.tickRates[this.nextIndex % this.tickRates.length] = MathHelper.clamp((float)(20.0f / timeElapsed), (float)0.0f, (float)20.0f);
            ++this.nextIndex;
        }
        this.timeLastTimeUpdate = System.nanoTime();
    };

    public TPSHandler() {
        Arrays.fill(this.tickRates, 0.0f);
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    public float getTPS() {
        float numTicks = 0.0f;
        float sumTickRates = 0.0f;
        for (float tickRate : this.tickRates) {
            if (!(tickRate > 0.0f)) continue;
            sumTickRates += tickRate;
            numTicks += 1.0f;
        }
        return MathHelper.clamp((float)(sumTickRates / numTicks), (float)0.0f, (float)20.0f);
    }
}

