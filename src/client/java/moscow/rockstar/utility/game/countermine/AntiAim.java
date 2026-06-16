/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.option.Perspective
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.entity.EntityRenderDispatcher
 *  net.minecraft.client.render.entity.EntityRenderer
 *  net.minecraft.client.render.entity.PlayerEntityRenderer
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package moscow.rockstar.utility.game.countermine;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.modules.other.CounterMine;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.countermine.RageBot;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class AntiAim
implements IMinecraft {
    public static boolean FORCE;
    public float yaw;
    public float pitch;
    private BooleanSetting antiAim;
    private BooleanSetting freestand;
    private ModeSetting mode;
    private ModeSetting.Value statich;
    private ModeSetting.Value fake;
    private CounterMine counterMine;
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (this.antiAim.isEnabled() && this.counterMine.getJumping().finished(1000L)) {
            float fromYaw = RageBot.TARGET_YAW;
            int age = AntiAim.mc.player.age;
            float f = this.statich.isSelected() ? fromYaw - 180.0f : (this.yaw = fromYaw - 90.0f - (float)(age % 5 == 0 ? 0 : (age % 5 == 1 ? 180 : 90)));
            if (this.freestand.isEnabled()) {
                boolean right;
                Vec3d eye = AntiAim.mc.player.getEyePos();
                float lfx = (float)MathUtility.cos(Math.toRadians(fromYaw));
                float lfz = (float)MathUtility.sin(Math.toRadians(fromYaw));
                float ltx = (float)(MathUtility.cos(Math.toRadians(fromYaw)) + MathUtility.cos(Math.toRadians(fromYaw + 90.0f)));
                float ltz = (float)(MathUtility.sin(Math.toRadians(fromYaw)) + MathUtility.sin(Math.toRadians(fromYaw + 90.0f)));
                float rfx = (float)MathUtility.cos(Math.toRadians(fromYaw - 180.0f));
                float rfz = (float)MathUtility.sin(Math.toRadians(fromYaw - 180.0f));
                float rtx = (float)(MathUtility.cos(Math.toRadians(fromYaw - 180.0f)) + MathUtility.cos(Math.toRadians(fromYaw + 90.0f)));
                float rtz = (float)(MathUtility.sin(Math.toRadians(fromYaw - 180.0f)) + MathUtility.sin(Math.toRadians(fromYaw + 90.0f)));
                boolean left = AntiAim.mc.world.raycast(new RaycastContext(eye.add((double)lfx, 0.0, (double)lfz), eye.add((double)ltx, 0.0, (double)ltz), RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, (Entity)AntiAim.mc.player)).getType() == HitResult.Type.MISS;
                boolean bl = right = AntiAim.mc.world.raycast(new RaycastContext(eye.add((double)rfx, 0.0, (double)rfz), eye.add((double)rtx, 0.0, (double)rtz), RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, (Entity)AntiAim.mc.player)).getType() == HitResult.Type.MISS;
                if (left != right) {
                    this.yaw = left ? (this.statich.isSelected() ? fromYaw - 270.0f : fromYaw - 270.0f - (float)(age % 5 == 0 ? 180 : 0)) : (this.statich.isSelected() ? fromYaw - 90.0f : fromYaw - 90.0f - (float)(age % 5 == 0 ? 180 : 0));
                }
            }
            this.pitch = 90.0f;
            AntiAim.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(AntiAim.mc.player.getX(), AntiAim.mc.player.getY(), AntiAim.mc.player.getZ(), this.yaw, this.pitch, AntiAim.mc.player.isOnGround(), AntiAim.mc.player.horizontalCollision));
        }
    };
    private final EventListener<Render3DEvent> on3DRender = event -> {
        if (AntiAim.mc.world == null || AntiAim.mc.player == null || !this.antiAim.isEnabled()) {
            return;
        }
        MatrixStack ms = event.getMatrices();
        Camera camera = AntiAim.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        ms.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        this.renderTransparentPlayer(ms, camera, event.getTickDelta());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        ms.pop();
    };

    public AntiAim(CounterMine cm) {
        this.counterMine = cm;
        this.antiAim = new BooleanSetting(cm, "AntAim");
        this.mode = new ModeSetting((SettingsContainer)cm, "AA Mode", () -> !this.antiAim.isEnabled());
        this.statich = new ModeSetting.Value(this.mode, "Static");
        this.fake = new ModeSetting.Value(this.mode, "Fake");
        this.freestand = new BooleanSetting((SettingsContainer)cm, "AA FreeStand", () -> !this.antiAim.isEnabled());
    }

    public void renderTransparentPlayer(MatrixStack matrices, Camera camera, float tickDelta) {
        if (AntiAim.mc.player == null || AntiAim.mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            return;
        }
        Vec3d playerPos = AntiAim.mc.player.getPos();
        Vec3d renderPos = Utils.getInterpolatedPos((Entity)AntiAim.mc.player, tickDelta);
        Vec3d cameraPos = camera.getPos();
        matrices.push();
        double x = renderPos.x - cameraPos.x;
        double y = renderPos.y - cameraPos.y;
        double z = renderPos.z - cameraPos.z;
        ColorRGBA c = Colors.ACCENT;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor((float)(c.getRed() / 255.0f), (float)(c.getGreen() / 255.0f), (float)(c.getBlue() / 255.0f), (float)0.5f);
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        PlayerEntityRenderer playerRenderer = (PlayerEntityRenderer)(Object)dispatcher.getRenderer((Entity)AntiAim.mc.player);
        if (playerRenderer != null) {
            float originalYaw = AntiAim.mc.player.getYaw();
            float originalPitch = AntiAim.mc.player.getPitch();
            float originalHeadYaw = AntiAim.mc.player.headYaw;
            float originalBodyYaw = AntiAim.mc.player.bodyYaw;
            float originalPrevYaw = AntiAim.mc.player.prevYaw;
            float originalPrevPitch = AntiAim.mc.player.prevPitch;
            float originalPrevHeadYaw = AntiAim.mc.player.prevHeadYaw;
            float originalPrevBodyYaw = AntiAim.mc.player.prevBodyYaw;
            AntiAim.mc.player.setYaw(this.yaw);
            AntiAim.mc.player.setPitch(this.pitch);
            AntiAim.mc.player.headYaw = this.yaw;
            AntiAim.mc.player.bodyYaw = this.yaw;
            AntiAim.mc.player.prevYaw = this.yaw;
            AntiAim.mc.player.prevPitch = this.pitch;
            AntiAim.mc.player.prevHeadYaw = this.yaw;
            AntiAim.mc.player.prevBodyYaw = this.yaw;
            FORCE = true;
            try {
                dispatcher.render((Entity)AntiAim.mc.player, x, y, z, tickDelta, matrices, (VertexConsumerProvider)mc.getBufferBuilders().getEntityVertexConsumers(), 0xF000F0, (EntityRenderer)playerRenderer);
                mc.getBufferBuilders().getEntityVertexConsumers().draw();
            }
            catch (Exception exception) {
                // empty catch block
            }
            FORCE = false;
            AntiAim.mc.player.setYaw(originalYaw);
            AntiAim.mc.player.setPitch(originalPitch);
            AntiAim.mc.player.headYaw = originalHeadYaw;
            AntiAim.mc.player.bodyYaw = originalBodyYaw;
            AntiAim.mc.player.prevYaw = originalPrevYaw;
            AntiAim.mc.player.prevPitch = originalPrevPitch;
            AntiAim.mc.player.prevHeadYaw = originalPrevHeadYaw;
            AntiAim.mc.player.prevBodyYaw = originalPrevBodyYaw;
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.disableBlend();
        matrices.pop();
    }

    @Generated
    public BooleanSetting getAntiAim() {
        return this.antiAim;
    }

    @Generated
    public BooleanSetting getFreestand() {
        return this.freestand;
    }
}
