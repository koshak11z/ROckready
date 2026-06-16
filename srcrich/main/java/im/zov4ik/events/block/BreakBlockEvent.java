package im.zov4ik.events.block;

import net.minecraft.util.math.BlockPos;
import im.zov4ik.utils.client.managers.event.events.Event;

public record BreakBlockEvent(BlockPos blockPos) implements Event {}
