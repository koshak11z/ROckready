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
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.EntityDeathEvent;
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
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
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
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Kill Effects", category=ModuleCategory.VISUALS, desc="modules.descriptions.kill_effects")
public class KillEffects
extends BaseModule {
    private final List<Lightning> lightnings = new ArrayList<Lightning>();
    private final ColorSetting color = new ColorSetting(this, "modules.settings.kill_effects.color").color(Colors.ACCENT);
    private final EventListener<EntityDeathEvent> onEntityDeath = event -> {
        if (event.getEntity().isRemoved()) {
            return;
        }
        this.lightnings.add(new Lightning(event.getEntity().getPos(), this.color.getColor()));
    };
    private final EventListener<Render3DEvent> on3DRender = event -> {
        MatrixStack ms = event.getMatrices();
        Camera camera = KillEffects.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
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
        for (Lightning lightning2 : this.lightnings) {
            lightning2.render(builder, event.getMatrices(), camera);
            if (lightning2.animation.getValue() != 1.0f) continue;
            lightning2.showing = false;
        }
        BuiltBuffer builtBuffer = builder.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        ms.pop();
        this.lightnings.removeIf(lightning -> !lightning.showing && lightning.animation.getValue() == 0.0f);
    };

    static class Lightning {
        final Vec3d pos;
        final ColorRGBA color;
        boolean showing = true;
        final Animation animation = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
        final List<Vec3d> poses = new ArrayList<Vec3d>();

        public Lightning(Vec3d pos, ColorRGBA color) {
            this.pos = pos;
            this.color = color;
            Vec3d lastPos = pos;
            for (int i = 0; i < 200; ++i) {
                lastPos = lastPos.add((double)MathUtility.random(-0.4f, 0.4f), 0.25, (double)MathUtility.random(-0.4f, 0.4f));
                this.poses.add(lastPos);
            }
        }

        void render(BufferBuilder builder, MatrixStack ms, Camera camera) {
            this.animation.setEasing(Easing.BOUNCE_IN);
            this.animation.setDuration(500L);
            this.animation.update(this.showing);
            for (Vec3d pos : this.poses) {
                float size = (float)(2.0 + 5.0 * (pos.y - this.pos.y) / 50.0);
                ms.push();
                RenderUtility.prepareMatrices(ms, pos);
                ms.multiply(camera.getRotation());
                DrawUtility.drawImage(ms, builder, (double)(-size / 2.0f), (double)(-size / 2.0f), 0.0, (double)size, (double)size, this.color.withAlpha(255.0f * this.animation.getValue() * 0.4f));
                ms.pop();
            }
        }
    }
}

