/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.CrystalRenderer;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@ModuleInfo(name="Target ESP", category=ModuleCategory.VISUALS, desc="\u041f\u043e\u043c\u0435\u0447\u0430\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u043d\u0443\u044e \u0446\u0435\u043b\u044c")
public class TargetESP
extends BaseModule {
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.target_esp.mode");
    private final ModeSetting.Value souls = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.souls");
    private final ModeSetting.Value crystals = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.crystals").select();
    private final ColorSetting colorTarget = new ColorSetting(this, "color").color(Colors.ACCENT);
    private final Animation animation = new Animation(300L, 0.0f, Easing.BOTH_CUBIC);
    private final Animation moving = new Animation(70L, 0.0f, Easing.LINEAR);
    private LivingEntity prevTarget;
    private final EventListener<Render3DEvent> onRender3D = event -> {
        LivingEntity target2;
        if (!EntityUtility.isInGame()) {
            return;
        }
        Entity target1 = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        LivingEntity target = target1 instanceof LivingEntity ? (target2 = (LivingEntity)target1) : null;
        this.animation.setEasing(Easing.FIGMA_EASE_IN_OUT);
        this.animation.update(target != null);
        this.moving.update(this.moving.getValue() + 10.0f + 50.0f);
        if (target != null) {
            this.prevTarget = target;
        }
        if (this.prevTarget == null || this.animation.getValue() == 0.0f) {
            return;
        }
        MatrixStack ms = event.getMatrices();
        ms.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        if (TargetESP.mc.world.raycast(new RaycastContext(TargetESP.mc.gameRenderer.getCamera().getPos(), this.prevTarget.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)TargetESP.mc.player)).getType() != HitResult.Type.MISS) {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        if (this.crystals.isSelected()) {
            this.drawCrystals(ms, this.prevTarget);
        } else {
            this.drawGhosts(ms, this.prevTarget);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        ms.pop();
    };

    private void drawCrystals(MatrixStack ms, LivingEntity target) {
        Camera camera = TargetESP.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        ColorRGBA color = Rockstar.getInstance().getThemeManager().isCustomTheme() ? Colors.getTargetESPColor() : this.colorTarget.getColor();
        float width = this.prevTarget.getWidth() * 1.5f;
        RenderUtility.prepareMatrices(ms, this.getRenderPos(this.prevTarget));
        BufferBuilder builder = CrystalRenderer.createBuffer();
        for (int i = 0; i < 360; i += 20) {
            float val = 1.2f - 0.5f * this.animation.getValue();
            float sin = (float)(MathUtility.sin((float)Math.toRadians((float)i + this.moving.getValue() * 0.3f)) * (double)width * (double)val);
            float cos = (float)(MathUtility.cos((float)Math.toRadians((float)i + this.moving.getValue() * 0.3f)) * (double)width * (double)val);
            float size = 0.1f;
            ms.push();
            ms.translate((double)sin, (double)0.1f + (double)target.getHeight() * Math.abs(MathUtility.sin(i)), (double)cos);
            Vec3d crystalPos = this.getRenderPos(this.prevTarget).add((double)sin, 1.0, (double)cos);
            Vec3d targetPos = target.getPos().add(0.0, (double)target.getHeight() / 2.0, 0.0);
            Vector3f directionToTarget = new Vector3f((float)(targetPos.x - crystalPos.x), (float)(targetPos.y - crystalPos.y), (float)(targetPos.z - crystalPos.z)).normalize();
            Vector3f initialDirection = new Vector3f(0.0f, 1.0f, 0.0f);
            Quaternionf rotation = new Quaternionf().rotationTo((Vector3fc)initialDirection, (Vector3fc)directionToTarget);
            ms.multiply(rotation);
            CrystalRenderer.render(ms, builder, 0.0f, 0.0f, 0.0f, size, color.withAlpha(255.0f * this.animation.getValue()));
            ms.pop();
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        Identifier id = Rockstar.id("textures/bloom.png");
        RenderSystem.setShaderTexture((int)0, (Identifier)id);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        float bigSize = 1.0f;
        for (int i = 0; i < 360; i += 20) {
            float val = 1.2f - 0.5f * this.animation.getValue();
            float sin = (float)(MathUtility.sin((float)Math.toRadians((float)i + this.moving.getValue() * 0.3f)) * (double)width * (double)val);
            float cos = (float)(MathUtility.cos((float)Math.toRadians((float)i + this.moving.getValue() * 0.3f)) * (double)width * (double)val);
            float size = 0.1f;
            ms.push();
            ms.translate((double)sin, (double)0.1f + (double)target.getHeight() * Math.abs(MathUtility.sin(i)), (double)cos);
            ms.multiply(camera.getRotation());
            DrawUtility.drawImage(ms, buffer, (double)(-bigSize / 2.0f), (double)(-bigSize / 2.0f), 0.0, (double)bigSize, (double)bigSize, color.withAlpha(255.0f * this.animation.getValue() * 0.2f));
            ms.pop();
        }
        RenderUtility.buildBuffer(buffer);
    }

    private void drawGhosts(MatrixStack ms, LivingEntity target) {
        Camera camera = TargetESP.mc.gameRenderer.getCamera();
        ColorRGBA color = Rockstar.getInstance().getThemeManager().isCustomTheme() ? Colors.getTargetESPColor() : this.colorTarget.getColor();
        Identifier id = Rockstar.id("textures/bloom.png");
        float width = this.prevTarget.getWidth() * 1.5f;
        RenderSystem.setShaderTexture((int)0, (Identifier)id);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        RenderUtility.prepareMatrices(ms, this.getRenderPos(this.prevTarget));
        int step = 2;
        int wormTick = 0;
        int wormCD = 0;
        int wormCount = 0;
        for (int i = 0; i < 360; i += step) {
            float size = 0.13f + 0.005f * (float)wormTick;
            float bigSize = 0.7f + 0.005f * (float)wormTick;
            if (wormCD > 0) {
                wormCD -= step;
                continue;
            }
            if ((wormTick += step) > 50) {
                wormCD = 100;
                wormTick = 0;
                ++wormCount;
                continue;
            }
            float val = Math.max(0.5f, 1.2f - 0.5f * this.animation.getValue());
            float sin = (float)(MathUtility.sin((float)Math.toRadians((float)i + this.moving.getValue() * 1.0f)) * (double)width * (double)val);
            float cos = (float)(MathUtility.cos((float)Math.toRadians((float)i + this.moving.getValue() * 1.0f)) * (double)width * (double)val);
            ms.push();
            ms.translate((double)sin, (double)(this.prevTarget.getHeight() / 1.5f) + (double)(this.prevTarget.getHeight() / 3.0f) * MathUtility.sin(Math.toRadians((float)i / 2.0f + this.moving.getValue() / 5.0f)), (double)cos);
            ms.multiply(camera.getRotation());
            DrawUtility.drawImage(ms, builder, (double)(-bigSize / 2.0f), (double)(-bigSize / 2.0f), (double)(-size / 2.0f), (double)bigSize, (double)bigSize, color.withAlpha(color.getAlpha() * this.animation.getValue() * 0.05f));
            DrawUtility.drawImage(ms, builder, (double)(-size / 2.0f), (double)(-size / 2.0f), (double)(-size / 2.0f), (double)size, (double)size, color.withAlpha(color.getAlpha() * this.animation.getValue()));
            ms.pop();
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
    }

    private Vec3d getRenderPos(LivingEntity target) {
        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
        return new Vec3d(MathHelper.lerp((double)tickDelta, (double)target.prevX, (double)target.getX()), MathHelper.lerp((double)tickDelta, (double)target.prevY, (double)target.getY()), MathHelper.lerp((double)tickDelta, (double)target.prevZ, (double)target.getZ()));
    }

    @Override
    public void tick() {
        super.tick();
    }
}

