/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.screen.slot.SlotActionType
 */
package moscow.rockstar.utility.inventory;

import java.util.function.Predicate;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.InventoryUtility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public abstract class ItemSlot
implements IMinecraft {
    public abstract ItemStack itemStack();

    public abstract int getIdForServer();

    public int syncId() {
        if (ItemSlot.mc.player == null || ItemSlot.mc.player.currentScreenHandler == null) {
            return 0;
        }
        return ItemSlot.mc.player.currentScreenHandler.syncId;
    }

    public Item item() {
        return this.itemStack().getItem();
    }

    public boolean isEmpty() {
        return this.itemStack().isEmpty();
    }

    public boolean contains(Item item) {
        return this.itemStack().getItem() == item;
    }

    public boolean matches(Predicate<ItemStack> predicate) {
        return predicate.test(this.itemStack());
    }

    public void swapTo(ItemSlot newSlot) {
        InventoryUtility.moveItem(this, newSlot);
    }

    public void moveToOffHand() {
        InventoryUtility.moveToOffHand(this);
    }

    public void click() {
        if (ItemSlot.mc.interactionManager == null) {
            return;
        }
        ItemSlot.mc.interactionManager.clickSlot(this.syncId(), this.getIdForServer(), 0, SlotActionType.PICKUP, (PlayerEntity)ItemSlot.mc.player);
    }
}

