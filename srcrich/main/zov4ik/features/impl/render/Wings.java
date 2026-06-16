package im.zov4ik.features.impl.render;

import im.zov4ik.utils.client.managers.event.EventHandler;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
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

public class Wings extends Module {
   private static final float DEFAULT_SPREAD = 8.0F;
   private static final int DEFAULT_ALPHA = 220;
   private static final Wings.WingPoint[] SHAPE = new Wings.WingPoint[]{
      new Wings.WingPoint(0.08F, 0.1F, 0.88F),
      new Wings.WingPoint(0.28F, 0.34F, 0.78F),
      new Wings.WingPoint(0.56F, 0.82F, 0.62F),
      new Wings.WingPoint(0.86F, 0.3F, 0.52F),
      new Wings.WingPoint(1.14F, 0.46F, 0.4F),
      new Wings.WingPoint(1.24F, 0.04F, 0.3F),
      new Wings.WingPoint(1.02F, -0.18F, 0.28F),
      new Wings.WingPoint(1.18F, -0.64F, 0.22F),
      new Wings.WingPoint(0.86F, -0.46F, 0.2F),
      new Wings.WingPoint(0.8F, -0.98F, 0.14F),
      new Wings.WingPoint(0.54F, -0.74F, 0.16F),
      new Wings.WingPoint(0.3F, -1.16F, 0.12F),
      new Wings.WingPoint(0.1F, -0.54F, 0.18F)
   };
   private final BooleanSetting self = new BooleanSetting("На себя", "Рисовать крылья на себе").setValue(true);
   private final BooleanSetting players = new BooleanSetting("На игроков", "Рисовать крылья на игроках").setValue(false);
   private final SliderSettings size = new SliderSettings("Размер", "Размер крыльев").range(0.75F, 1.35F).setValue(1.0F);
   private float selfBodyYaw;
   private boolean selfBodyYawInitialized;

   public Wings() {
      super("Wings", "Wings", ModuleCategory.RENDER);
      setup(self, players, size);
   }

