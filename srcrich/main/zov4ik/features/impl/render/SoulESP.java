package im.zov4ik.features.impl.render;

import im.zov4ik.utils.client.managers.event.EventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.render.WorldRenderEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.utils.display.color.ColorAssist;

public class SoulESP extends Module {
   private static final float DURATION = 3.0F;
   private static final float HEIGHT = 3.5F;
   private final List<SoulESP.Ghost> ghosts = new ArrayList<>();

   public SoulESP() {
      super("SoulESP", "Soul ESP", ModuleCategory.RENDER);
   }

   @EventHandler
   public void onPacket(PacketEvent e) {
      if (this.mc.player != null && this.mc.world != null) {
         if (e.getPacket() instanceof EntityStatusS2CPacket packet) {
            byte status = packet.getStatus();
            if ((status == 3 || status == 35) && packet.getEntity(this.mc.world) instanceof PlayerEntity player) {
               if (player == this.mc.player) {
                  return;
               }

               this.ghosts
                  .add(new SoulESP.Ghost(player.getPos(), player.getBodyYaw(), player.isSneaking(), player.age, System.currentTimeMillis()));
            }
         }
      } // 1
   }

   @EventHandler
   public void onRender(WorldRenderEvent e) {
      if (this.mc.player != null && this.mc.world != null && !this.ghosts.isEmpty()) {
         long now = System.currentTimeMillis();
         float dur = 3000.0F;
         this.ghosts.removeIf(gx -> (float)(now - gx.time) >= dur);
         Vec3d cam = this.mc.gameRenderer.getCamera().getPos();
         MatrixStack m = e.getStack();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

         for (SoulESP.Ghost g : this.ghosts) {
            float t = (float)(now - g.time) / dur;
            if (!(t >= 1.0F)) {
               float alpha = (1.0F - t) * 0.6F;
               float rise = 3.5F * this.ease(t);
               int colorFirst = ColorAssist.getClientColor();
               int colorSecond = ColorAssist.getClientColor();
               int c = ColorAssist.interpolateColor(colorFirst, colorSecond, t);
               float r = ColorAssist.getRed(c) / 255.0F;
               float gr = ColorAssist.getGreen(c) / 255.0F;
               float b = ColorAssist.getBlue(c) / 255.0F;
               m.push();
               m.translate(g.pos.x - cam.x, g.pos.y - cam.y + rise, g.pos.z - cam.z);
               m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - g.yaw));
               m.scale(-1.0F, -1.0F, 1.0F);
               m.translate(0.0, -1.5, 0.0);
               if (g.sneak) {
                  m.translate(0.0, 0.2, 0.0);
                  m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.0F));
               }

               Matrix4f mat = m.peek().getPositionMatrix();
               BufferBuilder buf = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
               float u = 0.0625F;
               float swing = MathHelper.sin(g.phase * 0.6662F) * 0.6F;
               this.box(buf, mat, -4.0F * u, 0.0F, -2.0F * u, 8.0F * u, 12.0F * u, 4.0F * u, r, gr, b, alpha);
               this.box(buf, mat, -4.0F * u, -8.0F * u, -4.0F * u, 8.0F * u, 8.0F * u, 8.0F * u, r, gr, b, alpha);
               m.push();
               m.translate(-6.0F * u, 2.0F * u, 0.0F);
               m.multiply(RotationAxis.POSITIVE_X.rotation(-swing));
               m.translate(6.0F * u, -2.0F * u, 0.0F);
               this.box(buf, m.peek().getPositionMatrix(), -8.0F * u, -2.0F * u, -2.0F * u, 4.0F * u, 12.0F * u, 4.0F * u, r, gr, b, alpha);
               m.pop();
               m.push();
               m.translate(6.0F * u, 2.0F * u, 0.0F);
               m.multiply(RotationAxis.POSITIVE_X.rotation(swing));
               m.translate(-6.0F * u, -2.0F * u, 0.0F);
               this.box(buf, m.peek().getPositionMatrix(), 4.0F * u, -2.0F * u, -2.0F * u, 4.0F * u, 12.0F * u, 4.0F * u, r, gr, b, alpha);
               m.pop();
               m.push();
               m.translate(-2.0F * u, 12.0F * u, 0.0F);
               m.multiply(RotationAxis.POSITIVE_X.rotation(swing));
               m.translate(2.0F * u, -12.0F * u, 0.0F);
               this.box(buf, m.peek().getPositionMatrix(), -4.0F * u, 12.0F * u, -2.0F * u, 4.0F * u, 12.0F * u, 4.0F * u, r, gr, b, alpha);
               m.pop();
               m.push();
               m.translate(2.0F * u, 12.0F * u, 0.0F);
               m.multiply(RotationAxis.POSITIVE_X.rotation(-swing));
               m.translate(-2.0F * u, -12.0F * u, 0.0F);
               this.box(buf, m.peek().getPositionMatrix(), 0.0F, 12.0F * u, -2.0F * u, 4.0F * u, 12.0F * u, 4.0F * u, r, gr, b, alpha);
               m.pop();
               BufferRenderer.drawWithGlobalProgram(buf.end());
               m.pop();
            }
         }

         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
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

   private float ease(float t) {
      return 1.0F - (float)Math.pow(1.0F - MathHelper.clamp(t, 0.0F, 1.0F), 3.0);
   }

   @Override
   public void deactivate() {
      this.ghosts.clear();
   }

   private static class Ghost {
      Vec3d pos;
      float yaw;
      boolean sneak;
      float phase;
      long time;

      Ghost(Vec3d pos, float yaw, boolean sneak, float phase, long time) {
         this.pos = pos;
         this.yaw = yaw;
         this.sneak = sneak;
         this.phase = phase;
         this.time = time;
      }
   }
}

