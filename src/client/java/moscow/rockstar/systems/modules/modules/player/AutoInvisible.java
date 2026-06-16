/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.screen.slot.SlotActionType
 */
package moscow.rockstar.systems.modules.modules.player;

import java.util.Map;
import java.util.TreeMap;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.game.PotionUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(name="Auto Invisible", category=ModuleCategory.PLAYER, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043f\u044c\u0435\u0442 \u0437\u0435\u043b\u044c\u0435 \u043d\u0435\u0432\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u0438")
public class AutoInvisible
extends BaseModule {
    private final Map<String, StatusEffectInstance> effects = new TreeMap<String, StatusEffectInstance>();
    private boolean isUsingPotion;
    private final BooleanSetting preDrink = new BooleanSetting(this, "\u041f\u0438\u0442\u044c \u0437\u0430\u0440\u0430\u043d\u0435\u0435");
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
        boolean shouldDrink;
        boolean hasInvisibility = AutoInvisible.mc.player.hasStatusEffect(StatusEffects.INVISIBILITY);
        StatusEffectInstance effect = hasInvisibility ? AutoInvisible.mc.player.getStatusEffect(StatusEffects.INVISIBILITY) : null;
        boolean bl = shouldDrink = !hasInvisibility;
        if (this.preDrink.isEnabled() && effect != null && effect.getDuration() <= 200) {
            shouldDrink = true;
        }
        if (shouldDrink) {
            ItemStack offhandItem = AutoInvisible.mc.player.getOffHandStack();
            boolean hasPotionInOffhand = this.isInvisibilityPotion(offhandItem);
            SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
            ItemSlot potionSlot = slotsToSearch.findItem(this::isInvisibilityPotion);
            OffhandSlot offhandSlot = new OffhandSlot();
            if (potionSlot != null && !hasPotionInOffhand) {
                InventoryUtility.moveItem(potionSlot, offhandSlot);
            }
            if (hasPotionInOffhand) {
                this.isUsingPotion = true;
                AutoInvisible.mc.options.useKey.setPressed(true);
            }
        } else if (this.isUsingPotion) {
            AutoInvisible.mc.options.useKey.setPressed(false);
            this.isUsingPotion = false;
            ItemStack offhandItem = AutoInvisible.mc.player.getOffHandStack();
            if (offhandItem.getItem() == Items.GLASS_BOTTLE) {
                AutoInvisible.mc.interactionManager.clickSlot(0, 45, 1, SlotActionType.THROW, (PlayerEntity)AutoInvisible.mc.player);
            }
        }
    };

    private boolean isInvisibilityPotion(ItemStack stack) {
        return PotionUtility.hasEffect(stack, (RegistryEntry<StatusEffect>)StatusEffects.INVISIBILITY);
    }

    @Generated
    public Map<String, StatusEffectInstance> getEffects() {
        return this.effects;
    }
}

