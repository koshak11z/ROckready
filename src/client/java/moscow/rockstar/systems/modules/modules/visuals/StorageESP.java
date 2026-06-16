/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.entity.BarrelBlockEntity
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.block.entity.ChestBlockEntity
 *  net.minecraft.block.entity.DispenserBlockEntity
 *  net.minecraft.block.entity.DropperBlockEntity
 *  net.minecraft.block.entity.EnderChestBlockEntity
 *  net.minecraft.block.entity.FurnaceBlockEntity
 *  net.minecraft.block.entity.HopperBlockEntity
 *  net.minecraft.block.entity.ShulkerBoxBlockEntity
 *  net.minecraft.block.entity.TrappedChestBlockEntity
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.vehicle.ChestMinecartEntity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockView
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.WorldUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.RenderUtility;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

@ModuleInfo(name="Storage ESP", category=ModuleCategory.VISUALS)
public class StorageESP
extends BaseModule {
    private static final Box FULL_BOX = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private static final Box EMPTY_BOX = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private final SelectSetting blocks = new SelectSetting(this, "modules.settings.storage_esp.blocks");
    private final SelectSetting.Value chests = new SelectSetting.Value(this.blocks, "modules.settings.storage_esp.blocks.chests").select();
    private final SelectSetting.Value enderChests = new SelectSetting.Value(this.blocks, "modules.settings.storage_esp.blocks.ender_chests").select();
    private final SelectSetting.Value trappedChests = new SelectSetting.Value(this.blocks, "modules.settings.storage_esp.blocks.trapped_chests");
    private final SelectSetting.Value furnaces = new SelectSetting.Value(this.blocks, "modules.settings.storage_esp.blocks.furnaces");
    private final SelectSetting.Value barrels = new SelectSetting.Value(this.blocks, "modules.settings.storage_esp.blocks.barrels").select();
    private final SelectSetting.Value minecart = new SelectSetting.Value(this.blocks, "modules.settings.storage_esp.blocks.minecart").select();
    private final SelectSetting.Value shulkers = new SelectSetting.Value(this.blocks, "modules.settings.storage_esp.blocks.shulkers").select();
    private final SelectSetting.Value droppers = new SelectSetting.Value(this.blocks, "modules.settings.storage_esp.blocks.droppers");
    private final SelectSetting.Value dispensers = new SelectSetting.Value(this.blocks, "modules.settings.storage_esp.blocks.dispensers");
    private final SelectSetting.Value hoppers = new SelectSetting.Value(this.blocks, "modules.settings.storage_esp.blocks.hoppers");
    private final SelectSetting renderMode = new SelectSetting(this, "modules.settings.storage_esp.render");
    private final SelectSetting.Value fill = new SelectSetting.Value(this.renderMode, "modules.settings.storage_esp.render.fill").select();
    private final SelectSetting.Value outline = new SelectSetting.Value(this.renderMode, "modules.settings.storage_esp.render.outline").select();
    private final SelectSetting.Value diagonals = new SelectSetting.Value(this.renderMode, "modules.settings.storage_esp.render.diagonals").select();
    private final SelectSetting.Value lines = new SelectSetting.Value(this.renderMode, "modules.settings.storage_esp.render.lines");
    private final SliderSetting maxDistance = new SliderSetting((SettingsContainer)this, "modules.settings.storage_esp.max_distance", "modules.settings.storage_esp.max_distance.description").min(5.0f).max(128.0f).step(1.0f).currentValue(128.0f);
    private final EventListener<Render3DEvent> on3DRender = event -> {
        if (StorageESP.mc.world == null || StorageESP.mc.player == null) {
            return;
        }
        MatrixStack matrices = event.getMatrices();
        Camera camera = StorageESP.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder quadsBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (BlockEntity blockEntity : WorldUtility.blockEntities) {
            if (!this.isValidEntity(blockEntity)) continue;
            List<Box> boundingBoxes = this.getBoundingBox(blockEntity);
            for (Box box : boundingBoxes) {
                if (!this.fill.isSelected()) continue;
                Draw3DUtility.renderFilledBox(matrices, quadsBuffer, box.offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()), this.getBlockColor(blockEntity).withAlpha(50.0f));
            }
        }
        for (Entity entity : StorageESP.mc.world.getEntities()) {
            if (!this.isValidCart(entity)) continue;
            Box boundingBox = entity.getBoundingBox();
            if (!this.fill.isSelected()) continue;
            Draw3DUtility.renderFilledBox(matrices, quadsBuffer, boundingBox.offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()), this.getEntityColor(entity).withAlpha(50.0f));
        }
        RenderUtility.buildBuffer(quadsBuffer);
        BufferBuilder linesBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (BlockEntity blockEntity : WorldUtility.blockEntities) {
            if (!this.isValidEntity(blockEntity)) continue;
            List<Box> boundingBoxes = this.getBoundingBox(blockEntity);
            for (Box boundingBox : boundingBoxes) {
                if (this.diagonals.isSelected()) {
                    Draw3DUtility.renderBoxInternalDiagonals(matrices, linesBuffer, boundingBox.offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()), this.getBlockColor(blockEntity).withAlpha(100.0f));
                }
                if (this.outline.isSelected()) {
                    Draw3DUtility.renderOutlinedBox(matrices, linesBuffer, boundingBox.offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()), this.getBlockColor(blockEntity).withAlpha(100.0f));
                }
                if (!this.lines.isSelected()) continue;
                Vec3d entityPos = blockEntity.getPos().toCenterPos();
                Draw3DUtility.renderLineFromPlayer(matrices, linesBuffer, entityPos, this.getBlockColor(blockEntity));
            }
        }
        for (Entity entity : StorageESP.mc.world.getEntities()) {
            if (!this.isValidCart(entity)) continue;
            Box boundingBox = entity.getBoundingBox();
            if (this.diagonals.isSelected()) {
                Draw3DUtility.renderBoxInternalDiagonals(matrices, linesBuffer, boundingBox.offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()), this.getEntityColor(entity).withAlpha(100.0f));
            }
            if (this.outline.isSelected()) {
                Draw3DUtility.renderOutlinedBox(matrices, linesBuffer, boundingBox.offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()), this.getEntityColor(entity).withAlpha(100.0f));
            }
            if (!this.lines.isSelected()) continue;
            Vec3d vec3d = entity.getPos();
            Draw3DUtility.renderLineFromPlayer(matrices, linesBuffer, vec3d, this.getEntityColor(entity));
        }
        RenderUtility.buildBuffer(linesBuffer);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };
    private final EventListener<WorldChangeEvent> onWorldChange = event -> WorldUtility.blockEntities.clear();

    private List<Box> getBoundingBox(BlockEntity blockEntity) {
        if (StorageESP.mc.world == null) {
            return List.of(EMPTY_BOX);
        }
        BlockPos blockPos = blockEntity.getPos();
        BlockState blockState = StorageESP.mc.world.getBlockState(blockPos);
        VoxelShape shape = blockState.getOutlineShape((BlockView)StorageESP.mc.world, blockPos);
        if (shape.isEmpty()) {
            return List.of(FULL_BOX.offset(blockPos));
        }
        return shape.getBoundingBoxes().stream().map(box -> box.offset(blockPos)).toList();
    }

    private boolean isValidEntity(BlockEntity entity) {
        double maxDistSq = this.maxDistance.getCurrentValue() * this.maxDistance.getCurrentValue();
        if (StorageESP.mc.player == null || StorageESP.mc.player.squaredDistanceTo(entity.getPos().toCenterPos()) > maxDistSq) {
            return false;
        }
        if (entity instanceof ChestBlockEntity && this.chests.isSelected()) {
            return true;
        }
        if (entity instanceof EnderChestBlockEntity && this.enderChests.isSelected()) {
            return true;
        }
        if (entity instanceof TrappedChestBlockEntity && this.trappedChests.isSelected()) {
            return true;
        }
        if (entity instanceof FurnaceBlockEntity && this.furnaces.isSelected()) {
            return true;
        }
        if (entity instanceof BarrelBlockEntity && this.barrels.isSelected()) {
            return true;
        }
        if (entity instanceof ShulkerBoxBlockEntity && this.shulkers.isSelected()) {
            return true;
        }
        if (entity instanceof DropperBlockEntity && this.droppers.isSelected()) {
            return true;
        }
        if (entity instanceof DispenserBlockEntity && this.dispensers.isSelected()) {
            return true;
        }
        return entity instanceof HopperBlockEntity && this.hoppers.isSelected();
    }

    private ColorRGBA getBlockColor(BlockEntity entity) {
        if (entity instanceof ChestBlockEntity) {
            return new ColorRGBA(255.0f, 131.0f, 54.0f);
        }
        if (entity instanceof EnderChestBlockEntity) {
            return new ColorRGBA(121.0f, 54.0f, 255.0f);
        }
        if (entity instanceof TrappedChestBlockEntity) {
            return new ColorRGBA(255.0f, 101.0f, 54.0f);
        }
        if (entity instanceof FurnaceBlockEntity) {
            return new ColorRGBA(126.0f, 126.0f, 126.0f);
        }
        if (entity instanceof BarrelBlockEntity) {
            return new ColorRGBA(255.0f, 185.0f, 54.0f);
        }
        if (entity instanceof ShulkerBoxBlockEntity) {
            return new ColorRGBA(181.0f, 54.0f, 255.0f);
        }
        if (entity instanceof DropperBlockEntity) {
            return new ColorRGBA(100.0f, 100.0f, 100.0f);
        }
        if (entity instanceof DispenserBlockEntity) {
            return new ColorRGBA(100.0f, 100.0f, 100.0f);
        }
        if (entity instanceof HopperBlockEntity) {
            return new ColorRGBA(100.0f, 100.0f, 100.0f);
        }
        return Colors.WHITE;
    }

    private ColorRGBA getEntityColor(Entity entity) {
        if (entity instanceof ChestMinecartEntity) {
            return new ColorRGBA(255.0f, 200.0f, 100.0f);
        }
        return Colors.WHITE;
    }

    private boolean isValidCart(Entity entity) {
        double maxDistSq = this.maxDistance.getCurrentValue() * this.maxDistance.getCurrentValue();
        if (StorageESP.mc.player == null || StorageESP.mc.player.squaredDistanceTo(entity.getPos()) > maxDistSq) {
            return false;
        }
        return entity instanceof ChestMinecartEntity && this.minecart.isSelected();
    }
}

