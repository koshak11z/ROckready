package im.zov4ik.events.block;

import net.minecraft.block.entity.BlockEntity;
import im.zov4ik.utils.client.managers.event.events.Event;

public record BlockEntityProgressEvent(BlockEntity blockEntity, Type type) implements Event {
    public enum Type {
        ADD, REMOVE
    }
}
