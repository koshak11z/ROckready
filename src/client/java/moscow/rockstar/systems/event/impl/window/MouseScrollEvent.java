/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.event.impl.window;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;

public class MouseScrollEvent
extends Event {
    private final double verticalAmount;

    @Generated
    public double getVerticalAmount() {
        return this.verticalAmount;
    }

    @Generated
    public MouseScrollEvent(double verticalAmount) {
        this.verticalAmount = verticalAmount;
    }
}

