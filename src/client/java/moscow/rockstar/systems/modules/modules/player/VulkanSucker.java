/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.modules.modules.player;

import java.util.Comparator;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;

@ModuleInfo(name="Vulkan Sucker", category=ModuleCategory.PLAYER, desc="Автоматически собирает порох и мембраны на ивенте Вулкан")
public class VulkanSucker
extends BaseModule {
    private final Timer timer = new Timer();
    private ItemEntity currentTarget;
    private float circleAngle = 0.0f;

    @Override
    public void tick() {
        if (VulkanSucker.mc.player == null || VulkanSucker.mc.world == null) {
            return;
        }
        if (!VulkanSucker.mc.options.forwardKey.isPressed()) {
            this.currentTarget = null;
            return;
        }
        List<ItemEntity> items = VulkanSucker.mc.world.getEntitiesByClass(ItemEntity.class, VulkanSucker.mc.player.getBoundingBox().expand(30.0), entity -> {
            Item item = entity.getStack().getItem();
            return item == Items.GUNPOWDER || item == Items.PHANTOM_MEMBRANE;
        });
        if (items.isEmpty()) {
            this.currentTarget = null;
            return;
        }
        items.sort(Comparator.comparingDouble(e -> VulkanSucker.mc.player.distanceTo(e)));
        ItemEntity target = items.getFirst();
        if (this.currentTarget != target) {
            this.currentTarget = target;
            this.timer.reset();
            this.circleAngle = 0.0f;
        }
        if (this.currentTarget != null) {
            if (!this.timer.finished(2000L)) {
                Rotation rotation = this.getPathRotation(this.currentTarget.getPos());
                Rockstar.getInstance().getRotationHandler().rotate(rotation, MoveCorrection.DIRECT, 180.0f, 180.0f, 180.0f, RotationPriority.NORMAL);
                VulkanSucker.mc.player.setYaw(rotation.getYaw());
            } else {
                double x = this.currentTarget.getX();
                double y = this.currentTarget.getY();
                double z = this.currentTarget.getZ();
                this.circleAngle += 5.0f;
                double rad = 2.0;
                double cx = x + Math.cos(Math.toRadians(this.circleAngle)) * rad;
                double cz = z + Math.sin(Math.toRadians(this.circleAngle)) * rad;
                Rotation rotation = RotationMath.getRotationTo(new Vec3d(cx, y, cz));
                Rockstar.getInstance().getRotationHandler().rotate(rotation, MoveCorrection.DIRECT, 180.0f, 180.0f, 180.0f, RotationPriority.NORMAL);
                VulkanSucker.mc.player.setYaw(rotation.getYaw());
            }
        }
    }

    private Rotation getPathRotation(Vec3d target) {
        Rotation directRotation = RotationMath.getRotationTo(target);
        if (this.isClear(directRotation.getYaw(), target)) {
            return directRotation;
        }
        for (int offset = 10; offset <= 90; offset += 10) {
            float yawLeft = directRotation.getYaw() - (float)offset;
            if (this.isClear(yawLeft, null)) {
                return new Rotation(yawLeft, directRotation.getPitch());
            }
            float yawRight = directRotation.getYaw() + (float)offset;
            if (this.isClear(yawRight, null)) {
                return new Rotation(yawRight, directRotation.getPitch());
            }
        }
        return directRotation;
    }

    private boolean isClear(float yaw, Vec3d finalTarget) {
        double rads = Math.toRadians(yaw);
        double distance = 2.0;
        double dx = -Math.sin(rads) * distance;
        double dz = Math.cos(rads) * distance;
        Vec3d start = VulkanSucker.mc.player.getEyePos();
        Vec3d end = start.add(dx, 0.0, dz);
        if (finalTarget != null) {
            double distToTarget = start.distanceTo(finalTarget);
            if (distToTarget < distance) {
                end = finalTarget.add(0.0, 0.5, 0.0);
            }
        }
        BlockHitResult result = VulkanSucker.mc.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)VulkanSucker.mc.player));
        return result.getType() == HitResult.Type.MISS;
    }
}

