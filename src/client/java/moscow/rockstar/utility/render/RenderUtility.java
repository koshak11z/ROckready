/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IWindow;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public final class RenderUtility
implements IMinecraft,
IWindow {
    public static void rotate(MatrixStack ms, float x, float y, float value) {
        ms.push();
        ms.translate(x, y, 0.0f);
        ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(value));
        ms.translate(-x, -y, 0.0f);
    }

    public static void scale(MatrixStack ms, float x, float y, float scale) {
        ms.push();
        ms.translate(x, y, 0.0f);
        ms.scale(scale, scale, 1.0f);
        ms.translate(-x, -y, 0.0f);
    }

    public static void end(MatrixStack ms) {
        ms.pop();
    }

    public static void prepareMatrices(MatrixStack matrices) {
        Camera camera = RenderUtility.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        Vec3d renderPos = Vec3d.ZERO.subtract(cameraPos);
        matrices.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
    }

    public static void prepareMatrices(MatrixStack matrices, Vec3d pos) {
        Camera camera = RenderUtility.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        Vec3d renderPos = pos.subtract(cameraPos);
        matrices.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
    }

    public static void setupRender3D(boolean bloomColor) {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask((boolean)false);
        if (bloomColor) {
            RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
    }

    public static void endRender3D() {
        RenderSystem.depthMask((boolean)true);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    public static void buildBuffer(BufferBuilder builder) {
        BuiltBuffer builtBuffer = builder.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
        }
    }

    @Generated
    private RenderUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

