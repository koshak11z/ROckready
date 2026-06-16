/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  pw.lucent.bridge.client.core.Network
 */
package moscow.rockstar.systems.bridge;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.GameTickEvent;
import pw.lucent.bridge.client.core.Network;

public class NetworkListener {
    private final EventListener<GameTickEvent> onTick = event -> Network.getInstance().onTick();

    public NetworkListener() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }
}

