/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.Items
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.visuals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.SoundEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Sound ESP", category=ModuleCategory.VISUALS, enabledByDefault=true, desc="\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0433\u0434\u0435 \u0431\u044b\u043b \u0432\u043e\u0441\u043f\u0440\u043e\u0438\u0437\u0432\u0435\u0434\u0435\u043d \u0437\u0432\u0443\u043a")
public class SoundESP
extends BaseModule {
    private final SelectSetting select = new SelectSetting(this, "\u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0442\u044c");
    private final SelectSetting.Value trident = new SelectSetting.Value(this.select, "\u0422\u0440\u0435\u0437\u0443\u0431\u0435\u0446").select();
    private final SelectSetting.Value tnt = new SelectSetting.Value(this.select, "\u0414\u0438\u043d\u0430\u043c\u0438\u0442");
    private final SelectSetting.Value fireworks = new SelectSetting.Value(this.select, "\u0424\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u0438");
    private final Map<String, SoundMarker> markers = new HashMap<String, SoundMarker>();
    private final Set<String> TARGET_SOUNDS = new HashSet<String>(Arrays.asList("minecraft:entity.generic.explode", "minecraft:item.trident.throw", "minecraft:item.trident.return", "minecraft:entity.firework_rocket.launch"));
    private final EventListener<SoundEvent> onSoundInstanceEvent = event -> {
        SoundInstance sound = event.getSound();
        Identifier soundId = sound.getId();
        String soundIdStr = soundId.toString();
        if (this.TARGET_SOUNDS.contains(soundIdStr)) {
            boolean add = false;
            if (soundIdStr.contains("generic.explode") && this.tnt.isSelected()) {
                add = true;
            } else if ((soundIdStr.contains("trident.throw") || soundIdStr.contains("trident.return")) && this.trident.isSelected()) {
                add = true;
            } else if (soundIdStr.contains("firework_rocket.launch") && this.fireworks.isSelected()) {
                add = true;
            }
            if (add && SoundESP.mc.player != null && SoundESP.mc.world != null) {
                String displayName = this.simplifySoundName(soundIdStr);
                long creationTime = System.currentTimeMillis();
                String key = displayName + "_" + creationTime;
                Vec3d pos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
                this.add(key, displayName, pos.x, pos.y, pos.z);
            }
        }
    };
    private final EventListener<PreHudRenderEvent> onHudRenderEvent = event -> {
        MatrixStack matrices = event.getContext().getMatrices();
        long currentTime = System.currentTimeMillis();
        this.markers.entrySet().removeIf(entry -> {
            SoundMarker marker = (SoundMarker)entry.getValue();
            return currentTime - marker.creationTime > 5000L;
        });
        RectBatching rect = new RectBatching(VertexFormats.POSITION_COLOR, event.getContext().getMatrices());
        for (SoundMarker marker : this.markers.values()) {
            float distance = (float)SoundESP.mc.player.getPos().distanceTo(marker.pos);
            String text = marker.name + " (" + String.format("%.1f", Float.valueOf(distance)) + "m)";
            this.renderBack((PreHudRenderEvent)event, matrices, text, marker);
        }
        ((Batching)rect).draw();
        FontBatching batching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);
        for (SoundMarker marker : this.markers.values()) {
            float distance = (float)SoundESP.mc.player.getPos().distanceTo(marker.pos);
            String text = marker.name + " (" + String.format("%.1f", Float.valueOf(distance)) + "m)";
            this.renderText((PreHudRenderEvent)event, matrices, text, marker);
        }
        batching.draw();
    };

    private void renderText(PreHudRenderEvent event, MatrixStack matrices, String displayText, SoundMarker marker) {
        Vec3d renderPos = marker.pos;
        Vec3d renderPosAdjusted = renderPos.add(0.0, 0.5, 0.0);
        Vec2f screenPos = Utils.worldToScreen(renderPosAdjusted);
        if (screenPos != null) {
            float distance = (float)SoundESP.mc.player.getPos().distanceTo(renderPos);
            float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
            String text = marker.name + " (" + String.format("%.1f", Float.valueOf(distance)) + "m)";
            int width = (int)Fonts.MEDIUM.getFont(11.0f).width(text);
            int x = -width / 2;
            matrices.push();
            matrices.translate(screenPos.x, screenPos.y, 0.0f);
            matrices.scale(scale, scale, 1.0f);
            event.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), text, x + 16, 5.0f, ColorRGBA.WHITE);
            if (marker.name.toLowerCase().contains("\u0432\u0437\u0440\u044b\u0432")) {
                event.getContext().drawItem(Items.TNT, (float)x, 3.0f, 0.75f);
            } else if (marker.name.toLowerCase().contains("\u0442\u0440\u0435\u0437\u0443\u0431\u0435\u0446")) {
                event.getContext().drawItem(Items.TRIDENT, (float)x, 3.0f, 0.75f);
            } else if (marker.name.toLowerCase().contains("\u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a")) {
                event.getContext().drawItem(Items.FIREWORK_ROCKET, (float)x, 3.0f, 0.75f);
            }
            matrices.pop();
        }
    }

    private void renderBack(PreHudRenderEvent event, MatrixStack matrices, String displayText, SoundMarker marker) {
        Vec3d renderPos = marker.pos;
        Vec3d renderPosAdjusted = renderPos.add(0.0, 0.5, 0.0);
        Vec2f screenPos = Utils.worldToScreen(renderPosAdjusted);
        if (screenPos != null) {
            float distance = (float)SoundESP.mc.player.getPos().distanceTo(renderPos);
            float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
            matrices.push();
            matrices.translate(screenPos.x, screenPos.y, 0.0f);
            matrices.scale(scale, scale, 1.0f);
            int textWidth = (int)Fonts.MEDIUM.getFont(11.0f).width(displayText);
            int x = -textWidth / 2;
            float y = 0.0f;
            event.getContext().drawRect(x - 3, y, textWidth + 26, Fonts.MEDIUM.getFont(11.0f).height() + 8.0f, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
            matrices.pop();
        }
    }

    private void add(String key, String displayName, double x, double y, double z) {
        Vec3d pos = new Vec3d(x, y, z);
        this.markers.put(key, new SoundMarker(displayName, pos, System.currentTimeMillis()));
    }

    private String simplifySoundName(String soundId) {
        if (soundId.contains("generic.explode")) {
            return "\u0412\u0437\u0440\u044b\u0432";
        }
        if (soundId.contains("trident.throw")) {
            return "\u0422\u0440\u0435\u0437\u0443\u0431\u0435\u0446 \u0431\u0440\u043e\u0448\u0435\u043d";
        }
        if (soundId.contains("trident.return")) {
            return "\u0422\u0440\u0435\u0437\u0443\u0431\u0435\u0446";
        }
        if (soundId.contains("firework_rocket.launch")) {
            return "\u0424\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a";
        }
        return soundId.replace("minecraft:", "");
    }

    record SoundMarker(String name, Vec3d pos, long creationTime) {
    }
}
