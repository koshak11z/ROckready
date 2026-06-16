/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.VertexFormat
 *  net.minecraft.client.render.VertexFormat$DrawMode
 */
package moscow.rockstar.utility.render.batching;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat;

public abstract class Batching {
    protected static Batching active;
    protected BufferBuilder builder;

    public Batching(VertexFormat vertexFormat) {
        this.builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, vertexFormat);
        active = this;
    }

    protected void build() {
        BuiltBuffer builtBuffer = this.builder.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
        }
    }

    public abstract void draw();

    @Generated
    public BufferBuilder getBuilder() {
        return this.builder;
    }

    @Generated
    public static Batching getActive() {
        return active;
    }
}

