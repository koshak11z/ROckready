package im.zov4ik.features.impl.misc.autobuy.catalog.items.list;

import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.settings.AutoBuyItemSettings;
import im.zov4ik.features.impl.misc.autobuy.catalog.settings.AutoBuySettingsManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.component.type.PotionContentsComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


public class HolyWorldProvider {

    private static List<AutoBuyableItem> items = null;

    public static List<AutoBuyableItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();

            

            
            items.add(new HolyWorldItem(
                    "Шлем Infinity",
                    Items.NETHERITE_HELMET,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.AQUA_AFFINITY, 1),
                            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
                            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROTECTION, 5),
                            new EnchantmentData(Enchantments.RESPIRATION, 3),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(Text.literal("Непробиваемый II").formatted(Formatting.GRAY))
            ));

            
            items.add(new HolyWorldItem(
                    "Нагрудник Infinity",
                    Items.NETHERITE_CHESTPLATE,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
                            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROTECTION, 5),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(Text.literal("Непробиваемый II").formatted(Formatting.GRAY))
            ));

            
            items.add(new HolyWorldItem(
                    "Поножи Infinity",
                    Items.NETHERITE_LEGGINGS,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
                            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROTECTION, 5),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(Text.literal("Непробиваемый II").formatted(Formatting.GRAY))
            ));

            
            items.add(new HolyWorldItem(
                    "Ботинки Infinity",
                    Items.NETHERITE_BOOTS,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
                            new EnchantmentData(Enchantments.DEPTH_STRIDER, 3),
                            new EnchantmentData(Enchantments.FEATHER_FALLING, 4),
                            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROTECTION, 5),
                            new EnchantmentData(Enchantments.SOUL_SPEED, 3),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(Text.literal("Непробиваемый II").formatted(Formatting.GRAY))
            ));

            

            
            items.add(new HolyWorldItem(
                    "Шлем Eternity",
                    Items.NETHERITE_HELMET,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.AQUA_AFFINITY, 1),
                            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
                            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROTECTION, 5),
                            new EnchantmentData(Enchantments.RESPIRATION, 3),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(Text.literal("Непробиваемый I").formatted(Formatting.GRAY))
            ));

            
            items.add(new HolyWorldItem(
                    "Нагрудник Eternity",
                    Items.NETHERITE_CHESTPLATE,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
                            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROTECTION, 5),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(Text.literal("Непробиваемый I").formatted(Formatting.GRAY))
            ));

            
            items.add(new HolyWorldItem(
                    "Штаны Eternity",
                    Items.NETHERITE_LEGGINGS,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
                            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROTECTION, 5),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(Text.literal("Непробиваемый I").formatted(Formatting.GRAY))
            ));

            
            items.add(new HolyWorldItem(
                    "Ботинки Eternity",
                    Items.NETHERITE_BOOTS,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
                            new EnchantmentData(Enchantments.DEPTH_STRIDER, 3),
                            new EnchantmentData(Enchantments.FEATHER_FALLING, 4),
                            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROTECTION, 5),
                            new EnchantmentData(Enchantments.SOUL_SPEED, 3),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(Text.literal("Непробиваемый I").formatted(Formatting.GRAY))
            ));

            

            
            items.add(new HolyWorldItem(
                    "Шлем солнца",
                    Items.GOLDEN_HELMET,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.AQUA_AFFINITY, 1),
                            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
                            new EnchantmentData(Enchantments.PROTECTION, 5),
                            new EnchantmentData(Enchantments.RESPIRATION, 3)
                    },
                    List.of(Text.literal("Непробиваемый II").formatted(Formatting.GRAY))
            ));

            
            
            
            items.add(new HolyWorldItem(
                    "Броневая элитра",
                    Items.ELYTRA,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.UNBREAKING, 4)
                    },
                    new AttributeData[]{
                            new AttributeData("minecraft:generic.armor", 8.0, 0, "chest")  
                    },
                    null  
            ));

            items.add(new HolyWorldItem(
                    "Элитры",
                    Items.ELYTRA,
                    0,
                    null, 
                    null  
            ));

            
            items.add(new HolyWorldItem(
                    "Меч Eternity",
                    Items.NETHERITE_SWORD,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.BANE_OF_ARTHROPODS, 7),
                            new EnchantmentData(Enchantments.FIRE_ASPECT, 2),
                            new EnchantmentData(Enchantments.LOOTING, 5),
                            new EnchantmentData(Enchantments.MENDING, 1),
                            new EnchantmentData(Enchantments.SHARPNESS, 7),
                            new EnchantmentData(Enchantments.SMITE, 7),
                            new EnchantmentData(Enchantments.SWEEPING_EDGE, 3),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(
                            Text.literal("Богач I").formatted(Formatting.GRAY),
                            Text.literal("Разрушитель II").formatted(Formatting.GRAY),
                            Text.literal("Критический II").formatted(Formatting.GRAY)
                    )
            ));

            items.add(new HolyWorldItem(
                    "Меч Infinity",
                    Items.NETHERITE_SWORD,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.BANE_OF_ARTHROPODS, 7),
                            new EnchantmentData(Enchantments.FIRE_ASPECT, 2),
                            new EnchantmentData(Enchantments.LOOTING, 5),
                            new EnchantmentData(Enchantments.MENDING, 1),
                            new EnchantmentData(Enchantments.SHARPNESS, 8),
                            new EnchantmentData(Enchantments.SMITE, 7),
                            new EnchantmentData(Enchantments.SWEEPING_EDGE, 3),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(
                            Text.literal("Богач VI").formatted(Formatting.GRAY),
                            Text.literal("Разрушитель II").formatted(Formatting.GRAY),
                            Text.literal("Критический II").formatted(Formatting.GRAY)
                    )
            ));

            
            items.add(new HolyWorldItem(
                    "Кирка Eternity",
                    Items.NETHERITE_PICKAXE,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.EFFICIENCY, 10),
                            new EnchantmentData(Enchantments.FORTUNE, 5),
                            new EnchantmentData(Enchantments.MENDING, 1),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    List.of(
                            Text.literal("Магнетизм I").formatted(Formatting.GRAY),
                            Text.literal("Неразрушимость I").formatted(Formatting.GRAY),
                            Text.literal("Автоплавка").formatted(Formatting.GRAY),
                            Text.literal("Опытный III").formatted(Formatting.GRAY),
                            Text.literal("Бур II").formatted(Formatting.GRAY)
                    )
            ));

            
            items.add(new HolyWorldItem(
                    "Арбалет Eternity",
                    Items.CROSSBOW,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.MULTISHOT, 1),
                            new EnchantmentData(Enchantments.PIERCING, 5),
                            new EnchantmentData(Enchantments.QUICK_CHARGE, 3),
                            new EnchantmentData(Enchantments.UNBREAKING, 3)
                    },
                    List.of(Text.literal("Оглушение II").formatted(Formatting.GRAY))
            ));

            
            items.add(new HolyWorldItem(
                    "Громовержец",
                    Items.TRIDENT,
                    0,
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.IMPALING, 5),
                            new EnchantmentData(Enchantments.LOOTING, 5),
                            new EnchantmentData(Enchantments.LOYALTY, 3),
                            new EnchantmentData(Enchantments.MENDING, 1),
                            new EnchantmentData(Enchantments.UNBREAKING, 5)
                    },
                    null 
            ));

            
            
            
            
            
            
            items.add(new HolyWorldSphereItem(
                    "Сфера Цербера",
                    "Сфера Цербера", 
                    Items.PLAYER_HEAD,
                    0,
                    null, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA5NWE3ZmQ5MGRhYTFiYmU3MDY5MDg5NzQwZTA1ZDBiZmM2NjI5NmVlM2M0MGVlNzFhNGUwYTY2MTZiMmJiYyJ9fX0=",
                    "Cerber", 
                    "hms-damage:5,hms-rush:1" 
            ));

            
            
            
            items.add(new HolyWorldSphereItem(
                    "Сфера Флеша",
                    "Сфера Флеша", 
                    Items.PLAYER_HEAD,
                    0, 
                    null, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzc0MDBlYTE5ZGJkODRmNzVjMzlhZDY4MjNhYzRlZjc4NmYzOWY0OGZjNmY4NDYwMjM2NmFjMjliODM3NDIyIn19fQ==",
                    "Flash", 
                    "hms-speed:3,hms-armor:1" 
            ));



            
            
            
            items.add(new HolyWorldSphereItem(
                    "Сфера ɪᴍᴍᴏʀᴛᴀʟɪᴛʏ",
                    "Сфера Имморталити", 
                    Items.PLAYER_HEAD,
                    0, 
                    null, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODNlZDRjZTIzOTMzZTY2ZTA0ZGYxNjA3MDY0NGY3NTk5ZWViNTUzMDdmN2VhZmU4ZDkyZjQwZmIzNTIwODYzYyJ9fX0=",
                    "Immortal", 
                    "hms-speed:2,hms-damage:3" 
            ));

            
            
            
            items.add(new HolyWorldSphereItem(
                    "Сфера ᴀʀᴍᴏʀᴛᴀʟɪᴛʏ",
                    "Сфера Арморталити", 
                    Items.PLAYER_HEAD,
                    0, 
                    null, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWE2MmI5ZGU2YTI2Yjg2ODY5Y2EyMmVhNDBmMWJkZTgwYTA0MzBhNTQ1NDdiZWNjZThmZGE4NzA3Nzc3MjU4ZiJ9fX0=",
                    "Armortality", 
                    "hms-armor:2,hms-damage:2,hms-health:2" 
            ));

            
            


            
            
            
            items.add(new HolyWorldSphereItem(
                    "Сфера на Скорость III",
                    "Сфера на скорость 3", 
                    Items.PLAYER_HEAD,
                    0, 
                    null, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM5MzY1NjQyYzZlZGRjZmVkZjViNWUxNGUyYmM3MTI1N2Q5ZTRhMzM2M2QxMjNjNmYzM2M1NWNhZmJmNmQifX19",
                    "Speed3" 
            ));



            
            
            
            items.add(new HolyWorldSphereItem(
                    "Сфера Eternity",
                    "Сфера Eternity", 
                    Items.PLAYER_HEAD,
                    0, 
                    null, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM5MzY1NjQyYzZlZGRjZmVkZjViNWUxNGUyYmM3MTI1N2Q5ZTRhMzM2M2QxMjNjNmYzM2M1NWNhZmJmNmQifX19",
                    "Eternity", 
                    "hms-speed:2,hms-damage:2,hms-armor:2" 
            ));

            
            
            
            items.add(new HolyWorldSphereItem(
                    "Сфера Stinger",
                    "Сфера Stinger", 
                    Items.PLAYER_HEAD,
                    0, 
                    null, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM5MzY1NjQyYzZlZGRjZmVkZjViNWUxNGUyYmM3MTI1N2Q5ZTRhMzM2M2QxMjNjNmYzM2M1NWNhZmJmNmQifX19",
                    "Stinger", 
                    "hms-speed:1,hms-armor:2,hms-damage:2" 
            ));



            
            
            
            items.add(new HolyWorldSphereItem(
                    "Сфера на броня III скорость II",
                    "Сфера на броня 3", 
                    Items.PLAYER_HEAD,
                    0, 
                    null, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmFmZjJlYjQ5OGU1YzZhMDQ0ODRmMGM5Zjc4NWI0NDg0NzlhYjIxM2RmOTVlYzkxMTc2YTMwOGExMmFkZDcwIn19fQ==",
                    "Mythical3", 
                    "hms-armor:3,hms-speed:2" 
            ));

            
            
            
            items.add(new HolyWorldSphereItem(
                    "Сфера на урон II броня III",
                    "Сфера на броня 3", 
                    Items.PLAYER_HEAD,
                    0, 
                    null, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmFmZjJlYjQ5OGU1YzZhMDQ0ODRmMGM5Zjc4NWI0NDg0NzlhYjIxM2RmOTVlYzkxMTc2YTMwOGExMmFkZDcwIn19fQ==",
                    "Speed", 
                    "hms-armor:3,hms-damage:2" 
            ));

            items.add(new HolyWorldSphereItem(
                    "Сфера на броня II урон III",
                    "Сфера на броня 3", 
                    Items.PLAYER_HEAD,
                    0, 
                    null, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmFmZjJlYjQ5OGU1YzZhMDQ0ODRmMGM5Zjc4NWI0NDg0NzlhYjIxM2RmOTVlYzkxMTc2YTMwOGExMmFkZDcwIn19fQ==",
                    "Mythical1", 
                    "hms-armor:2,hms-damage:3" 
            ));

            
            
            
            items.add(new HolyWorldTalikItem(
                    "Талисман Stinger",
                    Items.TOTEM_OF_UNDYING,
                    0,
                    new AttributeData[]{
                            new AttributeData("minecraft:generic.movement_speed", 0.1, 1, "offhand"), 
                            new AttributeData("minecraft:generic.attack_damage", 2.0, 0, "offhand"), 
                            new AttributeData("minecraft:generic.armor", 2.0, 0, "offhand")  
                    },
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.UNBREAKING, 1)
                    }
            ));

            
            
            
            items.add(new HolyWorldTalikItem(
                    "Талисман Infinity",
                    Items.TOTEM_OF_UNDYING,
                    0,
                    new AttributeData[]{
                            new AttributeData("minecraft:generic.attack_damage", 2.0, 0, "offhand"), 
                            new AttributeData("minecraft:generic.armor", 2.0, 0, "offhand"), 
                            new AttributeData("minecraft:generic.movement_speed", 0.2, 1, "offhand"), 
                            new AttributeData("minecraft:generic.max_health", 2.0, 0, "offhand")  
                    },
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.UNBREAKING, 1)
                    }
            ));

            
            
            
            items.add(new HolyWorldTalikItem(
                    "Талисман Eternity",
                    Items.TOTEM_OF_UNDYING,
                    0,
                    new AttributeData[]{
                            new AttributeData("minecraft:generic.attack_damage", 2.0, 0, "offhand"), 
                            new AttributeData("minecraft:generic.armor", 2.0, 0, "offhand"), 
                            new AttributeData("minecraft:generic.movement_speed", 0.2, 1, "offhand")  
                    },
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.UNBREAKING, 1)
                    }
            ));

            
            
            
            items.add(new HolyWorldTalikItem(
                    "Легендарный талисман",
                    Items.TOTEM_OF_UNDYING,
                    0,
                    new AttributeData[]{
                            new AttributeData("minecraft:generic.armor", 2.0, 0, "offhand"), 
                            new AttributeData("minecraft:generic.attack_damage", 2.0, 0, "offhand")  
                    },
                    new EnchantmentData[]{
                            new EnchantmentData(Enchantments.UNBREAKING, 1)
                    }
            ));

            
            items.add(new HolyWorldItem(
                    "Тотем бессмертия",
                    Items.TOTEM_OF_UNDYING,
                    0,
                    null, 
                    null  
            ));

            

            
            items.add(new HolyWorldExpBottleItem(
                    "Пузырек с 15 уровнем",
                    "15", 
                    Items.EXPERIENCE_BOTTLE,
                    0, 
                    315 
            ));

            items.add(new HolyWorldExpBottleItem(
                    "Пузырек с 30 уровнем",
                    "30",
                    Items.EXPERIENCE_BOTTLE,
                    0,
                    1395
            ));

            
            
            items.add(new HolyWorldExpBottleItem(
                    "Пузырек с 50 уровнем",
                    "50", 
                    Items.EXPERIENCE_BOTTLE,
                    0, 
                    5345 
            ));

            
            items.add(new HolyWorldExpBottleItem(
                    "Пузырек с 100 уровнем",
                    "100", 
                    Items.EXPERIENCE_BOTTLE,
                    0, 
                    30971 
            ));

            
            
            items.add(new HolyWorldExpBottleItem(
                    "Обычный пузырек опыта",
                    "опыт", 
                    Items.EXPERIENCE_BOTTLE,
                    0, 
                    0 
            ));

            


            
            
            items.add(new HolyWorldBackpackItem(
                    "Рюкзак I уровень",
                    "рюкзак 1 уровень", 
                    Items.PINK_SHULKER_BOX,
                    0, 
                    "mini" 
            ));

            
            
            items.add(new HolyWorldBackpackItem(
                    "Рюкзак II уровень",
                    "рюкзак 2 уровень", 
                    Items.LIGHT_BLUE_SHULKER_BOX,
                    0, 
                    "normal" 
            ));

            
            
            items.add(new HolyWorldBackpackItem(
                    "Рюкзак III уровень",
                    "рюкзак 3 уровень", 
                    Items.RED_SHULKER_BOX,
                    0, 
                    "big" 
            ));


            
            
            items.add(new HolyWorldBackpackItem(
                    "Рюкзак IV уровень",
                    "рюкзак 4 уровень", 
                    Items.MAGENTA_SHULKER_BOX,
                    0, 
                    "huge" 
            ));

            
            
            items.add(new HolyWorldBackpackItem(
                    "Рюкзак Infinity",
                    "рюкзак infinity", 
                    Items.LIME_SHULKER_BOX,
                    0, 
                    "infinity" 
            ));


            

            
            
            items.add(new HolyWorldPyrotechnicItem(
                    "Трапка",
                    "Трапка", 
                    Items.POPPED_CHORUS_FRUIT,
                    0, 
                    "ALTERNATIVE_TRAP" 
            ));

            
            
            items.add(new HolyWorldPyrotechnicItem(
                    "Взрывная трапка",
                    "Взрывная трапка", 
                    Items.PRISMARINE_SHARD,
                    0, 
                    "EXPLOSIVE_TRAP" 
            ));

            
            
            items.add(new HolyWorldPyrotechnicItem(
                    "Стан",
                    "Стан", 
                    Items.NETHER_STAR,
                    0, 
                    "STUN_STAR" 
            ));

            

            
            
            items.add(new HolyWorldKringeItem(
                    "Взрывная штучка",
                    "Взрывная штучка", 
                    Items.FIRE_CHARGE,
                    0, 
                    "ExplosiveStuff" 
            ));

            
            
            items.add(new HolyWorldKringeItem(
                    "Ком снега",
                    "Ком снега", 
                    Items.SNOWBALL,
                    0, 
                    "SnowBall" 
            ));

            

            
            
            items.add(new HolyWorldRuneItem(
                    "Руна «Бессмертие»",
                    "Бессмертие", 
                    Items.ORANGE_DYE,
                    0, 
                    "immortality" 
            ));

            

            
            items.add(new HolyWorldPotionItem(
                    "Улучшенное зелье силы",
                    "Улучшенное зелье силы", 
                    Items.POTION,
                    0, 
                    StatusEffects.STRENGTH,
                    2, 
                    List.of(3600, 7200) 
            ));

            
            items.add(new HolyWorldPotionItem(
                    "Улучшенное зелье скорости",
                    "Улучшенное зелье скорости", 
                    Items.POTION,
                    0, 
                    StatusEffects.SPEED,
                    2, 
                    List.of(3600) 
            ));

            
            items.add(new HolyWorldKringeItem(
                    "Зелье победителя",
                    "Зелье победителя", 
                    Items.POTION,
                    0, 
                    "win-potion", 
                    33461 
            ));

            
            
            items.add(new HolyWorldPotionItem(
                    "Зелье исцеления",
                    "Зелье исцеление", 
                    Items.POTION,
                    0, 
                    StatusEffects.INSTANT_HEALTH,
                    1, 
                    List.of(1), 
                    0xFF0000 
            ));

            
            items.add(new HolyWorldStandardPotionItem(
                    "Зелье черепашьей мощи",
                    "Зелье черепашьей мощи", 
                    Items.POTION,
                    0, 
                    "minecraft:long_turtle_master", 
                    0x7FB4B8 
            ));

            
            items.add(new HolyWorldStandardPotionItem(
                    "Зелье черепашьей мощи II",
                    "Зелье черепашьей мощи", 
                    Items.POTION,
                    0, 
                    "minecraft:strong_turtle_master", 
                    0x7FB4B8 
            ));

            

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Охотник",
                    "Охотник", 
                    Items.NETHERITE_SWORD,
                    0, 
                    "EXP_DROPPER" 
            ));

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Снеговик",
                    "Снеговик", 
                    Items.SNOW_BLOCK,
                    0, 
                    "BLINDNESS" 
            ));

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Иллюминатор",
                    "Иллюминатор", 
                    Items.SEA_LANTERN,
                    0, 
                    "PORTHOLE" 
            ));

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Эндермен",
                    "Эндермен", 
                    Items.ENDER_PEARL,
                    0, 
                    "ENDERMAN" 
            ));

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Анти Фантом",
                    "Анти Фантом", 
                    Items.PHANTOM_MEMBRANE,
                    0, 
                    "ANTI_PHANTOM" 
            ));

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Телекинез",
                    "Телекинез", 
                    Items.HONEY_BLOCK,
                    0, 
                    "TELEKINESIS" 
            ));

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Гравитация",
                    "Гравитация", 
                    Items.FEATHER,
                    0, 
                    "GRAVITY" 
            ));

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Вампиризм",
                    "Вампиризм", 
                    Items.WITHER_SKELETON_SKULL,
                    0, 
                    "VAMPIRE" 
            ));

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Справедливость",
                    "Справедливость", 
                    Items.POTION,
                    0, 
                    "JUSTICE" 
            ));

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Универсальный ключ",
                    "Универсальный ключ", 
                    Items.TRIPWIRE_HOOK,
                    0, 
                    "UNIVERSAL_KEY" 
            ));

            
            
            items.add(new HolyWorldKringeEffectItem(
                    "Фармер",
                    "Фармер", 
                    Items.DIAMOND_SWORD,
                    0, 
                    "FARMER" 
            ));

            items.add(new HolyWorldItem(
                    "Зачарованное золотое яблоко",
                    Items.ENCHANTED_GOLDEN_APPLE,
                    0,
                    null, 
                    null  
            ));


            items.add(new HolyWorldItem(
                    "Золотое яблоко",
                    Items.GOLDEN_APPLE,
                    0,
                    null, 
                    null  
            ));


            items.add(new HolyWorldItem(
                    "Золотая морковь",
                    Items.GOLDEN_CARROT,
                    0,
                    null, 
                    null  
            ));

            items.add(new HolyWorldItem(
                    "Эндер жемчуг",
                    "Эндер-жемчуг", 
                    Items.ENDER_PEARL,
                    0,
                    null, 
                    null, 
                    null  
            ));

            items.add(new HolyWorldItem(
                    "Плод хоруса",
                    Items.CHORUS_FRUIT,
                    0,
                    null, 
                    null  
            ));

            items.add(new HolyWorldItem(
                    "Осколочное яйцо",
                    Items.PUFFERFISH_SPAWN_EGG,
                    0,
                    null,
                    List.of(
                            Text.literal("получайте осколочные яйца").formatted(Formatting.GRAY),
                            Text.literal("добывая вещи из посылок").formatted(Formatting.GRAY),
                            Text.literal("можно обменять на яйца призыва").formatted(Formatting.GRAY),
                            Text.literal("у специального NPC на спавне").formatted(Formatting.GRAY)
                    )
            ));

            items.add(new HolyWorldItem(
                    "Осколочное эндер яйцо",
                    Items.ENDERMAN_SPAWN_EGG,
                    0,
                    null,
                    List.of(
                            Text.literal("получайте такие осколочные эндер яйца").formatted(Formatting.GRAY),
                            Text.literal("исследуя и сражаясь за Эндер-мир").formatted(Formatting.GRAY),
                            Text.literal("можно обменять на ценное Яйцо Дракона").formatted(Formatting.GRAY),
                            Text.literal("у специального NPC на спавне").formatted(Formatting.GRAY)
                    )
            ));

            items.add(new HolyWorldItem(
                    "Яйцо Дракона",
                    Items.DRAGON_EGG,
                    0,
                    null,
                    List.of(
                            Text.literal("каждые 3 минуты стоимость яйца").formatted(Formatting.GRAY),
                            Text.literal("повышается на 1 слиток (если в сундучке)").formatted(Formatting.GRAY),
                            Text.literal("узнать стоимость: /egg price").formatted(Formatting.GRAY),
                            Text.literal("обменять на сапфиры: /egg swap").formatted(Formatting.GRAY),
                            Text.literal("прятать можно только в обычном сундучке").formatted(Formatting.GRAY),
                            Text.literal("топ по спрятанным яйцам: /warp top").formatted(Formatting.GRAY)
                    )
            ));

            items.add(new HolyWorldItem(
                    "Золотой Спавнер",
                    Items.SPAWNER,
                    0,
                    null,
                    List.of(
                            Text.literal("каждую секунду в радиусе").formatted(Formatting.GRAY),
                            Text.literal("5 блоков от спавнера игроку").formatted(Formatting.GRAY),
                            Text.literal("начисляются монеты").formatted(Formatting.GRAY),
                            Text.literal("сломать спавнер можно").formatted(Formatting.GRAY),
                            Text.literal("только киркой Джейка").formatted(Formatting.GRAY)
                    )
            ));


            
            
            items.add(new HolyWorldKringeItem(
                    "Артефакт",
                    "Артефакт", 
                    Items.CONDUIT,
                    0, 
                    "EmptyArtefact" 
            ));

            items.add(new HolyWorldItem(
                    "Фейерверк",
                    Items.FIREWORK_ROCKET,
                    0,
                    null, 
                    null  
            ));


            items.add(new HolyWorldItem(
                    "Незеритовый слиток",
                    Items.NETHERITE_INGOT,
                    0,
                    null, 
                    null  
            ));


            items.add(new HolyWorldItem(
                    "Порох",
                    Items.GUNPOWDER,
                    0,
                    null,
                    null
            ));

            
            items.add(new HolyWorldItem(
                    "Боевой фрагмент",
                    Items.PRISMARINE_CRYSTALS,
                    0,
                    null, 
                    null  
            ));

            
            
            items.add(new HolyWorldPyrotechnicItem(
                    "Взрывчатое вещество",
                    "Взрывчатое вещество", 
                    Items.CLAY,
                    0, 
                    "EXPLOSIVE_SUBSTANCE" 
            ));

            

            
            
            items.add(new HolyWorldPyrotechnicItem(
                    "Динамит А",
                    "Динамит А", 
                    Items.TNT,
                    0, 
                    "A" 
            ));

            
            
            items.add(new HolyWorldPyrotechnicItem(
                    "Динамит B",
                    "динамит б", 
                    Items.TNT,
                    0, 
                    "B" 
            ));

            
            
            items.add(new HolyWorldPyrotechnicItem(
                    "Динамит B2",
                    "динамит б2", 
                    Items.TNT,
                    0, 
                    "B2" 
            ));

            
            
            items.add(new HolyWorldPyrotechnicItem(
                    "C4 ВзРыВчАтКа",
                    "с4 взрывчатка", 
                    Items.TNT,
                    0, 
                    "C4" 
            ));

            
            
            items.add(new HolyWorldKringeItem(
                    "Золотая кирка Джейка",
                    "Золотая кирка Джейка", 
                    Items.GOLDEN_PICKAXE,
                    0, 
                    "jake-pickaxe" 
            ));

            
            
            items.add(new HolyWorldSphereShardItem(
                    "Осколок сферы",
                    "Осколок сферы", 
                    Items.PLAYER_HEAD,
                    0, 
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY3YmJjZTIzZTgxNjJlNDJkMjA3MDU1YjBjZTkwZjBlZDU3YjAxNWU1MjEyMTM5YWM4ZmM3ZTZkNDVkZGZjYSJ9fX0="
            ));

        }
        return items;
    }

    public static void reload() {
        items = null;
    }

    
    public static class EnchantmentData {
        public final RegistryKey<Enchantment> enchantment;
        public final int level;

        public EnchantmentData(RegistryKey<Enchantment> enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }
    }

    
    public static class HolyWorldItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName; 
        private final Item material;
        private final int price;
        private final EnchantmentData[] requiredEnchantments;
        private final AttributeData[] attributes;
        private final List<Text> loreTexts;
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldItem(String displayName, Item material, int price,
                             EnchantmentData[] requiredEnchantments, List<Text> loreTexts) {
            this(displayName, null, material, price, requiredEnchantments, null, loreTexts);
        }

        public HolyWorldItem(String displayName, Item material, int price,
                             EnchantmentData[] requiredEnchantments, AttributeData[] attributes, List<Text> loreTexts) {
            this(displayName, null, material, price, requiredEnchantments, attributes, loreTexts);
        }

        public HolyWorldItem(String displayName, String searchName, Item material, int price,
                             EnchantmentData[] requiredEnchantments, AttributeData[] attributes, List<Text> loreTexts) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.requiredEnchantments = requiredEnchantments;
            this.attributes = attributes;
            this.loreTexts = loreTexts;
            this.enabled = true; 
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            if (requiredEnchantments != null && requiredEnchantments.length > 0) {
                addEnchantments(stack, requiredEnchantments);
            }

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.GREEN));

            
            if (loreTexts != null && !loreTexts.isEmpty()) {
                stack.set(DataComponentTypes.LORE, new LoreComponent(loreTexts));
            }

            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putInt("RequiredEnchantCount", requiredEnchantments != null ? requiredEnchantments.length : 0);
            nbt.putInt("HideFlags", 127); 
            nbt.putBoolean("Unbreakable", true); 

            
            if (attributes != null && attributes.length > 0) {
                NbtList attributeList = new NbtList();
                for (AttributeData attr : attributes) {
                    NbtCompound attributeNbt = new NbtCompound();
                    attributeNbt.putString("AttributeName", attr.attributeName);
                    attributeNbt.putDouble("Amount", attr.amount);
                    attributeNbt.putInt("Operation", attr.operation);
                    attributeNbt.putString("Slot", attr.slot);
                    attributeNbt.putString("Name", java.util.UUID.randomUUID().toString());
                    attributeNbt.putIntArray("UUID", new int[]{
                            (int)(Math.random() * Integer.MAX_VALUE),
                            (int)(Math.random() * Integer.MAX_VALUE),
                            (int)(Math.random() * Integer.MAX_VALUE),
                            (int)(Math.random() * Integer.MAX_VALUE)
                    });
                    attributeList.add(attributeNbt);
                }
                nbt.put("AttributeModifiers", attributeList);
            }

            
            if (requiredEnchantments != null && requiredEnchantments.length > 0) {
                NbtList enchantList = new NbtList();
                for (EnchantmentData data : requiredEnchantments) {
                    NbtCompound enchantNbt = new NbtCompound();
                    enchantNbt.putString("id", data.enchantment.getValue().toString());
                    enchantNbt.putShort("lvl", (short) data.level);
                    enchantList.add(enchantNbt);
                }
                nbt.put("RequiredEnchantments", enchantList);
            }

            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }

        
        public EnchantmentData[] getRequiredEnchantments() {
            return requiredEnchantments;
        }
    }

    
    public static class HolyWorldSphereItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName;
        private final Item material;
        private final int price;
        private final String skullUuid;
        private final String texture;
        private final String sphereName; 
        private final String requiredEffects; 
        private final AttributeData[] attributes;
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldSphereItem(String displayName, String searchName, Item material, int price, String skullUuid, String texture, String sphereName) {
            this(displayName, searchName, material, price, skullUuid, texture, sphereName, null);
        }

        public HolyWorldSphereItem(String displayName, String searchName, Item material, int price, String skullUuid, String texture, String sphereName, String requiredEffects) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.skullUuid = skullUuid;
            this.texture = texture;
            this.sphereName = sphereName;
            this.requiredEffects = requiredEffects;
            this.attributes = null;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.RED));

            
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);

            
            if (texture != null && !texture.isEmpty() && material == Items.PLAYER_HEAD) {
                UUID skullId;

                
                
                if (skullUuid == null || skullUuid.isEmpty()) {
                    
                    String uuidSource = "HolyWorldSphere:" + displayName + ":" + texture;
                    skullId = UUID.nameUUIDFromBytes(uuidSource.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } else {
                    try {
                        skullId = UUID.fromString(skullUuid);
                    } catch (IllegalArgumentException e) {
                        
                        String uuidSource = "HolyWorldSphere:" + displayName + ":" + texture;
                        skullId = UUID.nameUUIDFromBytes(uuidSource.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    }
                }

                NbtCompound skullOwner = new NbtCompound();
                skullOwner.putUuid("Id", skullId);

                NbtCompound properties = new NbtCompound();
                NbtList textures = new NbtList();
                NbtCompound textureNbt = new NbtCompound();
                textureNbt.putString("Value", texture);
                textures.add(textureNbt);
                properties.put("textures", textures);
                skullOwner.put("Properties", properties);
                nbt.put("SkullOwner", skullOwner);

                
                GameProfile profile = new GameProfile(skullId, "");
                profile.getProperties().put("textures", new Property("textures", texture));
                stack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
            }

            

            
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldSphere", true);

            
            if (sphereName != null && !sphereName.isEmpty()) {
                nbt.putString("sphereName", sphereName);
            }

            
            if (requiredEffects != null && !requiredEffects.isEmpty()) {
                nbt.putString("requiredEffects", requiredEffects);
            }

            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }
    }

    
    public static class HolyWorldTalikItem implements AutoBuyableItem {
        private final String displayName;
        private final Item material;
        private final int price;
        private final AttributeData[] attributes;
        private final EnchantmentData[] enchantments;
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldTalikItem(String displayName, Item material, int price, AttributeData[] attributes, EnchantmentData[] enchantments) {
            this.displayName = displayName;
            this.material = material;
            this.price = price;
            this.attributes = attributes;
            this.enchantments = enchantments;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            if (enchantments != null && enchantments.length > 0) {
                addEnchantments(stack, enchantments);
            }

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.BLUE));

            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldTalik", true); 
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);

            
            if (attributes != null && attributes.length > 0) {
                NbtList attributeList = new NbtList();
                for (AttributeData attr : attributes) {
                    NbtCompound attributeNbt = new NbtCompound();
                    attributeNbt.putString("AttributeName", attr.attributeName);
                    attributeNbt.putDouble("Amount", attr.amount);
                    attributeNbt.putInt("Operation", attr.operation);
                    attributeNbt.putString("Slot", attr.slot);
                    attributeNbt.putString("Name", java.util.UUID.randomUUID().toString());
                    attributeNbt.putIntArray("UUID", new int[]{
                            (int)(Math.random() * Integer.MAX_VALUE),
                            (int)(Math.random() * Integer.MAX_VALUE),
                            (int)(Math.random() * Integer.MAX_VALUE),
                            (int)(Math.random() * Integer.MAX_VALUE)
                    });
                    attributeList.add(attributeNbt);
                }
                nbt.put("AttributeModifiers", attributeList);
            }

            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }
    }

    
    public static class HolyWorldExpBottleItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName; 
        private final Item material;
        private final int price;
        private final int expValue; 
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldExpBottleItem(String displayName, String searchName, Item material, int price, int expValue) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.expValue = expValue;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.GOLD));

            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldExpBottle", true);
            nbt.putInt("holy-exp-bottle-value", expValue); 
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }
    }

    
    public static class HolyWorldBackpackItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName;
        private final Item material;
        private final int price;
        private final String backpackType; 
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldBackpackItem(String displayName, String searchName, Item material, int price, String backpackType) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.backpackType = backpackType;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.LIGHT_PURPLE));

            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldBackpack", true);
            nbt.putString("backpackType", backpackType);
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }

        public String getBackpackType() {
            return backpackType;
        }
    }

    
    public static class HolyWorldPyrotechnicItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName;
        private final Item material;
        private final int price;
        private final String pyrotechnicType; 
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldPyrotechnicItem(String displayName, String searchName, Item material, int price, String pyrotechnicType) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.pyrotechnicType = pyrotechnicType;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.LIGHT_PURPLE));

            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldPyrotechnic", true);
            nbt.putString("pyrotechnicType", pyrotechnicType);
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }

        public String getPyrotechnicType() {
            return pyrotechnicType;
        }
    }

    
    public static class HolyWorldKringeItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName;
        private final Item material;
        private final int price;
        private final String kringeType; 
        private final Integer customColor; 
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldKringeItem(String displayName, String searchName, Item material, int price, String kringeType) {
            this(displayName, searchName, material, price, kringeType, null);
        }

        public HolyWorldKringeItem(String displayName, String searchName, Item material, int price, String kringeType, Integer customColor) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.kringeType = kringeType;
            this.customColor = customColor;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.RED));

            
            if (customColor != null && material == Items.POTION) {
                PotionContentsComponent potionContents = new PotionContentsComponent(
                        Optional.empty(), 
                        Optional.of(customColor), 
                        List.of(), 
                        Optional.empty() 
                );
                stack.set(DataComponentTypes.POTION_CONTENTS, potionContents);
            }

            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldKringe", true);
            nbt.putString("kringeType", kringeType);
            if (customColor != null) {
                nbt.putInt("customColor", customColor);
            }
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }

        public String getKringeType() {
            return kringeType;
        }
    }

    
    public static class HolyWorldRuneItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName;
        private final Item material;
        private final int price;
        private final String runeId; 
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldRuneItem(String displayName, String searchName, Item material, int price, String runeId) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.runeId = runeId;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.GOLD));

            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldRune", true);
            nbt.putString("runeId", runeId);
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            mergeCustomData(stack, nbt);

            
            if ("immortality".equals(runeId)) {
                stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }

        public String getRuneId() {
            return runeId;
        }
    }

    
    public static class HolyWorldKringeEffectItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName;
        private final Item material;
        private final int price;
        private final String effectType; 
        private final Integer customColor; 
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldKringeEffectItem(String displayName, String searchName, Item material, int price, String effectType) {
            this(displayName, searchName, material, price, effectType, null);
        }

        public HolyWorldKringeEffectItem(String displayName, String searchName, Item material, int price, String effectType, Integer customColor) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.effectType = effectType;
            this.customColor = customColor;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.GOLD));

            
            if (customColor != null && material == Items.POTION) {
                PotionContentsComponent potionContents = new PotionContentsComponent(
                        Optional.empty(), 
                        Optional.of(customColor), 
                        List.of(), 
                        Optional.empty() 
                );
                stack.set(DataComponentTypes.POTION_CONTENTS, potionContents);
            }

            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldKringeEffect", true);
            nbt.putString("effectType", effectType);
            if (customColor != null) {
                nbt.putInt("customColor", customColor);
            }
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }

        public String getEffectType() {
            return effectType;
        }
    }

    
    public static class HolyWorldPotionItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName;
        private final Item material;
        private final int price;
        private final RegistryEntry<StatusEffect> effectType;
        private final int amplifier;
        private final List<Integer> allowedDurations; 
        private final Integer customColor; 
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldPotionItem(String displayName, String searchName, Item material, int price,
                                   RegistryEntry<StatusEffect> effectType, int amplifier, List<Integer> allowedDurations) {
            this(displayName, searchName, material, price, effectType, amplifier, allowedDurations, null);
        }

        public HolyWorldPotionItem(String displayName, String searchName, Item material, int price,
                                   RegistryEntry<StatusEffect> effectType, int amplifier, List<Integer> allowedDurations, Integer customColor) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.effectType = effectType;
            this.amplifier = amplifier;
            this.allowedDurations = allowedDurations;
            this.customColor = customColor;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.RED));

            
            StatusEffectInstance effect = new StatusEffectInstance(effectType, allowedDurations.isEmpty() ? 3600 : allowedDurations.get(0), amplifier);
            PotionContentsComponent potionContents = new PotionContentsComponent(
                    Optional.empty(), 
                    customColor != null ? Optional.of(customColor) : Optional.empty(), 
                    List.of(effect), 
                    Optional.empty() 
            );
            stack.set(DataComponentTypes.POTION_CONTENTS, potionContents);

            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldPotion", true);
            nbt.putString("effectId", effectType.getIdAsString());
            nbt.putInt("amplifier", amplifier);
            NbtList durationsList = new NbtList();
            for (Integer duration : allowedDurations) {
                NbtCompound durationNbt = new NbtCompound();
                durationNbt.putInt("duration", duration);
                durationsList.add(durationNbt);
            }
            nbt.put("allowedDurations", durationsList);
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }

        public RegistryEntry<StatusEffect> getEffectType() {
            return effectType;
        }

        public int getAmplifier() {
            return amplifier;
        }

        public List<Integer> getAllowedDurations() {
            return allowedDurations;
        }
    }

    
    public static class HolyWorldStandardPotionItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName;
        private final Item material;
        private final int price;
        private final String potionType; 
        private final Integer customColor; 
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldStandardPotionItem(String displayName, String searchName, Item material, int price, String potionType) {
            this(displayName, searchName, material, price, potionType, null);
        }

        public HolyWorldStandardPotionItem(String displayName, String searchName, Item material, int price, String potionType, Integer customColor) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.potionType = potionType;
            this.customColor = customColor;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.AQUA));

            
            if (customColor != null) {
                PotionContentsComponent potionContents = new PotionContentsComponent(
                        Optional.empty(), 
                        Optional.of(customColor), 
                        List.of(), 
                        Optional.empty() 
                );
                stack.set(DataComponentTypes.POTION_CONTENTS, potionContents);
            }

            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldStandardPotion", true);
            nbt.putString("potionType", potionType);
            if (customColor != null) {
                nbt.putInt("customColor", customColor);
            }
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }

        public String getPotionType() {
            return potionType;
        }
    }

    
    public static class HolyWorldMultiEffectPotionItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName;
        private final Item material;
        private final int price;
        private final int customColor;
        private final List<StatusEffectInstance> effects;
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldMultiEffectPotionItem(String displayName, String searchName, Item material, int price,
                                              int customColor, List<StatusEffectInstance> effects) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.customColor = customColor;
            this.effects = effects;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.AQUA));

            
            PotionContentsComponent potionContents = new PotionContentsComponent(
                    Optional.empty(), 
                    Optional.of(customColor), 
                    effects, 
                    Optional.empty() 
            );
            stack.set(DataComponentTypes.POTION_CONTENTS, potionContents);

            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldMultiEffectPotion", true);
            nbt.putInt("customColor", customColor);
            NbtList effectsList = new NbtList();
            for (StatusEffectInstance effect : effects) {
                NbtCompound effectNbt = new NbtCompound();
                effectNbt.putString("effectId", effect.getEffectType().getIdAsString());
                effectNbt.putInt("amplifier", effect.getAmplifier());
                effectNbt.putInt("duration", effect.getDuration());
                effectsList.add(effectNbt);
            }
            nbt.put("effects", effectsList);
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }

        public List<StatusEffectInstance> getEffects() {
            return effects;
        }

        public int getCustomColor() {
            return customColor;
        }
    }

    
    public static class HolyWorldSphereShardItem implements AutoBuyableItem {
        private final String displayName;
        private final String searchName;
        private final Item material;
        private final int price;
        private final String texture;
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public HolyWorldSphereShardItem(String displayName, String searchName, Item material, int price, String texture) {
            this.displayName = displayName;
            this.searchName = searchName;
            this.material = material;
            this.price = price;
            this.texture = texture;
            this.enabled = true;
            this.settings = new AutoBuyItemSettings(price, material, displayName);
            AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getSearchName() {
            return searchName != null ? searchName : displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);

            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.LIGHT_PURPLE));

            
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);

            
            if (texture != null && !texture.isEmpty() && material == Items.PLAYER_HEAD) {
                UUID skullId = UUID.fromString("9afca6b1-556f-3cf9-b349-3886d7d2c53b");

                NbtCompound skullOwner = new NbtCompound();
                skullOwner.putUuid("Id", skullId);

                NbtCompound properties = new NbtCompound();
                NbtList textures = new NbtList();
                NbtCompound textureNbt = new NbtCompound();
                textureNbt.putString("Value", texture);
                textures.add(textureNbt);
                properties.put("textures", textures);
                skullOwner.put("Properties", properties);
                nbt.put("SkullOwner", skullOwner);

                
                nbt.putIntArray("SkullOwnerOrig", new int[]{0, 778770836, 0, 778770836});

                
                GameProfile profile = new GameProfile(skullId, "");
                profile.getProperties().put("textures", new Property("textures", texture));
                stack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
            }

            
            NbtCompound publicBukkitValues = new NbtCompound();
            publicBukkitValues.putByte("magicspheres:burned-sphere-shard", (byte)1);
            nbt.put("PublicBukkitValues", publicBukkitValues);

            
            nbt.putInt("sphereEffect", 1);

            
            nbt.putBoolean("HolyWorldItem", true);
            nbt.putBoolean("HolyWorldSphereShard", true);

            mergeCustomData(stack, nbt);

            return stack;
        }

        @Override
        public int getPrice() {
            return price;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public AutoBuyItemSettings getSettings() {
            return settings;
        }
    }

    
    private static class AttributeData {
        final String attributeName;
        final double amount;
        final int operation;
        final String slot;

        AttributeData(String attributeName, double amount, int operation, String slot) {
            this.attributeName = attributeName;
            this.amount = amount;
            this.operation = operation;
            this.slot = slot;
        }
    }

    
    private static void addEnchantments(ItemStack stack, EnchantmentData[] enchantments) {
        NbtCompound customData = stack.get(DataComponentTypes.CUSTOM_DATA) != null ? stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt() : new NbtCompound();
        NbtList requiredEnchantments = customData.contains("RequiredEnchantments", NbtElement.LIST_TYPE)
                ? customData.getList("RequiredEnchantments", NbtElement.COMPOUND_TYPE)
                : new NbtList();
        for (EnchantmentData data : enchantments) {
            NbtCompound enchantNbt = new NbtCompound();
            enchantNbt.putString("id", data.enchantment.getValue().toString());
            enchantNbt.putShort("lvl", (short) data.level);
            requiredEnchantments.add(enchantNbt);
        }
        customData.put("RequiredEnchantments", requiredEnchantments);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
    }

    private static void mergeCustomData(ItemStack stack, NbtCompound nbt) {
        NbtCompound merged = stack.get(DataComponentTypes.CUSTOM_DATA) != null ? stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt() : new NbtCompound();
        for (String key : nbt.getKeys()) {
            merged.put(key, nbt.get(key).copy());
        }
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(merged));
    }
}
