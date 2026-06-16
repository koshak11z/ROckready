package im.zov4ik.features.impl.misc.autobuy.catalog.util;

import im.zov4ik.features.impl.misc.autobuy.catalog.items.list.HolyWorldProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AuctionUtils {
    public static final Pattern funTimePricePattern = Pattern.compile("\\$\\s*([0-9][0-9\\s,]*)");

    public static final Pattern funTimePriceLabelPattern = Pattern.compile("(?iu)\\$?\\s*цен[аa]\\s*:?\\s*\\$?\\s*([0-9][0-9\\s,]*)");

    public static final Pattern holyWorldPricePattern = Pattern.compile("(\\d+(?:[\\s,]\\d{3})*)¤");

    public static int getPrice(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return -1;
        }

        List<String> sources = new ArrayList<>();

        var lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                String s = line.getString();
                if (s != null && !s.isEmpty()) sources.add(s.replaceAll("§.", ""));
            }
        }

        sources.add(stack.getName().getString().replaceAll("§.", ""));

        if (stack.getComponents() != null) {
            String raw = stack.getComponents().toString();
            int start = raw.indexOf("literal{ $");
            if (start >= 0) {
                int end = raw.indexOf("}", start + 10);
                if (end > start) sources.add(raw.substring(start + 10, end));
            }
        }

        for (String s : sources) {
            java.util.regex.Matcher m = funTimePriceLabelPattern.matcher(s);
            if (m.find()) {
                int v = parseClean(m.group(1));
                if (v > 0) return v;
            }
        }

        for (String s : sources) {
            java.util.regex.Matcher m = funTimePricePattern.matcher(s);
            if (m.find()) {
                int v = parseClean(m.group(1));
                if (v > 0) return v;
            }
        }

        for (String s : sources) {
            java.util.regex.Matcher m = holyWorldPricePattern.matcher(s);
            if (m.find()) {
                int v = parseClean(m.group(1));
                if (v > 0) return v;
            }
        }

        return -1;
    }

    private static int parseClean(String raw) {
        if (raw == null || raw.isBlank()) return -1;
        try {
            String clean = raw.replaceAll("[\\s,]", "").replaceAll("\\.\\d+$", "");
            if (clean.isEmpty()) return -1;
            return Integer.parseInt(clean);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public static int getPriceFromNearbySlots(List<Slot> slots, int itemSlotId) {

        int[] offsets = new int[]{9, -9};

        for (int offset : offsets) {
            int priceSlotId = itemSlotId + offset;
            if (priceSlotId < 0 || priceSlotId >= slots.size()) continue;

            Slot priceSlot = slots.get(priceSlotId);
            if (priceSlot == null) continue;

            ItemStack priceStack = priceSlot.getStack();
            if (priceStack.isEmpty()) continue;
        }
        return -1;
    }



    private static String cleanString(String str) {
        if (str == null) return "";
        return str.toLowerCase().trim()
                .replaceAll("§.", "")
                .replaceAll("[^a-zа-яё0-9\\s\\[\\]★]", "")
                .replaceAll("\\s+", " ");
    }

    public static boolean isArmorItem(ItemStack stack) {
        return stack.getItem() == Items.NETHERITE_HELMET ||
                stack.getItem() == Items.NETHERITE_CHESTPLATE ||
                stack.getItem() == Items.NETHERITE_LEGGINGS ||
                stack.getItem() == Items.NETHERITE_BOOTS ||
                stack.getItem() == Items.DIAMOND_HELMET ||
                stack.getItem() == Items.DIAMOND_CHESTPLATE ||
                stack.getItem() == Items.DIAMOND_LEGGINGS ||
                stack.getItem() == Items.DIAMOND_BOOTS ||
                stack.getItem() == Items.IRON_HELMET ||
                stack.getItem() == Items.IRON_CHESTPLATE ||
                stack.getItem() == Items.IRON_LEGGINGS ||
                stack.getItem() == Items.IRON_BOOTS ||
                stack.getItem() == Items.GOLDEN_HELMET ||
                stack.getItem() == Items.GOLDEN_CHESTPLATE ||
                stack.getItem() == Items.GOLDEN_LEGGINGS ||
                stack.getItem() == Items.GOLDEN_BOOTS ||
                stack.getItem() == Items.CHAINMAIL_HELMET ||
                stack.getItem() == Items.CHAINMAIL_CHESTPLATE ||
                stack.getItem() == Items.CHAINMAIL_LEGGINGS ||
                stack.getItem() == Items.CHAINMAIL_BOOTS ||
                stack.getItem() == Items.LEATHER_HELMET ||
                stack.getItem() == Items.LEATHER_CHESTPLATE ||
                stack.getItem() == Items.LEATHER_LEGGINGS ||
                stack.getItem() == Items.LEATHER_BOOTS ||
                stack.getItem() == Items.TURTLE_HELMET;
    }

    public static boolean hasThornsEnchantment(ItemStack stack) {
        var enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) {
            return false;
        }

        for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
            String enchantId = entry.getIdAsString();
            if (enchantId != null) {
                String lowerEnchantId = enchantId.toLowerCase();
                if (lowerEnchantId.contains("thorns") || lowerEnchantId.contains("шип")) {
                    return true;
                }
            }
        }

        var lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                String loreStr = line.getString().toLowerCase();
                if (loreStr.contains("thorns") || loreStr.contains("шип")) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean compareItem(ItemStack a, ItemStack b) {
        
        

        String aName = a.getName().getString();
        String bName = b.getName().getString();
        String aItemType = a.getItem().toString();
        String bItemType = b.getItem().toString();

        
        
        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);
        boolean isHolyWorldItem = false;
        boolean isHolyWorldSphere = false;
        if (bCustomData != null) {
            NbtCompound bNbt = bCustomData.copyNbt();
            if (bNbt != null) {
                isHolyWorldItem = bNbt.getBoolean("HolyWorldItem");
                isHolyWorldSphere = bNbt.getBoolean("HolyWorldSphere");
            }
        }

        
        if (isHolyWorldSphere) {
            
            if (a.getItem() == Items.END_CRYSTAL || a.getItem() == Items.PLAYER_HEAD) {
                return compareHolyWorldSphere(a, b);
            }
            return false;
        }

        
        
        boolean isSpookyTimeItem = false;
        boolean isSpookyTimeSphere = false;
        boolean isSpookyTimeTalik = false;
        boolean isSpookyTimePotion = false;
        if (bCustomData != null) {
            NbtCompound bNbtSpooky = bCustomData.copyNbt();
            if (bNbtSpooky != null && bNbtSpooky.getBoolean("SpookyTimeItem")) {
                isSpookyTimeItem = true;
                isSpookyTimeSphere = bNbtSpooky.getBoolean("SpookyTimeSphere");
                isSpookyTimeTalik = bNbtSpooky.getBoolean("SpookyTimeTalik");
                isSpookyTimePotion = bNbtSpooky.getBoolean("SpookyTimePotion");
            }
        }

        
        if (isSpookyTimePotion) {
            boolean isPotion = (a.getItem() == Items.SPLASH_POTION || a.getItem() == Items.POTION) &&
                    (b.getItem() == Items.SPLASH_POTION || b.getItem() == Items.POTION);
            if (!isPotion) {
                return false;
            }
            
            
            return compareSpookyTimePotion(a, b);
        }

        
        if (isSpookyTimeSphere) {
            
            if (a.getItem() == Items.PLAYER_HEAD && b.getItem() == Items.PLAYER_HEAD) {
                return compareTalismanByAttributes(a, b);
            }
            return false;
        }

        
        if (a.getItem() != b.getItem()) {
            return false;
        }

        if (isHolyWorldItem) {
            NbtCompound bNbt = bCustomData.copyNbt();
            if (bNbt != null) {
                
                if (bNbt.getBoolean("HolyWorldExpBottle")) {
                    return compareHolyWorldExpBottle(a, b);
                }
                
                if (bNbt.getBoolean("HolyWorldBackpack")) {
                    return compareHolyWorldBackpack(a, b);
                }
                
                if (bNbt.getBoolean("HolyWorldPyrotechnic")) {
                    return compareHolyWorldPyrotechnic(a, b);
                }
                
                if (bNbt.getBoolean("HolyWorldKringe")) {
                    return compareHolyWorldKringe(a, b);
                }
                
                if (bNbt.getBoolean("HolyWorldRune")) {
                    return compareHolyWorldRune(a, b);
                }
                
                if (bNbt.getBoolean("HolyWorldSphereShard")) {
                    return compareHolyWorldSphereShard(a, b);
                }
                
                if (bNbt.getBoolean("HolyWorldKringeEffect")) {
                    return compareHolyWorldKringeEffect(a, b);
                }
                
                if (bNbt.getBoolean("HolyWorldPotion")) {
                    return compareHolyWorldPotion(a, b);
                }
                
                if (bNbt.getBoolean("HolyWorldStandardPotion")) {
                    return compareHolyWorldStandardPotion(a, b);
                }
                
                if (bNbt.getBoolean("HolyWorldMultiEffectPotion")) {
                    return compareHolyWorldMultiEffectPotion(a, b);
                }
                
                if (bNbt.getBoolean("HolyWorldTalik")) {
                    return compareHolyWorldTalik(a, b);
                }
                
                if (bNbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
                    NbtList bAttributes = bNbt.getList("AttributeModifiers", NbtElement.LIST_TYPE);
                    if (!bAttributes.isEmpty()) {
                        
                        return compareTalismanByAttributes(a, b);
                    }
                }
                
                if (!holyWorldArmorVariantMatchesByNbt(a, b)) {
                    return false;
                }
                return compareByEnchantments(a, b);
            }
        }

        
        if (isSpookyTimeItem && !isSpookyTimePotion && !isSpookyTimeSphere) {
            
            if (a.getItem() != b.getItem()) {
                return false;
            }

            NbtCompound bNbt = bCustomData.copyNbt();
            if (bNbt != null) {
                
                if (bNbt.getBoolean("SpookyTimeSpecial")) {
                    return compareSpookyTimeSpecial(a, b);
                }
                
                if (isSpookyTimeTalik) {
                    if (a.getItem() == Items.TOTEM_OF_UNDYING && b.getItem() == Items.TOTEM_OF_UNDYING) {
                        
                        NbtList bAttributes = bNbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
                        if (bAttributes != null && !bAttributes.isEmpty()) {
                            
                            return compareTalismanByAttributes(a, b);
                        } else {
                            
                            var aCustomDataTalik = a.get(DataComponentTypes.CUSTOM_DATA);
                            if (aCustomDataTalik != null) {
                                NbtCompound aNbtTalik = aCustomDataTalik.copyNbt();
                                if (aNbtTalik != null && aNbtTalik.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
                                    NbtList aAttributesTalik = aNbtTalik.getList("AttributeModifiers", NbtElement.LIST_TYPE);
                                    if (!aAttributesTalik.isEmpty()) {
                                        
                                        return false;
                                    }
                                }
                            }
                            
                            return true;
                        }
                    }
                    return false;
                }
            }
            
            
            return compareByEnchantments(a, b);
        }

        
        
        if (a.getItem() == Items.TOTEM_OF_UNDYING && b.getItem() == Items.TOTEM_OF_UNDYING) {
            
            boolean bHasAttributes = false;
            var bCustomDataTalik = b.get(DataComponentTypes.CUSTOM_DATA);
            if (bCustomDataTalik != null) {
                NbtCompound bNbtTalik = bCustomDataTalik.copyNbt();
                if (bNbtTalik != null && bNbtTalik.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
                    NbtList bAttributesTalik = bNbtTalik.getList("AttributeModifiers", NbtElement.LIST_TYPE);
                    if (!bAttributesTalik.isEmpty()) {
                        bHasAttributes = true;
                        
                        return compareTalismanByAttributes(a, b);
                    }
                }
            }

            
            boolean aHasAttributes = false;
            var aCustomDataTalik = a.get(DataComponentTypes.CUSTOM_DATA);
            if (aCustomDataTalik != null) {
                NbtCompound aNbtTalik = aCustomDataTalik.copyNbt();
                if (aNbtTalik != null && aNbtTalik.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
                    NbtList aAttributesTalik = aNbtTalik.getList("AttributeModifiers", NbtElement.LIST_TYPE);
                    if (!aAttributesTalik.isEmpty()) {
                        aHasAttributes = true;
                    }
                }
            }

            
            if (!bHasAttributes && aHasAttributes) {
                return false;
            }

            return true;
        }

        
        var bCustomDataHead = b.get(DataComponentTypes.CUSTOM_DATA);
        if (bCustomDataHead != null) {
            NbtCompound bNbt = bCustomDataHead.copyNbt();
            if (bNbt != null && bNbt.getBoolean("HolyWorldSphere")) {
                
                return compareHolyWorldSphere(a, b);
            }
        }

        
        if (a.getItem() == Items.PLAYER_HEAD && b.getItem() == Items.PLAYER_HEAD) {
            
            var bCustomDataHead2 = b.get(DataComponentTypes.CUSTOM_DATA);
            if (bCustomDataHead2 != null) {
                NbtCompound bNbt = bCustomDataHead2.copyNbt();
                if (bNbt != null && bNbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
                    return compareTalismanByAttributes(a, b);
                }
            }
            
            return false;
        }

        var aLore = a.get(DataComponentTypes.LORE);
        var bLoreComp = b.get(DataComponentTypes.LORE);
        boolean hasLore = bLoreComp != null && !bLoreComp.lines().isEmpty();

        if (hasLore) {
            List<Text> expectedLore = bLoreComp.lines();

            if (aLore == null || aLore.lines().isEmpty()) {
                
                return false;
            } else {
                List<String> auctionLoreStrings = aLore.lines().stream()
                        .map(text -> cleanString(text.getString()))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                String auctionLoreJoined = String.join(" ", auctionLoreStrings);

                boolean hasOriginalMarker = false;
                for (String line : auctionLoreStrings) {
                    if (line.contains("оригинальный предмет") || line.contains("★")) {
                        hasOriginalMarker = true;
                        break;
                    }
                }

                int matchCount = 0;
                int requiredMatches = 0;

                for (Text expected : expectedLore) {
                    String expectedStr = cleanString(expected.getString());
                    if (expectedStr.isEmpty()) continue;

                    boolean isOriginalMarker = expectedStr.contains("оригинальный предмет") || expectedStr.contains("★");

                    if (isOriginalMarker) {
                        if (!hasOriginalMarker) {
                            return false;
                        }
                        matchCount++;
                        requiredMatches++;
                        continue;
                    }

                    requiredMatches++;

                    boolean found = false;
                    for (String auctionLine : auctionLoreStrings) {
                        if (auctionLine.contains(expectedStr) || expectedStr.contains(auctionLine)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found && auctionLoreJoined.contains(expectedStr)) {
                        found = true;
                    }

                    if (found) {
                        matchCount++;
                    }
                }

                if (matchCount < requiredMatches) {
                    return false;
                }
                
                return true;
            }
        }

        
        
        if ((a.getItem() == Items.SPLASH_POTION && b.getItem() == Items.SPLASH_POTION) ||
                (a.getItem() == Items.POTION && b.getItem() == Items.POTION)) {
            var aPotion = a.get(DataComponentTypes.POTION_CONTENTS);
            var bPotion = b.get(DataComponentTypes.POTION_CONTENTS);

            if (bPotion != null) {
                
                List<StatusEffectInstance> bEffectsList = new ArrayList<>();
                bPotion.getEffects().forEach(bEffectsList::add);

                if (!bEffectsList.isEmpty()) {
                    
                    if (aPotion == null) {
                        return false;
                    }

                    List<StatusEffectInstance> aEffectsList = new ArrayList<>();
                    aPotion.getEffects().forEach(aEffectsList::add);

                    if (aEffectsList.isEmpty()) {
                        return false;
                    }

                    
                    java.util.List<String> matchedEffects = new java.util.ArrayList<>();
                    for (StatusEffectInstance bEffect : bEffectsList) {
                        boolean found = false;
                        for (StatusEffectInstance aEffect : aEffectsList) {
                            if (aEffect.getEffectType().equals(bEffect.getEffectType()) &&
                                    aEffect.getAmplifier() == bEffect.getAmplifier()) {
                                
                                if (bEffect.getDuration() > 0) {
                                    
                                    int diff = Math.abs(aEffect.getDuration() - bEffect.getDuration());
                                    if (diff <= 20) { 
                                        found = true;
                                        matchedEffects.add(bEffect.getEffectType().value().getName().getString() + "=" + bEffect.getAmplifier() + " (длительность: " + aEffect.getDuration() + " тиков)");
                                        break;
                                    }
                                } else {
                                    found = true;
                                    matchedEffects.add(bEffect.getEffectType().value().getName().getString() + "=" + bEffect.getAmplifier());
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            return false;
                        }
                    }
                    
                    return true;
                }
            }
        }

        
        return compareByEnchantments(a, b);
    }

    
    public static String getEnchantmentName(String enchantId) {
        switch (enchantId) {
            case "protection":
                return "защита";
            case "fire_protection":
                return "огнестойкость";
            case "blast_protection":
                return "взрывоустойчивость";
            case "projectile_protection":
                return "защита от снарядов";
            case "thorns":
                return "шипы";
            case "unbreaking":
                return "прочность";
            case "sharpness":
                return "острота";
            case "smite":
                return "небесная кара";
            case "bane_of_arthropods":
                return "гибель насекомых";
            case "knockback":
                return "отбрасывание";
            case "fire_aspect":
                return "заговор огня";
            case "looting":
                return "добыча";
            case "efficiency":
                return "эффективность";
            case "fortune":
                return "удача";
            case "silk_touch":
                return "шелковое касание";
            case "power":
                return "сила";
            case "punch":
                return "отдача";
            case "flame":
                return "пламя";
            case "infinity":
                return "бесконечность";
            case "mending":
                return "починка";
            case "frost_walker":
                return "ледяной путь";
            case "depth_strider":
                return "подводная ходьба";
            case "respiration":
                return "дыхание";
            case "aqua_affinity":
                return "подводная добыча";
            case "sweeping_edge":
                return "разящий клинок";
            default:
                return null;
        }
    }

    
    private static final String LORE_IMPERETRABLE_II = "Непробиваемый II";
    private static final String LORE_IMPERETRABLE_I = "Непробиваемый I";

    /** Вариант брони по лору: Infinity = II (2), Eternity = I (1). 0 = неизвестно. */
    private static int getHolyWorldArmorVariant(ItemStack stack) {
        var lore = stack.get(DataComponentTypes.LORE);
        if (lore == null || lore.lines().isEmpty()) return 0;
        for (Text line : lore.lines()) {
            String s = line.getString();
            if (s != null) {
                if (s.contains(LORE_IMPERETRABLE_II)) return 2;
                if (s.contains(LORE_IMPERETRABLE_I)) return 1;
            }
        }
        return 0;
    }

    private static boolean holyWorldArmorVariantMatchesByNbt(ItemStack a, ItemStack b) {
        int variantB = getHolyWorldArmorVariant(b);
        if (variantB == 0) return true;
        int variantA = getHolyWorldArmorVariant(a);
        if (variantA == 0) return true;
        return variantA == variantB;
    }

    public static boolean compareByEnchantments(ItemStack a, ItemStack b) {
        
        var aEnchants = a.get(DataComponentTypes.ENCHANTMENTS);
        var bLore = b.get(DataComponentTypes.LORE);

        
        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);
        NbtList requiredEnchantmentsNbt = null;
        if (bCustomData != null) {
            NbtCompound bNbt = bCustomData.copyNbt();
            if (bNbt != null && bNbt.contains("RequiredEnchantments", NbtElement.LIST_TYPE)) {
                requiredEnchantmentsNbt = bNbt.getList("RequiredEnchantments", NbtElement.COMPOUND_TYPE);
            }
        }

        var bEnchants = b.get(DataComponentTypes.ENCHANTMENTS);

        
        
        boolean hasRequiredEnchantments = (bEnchants != null && !bEnchants.isEmpty())
                || (requiredEnchantmentsNbt != null && !requiredEnchantmentsNbt.isEmpty());
        boolean hasExpectedLore = bLore != null && !bLore.lines().isEmpty();
        if (!hasRequiredEnchantments && !hasExpectedLore) {
            return true;
        }

        if (hasRequiredEnchantments && (aEnchants == null || aEnchants.isEmpty())) {
            return false;
        }

        
        java.util.List<String> matchedEnchantments = new java.util.ArrayList<>();
        if (requiredEnchantmentsNbt != null && !requiredEnchantmentsNbt.isEmpty()) {
            
            for (int i = 0; i < requiredEnchantmentsNbt.size(); i++) {
                NbtCompound enchantNbt = requiredEnchantmentsNbt.getCompound(i);
                String enchantId = enchantNbt.getString("id");
                int requiredLevel = enchantNbt.getShort("lvl");

                
                int actualLevel = 0;
                boolean foundEnchant = false;
                for (RegistryEntry<Enchantment> aEnchant : aEnchants.getEnchantments()) {
                    if (aEnchant.getIdAsString().equals(enchantId)) {
                        actualLevel = aEnchants.getLevel(aEnchant);
                        foundEnchant = true;
                        break;
                    }
                }

                
                if (requiredLevel == -1) {
                    if (!foundEnchant) {
                        return false;
                    }
                    matchedEnchantments.add(enchantId + "=" + actualLevel + " (требуется любой уровень)");
                } else {
                    
                    if (!foundEnchant || actualLevel < requiredLevel) {
                        return false;
                    }
                    matchedEnchantments.add(enchantId + "=" + actualLevel + " (требуется " + requiredLevel + ")");
                }
            }
        } else if (bEnchants != null && !bEnchants.isEmpty()) {
            
            for (RegistryEntry<Enchantment> bEnchant : bEnchants.getEnchantments()) {
                int requiredLevel = bEnchants.getLevel(bEnchant);
                int actualLevel = aEnchants.getLevel(bEnchant);

                if (actualLevel < requiredLevel) {
                    return false;
                }
                matchedEnchantments.add(bEnchant.getIdAsString() + "=" + actualLevel + " (требуется " + requiredLevel + ")");
            }
        }


        
        var aLore = a.get(DataComponentTypes.LORE);

        if (bLore != null && !bLore.lines().isEmpty()) {
            if (aLore == null || aLore.lines().isEmpty()) {
                return false;
            }

            List<String> aLoreStrings = aLore.lines().stream()
                    .map(text -> cleanString(text.getString()))
                    .filter(s -> !s.isEmpty())
                    .toList();

            java.util.List<String> matchedLore = new java.util.ArrayList<>();
            for (Text expectedLine : bLore.lines()) {
                String expectedStr = cleanString(expectedLine.getString());
                if (expectedStr.isEmpty()) continue;

                boolean found = aLoreStrings.stream()
                        .anyMatch(aLine -> aLine.contains(expectedStr) || expectedStr.contains(aLine));

                if (!found) {
                    return false;
                }
                matchedLore.add(expectedStr);
            }

        }

        return true;
    }

    
    public static boolean compareHolyWorldSphere(ItemStack a, ItemStack b) {
        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);

        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null) {
            return false;
        }

        
        String requiredSphereName = bNbt.getString("sphereName");
        String requiredEffects = bNbt.getString("requiredEffects");

        if (requiredSphereName == null || requiredSphereName.isEmpty()) {
            return false;
        }

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);
        if (aCustomData == null) {
            return false;
        }

        NbtCompound aNbt = aCustomData.copyNbt();
        if (aNbt == null) {
            return false;
        }

        if (!aNbt.contains("sphereEffect", NbtElement.COMPOUND_TYPE)) {
            
            if (aNbt.contains("sphereEffect", NbtElement.STRING_TYPE)) {
                
            }
            return false;
        }

        NbtCompound sphereEffect = aNbt.getCompound("sphereEffect");
        String sphereName = sphereEffect.getString("name");
        String effectsJson = sphereEffect.getString("effects");

        if (!requiredSphereName.equals(sphereName)) {
            return false;
        }

        String templateTexture = extractSkullTexture(b);
        if (templateTexture != null && !templateTexture.isEmpty()) {
            String candidateTexture = extractSkullTexture(a);
            if (candidateTexture == null || !templateTexture.equals(candidateTexture)) {
                return false;
            }
        }

        
        if (requiredEffects != null && !requiredEffects.isEmpty()) {
            if (effectsJson == null || effectsJson.isEmpty()) {
                return false;
            }

            
            String[] requiredParts = requiredEffects.split(",");
            for (String part : requiredParts) {
                String[] kv = part.trim().split(":");
                if (kv.length != 2) continue;

                String effectName = kv[0].trim();
                String effectLevel = kv[1].trim();

                
                boolean hasEffect = effectsJson.contains("\"nbtName\":\"" + effectName + "\"") &&
                        effectsJson.contains("\"lvl\":" + effectLevel);

                if (!hasEffect) {
                    return false;
                }
            }
            return true;
        }

        
        if (effectsJson != null && !effectsJson.isEmpty()) {
            int effectCount = 0;
            int idx = 0;
            while ((idx = effectsJson.indexOf("\"nbtName\"", idx)) != -1) {
                effectCount++;
                idx++;
            }

            
            if (effectCount <= 1) {
                return true;
            } else {
                
                return false;
            }
        }

        return true;
    }

    private static String extractSkullTexture(ItemStack stack) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }

        NbtCompound nbt = customData.copyNbt();
        if (nbt == null || !nbt.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
            return null;
        }

        NbtCompound skullOwner = nbt.getCompound("SkullOwner");
        if (!skullOwner.contains("Properties", NbtElement.COMPOUND_TYPE)) {
            return null;
        }

        NbtCompound properties = skullOwner.getCompound("Properties");
        if (!properties.contains("textures", NbtElement.LIST_TYPE)) {
            return null;
        }

        NbtList textures = properties.getList("textures", NbtElement.COMPOUND_TYPE);
        if (textures.isEmpty()) {
            return null;
        }

        NbtCompound textureEntry = textures.getCompound(0);
        return textureEntry.contains("Value", NbtElement.STRING_TYPE) ? textureEntry.getString("Value") : null;
    }

    
    private static boolean compareSphereEffectsToAttributes(String effectsJson, NbtList bAttributes) {
        if (effectsJson == null || effectsJson.isEmpty() || bAttributes.isEmpty()) {
            return false;
        }

        
        
        List<SphereEffect> effects = parseSphereEffects(effectsJson);
        if (effects.isEmpty()) {
            return false;
        }

        
        Set<AttributeInfo> bAttributeSet = new HashSet<>();
        for (int i = 0; i < bAttributes.size(); i++) {
            NbtCompound attr = bAttributes.getCompound(i);
            String attributeName = attr.getString("AttributeName");
            double amount = attr.getDouble("Amount");
            int operation = attr.getInt("Operation");
            String slot = attr.getString("Slot");
            bAttributeSet.add(new AttributeInfo(attributeName, amount, operation, slot));
        }

        
        for (AttributeInfo bAttr : bAttributeSet) {
            boolean found = false;
            for (SphereEffect effect : effects) {
                
                String mappedAttribute = mapEffectToAttribute(effect.nbtName);
                if (mappedAttribute == null) {
                    continue;
                }

                
                double mappedAmount = mapEffectLevelToAmount(effect.nbtName, effect.lvl);
                int mappedOperation = mapEffectToOperation(effect.nbtName);

                
                if (bAttr.attributeName.equals(mappedAttribute) &&
                        Math.abs(bAttr.amount - mappedAmount) < 0.01 &&
                        bAttr.operation == mappedOperation &&
                        bAttr.slot.equals("offhand")) { 
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false; 
            }
        }

        return true;
    }

    
    private static List<SphereEffect> parseSphereEffects(String json) {
        List<SphereEffect> effects = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return effects;
        }

        
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\\{\"lvl\":(\\d+),\"nbtName\":\"([^\"]+)\"\\}"
        );
        java.util.regex.Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            int lvl = Integer.parseInt(matcher.group(1));
            String nbtName = matcher.group(2);
            effects.add(new SphereEffect(nbtName, lvl));
        }

        return effects;
    }

    
    private static String mapEffectToAttribute(String nbtName) {
        switch (nbtName) {
            case "hms-damage":
                return "minecraft:generic.attack_damage";
            case "hms-rush":
            case "hms-speed":
                return "minecraft:generic.movement_speed";
            case "hms-armor":
                return "minecraft:generic.armor";
            case "hms-health":
                return "minecraft:generic.max_health";
            default:
                return null;
        }
    }

    
    private static double mapEffectLevelToAmount(String nbtName, int lvl) {
        switch (nbtName) {
            case "hms-damage":
                return (double) lvl; 
            case "hms-rush":
                return 0.1 * lvl; 
            case "hms-speed":
                return 0.1 * lvl; 
            case "hms-armor":
                return (double) lvl; 
            case "hms-health":
                return (double) lvl; 
            default:
                return 0.0;
        }
    }

    
    private static int mapEffectToOperation(String nbtName) {
        switch (nbtName) {
            case "hms-rush":
            case "hms-speed":
                return 1; 
            case "hms-damage":
            case "hms-armor":
            case "hms-health":
                return 0; 
            default:
                return 0;
        }
    }

    
    private static class SphereEffect {
        final String nbtName;
        final int lvl;

        SphereEffect(String nbtName, int lvl) {
            this.nbtName = nbtName;
            this.lvl = lvl;
        }
    }

    
    private static String normalizeAttributeName(String attributeName) {
        if (attributeName == null || attributeName.isEmpty()) {
            return attributeName;
        }
        if (!attributeName.contains(":")) {
            return "minecraft:" + attributeName;
        }
        return attributeName;
    }

    
    private static boolean attributeNamesMatch(String name1, String name2) {
        String normalized1 = normalizeAttributeName(name1);
        String normalized2 = normalizeAttributeName(name2);
        return normalized1.equals(normalized2) ||
                normalized2.equals(normalized1) ||
                normalized2.endsWith(normalized1) ||
                normalized1.endsWith(normalized2.replace("minecraft:", ""));
    }

    
    private static boolean compareAttributesNbtToComponents(NbtList bAttributes, AttributeModifiersComponent aModifiers) {
        
        Set<AttributeInfo> bAttributeSet = new HashSet<>();
        for (int i = 0; i < bAttributes.size(); i++) {
            NbtCompound attr = bAttributes.getCompound(i);
            String attributeName = attr.getString("AttributeName");
            double amount = attr.getDouble("Amount");
            int operation = attr.getInt("Operation");
            String slot = attr.getString("Slot");
            bAttributeSet.add(new AttributeInfo(attributeName, amount, operation, slot));
        }

        
        
        int aModifiersCount = 0;
        for (var ignored : aModifiers.modifiers()) {
            aModifiersCount++;
        }
        if (bAttributeSet.size() != aModifiersCount) {
            return false; 
        }

        
        for (AttributeInfo bAttr : bAttributeSet) {
            boolean found = false;

            for (var entry : aModifiers.modifiers()) {
                String attrId = entry.attribute().getIdAsString();
                double value = entry.modifier().value();
                int operation = entry.modifier().operation().getId();
                String slot = entry.slot().asString();

                
                
                
                boolean nameMatch = attributeNamesMatch(bAttr.attributeName, attrId);

                if (nameMatch &&
                        Math.abs(bAttr.amount - value) < 0.01 &&
                        bAttr.operation == operation &&
                        bAttr.slot.equals(slot)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false; 
            }
        }

        return true;
    }

    
    public static boolean compareHolyWorldExpBottle(ItemStack a, ItemStack b) {
        

        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);

        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("HolyWorldExpBottle")) {
            return false;
        }

        
        int requiredExpValue = bNbt.getInt("holy-exp-bottle-value");

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);

        if (aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null) {
                if (requiredExpValue == 0) {
                    
                    
                    if (!aNbt.contains("holy-exp-bottle-value")) {
                        
                        return true;
                    }
                    
                    int actualExpValue = aNbt.getInt("holy-exp-bottle-value");
                    
                    if (actualExpValue != 315 && actualExpValue != 1395 && actualExpValue != 5345 && actualExpValue != 30971) {
                        
                        return true;
                    }
                    return false;
                } else {
                    
                    if (aNbt.contains("holy-exp-bottle-value")) {
                        int actualExpValue = aNbt.getInt("holy-exp-bottle-value");
                        
                        if (actualExpValue == requiredExpValue) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        } else {
            
            if (requiredExpValue == 0) {
                
                return true;
            } else {
                return false;
            }
        }

        
        var components = a.getComponents();
        if (components != null) {
            String componentsStr = components.toString();
            if (requiredExpValue == 0) {
                
                if (!componentsStr.contains("holy-exp-bottle-value")) {
                    return true;
                }
                
                if (!componentsStr.contains("holy-exp-bottle-value:315") &&
                        !componentsStr.contains("holy-exp-bottle-value:1395") &&
                        !componentsStr.contains("holy-exp-bottle-value:5345") &&
                        !componentsStr.contains("holy-exp-bottle-value:30971")) {
                    return true;
                }
            } else {
                
                String searchPattern = "holy-exp-bottle-value:" + requiredExpValue;
                if (componentsStr.contains(searchPattern)) {
                    return true;
                }
            }
        }

        return false;
    }

    
    public static boolean compareHolyWorldBackpack(ItemStack a, ItemStack b) {
        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);

        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("HolyWorldBackpack")) {
            return false;
        }

        String requiredType = bNbt.getString("backpackType");
        if (requiredType == null || requiredType.isEmpty()) {
            return false;
        }

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);

        if (aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null) {
                
                if (aNbt.contains("PublicBukkitValues", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound bukkitValues = aNbt.getCompound("PublicBukkitValues");
                    if (bukkitValues.contains("litebackpacks:backpack")) {
                        String backpackType = bukkitValues.getString("litebackpacks:backpack");
                        if (requiredType.equals(backpackType)) {
                            return true;
                        }
                    }
                }
            }
        }

        
        var components = a.getComponents();
        if (components != null) {
            String componentsStr = components.toString();
            
            if (componentsStr.contains("litebackpacks:backpack") &&
                    componentsStr.contains("\"" + requiredType + "\"")) {
                return true;
            }
        }

        return false;
    }

    
    public static boolean compareHolyWorldPyrotechnic(ItemStack a, ItemStack b) {
        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);

        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("HolyWorldPyrotechnic")) {
            return false;
        }

        String requiredType = bNbt.getString("pyrotechnicType");
        if (requiredType == null || requiredType.isEmpty()) {
            return false;
        }

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);

        if (aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null) {
                
                if (aNbt.contains("pyrotechnic-item", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound pyroItem = aNbt.getCompound("pyrotechnic-item");
                    if (pyroItem.contains("name")) {
                        String pyroType = pyroItem.getString("name");
                        if (requiredType.equals(pyroType)) {
                            return true;
                        }
                    }
                }
            }
        }

        
        var components = a.getComponents();
        if (components != null) {
            String componentsStr = components.toString();
            
            if (componentsStr.contains("pyrotechnic-item") &&
                    componentsStr.contains("\"" + requiredType + "\"")) {
                return true;
            }
        }

        return false;
    }

    
    public static boolean compareHolyWorldKringe(ItemStack a, ItemStack b) {
        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);

        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("HolyWorldKringe")) {
            return false;
        }

        String requiredType = bNbt.getString("kringeType");
        if (requiredType == null || requiredType.isEmpty()) {
            return false;
        }

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);

        if (aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null) {
                
                if (aNbt.contains("kringeItems", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound kringeItem = aNbt.getCompound("kringeItems");
                    if (kringeItem.contains("type")) {
                        String kringeType = kringeItem.getString("type");
                        if (requiredType.equals(kringeType)) {
                            return true;
                        }
                    }
                }
            }
        }

        
        var components = a.getComponents();
        if (components != null) {
            String componentsStr = components.toString();
            
            if (componentsStr.contains("kringeItems") &&
                    componentsStr.contains("\"" + requiredType + "\"")) {
                return true;
            }
        }

        return false;
    }

    
    public static boolean compareHolyWorldRune(ItemStack a, ItemStack b) {
        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);

        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("HolyWorldRune")) {
            return false;
        }

        String requiredRuneId = bNbt.getString("runeId");
        if (requiredRuneId == null || requiredRuneId.isEmpty()) {
            return false;
        }

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);

        if (aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null) {
                
                if (aNbt.contains("PublicBukkitValues", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound bukkitValues = aNbt.getCompound("PublicBukkitValues");
                    if (bukkitValues.contains("literunes:rune-id")) {
                        String runeId = bukkitValues.getString("literunes:rune-id");
                        if (requiredRuneId.equals(runeId)) {
                            return true;
                        }
                    }
                }
            }
        }

        
        var components = a.getComponents();
        if (components != null) {
            String componentsStr = components.toString();
            if (componentsStr.contains("literunes:rune-id") &&
                    componentsStr.contains("\"" + requiredRuneId + "\"")) {
                return true;
            }
        }

        return false;
    }

    
    public static boolean compareHolyWorldTalik(ItemStack a, ItemStack b) {
        
        return compareTalismanByAttributes(a, b);
    }

    
    public static boolean compareHolyWorldSphereShard(ItemStack a, ItemStack b) {
        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);

        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("HolyWorldSphereShard")) {
            return false;
        }

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);

        if (aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null) {
                
                if (aNbt.contains("PublicBukkitValues", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound bukkitValues = aNbt.getCompound("PublicBukkitValues");
                    if (bukkitValues.contains("magicspheres:burned-sphere-shard")) {
                        
                        byte shardValue = bukkitValues.getByte("magicspheres:burned-sphere-shard");
                        if (shardValue != 0) {
                            
                            if (aNbt.contains("sphereEffect")) {
                                int sphereEffect = aNbt.getInt("sphereEffect");
                                if (sphereEffect == 1) {
                                    return true;
                                }
                            } else {
                                
                                return true;
                            }
                        }
                    }
                }
                
                if (aNbt.contains("sphereEffect")) {
                    int sphereEffect = aNbt.getInt("sphereEffect");
                    if (sphereEffect == 1) {
                        return true;
                    }
                }
            }
        }

        
        var components = a.getComponents();
        if (components != null) {
            String componentsStr = components.toString();
            if (componentsStr.contains("magicspheres:burned-sphere-shard") ||
                    componentsStr.contains("sphereEffect:1")) {
                return true;
            }
        }

        return false;
    }

    
    public static boolean compareHolyWorldKringeEffect(ItemStack a, ItemStack b) {
        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);

        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("HolyWorldKringeEffect")) {
            return false;
        }

        String requiredType = bNbt.getString("effectType");
        if (requiredType == null || requiredType.isEmpty()) {
            return false;
        }

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);

        if (aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null) {
                
                if (aNbt.contains("kringeEffect", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound kringeEffect = aNbt.getCompound("kringeEffect");
                    if (kringeEffect.contains("type")) {
                        String effectType = kringeEffect.getString("type");
                        if (requiredType.equals(effectType)) {
                            return true;
                        }
                    }
                }
            }
        }

        
        var components = a.getComponents();
        if (components != null) {
            String componentsStr = components.toString();
            if (componentsStr.contains("kringeEffect") &&
                    componentsStr.contains("\"" + requiredType + "\"")) {
                return true;
            }
        }

        return false;
    }

    
    public static boolean compareHolyWorldPotion(ItemStack a, ItemStack b) {
        

        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);
        if (bCustomData == null) return false;

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("HolyWorldPotion")) {
            return false;
        }

        String requiredEffectId = bNbt.getString("effectId");
        int requiredAmplifier = bNbt.getInt("amplifier");
        NbtList allowedDurationsList = bNbt.getList("allowedDurations", NbtElement.COMPOUND_TYPE);

        
        java.util.List<Integer> allowedDurations = new java.util.ArrayList<>();
        for (NbtElement element : allowedDurationsList) {
            if (element instanceof NbtCompound compound) {
                if (compound.contains("duration", NbtElement.INT_TYPE)) {
                    allowedDurations.add(compound.getInt("duration"));
                }
            }
        }

        

        
        var aPotion = a.get(DataComponentTypes.POTION_CONTENTS);
        if (aPotion == null) {
            return false;
        }

        
        var aPotionType = aPotion.potion();
        if (aPotionType.isPresent()) {
            String potionTypeId = aPotionType.get().getIdAsString();

            
            if (potionTypeId.equals("minecraft:strong_healing") &&
                    requiredEffectId.equals("minecraft:instant_health") &&
                    requiredAmplifier == 1) {
                return true;
            }
        }

        List<StatusEffectInstance> aEffectsList = new ArrayList<>();
        aPotion.getEffects().forEach(aEffectsList::add);

        if (aEffectsList.isEmpty()) {
            return false;
        }

        
        for (StatusEffectInstance aEffect : aEffectsList) {
            String effectId = aEffect.getEffectType().getIdAsString();
            int amplifier = aEffect.getAmplifier();

            if (effectId.equals(requiredEffectId) && amplifier == requiredAmplifier) {
                
                int aDuration = aEffect.getDuration();

                if (allowedDurations.isEmpty()) {
                    
                    return true;
                }

                
                for (Integer allowedDuration : allowedDurations) {
                    int diff = Math.abs(aDuration - allowedDuration);
                    if (diff <= 20) { 
                        return true;
                    }
                }
            }
        }

        return false;
    }

    
    public static boolean compareHolyWorldStandardPotion(ItemStack a, ItemStack b) {
        

        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);
        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null) {
            return false;
        }

        if (!bNbt.getBoolean("HolyWorldStandardPotion")) {
            return false;
        }

        String requiredPotionType = bNbt.getString("potionType");
        if (requiredPotionType == null || requiredPotionType.isEmpty()) {
            return false;
        }

        
        var aPotion = a.get(DataComponentTypes.POTION_CONTENTS);
        if (aPotion == null) {
            
            var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);
            if (aCustomData != null) {
                NbtCompound aNbt = aCustomData.copyNbt();
                if (aNbt != null && aNbt.contains("Potion", NbtElement.STRING_TYPE)) {
                    String nbtPotionType = aNbt.getString("Potion");
                    if (nbtPotionType.equals(requiredPotionType)) {
                        return true;
                    }
                }
            }
            return false;
        }

        
        var aPotionType = aPotion.potion();
        if (aPotionType.isEmpty()) {
            
            var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);
            if (aCustomData != null) {
                NbtCompound aNbt = aCustomData.copyNbt();
                if (aNbt != null && aNbt.contains("Potion", NbtElement.STRING_TYPE)) {
                    String nbtPotionType = aNbt.getString("Potion");
                    if (nbtPotionType.equals(requiredPotionType)) {
                        return true;
                    }
                }
            }
            return false;
        }

        String aPotionTypeId = aPotionType.get().getIdAsString();

        if (aPotionTypeId.equals(requiredPotionType)) {
            return true;
        }

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);
        if (aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null && aNbt.contains("Potion", NbtElement.STRING_TYPE)) {
                String nbtPotionType = aNbt.getString("Potion");
                if (nbtPotionType.equals(requiredPotionType)) {
                    return true;
                }
            }
        }

        return false;
    }

    
    public static boolean compareHolyWorldMultiEffectPotion(ItemStack a, ItemStack b) {
        

        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);
        if (bCustomData == null) return false;

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("HolyWorldMultiEffectPotion")) {
            return false;
        }

        NbtList requiredEffectsList = bNbt.getList("effects", NbtElement.COMPOUND_TYPE);
        if (requiredEffectsList.isEmpty()) {
            return false;
        }

        
        var aPotion = a.get(DataComponentTypes.POTION_CONTENTS);
        if (aPotion == null) {
            return false;
        }

        List<StatusEffectInstance> aEffectsList = new ArrayList<>();
        aPotion.getEffects().forEach(aEffectsList::add);

        if (aEffectsList.isEmpty()) {
            return false;
        }

        
        int matchedCount = 0;
        for (NbtElement element : requiredEffectsList) {
            if (!(element instanceof NbtCompound effectNbt)) continue;

            String requiredEffectId = effectNbt.getString("effectId");
            int requiredAmplifier = effectNbt.getInt("amplifier");
            int requiredDuration = effectNbt.getInt("duration");

            boolean found = false;
            for (StatusEffectInstance aEffect : aEffectsList) {
                String effectId = aEffect.getEffectType().getIdAsString();
                int amplifier = aEffect.getAmplifier();
                int duration = aEffect.getDuration();

                if (effectId.equals(requiredEffectId) && amplifier == requiredAmplifier) {
                    
                    int diff = Math.abs(duration - requiredDuration);
                    if (diff <= 20 || requiredDuration == 0) { 
                        found = true;
                        matchedCount++;
                        break;
                    }
                }
            }

            if (!found) {
                return false;
            }
        }

        if (matchedCount == requiredEffectsList.size()) {
            return true;
        }

        return false;
    }

    
    public static boolean compareSpookyTimeSpecial(ItemStack a, ItemStack b) {
        

        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);
        if (bCustomData == null) return false;

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("SpookyTimeSpecial")) {
            return false;
        }

        String requiredSpookyItemType = bNbt.getString("spookyItemType");
        if (requiredSpookyItemType == null || requiredSpookyItemType.isEmpty()) {
            return false;
        }

        
        if (a.getItem() != b.getItem()) {
            return false;
        }

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);
        if (aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null) {
                
                if (aNbt.contains("PublicBukkitValues", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound bukkitValues = aNbt.getCompound("PublicBukkitValues");
                    
                    for (String key : bukkitValues.getKeys()) {
                        if (key.endsWith(requiredSpookyItemType)) {
                            return true;
                        }
                    }
                }

                
                if (aNbt.contains("spookyItemType", NbtElement.STRING_TYPE)) {
                    String actualSpookyItemType = aNbt.getString("spookyItemType");
                    if (actualSpookyItemType.endsWith(requiredSpookyItemType)) {
                        return true;
                    }
                }

                
                var components = a.getComponents();
                if (components != null) {
                    String componentsStr = components.toString();
                    
                    if (componentsStr.contains(requiredSpookyItemType)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    
    public static boolean compareSpookyTimePotion(ItemStack a, ItemStack b) {
        

        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);
        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null || !bNbt.getBoolean("SpookyTimePotion")) {
            return false;
        }

        NbtList requiredEffectsList = bNbt.getList("effects", NbtElement.COMPOUND_TYPE);
        if (requiredEffectsList.isEmpty()) {
            return false;
        }

        
        var aPotion = a.get(DataComponentTypes.POTION_CONTENTS);
        if (aPotion == null) {
            return false;
        }

        List<StatusEffectInstance> aEffectsList = new ArrayList<>();
        aPotion.getEffects().forEach(aEffectsList::add);

        if (aEffectsList.isEmpty()) {
            return false;
        }

        
        int matchedCount = 0;
        for (NbtElement element : requiredEffectsList) {
            if (!(element instanceof NbtCompound effectNbt)) continue;

            String requiredEffectId = effectNbt.getString("effectId");
            int requiredAmplifier = effectNbt.getInt("amplifier");
            int requiredDuration = effectNbt.getInt("duration");

            boolean found = false;
            for (StatusEffectInstance aEffect : aEffectsList) {
                String effectId = aEffect.getEffectType().getIdAsString();
                int amplifier = aEffect.getAmplifier();
                int duration = aEffect.getDuration();

                if (effectId.equals(requiredEffectId) && amplifier == requiredAmplifier) {
                    
                    int diff = Math.abs(duration - requiredDuration);
                    if (diff <= 20 || requiredDuration == 0) { 
                        found = true;
                        matchedCount++;
                        break;
                    }
                }
            }

            if (!found) {
                return false;
            }
        }

        return matchedCount == requiredEffectsList.size();
    }

    
    public static boolean compareTalismanByAttributes(ItemStack a, ItemStack b) {
        var bCustomData = b.get(DataComponentTypes.CUSTOM_DATA);
        if (bCustomData == null) {
            return false;
        }

        NbtCompound bNbt = bCustomData.copyNbt();
        if (bNbt == null) {
            return false;
        }

        
        NbtList bAttributes = bNbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
        if (bAttributes.isEmpty()) {
            return false;
        }

        
        var aCustomData = a.get(DataComponentTypes.CUSTOM_DATA);
        NbtList aAttributes = null;

        if (aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null) {
                aAttributes = aNbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
            }
        }

        
        AttributeModifiersComponent aModifiers = a.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if ((aAttributes == null || aAttributes.isEmpty()) && aModifiers != null) {
            
            boolean result = compareAttributesNbtToComponents(bAttributes, aModifiers);
            return result;
        }

        
        
        if (aAttributes == null || aAttributes.isEmpty()) {
            
            if (aCustomData != null) {
                NbtCompound aNbt = aCustomData.copyNbt();
                if (aNbt != null && aNbt.contains("sphereEffect", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound sphereEffect = aNbt.getCompound("sphereEffect");
                    if (sphereEffect.contains("effects", NbtElement.STRING_TYPE)) {
                        String effectsJson = sphereEffect.getString("effects");
                        
                        boolean result = compareSphereEffectsToAttributes(effectsJson, bAttributes);
                        return result;
                    }
                }
            }
            return false;
        }

        
        if (aAttributes.size() < bAttributes.size() && aCustomData != null) {
            NbtCompound aNbt = aCustomData.copyNbt();
            if (aNbt != null && aNbt.contains("sphereEffect", NbtElement.COMPOUND_TYPE)) {
                NbtCompound sphereEffect = aNbt.getCompound("sphereEffect");
                if (sphereEffect.contains("effects", NbtElement.STRING_TYPE)) {
                    String effectsJson = sphereEffect.getString("effects");
                    
                    boolean result = compareSphereEffectsToAttributes(effectsJson, bAttributes);
                    return result;
                }
            }
        }

        
        java.util.Set<AttributeInfo> bAttributeSet = new java.util.HashSet<>();
        for (int i = 0; i < bAttributes.size(); i++) {
            NbtCompound attr = bAttributes.getCompound(i);
            String attributeName = attr.getString("AttributeName");
            double amount = attr.getDouble("Amount");
            int operation = attr.getInt("Operation");
            String slot = attr.getString("Slot");

            bAttributeSet.add(new AttributeInfo(attributeName, amount, operation, slot));
        }

        
        java.util.List<String> matchedAttributes = new java.util.ArrayList<>();
        for (AttributeInfo bAttr : bAttributeSet) {
            boolean found = false;
            for (int i = 0; i < aAttributes.size(); i++) {
                NbtCompound attr = aAttributes.getCompound(i);
                String attributeName = attr.getString("AttributeName");
                double amount = attr.getDouble("Amount");
                int operation = attr.getInt("Operation");
                String slot = attr.getString("Slot");

                
                boolean nameMatch = attributeNamesMatch(bAttr.attributeName, attributeName);
                boolean amountMatch = Math.abs(bAttr.amount - amount) < 0.01; 
                boolean operationMatch = bAttr.operation == operation;
                boolean slotMatch = bAttr.slot.equals(slot);

                if (nameMatch && operationMatch && slotMatch && amountMatch) {
                    found = true;
                    matchedAttributes.add(attributeName + "=" + amount + " (op=" + operation + ", slot=" + slot + ")");
                    break;
                }
            }

            if (!found) {
                return false; 
            }
        }

        return true;
    }

    
    private static class AttributeInfo {
        final String attributeName;
        final double amount;
        final int operation;
        final String slot;

        AttributeInfo(String attributeName, double amount, int operation, String slot) {
            this.attributeName = attributeName;
            this.amount = amount;
            this.operation = operation;
            this.slot = slot;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AttributeInfo that = (AttributeInfo) o;
            return Double.compare(that.amount, amount) == 0 &&
                    operation == that.operation &&
                    attributeName.equals(that.attributeName) &&
                    slot.equals(that.slot);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(attributeName, amount, operation, slot);
        }
    }

}
