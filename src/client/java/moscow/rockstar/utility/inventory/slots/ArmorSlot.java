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

public class ArmorSlot
extends ItemSlot {
    private final int armorSlotIndex;

    public ArmorSlot(int armorSlotIndex) {
        if (armorSlotIndex < 0 || armorSlotIndex > 3) {
            throw new IllegalArgumentException("Armor Slot Index must be between 0 and 3");
        }
        this.armorSlotIndex = armorSlotIndex;
    }

    @Override
    public ItemStack itemStack() {
        if (ArmorSlot.mc.player == null || ArmorSlot.mc.player.getInventory() == null) {
            return ItemStack.EMPTY;
        }
        return ArmorSlot.mc.player.getInventory().getArmorStack(this.armorSlotIndex);
    }

    @Override
    public int getIdForServer() {
        return 8 - this.armorSlotIndex;
    }

    @Generated
    public int getArmorSlotIndex() {
        return this.armorSlotIndex;
    }
}

