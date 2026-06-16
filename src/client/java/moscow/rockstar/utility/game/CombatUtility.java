/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.gui.screen.ingame.InventoryScreen
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.AxeItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.MaceItem
 *  net.minecraft.item.ShieldItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.utility.game;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.combat.Aura;
import moscow.rockstar.systems.modules.modules.combat.Criticals;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class CombatUtility
implements IMinecraft {
    public static HotbarSlot getMace() {
        boolean useWindBurst = CombatUtility.mc.player.fallDistance > 2.0f;
        RegistryKey<Enchantment> targetEnchantment = useWindBurst ? Enchantments.WIND_BURST : Enchantments.BREACH;
        SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
        HotbarSlot slot = slotsToSearch.findItem(stack -> CombatUtility.hasMaceEnchantment(targetEnchantment, stack));
        if (slot == null) {
            slot = slotsToSearch.findItem(Items.MACE);
        }
        return slot;
    }

    public static float getFallDistance(LivingEntity target) {
        SlotGroup<HotbarSlot> slotsToSearch;
        HotbarSlot slot;
        if (CombatUtility.mc.player.getMainHandStack().getItem() instanceof MaceItem) {
            // empty if block
        }
        if ((slot = (slotsToSearch = SlotGroups.hotbar()).findItem(Items.MACE)) != null) {
            return 0.7f;
        }
        return ServerUtility.isFS() || ServerUtility.isST() ? (Rockstar.getInstance().getModuleManager().getModule(Aura.class).getAttacks() % 10 == 0 ? 0.4f : (Rockstar.getInstance().getModuleManager().getModule(Aura.class).getAttacks() % 5 == 0 ? 0.2f : 0.0f)) : 0.0f;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean canPerformCriticalHit(LivingEntity target, boolean ignoreSprint) {
        if (CombatUtility.mc.world == null) return false;
        if (CombatUtility.mc.player == null) {
            return false;
        }
        Block blockAboveHead = CombatUtility.mc.world.getBlockState(CombatUtility.mc.player.getBlockPos().up(2)).getBlock();
        Aura aura = Rockstar.getInstance().getModuleManager().getModule(Aura.class);
        if (CombatUtility.mc.player.isClimbing()) return true;
        if (EntityUtility.getBlock(0.0, 2.0, 0.0) != Blocks.AIR && (Rockstar.getInstance().getModuleManager().getModule(Aura.class).getAttacks() % 10 == 0 || Rockstar.getInstance().getModuleManager().getModule(Aura.class).getAttacks() % 5 == 0)) {
            if (ServerUtility.isST()) return true;
            if (ServerUtility.isFS()) return true;
        }
        if (CombatUtility.mc.currentScreen instanceof InventoryScreen) return true;
        if (CombatUtility.mc.player.isTouchingWater() && EntityUtility.getBlock(0.0, 1.0, 0.0) == Blocks.WATER) {
            if (CombatUtility.mc.player.fallDistance <= 0.0f) return true;
        }
        if (CombatUtility.mc.player.isSwimming()) return true;
        if (CombatUtility.mc.world.getBlockState(CombatUtility.mc.player.getBlockPos()).isOf(Blocks.COBWEB)) return true;
        if (CombatUtility.mc.player.isInLava()) return true;
        if (aura.getFastPvp().isSelected()) return true;
        if (CombatUtility.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) return true;
        if (CombatUtility.mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return true;
        if (CombatUtility.mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) return true;
        if (CombatUtility.mc.player.hasVehicle()) return true;
        if (CombatUtility.mc.player.getMainHandStack().getItem() instanceof MaceItem) {
            if (CombatUtility.mc.player.fallDistance > 1.0f) {
                return true;
            }
        } else if (CombatUtility.mc.player.fallDistance > CombatUtility.getFallDistance(target)) return true;
        if (ServerUtility.isST() && Rockstar.getInstance().getModuleManager().getModule(Aura.class).getAttacks() % 5 == 0) {
            if (EntityUtility.getBlock(0.0, 2.0, 0.0) != Blocks.AIR) return true;
        }
        if (!Rockstar.getInstance().getModuleManager().getModule(Criticals.class).canCritical()) return false;
        return true;
    }

    public static boolean canBreakShield(LivingEntity target) {
        if (CombatUtility.mc.player == null || CombatUtility.mc.player.isDead()) {
            return false;
        }
        if (target.isDead()) {
            return false;
        }
        HotbarSlot axeSlot = SlotGroups.hotbar().findItem(itemStack -> itemStack.getItem() instanceof AxeItem);
        if (axeSlot == null) {
            return false;
        }
        Vec3d facingVector = target.getRotationVector();
        Vec3d deltaPos = new Vec3d(target.getPos().getX() - CombatUtility.mc.player.getPos().getX(), 0.0, target.getPos().getZ() - CombatUtility.mc.player.getPos().getZ());
        return deltaPos.dotProduct(facingVector) < 0.0;
    }

    public static boolean shouldBreakShield(LivingEntity target) {
        return target.isUsingItem() && target.getActiveItem().getItem() instanceof ShieldItem;
    }

    public static void tryBreakShield(LivingEntity target) {
        if (CombatUtility.mc.player == null || CombatUtility.mc.interactionManager == null) {
            return;
        }
        SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
        HotbarSlot slot = slotsToSearch.findItem(item -> item.getItem() instanceof AxeItem);
        if (slot != null && target instanceof PlayerEntity && target.isUsingItem() && target.getActiveItem().getItem() instanceof ShieldItem) {
            CombatUtility.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
            CombatUtility.mc.interactionManager.attackEntity((PlayerEntity)CombatUtility.mc.player, (Entity)target);
            CombatUtility.mc.player.networkHandler.sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
            CombatUtility.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(CombatUtility.mc.player.getInventory().selectedSlot));
        }
    }

    public static boolean stalin(LivingEntity target) {
        Vec3d pos = target.getPos();
        Box hitbox = target.getBoundingBox();
        float off = 0.05f;
        return !CombatUtility.isAir(hitbox.minX - (double)off, pos.y, hitbox.minZ - (double)off) || !CombatUtility.isAir(hitbox.maxX + (double)off, pos.y, hitbox.minZ - (double)off) || !CombatUtility.isAir(hitbox.minX - (double)off, pos.y, hitbox.maxZ + (double)off) || !CombatUtility.isAir(hitbox.maxX + (double)off, pos.y, hitbox.maxZ + (double)off);
    }

    private static boolean isAir(double x, double y, double z) {
        return CombatUtility.mc.world.getBlockState(new BlockPos((int)x, (int)y, (int)z)).getBlock() == Blocks.AIR;
    }

    @Generated
    private CombatUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static boolean hasMaceEnchantment(RegistryKey targetEnchantment, ItemStack stack) {
        if (!(stack.getItem() instanceof MaceItem)) {
            return false;
        }
        return EnchantmentUtility.getEnchantmentLevel(stack, (RegistryKey<Enchantment>)targetEnchantment) > 0;
    }
}
