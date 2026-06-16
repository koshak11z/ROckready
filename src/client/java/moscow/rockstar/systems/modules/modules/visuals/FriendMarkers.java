/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.visuals;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.render.CrystalRenderer;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Friend Markers", desc="\u0412\u044b\u0434\u0435\u043b\u044f\u0435\u0442 \u0434\u0440\u0443\u0437\u0435\u0439", category=ModuleCategory.VISUALS)
public class FriendMarkers
extends BaseModule {
    private final ModeSetting setting = new ModeSetting(this, "modules.settings.friends_markers.setting");
    private final ModeSetting.Value heads = new ModeSetting.Value(this.setting, "modules.settings.friends_markers.heads");
    private final ModeSetting.Value sims = new ModeSetting.Value(this.setting, "Sims");
    private final EventListener<Render3DEvent> onRender3D = event -> {
        if (!this.sims.isSelected()) {
            return;
        }
        RenderUtility.setupRender3D(true);
        MatrixStack ms = event.getMatrices();
        Camera camera = FriendMarkers.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        ColorRGBA color = new ColorRGBA(52.0f, 199.0f, 89.0f);
        BufferBuilder builder = CrystalRenderer.createBuffer();
        for (AbstractClientPlayerEntity player : FriendMarkers.mc.world.getPlayers()) {
            if (!Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())) continue;
            ms.push();
            RenderUtility.prepareMatrices(ms, Utils.getInterpolatedPos((Entity)player, event.getTickDelta()));
            float size = 0.1f;
            CrystalRenderer.render(ms, builder, 0.0f, player.getHeight() + 0.4f, 0.0f, size, color.withAlpha(255.0f));
            ms.pop();
        }
        BuiltBuffer built = builder.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)built);
        }
        RenderUtility.endRender3D();
    };

    @Generated
    public ModeSetting.Value getHeads() {
        return this.heads;
    }
}

