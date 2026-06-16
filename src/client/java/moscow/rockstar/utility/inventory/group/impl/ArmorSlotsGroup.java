/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.inventory.group.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.slots.ArmorSlot;

public class ArmorSlotsGroup
extends SlotGroup<ArmorSlot> {
    public ArmorSlotsGroup() {
        super(ArmorSlotsGroup.createSlots());
    }

    private static List<ArmorSlot> createSlots() {
        ArrayList<ArmorSlot> slots = new ArrayList<ArmorSlot>();
        for (int i = 0; i < 4; ++i) {
            slots.add(new ArmorSlot(i));
        }
        return slots;
    }
}

