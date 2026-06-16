/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.utility.rotations;

import lombok.Generated;

public enum RotationPriority {
    NOT_IMPORTANT(-2),
    NORMAL(0),
    TO_TARGET(2),
    OVERRIDE(5),
    USE_ITEM(4),
    MAX(6);

    private final int priority;

    @Generated
    public int getPriority() {
        return this.priority;
    }

    @Generated
    private RotationPriority(int priority) {
        this.priority = priority;
    }
}

