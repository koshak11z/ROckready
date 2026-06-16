/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gl.ShaderProgram
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.render.VertexFormat
 */
package moscow.rockstar.utility.render.batching.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.framework.msdf.MsdfFont;
import moscow.rockstar.framework.msdf.MsdfRenderer;
import moscow.rockstar.utility.render.batching.Batching;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.VertexFormat;

public class FontBatching
extends Batching {
    protected MsdfFont font;

    public FontBatching(VertexFormat vertexFormat, MsdfFont font) {
        super(vertexFormat);
        this.font = font;
    }

    @Override
    public void draw() {
        float thickness = 0.05f;
        float smoothness = 0.5f;
        float spacing = 0.0f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture((int)0, (int)this.font.getTextureId());
        ShaderProgram shader = RenderSystem.setShader((ShaderProgramKey)MsdfRenderer.MSDF_FONT_SHADER_KEY);
        shader.getUniform("Range").set(this.font.getAtlas().range());
        shader.getUniform("Thickness").set(thickness);
        shader.getUniform("Smoothness").set(smoothness);
        shader.getUniform("EnableFadeout").set(0);
        this.build();
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        if (active == this) {
            active = null;
        }
    }
}

