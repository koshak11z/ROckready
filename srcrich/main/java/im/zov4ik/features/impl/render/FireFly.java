package im.zov4ik.features.impl.render;

import im.zov4ik.utils.client.managers.event.EventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Last;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.display.color.ColorAssist;

public class FireFly extends Module {
   public final SliderSettings count = new SliderSettings("Количество", "Количество светлячков").range(10, 100).setValue(40);
   public final BooleanSetting themeColor = new BooleanSetting("Цвет от темы", "Использовать цвет клиента").setValue(true);
   private static final float SPEED = 0.35F;
   private static final float SPAWN_RADIUS = 35.0F;
   private static final int TRAIL_LENGTH = 70;
   private final List<FireFly.FireFlyEntity> particles = new ArrayList<>();
   private final Random random = new Random();
   private final Last listener = context -> {
      if (this.isState()) {
         this.onRender3D(context.matrixStack(), context.camera(), context.tickCounter().getTickDelta(true));
      } // 1
   };
   private boolean registered = false;

   public FireFly() {
      super("Fire Fly", "Fire Fly", ModuleCategory.RENDER);
      setup(count, themeColor);
   }

   @Override
   public void activate() {
      this.particles.clear();
      if (!this.registered) {
         WorldRenderEvents.LAST.register(this.listener);
         this.registered = true;
      }
   }

   @Override
   public void deactivate() {
      this.particles.clear();
   }

   @EventHandler
   private void onPlayerTick(TickEvent e) {
      if (this.mc.player != null && this.mc.world != null) {
         Vec3d playerPos = this.mc.player.getPos();
         float speedMult = 0.35F;
         float maxSpeed = speedMult * 1.5F;
         this.particles.forEach(p -> p.update(speedMult, maxSpeed, playerPos));
         this.particles.removeIf(p -> p.isDead(playerPos.x, playerPos.y, playerPos.z));
         int targetCount = this.count.getInt();

         while (this.particles.size() > targetCount) {
            this.particles.remove(this.particles.size() - 1);
         }

         while (this.particles.size() < targetCount) {
            this.spawnParticle(playerPos);
         }
      }
   }

   private void spawnParticle(Vec3d playerPos) {
      double distance = this.random.nextDouble() * 30.0 + 5.0;
      double yawRad = Math.toRadians(this.random.nextDouble() * 360.0);
      double xOffset = -Math.sin(yawRad) * distance;
      double zOffset = Math.cos(yawRad) * distance;
      double yOffset = (this.random.nextDouble() - 0.3) * 8.0 + 1.0;
      double velocitySpeed = 0.35F;
      double velocityYaw = Math.toRadians(this.random.nextDouble() * 360.0);
      double velocityPitch = Math.toRadians((this.random.nextDouble() - 0.5) * 60.0);
      double velX = -Math.sin(velocityYaw) * Math.cos(velocityPitch) * velocitySpeed;
      double velY = Math.sin(velocityPitch) * velocitySpeed * 0.5;
      double velZ = Math.cos(velocityYaw) * Math.cos(velocityPitch) * velocitySpeed;
      int[] randomColors = new int[]{-10496, -256, -16711936, -16711681, -38476, -23296, -16728065};
      int randomColor = randomColors[this.random.nextInt(randomColors.length)];
      this.particles
         .add(
            new FireFly.FireFlyEntity(
               playerPos.x + xOffset, playerPos.y + yOffset, playerPos.z + zOffset, velX, velY, velZ, randomColor, 70
            )
         );
   }

