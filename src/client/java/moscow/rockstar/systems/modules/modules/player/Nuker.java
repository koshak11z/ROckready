/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
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
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package moscow.rockstar.systems.modules.modules.player;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationPriority;
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
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@ModuleInfo(name="Nuker", category=ModuleCategory.PLAYER, desc="\u041a\u043e\u043f\u0430\u0435\u0442 \u0442\u0435\u0440\u0440\u0438\u0442\u043e\u0440\u0438\u044e \u0432\u043e\u043a\u0440\u0443\u0433 \u0441\u0435\u0431\u044f \u043d\u0430 \u0430\u0432\u0442\u043e-\u0448\u0430\u0445\u0442\u0435")
public class Nuker
extends BaseModule {
    private final SliderSetting xzDistance = new SliderSetting((SettingsContainer)this, "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f XZ", "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f, \u043d\u0430 \u043a\u043e\u0442\u043e\u0440\u043e\u0439 \u0431\u0443\u0434\u0435\u0442 \u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c " + this.getName() + " \u043f\u043e \u0433\u043e\u0440\u0438\u0437\u043e\u043d\u0442\u0430\u043b\u0438").step(1.0f).min(2.0f).max(6.0f).currentValue(4.0f);
    private final SliderSetting yDistance = new SliderSetting((SettingsContainer)this, "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f Y", "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f, \u043d\u0430 \u043a\u043e\u0442\u043e\u0440\u043e\u0439 \u0431\u0443\u0434\u0435\u0442 \u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c " + this.getName() + " \u043f\u043e \u0432\u0435\u0440\u0442\u0438\u043a\u0430\u043b\u0438").step(1.0f).min(2.0f).max(6.0f).currentValue(5.0f);
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        BlockPos pos;
        BlockPos offset;
        int z;
        int x;
        int y;
        int radius = this.range();
        BlockPos minPos = new BlockPos(-71, 77, -15);
        BlockPos maxPos = new BlockPos(-51, 86, 5);
        boolean spawn = ServerUtility.spawn();
        for (y = 0; y < radius * 2; ++y) {
            for (x = 0; x < radius * 2; ++x) {
                for (z = 0; z < radius * 2; ++z) {
                    offset = new BlockPos((x % 2 == 0 ? -x : x) / 2, (y % 2 == 0 ? -y : y) / 2, (z % 2 == 0 ? -z : z) / 2);
                    pos = Nuker.mc.player.getBlockPos().add((Vec3i)offset);
                    if ((pos.getX() < minPos.getX() || pos.getX() > maxPos.getX() || pos.getY() < minPos.getY() || pos.getY() > maxPos.getY() || pos.getZ() < minPos.getZ() || pos.getZ() > maxPos.getZ()) && spawn || Nuker.mc.world.getBlockState(pos).getBlock() != Blocks.DIAMOND_ORE) continue;
                    double posX = pos.getX();
                    double posY = pos.getY();
                    double posZ = pos.getZ();
                    double deltaX = posX - Nuker.mc.player.getX();
                    double deltaY = posY - (Nuker.mc.player.getY() + (double)Nuker.mc.player.getEyeHeight(Nuker.mc.player.getPose()));
                    double deltaZ = posZ - Nuker.mc.player.getZ();
                    double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f + MathUtility.random(-2.0, 2.0);
                    float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance))) + MathUtility.random(-1.0, 1.0);
                    Nuker.mc.player.networkHandler.sendPacket((Packet)new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
                    Rockstar.getInstance().getRotationHandler().rotate(new Rotation(yaw, pitch), MoveCorrection.SILENT, 180.0f, 180.0f, 180.0f, RotationPriority.NORMAL);
                    Direction direction = Nuker.getDirection(pos);
                    Nuker.mc.interactionManager.updateBlockBreakingProgress(pos, direction);
                    Nuker.mc.player.swingHand(Hand.MAIN_HAND);
                    return;
                }
            }
        }
        y = 0;
        while ((float)y < this.yDistance.getCurrentValue()) {
            for (x = 0; x < radius * 2; ++x) {
                for (z = 0; z < radius * 2; ++z) {
                    offset = new BlockPos((x % 2 == 0 ? -x : x) / 2, y, (z % 2 == 0 ? -z : z) / 2);
                    pos = Nuker.mc.player.getBlockPos().up().add((Vec3i)offset);
                    if ((pos.getX() < minPos.getX() || pos.getX() > maxPos.getX() || pos.getY() < minPos.getY() || pos.getY() > maxPos.getY() || pos.getZ() < minPos.getZ() || pos.getZ() > maxPos.getZ()) && spawn || Nuker.mc.world.getBlockState(pos).getBlock() == Blocks.AIR) continue;
                    double posX = pos.getX();
                    double posY = pos.getY();
                    double posZ = pos.getZ();
                    double deltaX = posX - Nuker.mc.player.getX();
                    double deltaY = posY - (Nuker.mc.player.getY() + (double)Nuker.mc.player.getEyeHeight(Nuker.mc.player.getPose()));
                    double deltaZ = posZ - Nuker.mc.player.getZ();
                    double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f + MathUtility.random(-2.0, 2.0);
                    float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance))) + MathUtility.random(-1.0, 1.0);
                    Rockstar.getInstance().getRotationHandler().rotate(new Rotation(yaw, pitch), MoveCorrection.SILENT, 180.0f, 180.0f, 180.0f, RotationPriority.NORMAL);
                    Direction direction = Nuker.getDirection(pos);
                    Nuker.mc.interactionManager.updateBlockBreakingProgress(pos, direction);
                    Nuker.mc.player.swingHand(Hand.MAIN_HAND);
                    return;
                }
            }
            ++y;
        }
    };
    private final EventListener<Render3DEvent> on3DRender = event -> {
        BlockPos pos;
        BlockPos additional;
        int z;
        int x;
        int y;
        if (Nuker.mc.world == null || Nuker.mc.player == null) {
            return;
        }
        MatrixStack matrices = event.getMatrices();
        Camera camera = Nuker.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.lineWidth((float)10.0f);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        matrices.push();
        matrices.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        int radius = this.range();
        BlockPos minPos = new BlockPos(-71, 77, -15);
        BlockPos maxPos = new BlockPos(-51, 86, 5);
        boolean spawn = ServerUtility.spawn();
        if (spawn) {
            Draw3DUtility.renderOutlinedBox(event.getMatrices(), buffer, new Box((double)minPos.getX(), (double)minPos.getY(), (double)minPos.getZ(), (double)(maxPos.getX() + 1), (double)(maxPos.getY() + 1), (double)(maxPos.getZ() + 1)), ColorRGBA.GREEN.withAlpha(110.0f));
        }
        for (y = 0; y < radius * 2; ++y) {
            for (x = 0; x < radius * 2; ++x) {
                for (z = 0; z < radius * 2; ++z) {
                    additional = new BlockPos((x % 2 == 0 ? -x : x) / 2, (y % 2 == 0 ? -y : y) / 2, (z % 2 == 0 ? -z : z) / 2);
                    pos = Nuker.mc.player.getBlockPos().add((Vec3i)additional);
                    if ((pos.getX() < minPos.getX() || pos.getX() > maxPos.getX() || pos.getY() < minPos.getY() || pos.getY() > maxPos.getY() || pos.getZ() < minPos.getZ() || pos.getZ() > maxPos.getZ()) && spawn || Nuker.mc.world.getBlockState(pos).getBlock() != Blocks.DIAMOND_ORE) continue;
                    Direction direction = Nuker.getDirection(pos);
                    Draw3DUtility.renderOutlinedBox(event.getMatrices(), buffer, Nuker.mc.world.getBlockState(pos).getCullingShape().getBoundingBox().offset(pos), ColorRGBA.GREEN.withAlpha(250.0f));
                    Draw3DUtility.renderBoxInternalDiagonals(event.getMatrices(), buffer, Nuker.mc.world.getBlockState(pos).getCullingShape().getBoundingBox().offset(pos), ColorRGBA.GREEN.withAlpha(250.0f));
                    BuiltBuffer builtBuffer = buffer.endNullable();
                    if (builtBuffer != null) {
                        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
                    }
                    RenderSystem.enableCull();
                    RenderSystem.enableDepthTest();
                    RenderSystem.disableBlend();
                    return;
                }
            }
        }
        y = 0;
        while ((float)y < this.yDistance.getCurrentValue()) {
            for (x = 0; x < radius * 2; ++x) {
                for (z = 0; z < radius * 2; ++z) {
                    additional = new BlockPos((x % 2 == 0 ? -x : x) / 2, y, (z % 2 == 0 ? -z : z) / 2);
                    pos = Nuker.mc.player.getBlockPos().up().add((Vec3i)additional);
                    if ((pos.getX() < minPos.getX() || pos.getX() > maxPos.getX() || pos.getY() < minPos.getY() || pos.getY() > maxPos.getY() || pos.getZ() < minPos.getZ() || pos.getZ() > maxPos.getZ()) && spawn || Nuker.mc.world.getBlockState(pos).getBlock() == Blocks.AIR) continue;
                    Direction direction = Nuker.getDirection(pos);
                    Draw3DUtility.renderOutlinedBox(event.getMatrices(), buffer, Nuker.mc.world.getBlockState(pos).getCullingShape().getBoundingBox().offset(pos), ColorRGBA.GREEN.withAlpha(250.0f));
                    Draw3DUtility.renderBoxInternalDiagonals(event.getMatrices(), buffer, Nuker.mc.world.getBlockState(pos).getCullingShape().getBoundingBox().offset(pos), ColorRGBA.GREEN.withAlpha(250.0f));
                    BuiltBuffer builtBuffer = buffer.endNullable();
                    if (builtBuffer != null) {
                        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
                    }
                    RenderSystem.enableCull();
                    RenderSystem.enableDepthTest();
                    RenderSystem.disableBlend();
                    return;
                }
            }
            ++y;
        }
        BuiltBuffer builtBuffer = buffer.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
        }
        matrices.pop();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };

    public int range() {
        return (int)this.xzDistance.getCurrentValue();
    }

    public static Direction getDirection(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(Nuker.mc.player.getX(), Nuker.mc.player.getY() + (double)Nuker.mc.player.getEyeHeight(Nuker.mc.player.getPose()), Nuker.mc.player.getZ());
        if ((double)pos.getY() > eyesPos.y) {
            if (Nuker.mc.world.getBlockState(pos.add(0, -1, 0)).isReplaceable()) {
                return Direction.DOWN;
            }
            return Nuker.mc.player.getHorizontalFacing().getOpposite();
        }
        if (!Nuker.mc.world.getBlockState(pos.add(0, 1, 0)).isReplaceable()) {
            return Nuker.mc.player.getHorizontalFacing().getOpposite();
        }
        return Direction.UP;
    }
}

