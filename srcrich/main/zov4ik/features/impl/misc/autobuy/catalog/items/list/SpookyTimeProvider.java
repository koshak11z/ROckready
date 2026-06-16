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
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


public class SpookyTimeProvider {

    private static List<AutoBuyableItem> items = null;

    public static List<AutoBuyableItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
            addItems();
        }
        return items;
    }

    public static void reload() {
        items = null;
    }

    private static void addItems() {
        
        List<EnchantmentData> krushhelmet = Arrays.asList(
            new EnchantmentData(Enchantments.AQUA_AFFINITY, -1),
            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
            new EnchantmentData(Enchantments.MENDING, -1),
            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
            new EnchantmentData(Enchantments.PROTECTION, 5),
            new EnchantmentData(Enchantments.RESPIRATION, 3),
            new EnchantmentData(Enchantments.UNBREAKING, 5)
        );
        
        List<EnchantmentData> krushChestplateEnchants = Arrays.asList(
            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
            new EnchantmentData(Enchantments.MENDING, -1),
            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
            new EnchantmentData(Enchantments.PROTECTION, 5),
            new EnchantmentData(Enchantments.UNBREAKING, 5)
        );
        
        List<EnchantmentData> krushLegginsEnchants = Arrays.asList(
            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
            new EnchantmentData(Enchantments.MENDING, -1),
            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
            new EnchantmentData(Enchantments.PROTECTION, 5),
            new EnchantmentData(Enchantments.UNBREAKING, 5)
        );
        
        List<EnchantmentData> krushBootsEnchants = Arrays.asList(
            new EnchantmentData(Enchantments.BLAST_PROTECTION, 5),
            new EnchantmentData(Enchantments.DEPTH_STRIDER, 3),
            new EnchantmentData(Enchantments.FEATHER_FALLING, 4),
            new EnchantmentData(Enchantments.FIRE_PROTECTION, 5),
            new EnchantmentData(Enchantments.MENDING, -1),
            new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5),
            new EnchantmentData(Enchantments.PROTECTION, 5),
            new EnchantmentData(Enchantments.SOUL_SPEED, 3),
            new EnchantmentData(Enchantments.UNBREAKING, 5)
        );
        
        List<EnchantmentData> krushSwordEnchants = Arrays.asList(
            new EnchantmentData(Enchantments.BANE_OF_ARTHROPODS, 7),
            new EnchantmentData(Enchantments.FIRE_ASPECT, 2),
            new EnchantmentData(Enchantments.LOOTING, 5),
            new EnchantmentData(Enchantments.MENDING, -1),
            new EnchantmentData(Enchantments.SHARPNESS, 7),
            new EnchantmentData(Enchantments.SMITE, 7),
            new EnchantmentData(Enchantments.SWEEPING_EDGE, 3),
            new EnchantmentData(Enchantments.UNBREAKING, 5)
        );
        
        List<EnchantmentData> krushTrebEnchants = Arrays.asList(
            new EnchantmentData(Enchantments.CHANNELING, -1),
            new EnchantmentData(Enchantments.FIRE_ASPECT, 2),
            new EnchantmentData(Enchantments.IMPALING, 5),
            new EnchantmentData(Enchantments.LOYALTY, 3),
            new EnchantmentData(Enchantments.MENDING, -1),
            new EnchantmentData(Enchantments.SHARPNESS, 7),
            new EnchantmentData(Enchantments.UNBREAKING, 5)
        );
        
        List<EnchantmentData> krushArbEnchants = Arrays.asList(
            new EnchantmentData(Enchantments.MENDING, -1),
            new EnchantmentData(Enchantments.MULTISHOT, -1),
            new EnchantmentData(Enchantments.PIERCING, 5),
            new EnchantmentData(Enchantments.QUICK_CHARGE, 3),
            new EnchantmentData(Enchantments.UNBREAKING, 3)
        );
        
        List<EnchantmentData> krushPickaxeEnchants = Arrays.asList(
            new EnchantmentData(Enchantments.EFFICIENCY, 10),
            new EnchantmentData(Enchantments.FORTUNE, 5),
            new EnchantmentData(Enchantments.MENDING, -1),
            new EnchantmentData(Enchantments.UNBREAKING, 5)
        );
        
        List<EnchantmentData> krushElytraEnchants = Arrays.asList(
            new EnchantmentData(Enchantments.MENDING, -1),
            new EnchantmentData(Enchantments.UNBREAKING, 5)
        );
        
        List<EnchantmentData> repairBookEnchants = Arrays.asList(
            new EnchantmentData(Enchantments.MENDING, 1)
        );

        
        items.add(new SpookyTimeItem("Шлем Крушителя", Items.NETHERITE_HELMET, 0, krushhelmet.toArray(new EnchantmentData[0]), null));
        items.add(new SpookyTimeItem("Нагрудник Крушителя", Items.NETHERITE_CHESTPLATE, 0, krushChestplateEnchants.toArray(new EnchantmentData[0]), null));
        items.add(new SpookyTimeItem("Поножи Крушителя", Items.NETHERITE_LEGGINGS, 0, krushLegginsEnchants.toArray(new EnchantmentData[0]), null));
        items.add(new SpookyTimeItem("Ботинки Крушителя", Items.NETHERITE_BOOTS, 0, krushBootsEnchants.toArray(new EnchantmentData[0]), null));
        items.add(new SpookyTimeItem("Меч Крушителя", Items.NETHERITE_SWORD, 0, krushSwordEnchants.toArray(new EnchantmentData[0]), null));
        items.add(new SpookyTimeItem("Трезубец Крушителя", Items.TRIDENT, 0, krushTrebEnchants.toArray(new EnchantmentData[0]), null));
        items.add(new SpookyTimeItem("Арбалет Крушителя", Items.CROSSBOW, 0, krushArbEnchants.toArray(new EnchantmentData[0]), null));
        items.add(new SpookyTimeItem("Кирка Крушителя", Items.NETHERITE_PICKAXE, 0, krushPickaxeEnchants.toArray(new EnchantmentData[0]), null));
        items.add(new SpookyTimeItem("Элитры Крушителя", Items.ELYTRA, 0, krushElytraEnchants.toArray(new EnchantmentData[0]), null));

        
        
        EnchantmentData[] talismanEnchants = new EnchantmentData[]{
            new EnchantmentData(Enchantments.UNBREAKING, 1)
        };
        
        items.add(new SpookyTimeTalismanItem("Талисман Карателя", Items.TOTEM_OF_UNDYING, 0, 
            new AttributeData[]{
                createAttributes("minecraft:generic.movement_speed", 0.1, 1, "offhand"),
                createAttributes("minecraft:generic.max_health", -4.0, 0, "offhand"),
                createAttributes("minecraft:generic.attack_damage", 7.0, 0, "offhand")
            },
            talismanEnchants));
        
        items.add(new SpookyTimeTalismanItem("Талисман Крушителя", Items.TOTEM_OF_UNDYING, 0,
            new AttributeData[]{
                createAttributes("minecraft:generic.armor", 2.0, 0, "offhand"),
                createAttributes("minecraft:generic.armor_toughness", 2.0, 0, "offhand"),
                createAttributes("minecraft:generic.attack_damage", 3.0, 0, "offhand"),
                createAttributes("minecraft:generic.max_health", 4.0, 0, "offhand")
            },
            talismanEnchants));

        items.add(new SpookyTimeTalismanItem("Талисман Раздора", Items.TOTEM_OF_UNDYING, 0,
                new AttributeData[]{
                        createAttributes("minecraft:generic.attack_damage", 4.0, 0, "offhand"),
                        createAttributes("minecraft:generic.armor", -3.0, 0, "offhand"),
                        createAttributes("minecraft:generic.max_health", 2.0, 0, "offhand"),
                        createAttributes("minecraft:generic.movement_speed", 0.1, 1, "offhand"),
                        createAttributes("minecraft:generic.attack_speed", 0.1, 1, "offhand")
                },
                talismanEnchants));

        items.add(new SpookyTimeTalismanItem("Талисман Тирана", Items.TOTEM_OF_UNDYING, 0,
                new AttributeData[]{
                        createAttributes("minecraft:generic.armor", 2.0, 0, "offhand"),
                        createAttributes("minecraft:generic.attack_damage", 2.0, 0, "offhand"),
                        createAttributes("minecraft:generic.max_health", -4.0, 0, "offhand")
                },
                talismanEnchants));

        items.add(new SpookyTimeTalismanItem("Талисман Ярости", Items.TOTEM_OF_UNDYING, 0,
                new AttributeData[]{
                        createAttributes("minecraft:generic.attack_damage", 5.0, 0, "offhand"),
                        createAttributes("minecraft:generic.max_health", -4.0, 0, "offhand")
                },
                talismanEnchants));

        items.add(new SpookyTimeTalismanItem("Талисман Вихря", Items.TOTEM_OF_UNDYING, 0,
                new AttributeData[]{
                        createAttributes("minecraft:generic.max_health", 2.0, 0, "offhand"),
                        createAttributes("minecraft:generic.attack_speed", 0.15, 1, "offhand"),
                        createAttributes("minecraft:generic.movement_speed", 0.15, 1, "offhand")
                },
                talismanEnchants));

        items.add(new SpookyTimeTalismanItem("Талисман Мрака", Items.TOTEM_OF_UNDYING, 0,
                new AttributeData[]{
                        createAttributes("minecraft:generic.max_health", 1.5, 0, "offhand"),
                        createAttributes("minecraft:generic.armor", 1.5, 0, "offhand")
                },
                talismanEnchants));

        items.add(new SpookyTimeTalismanItem("Талисман Демона", Items.TOTEM_OF_UNDYING, 0,
                new AttributeData[]{
                        createAttributes("minecraft:generic.attack_speed", 0.1, 1, "offhand"),
                        createAttributes("minecraft:generic.attack_damage", 2.5, 0, "offhand")
                },
                talismanEnchants));
        
        
        items.add(new SpookyTimeTalismanItem("Тотем бессмертия", Items.TOTEM_OF_UNDYING, 0, null, null));


        items.add(new SpookyTimeSphereItem("Сфера Афины", Items.PLAYER_HEAD, 0,
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzg2MTE4NywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJTcGhlcmVBdGhlbmEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNmOWVlZGEzYmEyM2ZlMTQyM2M0MDM2ZTdkZDBhNzQ0NjFkZmY5NmJhZGM1YjJmMmI5ZmFhN2NjMTZmMzgyZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
            createAttributes("minecraft:generic.attack_speed", 0.15, 1, "offhand"),
            createAttributes("minecraft:generic.movement_speed", 0.15, 1, "offhand"),
            createAttributes("minecraft:generic.attack_damage", 3.0, 0, "offhand"),
            createAttributes("minecraft:generic.max_health", -2.0, 0, "offhand")));

        items.add(new SpookyTimeSphereItem("Сфера Титана", Items.PLAYER_HEAD, 0,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODFlOTY5ODQ1OGI3ODQxYzk2YWU0ZjI0ZWM4NGFlMDE3MjQxMDA2NDFjNTY0ZTJhN2IxODVmNDA2ZThlZDIzIn19fQ==",
                createAttributes("minecraft:generic.armor", 3.0, 0, "offhand"),
                createAttributes("minecraft:generic.armor_toughness", 3.0, 0, "offhand"),
                createAttributes("minecraft:generic.movement_speed", -0.15, 1, "offhand")));

        
        items.add(new SpookyTimeSphereItem("Сфера Хаоса", Items.PLAYER_HEAD, 0,
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODY0MTkwMCwKICAicHJvZmlsZUlkIiA6ICIxNzRjZmRiNGEzY2I0M2I1YmZjZGU0MjRjM2JiMmM2ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYXJhZWwxOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lN2E3YWU3Y2RjZjYxNmU4YjdhNDIyMWE2MjFiMjQzNTc1M2M2MGVkNmEyNThlYTA2MGRhZTMwMDJmZmU5ZTI4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
            createAttributes("minecraft:generic.attack_damage", 3.0, 0, "offhand"),
            createAttributes("minecraft:generic.movement_speed", 0.07, 1, "offhand"),
            createAttributes("minecraft:generic.attack_speed", 0.13, 1, "offhand"),
            createAttributes("minecraft:generic.armor", 2.0, 0, "offhand"),
            createAttributes("minecraft:generic.max_health", -4.0, 0, "offhand")));

        items.add(new SpookyTimeSphereItem("Сфера Сатира", Items.PLAYER_HEAD, 0,
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODYwODUyOCwKICAicHJvZmlsZUlkIiA6ICJkMTQ4NjFiM2UwZmM0Njk5OTFlMTcyNTllMzdiZjZhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJyYXhpdG9jbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NzFhOWE0OThiNGZhNWVjNDkzNjJmOWJjODhlZGE0ZjUyYjA0ZGU0OWQ3NWFhM2NhMzMyYTFmZWExYWEwZTU3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
            createAttributes("minecraft:generic.attack_damage", 2.0, 0, "offhand"),
            createAttributes("minecraft:generic.attack_speed", 0.15, 1, "offhand")));

        items.add(new SpookyTimeSphereItem("Сфера Бестии", Items.PLAYER_HEAD, 0,
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0MzgzNDkzMCwKICAicHJvZmlsZUlkIiA6ICI1MzUzNWIxN2M0ZDY0NWQ0YWUwY2U2ZjM4Zjk0NTFjYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJVYml2aXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQxMWFjMTczODFiOWZjZTliYWIzYzcyYWZkYjdmMTk4NTcwZGFmNDczMmJkODExZDMxYzIyN2Q4MGZhMzliMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
            createAttributes("minecraft:generic.armor", 1.0, 0, "offhand"),
            createAttributes("minecraft:generic.movement_speed", 0.1, 1, "offhand"),
            createAttributes("minecraft:generic.attack_speed", 0.1, 1, "offhand"),
            createAttributes("minecraft:generic.max_health", 4.0, 0, "offhand")));

        items.add(new SpookyTimeSphereItem("Сфера Ареса", Items.PLAYER_HEAD, 0,
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzc3NDI1NSwKICAicHJvZmlsZUlkIiA6ICJhYWMxYjA2OWNkMjE0NWE2ODNlNzQxNzE4MDcxMGU4MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJqdXNhbXUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE2YWRjNmJhZmNiNTdmZDcwN2RlZTdkZDZhNzM2ZmUxMjY3MTFkNTNhMWZkNmNlNzg5ZGE0MWIzYmUxM2YyYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
            createAttributes("minecraft:generic.attack_damage", 6.0, 0, "offhand"),
            createAttributes("minecraft:generic.max_health", -2.0, 0, "offhand"),
            createAttributes("minecraft:generic.armor", -2.0, 0, "offhand")));

        items.add(new SpookyTimeSphereItem("Сфера Гидры", Items.PLAYER_HEAD, 0,
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODUzMjE4MywKICAicHJvZmlsZUlkIiA6ICI1OGZmZWI5NTMxNGQ0ODcwYTQwYjVjYjQyZDRlYTU5OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTa2luREJuZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UzYzExOGQ2OTZkOTEwZTU0ZGUwMmNhNGQ4MDc1NDNmOWIxOGMwMDhjOTgzOGQyZmY2OTM3NzYyMmZiMWQzMiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
            createAttributes("minecraft:generic.armor", 2.0, 0, "offhand"),
            createAttributes("minecraft:generic.max_health", 4.0, 0, "offhand")));

        items.add(new SpookyTimeSphereItem("Сфера Икара", Items.PLAYER_HEAD, 0,
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODU4MjQ5MSwKICAicHJvZmlsZUlkIiA6ICJhZWNkODIxZTQyYzE0ZDJlOThmNTA1OTg1MWI5OWMzNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJSb2RyaVgyMDc1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M2ODAzZTZkNTY2N2EyZDYxMDYyOGJjM2IzMmY4NjNjZGE0OTVjNDY1NjE2ZGU2NTVjYjMyOTkzM2I2MWFmNzciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
            createAttributes("minecraft:generic.attack_damage", 2.0, 0, "offhand"),
            createAttributes("minecraft:generic.max_health", 2.0, 0, "offhand")));

        items.add(new SpookyTimeSphereItem("Сфера Эрида", Items.PLAYER_HEAD, 0,
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzg2MTE4NywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZlNGUyZjEwNDdmM2VjNmU5ZTQ1OTE4NDczOWUzM2I3YzFmYzYzYWQ4MjAyYmRhYjlmMDI0NTA4YWRkMjNlNWIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
            createAttributes("minecraft:generic.luck", 1.0, 0, "offhand"),
            createAttributes("minecraft:generic.max_health", 2.0, 0, "offhand")));

        
        items.add(new SpookyTimeItem("Пузырёк опыта", Items.EXPERIENCE_BOTTLE, 0, null, null));
        items.add(new SpookyTimeItem("Чарка", Items.ENCHANTED_GOLDEN_APPLE, 0, null, null));
        items.add(new SpookyTimeItem("Золотое яблоко", Items.GOLDEN_APPLE, 0, null, null));
        items.add(new SpookyTimeItem("Яблоко", Items.APPLE, 0, null, null));
        items.add(new SpookyTimeItem("Перка", Items.ENDER_PEARL, 0, null, null));
        items.add(new SpookyTimeItem("Незеритовый слиток", Items.NETHERITE_INGOT, 0, null, null));
        items.add(new SpookyTimeItem("Незеритовый лом", Items.NETHERITE_SCRAP, 0, null, null));
        items.add(new SpookyTimeItem("Алмаз", Items.DIAMOND, 0, null, null));
        items.add(new SpookyTimeItem("Изумруд", Items.EMERALD, 0, null, null));
        items.add(new SpookyTimeItem("Золотой слиток", Items.GOLD_INGOT, 0, null, null));
        items.add(new SpookyTimeItem("Алмазный блок", Items.DIAMOND_BLOCK, 0, null, null));
        items.add(new SpookyTimeItem("Изумрудный блок", Items.EMERALD_BLOCK, 0, null, null));
        items.add(new SpookyTimeItem("Золотой блок", Items.GOLD_BLOCK, 0, null, null));
        items.add(new SpookyTimeItem("Обсидиан", Items.OBSIDIAN, 0, null, null));
        items.add(new SpookyTimeItem("Голова дракона", Items.DRAGON_HEAD, 0, null, null));
        items.add(new SpookyTimeItem("Голова визер-скелета", Items.WITHER_SKELETON_SKULL, 0, null, null));
        items.add(new SpookyTimeItem("Древние обломки", Items.ANCIENT_DEBRIS, 0, null, null));
        items.add(new SpookyTimeItem("Яйцо призыва крестьянина", Items.VILLAGER_SPAWN_EGG, 0, null, null));
        items.add(new SpookyTimeItem("Яйцо зомби-крестьянина", Items.ZOMBIE_VILLAGER_SPAWN_EGG, 0, null, null));
        items.add(new SpookyTimeItem("Элитры", Items.ELYTRA, 0, null, null));
        items.add(new SpookyTimeItem("Золотая морковь", Items.GOLDEN_CARROT, 0, null, null));
        items.add(new SpookyTimeItem("Шалкер", Items.SHULKER_BOX, 0, null, null));
        items.add(new SpookyTimeItem("Маяк", Items.BEACON, 0, null, null));
        items.add(new SpookyTimeItem("Алмазная руда", Items.DIAMOND_ORE, 0, null, null));
        items.add(new SpookyTimeItem("Изумрудная руда", Items.EMERALD_ORE, 0, null, null));
        items.add(new SpookyTimeItem("Спавнер", Items.SPAWNER, 0, null, null));
        items.add(new SpookyTimeItem("Порох", Items.GUNPOWDER, 0, null, null));
        items.add(new SpookyTimeSpecialItem("Проклятая душа", Items.SOUL_LANTERN, 0, "soul-currency"));
        items.add(new SpookyTimeSpecialItem("Трапка", Items.NETHERITE_SCRAP, 0, "schematic-item-trap"));
        items.add(new SpookyTimeSpecialItem("Дезориентация", Items.ENDER_EYE, 0, "effect-item-diz"));
        items.add(new SpookyTimeSpecialItem("Явная пыль", Items.SUGAR, 0, "effect-item-dust"));
        items.add(new SpookyTimeSpecialItem("Пласт", Items.DRIED_KELP, 0, "schematic-item-plast"));
        items.add(new SpookyTimeSpecialItem("Божья аура", Items.PHANTOM_MEMBRANE, 0, "effect-item-god"));
        items.add(new SpookyTimeSpecialItem("Снежок заморозка", Items.SNOWBALL, 0, "effect-item-snowball"));
        items.add(new SpookyTimeSpecialItem("Молот Тора", Items.NETHERITE_PICKAXE, 0, "radius-item-mega-buldozer"));
        items.add(new SpookyTimeSpecialItem("Божье касание", Items.GOLDEN_PICKAXE, 0, "spawner-item-spawner-break"));
        items.add(new SpookyTimeSpecialItem("Мощный удар", Items.GOLDEN_PICKAXE, 0, "bedrock-item-bedrock-break"));
        items.add(new SpookyTimeItem("Книга починка", Items.ENCHANTED_BOOK, 0, repairBookEnchants.toArray(new EnchantmentData[0]), null));
        items.add(new SpookyTimeSpecialItem("Отмычка к сферам", Items.TRIPWIRE_HOOK, 0, "spheres"));
        items.add(new SpookyTimeSpecialItem("Отмычка к броне", Items.TRIPWIRE_HOOK, 0, "armors"));
        items.add(new SpookyTimeSpecialItem("Отмычка к оружию", Items.TRIPWIRE_HOOK, 0, "weapons"));
        items.add(new SpookyTimeSpecialItem("Отмычка к инструментам", Items.TRIPWIRE_HOOK, 0, "tools"));
        items.add(new SpookyTimeSpecialItem("Отмычка к ресурсам", Items.TRIPWIRE_HOOK, 0, "resources"));

        items.add(new SpookyTimeSpecialItem("Обычный мист", Items.CAMPFIRE, 0, "MILD"));
        items.add(new SpookyTimeSpecialItem("Богатый мист", Items.CAMPFIRE, 0, "WEAK"));
        items.add(new SpookyTimeSpecialItem("Легендарный мист", Items.CAMPFIRE, 0, "MEDIUM"));
        items.add(new SpookyTimeSpecialItem("Прогрузчик чанков 1x1", Items.STRUCTURE_BLOCK, 0, "executable-block-chunker-1"));
        items.add(new SpookyTimeSpecialItem("Прогрузчик чанков 2x2", Items.STRUCTURE_BLOCK, 0, "executable-block-chunker-2"));
        items.add(new SpookyTimeSpecialItem("Прогрузчик чанков 3x3", Items.STRUCTURE_BLOCK, 0, "executable-block-chunker-2"));
        items.add(new SpookyTimeSpecialItem("Дамагер", Items.JIGSAW, 0, "executable-block-damager"));
        items.add(new SpookyTimeItem("Динамит", Items.TNT, 0, null, null));
        items.add(new SpookyTimeSpecialItem("Таер вайт", Items.TNT, 0, "tnt-item-white"));
        items.add(new SpookyTimeSpecialItem("Таер блэк", Items.TNT, 0, "tnt-item-black"));
        items.add(new SpookyTimeSpecialItem("Неизбежный скин", Items.PAPER, 0, "trap-skin-item-inevitable"));
        items.add(new SpookyTimeSpecialItem("Драконий скин", Items.PAPER, 0, "trap-skin-item-dragon"));

        
        items.add(new SpookyTimePotionItem("Зелье силы", Items.POTION, 0,
            Arrays.asList(new StatusEffectInstance(StatusEffects.STRENGTH, 3600, 2))));
        items.add(new SpookyTimePotionItem("Зелье скорости", Items.POTION, 0,
            Arrays.asList(new StatusEffectInstance(StatusEffects.SPEED, 3600, 2))));
        items.add(new SpookyTimePotionItem("Зелье исцеления", Items.POTION, 0,
            Arrays.asList(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 1))));
        items.add(new SpookyTimePotionItem("Силка + Скорка (автопарсинг не работает)", Items.POTION, 0,
            Arrays.asList(
                new StatusEffectInstance(StatusEffects.STRENGTH, 3600, 2),
                new StatusEffectInstance(StatusEffects.SPEED, 3600, 2)
            )));

        
        items.add(new SpookyTimePotionItem("Хлопушка", Items.SPLASH_POTION, 0,
            Arrays.asList(
                new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 9),
                new StatusEffectInstance(StatusEffects.SPEED, 400, 4),
                new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 9),
                new StatusEffectInstance(StatusEffects.GLOWING, 3600, 0)
            ), 16738740));
        
        items.add(new SpookyTimePotionItem("Зелье Гнева", Items.SPLASH_POTION, 0,
            Arrays.asList(
                new StatusEffectInstance(StatusEffects.STRENGTH, 600, 4),
                new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 3)
            ), 10040115));
        
        items.add(new SpookyTimePotionItem("Зелье Палладина", Items.SPLASH_POTION, 0,
            Arrays.asList(
                new StatusEffectInstance(StatusEffects.RESISTANCE, 12000, 0),
                new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 12000, 0),
                new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 1200, 2),
                new StatusEffectInstance(StatusEffects.INVISIBILITY, 18000, 2)
            ), 65535));
        
        items.add(new SpookyTimePotionItem("Святая вода", Items.SPLASH_POTION, 0,
            Arrays.asList(
                new StatusEffectInstance(StatusEffects.REGENERATION, 1200, 2),
                new StatusEffectInstance(StatusEffects.INVISIBILITY, 12000, 1),
                new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 1)
            ), 16777215));
        
        items.add(new SpookyTimePotionItem("Зелье Ассасина", Items.SPLASH_POTION, 0,
            Arrays.asList(
                new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 3),
                new StatusEffectInstance(StatusEffects.SPEED, 6000, 2),
                new StatusEffectInstance(StatusEffects.HASTE, 1200, 0),
                new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1)
            ), 3355443));
        
        items.add(new SpookyTimePotionItem("Зелье Радиации", Items.SPLASH_POTION, 0,
            Arrays.asList(
                new StatusEffectInstance(StatusEffects.POISON, 400, 0),
                new StatusEffectInstance(StatusEffects.WITHER, 400, 0),
                new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 2),
                new StatusEffectInstance(StatusEffects.HUNGER, 400, 4),
                new StatusEffectInstance(StatusEffects.GLOWING, 400, 0)
            ), 3329330));
        
        items.add(new SpookyTimePotionItem("Снотворное", Items.SPLASH_POTION, 0,
            Arrays.asList(
                new StatusEffectInstance(StatusEffects.WEAKNESS, 1800, 1),
                new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, 1),
                new StatusEffectInstance(StatusEffects.WITHER, 1800, 2),
                new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0)
            ), 4737096));
    }

    private static AttributeData createAttributes(String attributeName, double amount, int operation, String slot) {
        return new AttributeData(attributeName, amount, operation, slot);
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

    
    public static class EnchantmentData {
        public final RegistryKey<Enchantment> enchantment;
        public final int level;

        public EnchantmentData(RegistryKey<Enchantment> enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }
    }

    
    public static class SpookyTimeItem implements AutoBuyableItem {
        private final String displayName;
        private final Item material;
        private final int price;
        private final EnchantmentData[] requiredEnchantments;
        private final List<Text> loreTexts;
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public SpookyTimeItem(String displayName, Item material, int price, 
                            EnchantmentData[] requiredEnchantments, List<Text> loreTexts) {
            this.displayName = displayName;
            this.material = material;
            this.price = price;
            this.requiredEnchantments = requiredEnchantments;
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
            return displayName;
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
            nbt.putBoolean("SpookyTimeItem", true);
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            
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
            
            SpookyTimeProvider.mergeCustomData(stack, nbt);
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

    
    public static class SpookyTimeTalismanItem implements AutoBuyableItem {
        private final String displayName;
        private final Item material;
        private final int price;
        private final AttributeData[] attributes;
        private final EnchantmentData[] enchantments;
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public SpookyTimeTalismanItem(String displayName, Item material, int price, AttributeData... attributes) {
            this(displayName, material, price, attributes, null);
        }

        public SpookyTimeTalismanItem(String displayName, Item material, int price, AttributeData[] attributes, EnchantmentData[] enchantments) {
            this.displayName = displayName;
            this.material = material;
            this.price = price;
            this.attributes = attributes;
            this.enchantments = enchantments;
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
            return displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);
            
            
            if (enchantments != null && enchantments.length > 0) {
                addEnchantments(stack, enchantments);
            }
            
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.YELLOW));
            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("SpookyTimeItem", true);
            nbt.putBoolean("SpookyTimeTalik", true);
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
                    attributeNbt.putString("Name", UUID.randomUUID().toString());
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
            
            SpookyTimeProvider.mergeCustomData(stack, nbt);
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

    
    public static class SpookyTimeSphereItem implements AutoBuyableItem {
        private final String displayName;
        private final Item material;
        private final int price;
        private final String texture;
        private final AttributeData[] attributes;
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public SpookyTimeSphereItem(String displayName, Item material, int price, String texture, AttributeData... attributes) {
            this.displayName = displayName;
            this.material = material;
            this.price = price;
            this.texture = texture;
            this.attributes = attributes;
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
            return displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.RED));
            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("SpookyTimeItem", true);
            nbt.putBoolean("SpookyTimeSphere", true);
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
                
                GameProfile profile = new GameProfile(skullId, "");
                profile.getProperties().put("textures", new Property("textures", texture));
                stack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
            }
            
            if (attributes != null && attributes.length > 0) {
                NbtList attributeList = new NbtList();
                for (AttributeData attr : attributes) {
                    NbtCompound attributeNbt = new NbtCompound();
                    attributeNbt.putString("AttributeName", attr.attributeName);
                    attributeNbt.putDouble("Amount", attr.amount);
                    attributeNbt.putInt("Operation", attr.operation);
                    attributeNbt.putString("Slot", attr.slot);
                    attributeNbt.putString("Name", UUID.randomUUID().toString());
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
            
            SpookyTimeProvider.mergeCustomData(stack, nbt);
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

    
    public static class SpookyTimePotionItem implements AutoBuyableItem {
        private final String displayName;
        private final Item material;
        private final int price;
        private final List<StatusEffectInstance> effects;
        private final Integer customColor;
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public SpookyTimePotionItem(String displayName, Item material, int price, List<StatusEffectInstance> effects) {
            this(displayName, material, price, effects, null);
        }

        public SpookyTimePotionItem(String displayName, Item material, int price, List<StatusEffectInstance> effects, Integer customColor) {
            this.displayName = displayName;
            this.material = material;
            this.price = price;
            this.effects = effects;
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
            return displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.LIGHT_PURPLE));
            
            if (effects != null && !effects.isEmpty()) {
                PotionContentsComponent potionContents = new PotionContentsComponent(
                    Optional.empty(),
                    customColor != null ? Optional.of(customColor) : Optional.empty(),
                    effects,
                    Optional.empty()
                );
                stack.set(DataComponentTypes.POTION_CONTENTS, potionContents);
            }
            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("SpookyTimeItem", true);
            nbt.putBoolean("SpookyTimePotion", true);
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            
            if (effects != null && !effects.isEmpty()) {
                NbtList effectsList = new NbtList();
                for (StatusEffectInstance effect : effects) {
                    NbtCompound effectNbt = new NbtCompound();
                    effectNbt.putString("effectId", effect.getEffectType().getIdAsString());
                    effectNbt.putInt("amplifier", effect.getAmplifier());
                    effectNbt.putInt("duration", effect.getDuration());
                    effectsList.add(effectNbt);
                }
                nbt.put("effects", effectsList);
            }
            
            SpookyTimeProvider.mergeCustomData(stack, nbt);
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

    
    public static class SpookyTimeSpecialItem implements AutoBuyableItem {
        private final String displayName;
        private final Item material;
        private final int price;
        private final String spookyItemType;
        private final AutoBuyItemSettings settings;
        private boolean enabled;

        public SpookyTimeSpecialItem(String displayName, Item material, int price, String spookyItemType) {
            this.displayName = displayName;
            this.material = material;
            this.price = price;
            this.spookyItemType = spookyItemType;
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
            return displayName;
        }

        @Override
        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(material);
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(Formatting.BOLD, Formatting.YELLOW));
            
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("SpookyTimeItem", true);
            nbt.putBoolean("SpookyTimeSpecial", true);
            nbt.putString("spookyItemType", spookyItemType);
            nbt.putInt("HideFlags", 127);
            nbt.putBoolean("Unbreakable", true);
            
            SpookyTimeProvider.mergeCustomData(stack, nbt);
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

        public String getSpookyItemType() {
            return spookyItemType;
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
