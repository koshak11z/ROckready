/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.entity.projectile.thrown.EnderPearlEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package moscow.rockstar.systems.modules.modules.player;

import java.util.Comparator;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;

@ModuleInfo(name="Target Pearl", category=ModuleCategory.PLAYER, desc="modules.descriptions.target_pearl")
public class TargetPearl
extends BaseModule {
    private final Timer delayTimer = new Timer();
    private BlockPos targetBlock;
    private int lastPearlId;
    private int lastOurPearlId;
    private float rotationYaw;
    private float rotationPitch;
    private int tick;
    private boolean shouldThrowPearl;
    private int thrownPearls = 0;
    private final Timer pearlDelayTimer = new Timer();
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (TargetPearl.mc.player == null || TargetPearl.mc.world == null) {
            return;
        }
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        if (this.tick > 0) {
            rotationHandler.rotate(new Rotation(this.rotationYaw, this.rotationPitch), MoveCorrection.NONE, 180.0f, 180.0f, 180.0f, RotationPriority.OVERRIDE);
            ++this.tick;
        }
        if (TargetPearl.mc.player.getHealth() < 5.0f) {
            return;
        }
        if (!this.delayTimer.finished(1000L)) {
            return;
        }
        for (Entity ent : TargetPearl.mc.world.getEntities()) {
            if (!(ent instanceof EnderPearlEntity)) continue;
            EnderPearlEntity pearl = (EnderPearlEntity)ent;
            if (pearl.getOwner() == TargetPearl.mc.player) {
                this.lastOurPearlId = pearl.getId();
                continue;
            }
            if (pearl.getId() == this.lastPearlId || pearl.getId() == this.lastOurPearlId) continue;
            TargetPearl.mc.world.getPlayers().stream().filter(p -> p != TargetPearl.mc.player).min(Comparator.comparingDouble(p -> p.squaredDistanceTo((Entity)pearl))).ifPresent(player -> {
                this.targetBlock = this.calcTrajectory((Entity)pearl);
                this.lastPearlId = pearl.getId();
            });
        }
        if (this.targetBlock == null) {
            return;
        }
        if (TargetPearl.mc.player.squaredDistanceTo(this.targetBlock.toCenterPos()) < 9.0) {
            return;
        }
        this.rotationPitch = (float)(-Math.toDegrees(this.calcTrajectory(this.targetBlock)));
        this.rotationYaw = (float)Math.toDegrees(Math.atan2((double)((float)this.targetBlock.getZ() + 0.5f) - TargetPearl.mc.player.getZ(), (double)((float)this.targetBlock.getX() + 0.5f) - TargetPearl.mc.player.getX())) - 90.0f;
        BlockPos tracedBP = this.checkTrajectory(this.rotationYaw, this.rotationPitch);
        if (tracedBP == null || this.targetBlock.getSquaredDistance((Vec3i)tracedBP) > 36.0) {
            return;
        }
        this.tick = 1;
        this.shouldThrowPearl = true;
        this.targetBlock = null;
        this.delayTimer.reset();
        this.thrownPearls = 0;
    };

    @Override
    public void tick() {
        if (this.shouldThrowPearl && this.tick >= 3) {
            int pearlsToThrow = 1;
            if (this.thrownPearls < pearlsToThrow && this.pearlDelayTimer.finished(100L)) {
                this.throwPearl();
                ++this.thrownPearls;
                this.pearlDelayTimer.reset();
            }
            if (this.thrownPearls >= pearlsToThrow) {
                this.shouldThrowPearl = false;
                this.tick = 0;
            }
        }
    }

    private void throwPearl() {
        SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
        ItemSlot pearlItemSlot = slotsToSearch.findItem(Items.ENDER_PEARL);
        if (pearlItemSlot != null) {
            int originalSlot = TargetPearl.mc.player.getInventory().selectedSlot;
            int pearlSlot = pearlItemSlot.getIdForServer();
            if (pearlSlot != originalSlot) {
                TargetPearl.mc.interactionManager.clickSlot(0, pearlSlot, 0, SlotActionType.PICKUP, (PlayerEntity)TargetPearl.mc.player);
                TargetPearl.mc.interactionManager.clickSlot(0, 36 + originalSlot, 0, SlotActionType.PICKUP, (PlayerEntity)TargetPearl.mc.player);
            }
            mc.getNetworkHandler().sendPacket((Packet)new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, (float)((int)(this.rotationYaw * 256.0f / 360.0f)), (float)((int)(this.rotationPitch * 256.0f / 360.0f))));
            mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
            if (pearlSlot != originalSlot) {
                TargetPearl.mc.interactionManager.clickSlot(0, 36 + originalSlot, 0, SlotActionType.PICKUP, (PlayerEntity)TargetPearl.mc.player);
                TargetPearl.mc.interactionManager.clickSlot(0, pearlSlot, 0, SlotActionType.PICKUP, (PlayerEntity)TargetPearl.mc.player);
                if (!TargetPearl.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    TargetPearl.mc.interactionManager.clickSlot(0, 36 + originalSlot, 0, SlotActionType.PICKUP, (PlayerEntity)TargetPearl.mc.player);
                }
            }
        }
    }

    private float calcTrajectory(BlockPos bp) {
        double a = Math.hypot((double)((float)bp.getX() + 0.5f) - TargetPearl.mc.player.getX(), (double)((float)bp.getZ() + 0.5f) - TargetPearl.mc.player.getZ());
        double y = 6.125 * ((double)((float)bp.getY() + 1.0f) - (TargetPearl.mc.player.getY() + (double)TargetPearl.mc.player.getEyeHeight(TargetPearl.mc.player.getPose())));
        y = (double)0.05f * ((double)0.05f * (a * a) + y);
        y = Math.sqrt(9.37890625 - y);
        double d = 3.0625 - y;
        y = Math.atan2(d * d + y, (double)0.05f * a);
        d = Math.atan2(d, (double)0.05f * a);
        return (float)Math.min(y, d);
    }

    private BlockPos calcTrajectory(Entity e) {
        return this.traceTrajectory(e.getX(), e.getY(), e.getZ(), e.getVelocity().x, e.getVelocity().y, e.getVelocity().z);
    }

    private BlockPos checkTrajectory(float yaw, float pitch) {
        if (Float.isNaN(pitch)) {
            return null;
        }
        float yawRad = yaw * ((float)Math.PI / 180);
        float pitchRad = pitch * ((float)Math.PI / 180);
        double x = TargetPearl.mc.player.getX() - Math.cos(yawRad) * (double)0.16f;
        double y = TargetPearl.mc.player.getY() + (double)TargetPearl.mc.player.getEyeHeight(TargetPearl.mc.player.getPose()) - 0.1000000014901161;
        double z = TargetPearl.mc.player.getZ() - Math.sin(yawRad) * (double)0.16f;
        double motionX = -Math.sin(yawRad) * Math.cos(pitchRad) * (double)0.4f;
        double motionY = -Math.sin(pitchRad) * (double)0.4f;
        double motionZ = Math.cos(yawRad) * Math.cos(pitchRad) * (double)0.4f;
        float distance = (float)Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX /= (double)distance;
        motionY /= (double)distance;
        motionZ /= (double)distance;
        motionX *= 1.5;
        motionY *= 1.5;
        motionZ *= 1.5;
        if (!TargetPearl.mc.player.isOnGround()) {
            motionY += TargetPearl.mc.player.getVelocity().y;
        }
        return this.traceTrajectory(x, y, z, motionX, motionY, motionZ);
    }

    private BlockPos traceTrajectory(double x, double y, double z, double mx, double my, double mz) {
        for (int i = 0; i < 300; ++i) {
            Vec3d lastPos = new Vec3d(x, y, z);
            mx *= 0.99;
            my *= 0.99;
            Vec3d pos = new Vec3d(x += mx, y += (my -= (double)0.03f), z += (mz *= 0.99));
            BlockHitResult bhr = TargetPearl.mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)TargetPearl.mc.player));
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK) {
                return bhr.getBlockPos();
            }
            for (Entity ent : TargetPearl.mc.world.getEntities()) {
                if (ent instanceof ArrowEntity || ent == TargetPearl.mc.player || ent instanceof EnderPearlEntity || !ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.2))) continue;
                return null;
            }
            if (y <= -65.0) break;
        }
        return null;
    }
}

