/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.combat;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.systems.event.impl.game.PostAttackEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.combat.Aura;
import moscow.rockstar.systems.modules.modules.movement.ElytraStrafe;
import moscow.rockstar.systems.modules.modules.player.Blink;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.CombatUtility;
import moscow.rockstar.utility.game.ElytraUtility;
import moscow.rockstar.utility.game.prediction.ElytraPredictionSystem;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Elytra Target", category=ModuleCategory.COMBAT, desc="\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u043f\u0440\u0435\u0441\u043b\u0435\u0434\u043e\u0432\u0430\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0435")
public class ElytraTarget
extends BaseModule {
    private final SliderSetting fireworkSlot = new SliderSetting(this, "modules.settings.elytra_target.fireworkSlot").min(1.0f).max(9.0f).step(1.0f).currentValue(7.0f).suffix(" slot");
    private final BooleanSetting swapVector = new BooleanSetting(this, "modules.settings.elytra_target.swapVector").enable();
    private final BooleanSetting defensive = new BooleanSetting(this, "modules.settings.elytra_target.defensive").enable();
    private boolean defensiveActive;
    private boolean prevDefensive;
    private Vec3d defensivePos;
    private final EventListener<SendPacketEvent> onPacket = event -> {
        if (this.defensiveActive) {
            this.blink().savePacket((SendPacketEvent)event);
        }
    };
    private final EventListener<AttackEvent> onAttack = event -> {};
    private final EventListener<PostAttackEvent> onPostAttack = event -> {
        if (CombatUtility.getMace() != null) {
            ElytraUtility.swapInHotbar(false);
            if (ElytraTarget.mc.player.isSprinting() && ElytraTarget.mc.player.input.hasForwardMovement() && ElytraTarget.mc.player.checkGliding()) {
                ElytraTarget.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraTarget.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }
        LivingEntity target = Rockstar.getInstance().getTargetManager().getLivingTarget();
        ElytraUtility.setLastVec(ElytraUtility.leaveVec(target));
        ElytraUtility.useFirework(this.fireworkSlot.getCurrentValue());
    };
    private final EventListener<Render3DEvent> on3DRender = event -> {
        BuiltBuffer builtLinesBuffer;
        MatrixStack matrices = event.getMatrices();
        Camera camera = ElytraTarget.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder linesBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (AbstractClientPlayerEntity player : ElytraTarget.mc.world.getPlayers()) {
            if (ElytraTarget.mc.player == player) continue;
            ElytraUtility.drawBoxes(matrices, linesBuffer, player.getBoundingBox().offset(ElytraPredictionSystem.predictPlayerPosition((PlayerEntity)player)).offset(-player.getX(), -player.getY(), -player.getZ()).offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()), Colors.ACCENT.withAlpha(100.0f));
        }
        if (this.defensivePos != null) {
            ElytraUtility.drawBoxes(matrices, linesBuffer, ElytraTarget.mc.player.getBoundingBox().offset(this.defensivePos).offset(-ElytraTarget.mc.player.getX(), -ElytraTarget.mc.player.getY(), -ElytraTarget.mc.player.getZ()).offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()), Colors.ACCENT.withAlpha(200.0f));
        }
        if ((builtLinesBuffer = linesBuffer.endNullable()) != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtLinesBuffer);
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };
    private final EventListener<HudRenderEvent> on2DRender = event -> {};

    @Override
    public void tick() {
        PlayerEntity player;
        Aura aura = Rockstar.getInstance().getModuleManager().getModule(Aura.class);
        LivingEntity target = Rockstar.getInstance().getTargetManager().getLivingTarget();
        if (!Rockstar.getInstance().getModuleManager().getModule(ElytraStrafe.class).isEnabled() && ElytraUtility.getFireworkTimer().finished(target != null && ElytraTarget.mc.player.distanceTo((Entity)target) < 6.0f ? 500L : (target != null ? 1000L : 1500L)) && ElytraTarget.mc.player.isGliding() && !ElytraTarget.mc.player.isUsingItem()) {
            ElytraUtility.useFirework(this.fireworkSlot.getCurrentValue());
        }
        if (ElytraTarget.mc.player.isGliding() && target != null) {
            RotationHandler handler = Rockstar.getInstance().getRotationHandler();
            Rotation rot = RotationMath.getRotationTo(ElytraUtility.leaving() ? target.getEyePos().add(ElytraUtility.leaveVec(target)) : ElytraUtility.targetPoint(target));
            handler.rotate(rot, MoveCorrection.SILENT, 180.0f, 180.0f, 180.0f, RotationPriority.OVERRIDE);
        }
        if (InventoryUtility.getChestplateSlot().item() == Items.ELYTRA && ElytraTarget.mc.player.isSprinting() && ElytraTarget.mc.player.input.hasForwardMovement() && ElytraTarget.mc.player.checkGliding()) {
            ElytraTarget.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraTarget.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
        boolean bl = this.defensiveActive = this.defensive.isEnabled() && target instanceof PlayerEntity && !ElytraPredictionSystem.isLeaving(player = (PlayerEntity)target) && player.isGliding() && !ElytraUtility.leaving() && ElytraTarget.mc.player.distanceTo((Entity)target) > 10.0f;
        if (this.defensiveActive != this.prevDefensive) {
            if (this.defensiveActive) {
                this.blink().onEnable();
                this.defensivePos = ElytraTarget.mc.player.getPos();
            } else {
                this.blink().onDisable();
                this.defensivePos = null;
            }
        }
        this.prevDefensive = this.defensiveActive;
        if (CombatUtility.getMace() != null) {
            ElytraUtility.swapInHotbar(target != null && ElytraTarget.mc.player.distanceTo((Entity)target) < 10.0f && ElytraTarget.mc.player.getAttackCooldownProgress(0.0f) >= 1.0f && ElytraTarget.mc.player.fallDistance > 5.0f);
        }
    }

    private Blink blink() {
        return Rockstar.getInstance().getModuleManager().getModule(Blink.class);
    }

    @Override
    public void onDisable() {
        this.defensiveActive = false;
    }

    @Generated
    public BooleanSetting getSwapVector() {
        return this.swapVector;
    }

    @Generated
    public boolean isDefensiveActive() {
        return this.defensiveActive;
    }

    @Generated
    public boolean isPrevDefensive() {
        return this.prevDefensive;
    }
}

