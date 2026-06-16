package im.zov4ik.features.impl.misc.autobuy.catalog.items;

import im.zov4ik.features.impl.misc.autobuy.catalog.settings.AutoBuyItemSettings;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;

public interface AutoBuyableItem {
    String getDisplayName();
    String getSearchName(); 
    ItemStack createItemStack();
    int getPrice();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    AutoBuyItemSettings getSettings();
    
    
    default Item getItem() {
        return createItemStack().getItem();
    }
    
    
    default boolean needsAdditionalCheck() {
        ItemStack stack = createItemStack();
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            var nbt = customData.copyNbt();
            if (nbt != null) {
                
                if (nbt.getBoolean("HolyWorldSphere") || 
                    nbt.getBoolean("HolyWorldSphereShard") ||
                    nbt.getBoolean("HolyWorldTalik") ||
                    nbt.getBoolean("HolyWorldExpBottle") ||
                    nbt.getBoolean("HolyWorldBackpack") ||
                    nbt.getBoolean("HolyWorldPyrotechnic") ||
                    nbt.getBoolean("HolyWorldKringe") ||
                    nbt.getBoolean("HolyWorldRune") ||
                    nbt.getBoolean("HolyWorldKringeEffect") ||
                    nbt.getBoolean("HolyWorldPotion") ||
                    nbt.getBoolean("HolyWorldStandardPotion") ||
                    nbt.getBoolean("HolyWorldMultiEffectPotion") ||
                    nbt.getBoolean("SpookyTimeSphere") ||
                    nbt.getBoolean("SpookyTimeTalik") ||
                    nbt.getBoolean("SpookyTimePotion") ||
                    nbt.getBoolean("SpookyTimeSpecial")) {
                    return true;
                }
                
                
                if (nbt.contains("spookyItemType", NbtElement.STRING_TYPE)) {
                    String spookyItemType = nbt.getString("spookyItemType");
                    if (spookyItemType != null && !spookyItemType.isEmpty()) {
                        return true;
                    }
                }
                
                
                if (nbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
                    var attributes = nbt.getList("AttributeModifiers", NbtElement.LIST_TYPE);
                    if (!attributes.isEmpty()) {
                        return true;
                    }
                }
                
                
                if (nbt.contains("RequiredEnchantments", NbtElement.LIST_TYPE)) {
                    var enchants = nbt.getList("RequiredEnchantments", NbtElement.LIST_TYPE);
                    if (!enchants.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        
        
        var enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants != null && !enchants.isEmpty()) {
            return true;
        }
        
        
        var lore = stack.get(DataComponentTypes.LORE);
        if (lore != null && !lore.lines().isEmpty()) {
            return true;
        }
        
        
        var potion = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potion != null && potion.getEffects() != null) {
            var effects = potion.getEffects();
            if (effects.iterator().hasNext()) {
                return true;
            }
        }
        
        return false;
    }
}
