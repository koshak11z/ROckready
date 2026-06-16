/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  lombok.Generated
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.ItemEnchantmentsComponent
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.entry.RegistryEntry
 */
package moscow.rockstar.utility.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Set;
import lombok.Generated;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public final class EnchantmentUtility {
    public static void getEnchantments(ItemStack itemStack, Object2IntMap<RegistryEntry<Enchantment>> enchantments) {
        enchantments.clear();
        if (!itemStack.isEmpty()) {
            Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> itemEnchantments = itemStack.getItem() == Items.ENCHANTED_BOOK ? ((ItemEnchantmentsComponent)itemStack.get(DataComponentTypes.STORED_ENCHANTMENTS)).getEnchantmentEntries() : itemStack.getEnchantments().getEnchantmentEntries();
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantments) {
                enchantments.put(entry.getKey(), entry.getIntValue());
            }
        }
    }

    @SafeVarargs
    public static boolean hasEnchantments(ItemStack itemStack, RegistryKey<Enchantment> ... enchantments) {
        if (itemStack.isEmpty()) {
            return false;
        }
        Object2IntArrayMap itemEnchantments = new Object2IntArrayMap();
        EnchantmentUtility.getEnchantments(itemStack, (Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments);
        for (RegistryKey<Enchantment> enchantment : enchantments) {
            if (EnchantmentUtility.hasEnchantment((Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments, enchantment)) continue;
            return false;
        }
        return true;
    }

    public static int getEnchantmentLevel(ItemStack itemStack, RegistryKey<Enchantment> enchantment) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        Object2IntArrayMap itemEnchantments = new Object2IntArrayMap();
        EnchantmentUtility.getEnchantments(itemStack, (Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments);
        return EnchantmentUtility.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments, enchantment);
    }

    public static int getEnchantmentLevel(Object2IntMap<RegistryEntry<Enchantment>> itemEnchantments, RegistryKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : Object2IntMaps.fastIterable(itemEnchantments)) {
            if (!entry.getKey().matchesKey(enchantment)) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    private static boolean hasEnchantment(Object2IntMap<RegistryEntry<Enchantment>> itemEnchantments, RegistryKey<Enchantment> enchantmentKey) {
        for (RegistryEntry enchantment : itemEnchantments.keySet()) {
            if (!enchantment.matchesKey(enchantmentKey)) continue;
            return true;
        }
        return false;
    }

    @Generated
    private EnchantmentUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
