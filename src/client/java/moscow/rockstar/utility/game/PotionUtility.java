/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.PotionItem
 *  net.minecraft.registry.entry.RegistryEntry
 */
package moscow.rockstar.utility.game;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.registry.entry.RegistryEntry;

public final class PotionUtility {
    public static boolean hasEffect(ItemStack stack, RegistryEntry<StatusEffect> effectType) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!(stack.getItem() instanceof PotionItem)) {
            return false;
        }
        PotionContentsComponent potionContents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) {
            return false;
        }
        for (StatusEffectInstance effect : potionContents.getEffects()) {
            if (effect.getEffectType() != effectType) continue;
            return true;
        }
        return false;
    }

    public static List<StatusEffectInstance> effects(ItemStack stack) {
        ArrayList<StatusEffectInstance> effects = new ArrayList<StatusEffectInstance>();
        if (stack == null || stack.isEmpty()) {
            return effects;
        }
        if (!(stack.getItem() instanceof PotionItem)) {
            return effects;
        }
        PotionContentsComponent potionContents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) {
            return effects;
        }
        potionContents.getEffects().forEach(effects::add);
        return effects;
    }

    @Generated
    private PotionUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

