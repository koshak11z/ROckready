package im.zov4ik.features.impl.render;

import im.zov4ik.events.block.BlockUpdateEvent;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.render.WorldLoadEvent;
import im.zov4ik.events.render.WorldRenderEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.display.geometry.Render3D;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AncientXray extends Module {
    private static final int DEFAULT_COLOR = 0xFFC3A278;
    private static final long RESCAN_DELAY_MS = 2500L;

    private final Set<BlockPos> debrisPositions = ConcurrentHashMap.newKeySet();

    private final ColorSetting color = new ColorSetting("Color", "Block highlight color")
            .value(DEFAULT_COLOR);
    private final SliderSettings range = new SliderSettings("Range", "Search and render distance")
            .range(16, 256)
            .setValue(96);
    private final SliderSettings lineWidth = new SliderSettings("Line Width", "Box outline width")
            .range(1.0F, 5.0F)
            .setValue(2.0F);
    private final BooleanSetting fill = new BooleanSetting("Fill", "Draw transparent block fill")
            .setValue(true);

    private long lastScanTime;

    public AncientXray() {
        super("AncientXray", "Ancient XRay", ModuleCategory.RENDER);
        setup(color, range, lineWidth, fill);
    }

    @Override
    public void activate() {
        debrisPositions.clear();
        scanLoadedChunks();
        lastScanTime = System.currentTimeMillis();
    }

    @Override
    public void deactivate() {
        debrisPositions.clear();
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        debrisPositions.clear();
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (event.getType() != PacketEvent.Type.RECEIVE) return;

        if (event.getPacket() instanceof ChunkDeltaUpdateS2CPacket packet) {
            packet.visitUpdates(this::handleBlock);
        } else if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            handleBlock(packet.getPos(), packet.getState());
        }
    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent event) {
        if (event.type() == BlockUpdateEvent.Type.UNLOAD) {
            debrisPositions.remove(event.pos());
            return;
        }

        handleBlock(event.pos(), event.state());
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null) {
            debrisPositions.clear();
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime >= RESCAN_DELAY_MS) {
            scanLoadedChunks();
            lastScanTime = currentTime;
        }

        BlockPos playerPos = mc.player.getBlockPos();
        double maxDistance = range.getValue() * range.getValue();

        for (BlockPos pos : debrisPositions) {
            if (!isValidRenderPosition(pos, playerPos, maxDistance)) {
                debrisPositions.remove(pos);
                continue;
            }

            Render3D.drawBox(new Box(pos), color.getColor(), lineWidth.getValue(), true, fill.isValue(), false);
        }
    }

    private void handleBlock(BlockPos pos, BlockState state) {
        if (state.getBlock() == Blocks.ANCIENT_DEBRIS) {
            debrisPositions.add(pos.toImmutable());
        } else {
            debrisPositions.remove(pos);
        }
    }

    private void scanLoadedChunks() {
        if (mc.world == null || mc.player == null) return;

        debrisPositions.clear();

        BlockPos playerPos = mc.player.getBlockPos();
        int blockRange = range.getInt();
        int chunkRange = Math.ceilDiv(blockRange, 16);
        int playerChunkX = playerPos.getX() >> 4;
        int playerChunkZ = playerPos.getZ() >> 4;
        double maxDistance = blockRange * blockRange;

        for (int chunkX = playerChunkX - chunkRange; chunkX <= playerChunkX + chunkRange; chunkX++) {
            for (int chunkZ = playerChunkZ - chunkRange; chunkZ <= playerChunkZ + chunkRange; chunkZ++) {
                if (!mc.world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) continue;

                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(chunkX, chunkZ);
                if (chunk != null) {
                    scanChunk(chunk, playerPos, maxDistance);
                }
            }
        }
    }

    private void scanChunk(WorldChunk chunk, BlockPos playerPos, double maxDistance) {
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        for (int sectionIndex = 0; sectionIndex <= chunk.getHighestNonEmptySection(); sectionIndex++) {
            ChunkSection section = chunk.getSection(sectionIndex);
            int sectionBaseY = (sectionIndex + (chunk.getBottomY() >> 4)) << 4;

            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        if (section.getBlockState(x, y, z).getBlock() != Blocks.ANCIENT_DEBRIS) continue;

                        BlockPos pos = new BlockPos(startX + x, sectionBaseY + y, startZ + z);
                        if (playerPos.getSquaredDistance(pos) <= maxDistance) {
                            debrisPositions.add(pos);
                        }
                    }
                }
            }
        }
    }

    private boolean isValidRenderPosition(BlockPos pos, BlockPos playerPos, double maxDistance) {
        if (playerPos.getSquaredDistance(pos) > maxDistance) return false;
        if (!isChunkLoaded(pos)) return false;

        return mc.world.getBlockState(pos).getBlock() == Blocks.ANCIENT_DEBRIS;
    }

    private boolean isChunkLoaded(BlockPos pos) {
        return mc.world != null && mc.world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
