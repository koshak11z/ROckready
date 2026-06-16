/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ai.catboost.CatBoostError
 *  ai.catboost.CatBoostModel
 *  ai.catboost.CatBoostPredictions
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 */
package moscow.rockstar.systems.ai;

import ai.catboost.CatBoostError;
import ai.catboost.CatBoostModel;
import ai.catboost.CatBoostPredictions;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class AIPredict
implements IMinecraft {
    private long lastSwingTimeMs;
    private CatBoostModel yawModel;
    private CatBoostModel pitchModel;
    private float prevYaw;
    private float prevPitch;
    private float prevTargetYaw;
    private float prevTargetPitch;
    private float prevDistance;
    private Rotation last = Rotation.ZERO;
    private final EventListener<AttackEvent> onAttackEvent = event -> {
        Entity tgt = event.getEntity();
        if (tgt != null) {
            this.lastSwingTimeMs = System.currentTimeMillis();
        }
    };

    public AIPredict() {
        try {
            this.yawModel = CatBoostModel.loadModel((String)"C:/Rockstar/delta_yaw_model.cbm");
            this.pitchModel = CatBoostModel.loadModel((String)"C:/Rockstar/delta_pitch_model.cbm");
        }
        catch (CatBoostError e) {
            e.printStackTrace();
        }
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    public Rotation predictRotation(Rotation prev, LivingEntity target) {
        long now = System.currentTimeMillis();
        double diffY = AIPredict.mc.player.getY() - target.getY();
        float tdy = this.calcTargetDeltaYaw(prev, (Entity)target);
        float tdp = this.calcTargetDeltaPitch(prev, (Entity)target);
        float dist = AIPredict.mc.player.distanceTo((Entity)target);
        float since = Math.min(500L, now - this.lastSwingTimeMs);
        float[][] features = new float[][]{{tdy, tdp, dist, since, AIPredict.mc.player.fallDistance, (float)diffY, this.prevTargetYaw, this.prevTargetPitch, this.prevYaw, this.prevPitch, this.prevDistance}};
        String[][] catFeatures = new String[features.length][0];
        try {
            CatBoostPredictions predsYaw = this.yawModel.predict((float[][])features, catFeatures);
            CatBoostPredictions predsPitch = this.pitchModel.predict((float[][])features, catFeatures);
            double predictedYaw = predsYaw.get(0, 0);
            double predictedPitch = predsPitch.get(0, 0);
            this.prevTargetYaw = tdy;
            this.prevTargetPitch = tdp;
            this.prevYaw = this.normalizeAngle(this.last.getYaw() - prev.getYaw());
            this.prevPitch = this.normalizeAngle(this.last.getPitch() - prev.getPitch());
            this.prevDistance = dist;
            this.last = new Rotation(prev.getYaw(), prev.getPitch());
            return new Rotation((float)((double)prev.getYaw() + predictedYaw), (float)((double)prev.getPitch() - predictedPitch));
        }
        catch (CatBoostError ex) {
            ex.printStackTrace();
            return prev;
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

    private float calcTargetDeltaYaw(Rotation rotation, Entity t) {
        double dx = t.getX() - AIPredict.mc.player.getX();
        double dz = t.getZ() - AIPredict.mc.player.getZ();
        float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        return this.normalizeAngle(targetYaw - rotation.getYaw());
    }

    private float calcTargetDeltaPitch(Rotation rotation, Entity t) {
        double dx = t.getX() - AIPredict.mc.player.getX();
        double dz = t.getZ() - AIPredict.mc.player.getZ();
        double dy = t.getEyeY() - AIPredict.mc.player.getEyeY();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        return this.normalizeAngle(targetPitch - rotation.getPitch());
    }
}

