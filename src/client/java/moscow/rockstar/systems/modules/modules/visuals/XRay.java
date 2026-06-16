/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockView
 *  net.minecraft.world.chunk.WorldChunk
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.WorldChunk;

@ModuleInfo(name="XRay", category=ModuleCategory.VISUALS, desc="\u041f\u043e\u0434\u0441\u0432\u0435\u0447\u0438\u0432\u0430\u0435\u0442 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d\u043d\u044b\u0435 \u0440\u0443\u0434\u044b")
public class XRay
extends BaseModule {
    private final Set<BlockPos> cachedBlocks = ConcurrentHashMap.newKeySet();
    private final SelectSetting blocks = new SelectSetting(this, "\u0420\u0443\u0434\u044b");
    public final SelectSetting.Value diamondOre = new SelectSetting.Value(this.blocks, "\u0410\u043b\u043c\u0430\u0437\u043d\u0430\u044f \u0440\u0443\u0434\u0430");
    public final SelectSetting.Value ironOre = new SelectSetting.Value(this.blocks, "\u0416\u0435\u043b\u0435\u0437\u043d\u0430\u044f \u0440\u0443\u0434\u0430");
    public final SelectSetting.Value goldOre = new SelectSetting.Value(this.blocks, "\u0417\u043e\u043b\u043e\u0442\u0430\u044f \u0440\u0443\u0434\u0430");
    public final SelectSetting.Value ancientOre = new SelectSetting.Value(this.blocks, "\u041e\u0431\u043b\u043e\u043c\u043a\u0438");
    public final SelectSetting.Value lapisOre = new SelectSetting.Value(this.blocks, "\u041b\u0430\u0437\u0443\u0440\u0438\u0442\u043e\u0432\u0430\u044f \u0440\u0443\u0434\u0430");
    private int diamonds = 0;
    private int ancient = 0;
    private int gold = 0;
    private int lapis = 0;
    private final EventListener<Render3DEvent> onHudRenderEvent = event -> {
        if (XRay.mc.world == null || XRay.mc.player == null) {
            return;
        }
        MatrixStack matrices = event.getMatrices();
        Camera camera = XRay.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        matrices.push();
        matrices.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.lineWidth((float)10.0f);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        double maxDistSq = 999999.0;
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (BlockPos pos : this.cachedBlocks) {
            if (XRay.mc.player.squaredDistanceTo(pos.toCenterPos()) > maxDistSq) continue;
            Box boundingBox = this.getBoundingBox(pos);
            Block block = XRay.mc.world.getBlockState(pos).getBlock();
            Draw3DUtility.renderFilledBox(event.getMatrices(), buffer, boundingBox, this.getBlockColor(block).withAlpha(30.0f));
        }
        BuiltBuffer builtBuffer = buffer.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
        }
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrices.pop();
    };
    private final EventListener<WorldChangeEvent> onWorldChange = event -> this.cachedBlocks.clear();

    public void scanChunk(WorldChunk chunk) {
        if (XRay.mc.world == null || chunk == null) {
            return;
        }
        int chunkX = chunk.getPos().getStartX();
        int chunkZ = chunk.getPos().getStartZ();
        for (int x = 0; x < 16; ++x) {
            for (int y = XRay.mc.world.getBottomY(); y < XRay.mc.world.getTopYInclusive(); ++y) {
                for (int z = 0; z < 16; ++z) {
                    BlockPos pos = new BlockPos(chunkX + x, y, chunkZ + z);
                    BlockState state = chunk.getBlockState(pos);
                    if (state.isAir() || !this.isBlockEnabled(state.getBlock())) continue;
                    this.cachedBlocks.add(pos);
                }
            }
        }
    }

    @Override
    public void onEnable() {
        if (!EntityUtility.isInGame()) {
            return;
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.cachedBlocks.clear();
        this.diamonds = 0;
        this.ancient = 0;
        this.gold = 0;
        this.lapis = 0;
        super.onDisable();
    }

    @Override
    public void tick() {
        this.countBlocks();
        this.removeInvalidBlocks();
        super.tick();
    }

    private void removeInvalidBlocks() {
        this.cachedBlocks.removeIf(pos -> !this.isInRenderDistance((BlockPos)pos));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void countBlocks() {
        int d = 0;
        int a = 0;
        int g = 0;
        int l = 0;
        Set<BlockPos> set = this.cachedBlocks;
        synchronized (set) {
            for (BlockPos pos : this.cachedBlocks) {
                Block block = XRay.mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.DIAMOND_ORE && this.diamondOre.isSelected()) {
                    ++d;
                    continue;
                }
                if (block == Blocks.ANCIENT_DEBRIS) {
                    ++a;
                    continue;
                }
                if (block == Blocks.GOLD_ORE && this.goldOre.isSelected()) {
                    ++g;
                    continue;
                }
                if (block != Blocks.LAPIS_ORE || !this.lapisOre.isSelected()) continue;
                ++l;
            }
        }
        this.diamonds = d;
        this.ancient = a;
        this.gold = g;
        this.lapis = l;
    }

    private ColorRGBA getBlockColor(Block block) {
        if (block == Blocks.ANCIENT_DEBRIS) {
            return new ColorRGBA(255.0f, 131.0f, 54.0f);
        }
        if (block == Blocks.DIAMOND_ORE) {
            return new ColorRGBA(121.0f, 54.0f, 255.0f);
        }
        if (block == Blocks.GOLD_ORE) {
            return new ColorRGBA(255.0f, 215.0f, 0.0f);
        }
        if (block == Blocks.LAPIS_ORE) {
            return new ColorRGBA(0.0f, 71.0f, 179.0f);
        }
        return Colors.WHITE;
    }

    private boolean isInRenderDistance(BlockPos pos) {
        return true;
    }

    private Box getBoundingBox(BlockPos blockEntity) {
        BlockState blockState = XRay.mc.world.getBlockState(blockEntity);
        VoxelShape shape = blockState.getOutlineShape((BlockView)XRay.mc.world, blockEntity);
        if (shape.isEmpty()) {
            return new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(blockEntity);
        }
        return shape.getBoundingBox().offset(blockEntity);
    }

    public boolean isBlockEnabled(Block block) {
        if (block == Blocks.DIAMOND_ORE && this.diamondOre.isSelected()) {
            return true;
        }
        if (block == Blocks.IRON_ORE && this.ironOre.isSelected()) {
            return true;
        }
        if (block == Blocks.GOLD_ORE && this.goldOre.isSelected()) {
            return true;
        }
        if (block == Blocks.LAPIS_ORE && this.lapisOre.isSelected()) {
            return true;
        }
        return block == Blocks.ANCIENT_DEBRIS && this.ancientOre.isSelected();
    }

    @Generated
    public Set<BlockPos> getCachedBlocks() {
        return this.cachedBlocks;
    }

    @Generated
    public int getDiamonds() {
        return this.diamonds;
    }

    @Generated
    public int getAncient() {
        return this.ancient;
    }

    @Generated
    public int getGold() {
        return this.gold;
    }

    @Generated
    public int getLapis() {
        return this.lapis;
    }
}

