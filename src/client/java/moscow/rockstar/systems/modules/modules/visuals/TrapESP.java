/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.ChestBlock
 *  net.minecraft.block.HopperBlock
 *  net.minecraft.block.PressurePlateBlock
 *  net.minecraft.block.TntBlock
 *  net.minecraft.block.TripwireBlock
 *  net.minecraft.block.TripwireHookBlock
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
 *  net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket
 *  net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.ChunkPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Direction$Type
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.BlockView
 */
package moscow.rockstar.systems.modules.modules.visuals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;

@ModuleInfo(name="Trap ESP", category=ModuleCategory.VISUALS)
public class TrapESP
extends BaseModule {
    private volatile List<Trap> traps = Collections.emptyList();
    private final Timer updateTimer = new Timer();
    private final Deque<BlockPos> scanQueue = new ConcurrentLinkedDeque<BlockPos>();
    private final Set<BlockPos> enqueuedColumns = Collections.newSetFromMap(new ConcurrentHashMap());
    private final Map<Long, Trap> detectedTraps = new ConcurrentHashMap<Long, Trap>();
    private static final long SCAN_INTERVAL_MS = 5000L;
    private static final int SCAN_RADIUS = 64;
    private static final int COLUMNS_PER_TICK = 96;
    private static final int MAX_TRAP_DEPTH = 24;
    private static final int MIN_TRAP_DEPTH = 5;
    private final Block[] REGION_BLOCKS = new Block[]{Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE};
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        BlockPos columnBase;
        if (TrapESP.mc.player == null || TrapESP.mc.world == null) {
            return;
        }
        if (this.updateTimer.finished(5000L) && this.scanQueue.isEmpty()) {
            this.enqueueFullScan();
            this.updateTimer.reset();
        }
        int processed = 0;
        long timeBudgetNs = 2000000L;
        long startNs = System.nanoTime();
        while (processed < 96 && (columnBase = this.scanQueue.poll()) != null) {
            this.enqueuedColumns.remove(columnBase);
            ++processed;
            Trap t = this.scanColumnForTrap(columnBase);
            long key = TrapESP.packXZ(columnBase.getX(), columnBase.getZ());
            if (t != null) {
                this.detectedTraps.put(key, t);
            } else {
                this.detectedTraps.remove(key);
            }
            if (System.nanoTime() - startNs <= timeBudgetNs) continue;
            break;
        }
        this.traps = new ArrayList<Trap>(this.detectedTraps.values());
    };
    private final EventListener<ReceivePacketEvent> onPacket = event -> {
        if (TrapESP.mc.player == null || TrapESP.mc.world == null) {
            return;
        }
        try {
            Packet<?> packet = event.getPacket();
            if (packet instanceof ChunkDataS2CPacket dataPacket) {
                this.enqueueChunk(new ChunkPos(dataPacket.getChunkX(), dataPacket.getChunkZ()));
            }
            if (packet instanceof ChunkDeltaUpdateS2CPacket deltaPacket) {
                deltaPacket.visitUpdates((pos, state) -> this.enqueueColumn(pos.getX(), pos.getZ()));
            }
            if (packet instanceof BlockUpdateS2CPacket blockPacket) {
                BlockPos pos2 = blockPacket.getPos();
                this.enqueueColumn(pos2.getX(), pos2.getZ());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    };
    private final EventListener<WorldChangeEvent> onWorldChange = event -> this.reset();
    private final EventListener<PreHudRenderEvent> onHud = ev -> {
        CustomDrawContext ctx = ev.getContext();
        MatrixStack ms = ctx.getMatrices();
        Font font = Fonts.MEDIUM.getFont(9.0f);
        int fontHeight = (int)font.height();
        for (Trap trap : this.traps) {
            Vec2f screen = Utils.worldToScreen(trap.pos.toCenterPos().add(0.0, 0.5, 0.0));
            if (screen == null) continue;
            String l1 = Localizator.translate("modules.trap_esp.label");
            String l2 = Localizator.translate("modules.trap_esp.depth", trap.depth);
            String l3 = trap.hasPrivate ? Localizator.translate("modules.trap_esp.private") : Localizator.translate("modules.trap_esp.no_private");
            int iconSize = 9;
            int textPadding = 6;
            int totalWidth = (int)(font.width(l2) + (float)textPadding);
            float titleWidth = font.width(l1) + 6.0f + (float)iconSize + 2.0f;
            float miniHeight = fontHeight * 2;
            float height = miniHeight * 3.0f;
            ctx.pushMatrix();
            ms.translate(screen.x, screen.y - height, 0.0f);
            ctx.drawRect(-titleWidth / 2.0f, 0.0f, titleWidth, miniHeight, ColorRGBA.BLACK.withAlpha(150.0f));
            ctx.drawText(font, l1, -titleWidth / 2.0f + (float)iconSize + 5.0f, 3.0f, Colors.WHITE);
            ctx.drawTexture(Rockstar.id("icons/trap.png"), -titleWidth / 2.0f + 2.0f, (float)fontHeight - (float)iconSize / 2.0f, iconSize, iconSize, Colors.WHITE);
            ctx.drawRect((float)(-totalWidth) / 2.0f, miniHeight, totalWidth, miniHeight, ColorRGBA.BLACK.withAlpha(150.0f));
            ctx.drawText(font, l2, (float)(-totalWidth) / 2.0f + 2.0f, miniHeight + 3.0f, Colors.WHITE.withAlpha(200.0f));
            ctx.drawRect(-(font.width(l3) + 6.0f) / 2.0f, miniHeight * 2.0f, font.width(l3) + 6.0f, miniHeight, ColorRGBA.BLACK.withAlpha(150.0f));
            ctx.drawText(font, l3, -font.width(l3) / 2.0f, miniHeight * 2.0f + 3.0f, Colors.WHITE.withAlpha(200.0f));
            ctx.popMatrix();
        }
    };

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        this.reset();
        this.enqueueFullScan();
        super.onEnable();
    }

    public void reset() {
        this.scanQueue.clear();
        this.enqueuedColumns.clear();
        this.detectedTraps.clear();
        this.traps = Collections.emptyList();
        this.updateTimer.reset();
    }

    private void enqueueFullScan() {
        if (TrapESP.mc.player == null) {
            return;
        }
        BlockPos playerPos = TrapESP.mc.player.getBlockPos();
        ArrayList<BlockPos> cols = new ArrayList<BlockPos>(16641);
        for (int dx = -64; dx <= 64; ++dx) {
            for (int dz = -64; dz <= 64; ++dz) {
                BlockPos col = new BlockPos(playerPos.getX() + dx, playerPos.getY(), playerPos.getZ() + dz);
                if (!this.enqueuedColumns.add(col)) continue;
                cols.add(col);
            }
        }
        cols.sort(Comparator.comparingDouble(pos -> pos.getSquaredDistance((Vec3i)playerPos)));
        this.scanQueue.addAll(cols);
    }

    private void enqueueChunk(ChunkPos chunkPos) {
        if (TrapESP.mc.player == null) {
            return;
        }
        int baseY = TrapESP.mc.player.getBlockPos().getY();
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        for (int x = startX; x < startX + 16; ++x) {
            for (int z = startZ; z < startZ + 16; ++z) {
                BlockPos col = new BlockPos(x, baseY, z);
                if (!this.enqueuedColumns.add(col)) continue;
                this.scanQueue.add(col);
            }
        }
    }

    private void enqueueColumn(int x, int z) {
        if (TrapESP.mc.player == null) {
            return;
        }
        int baseY = TrapESP.mc.player.getBlockPos().getY();
        BlockPos pos = new BlockPos(x, baseY, z);
        if (this.enqueuedColumns.add(pos)) {
            this.scanQueue.add(pos);
        }
    }

    private Trap scanColumnForTrap(BlockPos columnBase) {
        if (TrapESP.mc.player == null || TrapESP.mc.world == null) {
            return null;
        }
        int baseY = columnBase.getY();
        for (int dy = 20; dy >= -20; --dy) {
            BlockPos bottom;
            BlockState bottomState;
            BlockPos start = new BlockPos(columnBase.getX(), baseY + dy, columnBase.getZ());
            int depth = 0;
            boolean inShaft = false;
            for (int i = 0; i < 24; ++i) {
                BlockPos pos = start.down(i);
                BlockState state = TrapESP.mc.world.getBlockState(pos);
                if (!state.isAir() || !TrapESP.mc.world.getFluidState(pos).isEmpty()) {
                    if (!inShaft) continue;
                    break;
                }
                int walls = 0;
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    BlockPos side = pos.offset(dir);
                    BlockState sideState = TrapESP.mc.world.getBlockState(side);
                    if (sideState.isAir() || sideState.getCollisionShape((BlockView)TrapESP.mc.world, side).isEmpty()) continue;
                    ++walls;
                }
                if (walls < 4) {
                    if (!inShaft) continue;
                    break;
                }
                inShaft = true;
                ++depth;
            }
            if (depth < 5 || (bottomState = TrapESP.mc.world.getBlockState(bottom = start.down(depth))).isAir() || bottomState.getCollisionShape((BlockView)TrapESP.mc.world, bottom).isEmpty()) continue;
            boolean flagged = this.hasNearbyIndicatorsOrValuables(start, 6);
            return new Trap(start, depth, flagged);
        }
        return null;
    }

    private boolean hasNearbyIndicatorsOrValuables(BlockPos center, int radius) {
        if (TrapESP.mc.player == null || TrapESP.mc.world == null) {
            return false;
        }
        BlockPos min = center.add(-radius, -radius, -radius);
        BlockPos max = center.add(radius, radius, radius);
        for (BlockPos pos : BlockPos.iterate((BlockPos)min, (BlockPos)max)) {
            BlockState state = TrapESP.mc.world.getBlockState(pos);
            if (state.isAir()) continue;
            if (state.getBlock() instanceof TntBlock || state.getBlock() instanceof PressurePlateBlock || state.getBlock() instanceof TripwireBlock || state.getBlock() instanceof TripwireHookBlock || state.getBlock() instanceof HopperBlock || state.getBlock() instanceof ChestBlock) {
                return true;
            }
            Block block = state.getBlock();
            for (Block region : this.REGION_BLOCKS) {
                if (block != region) continue;
                return true;
            }
        }
        return false;
    }

    private static long packXZ(int x, int z) {
        return (long)x << 32 ^ (long)z & 0xFFFFFFFFL;
    }

    static class Trap {
        BlockPos pos;
        int depth;
        boolean hasPrivate;

        @Generated
        public Trap(BlockPos pos, int depth, boolean hasPrivate) {
            this.pos = pos;
            this.depth = depth;
            this.hasPrivate = hasPrivate;
        }
    }
}
