/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.item.ItemStack
 */
package moscow.rockstar.utility.inventory.slots;

import lombok.Generated;
import moscow.rockstar.utility.inventory.ItemSlot;
import net.minecraft.item.ItemStack;

public class InventorySlot
extends ItemSlot {
    private final int slotId;

    public InventorySlot(int slotId) {
        if (slotId < 0 || slotId > 26) {
            throw new IllegalArgumentException("Inventory Slot ID must be between 0 and 26");
        }
        this.slotId = slotId;
    }

    @Override
    public ItemStack itemStack() {
        if (InventorySlot.mc.player == null || InventorySlot.mc.player.getInventory() == null) {
            return ItemStack.EMPTY;
        }
        return InventorySlot.mc.player.getInventory().getStack(this.slotId + 9);
    }

    @Override
    public int getIdForServer() {
        return this.slotId + 9;
    }

    @Generated
    public int getSlotId() {
        return this.slotId;
    }
}

