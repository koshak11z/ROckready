/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.math.BlockPos
 */
package moscow.rockstar.systems.event.impl.game;

import lombok.Generated;
import moscow.rockstar.systems.event.EventCancellable;
import net.minecraft.util.math.BlockPos;

public class StartBreakBlockEvent
extends EventCancellable {
    private final BlockPos blockPos;

    public StartBreakBlockEvent(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    @Generated
    public BlockPos getBlockPos() {
        return this.blockPos;
    }
}

