/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.ActionResult
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package moscow.rockstar.systems.modules.modules.player;

import java.util.Arrays;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@ModuleInfo(name="Scaffold", desc="modules.descriptions.scaffold", category=ModuleCategory.PLAYER)
public class Scaffold
extends BaseModule {
    private static final List<Block> BLACKLIST = Arrays.asList(Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST, Blocks.SAND, Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.STONE_PRESSURE_PLATE, Blocks.OAK_PRESSURE_PLATE, Blocks.BIRCH_PRESSURE_PLATE, Blocks.SPRUCE_PRESSURE_PLATE, Blocks.JUNGLE_PRESSURE_PLATE, Blocks.ACACIA_PRESSURE_PLATE, Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.CRIMSON_PRESSURE_PLATE, Blocks.WARPED_PRESSURE_PLATE);
    private final Timer placeTimer = new Timer();
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (Scaffold.mc.player == null || Scaffold.mc.world == null || Scaffold.mc.interactionManager == null) {
            return;
        }
        BlockPos below = this.getPredictedPos();
        if (!Scaffold.mc.world.getBlockState(below).isAir()) {
            return;
        }
        int slot = this.findBlockSlot();
        if (slot == -1 && (slot = this.findInventoryBlock()) != -1) {
            InventorySlot inv = InventoryUtility.getInventorySlot(slot);
            HotbarSlot target = InventoryUtility.getCurrentHotbarSlot();
            InventoryUtility.moveItem(inv, target);
            slot = target.getSlotId();
        }
        if (Scaffold.mc.player.getInventory().selectedSlot != slot) {
            Scaffold.mc.player.getInventory().selectedSlot = slot;
        }
        if (this.placeTimer.finished(50L)) {
            ActionResult result;
            BlockHitResult hit = this.findHit(below);
            if (hit == null) {
                return;
            }
            Vec3d hitVec = hit.getPos();
            Rotation rotation = RotationMath.getRotationTo(hitVec);
            float yawDiff = Math.abs(rotation.getYaw() - Scaffold.mc.player.getYaw());
            float pitchDiff = Math.abs(rotation.getPitch() - Scaffold.mc.player.getPitch());
            if (yawDiff > 10.0f || pitchDiff > 10.0f) {
                Rockstar.getInstance().getRotationHandler().rotate(rotation, MoveCorrection.DIRECT, 100.0f, 100.0f, 100.0f, RotationPriority.USE_ITEM);
            }
            if ((result = Scaffold.mc.interactionManager.interactBlock(Scaffold.mc.player, Hand.MAIN_HAND, hit)).isAccepted() && Rockstar.getInstance().getRotationHandler().isIdling()) {
                Scaffold.mc.player.swingHand(Hand.MAIN_HAND);
                this.placeTimer.reset();
            }
        }
    };

    private int findInventoryBlock() {
        for (int i = 0; i < 27; ++i) {
            BlockItem blockItem;
            ItemStack stack = Scaffold.mc.player.getInventory().getStack(i + 9);
            Item item = stack.getItem();
            if (stack.getCount() <= 0 || !(item instanceof BlockItem) || BLACKLIST.contains((blockItem = (BlockItem)item).getBlock())) continue;
            return i;
        }
        return -1;
    }

    private BlockPos getPredictedPos() {
        Vec3d vel = Scaffold.mc.player.getVelocity();
        int dx = (int)Math.round(vel.x);
        int dz = (int)Math.round(vel.z);
        BlockPos pos = Scaffold.mc.player.getBlockPos().add(dx, 0, dz);
        return pos.down();
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            BlockItem blockItem;
            ItemStack stack = Scaffold.mc.player.getInventory().getStack(i);
            Item item = stack.getItem();
            if (stack.getCount() <= 0 || !(item instanceof BlockItem) || BLACKLIST.contains((blockItem = (BlockItem)item).getBlock())) continue;
            return i;
        }
        return -1;
    }

    private BlockHitResult findHit(BlockPos target) {
        Direction[] faces;
        for (Direction face : faces = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            BlockPos neighbour = target.offset(face);
            if (Scaffold.mc.world.getBlockState(neighbour).isAir()) continue;
            Vec3d hitVec = Vec3d.ofCenter((Vec3i)neighbour).add(Vec3d.of((Vec3i)face.getVector()).multiply(0.5));
            return new BlockHitResult(hitVec, face.getOpposite(), neighbour, false);
        }
        return null;
    }
}

