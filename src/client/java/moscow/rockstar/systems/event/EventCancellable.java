/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.event;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;

public class EventCancellable
extends Event {
    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    @Generated
    public boolean isCancelled() {
        return this.cancelled;
    }
}

