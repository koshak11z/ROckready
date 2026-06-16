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
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Arrows", category=ModuleCategory.VISUALS, desc="modules.descriptions.tracers")
public class Arrows
extends BaseModule {
    private final BooleanSetting lines = new BooleanSetting(this, "lines");
    private final SelectSetting targets = new SelectSetting((SettingsContainer)this, "modules.settings.tracers.targets", "modules.settings.tracers.targets.description");
    private final SelectSetting.Value players = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.players").select();
    private final SelectSetting.Value animals = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.animals");
    private final SelectSetting.Value mobs = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.mobs");
    private final SelectSetting.Value invisibles = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.invisibles").select();
    private final SelectSetting.Value nakedPlayers = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.naked_players").select();
    private final SelectSetting.Value friends = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.friends").select();
    private final Map<Entity, ArrowsAnimation> animations = new HashMap<Entity, ArrowsAnimation>();
    private final EventListener<HudRenderEvent> onHud = event -> {
        if (Arrows.mc.player == null || Arrows.mc.world == null || this.lines.isEnabled()) {
            return;
        }
        CustomDrawContext context = event.getContext();
        MatrixStack ms = context.getMatrices();
        TargetSettings targetSettings = new TargetSettings.Builder().targetPlayers(this.players.isSelected()).targetAnimals(this.animals.isSelected()).targetInvisibles(this.invisibles.isSelected()).targetFriends(this.friends.isSelected()).targetNakedPlayers(this.nakedPlayers.isSelected()).targetMobs(this.mobs.isSelected()).build();
        HashSet<Entity> toRemove = new HashSet<Entity>();
        for (Map.Entry<Entity, ArrowsAnimation> entry : this.animations.entrySet()) {
            LivingEntity livingEntity;
            Entity entity = entry.getKey();
            ArrowsAnimation animation = entry.getValue();
            boolean shouldShow = Arrows.mc.world.hasEntity(entity) && entity instanceof LivingEntity && targetSettings.isEntityValid((Entity)(livingEntity = (LivingEntity)entity));
            animation.showing.update(shouldShow);
            animation.showing.setDuration(500L);
            if (animation.showing.getValue() != 0.0f || shouldShow) continue;
            toRemove.add(entity);
        }
        for (Entity entity : Arrows.mc.world.getEntities()) {
            LivingEntity livingEntity;
            if (!(entity instanceof LivingEntity) || !targetSettings.isEntityValid((Entity)(livingEntity = (LivingEntity)entity)) || this.animations.containsKey(entity)) continue;
            this.animations.put(entity, new ArrowsAnimation());
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.disableCull();
        ms.push();
        IconBatching iconBatching = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());
        ms.translate(sr.getScaledWidth() / 2.0f, sr.getScaledHeight() / 2.0f, 0.0f);
        for (Map.Entry<Entity, ArrowsAnimation> arrow : this.animations.entrySet()) {
            if (!(arrow.getValue().showing.getValue() > 0.0f)) continue;
            RenderUtility.rotate(ms, 0.0f, 0.0f, this.calculateAngle(arrow.getKey(), event.getTickDelta()));
            RenderUtility.scale(ms, 0.0f, 0.0f, 2.0f - arrow.getValue().showing.getValue());
            context.drawTexture(Rockstar.id("textures/arrow.png"), -10.0f, 40.0f, 20.0f, 20.0f, (Rockstar.getInstance().getFriendManager().isFriend(arrow.getKey().getName().getString()) ? Colors.GREEN : Colors.ACCENT).mulAlpha(arrow.getValue().showing.getValue()));
            RenderUtility.end(ms);
            RenderUtility.end(ms);
        }
        iconBatching.draw();
        for (Entity entity : toRemove) {
            this.animations.remove(entity);
        }
        ms.pop();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    };
    private final EventListener<Render3DEvent> on3DRender = event -> {
        if (Arrows.mc.player == null || Arrows.mc.world == null || !this.lines.isEnabled()) {
            return;
        }
        MatrixStack matrices = event.getMatrices();
        TargetSettings targetSettings = new TargetSettings.Builder().targetPlayers(this.players.isSelected()).targetAnimals(this.animals.isSelected()).targetInvisibles(this.invisibles.isSelected()).targetFriends(this.friends.isSelected()).targetNakedPlayers(this.nakedPlayers.isSelected()).targetMobs(this.mobs.isSelected()).build();
        RenderUtility.setupRender3D(false);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (Entity entity : Arrows.mc.world.getEntities()) {
            LivingEntity livingEntity;
            if (!(entity instanceof LivingEntity) || !targetSettings.isEntityValid((Entity)(livingEntity = (LivingEntity)entity))) continue;
            Vec3d entityPos = Utils.getInterpolatedPos((Entity)livingEntity, event.getTickDelta());
            Draw3DUtility.renderLineFromPlayer(matrices, builder, entityPos.add(0.0, (double)(livingEntity.getHeight() / 2.0f), 0.0), Colors.WHITE);
        }
        RenderUtility.buildBuffer(builder);
        RenderUtility.endRender3D();
    };

    private float calculateAngle(Entity entity, float partialTicks) {
        Vec3d pos = Utils.getInterpolatedPos(entity, partialTicks).subtract(Arrows.mc.gameRenderer.getCamera().getPos());
        double cos = MathHelper.cos((float)((float)((double)Arrows.mc.gameRenderer.getCamera().getYaw() * (Math.PI / 180))));
        double sin = MathHelper.sin((float)((float)((double)Arrows.mc.gameRenderer.getCamera().getYaw() * (Math.PI / 180))));
        double rotY = -(pos.z * cos - pos.x * sin);
        double rotX = -(pos.x * cos + pos.z * sin);
        return (float)(Math.atan2(rotY, rotX) * 180.0 / Math.PI - 90.0);
    }

    public BooleanSetting getLines() {
        return this.lines;
    }

    static class ArrowsAnimation {
        Animation showing = new Animation(300L, Easing.BAKEK);
        Animation rotating = new Animation(300L, Easing.BAKEK);

        ArrowsAnimation() {
        }
    }
}

