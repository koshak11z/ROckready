package im.zov4ik.features.impl.render;

import im.zov4ik.utils.client.managers.event.EventHandler;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.SkinTextures.Model;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import im.zov4ik.events.render.WorldRenderEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.display.color.ColorAssist;

public class ShaderESP extends Module {
   private final SliderSettings alpha = new SliderSettings("Прозрачность", "Прозрачность ESP").range(0.1F, 1.0F).setValue(0.8F);
   private final SliderSettings brightness = new SliderSettings("Яркость", "Яркость ESP").range(0.1F, 1.0F).setValue(0.6F);
   private final SliderSettings lineWidth = new SliderSettings("Толщина линий", "Толщина линий").range(0.1F, 3.0F).setValue(1.0F);
   private final BooleanSetting throughWalls = new BooleanSetting("Сквозь стены", "Рендерить сквозь стены").setValue(true);
   private final BooleanSetting glow = new BooleanSetting("Свечение", "Свечение линий").setValue(true);
   private final SliderSettings glowIntensity = new SliderSettings("Сила свечения", "Интенсивность свечения").range(0.5F, 4.0F).setValue(1.5F);
   private final SliderSettings glowLayers = new SliderSettings("Слои свечения", "Количество слоев свечения").range(1, 6).setValue(3.0F);

   public ShaderESP() {
      super("ShaderESP", "Shader ESP", ModuleCategory.RENDER);
      setup(alpha, brightness, lineWidth, throughWalls, glow, glowIntensity, glowLayers);
   }

