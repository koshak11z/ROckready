/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.mixins.BacktrackableEntity;
import moscow.rockstar.utility.render.Draw3DUtility;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Back Track", desc="\u0417\u0430\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442 \u0445\u0438\u0442\u0431\u043e\u043a\u0441", category=ModuleCategory.COMBAT)
public class BackTrack
extends BaseModule {
    private final BooleanSetting visual = new BooleanSetting(this, "\u0412\u0438\u0437\u0443\u0430\u043b\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u0442\u044c");
    private final SliderSetting delay = new SliderSetting(this, "\u0432\u0440\u0435\u043c\u044f").min(50.0f).max(1000.0f).step(50.0f).currentValue(100.0f);
    private final EventListener<ClientPlayerTickEvent> updateEvent = event -> {
        for (PlayerEntity player : BackTrack.mc.world.getPlayers()) {
            BacktrackableEntity backtrackableEntity;
            if (player == BackTrack.mc.player || Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString()) || !(player instanceof BacktrackableEntity) || (backtrackableEntity = (BacktrackableEntity)player).rockstar2_0$getBackTracks().size() <= 2) continue;
            backtrackableEntity.rockstar2_0$getBackTracks().removeFirst();
        }
    };
    private final EventListener<Render3DEvent> event3d = e -> {
        if (!this.visual.isEnabled()) {
            return;
        }
        MatrixStack ms = e.getMatrices();
        Vec3d cameraPos = BackTrack.mc.gameRenderer.getCamera().getPos();
        for (PlayerEntity player : BackTrack.mc.world.getPlayers()) {
            BacktrackableEntity backtrackable;
            List<Position> backTracks;
            if (player == BackTrack.mc.player || Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString()) || !(player instanceof BacktrackableEntity) || (backTracks = (backtrackable = (BacktrackableEntity)player).rockstar2_0$getBackTracks()).isEmpty()) continue;
            long now = System.currentTimeMillis();
            backTracks.removeIf(pos -> (float)(now - pos.time()) > this.delay.getCurrentValue());
            Position last = backTracks.getLast();
            Vec3d lastPos = last.pos();
            BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            ms.push();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Draw3DUtility.renderOutlinedBox(ms, buffer, player.getBoundingBox().offset(lastPos.subtract(player.getPos())).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z), ColorRGBA.WHITE.withAlpha(180.0f));
            BuiltBuffer built = buffer.endNullable();
            if (built != null) {
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)built);
            }
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            ms.pop();
        }
    };

    public record Position(Vec3d pos, long time) {
    }
}

