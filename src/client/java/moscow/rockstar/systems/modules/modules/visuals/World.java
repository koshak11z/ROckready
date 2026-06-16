/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Quaternionf
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

@ModuleInfo(name="World", category=ModuleCategory.VISUALS, desc="\u0412\u0438\u0437\u0443\u0430\u043b\u044c\u043d\u044b\u0435 \u0434\u043e\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u044f \u043c\u0438\u0440\u0430")
public class World
extends BaseModule {
    private final List<Particle> particles = new ArrayList<Particle>();
    private final ColorSetting color = new ColorSetting(this, "color").color(Colors.ACCENT);
    private final EventListener<Render3DEvent> on3DRender = event -> {
        MatrixStack ms = event.getMatrices();
        Camera camera = World.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        ColorRGBA drawColor = Rockstar.getInstance().getThemeManager().isCustomTheme() ? Colors.getWorldColor() : this.color.getColor();
        ms.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        Identifier id = Rockstar.id("textures/bloom.png");
        RenderSystem.setShaderTexture((int)0, (Identifier)id);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (Particle particle : this.particles) {
            Vec3d pos = Utils.getInterpolatedPos(particle.prev, particle.pos, event.getTickDelta());
            float bigSize = 4.0f * particle.size;
            ms.push();
            RenderUtility.prepareMatrices(ms, pos);
            ms.multiply(camera.getRotation());
            DrawUtility.drawImage(ms, builder, (double)(-bigSize / 2.0f), (double)(-bigSize / 2.0f), 0.0, (double)bigSize, (double)bigSize, drawColor.withAlpha(255.0f * particle.alpha.getValue() * 0.4f));
            ms.pop();
        }
        BuiltBuffer builtLinesBuffer1 = builder.endNullable();
        if (builtLinesBuffer1 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtLinesBuffer1);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        ms.pop();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder linesBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (Particle particle : this.particles) {
            particle.alpha.update(!particle.timer.finished(particle.liveTicks));
            Vec3d pos = Utils.getInterpolatedPos(particle.prev, particle.pos, event.getTickDelta());
            Vec3d rot = Utils.getInterpolatedPos(particle.prevRot, particle.rotate, event.getTickDelta());
            ms.push();
            ms.translate(pos.add(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()));
            ms.multiply(new Quaternionf().rotationXYZ((float)rot.x, (float)rot.y, (float)rot.z));
            ms.scale(particle.size, particle.size, particle.size);
            Draw3DUtility.renderBoxInternalDiagonals(ms, linesBuffer, new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5), drawColor.withAlpha(255.0f * particle.alpha.getValue() * 0.4f));
            Draw3DUtility.renderOutlinedBox(ms, linesBuffer, new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5), drawColor.withAlpha(205.0f * particle.alpha.getValue()));
            ms.pop();
        }
        BuiltBuffer builtLinesBuffer = linesBuffer.endNullable();
        if (builtLinesBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtLinesBuffer);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };

    @Override
    public void tick() {
        this.particles.removeIf(particle -> particle.alpha.getValue() == 0.0f && particle.timer.finished(particle.liveTicks));
        for (Particle particle2 : this.particles) {
            particle2.tick();
        }
        if (this.particles.size() < 100) {
            this.particles.add(new Particle(World.mc.player.getPos().add((double)MathUtility.random(-20.0, 20.0), (double)MathUtility.random(0.0, 5.0), (double)MathUtility.random(-20.0, 20.0)), Vec3d.ZERO, new Vec3d((double)MathUtility.random(-1.0, 1.0), (double)MathUtility.random(0.0, 2.0), (double)MathUtility.random(-1.0, 1.0)), new Vec3d((double)MathUtility.random(-1.0, 1.0), (double)MathUtility.random(-1.0, 1.0), (double)MathUtility.random(-1.0, 1.0)), (long)MathUtility.random(1500.0, 4500.0), MathUtility.random(0.1f, 0.3f)));
        }
    }

    static class Particle {
        Vec3d prev;
        Vec3d prevRot;
        Vec3d pos;
        Vec3d rotate;
        Vec3d motion;
        Vec3d rotateMotion;
        final long liveTicks;
        float size;
        final Timer timer = new Timer();
        final Animation alpha = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

        public Particle(Vec3d pos, Vec3d rotate, Vec3d motion, Vec3d rotateMotion, long liveTicks, float size) {
            this.pos = pos;
            this.rotate = rotate;
            this.motion = motion.multiply((double)0.04f);
            this.rotateMotion = rotateMotion.multiply((double)0.04f);
            this.liveTicks = liveTicks;
            this.size = size;
            this.prevRot = rotate;
            this.prev = pos;
            this.alpha.setDuration(1000L);
        }

        void tick() {
            this.prev = this.pos;
            this.prevRot = this.rotate;
            this.pos = this.pos.add(this.motion);
            this.rotate = this.rotate.add(this.rotateMotion);
            this.motion = this.motion.multiply(0.98);
            this.rotateMotion = this.rotateMotion.multiply(0.98);
        }
    }
}

