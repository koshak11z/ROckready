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
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@ModuleInfo(name="Object Info", category=ModuleCategory.PLAYER, desc="\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0438\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044e \u043e \u0442\u0440\u0430\u043f\u043a\u0430\u0445 \u0438 \u043f\u043b\u0430\u0441\u0442\u0430\u0445 \u0432 \u043c\u0438\u0440\u0435")
public class ObjectInfo
extends BaseModule {
    private final Map<BlockPos, Info> infos = new HashMap<BlockPos, Info>();
    private final Timer timer = new Timer();
    private final EventListener<ReceivePacketEvent> onSoundInstanceEvent = event -> {
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof PlaySoundS2CPacket) {
            BlockPos pos;
            PlaySoundS2CPacket sound = (PlaySoundS2CPacket)patt0$temp;
            String soundName = sound.getSound().getIdAsString();
            if (soundName.contains("minecraft:block.piston.extend") || soundName.contains("minecraft:block.piston.contract")) {
                pos = new BlockPos((int)sound.getX(), (int)sound.getY(), (int)sound.getZ());
                if ((sound.getVolume() == 0.5f || sound.getVolume() == 0.7f) && sound.getPitch() == 0.5f) {
                    this.infos.put(pos, new Info(pos.up().add(0, 0, 0), ObjType.TRAP));
                }
                this.timer.reset();
            }
            if (soundName.contains("minecraft:block.anvil.place")) {
                pos = new BlockPos((int)sound.getX(), (int)sound.getY(), (int)sound.getZ());
                if (!(sound.getVolume() != 0.5f && sound.getVolume() != 0.7f || sound.getPitch() != 1.1f && sound.getPitch() != 0.5f)) {
                    this.infos.put(pos, new Info(pos.up().add(0, 0, 0), ObjType.PLAST));
                }
                this.timer.reset();
            }
            if (soundName.contains("entity.evoker_fangs.attack")) {
                pos = new BlockPos((int)sound.getX(), (int)sound.getY(), (int)sound.getZ());
                if (sound.getVolume() != 0.5f && sound.getVolume() != 0.7f || sound.getPitch() == 0.85f || sound.getPitch() == 1.0f) {
                    // empty if block
                }
                this.timer.reset();
            }
        }
    };
    private final EventListener<PreHudRenderEvent> onRender2D = event -> {
        BlockPos toRemove = null;
        for (Map.Entry<BlockPos, Info> entry : this.infos.entrySet()) {
            Info info = entry.getValue();
            info.draw((PreHudRenderEvent)event);
            if (!info.start.finished(info.getType().getTime())) continue;
            toRemove = entry.getKey();
        }
        if (toRemove != null) {
            this.infos.remove(toRemove);
        }
    };
    private final EventListener<Render3DEvent> onRender3D = event -> {
        BlockPos toRemove = null;
        for (Map.Entry<BlockPos, Info> entry : this.infos.entrySet()) {
            Info info = entry.getValue();
            info.draw3D((Render3DEvent)event);
            if (!info.start.finished(info.getType().getTime())) continue;
            toRemove = entry.getKey();
        }
        if (toRemove != null) {
            this.infos.remove(toRemove);
        }
    };

    static class Info {
        final BlockPos pos;
        final ObjType type;
        Timer start = new Timer();

        void draw(PreHudRenderEvent e) {
            int remained = (int)((float)(this.type.getTime() - this.start.getElapsedTime()) / 1000.0f);
            MatrixStack matrices = e.getContext().getMatrices();
            BlockPos renderPos = this.pos;
            Vec3d renderPosAdjusted = renderPos.add(0, 1, 0).toCenterPos();
            Vec2f screenPos = Utils.worldToScreen(renderPosAdjusted);
            if (screenPos != null) {
                float distance = (float)IMinecraft.mc.player.getPos().distanceTo(Vec3d.of((Vec3i)renderPos));
                float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
                matrices.push();
                matrices.translate(screenPos.x, screenPos.y, 0.0f);
                matrices.scale(scale, scale, 1.0f);
                String text = this.type.getName() + " (" + remained + " sec)";
                int width = (int)Fonts.MEDIUM.getFont(11.0f).width(text);
                int x = -width / 2 - 9;
                e.getContext().drawRoundedRect((float)(x - 3), 2.0f, (float)(width + 24), Fonts.MEDIUM.getFont(11.0f).height() + 9.0f, BorderRadius.top(3.0f, 3.0f), new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
                e.getContext().drawRoundedRect((float)(x - 3), Fonts.MEDIUM.getFont(11.0f).height() + 9.0f, (float)(width + 24) * (1.0f - (float)this.start.getElapsedTime() / (float)this.type.getTime()), 2.0f, BorderRadius.bottom(0.1f, 0.1f), new ColorRGBA(255.0f, 0.0f, 0.0f));
                e.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), text, x + 14, 5.0f, ColorRGBA.WHITE);
                e.getContext().drawItem(this.type.getItem(), (float)x, 3.0f, 0.75f);
                matrices.pop();
            }
        }

        void draw3D(Render3DEvent e) {
            if (IMinecraft.mc.world == null || IMinecraft.mc.player == null || this.type == ObjType.PLAST) {
                return;
            }
            MatrixStack matrices = e.getMatrices();
            Camera camera = IMinecraft.mc.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();
            matrices.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
            RenderSystem.lineWidth((float)10.0f);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            int radius = 2;
            ColorRGBA color = ColorRGBA.RED.withAlpha(110.0f);
            switch (this.type.ordinal()) {
                case 1: 
                case 2: {
                    radius = 4;
                    color = ColorRGBA.YELLOW.withAlpha(150.0f);
                    break;
                }
                case 0: {
                    color = ColorRGBA.RED.withAlpha(110.0f);
                }
            }
            BlockPos minPos = this.pos.add(-radius, -radius, -radius);
            BlockPos maxPos = this.pos.add(radius, radius, radius);
            Draw3DUtility.renderOutlinedBox(e.getMatrices(), buffer, new Box((double)minPos.getX(), (double)minPos.getY(), (double)minPos.getZ(), (double)(maxPos.getX() + 1), (double)(maxPos.getY() + 1), (double)(maxPos.getZ() + 1)), color);
            BuiltBuffer builtBuffer = buffer.endNullable();
            if (builtBuffer != null) {
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
            }
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }

        @Generated
        public BlockPos getPos() {
            return this.pos;
        }

        @Generated
        public ObjType getType() {
            return this.type;
        }

        @Generated
        public Timer getStart() {
            return this.start;
        }

        @Generated
        public Info(BlockPos pos, ObjType type) {
            this.pos = pos;
            this.type = type;
        }
    }

    static enum ObjType {
        TRAP("\u0422\u0440\u0430\u043f\u043a\u0430", Items.NETHERITE_SCRAP, 15000L),
        DRAGON_FT("\u0414\u0440\u0430\u043a\u043e\u043d\u043a\u0430", Items.NETHERITE_SCRAP, 30000L),
        DRAGON_ST("\u0414\u0440\u0430\u043a\u043e\u043d\u043a\u0430", Items.NETHERITE_SCRAP, 60000L),
        PLAST("\u041f\u043b\u0430\u0441\u0442", Items.DRIED_KELP, 20000L);

        final String name;
        final Item item;
        final long time;

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public Item getItem() {
            return this.item;
        }

        @Generated
        public long getTime() {
            return this.time;
        }

        @Generated
        private ObjType(String name, Item item, long time) {
            this.name = name;
            this.item = item;
            this.time = time;
        }
    }
}