   @EventHandler // 1
   public void onRender(WorldRenderEvent event) {
      if (this.mc.world != null && this.mc.player != null) {
         float tickDelta = event.getPartialTicks();
         Vec3d cam = this.mc.gameRenderer.getCamera().getPos();
         int color = ColorAssist.getClientColor();

         for (Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof AbstractClientPlayerEntity player
               && player.isAlive()
               && (player != this.mc.player || this.mc.options.getPerspective() != Perspective.FIRST_PERSON)) {
               this.renderChams(event.getStack(), player, cam, tickDelta, color);
            }
         }
      }
   }

   private void renderChams(MatrixStack stack, AbstractClientPlayerEntity player, Vec3d cam, float tickDelta, int color) {
      float a = this.alpha.getValue();
      float bright = this.brightness.getValue();
      float r = (color >> 16 & 0xFF) / 255.0F * bright;
      float g = (color >> 8 & 0xFF) / 255.0F * bright;
      float b = (color & 0xFF) / 255.0F * bright;
      double x = player.prevX + (player.getX() - player.prevX) * tickDelta;
      double y = player.prevY + (player.getY() - player.prevY) * tickDelta;
      double z = player.prevZ + (player.getZ() - player.prevZ) * tickDelta;
      float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);
      float headYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevHeadYaw, player.headYaw);
      float pitch = MathHelper.lerp(tickDelta, player.prevPitch, player.getPitch());
      float netHeadYaw = MathHelper.wrapDegrees(headYaw - bodyYaw);
      float limbPos = player.limbAnimator.getPos(tickDelta);
      float limbSpeed = player.limbAnimator.getSpeed(tickDelta);
      float swing = MathHelper.sin(limbPos * 0.6662F) * 0.6F * limbSpeed;
      float swingProgress = player.getHandSwingProgress(tickDelta);
      boolean mainRight = player.getMainArm() == Arm.RIGHT;
      Hand swingHand = player.preferredHand;
      float swingAngle = -((float)(Math.sin(Math.sqrt(swingProgress) * Math.PI) * 1.2F));
      float rightSwingX = swingHand == Hand.MAIN_HAND == mainRight ? swingAngle : 0.0F;
      float leftSwingX = swingHand == Hand.MAIN_HAND != mainRight ? swingAngle : 0.0F;
      boolean slim = player.getSkinTextures().model() == Model.SLIM;
      boolean sneak = player.isInSneakingPose();
      boolean elytra = player.isGliding();
      boolean swim = player.isSwimming();
      stack.push();
      stack.translate(x - cam.x, y - cam.y, z - cam.z);
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - bodyYaw));
      if (elytra) {
         float ticks = player.getGlidingTicks() + tickDelta;
         float factor = MathHelper.clamp(ticks * ticks / 100.0F, 0.0F, 1.0F);
         if (!player.isUsingRiptide()) {
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(factor * (-90.0F - pitch)));
         }
      } else if (swim) {
         float swimPitch = player.isSubmergedInWater() ? -90.0F - pitch : -90.0F;
         stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swimPitch));
         stack.translate(0.0, -1.0, 0.3);
      }

      stack.scale(-1.0F, -1.0F, 1.0F);
      stack.scale(0.9375F, 0.9375F, 0.9375F);
      stack.translate(0.0, -1.501, 0.0);
      if (sneak) {
         stack.translate(0.0, 0.2, 0.0);
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      if (this.throughWalls.isValue()) {
         RenderSystem.disableDepthTest();
      }

      float idleTime = (player.age + tickDelta) * 0.05F;
      
      float lw = this.lineWidth.getValue();
      float la = MathHelper.clamp(a + 0.2F, 0.0F, 1.0F);
      float lr = MathHelper.clamp(r + 0.15F, 0.0F, 1.0F);
      float lg = MathHelper.clamp(g + 0.15F, 0.0F, 1.0F);
      float lb = MathHelper.clamp(b + 0.15F, 0.0F, 1.0F);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
      RenderSystem.lineWidth(lw);
      if (this.glow.isValue()) {
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ONE, DstFactor.ZERO);
         int layers = Math.max(1, this.glowLayers.getInt());
         float intensity = this.glowIntensity.getValue();

         for (int i = layers; i >= 1; i--) {
            float expand = i * 0.5F * intensity;
            float glowA = MathHelper.clamp(la * (1.0F / (i + 1)) * 0.7F, 0.0F, 1.0F);
            BufferBuilder buf = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.LINES);
            this.drawBodyLines(
               stack, buf, swing, rightSwingX, leftSwingX, netHeadYaw, pitch, slim, sneak, swim, limbPos, limbSpeed, idleTime, lr, lg, lb, glowA, expand
            );
            BufferRenderer.drawWithGlobalProgram(buf.end());
         }

         RenderSystem.defaultBlendFunc();
      }

      BufferBuilder buf = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.LINES);
      this.drawBodyLines(stack, buf, swing, rightSwingX, leftSwingX, netHeadYaw, pitch, slim, sneak, swim, limbPos, limbSpeed, idleTime, lr, lg, lb, la, 0.0F);
      BufferRenderer.drawWithGlobalProgram(buf.end());
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableCull();
      if (this.throughWalls.isValue()) {
         RenderSystem.enableDepthTest();
      }

      RenderSystem.disableBlend();
      stack.pop();
   }

   private void drawBody(
      MatrixStack m,
      BufferBuilder buf,
      float swing,
      float rightSwingX,
      float leftSwingX,
      float headYaw,
      float headPitch,
      boolean slim,
      boolean sneak,
      boolean swim,
      float limbPos,
      float limbSpeed,
      float idleTime,
      float r,
      float g,
      float b,
      float a
   ) {
      float u = 0.0625F;
      float armW = slim ? 3.0F : 4.0F;
      float armSwayZ = MathHelper.sin(idleTime) * 0.04F + 0.03F * limbSpeed;
      float swimPhase = limbPos * 0.6662F;
      float swimCycle = MathHelper.sin(swimPhase) * limbSpeed;
      float swimKick = swim ? swimCycle * 0.4F : 0.0F;
      m.push();
      if (sneak) {
         m.translate(0.0F, 12.0F * u, 0.0F);
         m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.64F));
         m.translate(0.0F, -12.0F * u, 0.0F);
      }

      m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(headYaw));
      m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch));
      this.box(buf, m.peek().getPositionMatrix(), -4.0F * u, -8.0F * u, -4.0F * u, 8.0F * u, 8.0F * u, 8.0F * u, r, g, b, a);
      m.pop();
      m.push();
      if (sneak) {
         m.translate(0.0F, 12.0F * u, 0.0F);
         m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.64F));
         m.translate(0.0F, -12.0F * u, 0.0F);
      }

      if (swim) {
         m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(limbPos * 0.3331F) * 3.0F * limbSpeed));
      }

      this.box(buf, m.peek().getPositionMatrix(), -4.0F * u, 0.0F, -2.0F * u, 8.0F * u, 12.0F * u, 4.0F * u, r, g, b, a);
      m.pop();
      float swimArmX = swim ? swimCycle * 0.6F - (float) (Math.PI / 2) : 0.0F;
      float rightArmX = swim ? swimArmX : swing;
      float leftArmX = swim ? swimArmX : -swing;
      float swimSpread = swim ? MathHelper.clamp(swimCycle, 0.0F, 1.0F) * (float) (Math.PI / 4) : 0.0F;
      float rightArmZ = swim ? swimSpread : 0.0F;
      float leftArmZ = swim ? -swimSpread : 0.0F;
      m.push();
      if (sneak) {
         m.translate(0.0F, 12.0F * u, 0.0F);
         m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.64F));
         m.translate(0.0F, -12.0F * u, 0.0F);
      }

      m.translate(-4.0F * u, 0.0F, 0.0F);
      m.multiply(RotationAxis.POSITIVE_X.rotation(rightArmX + (swim ? 0.0F : rightSwingX)));
      m.multiply(RotationAxis.POSITIVE_Z.rotation(swim ? rightArmZ : armSwayZ));
      this.box(buf, m.peek().getPositionMatrix(), -armW * u, 0.0F, -2.0F * u, armW * u, 12.0F * u, 4.0F * u, r, g, b, a);
      m.pop();
      m.push();
      if (sneak) {
         m.translate(0.0F, 12.0F * u, 0.0F);
         m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.64F));
         m.translate(0.0F, -12.0F * u, 0.0F);
      }

      m.translate(4.0F * u, 0.0F, 0.0F);
      m.multiply(RotationAxis.POSITIVE_X.rotation(leftArmX + (swim ? 0.0F : leftSwingX)));
      m.multiply(RotationAxis.POSITIVE_Z.rotation(swim ? leftArmZ : -armSwayZ));
      this.box(buf, m.peek().getPositionMatrix(), 0.0F, 0.0F, -2.0F * u, armW * u, 12.0F * u, 4.0F * u, r, g, b, a);
      m.pop();
      m.push();
      m.translate(-2.0F * u, 12.0F * u, 0.0F);
      m.multiply(RotationAxis.POSITIVE_X.rotation(swim ? -swimKick : -swing));
      this.box(buf, m.peek().getPositionMatrix(), -2.0F * u, 0.0F, -2.0F * u, 4.0F * u, 12.0F * u, 4.0F * u, r, g, b, a);
      m.pop();
      m.push();
      m.translate(2.0F * u, 12.0F * u, 0.0F);
      m.multiply(RotationAxis.POSITIVE_X.rotation(swim ? swimKick : swing));
      this.box(buf, m.peek().getPositionMatrix(), -2.0F * u, 0.0F, -2.0F * u, 4.0F * u, 12.0F * u, 4.0F * u, r, g, b, a);
      m.pop();
   }

   private void drawBodyLines(
      MatrixStack m,
      BufferBuilder buf,
      float swing,
      float rightSwingX,
      float leftSwingX,
      float headYaw,
      float headPitch,
      boolean slim,
      boolean sneak,
      boolean swim,
      float limbPos,
      float limbSpeed,
      float idleTime,
      float r,
      float g,
      float b,
      float a,
      float expand
   ) {
      float u = 0.0625F;
      float armW = slim ? 3.0F : 4.0F;
      float armSwayZ = MathHelper.sin(idleTime) * 0.04F + 0.03F * limbSpeed;
      float swimPhase = limbPos * 0.6662F;
      float swimCycle = MathHelper.sin(swimPhase) * limbSpeed;
      float swimArmX = swim ? swimCycle * 0.6F - (float) (Math.PI / 2) : 0.0F;
      float rightArmX = swim ? swimArmX : swing;
      float leftArmX = swim ? swimArmX : -swing;
      float swimSpread = swim ? MathHelper.clamp(swimCycle, 0.0F, 1.0F) * (float) (Math.PI / 4) : 0.0F;
      float rightArmZ = swim ? swimSpread : 0.0F;
      float leftArmZ = swim ? -swimSpread : 0.0F;
      float swimKick = swim ? swimCycle * 0.4F : 0.0F;
      float ex = expand * u;
      m.push();
      if (sneak) {
         m.translate(0.0F, 12.0F * u, 0.0F);
         m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.64F));
         m.translate(0.0F, -12.0F * u, 0.0F);
      }

      m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(headYaw));
      m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch));
      this.boxLines(
         buf,
         m.peek().getPositionMatrix(),
         -4.0F * u - ex,
         -8.0F * u - ex,
         -4.0F * u - ex,
         8.0F * u + ex * 2.0F,
         8.0F * u + ex * 2.0F,
         8.0F * u + ex * 2.0F,
         r,
         g,
         b,
         a
      );
      m.pop();
      m.push();
      if (sneak) {
         m.translate(0.0F, 12.0F * u, 0.0F);
         m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.64F));
         m.translate(0.0F, -12.0F * u, 0.0F);
      }

      if (swim) {
         m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(limbPos * 0.3331F) * 3.0F * limbSpeed));
      }

      this.boxLines(
         buf,
         m.peek().getPositionMatrix(),
         -4.0F * u - ex,
         0.0F - ex,
         -2.0F * u - ex,
         8.0F * u + ex * 2.0F,
         12.0F * u + ex * 2.0F,
         4.0F * u + ex * 2.0F,
         r,
         g,
         b,
         a
      );
      m.pop();
      m.push();
      if (sneak) {
         m.translate(0.0F, 12.0F * u, 0.0F);
         m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.64F));
         m.translate(0.0F, -12.0F * u, 0.0F);
      }

      m.translate(-4.0F * u, 0.0F, 0.0F);
      m.multiply(RotationAxis.POSITIVE_X.rotation(rightArmX + (swim ? 0.0F : rightSwingX)));
      m.multiply(RotationAxis.POSITIVE_Z.rotation(swim ? rightArmZ : armSwayZ));
      this.boxLines(
         buf,
         m.peek().getPositionMatrix(),
         -armW * u - ex,
         0.0F - ex,
         -2.0F * u - ex,
         armW * u + ex * 2.0F,
         12.0F * u + ex * 2.0F,
         4.0F * u + ex * 2.0F,
         r,
         g,
         b,
         a
      );
      m.pop();
      m.push();
      if (sneak) {
         m.translate(0.0F, 12.0F * u, 0.0F);
         m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.64F));
         m.translate(0.0F, -12.0F * u, 0.0F);
      }

      m.translate(4.0F * u, 0.0F, 0.0F);
      m.multiply(RotationAxis.POSITIVE_X.rotation(leftArmX + (swim ? 0.0F : leftSwingX)));
      m.multiply(RotationAxis.POSITIVE_Z.rotation(swim ? leftArmZ : -armSwayZ));
      this.boxLines(
         buf,
         m.peek().getPositionMatrix(),
         0.0F - ex,
         0.0F - ex,
         -2.0F * u - ex,
         armW * u + ex * 2.0F,
         12.0F * u + ex * 2.0F,
         4.0F * u + ex * 2.0F,
         r,
         g,
         b,
         a
      );
      m.pop();
      m.push();
      m.translate(-2.0F * u, 12.0F * u, 0.0F);
      m.multiply(RotationAxis.POSITIVE_X.rotation(swim ? -swimKick : -swing));
      this.boxLines(
         buf,
         m.peek().getPositionMatrix(),
         -2.0F * u - ex,
         0.0F - ex,
         -2.0F * u - ex,
         4.0F * u + ex * 2.0F,
         12.0F * u + ex * 2.0F,
         4.0F * u + ex * 2.0F,
         r,
         g,
         b,
         a
      );
      m.pop();
      m.push();
      m.translate(2.0F * u, 12.0F * u, 0.0F);
      m.multiply(RotationAxis.POSITIVE_X.rotation(swim ? swimKick : swing));
      this.boxLines(
         buf,
         m.peek().getPositionMatrix(),
         -2.0F * u - ex,
         0.0F - ex,
         -2.0F * u - ex,
         4.0F * u + ex * 2.0F,
         12.0F * u + ex * 2.0F,
         4.0F * u + ex * 2.0F,
         r,
         g,
         b,
         a
      );
      m.pop();
   }

   private void box(BufferBuilder b, Matrix4f m, float x, float y, float z, float sx, float sy, float sz, float r, float g, float bl, float a) {
      float x2 = x + sx;
      float y2 = y + sy;
      float z2 = z + sz;
      b.vertex(m, x, y, z2).color(r, g, bl, a);
      b.vertex(m, x2, y, z2).color(r, g, bl, a);
      b.vertex(m, x2, y2, z2).color(r, g, bl, a);
      b.vertex(m, x, y2, z2).color(r, g, bl, a);
      b.vertex(m, x2, y, z).color(r, g, bl, a);
      b.vertex(m, x, y, z).color(r, g, bl, a);
      b.vertex(m, x, y2, z).color(r, g, bl, a);
      b.vertex(m, x2, y2, z).color(r, g, bl, a);
      b.vertex(m, x, y, z).color(r, g, bl, a);
      b.vertex(m, x, y, z2).color(r, g, bl, a);
      b.vertex(m, x, y2, z2).color(r, g, bl, a);
      b.vertex(m, x, y2, z).color(r, g, bl, a);
      b.vertex(m, x2, y, z2).color(r, g, bl, a);
      b.vertex(m, x2, y, z).color(r, g, bl, a);
      b.vertex(m, x2, y2, z).color(r, g, bl, a);
      b.vertex(m, x2, y2, z2).color(r, g, bl, a);
      b.vertex(m, x, y2, z2).color(r, g, bl, a);
      b.vertex(m, x2, y2, z2).color(r, g, bl, a);
      b.vertex(m, x2, y2, z).color(r, g, bl, a);
      b.vertex(m, x, y2, z).color(r, g, bl, a);
      b.vertex(m, x, y, z).color(r, g, bl, a);
      b.vertex(m, x2, y, z).color(r, g, bl, a);
      b.vertex(m, x2, y, z2).color(r, g, bl, a);
      b.vertex(m, x, y, z2).color(r, g, bl, a);
   }

   private void boxLines(BufferBuilder b, Matrix4f m, float x, float y, float z, float sx, float sy, float sz, float r, float g, float bl, float a) {
      float x2 = x + sx;
      float y2 = y + sy;
      float z2 = z + sz;
      this.line(b, m, x, y, z, x2, y, z, r, g, bl, a);
      this.line(b, m, x2, y, z, x2, y2, z, r, g, bl, a);
      this.line(b, m, x2, y2, z, x, y2, z, r, g, bl, a);
      this.line(b, m, x, y2, z, x, y, z, r, g, bl, a);
      this.line(b, m, x, y, z2, x2, y, z2, r, g, bl, a);
      this.line(b, m, x2, y, z2, x2, y2, z2, r, g, bl, a);
      this.line(b, m, x2, y2, z2, x, y2, z2, r, g, bl, a);
      this.line(b, m, x, y2, z2, x, y, z2, r, g, bl, a);
      this.line(b, m, x, y, z, x, y, z2, r, g, bl, a);
      this.line(b, m, x2, y, z, x2, y, z2, r, g, bl, a);
      this.line(b, m, x2, y2, z, x2, y2, z2, r, g, bl, a);
      this.line(b, m, x, y2, z, x, y2, z2, r, g, bl, a);
   }

   private void line(BufferBuilder b, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float bl, float a) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      float dz = z2 - z1;
      float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (len == 0.0F) {
         len = 1.0F;
      }

      b.vertex(m, x1, y1, z1).color(r, g, bl, a).normal(dx / len, dy / len, dz / len);
      b.vertex(m, x2, y2, z2).color(r, g, bl, a).normal(dx / len, dy / len, dz / len);
   }
}

