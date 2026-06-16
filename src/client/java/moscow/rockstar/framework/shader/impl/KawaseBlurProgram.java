/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.GlUniform
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.util.Identifier
 */
package moscow.rockstar.framework.shader.impl;

import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.interfaces.IWindow;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;

public class KawaseBlurProgram
extends GlProgram
implements IWindow {
    private GlUniform resolutionUniform;
    private GlUniform offsetUniform;
    private GlUniform saturationUniform;
    private GlUniform tintIntensityUniform;
    private GlUniform tintColorUniform;

    public KawaseBlurProgram(Identifier identifier) {
        super(identifier, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @CompileBytecode
    public void updateUniforms(float offset) {
        this.offsetUniform.set(offset);
        this.resolutionUniform.set(1.0f / (float)mw.getScaledWidth(), 1.0f / (float)mw.getScaledHeight());
        this.saturationUniform.set(1.0f);
        this.tintIntensityUniform.set(0.0f);
        this.tintColorUniform.set(1.0f, 1.0f, 1.0f);
    }

    public void updateUniforms(float offset, int textureWidth, int textureHeight) {
        this.offsetUniform.set(offset);
        float invW = textureWidth > 0 ? 1.0f / (float)textureWidth : 0.0f;
        float invH = textureHeight > 0 ? 1.0f / (float)textureHeight : 0.0f;
        this.resolutionUniform.set(invW, invH);
        this.saturationUniform.set(1.0f);
        this.tintIntensityUniform.set(0.0f);
        this.tintColorUniform.set(1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void setup() {
        this.resolutionUniform = this.findUniform("Resolution");
        this.offsetUniform = this.findUniform("Offset");
        this.saturationUniform = this.findUniform("Saturation");
        this.tintIntensityUniform = this.findUniform("TintIntensity");
        this.tintColorUniform = this.findUniform("TintColor");
        super.setup();
    }
}

