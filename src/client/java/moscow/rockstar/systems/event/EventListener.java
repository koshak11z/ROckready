/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.event;

import moscow.rockstar.systems.event.Event;

public interface EventListener<T extends Event> {
    public void onEvent(T var1);

    default public int getPriority() {
        return 0;
    }
}

