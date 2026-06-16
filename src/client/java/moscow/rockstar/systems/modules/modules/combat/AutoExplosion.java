/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Auto Explosion", desc="modules.descriptions.auto_explosion", category=ModuleCategory.COMBAT)
public class AutoExplosion
extends BaseModule {
    private final BindSetting bind = new BindSetting(this, "modules.settings.auto_explosion.bind");
    private final BooleanSetting attackOthers = new BooleanSetting(this, "modules.settings.auto_explosion.attack_others");
    private final BooleanSetting selfSave = new BooleanSetting(this, "modules.settings.auto_explosion.self_save");
    private final SliderSetting delay = new SliderSetting((SettingsContainer)this, "modules.settings.auto_explosion.delay", () -> this.bind.getKey() == -1).min(100.0f).max(1000.0f).step(50.0f).currentValue(500.0f);
    private int lastPlacedCrystalId = -1;
    private final Timer delayTimer = new Timer();
    private boolean pressed;
    private final EventListener<KeyPressEvent> keyPressEvent = event -> {
        if (this.bind.isKey(event.getKey())) {
            this.pressed = event.getAction() == 1;
        }
    };

    @Override
    public void tick() {
        if (this.bind.getKey() != -1 && !this.pressed) {
            return;
        }
        SlotGroup<HotbarSlot> search = SlotGroups.hotbar();
        HotbarSlot slot = search.findItem(Items.END_CRYSTAL);
        BlockPos targetPos = this.findNearbyObsidian();
        if (slot == null || targetPos == null) {
            return;
        }
        int crystalSlot = slot.getIdForServer();
        Vec3d targetVec = new Vec3d((double)targetPos.getX() + 0.5, (double)targetPos.getY(), (double)targetPos.getZ() + 0.5);
        float[] rotations = this.calculateLookAngles(targetVec);
        Rockstar.getInstance().getRotationHandler().rotate(new Rotation(rotations[0], rotations[1]));
        if (this.delayTimer.finished((long)this.delay.getCurrentValue() / 2L)) {
            AutoExplosion.mc.player.getInventory().selectedSlot = crystalSlot - 36;
            this.placeCrystal(targetPos.down());
            if (!this.selfSave.isEnabled() || !this.isAboveCrystal(targetPos)) {
                this.attackNearbyCrystal(targetPos);
            }
            this.delayTimer.reset();
        }
        super.tick();
    }

    private BlockPos findNearbyObsidian() {
        int radius = 4;
        BlockPos playerPos = BlockPos.ofFloored((Position)AutoExplosion.mc.player.getPos());
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockPos placePos;
                    BlockPos checkPos = playerPos.add(x, y, z);
                    if (AutoExplosion.mc.world.getBlockState(checkPos).getBlock() != Blocks.OBSIDIAN || !AutoExplosion.mc.world.getBlockState(placePos = checkPos.up()).isAir()) continue;
                    return placePos;
                }
            }
        }
        return null;
    }

    private void attackNearbyCrystal(BlockPos pos) {
        double range = 6.0;
        Vec3d center = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
        for (Entity entity : AutoExplosion.mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity) || !(entity.getPos().squaredDistanceTo(center) <= range * range) || !this.attackOthers.isEnabled() && entity.getId() != this.lastPlacedCrystalId || this.selfSave.isEnabled() && AutoExplosion.mc.player.getY() > entity.getY()) continue;
            AutoExplosion.mc.interactionManager.attackEntity((PlayerEntity)AutoExplosion.mc.player, entity);
            AutoExplosion.mc.player.swingHand(Hand.MAIN_HAND);
            break;
        }
    }

    private void placeCrystal(BlockPos obsidianBlockPos) {
        Vec3d hitVec = new Vec3d((double)obsidianBlockPos.getX() + 0.5, (double)obsidianBlockPos.getY() + 1.0, (double)obsidianBlockPos.getZ() + 0.5);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, obsidianBlockPos, false);
        AutoExplosion.mc.interactionManager.interactBlock(AutoExplosion.mc.player, Hand.MAIN_HAND, hitResult);
        AutoExplosion.mc.player.swingHand(Hand.MAIN_HAND);
        mc.execute(() -> AutoExplosion.mc.world.getEntities().forEach(entity -> {
            if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(hitVec) < 1.0) {
                this.lastPlacedCrystalId = entity.getId();
            }
        }));
    }

    private float[] calculateLookAngles(Vec3d target) {
        Vec3d eyesPos = new Vec3d(AutoExplosion.mc.player.getX(), AutoExplosion.mc.player.getY() + (double)AutoExplosion.mc.player.getEyeHeight(AutoExplosion.mc.player.getPose()), AutoExplosion.mc.player.getZ());
        double diffX = target.x - eyesPos.x;
        double diffY = target.y - eyesPos.y;
        double diffZ = target.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{AutoExplosion.mc.player.getYaw() + MathHelper.wrapDegrees((float)(yaw - AutoExplosion.mc.player.getYaw())), AutoExplosion.mc.player.getPitch() + MathHelper.wrapDegrees((float)(pitch - AutoExplosion.mc.player.getPitch()))};
    }

    private boolean isAboveCrystal(BlockPos crystalPos) {
        return AutoExplosion.mc.player.getY() > (double)crystalPos.getY() + 1.0;
    }
}

