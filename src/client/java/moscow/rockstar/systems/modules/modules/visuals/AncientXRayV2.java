package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.player.AncientBot;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(name = "AncientXRay V2", category = ModuleCategory.VISUALS, desc = "srcrich AncientXrayV2 smart debris cache")
public final class AncientXRayV2 extends BaseModule {
    private static final int MAX_DEBRIS_PER_CHUNK = 12;
    private static final int MAX_VISIBLE_DEBRIS = 100;
    private static final int MAX_LOCAL_DEBRIS = 3;
    private static final int EXPLOSION_SCAN_RANGE = 48;
    private static final long DEBUG_INTERVAL_MS = 1500L;
    private static final long WORLD_WARMUP_MS = 6000L;
    private static final long NEW_CHUNK_DELAY_MS = 3500L;
    private static final long EXPLOSION_TRUST_MS = 90000L;
    private static final double TELEPORT_DISTANCE_SQUARED = 48.0D * 48.0D;
    private static final double EXPLOSION_TRUST_DISTANCE_SQUARED = 32.0D * 32.0D;
    private static final double LOCAL_DEBRIS_DISTANCE_SQUARED = 6.0D * 6.0D;
    private static final int DEFAULT_R = 195, DEFAULT_G = 162, DEFAULT_B = 120;
    private static final int BOT_R = 0, BOT_G = 255, BOT_B = 85;

    private final Set<BlockPos> debrisPositions = ConcurrentHashMap.newKeySet();
    private final Set<Long> queuedChunks = ConcurrentHashMap.newKeySet();
    private final Set<Long> suspiciousChunks = ConcurrentHashMap.newKeySet();
    private final Map<Long, Long> chunkLoadTimes = new ConcurrentHashMap<>();
    private final Map<BlockPos, Long> recentExplosions = new ConcurrentHashMap<>();
    private final Map<BlockPos, Long> revealedDebris = new ConcurrentHashMap<>();
    private final Queue<Long> scanQueue = new ConcurrentLinkedQueue<>();

    private final SliderSetting range = new SliderSetting(this, "Range").min(16.0f).max(192.0f).step(1.0f).currentValue(96.0f);
    private final SliderSetting yRange = new SliderSetting(this, "Y Range").min(8.0f).max(128.0f).step(1.0f).currentValue(48.0f);
    private final SliderSetting rescanDelay = new SliderSetting(this, "Rescan").min(1.0f).max(10.0f).step(1.0f).currentValue(2.0f).suffix(" s");
    private final SliderSetting chunksPerFrame = new SliderSetting(this, "Chunks/Frame").min(1.0f).max(16.0f).step(1.0f).currentValue(5.0f);
    private final SliderSetting lineWidth = new SliderSetting(this, "Line Width").min(1.0f).max(5.0f).step(0.5f).currentValue(2.0f);
    private final BooleanSetting packetTriggers = new BooleanSetting(this, "Packet Triggers").enable();
    private final BooleanSetting chatDebug = new BooleanSetting(this, "Chat Debug").enable();
    private final BooleanSetting fill = new BooleanSetting(this, "Fill").enable();
    private final BooleanSetting strictTrust = new BooleanSetting(this, "Strict trust").enable();

    private long lastFullScan;
    private long lastCleanup;
    private long lastDebugMessage;
    private long lastPlayerChunk = Long.MIN_VALUE;
    private long warmupUntil;
    private BlockPos lastPlayerPos;
    private int foundSinceDebug;
    private int lastDebugHighlighted = -1;

