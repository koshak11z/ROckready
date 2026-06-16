/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.equipment.EquipmentType
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.utility.game;

import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.combat.Aura;
import moscow.rockstar.systems.modules.modules.combat.ElytraTarget;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.CombatUtility;
import moscow.rockstar.utility.game.prediction.ElytraPredictionSystem;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.mixins.ArmorItemAddition;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class ElytraUtility
implements IMinecraft {
    private static Vec3d lastVec;
    private static final Timer fireworkTimer;

    public static void swapInHotbar(boolean chestplate) {
        HotbarSlot slot;
        SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
        HotbarSlot hotbarSlot = slot = chestplate ? slotsToSearch.findItem(itemStack -> {
            ArmorItem armorItem;
            Item patt0$temp = itemStack.getItem();
            return patt0$temp instanceof ArmorItem && ((ArmorItemAddition)(armorItem = (ArmorItem)patt0$temp)).rockstar$getType() == EquipmentType.CHESTPLATE;
        }) : slotsToSearch.findItem(Items.ELYTRA);
        if (slot != null) {
            HotbarSlot currentItem = InventoryUtility.getCurrentHotbarSlot();
            ElytraUtility.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
            InventoryUtility.selectHotbarSlot(slot);
            ElytraUtility.mc.interactionManager.interactItem((PlayerEntity)ElytraUtility.mc.player, Hand.MAIN_HAND);
            ((Slot)ElytraUtility.mc.player.currentScreenHandler.slots.get(6)).setStack(new ItemStack((ItemConvertible)(chestplate ? Items.NETHERITE_CHESTPLATE : Items.ELYTRA)));
            InventoryUtility.selectHotbarSlot(currentItem);
            ElytraUtility.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(ElytraUtility.mc.player.getInventory().selectedSlot));
        }
    }

    public static void drawBoxes(MatrixStack matrices, BufferBuilder linesBuffer, Box box, ColorRGBA color) {
        Draw3DUtility.renderOutlinedBox(matrices, linesBuffer, box, color);
        Draw3DUtility.renderBoxInternalDiagonals(matrices, linesBuffer, box, color);
    }

    public static boolean leaving() {
        PlayerEntity player;
        Entity entity;
        Aura aura = Rockstar.getInstance().getModuleManager().getModule(Aura.class);
        return !aura.isCooledDown() && (entity = Rockstar.getInstance().getTargetManager().getCurrentTarget()) instanceof PlayerEntity && !ElytraPredictionSystem.isLeaving(player = (PlayerEntity)entity) || CombatUtility.getMace() != null && !aura.getAttackTimer().finished(1500L);
    }

    public static void useFirework(float selectedSlot) {
        SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
        HotbarSlot slot = slotsToSearch.findItem(Items.FIREWORK_ROCKET);
        if (slot != null) {
            LivingEntity target = Rockstar.getInstance().getTargetManager().getLivingTarget();
            Rotation rot = target == null ? Rockstar.getInstance().getRotationHandler().getPlayerRotation() : RotationMath.getRotationTo(ElytraUtility.leaving() ? ElytraUtility.mc.player.getEyePos().add(ElytraUtility.leaveVec(target)) : ElytraUtility.targetPoint(target));
            ElytraUtility.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
            ElytraUtility.mc.interactionManager.sendSequencedPacket(ElytraUtility.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, rot.getYaw(), rot.getPitch()));
            ElytraUtility.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(ElytraUtility.mc.player.getInventory().selectedSlot));
            fireworkTimer.reset();
            return;
        }
        SlotGroup<InventorySlot> search = SlotGroups.inventory();
        InventorySlot invSlot = search.findItem(Items.FIREWORK_ROCKET);
        if (invSlot != null) {
            InventoryUtility.hotbarSwap(invSlot.getIdForServer(), (int)(selectedSlot - 1.0f));
            fireworkTimer.reset();
        }
    }

    public static Vec3d targetPoint(LivingEntity target) {
        Vec3d vec3d;
        if (Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isEnabled() && target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)target;
            vec3d = ElytraPredictionSystem.predictPlayerPosition(player);
        } else {
            vec3d = target.getPos();
        }
        return RotationMath.getNearestPoint(target, vec3d);
    }

    public static Vec3d leaveVec(LivingEntity target) {
        List<Vec3d> leaveVectors = List.of(new Vec3d(0.0, 20.0, 0.0), new Vec3d(0.0, -20.0, 0.0), new Vec3d(20.0, 0.0, 0.0), new Vec3d(-20.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 20.0), new Vec3d(0.0, 0.0, -20.0));
        if (CombatUtility.getMace() != null) {
            leaveVectors = List.of(new Vec3d(0.0, 20.0, 0.0));
        }
        Vec3d leaveVec = Vec3d.ZERO;
        for (Vec3d vector : leaveVectors) {
            if (!MathUtility.canSeen(target.getEyePos().add(vector)) || Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).getSwapVector().isEnabled() && vector.equals((Object)lastVec)) continue;
            leaveVec = vector;
            break;
        }
        return leaveVec;
    }

    public static float[] getLeftRightYaw45NotMultipleOf90(float yaw) {
        float nearest45;
        float baseYaw = yaw - yaw % 360.0f;
        float yawNormalized = yaw % 360.0f;
        float left;
        float right;
        if (yawNormalized < 0.0f) {
            yawNormalized += 360.0f;
            baseYaw -= 360.0f;
        }
        if ((nearest45 = (float)Math.round(yawNormalized / 45.0f) * 45.0f) % 90.0f == 0.0f) {
            float candidateLeft = (nearest45 - 45.0f) % 360.0f;
            float candidateRight = (nearest45 + 45.0f) % 360.0f;
            left = candidateLeft < yawNormalized ? candidateLeft : candidateLeft - 45.0f;
            right = candidateRight > yawNormalized ? candidateRight : candidateRight + 45.0f;
        } else if (nearest45 < yawNormalized) {
            left = nearest45;
            right = nearest45 + 90.0f;
        } else {
            left = nearest45 - 90.0f;
            right = nearest45;
        }
        return new float[]{left += baseYaw, right += baseYaw};
    }

    @Generated
    private ElytraUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setLastVec(Vec3d lastVec) {
        ElytraUtility.lastVec = lastVec;
    }

    @Generated
    public static Timer getFireworkTimer() {
        return fireworkTimer;
    }

    static {
        fireworkTimer = new Timer();
    }
}
