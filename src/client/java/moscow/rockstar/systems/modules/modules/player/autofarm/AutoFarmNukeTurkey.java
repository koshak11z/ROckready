/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.CropBlock
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package moscow.rockstar.systems.modules.modules.player.autofarm;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.player.AutoFarm;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class AutoFarmNukeTurkey
implements IMinecraft {
    private static final int SEARCH_RADIUS = 4;

    public void nuke() {
        AutoFarm autoFarm;
        Block block;
        BlockPos pos;
        BlockPos offset;
        int z;
        int x;
        int y;
        BlockPos targetBlockPos = null;
        Block targetBlock = null;
        for (y = -4; y <= 4; ++y) {
            for (x = -4; x <= 4; ++x) {
                for (z = -4; z <= 4; ++z) {
                    offset = new BlockPos((x % 2 == 0 ? -x : x) / 2, (y % 2 == 0 ? -y : y) / 2, (z % 2 == 0 ? -z : z) / 2);
                    pos = AutoFarmNukeTurkey.mc.player.getBlockPos().add((Vec3i)offset);
                    if (AutoFarmNukeTurkey.mc.player.getPos().distanceTo(Vec3d.ofCenter((Vec3i)pos)) > 6.0) continue;
                    block = AutoFarmNukeTurkey.mc.world.getBlockState(pos).getBlock();
                    autoFarm = Rockstar.getInstance().getModuleManager().getModule(AutoFarm.class);
                    if (!(autoFarm.getMelon().isSelected() && block == Blocks.MELON || autoFarm.getTikva().isSelected() && block == Blocks.PUMPKIN)) continue;
                    targetBlockPos = pos;
                    targetBlock = block;
                    break;
                }
                if (targetBlockPos != null) break;
            }
            if (targetBlockPos != null) break;
        }
        if (targetBlockPos == null) {
            for (y = 0; y < 3; ++y) {
                for (x = 0; x < 8; ++x) {
                    for (z = 0; z < 8; ++z) {
                        offset = new BlockPos((x % 2 == 0 ? -x : x) / 2, y, (z % 2 == 0 ? -z : z) / 2);
                        pos = AutoFarmNukeTurkey.mc.player.getBlockPos().up().add((Vec3i)offset);
                        if (AutoFarmNukeTurkey.mc.player.getPos().distanceTo(Vec3d.ofCenter((Vec3i)pos)) > 6.0 || AutoFarmNukeTurkey.mc.world.getBlockState(pos).getBlock() == Blocks.AIR) continue;
                        block = AutoFarmNukeTurkey.mc.world.getBlockState(pos).getBlock();
                        autoFarm = Rockstar.getInstance().getModuleManager().getModule(AutoFarm.class);
                        if (!(autoFarm.getMelon().isSelected() && block == Blocks.MELON || autoFarm.getTikva().isSelected() && block == Blocks.PUMPKIN || autoFarm.getAllCrops().isSelected() && block instanceof CropBlock)) continue;
                        targetBlockPos = pos;
                        targetBlock = block;
                        break;
                    }
                    if (targetBlockPos != null) break;
                }
                if (targetBlockPos != null) break;
            }
        }
        if (targetBlockPos != null && targetBlock != null) {
            double posX = targetBlockPos.getX();
            double posY = targetBlockPos.getY();
            double posZ = targetBlockPos.getZ();
            double deltaX = posX - AutoFarmNukeTurkey.mc.player.getX();
            double deltaY = posY - (AutoFarmNukeTurkey.mc.player.getY() + (double)AutoFarmNukeTurkey.mc.player.getEyeHeight(AutoFarmNukeTurkey.mc.player.getPose()));
            double deltaZ = posZ - AutoFarmNukeTurkey.mc.player.getZ();
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f + MathUtility.random(-2.0, 2.0);
            float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance))) + MathUtility.random(-1.0, 1.0);
            AutoFarmNukeTurkey.mc.player.networkHandler.sendPacket((Packet)new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
            Rockstar.getInstance().getRotationHandler().rotate(new Rotation(yaw, pitch), MoveCorrection.SILENT, 180.0f, 80.0f, 180.0f, RotationPriority.NORMAL);
            this.equipAxe();
            Direction direction = AutoFarmNukeTurkey.getDirection(targetBlockPos);
            this.breakBlockFast(targetBlockPos, direction);
        }
    }

    private void breakBlockFast(BlockPos pos, Direction direction) {
        if (AutoFarmNukeTurkey.mc.player == null || AutoFarmNukeTurkey.mc.player.networkHandler == null) return;
        AutoFarmNukeTurkey.mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction));
        AutoFarmNukeTurkey.mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
        AutoFarmNukeTurkey.mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void equipAxe() {
        SlotGroup<ItemSlot> search = SlotGroups.inventory().and(SlotGroups.hotbar());
        ItemSlot axe = search.findItem(Items.DIAMOND_AXE);
        if (axe != null) {
            if (axe instanceof HotbarSlot) {
                HotbarSlot itemHotbarSlot = (HotbarSlot)axe;
                if (InventoryUtility.getCurrentHotbarSlot().item() != axe.item()) {
                    InventoryUtility.selectHotbarSlot(itemHotbarSlot);
                }
            } else if (axe instanceof InventorySlot) {
                InventorySlot itemInventorySlot = (InventorySlot)axe;
                HotbarSlot currentSlot = InventoryUtility.getCurrentHotbarSlot();
                itemInventorySlot.swapTo(currentSlot);
            }
        }
    }

    public static Direction getDirection(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(AutoFarmNukeTurkey.mc.player.getX(), AutoFarmNukeTurkey.mc.player.getY() + (double)AutoFarmNukeTurkey.mc.player.getEyeHeight(AutoFarmNukeTurkey.mc.player.getPose()), AutoFarmNukeTurkey.mc.player.getZ());
        if ((double)pos.getY() > eyesPos.y) {
            if (AutoFarmNukeTurkey.mc.world.getBlockState(pos.add(0, -1, 0)).isReplaceable()) {
                return Direction.DOWN;
            }
            return AutoFarmNukeTurkey.mc.player.getHorizontalFacing().getOpposite();
        }
        if (!AutoFarmNukeTurkey.mc.world.getBlockState(pos.add(0, 1, 0)).isReplaceable()) {
            return AutoFarmNukeTurkey.mc.player.getHorizontalFacing().getOpposite();
        }
        return Direction.UP;
    }
}