   private void onRender3D(MatrixStack stack, Camera camera, float tickDelta) {
      if (this.mc.player != null && this.mc.world != null && !this.particles.isEmpty()) {
         Vec3d cameraPos = camera.getPos();
         boolean useTheme = this.themeColor.isValue();
         int clrTheme = ColorAssist.getClientColor();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, Identifier.of("textures/particles/glow.png"));
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.depthMask(false);
         RenderSystem.blendFuncSeparate(770, 1, 1, 0);
         RenderSystem.enableDepthTest();
         BufferBuilder buffer = null;
         stack.push();
         stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
         Matrix4f globalMatrix = stack.peek().getPositionMatrix();

         for (FireFly.FireFlyEntity particle : this.particles) {
            float lifeAlpha = particle.getLifeAlpha();
            if (!(lifeAlpha <= 0.01F) && particle.trail.size() >= 2) {
               int renderColor = useTheme ? clrTheme : particle.baseRandomColor;
               double px = particle.getInterpolatedX(tickDelta);
               double py = particle.getInterpolatedY(tickDelta);
               double pz = particle.getInterpolatedZ(tickDelta);
               List<Vec3d> points = new ArrayList<>();
               points.add(new Vec3d(px, py, pz));

               for (FireFly.TrailPoint p : particle.trail) {
                  points.add(new Vec3d(p.x, p.y, p.z));
               }

               for (int i = 0; i < points.size() - 1; i++) {
                  Vec3d current = points.get(i);
                  Vec3d next = points.get(i + 1);
                  float t1 = (float)i / (points.size() - 1);
                  float t2 = (float)(i + 1) / (points.size() - 1);
                  float w1 = 0.12F * (1.0F - t1);
                  float w2 = 0.12F * (1.0F - t2);
                  float alpha1 = (1.0F - t1) * (1.0F - t1) * lifeAlpha * 0.6F;
                  float alpha2 = (1.0F - t2) * (1.0F - t2) * lifeAlpha * 0.6F;
                  if (!(alpha1 <= 0.01F) || !(alpha2 <= 0.01F)) {
                     Vec3d dir = current.subtract(next);
                     if (!(dir.lengthSquared() < 1.0E-4)) {
                        Vec3d camToCur = cameraPos.subtract(current);
                        Vec3d cross1 = dir.crossProduct(camToCur);
                        if (!(cross1.lengthSquared() < 1.0E-4)) {
                           Vec3d right1 = cross1.normalize().multiply(w1);
                           Vec3d camToNext = cameraPos.subtract(next);
                           Vec3d cross2 = dir.crossProduct(camToNext);
                           if (!(cross2.lengthSquared() < 1.0E-4)) {
                              Vec3d right2 = cross2.normalize().multiply(w2);
                              int c1 = ColorAssist.setAlpha(renderColor, (int)(alpha1 * 255.0F));
                              int c2 = ColorAssist.setAlpha(renderColor, (int)(alpha2 * 255.0F));
                              if (buffer == null) {
                                 buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                              }

                              buffer.vertex(
                                    globalMatrix,
                                    (float)(current.x + right1.x),
                                    (float)(current.y + right1.y),
                                    (float)(current.z + right1.z)
                                 )
                                 .texture(0.0F, t1)
                                 .color(c1);
                              buffer.vertex(
                                    globalMatrix,
                                    (float)(current.x - right1.x),
                                    (float)(current.y - right1.y),
                                    (float)(current.z - right1.z)
                                 )
                                 .texture(1.0F, t1)
                                 .color(c1);
                              buffer.vertex(
                                    globalMatrix,
                                    (float)(next.x - right2.x),
                                    (float)(next.y - right2.y),
                                    (float)(next.z - right2.z)
                                 )
                                 .texture(1.0F, t2)
                                 .color(c2);
                              buffer.vertex(
                                    globalMatrix,
                                    (float)(next.x + right2.x),
                                    (float)(next.y + right2.y),
                                    (float)(next.z + right2.z)
                                 )
                                 .texture(0.0F, t2)
                                 .color(c2);
                           }
                        }
                     }
                  }
               }
            }
         }

         stack.pop();

         for (FireFly.FireFlyEntity particle : this.particles) {
            float lifeAlpha = particle.getLifeAlpha();
            if (!(lifeAlpha <= 0.01F)) {
               float pulseFloat = particle.getPulseAlpha() / 255.0F;
               float finalAlpha = pulseFloat * lifeAlpha;
               if (!(finalAlpha <= 0.01F)) {
                  int renderColor = useTheme ? clrTheme : particle.baseRandomColor;
                  double px = particle.getInterpolatedX(tickDelta);
                  double py = particle.getInterpolatedY(tickDelta);
                  double pz = particle.getInterpolatedZ(tickDelta);
                  stack.push();
                  stack.translate(px - cameraPos.x, py - cameraPos.y, pz - cameraPos.z);
                  stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                  stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                  Matrix4f localMatrix = stack.peek().getPositionMatrix();
                  if (buffer == null) {
                     buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                  }

                  this.drawQuad(buffer, localMatrix, 0.35F, renderColor, finalAlpha * 0.6F);
                  this.drawQuad(buffer, localMatrix, 0.22F, renderColor, finalAlpha);
                  this.drawQuad(buffer, localMatrix, 0.1F, -1, finalAlpha);
                  stack.pop();
               }
            }
         }

         if (buffer != null) {
            BuiltBuffer builtBuffer = buffer.end();
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.setShaderTexture(0, 0);
      }
   }

   private void drawQuad(BufferBuilder buffer, Matrix4f matrix, float size, int color, float alphaMod) {
      if (!(alphaMod <= 0.01F)) {
         int finalColor = ColorAssist.setAlpha(color, (int)(alphaMod * 255.0F));
         buffer.vertex(matrix, -size, -size, 0.0F).texture(0.0F, 0.0F).color(finalColor);
         buffer.vertex(matrix, -size, size, 0.0F).texture(0.0F, 1.0F).color(finalColor);
         buffer.vertex(matrix, size, size, 0.0F).texture(1.0F, 1.0F).color(finalColor);
         buffer.vertex(matrix, size, -size, 0.0F).texture(1.0F, 0.0F).color(finalColor);
      }
   }

