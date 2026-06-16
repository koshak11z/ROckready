/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.render.VertexFormat
 *  net.minecraft.client.util.math.MatrixStack
 */
package moscow.rockstar.utility.render.batching.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.batching.Batching;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;

public class RectBatching
extends Batching {
    private final MatrixStack matrices;

    public RectBatching(VertexFormat vertexFormat, MatrixStack matrices) {
        super(vertexFormat);
        this.matrices = matrices;
    }

    @Override
    public void draw() {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        DrawUtility.drawSetup();
        this.build();
        DrawUtility.drawEnd();
        if (active == this) {
            active = null;
        }
    }

    @Generated
    public MatrixStack getMatrices() {
        return this.matrices;
    }
}

