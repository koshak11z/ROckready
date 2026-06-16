/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  com.google.common.base.Suppliers
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gl.Framebuffer
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 */
package moscow.rockstar.framework.shader.impl;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.shader.impl.KawaseBlurProgram;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IWindow;
import moscow.rockstar.utility.render.CustomRenderTarget;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class BlurProgram
implements IMinecraft,
IWindow {
    private static final Framebuffer MAIN_FBO = mc.getFramebuffer();
    public static final Supplier<CustomRenderTarget> CACHE = Suppliers.memoize(() -> new CustomRenderTarget(false).setLinear());
    public static final Supplier<CustomRenderTarget> BUFFER = Suppliers.memoize(() -> new CustomRenderTarget(false).setLinear());
    private final Timer timer = new Timer();
    private static KawaseBlurProgram kawaseDownProgram;
    private static KawaseBlurProgram kawaseUpProgram;
    private float blurOffset = 1.0f;
    private float blurDownscale = 0.5f;

    @Compile
    public void initShaders() {
        kawaseDownProgram = new KawaseBlurProgram(Rockstar.id("kawase_down/data"));
        kawaseUpProgram = new KawaseBlurProgram(Rockstar.id("kawase_up/data"));
    }

    public void draw() {
        int step;
        int i;
        if (!this.timer.finished(25L)) {
            return;
        }
        this.blurOffset = Math.max(0.0f, Interface.blur());
        CustomRenderTarget cache = (CustomRenderTarget)CACHE.get();
        CustomRenderTarget buffer = (CustomRenderTarget)BUFFER.get();
        cache.setDownscale(this.blurDownscale).setLinear();
        buffer.setDownscale(this.blurDownscale).setLinear();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        kawaseDownProgram.use();
        kawaseDownProgram.updateUniforms(this.blurOffset, BlurProgram.MAIN_FBO.textureWidth, BlurProgram.MAIN_FBO.textureHeight);
        cache.setup();
        MAIN_FBO.beginRead();
        RenderSystem.setShaderTexture((int)0, (int)MAIN_FBO.getColorAttachment());
        this.drawQuad(0.0f, 0.0f, mw.getScaledWidth(), mw.getScaledHeight());
        cache.stop();
        CustomRenderTarget[] buffers = new CustomRenderTarget[]{cache, buffer};
        int steps = 3;
        for (i = 1; i < 3; ++i) {
            step = i % 2;
            buffers[step].setup();
            buffers[(step + 1) % 2].beginRead();
            RenderSystem.setShaderTexture((int)0, (int)buffers[(step + 1) % 2].getColorAttachment());
            kawaseDownProgram.updateUniforms(this.blurOffset, buffers[(step + 1) % 2].textureWidth, buffers[(step + 1) % 2].textureHeight);
            this.drawQuad(0.0f, 0.0f, mw.getScaledWidth(), mw.getScaledHeight());
            buffers[(step + 1) % 2].endRead();
            buffers[step].stop();
        }
        kawaseUpProgram.use();
        for (i = 0; i < 3; ++i) {
            step = i % 2;
            buffers[(step + 1) % 2].setup();
            buffers[step].beginRead();
            RenderSystem.setShaderTexture((int)0, (int)buffers[step].getColorAttachment());
            kawaseUpProgram.updateUniforms(this.blurOffset, buffers[step].textureWidth, buffers[step].textureHeight);
            this.drawQuad(0.0f, 0.0f, mw.getScaledWidth(), mw.getScaledHeight());
            buffers[step].endRead();
            buffers[step].stop();
        }
        MAIN_FBO.endRead();
        MAIN_FBO.beginWrite(false);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
    }

    private void drawQuad(float x, float y, float width, float height) {
        int color = -1;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(x, y, 0.0f).texture(0.0f, 1.0f).color(color);
        builder.vertex(x, y + height, 0.0f).texture(0.0f, 0.0f).color(color);
        builder.vertex(x + width, y + height, 0.0f).texture(1.0f, 0.0f).color(color);
        builder.vertex(x + width, y, 0.0f).texture(1.0f, 1.0f).color(color);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
    }

    public static int getTexture() {
        return ((CustomRenderTarget)BUFFER.get()).getColorAttachment();
    }

    @Generated
    public void setBlurOffset(float blurOffset) {
        this.blurOffset = blurOffset;
    }

    @Generated
    public void setBlurDownscale(float blurDownscale) {
        this.blurDownscale = blurDownscale;
    }
}

