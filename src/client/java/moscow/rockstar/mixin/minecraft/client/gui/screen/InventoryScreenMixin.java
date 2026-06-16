/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ingame.InventoryScreen
 *  net.minecraft.client.gui.screen.ingame.RecipeBookScreen
 *  net.minecraft.client.gui.screen.recipebook.RecipeBookWidget
 *  net.minecraft.client.gui.widget.ButtonWidget
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.player.PlayerInventory
 *  net.minecraft.screen.AbstractRecipeScreenHandler
 *  net.minecraft.screen.PlayerScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import moscow.rockstar.Rockstar;
import moscow.rockstar.mixin.accessors.ScreenAccessor;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.group.impl.ArmorSlotsGroup;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InventoryScreen.class})
public abstract class InventoryScreenMixin
extends RecipeBookScreen<PlayerScreenHandler>
implements IMinecraft {
    public InventoryScreenMixin(PlayerScreenHandler handler, RecipeBookWidget<?> recipeBook, PlayerInventory inventory, Text title) {
        super(handler, recipeBook, inventory, title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void dropButton(CallbackInfo ci) {
        if (Rockstar.INSTANCE.isPanic()) {
            return;
        }
        ButtonWidget widget = ButtonWidget.builder((Text)Text.of((String)Localizator.translate("inventory.button.drop_all")), b -> this.dropAll()).dimensions(this.x + this.backgroundWidth / 2 - 40, this.y - 20, 80, 18).build();
        ((ScreenAccessor)((Object)this)).invokeAddDrawableChild(widget);
    }

    @Unique
    private void dropAll() {
        SlotGroup<ItemSlot> slots = SlotGroups.inventory().and(SlotGroups.hotbar()).and(SlotGroups.offhand()).and(new ArmorSlotsGroup());
        for (ItemSlot slot : slots.getSlots()) {
            if (slot.isEmpty()) continue;
            InventoryScreenMixin.mc.interactionManager.clickSlot(InventoryScreenMixin.mc.player.currentScreenHandler.syncId, slot.getIdForServer(), 1, SlotActionType.THROW, (PlayerEntity)InventoryScreenMixin.mc.player);
        }
    }
}
