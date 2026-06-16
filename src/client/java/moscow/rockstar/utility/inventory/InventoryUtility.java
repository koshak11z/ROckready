/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.utility.inventory;

import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.impl.HotbarSlotsGroup;
import moscow.rockstar.utility.inventory.slots.ArmorSlot;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.NotNull;

public final class InventoryUtility
implements IMinecraft {
    public static HotbarSlot getHotbarSlot(int slotId) {
        return new HotbarSlot(slotId);
    }

    public static InventorySlot getInventorySlot(int slotId) {
        return new InventorySlot(slotId);
    }

    public static ArmorSlot getArmorSlot(int armorIndex) {
        return new ArmorSlot(armorIndex);
    }

    public static ArmorSlot getHelmetSlot() {
        return InventoryUtility.getArmorSlot(3);
    }

    public static ArmorSlot getChestplateSlot() {
        return InventoryUtility.getArmorSlot(2);
    }

    public static ArmorSlot getLeggingsSlot() {
        return InventoryUtility.getArmorSlot(1);
    }

    public static ArmorSlot getBootsSlot() {
        return InventoryUtility.getArmorSlot(0);
    }

    public static OffhandSlot getOffHandSlot() {
        return new OffhandSlot();
    }

    public static boolean hasItemInOffHand(Item item) {
        return InventoryUtility.getOffHandSlot().contains(item);
    }

    public static boolean offHandItemMatches(Predicate<ItemStack> predicate) {
        return InventoryUtility.getOffHandSlot().matches(predicate);
    }

    public static boolean isOffHandEmpty() {
        return InventoryUtility.getOffHandSlot().isEmpty();
    }

    public static void moveItem(ItemSlot from, ItemSlot to) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        from.click();
        to.click();
        if (!to.isEmpty()) {
            from.click();
        }
        mc.getNetworkHandler().sendPacket((Packet)new CloseHandledScreenC2SPacket(0));
    }

    public static void quickMove(int from) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        InventoryUtility.mc.interactionManager.clickSlot(InventoryUtility.mc.player.currentScreenHandler.syncId, from, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)InventoryUtility.mc.player);
    }

    public static void moveItem(int from, int to) {
        InventoryUtility.moveItem(from, to, false);
    }

    public static void moveItem(int from, int to, boolean back) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        InventoryUtility.mc.interactionManager.clickSlot(InventoryUtility.mc.player.currentScreenHandler.syncId, from, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtility.mc.player);
        InventoryUtility.mc.interactionManager.clickSlot(InventoryUtility.mc.player.currentScreenHandler.syncId, to, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtility.mc.player);
        if (back) {
            InventoryUtility.mc.interactionManager.clickSlot(InventoryUtility.mc.player.currentScreenHandler.syncId, from, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtility.mc.player);
        }
    }

    public static void moveHalf(int from, int to) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        InventoryUtility.mc.interactionManager.clickSlot(InventoryUtility.mc.player.currentScreenHandler.syncId, from, 1, SlotActionType.PICKUP, (PlayerEntity)InventoryUtility.mc.player);
        InventoryUtility.mc.interactionManager.clickSlot(InventoryUtility.mc.player.currentScreenHandler.syncId, to, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtility.mc.player);
    }

    public static void swapOneItem(int from, int to) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        InventoryUtility.mc.interactionManager.clickSlot(InventoryUtility.mc.player.currentScreenHandler.syncId, from, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtility.mc.player);
        InventoryUtility.mc.interactionManager.clickSlot(InventoryUtility.mc.player.currentScreenHandler.syncId, to, 1, SlotActionType.PICKUP, (PlayerEntity)InventoryUtility.mc.player);
        InventoryUtility.mc.interactionManager.clickSlot(InventoryUtility.mc.player.currentScreenHandler.syncId, from, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtility.mc.player);
    }

    public static void hotbarSwap(int from, int to) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        InventoryUtility.mc.interactionManager.clickSlot(InventoryUtility.mc.player.currentScreenHandler.syncId, from, to, SlotActionType.SWAP, (PlayerEntity)InventoryUtility.mc.player);
    }

    public static boolean moveToHotbar(ItemSlot fromSlot, int hotbarSlotId) {
        HotbarSlot hotbarSlot = InventoryUtility.getHotbarSlot(hotbarSlotId);
        InventoryUtility.moveItem(fromSlot, hotbarSlot);
        return true;
    }

    public static boolean moveToArmor(ItemSlot fromSlot, int armorIndex) {
        ArmorSlot armorSlot = InventoryUtility.getArmorSlot(armorIndex);
        InventoryUtility.moveItem(fromSlot, armorSlot);
        return true;
    }

    public static void moveToOffHand(ItemSlot fromSlot) {
        OffhandSlot offHandSlot = InventoryUtility.getOffHandSlot();
        InventoryUtility.moveItem(fromSlot, offHandSlot);
    }

    @NotNull
    public static HotbarSlot getCurrentHotbarSlot() {
        if (InventoryUtility.mc.player == null || InventoryUtility.mc.player.getInventory() == null) {
            return new HotbarSlot(0);
        }
        return InventoryUtility.getHotbarSlot(InventoryUtility.mc.player.getInventory().selectedSlot);
    }

    public static void selectHotbarSlot(int slotId) {
        if (InventoryUtility.mc.player == null || InventoryUtility.mc.player.getInventory() == null || mc.getNetworkHandler() == null) {
            return;
        }
        if (slotId < 0 || slotId > 8) {
            throw new IllegalArgumentException("Hotbar slot ID must be between 0 and 8");
        }
        InventoryUtility.mc.player.getInventory().selectedSlot = slotId;
        mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(InventoryUtility.mc.player.getInventory().selectedSlot));
    }

    public static void selectHotbarSlot(HotbarSlot slot) {
        InventoryUtility.selectHotbarSlot(slot.getSlotId());
    }

    public static boolean selectItemInHotbar(Item item) {
        HotbarSlot slot = (HotbarSlot)new HotbarSlotsGroup().findItem(item);
        if (slot != null) {
            InventoryUtility.selectHotbarSlot(slot);
            return true;
        }
        return false;
    }

    public static int findItemInContainer(Predicate<ItemStack> predicate) {
        if (InventoryUtility.mc.player == null || InventoryUtility.mc.player.currentScreenHandler == null) {
            return -1;
        }
        for (int i = 0; i < InventoryUtility.mc.player.currentScreenHandler.slots.size(); ++i) {
            ItemStack stack = InventoryUtility.mc.player.currentScreenHandler.getSlot(i).getStack();
            if (!predicate.test(stack)) continue;
            return i;
        }
        return -1;
    }

    public static int findItemInContainer(Item item) {
        return InventoryUtility.findItemInContainer((ItemStack stack) -> stack.getItem() == item);
    }

    @Generated
    private InventoryUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

