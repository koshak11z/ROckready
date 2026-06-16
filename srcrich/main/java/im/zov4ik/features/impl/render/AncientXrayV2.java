package im.zov4ik.features.impl.render;

import im.zov4ik.events.block.BlockUpdateEvent;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.render.WorldLoadEvent;
import im.zov4ik.events.render.WorldRenderEvent;
import im.zov4ik.features.impl.player.AncientBot;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.display.geometry.Render3D;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Queue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class AncientXrayV2 extends Module {
    private static final int DEFAULT_COLOR = 0xFFC3A278;
    private static final int BOT_TARGET_COLOR = 0xFF00FF55;
    private static final int EXPLOSION_SCAN_RANGE = 48;
    private static final long DEBUG_INTERVAL_MS = 1500L;
    private static final long WORLD_WARMUP_MS = 6000L;
    private static final long NEW_CHUNK_DELAY_MS = 3500L;
    private static final double TELEPORT_DISTANCE_SQUARED = 48.0D * 48.0D;
    private static final int MAX_DEBRIS_PER_CHUNK = 12;
    private static final int MAX_VISIBLE_DEBRIS = 100;
    private static final int MAX_LOCAL_DEBRIS = 3;
    private static final double LOCAL_DEBRIS_DISTANCE_SQUARED = 6.0D * 6.0D;
    private static final long EXPLOSION_TRUST_MS = 90000L;
    private static final double EXPLOSION_TRUST_DISTANCE_SQUARED = 32.0D * 32.0D;
    private static final int MAX_AIR_SIDES = 4;
    private static final int VANILLA_DEBRIS_MIN_Y = 8;
    private static final int VANILLA_DEBRIS_MAX_Y = 119;

    private final Set<BlockPos> debrisPositions = ConcurrentHashMap.newKeySet();
    private final Set<Long> queuedChunks = ConcurrentHashMap.newKeySet();
    private final Set<Long> suspiciousChunks = ConcurrentHashMap.newKeySet();
    private final Map<Long, Long> chunkLoadTimes = new ConcurrentHashMap<>();
    private final Map<BlockPos, Long> recentExplosions = new ConcurrentHashMap<>();
    private final Map<BlockPos, Long> revealedDebris = new ConcurrentHashMap<>();
    private final Queue<Long> scanQueue = new ConcurrentLinkedQueue<>();

    private final ColorSetting color = new ColorSetting("Color", "Ancient debris highlight color")
            .value(DEFAULT_COLOR);
    private final SliderSettings range = new SliderSettings("Range", "Chunk cache search radius")
            .range(16, 192)
            .setValue(96);
    private final SliderSettings yRange = new SliderSettings("Y Range", "Vertical cache search radius")
            .range(8, 128)
            .setValue(48);
    private final SliderSettings rescanDelay = new SliderSettings("Rescan", "Full cache rescan delay in seconds")
            .range(1, 10)
            .setValue(2);
    private final SliderSettings chunksPerFrame = new SliderSettings("Chunks/Frame", "Chunk cache scan budget")
            .range(1, 16)
            .setValue(5);
    private final SliderSettings lineWidth = new SliderSettings("Line Width", "Box outline width")
            .range(1.0F, 5.0F)
            .setValue(2.0F);
    private final BooleanSetting packetTriggers = new BooleanSetting("Packet Triggers", "Use packets to refresh cached chunks")
            .setValue(true);
    private final BooleanSetting chatDebug = new BooleanSetting("Chat Debug", "Print AncientXrayV2 counters to chat")
            .setValue(true);
    private final BooleanSetting fill = new BooleanSetting("Fill", "Draw transparent block fill")
            .setValue(true);

    private long lastFullScan;
    private long lastCleanup;
    private long lastDebugMessage;
    private long lastPlayerChunk = Long.MIN_VALUE;
    private long warmupUntil;
    private BlockPos lastPlayerPos;
    private int foundSinceDebug;
    private int lastDebugHighlighted = -1;

    public AncientXrayV2() {
        super("AncientXrayV2", "Ancient XRay V2", ModuleCategory.RENDER);
        setup(color, range, yRange, rescanDelay, chunksPerFrame, lineWidth, packetTriggers, chatDebug, fill);
    }

    public List<BlockPos> getDebrisPositionsSnapshot() {
        return getLimitedDebrisPositions();
    }

    public boolean isTrustedDebrisPosition(BlockPos pos) {
        return isTrustedDebris(pos);
    }

    public void trustExplosionAround(BlockPos center) {
        if (center == null) return;
        recentExplosions.put(center.toImmutable(), System.currentTimeMillis());
        forgiveChunksAround(center, EXPLOSION_SCAN_RANGE);
        queueChunksAround(center, EXPLOSION_SCAN_RANGE);
        cleanupInvalidPositions();
    }

    @Override
    public void activate() {
        clearCache();
        startWarmup();
        queuePlayerChunks();
    }

    @Override
    public void deactivate() {
        clearCache();
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        clearCache();
        startWarmup();
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!packetTriggers.isValue() || event.getType() != PacketEvent.Type.RECEIVE) return;

        if (event.getPacket() instanceof ChunkDeltaUpdateS2CPacket packet) {
            packet.visitUpdates((pos, state) -> {
                markRevealed(pos, state);
                handleBlock(pos, state);
                queueChunk(pos);
            });
        } else if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            markRevealed(packet.getPos(), packet.getState());
            handleBlock(packet.getPos(), packet.getState());
            queueChunk(packet.getPos());
        } else if (event.getPacket() instanceof ChunkDataS2CPacket packet) {
            chunkLoadTimes.put(ChunkPos.toLong(packet.getChunkX(), packet.getChunkZ()), System.currentTimeMillis());
            queueChunk(packet.getChunkX(), packet.getChunkZ());
        } else if (event.getPacket() instanceof LightUpdateS2CPacket packet) {
            queueChunk(packet.getChunkX(), packet.getChunkZ());
        } else if (event.getPacket() instanceof ExplosionS2CPacket packet) {
            BlockPos center = BlockPos.ofFloored(packet.center());
            recentExplosions.put(center, System.currentTimeMillis());
            forgiveChunksAround(center, EXPLOSION_SCAN_RANGE);
            queueChunksAround(center, EXPLOSION_SCAN_RANGE);
        } else if (event.getPacket() instanceof UnloadChunkS2CPacket packet) {
            clearChunk(packet.pos().x, packet.pos().z);
        }
    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent event) {
        if (event.type() == BlockUpdateEvent.Type.UNLOAD) {
            debrisPositions.remove(event.pos());
            return;
        }

        if (event.type() == BlockUpdateEvent.Type.UPDATE) {
            markRevealed(event.pos(), event.state());
        }
        handleBlock(event.pos(), event.state());

        if (event.type() == BlockUpdateEvent.Type.UPDATE) {
            queueChunk(event.pos());
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null) {
            clearCache();
            return;
        }

        long now = System.currentTimeMillis();
        detectTeleport(now);
        if (now < warmupUntil) {
            cleanupInvalidPositions();
            renderBotVisualTarget(lineWidth.getValue());
            return;
        }

        long playerChunk = ChunkPos.toLong(mc.player.getBlockPos());
        if (playerChunk != lastPlayerChunk || now - lastFullScan >= rescanDelay.getValue() * 1000L) {
            queuePlayerChunks();
            lastPlayerChunk = playerChunk;
            lastFullScan = now;
        }

        scanQueuedChunks(chunksPerFrame.getInt());

        if (now - lastCleanup >= 500L) {
            cleanupInvalidPositions();
            lastCleanup = now;
        }

        enforceVisibleLimit();

        int highlighted = renderDebris();
        sendDebugMessage(now, highlighted);
    }

    private void handleBlock(BlockPos pos, BlockState state) {
        if (System.currentTimeMillis() < warmupUntil) return;
        long chunkKey = ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4);
        if (suspiciousChunks.contains(chunkKey)) return;

        if (state.getBlock() == Blocks.ANCIENT_DEBRIS) {
            if (!isTrustedDebris(pos)) {
                debrisPositions.remove(pos);
                return;
            }

            if (debrisPositions.add(pos.toImmutable())) {
                foundSinceDebug++;
                enforceVisibleLimit();
            }
        } else {
            debrisPositions.remove(pos);
        }
    }

    private void queuePlayerChunks() {
        if (mc.player != null) {
            queueChunksAround(mc.player.getBlockPos(), range.getInt());
        }
    }

    private void queueChunksAround(BlockPos center, int blockRange) {
        if (mc.world == null) return;

        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        int chunkRange = Math.ceilDiv(blockRange, 16);

        for (int chunkX = centerChunkX - chunkRange; chunkX <= centerChunkX + chunkRange; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRange; chunkZ <= centerChunkZ + chunkRange; chunkZ++) {
                queueChunk(chunkX, chunkZ);
            }
        }
    }

    private void queueChunk(BlockPos pos) {
        queueChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private void queueChunk(int chunkX, int chunkZ) {
        long key = ChunkPos.toLong(chunkX, chunkZ);
        if (queuedChunks.add(key)) {
            scanQueue.add(key);
        }
    }

    private void forgiveChunksAround(BlockPos center, int blockRange) {
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        int chunkRange = Math.ceilDiv(blockRange, 16);

        for (int chunkX = centerChunkX - chunkRange; chunkX <= centerChunkX + chunkRange; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRange; chunkZ <= centerChunkZ + chunkRange; chunkZ++) {
                long key = ChunkPos.toLong(chunkX, chunkZ);
                suspiciousChunks.remove(key);
                chunkLoadTimes.put(key, 0L);
            }
        }
    }

    private void scanQueuedChunks(int limit) {
        if (mc.world == null || mc.player == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        double maxDistance = range.getValue() * range.getValue();
        long now = System.currentTimeMillis();

        for (int i = 0; i < limit; i++) {
            Long key = scanQueue.poll();
            if (key == null) return;

            queuedChunks.remove(key);
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) (long) key;

            Long loadedAt = chunkLoadTimes.get(key);
            if (loadedAt != null && now - loadedAt < NEW_CHUNK_DELAY_MS) {
                queueChunk(chunkX, chunkZ);
                continue;
            }

            if (suspiciousChunks.contains(key)) {
                clearChunkPositions(chunkX, chunkZ);
                continue;
            }

            if (mc.world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(chunkX, chunkZ);
                if (chunk != null) {
                    foundSinceDebug += scanChunk(chunk, playerPos, maxDistance);
                }
            } else {
                unloadChunk(chunkX, chunkZ);
            }
        }
    }

    private int scanChunk(WorldChunk chunk, BlockPos playerPos, double maxDistance) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        int minY = Math.max(mc.world.getBottomY(), playerPos.getY() - yRange.getInt());
        int maxY = Math.min(mc.world.getTopYInclusive(), playerPos.getY() + yRange.getInt());
        int found = 0;
        List<BlockPos> foundPositions = new ArrayList<>();

        clearChunkPositions(chunkX, chunkZ);

        for (int sectionIndex = 0; sectionIndex <= chunk.getHighestNonEmptySection(); sectionIndex++) {
            ChunkSection section = chunk.getSection(sectionIndex);
            if (section.isEmpty()) continue;

            int sectionBaseY = (sectionIndex + (chunk.getBottomY() >> 4)) << 4;
            int sectionEndY = sectionBaseY + 15;
            if (sectionEndY < minY || sectionBaseY > maxY) continue;

            int localMinY = Math.max(0, minY - sectionBaseY);
            int localMaxY = Math.min(15, maxY - sectionBaseY);

            for (int y = localMinY; y <= localMaxY; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        if (section.getBlockState(x, y, z).getBlock() != Blocks.ANCIENT_DEBRIS) continue;

                        BlockPos pos = new BlockPos(startX + x, sectionBaseY + y, startZ + z);
                        if (playerPos.getSquaredDistance(pos) <= maxDistance && isTrustedDebris(pos)) {
                            foundPositions.add(pos.toImmutable());
                            found++;
                        }
                    }
                }
            }
        }

        if (found > MAX_DEBRIS_PER_CHUNK) {
            suspiciousChunks.add(ChunkPos.toLong(chunkX, chunkZ));
            foundPositions.clear();
            return 0;
        }

        debrisPositions.addAll(foundPositions);
        enforceVisibleLimit();
        return found;
    }

    private void cleanupInvalidPositions() {
        if (mc.world == null || mc.player == null) {
            debrisPositions.clear();
            return;
        }

        BlockPos playerPos = mc.player.getBlockPos();
        double maxDistance = range.getValue() * range.getValue();

        debrisPositions.removeIf(pos -> playerPos.getSquaredDistance(pos) > maxDistance
                || !mc.world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)
                || mc.world.getBlockState(pos).getBlock() != Blocks.ANCIENT_DEBRIS
                || !isTrustedDebris(pos));

        recentExplosions.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > EXPLOSION_TRUST_MS);
    }

    private boolean isTrustedDebris(BlockPos pos) {
        if (mc.world == null) return false;
        Long revealedAt = revealedDebris.get(pos);
        if (revealedAt == null) return false;
        if (System.currentTimeMillis() - revealedAt > EXPLOSION_TRUST_MS) return false;
        return hasAirSide(pos);
    }

    private void markRevealed(BlockPos pos, BlockState state) {
        if (state.getBlock() == Blocks.ANCIENT_DEBRIS) {
            revealedDebris.put(pos.toImmutable(), System.currentTimeMillis());
        } else {
            revealedDebris.remove(pos);
        }
    }

    private boolean isNearRecentExplosion(BlockPos pos) {
        long now = System.currentTimeMillis();
        for (Map.Entry<BlockPos, Long> entry : recentExplosions.entrySet()) {
            if (now - entry.getValue() <= EXPLOSION_TRUST_MS && entry.getKey().getSquaredDistance(pos) <= EXPLOSION_TRUST_DISTANCE_SQUARED) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAirSide(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (mc.world.getBlockState(pos.offset(direction)).isAir()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDangerSide(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState state = mc.world.getBlockState(pos.offset(direction));
            if (state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE) || state.isOf(Blocks.SOUL_FIRE) || state.isOf(Blocks.WATER)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDenseLocalCluster(BlockPos pos) {
        int nearby = 1;
        for (BlockPos cached : debrisPositions) {
            if (!cached.equals(pos) && cached.getSquaredDistance(pos) <= LOCAL_DEBRIS_DISTANCE_SQUARED && ++nearby > MAX_LOCAL_DEBRIS) {
                return true;
            }
        }
        return false;
    }

    private int renderDebris() {
        if (mc.world == null || mc.player == null) return 0;

        int renderColor = color.getColor();
        float width = lineWidth.getValue();
        boolean drawFill = fill.isValue();
        int highlighted = 0;

        for (BlockPos pos : getLimitedDebrisPositions()) {
            Render3D.drawBox(new Box(pos), renderColor, width, true, drawFill, false);
            highlighted++;
        }

        BlockPos botTarget = getBotTargetDebris();
        if (botTarget != null && mc.world.getBlockState(botTarget).isOf(Blocks.ANCIENT_DEBRIS) && isTrustedDebris(botTarget)) {
            Render3D.drawBox(new Box(botTarget), BOT_TARGET_COLOR, Math.max(width, 3.0F), true, true, false);
            if (!debrisPositions.contains(botTarget)) {
                highlighted++;
            }
        }

        renderBotVisualTarget(width);

        return highlighted;
    }

    private void renderBotVisualTarget(float width) {
        if (mc.player == null) return;

        BlockPos visualTarget = getBotVisualTarget();
        if (visualTarget != null) {
            Render3D.drawLine(mc.player.getEyePos(), Vec3d.ofCenter(visualTarget), BOT_TARGET_COLOR, Math.max(width, 3.0F), false);
            Render3D.drawBox(new Box(visualTarget), BOT_TARGET_COLOR, Math.max(width, 2.5F), true, false, false);
        }
    }

    private BlockPos getBotTargetDebris() {
        AncientBot bot = Instance.get(AncientBot.class);
        return bot == null || !bot.isState() ? null : bot.getTargetDebris();
    }

    private BlockPos getBotVisualTarget() {
        AncientBot bot = Instance.get(AncientBot.class);
        return bot == null || !bot.isState() ? null : bot.getVisualTarget();
    }

    private void sendDebugMessage(long now, int highlighted) {
        if (!chatDebug.isValue() || mc.player == null || now - lastDebugMessage < DEBUG_INTERVAL_MS) return;
        if (foundSinceDebug == 0 && highlighted == lastDebugHighlighted) return;

        ChatMessage.ancientmessage("Подсвечено обломков: " + highlighted + "/" + MAX_VISIBLE_DEBRIS);
        foundSinceDebug = 0;
        lastDebugHighlighted = highlighted;
        lastDebugMessage = now;
    }

    private void clearChunk(int chunkX, int chunkZ) {
        unloadChunk(chunkX, chunkZ);
    }

    private void clearChunkPositions(int chunkX, int chunkZ) {
        debrisPositions.removeIf(pos -> (pos.getX() >> 4) == chunkX && (pos.getZ() >> 4) == chunkZ);
    }

    private void unloadChunk(int chunkX, int chunkZ) {
        long key = ChunkPos.toLong(chunkX, chunkZ);
        clearChunkPositions(chunkX, chunkZ);
        queuedChunks.remove(key);
        suspiciousChunks.remove(key);
        chunkLoadTimes.remove(key);
    }

    private void detectTeleport(long now) {
        BlockPos playerPos = mc.player.getBlockPos();
        if (lastPlayerPos != null && lastPlayerPos.getSquaredDistance(playerPos) > TELEPORT_DISTANCE_SQUARED) {
            clearCache();
            startWarmup(now);
        }
        lastPlayerPos = playerPos.toImmutable();
    }

    private void startWarmup() {
        startWarmup(System.currentTimeMillis());
    }

    private void startWarmup(long now) {
        warmupUntil = now + WORLD_WARMUP_MS;
        lastFullScan = now;
        lastCleanup = now;
    }

    private List<BlockPos> getLimitedDebrisPositions() {
        if (mc.player == null) {
            return debrisPositions.stream()
                    .limit(MAX_VISIBLE_DEBRIS)
                    .toList();
        }

        BlockPos playerPos = mc.player.getBlockPos();
        return debrisPositions.stream()
                .sorted(Comparator.comparingDouble(pos -> playerPos.getSquaredDistance(pos)))
                .limit(MAX_VISIBLE_DEBRIS)
                .toList();
    }

    private void enforceVisibleLimit() {
        if (debrisPositions.size() <= MAX_VISIBLE_DEBRIS) return;

        Set<BlockPos> keep = ConcurrentHashMap.newKeySet();
        keep.addAll(getLimitedDebrisPositions());
        debrisPositions.removeIf(pos -> !keep.contains(pos));
    }

    private void clearCache() {
        debrisPositions.clear();
        queuedChunks.clear();
        suspiciousChunks.clear();
        chunkLoadTimes.clear();
        revealedDebris.clear();
        recentExplosions.clear();
        scanQueue.clear();
        lastFullScan = 0L;
        lastCleanup = 0L;
        lastDebugMessage = 0L;
        lastPlayerChunk = Long.MIN_VALUE;
        lastPlayerPos = mc.player == null ? null : mc.player.getBlockPos().toImmutable();
        foundSinceDebug = 0;
        lastDebugHighlighted = -1;
    }
}
