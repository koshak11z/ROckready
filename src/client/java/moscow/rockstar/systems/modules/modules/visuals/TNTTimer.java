/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.TntEntity
 *  net.minecraft.item.Items
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="TNT Timer", category=ModuleCategory.VISUALS, desc="modules.descriptions.tnt_timer")
public class TNTTimer
extends BaseModule {
    private final EventListener<PreHudRenderEvent> onHudRenderEvent = event -> {
        MatrixStack matrices = event.getContext().getMatrices();
        RectBatching rect = new RectBatching(VertexFormats.POSITION_COLOR, event.getContext().getMatrices());
        for (Entity entity : TNTTimer.mc.world.getEntities()) {
            if (!(entity instanceof TntEntity)) continue;
            TntEntity tnt = (TntEntity)entity;
            this.renderBack((PreHudRenderEvent)event, matrices, tnt);
        }
        ((Batching)rect).draw();
        FontBatching batching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);
        for (Entity entity : TNTTimer.mc.world.getEntities()) {
            if (!(entity instanceof TntEntity)) continue;
            TntEntity tnt = (TntEntity)entity;
            this.renderText((PreHudRenderEvent)event, matrices, tnt);
        }
        batching.draw();
    };

    private void renderBack(PreHudRenderEvent event, MatrixStack matrices, TntEntity entity) {
        int fuse = entity.getFuse();
        float seconds = (float)fuse / 20.0f;
        String text = Localizator.translate("modules.tnt_timer.format", Float.valueOf(seconds));
        Vec3d renderPos = entity.getLerpedPos(event.getTickDelta()).add(0.0, 0.5, 0.0);
        Vec2f screenPos = Utils.worldToScreen(renderPos);
        if (screenPos != null) {
            float distance = (float)TNTTimer.mc.player.getPos().distanceTo(renderPos);
            float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
            matrices.push();
            matrices.translate(screenPos.x - 6.0f, screenPos.y, 0.0f);
            matrices.scale(scale, scale, 1.0f);
            int width = (int)Fonts.MEDIUM.getFont(11.0f).width(text);
            int x = -width / 2;
            event.getContext().drawRect(x - 3, 1.0f, width + 26, Fonts.MEDIUM.getFont(11.0f).height() + 8.0f, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
            matrices.pop();
        }
    }

    private void renderText(PreHudRenderEvent event, MatrixStack matrices, TntEntity entity) {
        int fuse = entity.getFuse();
        float seconds = (float)fuse / 20.0f;
        String text = Localizator.translate("modules.tnt_timer.format", Float.valueOf(seconds));
        Vec3d renderPos = entity.getLerpedPos(event.getTickDelta()).add(0.0, 0.5, 0.0);
        Vec2f screenPos = Utils.worldToScreen(renderPos);
        if (screenPos != null) {
            float distance = (float)TNTTimer.mc.player.getPos().distanceTo(renderPos);
            float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
            matrices.push();
            matrices.translate(screenPos.x - 6.0f, screenPos.y, 0.0f);
            matrices.scale(scale, scale, 1.0f);
            int width = (int)Fonts.MEDIUM.getFont(11.0f).width(text);
            int x = -width / 2;
            event.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), text, x + 16, 5.0f, ColorRGBA.WHITE);
            event.getContext().drawItem(Items.TNT, (float)x, 3.0f, 0.75f);
            matrices.pop();
        }
    }
}