   @EventHandler
   public void onRender3D(WorldRenderEvent event) {
      if (this.mc.player != null && this.mc.world != null && this.mc.gameRenderer != null) {
         MatrixStack stack = event.getStack();
         float tickDelta = event.getPartialTicks();
         Vec3d camera = this.mc.gameRenderer.getCamera().getPos();
         stack.push();
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         if (this.self.isValue()
            && !this.mc.options.getPerspective().isFirstPerson()
            && this.mc.player.isAlive()
            && !this.hasElytra(this.mc.player)) {
            try {
               this.renderWings(stack, this.mc.player, tickDelta, camera);
            } catch (Exception var10) {
            }
         }

         if (this.players.isValue()) {
            for (Entity entity : this.mc.world.getEntities()) {
               if (entity instanceof PlayerEntity player && player != this.mc.player && player.isAlive() && !this.hasElytra(player)) {
                  try {
                     this.renderWings(stack, player, tickDelta, camera);
                  } catch (Exception var9) {
                  }
               }
            }
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ZERO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         stack.pop();
      }
   }

   private boolean hasElytra(PlayerEntity player) {
      return player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
   }

   private void renderWings(MatrixStack stack, PlayerEntity player, float tickDelta, Vec3d camera) {
      double x = MathHelper.lerp(tickDelta, player.prevX, player.getX()) - camera.x;
      double y = MathHelper.lerp(tickDelta, player.prevY, player.getY()) - camera.y;
      double z = MathHelper.lerp(tickDelta, player.prevZ, player.getZ()) - camera.z;
      float bodyYaw = this.resolveBodyYaw(player, tickDelta);
      float move = MathHelper.clamp(player.limbAnimator.getSpeed(tickDelta), 0.0F, 1.0F);
      Wings.WingPose pose = this.resolvePose(player, tickDelta);
      if (pose != null) {
         float flap = (float)Math.sin((player.age + tickDelta) * pose.flapSpeed) * pose.flapAmplitude;
         float open = (8.0F + flap + move * pose.motionSpreadBoost) * pose.openMultiplier;
         float wingScale = this.size.getValue() * pose.scaleMultiplier;
         int baseColor = this.resolveBaseColor();
         int glowColor = this.resolveGlowColor(baseColor);
         int coreColor = this.resolveCoreColor(baseColor);
         int outlineColor = baseColor;
         stack.push();
         stack.translate(x, y, z);
         stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - bodyYaw));
         if (pose.preTranslateY != 0.0F || pose.preTranslateZ != 0.0F) {
            stack.translate(0.0F, pose.preTranslateY, pose.preTranslateZ);
         }

         if (pose.pitchRotation != 0.0F) {
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pose.pitchRotation));
         }

         if (pose.rollRotation != 0.0F) {
            stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(pose.rollRotation));
         }

         stack.translate(0.0F, pose.anchorY, pose.anchorZ);
         stack.scale(wingScale, wingScale, wingScale);
         this.renderWingSide(stack, -1.0F, open, baseColor, glowColor, coreColor, outlineColor, pose);
         this.renderWingSide(stack, 1.0F, open, baseColor, glowColor, coreColor, outlineColor, pose);
         stack.pop();
      }
   } // 1

   private void renderWingSide(MatrixStack stack, float side, float open, int baseColor, int glowColor, int coreColor, int outlineColor, Wings.WingPose pose) {
      stack.push();
      stack.translate(side * pose.sideOffset, pose.sideYOffset, pose.sideZOffset);
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * open));
      stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * pose.sideRoll));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pose.sidePitch));
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      this.drawWingLayer(stack, side, 1.22F, setAlpha(glowColor, 48), setAlpha(glowColor, 0));
      this.drawWingLayer(stack, side, 0.84F, setAlpha(coreColor, 57), setAlpha(coreColor, 0));
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
      this.drawWingLayer(stack, side, 1.0F, setAlpha(baseColor, 220), setAlpha(baseColor, 10));
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      this.drawWingOutline(stack, side, 1.0F, setAlpha(outlineColor, 136));
      this.drawWingRibs(stack, side, 0.96F, setAlpha(glowColor, 44));
      stack.pop();
   }

   private void drawWingLayer(MatrixStack stack, float side, float scale, int rootColor, int edgeColor) {
      Matrix4f matrix = stack.peek().getPositionMatrix();
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

      for (int i = 0; i < SHAPE.length; i++) {
         Wings.WingPoint cur = SHAPE[i];
         Wings.WingPoint next = SHAPE[(i + 1) % SHAPE.length];
         this.vertex(buffer, matrix, 0.0F, 0.0F, 0.0F, rootColor);
         this.vertex(buffer, matrix, side * cur.x * scale, cur.y * scale, 0.0F, this.applyPointAlpha(edgeColor, cur.alphaMul));
         this.vertex(buffer, matrix, side * next.x * scale, next.y * scale, 0.0F, this.applyPointAlpha(edgeColor, next.alphaMul));
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }

   private void drawWingOutline(MatrixStack stack, float side, float scale, int color) {
      Matrix4f matrix = stack.peek().getPositionMatrix();
      RenderSystem.lineWidth(1.35F);
      GL11.glEnable(2848);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

      for (Wings.WingPoint point : SHAPE) {
         this.vertex(buffer, matrix, side * point.x * scale, point.y * scale, 0.0F, color);
      }

      this.vertex(buffer, matrix, side * SHAPE[0].x * scale, SHAPE[0].y * scale, 0.0F, color);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      GL11.glDisable(2848);
   }

   private void drawWingRibs(MatrixStack stack, float side, float scale, int color) {
      Matrix4f matrix = stack.peek().getPositionMatrix();
      int[] ribIndices = new int[]{2, 4, 7, 9, 11};
      RenderSystem.lineWidth(0.9F);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.POSITION_COLOR);

      for (int idx : ribIndices) {
         Wings.WingPoint point = SHAPE[idx];
         this.vertex(buffer, matrix, 0.0F, 0.0F, 0.0F, setAlpha(color, Math.max(8, (int)(alpha(color) * 0.75F))));
         this.vertex(buffer, matrix, side * point.x * scale, point.y * scale, 0.0F, this.applyPointAlpha(color, point.alphaMul));
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }

   private int applyPointAlpha(int color, float multiplier) {
      return setAlpha(color, Math.max(0, Math.min(255, (int)(alpha(color) * multiplier))));
   }

   private static int setAlpha(int color, int a) {
      return MathHelper.clamp(a, 0, 255) << 24 | color & 16777215;
   }

   private static int alpha(int color) {
      return color >> 24 & 0xFF;
   }

   private static int red(int color) {
      return color >> 16 & 0xFF;
   }

   private static int green(int color) {
      return color >> 8 & 0xFF;
   }

   private static int blue(int color) {
      return color & 0xFF;
   }

   private static int getColor(int r, int g, int b, int a) {
      return a << 24 | r << 16 | g << 8 | b;
   }

   private void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, int color) {
      buffer.vertex(matrix, x, y, z).color(red(color) / 255.0F, green(color) / 255.0F, blue(color) / 255.0F, alpha(color) / 255.0F);
   }

   private int resolveBaseColor() {
      return ColorAssist.getClientColor();
   }

   private int resolveGlowColor(int base) {
      return ColorAssist.interpolateColor(base, getColor(255, 255, 255, 255), 0.28F);
   }

   private int resolveCoreColor(int base) {
      return ColorAssist.interpolateColor(base, getColor(255, 255, 255, 255), 0.55F);
   }

   private float resolveBodyYaw(PlayerEntity player, float tickDelta) {
      float target = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);
      if (player != this.mc.player) {
         return target;
      } else if (this.selfBodyYawInitialized && player.age >= 2) {
         this.selfBodyYaw = approachDegrees(this.selfBodyYaw, target, 14.0F);
         return this.selfBodyYaw;
      } else {
         this.selfBodyYaw = target;
         this.selfBodyYawInitialized = true;
         return this.selfBodyYaw;
      }
   }

   private static float approachDegrees(float current, float target, float maxDelta) {
      float delta = MathHelper.wrapDegrees(target - current);
      delta = MathHelper.clamp(delta, -maxDelta, maxDelta);
      return current + delta;
   }

   private Wings.WingPose resolvePose(PlayerEntity player, float tickDelta) {
      float pitch = MathHelper.lerp(tickDelta, player.prevPitch, player.getPitch());
      if (player.isGliding()) {
         float flightTicks = player.getGlidingTicks() + tickDelta;
         float flightProgress = MathHelper.clamp(flightTicks * flightTicks / 100.0F, 0.0F, 1.0F);
         float pitchRotation = flightProgress * (-90.0F - pitch);
         return new Wings.WingPose(0.34F, 0.46F, 0.0F, 0.0F, pitchRotation, 0.0F, 0.76F, 0.92F, 0.1F, 0.58F, 0.05F, 0.06F, -5.0F, -2.0F, 0.13F);
      } else if (player.isTouchingWater()) {
         return null;
      } else {
         return player.isSneaking()
            ? new Wings.WingPose(0.0F, 0.0F, 0.96F, 0.1F, 18.0F, 0.0F, 1.0F, 1.0F, 0.18F, 4.5F, 0.06F, 0.02F, -11.0F, -4.0F, 0.12F)
            : new Wings.WingPose(0.0F, 0.0F, 1.38F, 0.1F, 0.0F, 0.0F, 1.0F, 1.0F, 0.18F, 4.5F, 0.06F, 0.02F, -11.0F, -4.0F, 0.12F);
      }
   }

   @Override
   public void deactivate() {
      this.selfBodyYawInitialized = false;
   }

   private static final class WingPoint {
      final float x;
      final float y;
      final float alphaMul;

      WingPoint(float x, float y, float alphaMul) {
         this.x = x;
         this.y = y;
         this.alphaMul = alphaMul;
      }
   }

   private static final class WingPose {
      final float preTranslateY;
      final float preTranslateZ;
      final float anchorY;
      final float anchorZ;
      final float pitchRotation;
      final float rollRotation;
      final float openMultiplier;
      final float scaleMultiplier;
      final float motionSpreadBoost;
      final float flapAmplitude;
      final float sideOffset;
      final float sideYOffset;
      final float sideZOffset;
      final float sideRoll;
      final float sidePitch;
      final float flapSpeed;

      WingPose(
         float preTranslateY,
         float preTranslateZ,
         float anchorY,
         float anchorZ,
         float pitchRotation,
         float rollRotation,
         float openMultiplier,
         float scaleMultiplier,
         float motionSpreadBoost,
         float flapAmplitude,
         float sideOffset,
         float sideZOffset,
         float sideRoll,
         float sidePitch,
         float flapSpeed
      ) {
         this(
            preTranslateY,
            preTranslateZ,
            anchorY,
            anchorZ,
            pitchRotation,
            rollRotation,
            openMultiplier,
            scaleMultiplier,
            motionSpreadBoost,
            flapAmplitude,
            sideOffset,
            0.0F,
            sideZOffset,
            sideRoll,
            sidePitch,
            flapSpeed
         );
      }

      WingPose(
         float preTranslateY,
         float preTranslateZ,
         float anchorY,
         float anchorZ,
         float pitchRotation,
         float rollRotation,
         float openMultiplier,
         float scaleMultiplier,
         float motionSpreadBoost,
         float flapAmplitude,
         float sideOffset,
         float sideYOffset,
         float sideZOffset,
         float sideRoll,
         float sidePitch,
         float flapSpeed
      ) {
         this.preTranslateY = preTranslateY;
         this.preTranslateZ = preTranslateZ;
         this.anchorY = anchorY;
         this.anchorZ = anchorZ;
         this.pitchRotation = pitchRotation;
         this.rollRotation = rollRotation;
         this.openMultiplier = openMultiplier;
         this.scaleMultiplier = scaleMultiplier;
         this.motionSpreadBoost = motionSpreadBoost;
         this.flapAmplitude = flapAmplitude;
         this.sideOffset = sideOffset;
         this.sideYOffset = sideYOffset;
         this.sideZOffset = sideZOffset;
         this.sideRoll = sideRoll;
         this.sidePitch = sidePitch;
         this.flapSpeed = flapSpeed;
      }
   }
}

