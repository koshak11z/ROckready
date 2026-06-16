/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gl.Defines
 *  net.minecraft.client.gl.GlUniform
 *  net.minecraft.client.gl.ShaderLoader$LoadException
 *  net.minecraft.client.gl.ShaderProgram
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.render.RenderPhase$ShaderProgram
 *  net.minecraft.client.render.VertexFormat
 *  net.minecraft.util.Identifier
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package moscow.rockstar.framework.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.mixin.accessors.ShaderProgramAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

public class GlProgram {
    private static final List<Runnable> REGISTERED_PROGRAMS = new ArrayList<Runnable>();
    protected ShaderProgram backingProgram;
    protected ShaderProgramKey programKey;

    public GlProgram(Identifier id, VertexFormat vertexFormat) {
        this.programKey = new ShaderProgramKey(id.withPrefixedPath("core/"), vertexFormat, Defines.EMPTY);
        REGISTERED_PROGRAMS.add(() -> {
            try {
                this.backingProgram = MinecraftClient.getInstance().getShaderLoader().getProgramToLoad(this.programKey);
                this.setup();
            }
            catch (ShaderLoader.LoadException e) {
                throw new RuntimeException("Failed to initialize shader program", e);
            }
        });
    }

    public RenderPhase.ShaderProgram renderPhaseProgram() {
        return new RenderPhase.ShaderProgram(this.programKey);
    }

    public ShaderProgram use() {
        return RenderSystem.setShader((ShaderProgramKey)this.programKey);
    }

    protected void setup() {
    }

    public GlUniform findUniform(String name) {
        return ((ShaderProgramAccessor)this.backingProgram).getUniformsByName().get(name);
    }

    @ApiStatus.Internal
    public static void loadAndSetupPrograms() {
        REGISTERED_PROGRAMS.forEach(Runnable::run);
    }
}

