/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.screen.slot.SlotActionType
 */
package moscow.rockstar.systems.modules.modules.other;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(name="Inventory Cleaner", category=ModuleCategory.OTHER, desc="\u041e\u0447\u0438\u0449\u0430\u0435\u0442 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c \u043e\u0442 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d\u043d\u044b\u0445 \u0431\u043b\u043e\u043a\u043e\u0432")
public class InventoryCleaner
extends BaseModule {
    private final Timer timer = new Timer();
    private final List<Item> items = List.of(Items.STONE, Items.COBBLESTONE, Items.GRANITE, Items.IRON_ORE, Items.GOLD_ORE, Items.LAPIS_ORE);
    private final List<ItemSlot> slots = new ArrayList<ItemSlot>();
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (!this.isEnabled() || InventoryCleaner.mc.player == null || InventoryCleaner.mc.player.currentScreenHandler == null) {
            return;
        }
        if (this.timer.finished(150L)) {
            this.slots.clear();
            SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
            for (Item item : this.items) {
                ItemSlot itemSlot = slotsToSearch.findItem(item);
                if (itemSlot == null) continue;
                this.slots.add(itemSlot);
            }
            if (this.slots.isEmpty()) {
                return;
            }
            ItemSlot slot = this.slots.removeFirst();
            InventoryCleaner.mc.interactionManager.clickSlot(InventoryCleaner.mc.player.currentScreenHandler.syncId, slot.getIdForServer(), 1, SlotActionType.THROW, (PlayerEntity)InventoryCleaner.mc.player);
            this.timer.reset();
        }
    };
}

