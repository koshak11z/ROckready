package im.zov4ik.events.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import im.zov4ik.utils.client.managers.event.events.Event;

public record BlockUpdateEvent(BlockState state, BlockPos pos, Type type) implements Event {
    public enum Type {
        LOAD, UNLOAD, UPDATE
    }
}
