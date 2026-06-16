/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.render.entity.EntityRenderer
 *  net.minecraft.client.render.entity.LivingEntityRenderer
 *  net.minecraft.client.render.entity.model.EntityModel
 *  net.minecraft.client.render.entity.state.LivingEntityRenderState
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.Vec2f
 *  org.joml.Matrix4f
 */
package moscow.rockstar.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.gradient.Gradient;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.framework.shader.impl.BlurProgram;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IWindow;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.ColorUtility;
import moscow.rockstar.utility.render.CustomRenderTarget;
import moscow.rockstar.utility.render.HookLimiter;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import moscow.rockstar.utility.render.batching.impl.RoundedRectBatching;
import moscow.rockstar.utility.render.batching.impl.SquircleBatching;
import moscow.rockstar.utility.render.obj.CustomSprite;
import moscow.rockstar.utility.render.penis.PenisSprite;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import org.joml.Matrix4f;
import ru.kotopushka.compiler.sdk.annotations.Initialization;

public final class DrawUtility
implements IMinecraft,
IWindow {
    public static final float DEFAULT_SMOOTHNESS = 0.5f;
    public static final HookLimiter limiter = new HookLimiter(true);
    public static GlProgram rectangleProgram;
    private static GlProgram squircleProgram;
    private static GlProgram roundedTextureProgram;
    private static GlProgram squircleTextureProgram;
    private static GlProgram borderProgram;
    private static GlProgram loadingProgram;
    private static GlProgram glassProgram;
    private static GlProgram gradientRectangleProgram;
    public static BlurProgram blurProgram;
    private static final CustomRenderTarget buffer;

    @Initialization
    public static void initializeShaders() {
        rectangleProgram = new GlProgram(Rockstar.id("rectangle/data"), VertexFormats.POSITION_COLOR);
        squircleProgram = new GlProgram(Rockstar.id("squircle/data"), VertexFormats.POSITION_COLOR);
        squircleTextureProgram = new GlProgram(Rockstar.id("squircle_texture/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        roundedTextureProgram = new GlProgram(Rockstar.id("texture/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        borderProgram = new GlProgram(Rockstar.id("border/data"), VertexFormats.POSITION_COLOR);
        loadingProgram = new GlProgram(Rockstar.id("loading/data"), VertexFormats.POSITION_COLOR);
        glassProgram = new GlProgram(Rockstar.id("liquidglass/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        gradientRectangleProgram = new GlProgram(Rockstar.id("gradient_rectangle/data"), VertexFormats.POSITION_COLOR);
        blurProgram = new BlurProgram();
        blurProgram.initShaders();
    }

    public static void updateBuffer() {
        buffer.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        buffer.setup();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        mc.getFramebuffer().beginRead();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)mc.getFramebuffer().getColorAttachment());
        DrawUtility.drawQuad(0.0f, 0.0f, mw.getScaledWidth(), mw.getScaledHeight(), true);
        mc.getFramebuffer().endRead();
        RenderSystem.disableBlend();
        mc.getFramebuffer().beginWrite(true);
        buffer.stop();
    }

    private static void drawQuad(float x, float y, float width, float height, boolean flip) {
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        int color = -1;
        float vTop = flip ? 0.0f : 1.0f;
        float vBottom = flip ? 1.0f : 0.0f;
        builder.vertex(x, y, 0.0f).texture(0.0f, vBottom).color(-1);
        builder.vertex(x, y + height, 0.0f).texture(0.0f, vTop).color(-1);
        builder.vertex(x + width, y + height, 0.0f).texture(1.0f, vTop).color(-1);
        builder.vertex(x + width, y, 0.0f).texture(1.0f, vBottom).color(-1);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawLine(MatrixStack matrices, Vec2f from, Vec2f to, ColorRGBA color) {
        matrices.push();
        try {
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.lineWidth((float)1.0f);
            DrawUtility.drawSetup();
            BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            builder.vertex(matrix4f, from.x, from.y, 0.0f).color(color.getRGB());
            builder.vertex(matrix4f, to.x, to.y, 0.0f).color(color.getRGB());
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
            DrawUtility.drawEnd();
        }
        finally {
            RenderSystem.disableBlend();
            RenderSystem.lineWidth((float)1.0f);
            matrices.pop();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawBezier(MatrixStack matrices, Vec2f p0, Vec2f p1, Vec2f p2, Vec2f p3, ColorRGBA color, int resolution) {
        matrices.push();
        try {
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.lineWidth((float)1.0f);
            DrawUtility.drawSetup();
            BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= resolution; ++i) {
                float t = (float)i / (float)resolution;
                float x = (float)MathUtility.cubicBezier(t, p0.x, p1.x, p2.x, p3.x);
                float y = (float)MathUtility.cubicBezier(t, p0.y, p1.y, p2.y, p3.y);
                builder.vertex(matrix4f, x, y, 0.0f).color(color.getRGB());
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
            DrawUtility.drawEnd();
        }
        finally {
            RenderSystem.disableBlend();
            RenderSystem.lineWidth((float)1.0f);
            matrices.pop();
        }
    }

    private static float cubicBezier(float t, float p0, float p1, float p2, float p3) {
        float u = 1.0f - t;
        float tt = t * t;
        float uu = u * u;
        return uu * u * p0 + 3.0f * uu * t * p1 + 3.0f * u * tt * p2 + tt * t * p3;
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, ColorRGBA color) {
        BufferBuilder builder;
        Batching batching = Batching.getActive();
        if (batching instanceof RectBatching) {
            RectBatching batching2 = (RectBatching)batching;
            builder = batching2.getBuilder();
            Matrix4f matrix4f = batching2.getMatrices().peek().getPositionMatrix();
            builder.vertex(matrix4f, x, y + height, 0.0f).color(color.getRGB());
            builder.vertex(matrix4f, x + width, y + height, 0.0f).color(color.getRGB());
            builder.vertex(matrix4f, x + width, y, 0.0f).color(color.getRGB());
            builder.vertex(matrix4f, x, y, 0.0f).color(color.getRGB());
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        DrawUtility.drawSetup();
        builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, x, y + height, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, x, y, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        matrices.pop();
    }

    public static void drawSquircle(MatrixStack matrices, float x, float y, float width, float height, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f m = matrices.peek().getPositionMatrix();
        float smoothness = 0.5f;
        Batching batching = Batching.getActive();
        if (batching instanceof SquircleBatching) {
            SquircleBatching sb = (SquircleBatching)batching;
            sb.add(m, x, y, width, height, borderRadius.topLeftRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f, borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomRightRadius() * squirt / 2.0f, color.getRGB());
            matrices.pop();
            return;
        }
        squircleProgram.use();
        squircleProgram.findUniform("Size").set(width, height);
        squircleProgram.findUniform("Radius").set(borderRadius.topLeftRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f, borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomRightRadius() * squirt / 2.0f);
        squircleProgram.findUniform("Smoothness").set(smoothness);
        squircleProgram.findUniform("CornerSmoothness").set(squirt);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float ax = x - horizontalPadding / 2.0f;
        float ay = y - verticalPadding / 2.0f;
        float aw = width + horizontalPadding;
        float ah = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(m, ax, ay, 0.0f).color(color.getRGB());
        builder.vertex(m, ax, ay + ah, 0.0f).color(color.getRGB());
        builder.vertex(m, ax + aw, ay + ah, 0.0f).color(color.getRGB());
        builder.vertex(m, ax + aw, ay, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        matrices.pop();
    }

    public static void drawLoadingRect(MatrixStack matrices, float x, float y, float width, float height, float progress, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.5f;
        loadingProgram.use();
        loadingProgram.findUniform("Size").set(width, height);
        loadingProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        loadingProgram.findUniform("Smoothness").set(smoothness);
        loadingProgram.findUniform("Progress").set(progress);
        loadingProgram.findUniform("StripeWidth").set(0.0f);
        loadingProgram.findUniform("Fade").set(0.5f);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        matrices.pop();
    }

    public static void drawLiquidGlass(MatrixStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color, float globalAlpha, float fresnelPower, ColorRGBA fresnelColor, float baseAlpha, boolean fresnelInvert, float fresnelMix, float distortStrength, float squirt, boolean clean) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        DrawUtility.drawSetup();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture((int)0, (int)(clean ? mc.getFramebuffer().getColorAttachment() : BlurProgram.getTexture()));
        glassProgram.use();
        glassProgram.findUniform("GlobalAlpha").set(globalAlpha);
        glassProgram.findUniform("Size").set(width, height);
        glassProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        glassProgram.findUniform("Smoothness").set(0.5f);
        glassProgram.findUniform("FresnelPower").set(fresnelPower);
        glassProgram.findUniform("FresnelColor").set(ColorUtility.getRGBf(fresnelColor.getRGB()));
        glassProgram.findUniform("FresnelAlpha").set(ColorUtility.alphaf(fresnelColor.getRGB()));
        glassProgram.findUniform("BaseAlpha").set(baseAlpha);
        glassProgram.findUniform("FresnelInvert").set(fresnelInvert ? 1 : 0);
        glassProgram.findUniform("FresnelMix").set(fresnelMix);
        glassProgram.findUniform("DistortStrength").set(distortStrength);
        glassProgram.findUniform("CornerSmoothness").set(squirt);
        int screenWidth = mw.getScaledWidth();
        int screenHeight = mw.getScaledHeight();
        float u = x / (float)screenWidth;
        float v = ((float)screenHeight - y - height) / (float)screenHeight;
        float texWidth = width / (float)screenWidth;
        float texHeight = height / (float)screenHeight;
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix, x, y, 0.0f).texture(u, v + texHeight).color(color.getRGB());
        builder.vertex(matrix, x, y + height, 0.0f).texture(u, v).color(color.getRGB());
        builder.vertex(matrix, x + width, y + height, 0.0f).texture(u + texWidth, v).color(color.getRGB());
        builder.vertex(matrix, x + width, y, 0.0f).texture(u + texWidth, v + texHeight).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.enableCull();
        DrawUtility.drawEnd();
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f m = matrices.peek().getPositionMatrix();
        float smoothness = 0.5f;
        Batching batching = Batching.getActive();
        if (batching instanceof RoundedRectBatching) {
            RoundedRectBatching rb = (RoundedRectBatching)batching;
            rb.add(m, x, y, width, height, borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius(), color.getRGB());
            matrices.pop();
            return;
        }
        rectangleProgram.use();
        rectangleProgram.findUniform("Size").set(width, height);
        rectangleProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        rectangleProgram.findUniform("Smoothness").set(smoothness);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float ax = x - horizontalPadding / 2.0f;
        float ay = y - verticalPadding / 2.0f;
        float aw = width + horizontalPadding;
        float ah = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(m, ax, ay, 0.0f).color(color.getRGB());
        builder.vertex(m, ax, ay + ah, 0.0f).color(color.getRGB());
        builder.vertex(m, ax + aw, ay + ah, 0.0f).color(color.getRGB());
        builder.vertex(m, ax + aw, ay, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        matrices.pop();
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color1, ColorRGBA color2, ColorRGBA color3, ColorRGBA color4) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.5f;
        gradientRectangleProgram.use();
        gradientRectangleProgram.findUniform("Size").set(width, height);
        gradientRectangleProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        gradientRectangleProgram.findUniform("Smoothness").set(smoothness);
        gradientRectangleProgram.findUniform("TopLeftColor").set(color1.getRed() / 255.0f, color1.getGreen() / 255.0f, color1.getBlue() / 255.0f, color1.getAlpha() / 255.0f);
        gradientRectangleProgram.findUniform("BottomLeftColor").set(color2.getRed() / 255.0f, color2.getGreen() / 255.0f, color2.getBlue() / 255.0f, color2.getAlpha() / 255.0f);
        gradientRectangleProgram.findUniform("BottomRightColor").set(color3.getRed() / 255.0f, color3.getGreen() / 255.0f, color3.getBlue() / 255.0f, color3.getAlpha() / 255.0f);
        gradientRectangleProgram.findUniform("TopRightColor").set(color4.getRed() / 255.0f, color4.getGreen() / 255.0f, color4.getBlue() / 255.0f, color4.getAlpha() / 255.0f);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(color1.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(color2.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(color3.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(color4.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        matrices.pop();
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, Gradient gradient) {
        DrawUtility.drawRoundedRect(matrices, x, y, width, height, borderRadius, gradient.getTopLeftColor(), gradient.getBottomLeftColor(), gradient.getBottomRightColor(), gradient.getTopRightColor());
    }

    public static void drawRoundedBorder(MatrixStack matrices, float x, float y, float width, float height, float borderThickness, BorderRadius borderRadius, ColorRGBA borderColor) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float internalSmoothness = 0.5f;
        float externalSmoothness = 1.0f;
        borderProgram.use();
        borderProgram.findUniform("Size").set(width, height);
        borderProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        borderProgram.findUniform("Smoothness").set(internalSmoothness, externalSmoothness);
        borderProgram.findUniform("Thickness").set(borderThickness);
        DrawUtility.drawSetup();
        float horizontalPadding = -externalSmoothness / 2.0f + externalSmoothness * 2.0f;
        float verticalPadding = externalSmoothness / 2.0f + externalSmoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(borderColor.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        matrices.pop();
    }

    public static void drawTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, ColorRGBA textureColor) {
        BufferBuilder builder;
        Batching batching = Batching.getActive();
        if (batching instanceof IconBatching) {
            IconBatching batching2 = (IconBatching)batching;
            builder = batching2.getBuilder();
            Matrix4f matrix4f = batching2.getMatrices().peek().getPositionMatrix();
            RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
            builder.vertex(matrix4f, x, y, 0.0f).texture(0.0f, 0.0f).color(textureColor.getRGB());
            builder.vertex(matrix4f, x, y + height, 0.0f).texture(0.0f, 1.0f).color(textureColor.getRGB());
            builder.vertex(matrix4f, x + width, y + height, 0.0f).texture(1.0f, 1.0f).color(textureColor.getRGB());
            builder.vertex(matrix4f, x + width, y, 0.0f).texture(1.0f, 0.0f).color(textureColor.getRGB());
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        DrawUtility.drawSetup();
        builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0f).texture(0.0f, 0.0f).color(textureColor.getRGB());
        builder.vertex(matrix4f, x, y + height, 0.0f).texture(0.0f, 1.0f).color(textureColor.getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0.0f).texture(1.0f, 1.0f).color(textureColor.getRGB());
        builder.vertex(matrix4f, x + width, y, 0.0f).texture(1.0f, 0.0f).color(textureColor.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, float u1, float u2, float v1, float v2, ColorRGBA clor) {
        Batching batching = Batching.getActive();
        if (batching instanceof IconBatching) {
            IconBatching batching2 = (IconBatching)batching;
            BufferBuilder builder = batching2.getBuilder();
            Matrix4f matrix4f = batching2.getMatrices().peek().getPositionMatrix();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
            int color = clor.getRGB();
            float x2 = x + width;
            float y2 = y + height;
            builder.vertex(matrix4f, x, y, 0.0f).texture(u1, v1).color(color);
            builder.vertex(matrix4f, x, y2, 0.0f).texture(u1, v2).color(color);
            builder.vertex(matrix4f, x2, y2, 0.0f).texture(u2, v2).color(color);
            builder.vertex(matrix4f, x2, y, 0.0f).texture(u2, v1).color(color);
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.push();
        int color = clor.getRGB();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float x2 = x + width;
        float y2 = y + height;
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0f).texture(u1, v1).color(color);
        builder.vertex(matrix4f, x, y2, 0.0f).texture(u1, v2).color(color);
        builder.vertex(matrix4f, x2, y2, 0.0f).texture(u2, v2).color(color);
        builder.vertex(matrix4f, x2, y, 0.0f).texture(u2, v1).color(color);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
        RenderSystem.disableBlend();
    }

    public static void drawAnimationSprite(MatrixStack matrices, PenisSprite sprite, float x, float y, float width, float height, ColorRGBA color) {
        if (sprite == null) {
            return;
        }
        DrawUtility.drawTexture(matrices, sprite.texture(), x, y, width, height, sprite.u1(), sprite.u2(), sprite.v1(), sprite.v2(), color);
    }

    public static void drawSprite(MatrixStack matrices, CustomSprite sprite, float x, float y, float width, float height, ColorRGBA color) {
        DrawUtility.drawTexture(matrices, Rockstar.id(sprite.getTexture().getTexture()), x, y, width, height, sprite.x / sprite.getTexture().getWidth(), (sprite.x + sprite.getTexture().getStep()) / sprite.getTexture().getWidth(), 0.0f, 1.0f, color);
    }

    public static void drawRoundedTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius) {
        DrawUtility.drawRoundedTexture(matrices, identifier, x, y, width, height, borderRadius, Colors.WHITE);
    }

    public static void drawRoundedTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.5f;
        roundedTextureProgram.use();
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        roundedTextureProgram.findUniform("Smoothness").set(smoothness);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).texture(0.0f, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).texture(0.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).texture(1.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).texture(1.0f, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawShadow(MatrixStack matrices, float x, float y, float width, float height, float softness, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        Batching batching = Batching.getActive();
        if (batching instanceof IconBatching) {
            IconBatching batching2 = (IconBatching)batching;
            BufferBuilder builder = batching2.getBuilder();
            float horizontalPadding = -softness / 2.0f + softness * 2.0f;
            float verticalPadding = softness / 2.0f + softness;
            float adjustedX = x - horizontalPadding / 2.0f;
            float adjustedY = y - verticalPadding / 2.0f;
            float adjustedWidth = width + horizontalPadding;
            float adjustedHeight = height + verticalPadding;
            builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(color.getRGB());
            builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
            builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
            builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(color.getRGB());
            return;
        }
        rectangleProgram.use();
        rectangleProgram.findUniform("Size").set(width, height);
        rectangleProgram.findUniform("Radius").set(borderRadius.topLeftRadius() * 3.0f, borderRadius.bottomLeftRadius() * 3.0f, borderRadius.topRightRadius() * 3.0f, borderRadius.bottomRightRadius() * 3.0f);
        rectangleProgram.findUniform("Smoothness").set(softness);
        DrawUtility.drawSetup();
        float horizontalPadding = -softness / 2.0f + softness * 2.0f;
        float verticalPadding = softness / 2.0f + softness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        matrices.pop();
    }

    public static void drawBlur(MatrixStack matrices, float x, float y, float width, float height, float blurRadius, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.03f;
        blurRadius /= 22.5f;
        if (blurRadius <= 0.0f) {
            return;
        }
        squircleTextureProgram.use();
        RenderSystem.setShaderTexture((int)0, (int)BlurProgram.getTexture());
        squircleTextureProgram.findUniform("Size").set(width, height);
        squircleTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f, borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomRightRadius() * squirt / 2.0f);
        squircleTextureProgram.findUniform("Smoothness").set(0.1f);
        squircleTextureProgram.findUniform("CornerSmoothness").set(squirt);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        float u = adjustedX / (float)screenWidth;
        float v = ((float)screenHeight - adjustedY - adjustedHeight) / (float)screenHeight;
        float texWidth = adjustedWidth / (float)screenWidth;
        float texHeight = adjustedHeight / (float)screenHeight;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).texture(u, v + texHeight).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).texture(u, v).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).texture(u + texWidth, v).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).texture(u + texWidth, v + texHeight).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawBlur(MatrixStack matrices, float x, float y, float width, float height, float blurRadius, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        blurRadius /= 22.5f;
        if (blurRadius <= 0.0f) {
            return;
        }
        roundedTextureProgram.use();
        RenderSystem.setShaderTexture((int)0, (int)BlurProgram.getTexture());
        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        roundedTextureProgram.findUniform("Smoothness").set(0.01f);
        DrawUtility.drawSetup();
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        float u = x / (float)screenWidth;
        float v = ((float)screenHeight - y - height) / (float)screenHeight;
        float texWidth = width / (float)screenWidth;
        float texHeight = height / (float)screenHeight;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0f).texture(u, v + texHeight).color(color.getRGB());
        builder.vertex(matrix4f, x, y + height, 0.0f).texture(u, v).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0.0f).texture(u + texWidth, v).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y, 0.0f).texture(u + texWidth, v + texHeight).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawImage(MatrixStack matrices, BufferBuilder builder, double x, double y, double z, double width, double height, ColorRGBA color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        builder.vertex(matrix, (float)x, (float)(y + height), (float)z).texture(0.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix, (float)(x + width), (float)(y + height), (float)z).texture(1.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix, (float)(x + width), (float)y, (float)z).texture(1.0f, 0.0f).color(color.getRGB());
        builder.vertex(matrix, (float)x, (float)y, (float)z).texture(0.0f, 0.0f).color(color.getRGB());
    }

    public static void drawImage(MatrixStack matrices, Identifier identifier, double x, double y, double z, double width, double height, ColorRGBA color) {
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        builder.vertex(matrix, (float)x, (float)(y + height), (float)z).texture(0.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix, (float)(x + width), (float)(y + height), (float)z).texture(1.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix, (float)(x + width), (float)y, (float)z).texture(1.0f, 0.0f).color(color.getRGB());
        builder.vertex(matrix, (float)x, (float)y, (float)z).texture(0.0f, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
    }

    public static void drawPlayerHeadWithHat(MatrixStack matrices, AbstractClientPlayerEntity player, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        Identifier skinTexture = player.getSkinTextures().texture();
        DrawUtility.drawPlayerHeadWithRoundedShader(matrices, skinTexture, x, y, size, borderRadius, color);
        DrawUtility.drawPlayerHatLayerWithRoundedShader(matrices, skinTexture, x, y, size, borderRadius, color);
    }

    public static <T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> void drawEntityHeadWithHat(MatrixStack matrices, T entity, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer(entity);
        if (renderer instanceof LivingEntityRenderer) {
            LivingEntityRenderer renderer1 = (LivingEntityRenderer)renderer;
            LivingEntityRenderer livingRenderer = (LivingEntityRenderer)renderer;
            LivingEntityRenderState state = (LivingEntityRenderState)livingRenderer.createRenderState();
            Identifier skinTexture = livingRenderer.getTexture(state);
            DrawUtility.drawPlayerHeadWithRoundedShader(matrices, skinTexture, x, y, size, borderRadius, color);
            DrawUtility.drawPlayerHatLayerWithRoundedShader(matrices, skinTexture, x, y, size, borderRadius, color);
        }
    }

    public static void drawPlayerHeadWithRoundedShader(MatrixStack matrices, Identifier skinTexture, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawRoundedTextureWithUV(matrices, skinTexture, x, y, size, size, borderRadius, color, 0.125f, 0.125f, 0.25f, 0.25f);
    }

    private static void drawPlayerHatLayerWithRoundedShader(MatrixStack matrices, Identifier skinTexture, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DrawUtility.drawRoundedTextureWithUV(matrices, skinTexture, x, y, size, size, borderRadius, color, 0.625f, 0.125f, 0.75f, 0.25f);
        RenderSystem.disableBlend();
    }

    public static void drawRoundedTextureWithUV(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color, float u1, float v1, float u2, float v2) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.5f;
        roundedTextureProgram.use();
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        roundedTextureProgram.findUniform("Smoothness").set(smoothness);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).texture(u1, v1).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).texture(u1, v2).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).texture(u2, v2).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).texture(u2, v1).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtility.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawSetup() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void drawEnd() {
        RenderSystem.disableBlend();
    }

    @Generated
    private DrawUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static GlProgram getSquircleProgram() {
        return squircleProgram;
    }

    static {
        buffer = new CustomRenderTarget(false);
    }

    record HeadUV(float u1, float v1, float uSize, float vSize) {
    }
}

