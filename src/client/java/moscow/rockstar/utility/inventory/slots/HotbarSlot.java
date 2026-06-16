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

public class HotbarSlot
extends ItemSlot {
    private final int slotId;

    public HotbarSlot(int slotId) {
        if (slotId < 0 || slotId > 8) {
            throw new IllegalArgumentException("Hotbar Slot ID must be between 0 and 8");
        }
        this.slotId = slotId;
    }

    @Override
    public ItemStack itemStack() {
        if (HotbarSlot.mc.player == null || HotbarSlot.mc.player.getInventory() == null) {
            return ItemStack.EMPTY;
        }
        return HotbarSlot.mc.player.getInventory().getStack(this.slotId);
    }

    @Override
    public int getIdForServer() {
        return 36 + this.slotId;
    }

    @Generated
    public int getSlotId() {
        return this.slotId;
    }
}

