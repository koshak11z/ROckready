/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.client.util.math.MatrixStack$Entry
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package moscow.rockstar.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Draw3DUtility
implements IMinecraft {
    public static void renderGlowingBox(MatrixStack matrices, BufferBuilder buffer, Box box, ColorRGBA color) {
        float r = color.getRed();
        float g = color.getGreen();
        float b = color.getBlue();
        float baseAlpha = color.getAlpha();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        int glowLayers = 3;
        float glowStep = 0.1f;
        for (int i = glowLayers; i >= 1; --i) {
            float expand = (float)i * glowStep;
            float alpha = baseAlpha * (0.15f / (float)i);
            Draw3DUtility.renderFilledBox(matrices, buffer, box.expand((double)expand), new ColorRGBA(r, g, b, alpha));
        }
        Draw3DUtility.renderFilledBox(matrices, buffer, box, new ColorRGBA(r, g, b, baseAlpha));
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderFilledBox(MatrixStack matrices, BufferBuilder buffer, Box box, ColorRGBA color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;
        Draw3DUtility.renderFilledBox(matrices, buffer, box, r, g, b, a);
    }

    public static void renderBoxInternalDiagonals(MatrixStack matrices, BufferBuilder buf, Box box, ColorRGBA color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;
        float minX = (float)box.minX;
        float minY = (float)box.minY;
        float minZ = (float)box.minZ;
        float maxX = (float)box.maxX;
        float maxY = (float)box.maxY;
        float maxZ = (float)box.maxZ;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buf.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buf.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buf.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buf.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buf.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buf.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
    }

    public static void renderFilledBox(MatrixStack matrices, BufferBuilder buffer, Box box, float r, float g, float b, float a) {
        float minX = (float)box.minX;
        float minY = (float)box.minY;
        float minZ = (float)box.minZ;
        float maxX = (float)box.maxX;
        float maxY = (float)box.maxY;
        float maxZ = (float)box.maxZ;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
    }

    public static void renderOutlinedBox(MatrixStack matrices, BufferBuilder buffer, Box box, ColorRGBA color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;
        float minX = (float)box.minX;
        float minY = (float)box.minY;
        float minZ = (float)box.minZ;
        float maxX = (float)box.maxX;
        float maxY = (float)box.maxY;
        float maxZ = (float)box.maxZ;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
    }

    public static void drawLine(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d startPos, Vec3d endPos, ColorRGBA color) {
        MatrixStack.Entry entry = matrices.peek();
        Vec3d normalized = endPos.subtract(startPos).normalize();
        Vector3f startVector = new Vector3f((float)startPos.x, (float)startPos.y, (float)startPos.z);
        vertexConsumer.vertex(entry, startVector).color(color.getRGB()).normal(entry, (float)normalized.x, (float)normalized.y, (float)normalized.z);
        vertexConsumer.vertex(entry, (float)endPos.x, (float)endPos.y, (float)endPos.z).color(color.getRGB()).normal(entry, (float)normalized.x, (float)normalized.y, (float)normalized.z);
    }

    public static void drawLine(MatrixStack matrices, BufferBuilder builder, Vec3d startPos, Vec3d endPos, ColorRGBA color) {
        MatrixStack.Entry matrixEntry = matrices.peek();
        Matrix4f matrix4f = matrixEntry.getPositionMatrix();
        Vec3d normalized = endPos.subtract(startPos).normalize();
        builder.vertex(matrix4f, (float)startPos.x, (float)startPos.y, (float)startPos.z).color(color.getRGB()).normal(matrixEntry, (float)normalized.x, (float)normalized.y, (float)normalized.z);
        builder.vertex(matrix4f, (float)endPos.x, (float)endPos.y, (float)endPos.z).color(color.getRGB()).normal(matrixEntry, (float)normalized.x, (float)normalized.y, (float)normalized.z);
    }

    public static void renderLineFromPlayer(MatrixStack matrices, BufferBuilder builder, Vec3d endPos, ColorRGBA color) {
        Camera camera = Draw3DUtility.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        Vec3d cameraRotationVector = new Vec3d(0.0, 0.0, 27.0).rotateX((float)(-Math.toRadians(camera.getPitch()))).rotateY((float)(-Math.toRadians(camera.getYaw())));
        Vec3d cameraRelatedEndPos = endPos.subtract(cameraPos);
        Vec3d startPos = new Vec3d(cameraRotationVector.getX(), cameraRotationVector.getY(), cameraRotationVector.getZ());
        Draw3DUtility.drawLine(matrices, builder, startPos, cameraRelatedEndPos, color);
    }

    @Generated
    private Draw3DUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

