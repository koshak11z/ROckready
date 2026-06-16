/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.mob.CreeperEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.text.Text
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.Rotation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Creeper Farm", category=ModuleCategory.PLAYER, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0443\u0431\u0438\u0432\u0430\u0435\u0442 \u043a\u0440\u0438\u043f\u0435\u0440\u043e\u0432 \u043d\u0430 \u0444\u0430\u0440\u043c\u0438\u043b\u043a\u0435")
public class CreeperFarm
extends BaseModule {
    private boolean retreating;
    private Vec3d retreatTarget;

    @Override
    public void tick() {
        if (CreeperFarm.mc.player == null || !FabricLoader.getInstance().isModLoaded("baritone")) {
            return;
        }
        LivingEntity creeper = this.findClosestCreeper();
        if (creeper == null) {
            return;
        }
        double distance = CreeperFarm.mc.player.getPos().distanceTo(creeper.getPos());
        if (distance <= 3.3) {
            Rotation rot = this.calculateCreeperRotation(creeper);
            Rockstar.getInstance().getRotationHandler().rotate(rot);
            if ((double)CreeperFarm.mc.player.getAttackCooldownProgress(1.0f) >= 0.9) {
                CreeperFarm.mc.interactionManager.attackEntity((PlayerEntity)CreeperFarm.mc.player, (Entity)creeper);
                CreeperFarm.mc.player.swingHand(Hand.MAIN_HAND);
                Vec3d playerPos = CreeperFarm.mc.player.getPos();
                Vec3d creeperPos = creeper.getPos();
                Vec3d away = playerPos.subtract(creeperPos).normalize();
                this.retreatTarget = playerPos.add(away.multiply(4.0));
                this.retreating = true;
            }
        }
    }

    private Rotation calculateCreeperRotation(LivingEntity target) {
        Vec3d toCreeper = new Vec3d(target.getX(), target.getY() + (double)(CreeperFarm.mc.player.distanceTo((Entity)target) < 2.0f ? 0.5f : target.getEyeHeight(target.getPose())), target.getZ());
        double dx = toCreeper.x;
        double dy = toCreeper.y;
        double dz = toCreeper.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f + MathUtility.random(-2.0, 2.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz))) + MathUtility.random(-1.0, 1.0);
        return new Rotation(yaw, pitch);
    }

    private Rotation calculateRotationToward(Vec3d targetPos) {
        Vec3d playerEyes = new Vec3d(CreeperFarm.mc.player.getX(), CreeperFarm.mc.player.getY() + (double)CreeperFarm.mc.player.getEyeHeight(CreeperFarm.mc.player.getPose()), CreeperFarm.mc.player.getZ());
        Vec3d toPoint = targetPos.add(0.0, (double)CreeperFarm.mc.player.getEyeHeight(CreeperFarm.mc.player.getPose()), 0.0).subtract(playerEyes);
        double dx = toPoint.x;
        double dy = toPoint.y;
        double dz = toPoint.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f + MathUtility.random(-2.0, 2.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz))) + MathUtility.random(-1.0, 1.0);
        return new Rotation(yaw, pitch);
    }

    private LivingEntity findClosestCreeper() {
        LivingEntity closest = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : CreeperFarm.mc.world.getEntities()) {
            double d;
            if (!(e instanceof CreeperEntity) || !((d = CreeperFarm.mc.player.getPos().distanceTo(e.getPos())) <= 50.0) || !(Math.abs(CreeperFarm.mc.player.getY() - e.getY()) < 4.0) || !(d < bestDist)) continue;
            bestDist = d;
            closest = (LivingEntity)e;
        }
        return closest;
    }

    @Override
    public void onEnable() {
        if (CreeperFarm.mc.player == null) {
            return;
        }
        if (FabricLoader.getInstance().isModLoaded("baritone")) {
            CreeperFarm.mc.player.networkHandler.sendChatMessage("#follow entity creeper");
            CreeperFarm.mc.player.networkHandler.sendChatMessage("#allowBreak false");
        } else {
            MessageUtility.info(Text.of((String)("\u0414\u043b\u044f \u0440\u0430\u0431\u043e\u0442\u044b " + this.getName() + " \u043d\u0443\u0436\u0435\u043d \u043c\u043e\u0434 baritone")));
            this.toggle();
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (FabricLoader.getInstance().isModLoaded("baritone")) {
            CreeperFarm.mc.player.networkHandler.sendChatMessage("#stop");
        }
        super.onDisable();
    }
}

