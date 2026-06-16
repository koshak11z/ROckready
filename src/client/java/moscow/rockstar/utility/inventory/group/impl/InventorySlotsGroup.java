/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.inventory.group.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.slots.InventorySlot;

public class InventorySlotsGroup
extends SlotGroup<InventorySlot> {
    public InventorySlotsGroup() {
        super(InventorySlotsGroup.createSlots());
    }

    private static List<InventorySlot> createSlots() {
        ArrayList<InventorySlot> slots = new ArrayList<InventorySlot>();
        for (int i = 0; i < 27; ++i) {
            slots.add(new InventorySlot(i));
        }
        return slots;
    }
}