    private final EventListener<WorldChangeEvent> onWorldChange = event -> {
        this.clearCache();
        this.startWarmup();
    };

    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        if (!this.packetTriggers.isEnabled() || mc.world == null) return;
        if (event.getPacket() instanceof ChunkDeltaUpdateS2CPacket packet) {
            packet.visitUpdates((pos, state) -> {
                this.markRevealed(pos, state);
                this.handleBlock(pos, state);
                this.queueChunk(pos);
            });
        } else if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            this.markRevealed(packet.getPos(), packet.getState());
            this.handleBlock(packet.getPos(), packet.getState());
            this.queueChunk(packet.getPos());
        } else if (event.getPacket() instanceof ChunkDataS2CPacket packet) {
            long key = ChunkPos.toLong(packet.getChunkX(), packet.getChunkZ());
            this.chunkLoadTimes.put(key, System.currentTimeMillis());
            this.queueChunk(packet.getChunkX(), packet.getChunkZ());
        } else if (event.getPacket() instanceof LightUpdateS2CPacket packet) {
            this.queueChunk(packet.getChunkX(), packet.getChunkZ());
        } else if (event.getPacket() instanceof ExplosionS2CPacket packet) {
            BlockPos center = BlockPos.ofFloored(packet.center());
            this.recentExplosions.put(center, System.currentTimeMillis());
            this.forgiveChunksAround(center, EXPLOSION_SCAN_RANGE);
            this.queueChunksAround(center, EXPLOSION_SCAN_RANGE);
        } else if (event.getPacket() instanceof UnloadChunkS2CPacket packet) {
            this.unloadChunk(packet.pos().x, packet.pos().z);
        }
    };

    private final EventListener<Render3DEvent> onRender3D = event -> this.renderTick(event);

    @Override
    public void onEnable() {
        super.onEnable();
        this.clearCache();
        this.startWarmup();
        this.queuePlayerChunks();
    }

    @Override
    public void onDisable() {
        this.clearCache();
        super.onDisable();
    }

    @Override
    public void tick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        this.detectTeleport(now);
        if (now < this.warmupUntil) {
            this.cleanupInvalidPositions();
            return;
        }
        long playerChunk = ChunkPos.toLong(mc.player.getBlockPos());
        if (playerChunk != this.lastPlayerChunk || now - this.lastFullScan >= (long)this.rescanDelay.getCurrentValue() * 1000L) {
            this.queuePlayerChunks();
            this.lastPlayerChunk = playerChunk;
            this.lastFullScan = now;
        }
        this.scanQueuedChunks((int)this.chunksPerFrame.getCurrentValue());
        if (now - this.lastCleanup >= 500L) {
            this.cleanupInvalidPositions();
            this.lastCleanup = now;
        }
        this.enforceVisibleLimit();
        if (this.chatDebug.isEnabled() && now - this.lastDebugMessage >= DEBUG_INTERVAL_MS) {
            int highlighted = this.getLimitedDebrisPositions().size();
            if (this.foundSinceDebug != 0 || highlighted != this.lastDebugHighlighted) {
                MessageUtility.info(Text.of("§6[AncientXRayV2] §fПодсвечено: §e" + highlighted + "/" + MAX_VISIBLE_DEBRIS + " §7queue=" + this.scanQueue.size()));
                this.foundSinceDebug = 0;
                this.lastDebugHighlighted = highlighted;
            }
            this.lastDebugMessage = now;
        }
        super.tick();
    }

    public List<BlockPos> getDebrisPositionsSnapshot() { return this.getLimitedDebrisPositions(); }
    public List<BlockPos> getNearestDebris(int limit) { return this.getLimitedDebrisPositions().stream().limit(limit).toList(); }
    public BlockPos getNearestDebris() { List<BlockPos> list = this.getNearestDebris(1); return list.isEmpty() ? null : list.get(0); }
    public boolean isTrustedDebrisPosition(BlockPos pos) { return this.isTrustedDebris(pos); }
    public void markFinished(BlockPos pos) { if (pos != null) this.debrisPositions.remove(pos); }
    public void trustExplosionAround(BlockPos center) {
        if (center == null) return;
        this.recentExplosions.put(center.toImmutable(), System.currentTimeMillis());
        this.forgiveChunksAround(center, EXPLOSION_SCAN_RANGE);
        this.queueChunksAround(center, EXPLOSION_SCAN_RANGE);
        this.cleanupInvalidPositions();
    }

    private void renderTick(Render3DEvent event) {
        if (mc.player == null || mc.world == null) { this.clearCache(); return; }
        this.renderDebris(event);
    }

    private void handleBlock(BlockPos pos, BlockState state) {
        if (System.currentTimeMillis() < this.warmupUntil) return;
        long chunkKey = ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4);
        if (this.suspiciousChunks.contains(chunkKey)) return;
        if (state.getBlock() == Blocks.ANCIENT_DEBRIS) {
            if (!this.isTrustedDebris(pos)) { this.debrisPositions.remove(pos); return; }
            if (this.debrisPositions.add(pos.toImmutable())) { this.foundSinceDebug++; this.enforceVisibleLimit(); }
        } else this.debrisPositions.remove(pos);
    }

    private void queuePlayerChunks() { if (mc.player != null) this.queueChunksAround(mc.player.getBlockPos(), (int)this.range.getCurrentValue()); }
    private void queueChunksAround(BlockPos center, int blockRange) {
        int centerChunkX = center.getX() >> 4, centerChunkZ = center.getZ() >> 4;
        int chunkRange = Math.max(1, (int)Math.ceil(blockRange / 16.0D));
        for (int x = centerChunkX - chunkRange; x <= centerChunkX + chunkRange; x++) for (int z = centerChunkZ - chunkRange; z <= centerChunkZ + chunkRange; z++) this.queueChunk(x, z);
    }
    private void queueChunk(BlockPos pos) { this.queueChunk(pos.getX() >> 4, pos.getZ() >> 4); }
    private void queueChunk(int chunkX, int chunkZ) { long key = ChunkPos.toLong(chunkX, chunkZ); if (this.queuedChunks.add(key)) this.scanQueue.add(key); }
    private void forgiveChunksAround(BlockPos center, int blockRange) {
        int centerChunkX = center.getX() >> 4, centerChunkZ = center.getZ() >> 4;
        int chunkRange = Math.max(1, (int)Math.ceil(blockRange / 16.0D));
        for (int x = centerChunkX - chunkRange; x <= centerChunkX + chunkRange; x++) for (int z = centerChunkZ - chunkRange; z <= centerChunkZ + chunkRange; z++) { long key = ChunkPos.toLong(x, z); this.suspiciousChunks.remove(key); this.chunkLoadTimes.put(key, 0L); }
    }

    private void scanQueuedChunks(int limit) {
        BlockPos playerPos = mc.player.getBlockPos();
        double maxDistance = this.range.getCurrentValue() * this.range.getCurrentValue();
        long now = System.currentTimeMillis();
        for (int i = 0; i < limit; i++) {
            Long key = this.scanQueue.poll();
            if (key == null) return;
            this.queuedChunks.remove(key);
            int chunkX = (int)(key >> 32);
            int chunkZ = (int)(long)key;
            Long loadedAt = this.chunkLoadTimes.get(key);
            if (loadedAt != null && now - loadedAt < NEW_CHUNK_DELAY_MS) { this.queueChunk(chunkX, chunkZ); continue; }
            if (this.suspiciousChunks.contains(key)) { this.clearChunkPositions(chunkX, chunkZ); continue; }
            try {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(chunkX, chunkZ);
                if (chunk != null) this.foundSinceDebug += this.scanChunk(chunk, playerPos, maxDistance);
                else this.unloadChunk(chunkX, chunkZ);
            } catch (Throwable ignored) { this.unloadChunk(chunkX, chunkZ); }
        }
    }

    private int scanChunk(WorldChunk chunk, BlockPos playerPos, double maxDistance) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        int minY = Math.max(mc.world.getBottomY(), playerPos.getY() - (int)this.yRange.getCurrentValue());
        int maxY = Math.min(mc.world.getTopYInclusive(), playerPos.getY() + (int)this.yRange.getCurrentValue());
        int found = 0;
        List<BlockPos> foundPositions = new ArrayList<>();
        this.clearChunkPositions(chunkX, chunkZ);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                    if (playerPos.getSquaredDistance(pos) > maxDistance) continue;
                    if (chunk.getBlockState(pos).getBlock() != Blocks.ANCIENT_DEBRIS) continue;
                    if (!this.isTrustedDebris(pos)) continue;
                    foundPositions.add(pos.toImmutable());
                    found++;
                }
            }
        }
        if (found > MAX_DEBRIS_PER_CHUNK) {
            this.suspiciousChunks.add(ChunkPos.toLong(chunkX, chunkZ));
            foundPositions.clear();
            return 0;
        }
        this.debrisPositions.addAll(foundPositions);
        this.enforceVisibleLimit();
        return found;
    }

    private void cleanupInvalidPositions() {
        if (mc.world == null || mc.player == null) { this.debrisPositions.clear(); return; }
        BlockPos playerPos = mc.player.getBlockPos();
        double maxDistance = this.range.getCurrentValue() * this.range.getCurrentValue();
        this.debrisPositions.removeIf(pos -> playerPos.getSquaredDistance(pos) > maxDistance
                || !mc.world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)
                || mc.world.getBlockState(pos).getBlock() != Blocks.ANCIENT_DEBRIS
                || !this.isTrustedDebris(pos));
        long now = System.currentTimeMillis();
        this.recentExplosions.entrySet().removeIf(e -> now - e.getValue() > EXPLOSION_TRUST_MS);
        this.revealedDebris.entrySet().removeIf(e -> now - e.getValue() > EXPLOSION_TRUST_MS);
    }

    private boolean isTrustedDebris(BlockPos pos) {
        if (mc.world == null) return false;
        if (!this.strictTrust.isEnabled()) return true;
        if (pos.getY() < 8 || pos.getY() > 119) return false;
        Long revealedAt = this.revealedDebris.get(pos);
        boolean revealed = revealedAt != null && System.currentTimeMillis() - revealedAt <= EXPLOSION_TRUST_MS;
        return (revealed || this.isNearRecentExplosion(pos)) && this.hasAirSide(pos) && !this.hasDangerSide(pos) && !this.hasDenseLocalCluster(pos);
    }

    private void markRevealed(BlockPos pos, BlockState state) { if (state.getBlock() == Blocks.ANCIENT_DEBRIS) this.revealedDebris.put(pos.toImmutable(), System.currentTimeMillis()); else this.revealedDebris.remove(pos); }
    private boolean isNearRecentExplosion(BlockPos pos) { long now = System.currentTimeMillis(); for (Map.Entry<BlockPos, Long> e : this.recentExplosions.entrySet()) if (now - e.getValue() <= EXPLOSION_TRUST_MS && e.getKey().getSquaredDistance(pos) <= EXPLOSION_TRUST_DISTANCE_SQUARED) return true; return false; }
    private boolean hasAirSide(BlockPos pos) { for (Direction d : Direction.values()) if (mc.world.getBlockState(pos.offset(d)).isAir()) return true; return false; }
    private boolean hasDangerSide(BlockPos pos) { for (Direction d : Direction.values()) { BlockState s = mc.world.getBlockState(pos.offset(d)); if (s.isOf(Blocks.LAVA) || s.isOf(Blocks.FIRE) || s.isOf(Blocks.SOUL_FIRE) || s.isOf(Blocks.WATER)) return true; } return false; }
    private boolean hasDenseLocalCluster(BlockPos pos) { int nearby = 1; for (BlockPos cached : this.debrisPositions) if (!cached.equals(pos) && cached.getSquaredDistance(pos) <= LOCAL_DEBRIS_DISTANCE_SQUARED && ++nearby > MAX_LOCAL_DEBRIS) return true; return false; }

    private void renderDebris(Render3DEvent event) {
        List<BlockPos> positions = this.getLimitedDebrisPositions();
        BlockPos botTarget = this.getBotTargetDebris();
        BlockPos botVisual = this.getBotVisualTarget();
        if (positions.isEmpty() && botTarget == null && botVisual == null) return;
        MatrixStack matrices = event.getMatrices();
        Vec3d cameraPos = event.getCamera().getPos();
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.lineWidth(this.lineWidth.getCurrentValue());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder fillBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        ColorRGBA fillColor = new ColorRGBA(DEFAULT_R, DEFAULT_G, DEFAULT_B, this.fill.isEnabled() ? 45.0f : 0.0f);
        for (BlockPos pos : positions) if (this.fill.isEnabled()) Draw3DUtility.renderFilledBox(matrices, fillBuffer, new Box(pos).expand(0.01), fillColor);
        if (botTarget != null && this.validRenderTarget(botTarget)) Draw3DUtility.renderFilledBox(matrices, fillBuffer, new Box(botTarget).expand(0.02), new ColorRGBA(BOT_R, BOT_G, BOT_B, 70.0f));
        BuiltBuffer builtFill = fillBuffer.endNullable();
        if (builtFill != null) BufferRenderer.drawWithGlobalProgram(builtFill);
        BufferBuilder lineBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
        for (BlockPos pos : positions) Draw3DUtility.renderOutlinedBox(matrices, lineBuffer, new Box(pos).expand(0.01), new ColorRGBA(DEFAULT_R, DEFAULT_G, DEFAULT_B, 220.0f));
        if (botTarget != null && this.validRenderTarget(botTarget)) Draw3DUtility.renderOutlinedBox(matrices, lineBuffer, new Box(botTarget).expand(0.02), new ColorRGBA(BOT_R, BOT_G, BOT_B, 240.0f));
        if (botVisual != null) Draw3DUtility.renderOutlinedBox(matrices, lineBuffer, new Box(botVisual).expand(0.03), new ColorRGBA(BOT_R, BOT_G, BOT_B, 180.0f));
        BuiltBuffer builtLine = lineBuffer.endNullable();
        if (builtLine != null) BufferRenderer.drawWithGlobalProgram(builtLine);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private boolean validRenderTarget(BlockPos pos) { return mc.world != null && mc.world.getBlockState(pos).isOf(Blocks.ANCIENT_DEBRIS); }
    private BlockPos getBotTargetDebris() { try { AncientBot bot = Rockstar.getInstance().getModuleManager().getModule(AncientBot.class); return bot != null && bot.isEnabled() ? bot.getTargetDebris() : null; } catch (Throwable ignored) { return null; } }
    private BlockPos getBotVisualTarget() { try { AncientBot bot = Rockstar.getInstance().getModuleManager().getModule(AncientBot.class); return bot != null && bot.isEnabled() ? bot.getVisualTarget() : null; } catch (Throwable ignored) { return null; } }
    private void clearChunkPositions(int chunkX, int chunkZ) { this.debrisPositions.removeIf(pos -> (pos.getX() >> 4) == chunkX && (pos.getZ() >> 4) == chunkZ); }
    private void unloadChunk(int chunkX, int chunkZ) { long key = ChunkPos.toLong(chunkX, chunkZ); this.clearChunkPositions(chunkX, chunkZ); this.queuedChunks.remove(key); this.suspiciousChunks.remove(key); this.chunkLoadTimes.remove(key); }
    private void detectTeleport(long now) { BlockPos playerPos = mc.player.getBlockPos(); if (this.lastPlayerPos != null && this.lastPlayerPos.getSquaredDistance(playerPos) > TELEPORT_DISTANCE_SQUARED) { this.clearCache(); this.startWarmup(now); } this.lastPlayerPos = playerPos.toImmutable(); }
    private void startWarmup() { this.startWarmup(System.currentTimeMillis()); }
    private void startWarmup(long now) { this.warmupUntil = now + WORLD_WARMUP_MS; this.lastFullScan = now; this.lastCleanup = now; }
    private List<BlockPos> getLimitedDebrisPositions() { if (mc.player == null) return this.debrisPositions.stream().limit(MAX_VISIBLE_DEBRIS).toList(); BlockPos playerPos = mc.player.getBlockPos(); return this.debrisPositions.stream().sorted(Comparator.comparingDouble(playerPos::getSquaredDistance)).limit(MAX_VISIBLE_DEBRIS).toList(); }
    private void enforceVisibleLimit() { if (this.debrisPositions.size() <= MAX_VISIBLE_DEBRIS) return; Set<BlockPos> keep = ConcurrentHashMap.newKeySet(); keep.addAll(this.getLimitedDebrisPositions()); this.debrisPositions.removeIf(pos -> !keep.contains(pos)); }
    private void clearCache() { this.debrisPositions.clear(); this.queuedChunks.clear(); this.suspiciousChunks.clear(); this.chunkLoadTimes.clear(); this.revealedDebris.clear(); this.recentExplosions.clear(); this.scanQueue.clear(); this.lastFullScan = 0L; this.lastCleanup = 0L; this.lastDebugMessage = 0L; this.lastPlayerChunk = Long.MIN_VALUE; this.lastPlayerPos = mc.player == null ? null : mc.player.getBlockPos().toImmutable(); this.foundSinceDebug = 0; this.lastDebugHighlighted = -1; }
    @Generated public Set<BlockPos> getDebrisPositions() { return this.debrisPositions; }
}
