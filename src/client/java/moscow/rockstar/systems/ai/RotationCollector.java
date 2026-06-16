/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.entity.Entity
 */
package moscow.rockstar.systems.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;

@ModuleInfo(name="Recorder", category=ModuleCategory.OTHER)
public class RotationCollector
extends BaseModule {
    private static final long LOG_WINDOW_MS = 1500L;
    private final List<HashMap<String, Number>> dataset = new ArrayList<HashMap<String, Number>>();
    private float lastYaw;
    private float lastPitch;
    private float prevYaw;
    private float prevPitch;
    private float prevTargetYaw;
    private float prevTargetPitch;
    private float prevDistance;
    private long lastSwingTimeMs;
    private Entity lastTarget;
    private final EventListener<AttackEvent> onAttackEvent = event -> {
        long now;
        this.lastSwingTimeMs = now = System.currentTimeMillis();
        Entity target = event.getEntity();
        if (target != null) {
            this.lastTarget = target;
            ClientPlayerEntity p = MinecraftClient.getInstance().player;
            if (p != null) {
                this.lastYaw = p.getYaw();
                this.lastPitch = p.getPitch();
            }
        } else {
            this.lastTarget = null;
        }
    };
    private final EventListener<HudRenderEvent> onHudRender = event -> {
        long now = System.currentTimeMillis();
        if (this.lastTarget != null && now - this.lastSwingTimeMs <= 1500L && this.lastTarget.isAlive() && RotationCollector.mc.world.getPlayers().contains(this.lastTarget) && RotationCollector.mc.player.distanceTo(this.lastTarget) < 5.0f) {
            ClientPlayerEntity p = RotationCollector.mc.player;
            float yaw = p.getYaw();
            float diffYaw = this.normalizeAngle(yaw - this.lastYaw);
            float targetYaw = this.calcTargetDeltaYaw(p, this.lastTarget);
            if (Math.abs(diffYaw) < 50.0f || Math.signum(diffYaw) == Math.signum(targetYaw)) {
                event.getContext().drawCenteredText(Fonts.MEDIUM.getFont(8.0f), "Recording", sr.getScaledWidth() / 2.0f, 40.0f, ColorRGBA.WHITE);
            }
        }
    };
    private final EventListener<ClientPlayerTickEvent> onPlayerTick = event -> {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) {
            return;
        }
        long now = System.currentTimeMillis();
        float yaw = p.getYaw();
        float pitch = p.getPitch();
        float diffYaw = this.normalizeAngle(yaw - this.lastYaw);
        float diffPitch = this.normalizeAngle(pitch - this.lastPitch);
        float distance = -999.0f;
        float targetYaw = -999.0f;
        float targetPitch = -999.0f;
        if (this.lastTarget != null && now - this.lastSwingTimeMs <= 1500L && this.lastTarget.isAlive() && RotationCollector.mc.world.getPlayers().contains(this.lastTarget) && RotationCollector.mc.player.distanceTo(this.lastTarget) < 5.0f) {
            distance = p.distanceTo(this.lastTarget);
            targetYaw = this.calcTargetDeltaYaw(p, this.lastTarget);
            targetPitch = this.calcTargetDeltaPitch(p, this.lastTarget);
            float diffY = (float)(RotationCollector.mc.player.getY() - this.lastTarget.getY());
            if (Math.abs(targetYaw) < 30.0f || Math.signum(diffYaw) == Math.signum(targetYaw)) {
                HashMap<String, Number> rec = new HashMap<String, Number>();
                rec.put("deltaYaw", Float.valueOf(diffYaw));
                rec.put("deltaPitch", Float.valueOf(diffPitch));
                rec.put("timeSinceLastHitMs", now - this.lastSwingTimeMs);
                rec.put("distance", Float.valueOf(distance));
                rec.put("fallDistance", Float.valueOf(RotationCollector.mc.player.fallDistance));
                rec.put("diffY", Float.valueOf(diffY));
                rec.put("targetDeltaYaw", Float.valueOf(targetYaw));
                rec.put("targetDeltaPitch", Float.valueOf(targetPitch));
                rec.put("prevTargetYaw", Float.valueOf(this.prevTargetYaw == -999.0f ? targetYaw : this.prevTargetYaw));
                rec.put("prevTargetPitch", Float.valueOf(this.prevTargetPitch == -999.0f ? targetPitch : this.prevTargetPitch));
                rec.put("prevYaw", Float.valueOf(this.prevYaw));
                rec.put("prevPitch", Float.valueOf(this.prevPitch));
                rec.put("prevDistance", Float.valueOf(this.prevDistance == -999.0f ? distance : this.prevDistance));
                this.dataset.add(rec);
            }
        }
        this.prevTargetYaw = targetYaw;
        this.prevTargetPitch = targetPitch;
        this.prevYaw = diffYaw;
        this.prevPitch = diffPitch;
        this.prevDistance = distance;
        if (this.lastTarget != null && now - this.lastSwingTimeMs > 1500L && !this.dataset.isEmpty()) {
            this.dumpToJson();
            this.dataset.clear();
            this.lastTarget = null;
        }
        this.lastYaw = p.getYaw();
        this.lastPitch = p.getPitch();
    };

    private void dumpToJson() {
        Path out = MinecraftClient.getInstance().runDirectory.toPath().resolve("C:/Rockstar/kill_aura_dataset.json");
        try (FileWriter writer = new FileWriter(out.toFile(), true);){
            Gson gson = new GsonBuilder().create();
            writer.write(gson.toJson(this.dataset));
            writer.write("\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private float normalizeAngle(float angle) {
        if ((angle %= 360.0f) > 180.0f) {
            angle -= 360.0f;
        }
        if (angle < -180.0f) {
            angle += 360.0f;
        }
        return angle;
    }

    private float calcTargetDeltaYaw(ClientPlayerEntity p, Entity t) {
        double dx = t.getX() - p.getX();
        double dz = t.getZ() - p.getZ();
        float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        return this.normalizeAngle(targetYaw - p.getYaw());
    }

    private float calcTargetDeltaPitch(ClientPlayerEntity p, Entity t) {
        double dx = t.getX() - p.getX();
        double dz = t.getZ() - p.getZ();
        double dy = t.getEyeY() - p.getEyeY();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        return this.normalizeAngle(targetPitch - p.getPitch());
    }
}
