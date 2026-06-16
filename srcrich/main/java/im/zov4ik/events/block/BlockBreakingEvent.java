package im.zov4ik.events.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import im.zov4ik.utils.client.managers.event.events.Event;

public record BlockBreakingEvent(BlockPos blockPos, Direction direction) implements Event {}
