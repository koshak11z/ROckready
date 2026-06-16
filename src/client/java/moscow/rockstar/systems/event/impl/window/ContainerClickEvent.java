/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.event.impl.window;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;

public class ContainerClickEvent
extends Event {
    private final float x;
    private final float y;
    private final int button;

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public int getButton() {
        return this.button;
    }

    @Generated
    public ContainerClickEvent(float x, float y, int button) {
        this.x = x;
        this.y = y;
        this.button = button;
    }
}

