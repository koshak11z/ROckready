/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.inventory.group.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;

public class HotbarSlotsGroup
extends SlotGroup<HotbarSlot> {
    public HotbarSlotsGroup() {
        super(HotbarSlotsGroup.createSlots());
    }

    private static List<HotbarSlot> createSlots() {
        ArrayList<HotbarSlot> slots = new ArrayList<HotbarSlot>();
        for (int i = 0; i < 9; ++i) {
            slots.add(new HotbarSlot(i));
        }
        return slots;
    }
}

