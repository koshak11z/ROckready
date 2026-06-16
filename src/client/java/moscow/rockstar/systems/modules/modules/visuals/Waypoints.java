/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.text.Text
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.visuals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Waypoints", category=ModuleCategory.VISUALS, enabledByDefault=true)
public class Waypoints
extends BaseModule {
    private final Map<String, Waypoint> waypoints = new HashMap<String, Waypoint>();
    private final EventListener<KeyPressEvent> onClientPlayerTickEvent = event -> {
        if (event.getKey() == 86 && event.getAction() == 1 && Waypoints.mc.currentScreen == null && Waypoints.mc.player != null && Waypoints.mc.world != null) {
            Vec3d pos;
            Vec3d start = Waypoints.mc.player.getEyePos();
            Vec3d direction = Waypoints.mc.player.getRotationVec(mc.getRenderTickCounter().getTickDelta(true));
            Vec3d end = start.add(direction.multiply(200.0));
            PlayerEntity targetPlayer = null;
            double closestDistance = Double.MAX_VALUE;
            for (PlayerEntity player : Waypoints.mc.world.getPlayers()) {
                double distance;
                Box hitbox;
                Vec3d hit;
                if (player == Waypoints.mc.player || (hit = (Vec3d)(hitbox = player.getBoundingBox().expand(0.3)).raycast(start, end).orElse(null)) == null || !((distance = start.distanceTo(hit)) < closestDistance) || !(distance <= 200.0)) continue;
                closestDistance = distance;
                targetPlayer = player;
            }
            if (targetPlayer != null) {
                UUID playerUUID = targetPlayer.getUuid();
                if (this.waypoints.values().stream().anyMatch(w -> playerUUID.equals(w.playerUUID))) {
                    return;
                }
                String name = targetPlayer.getName().getString();
                pos = targetPlayer.getPos();
                this.add(name, pos.x, pos.y, pos.z, true, playerUUID);
                return;
            }
            HitResult raycastResult = Waypoints.mc.player.raycast(200.0, mc.getRenderTickCounter().getTickDelta(true), false);
            if (raycastResult.getType() == HitResult.Type.BLOCK && raycastResult instanceof BlockHitResult) {
                BlockHitResult blockHit = (BlockHitResult)raycastResult;
                pos = blockHit.getPos();
                String baseName = Localizator.translate("modules.waypoints.base_name");
                Object name = baseName;
                int counter = 1;
                while (this.waypoints.containsKey(name)) {
                    name = baseName + " " + counter++;
                }
                this.add((String)name, pos.x, pos.y, pos.z, true, null);
            }
        }
    };
    private final EventListener<PreHudRenderEvent> onHudRenderEvent = event -> {
        MatrixStack matrices = event.getContext().getMatrices();
        float tickDelta = event.getTickDelta();
        long currentTime = System.currentTimeMillis();
        this.waypoints.entrySet().removeIf(entry -> {
            Waypoint waypoint = (Waypoint)entry.getValue();
            if (waypoint.temp && currentTime - waypoint.creationTime > 5000L) {
                return true;
            }
            if (waypoint.playerUUID != null) {
                PlayerEntity player = Waypoints.mc.world.getPlayerByUuid(waypoint.playerUUID);
                if (player != null) {
                    Vec3d targetPos = Utils.getInterpolatedPos((Entity)player, tickDelta);
                    float alpha = 0.2f * tickDelta;
                    waypoint.pos = waypoint.pos.lerp(targetPos, (double)MathHelper.clamp((float)alpha, (float)0.0f, (float)1.0f));
                } else {
                    MessageUtility.info(Text.of((String)Localizator.translate("modules.waypoints.player_removed", waypoint.name)));
                    return true;
                }
            }
            return false;
        });
        for (Waypoint waypoint : this.waypoints.values()) {
            Vec3d renderPos = waypoint.pos;
            Vec3d renderPosAdjusted = renderPos.add(0.0, 0.5, 0.0);
            Vec2f screenPos = Utils.worldToScreen(renderPosAdjusted);
            if (screenPos == null) continue;
            float distance = (float)Waypoints.mc.player.getPos().distanceTo(renderPos);
            float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
            matrices.push();
            matrices.translate(screenPos.x, screenPos.y, 0.0f);
            matrices.scale(scale, scale, 1.0f);
            String text = waypoint.name + " (" + String.format("%.1f", Float.valueOf(distance)) + "m)";
            int width = (int)Fonts.MEDIUM.getFont(11.0f).width(text);
            int x = -width / 2;
            int iconSize = 32;
            event.getContext().drawRoundedRect((float)(x - 3), 2.0f, (float)(width + 8), Fonts.MEDIUM.getFont(11.0f).height() + 6.0f, BorderRadius.all(3.0f), new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
            event.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), text, x, 5.0f, ColorRGBA.WHITE);
            if (waypoint.playerUUID != null) {
                int iconY = (int)(-22.0f + (Fonts.MEDIUM.getFont(11.0f).height() - (float)iconSize) / 2.0f);
                event.getContext().drawTexture(Rockstar.id("icons/target2.png"), -15.0f, iconY, iconSize, iconSize, ColorRGBA.WHITE);
            }
            matrices.pop();
        }
    };

    private void add(String name, double x, double y, double z, boolean isTemp, UUID playerUUID) {
        Vec3d pos = new Vec3d(x, y, z);
        if (this.waypoints.containsKey(name)) {
            MessageUtility.error(Text.of((String)Localizator.translate("modules.waypoints.exists", name)));
            return;
        }
        this.waypoints.put(name, new Waypoint(name, pos, isTemp, System.currentTimeMillis(), playerUUID));
        ClientSounds.MODULE.play(0.5f);
    }

    private static class Waypoint {
        public String name;
        public Vec3d pos;
        public boolean temp;
        public long creationTime;
        public UUID playerUUID;

        @Generated
        public Waypoint(String name, Vec3d pos, boolean temp, long creationTime, UUID playerUUID) {
            this.name = name;
            this.pos = pos;
            this.temp = temp;
            this.creationTime = creationTime;
            this.playerUUID = playerUUID;
        }
    }
}