   private static class FireFlyEntity {
      double x;
      double y;
      double z;
      double prevX;
      double prevY;
      double prevZ;
      double velX;
      double velY;
      double velZ;
      final int baseRandomColor;
      long spawnTime;
      final List<FireFly.TrailPoint> trail = new ArrayList<>();
      final int maxTrailLength;
      double targetVelX;
      double targetVelY;
      double targetVelZ;
      long lastDirectionChange;
      final Random random = new Random();

      FireFlyEntity(double x, double y, double z, double velX, double velY, double velZ, int baseRandomColor, int maxTrailLength) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.prevX = x;
         this.prevY = y;
         this.prevZ = z;
         this.velX = velX;
         this.velY = velY;
         this.velZ = velZ;
         this.targetVelX = velX;
         this.targetVelY = velY;
         this.targetVelZ = velZ;
         this.baseRandomColor = baseRandomColor;
         this.spawnTime = System.currentTimeMillis();
         this.lastDirectionChange = System.currentTimeMillis();
         this.maxTrailLength = maxTrailLength;
      }

      void update(float speedMult, float maxSpeed, Vec3d playerPos) {
         this.prevX = this.x;
         this.prevY = this.y;
         this.prevZ = this.z;
         long timeSinceChange = System.currentTimeMillis() - this.lastDirectionChange;
         if (timeSinceChange > 2000 + this.random.nextInt(2000)) {
            double angle = Math.toRadians(this.random.nextDouble() * 360.0);
            double pitch = Math.toRadians((this.random.nextDouble() - 0.5) * 40.0);
            this.targetVelX = -Math.sin(angle) * Math.cos(pitch) * speedMult;
            this.targetVelY = Math.sin(pitch) * speedMult * 0.3;
            this.targetVelZ = Math.cos(angle) * Math.cos(pitch) * speedMult;
            this.lastDirectionChange = System.currentTimeMillis();
         }

         double dx = playerPos.x - this.x;
         double dy = playerPos.y + 1.0 - this.y;
         double dz = playerPos.z - this.z;
         double distToPlayer = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (distToPlayer > 35.0) {
            this.targetVelX += dx / distToPlayer * speedMult * 0.15;
            this.targetVelY += dy / distToPlayer * speedMult * 0.15;
            this.targetVelZ += dz / distToPlayer * speedMult * 0.15;
         }

         double lerpFactor = 0.02;
         this.velX = this.velX + (this.targetVelX - this.velX) * lerpFactor;
         this.velY = this.velY + (this.targetVelY - this.velY) * lerpFactor;
         this.velZ = this.velZ + (this.targetVelZ - this.velZ) * lerpFactor;
         double wobble = 0.03;
         this.velX = this.velX + (this.random.nextDouble() - 0.5) * wobble;
         this.velY = this.velY + (this.random.nextDouble() - 0.5) * wobble;
         this.velZ = this.velZ + (this.random.nextDouble() - 0.5) * wobble;
         this.velX = MathHelper.clamp(this.velX, -maxSpeed, maxSpeed);
         this.velY = MathHelper.clamp(this.velY, -maxSpeed, maxSpeed);
         this.velZ = MathHelper.clamp(this.velZ, -maxSpeed, maxSpeed);
         this.x = this.x + this.velX;
         this.y = this.y + this.velY;
         this.z = this.z + this.velZ;
         this.trail.add(0, new FireFly.TrailPoint(this.x, this.y, this.z));

         while (this.trail.size() > this.maxTrailLength) {
            this.trail.remove(this.trail.size() - 1);
         }
      }

      boolean isDead(double px, double py, double pz) {
         double dx = this.x - px;
         double dy = this.y - py;
         double dz = this.z - pz;
         return dx * dx + dy * dy + dz * dz > 6400.0;
      }

      double getInterpolatedX(float tickDelta) {
         return MathHelper.lerp(tickDelta, this.prevX, this.x);
      }

      double getInterpolatedY(float tickDelta) {
         return MathHelper.lerp(tickDelta, this.prevY, this.y);
      }

      double getInterpolatedZ(float tickDelta) {
         return MathHelper.lerp(tickDelta, this.prevZ, this.z);
      }

      int getPulseAlpha() {
         long age = System.currentTimeMillis() - this.spawnTime;
         double pulse = 0.8 + 0.2 * Math.sin(age / 200.0);
         return (int)(pulse * 255.0);
      }

      float getLifeAlpha() {
         long age = System.currentTimeMillis() - this.spawnTime;
         long fadeInDuration = 1000L;
         return age < fadeInDuration ? (float)age / (float)fadeInDuration : 1.0F;
      }
   }

   private static class TrailPoint {
      double x;
      double y;
      double z;

      TrailPoint(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }
   }
}

