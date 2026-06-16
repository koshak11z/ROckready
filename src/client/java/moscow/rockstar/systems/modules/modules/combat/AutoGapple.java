/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 */
package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@ModuleInfo(name="Auto GApple", category=ModuleCategory.COMBAT, desc="modules.descriptions.auto_gapple")
public class AutoGapple
extends BaseModule {
    private boolean eating;
    private ItemSlot swappedSlot = null;
    private ItemStack savedOffhandItem = null;
    private final SliderSetting healthEat = new SliderSetting(this, "modules.settings.auto_gapple.health").step(1.0f).min(1.0f).max(20.0f).currentValue(15.0f);
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (AutoGapple.mc.player == null || AutoGapple.mc.interactionManager == null) {
            return;
        }
        float totalHealth = AutoGapple.mc.player.getHealth() + AutoGapple.mc.player.getAbsorptionAmount();
        boolean shouldEat = totalHealth <= this.healthEat.getCurrentValue();
        OffhandSlot offhand = InventoryUtility.getOffHandSlot();
        ItemStack offhandStack = offhand.itemStack();
        if (shouldEat && !AutoGapple.mc.player.getItemCooldownManager().isCoolingDown(Items.GOLDEN_APPLE.getDefaultStack())) {
            if (this.isGoldenApple(offhandStack)) {
                this.startEating();
            } else {
                ItemSlot gappleSlot = this.findGappleSlot();
                if (gappleSlot != null) {
                    if (offhand.isEmpty()) {
                        InventoryUtility.moveToOffHand(gappleSlot);
                        this.startEating();
                    } else if (this.savedOffhandItem == null) {
                        this.savedOffhandItem = offhand.itemStack().copy();
                        this.swappedSlot = gappleSlot;
                        InventoryUtility.moveToOffHand(gappleSlot);
                        this.startEating();
                    }
                }
            }
        } else {
            this.stopEating();
            if (this.savedOffhandItem != null && this.swappedSlot != null) {
                InventoryUtility.moveToOffHand(this.swappedSlot);
                this.savedOffhandItem = null;
                this.swappedSlot = null;
            }
        }
    };

    private void startEating() {
        this.eating = true;
        AutoGapple.mc.options.useKey.setPressed(true);
    }

    private void stopEating() {
        if (this.eating) {
            AutoGapple.mc.options.useKey.setPressed(false);
            this.eating = false;
        }
    }

    private boolean isGoldenApple(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
    }

    private ItemSlot findGappleSlot() {
        for (int i = 0; i < 9; ++i) {
            HotbarSlot slot = InventoryUtility.getHotbarSlot(i);
            if (!slot.contains(Items.GOLDEN_APPLE) && !slot.contains(Items.ENCHANTED_GOLDEN_APPLE)) continue;
            return slot;
        }
        return null;
    }

    @Override
    public void onDisable() {
        this.stopEating();
        if (this.savedOffhandItem != null && this.swappedSlot != null) {
            InventoryUtility.moveToOffHand(this.swappedSlot);
            this.savedOffhandItem = null;
            this.swappedSlot = null;
        }
        super.onDisable();
    }
}

