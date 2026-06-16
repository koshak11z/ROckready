/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.DataComponentTypes
 */
package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.mixin.minecraft.client.IMinecraftClient;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import net.minecraft.component.DataComponentTypes;

@ModuleInfo(name="Auto Eat", category=ModuleCategory.PLAYER)
public class AutoEat
extends BaseModule {
    private boolean eating;
    private final SliderSetting food = new SliderSetting(this, "modules.settings.auto_eat.food").step(1.0f).min(1.0f).max(20.0f).currentValue(15.0f);
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if ((float)AutoEat.mc.player.getHungerManager().getFoodLevel() <= this.food.getCurrentValue()) {
            SlotGroup<ItemSlot> search = SlotGroups.inventory().and(SlotGroups.hotbar());
            ItemSlot foodSlot = search.findItem(stack -> stack.getItem().getDefaultStack().contains(DataComponentTypes.FOOD));
            if (!AutoEat.mc.player.getOffHandStack().contains(DataComponentTypes.FOOD) && foodSlot != null) {
                foodSlot.moveToOffHand();
            }
            this.eating = true;
            if (AutoEat.mc.currentScreen != null && !AutoEat.mc.player.isUsingItem()) {
                ((IMinecraftClient)mc).idoItemUse();
            } else {
                AutoEat.mc.options.useKey.setPressed(true);
            }
        } else if (this.eating) {
            this.eating = false;
            AutoEat.mc.options.useKey.setPressed(false);
        }
    };
}

