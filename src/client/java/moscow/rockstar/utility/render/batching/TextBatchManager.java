/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gl.ShaderProgram
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  org.joml.Matrix4f
 */
package moscow.rockstar.utility.render.batching;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.framework.msdf.MsdfFont;
import moscow.rockstar.framework.msdf.MsdfRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

public final class TextBatchManager {
    private static final Map<Integer, Batch> batches = new HashMap<Integer, Batch>();
    private static boolean globalBegun = false;

    public static void beginFrame() {
        if (globalBegun) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        globalBegun = true;
    }

    public static void addText(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z, float thickness, float spacing) {
        int key;
        Batch batch;
        if (!globalBegun) {
            TextBatchManager.beginFrame();
        }
        if ((batch = batches.get(key = font.getTextureId())) == null) {
            batch = new Batch(font);
            batches.put(key, batch);
            RenderSystem.setShaderTexture((int)0, (int)font.getTextureId());
            ShaderProgram shader = RenderSystem.setShader((ShaderProgramKey)MsdfRenderer.MSDF_FONT_SHADER_KEY);
            shader.getUniform("Range").set(font.getAtlas().range());
            shader.getUniform("Thickness").set(thickness);
            shader.getUniform("Smoothness").set(0.5f);
            shader.getUniform("EnableFadeout").set(0);
            batch.begun = true;
        }
        font.applyGlyphs(matrix, (VertexConsumer)batch.builder, text, size, thickness * 0.5f * size, spacing, x - 0.75f, y + size * 0.7f, z, color);
    }

    public static void addTextWithFade(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z, float thickness, float spacing, float fadeoutStart, float fadeoutEnd, float maxWidth, float textPosX) {
        ShaderProgram shader;
        int key;
        Batch batch;
        if (!globalBegun) {
            TextBatchManager.beginFrame();
        }
        if ((batch = batches.get(key = font.getTextureId())) == null) {
            batch = new Batch(font);
            batches.put(key, batch);
            RenderSystem.setShaderTexture((int)0, (int)font.getTextureId());
            shader = RenderSystem.setShader((ShaderProgramKey)MsdfRenderer.MSDF_FONT_SHADER_KEY);
            shader.getUniform("Range").set(font.getAtlas().range());
            shader.getUniform("Thickness").set(thickness);
            shader.getUniform("Smoothness").set(0.5f);
            batch.begun = true;
        }
        shader = RenderSystem.getShader();
        shader.getUniform("EnableFadeout").set(1);
        shader.getUniform("FadeoutStart").set(fadeoutStart);
        shader.getUniform("FadeoutEnd").set(fadeoutEnd);
        shader.getUniform("MaxWidth").set(maxWidth);
        shader.getUniform("TextPosX").set(textPosX);
        font.applyGlyphs(matrix, (VertexConsumer)batch.builder, text, size, thickness * 0.5f * size, spacing, x - 0.75f, y + size * 0.7f, z, color);
    }

    public static void endFrame() {
        if (!globalBegun) {
            return;
        }
        for (Batch batch : batches.values()) {
            BuiltBuffer built = batch.builder.endNullable();
            if (built == null) continue;
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)built);
        }
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        batches.clear();
        globalBegun = false;
    }

    private static final class Batch {
        final MsdfFont font;
        final BufferBuilder builder;
        boolean begun = false;

        Batch(MsdfFont font) {
            this.font = font;
            this.builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        }
    }
}

