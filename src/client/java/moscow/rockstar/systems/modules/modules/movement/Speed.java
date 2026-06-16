/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.rotations.Rotation;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Speed", category=ModuleCategory.MOVEMENT, desc="modules.descriptions.speed")
public class Speed
extends BaseModule {
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.speed.mode");
    private final ModeSetting.Value vanilla = new ModeSetting.Value(this.mode, "modules.settings.speed.vanilla");
    private final ModeSetting.Value elytra = new ModeSetting.Value(this.mode, "Spooky Elytra");

    @Override
    public void tick() {
        SlotGroup<HotbarSlot> slotsToSearch;
        HotbarSlot slot;
        if (this.vanilla.isSelected()) {
            BlockPos pos = Speed.mc.player.getBlockPos().add(0, -1, 0);
            Speed.mc.options.sneakKey.setPressed(false);
            Rockstar.getInstance().getRotationHandler().rotate(new Rotation(Speed.mc.player.getYaw(), 90.0f));
            if (Speed.mc.player.isOnGround() && !Speed.mc.options.jumpKey.isPressed()) {
                Speed.mc.player.jump();
                Vec3d velocity = Speed.mc.player.getVelocity();
                Speed.mc.player.setVelocity(velocity.x, velocity.y - (double)0.4f, velocity.z);
                BlockPos target5 = Speed.mc.player.getBlockPos().add(0, 1, 0);
                BlockPos target = Speed.mc.player.getBlockPos();
                BlockPos target1 = Speed.mc.player.getBlockPos().add(0, -1, 0);
                BlockPos target2 = Speed.mc.player.getBlockPos().add(0, -2, 0);
                Speed.mc.world.setBlockState(target1, Blocks.ICE.getDefaultState());
                Vec3d vector3d = new Vec3d((double)target.getX(), (double)target.getY(), (double)target.getZ());
                BlockHitResult result = new BlockHitResult(vector3d, Direction.UP, target, false);
                Vec3d vector3d1 = new Vec3d((double)target1.getX(), (double)target1.getY(), (double)target1.getZ());
                BlockHitResult result1 = new BlockHitResult(vector3d1, Direction.UP, target1, false);
                Vec3d vector3d2 = new Vec3d((double)target2.getX(), (double)target2.getY(), (double)target2.getZ());
                BlockHitResult result2 = new BlockHitResult(vector3d2, Direction.UP, target2, false);
                Speed.mc.player.networkHandler.sendPacket((Packet)new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, result1, 0));
            }
        } else if (this.elytra.isSelected() && (slot = (slotsToSearch = SlotGroups.hotbar()).findItem(Items.ELYTRA)) != null && Speed.mc.player.fallDistance > 1.0f) {
            HotbarSlot currentItem = InventoryUtility.getCurrentHotbarSlot();
            Speed.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
            InventoryUtility.selectHotbarSlot(slot);
            Speed.mc.interactionManager.interactItem((PlayerEntity)Speed.mc.player, Hand.MAIN_HAND);
            ((Slot)Speed.mc.player.currentScreenHandler.slots.get(6)).setStack(new ItemStack((ItemConvertible)Items.ELYTRA));
            if (Speed.mc.player.isSprinting() && Speed.mc.player.input.hasForwardMovement() && Speed.mc.player.checkGliding()) {
                Speed.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)Speed.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
            InventoryUtility.selectHotbarSlot(currentItem);
            Speed.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(Speed.mc.player.getInventory().selectedSlot));
        }
        super.tick();
    }

    @Override
    public void onDisable() {
        EntityUtility.resetTimer();
        super.onDisable();
    }
}

