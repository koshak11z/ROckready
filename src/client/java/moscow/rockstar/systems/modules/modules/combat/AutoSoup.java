/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.util.Hand
 */
package moscow.rockstar.systems.modules.modules.combat;

import java.util.List;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

@ModuleInfo(name="Auto Soup", category=ModuleCategory.COMBAT)
public class AutoSoup
extends BaseModule {
    int prevSlot = -1;
    int slot = -1;
    int soupTick = -1;
    private final SliderSetting health = new SliderSetting(this, "health").step(1.0f).min(1.0f).max(20.0f).currentValue(10.0f);
    private final Timer timer = new Timer();

    @Override
    public void tick() {
        if (this.soupTick >= 0) {
            if (this.soupTick == 2) {
                AutoSoup.mc.player.getInventory().selectedSlot = this.slot;
            } else if (this.soupTick == 1) {
                AutoSoup.mc.interactionManager.interactItem((PlayerEntity)AutoSoup.mc.player, Hand.MAIN_HAND);
            } else if (this.soupTick == 0) {
                AutoSoup.mc.player.dropSelectedItem(true);
                AutoSoup.mc.player.getInventory().selectedSlot = this.prevSlot;
            }
            --this.soupTick;
            return;
        }
        if (AutoSoup.mc.player.getHealth() >= this.health.getCurrentValue() || !this.timer.finished(300L)) {
            return;
        }
        HotbarSlot hotbarSlot = SlotGroups.hotbar().findItem(Items.MUSHROOM_STEW);
        if (hotbarSlot != null) {
            this.prevSlot = AutoSoup.mc.player.getInventory().selectedSlot;
            AutoSoup.mc.player.getInventory().selectedSlot = this.slot = hotbarSlot.getSlotId();
            this.soupTick = 1;
        } else {
            List<InventorySlot> inventorySlots = SlotGroups.inventory().findItems(Items.MUSHROOM_STEW);
            List<HotbarSlot> emptySlots = SlotGroups.hotbar().findItems(ItemStack::isEmpty);
            if (!inventorySlots.isEmpty() && !emptySlots.isEmpty()) {
                int maxSoups = Math.min(inventorySlots.size(), emptySlots.size());
                maxSoups = Math.min(maxSoups, 8);
                for (int i = 0; i < maxSoups; ++i) {
                    InventorySlot soupSlot = inventorySlots.get(i);
                    HotbarSlot emptySlot = emptySlots.get(i);
                    InventoryUtility.hotbarSwap(soupSlot.getIdForServer(), emptySlot.getSlotId());
                }
                this.prevSlot = AutoSoup.mc.player.getInventory().selectedSlot;
                this.slot = emptySlots.get(0).getSlotId();
                this.soupTick = 2;
            }
        }
        this.timer.reset();
    }
}

