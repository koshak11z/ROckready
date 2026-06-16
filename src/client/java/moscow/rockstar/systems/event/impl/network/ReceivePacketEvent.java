/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.packet.Packet
 */
package moscow.rockstar.systems.event.impl.network;

import lombok.Generated;
import moscow.rockstar.systems.event.EventCancellable;
import net.minecraft.network.packet.Packet;

public class ReceivePacketEvent
extends EventCancellable {
    private final Packet<?> packet;

    @Generated
    public Packet<?> getPacket() {
        return this.packet;
    }

    @Generated
    public ReceivePacketEvent(Packet<?> packet) {
        this.packet = packet;
    }
}

