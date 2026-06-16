package im.zov4ik.features.impl.misc.autobuy.catalog.items.list;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.customitem.NbtDefinedItem;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ManualProvider {
    public static List<AutoBuyableItem> getBaseItems() {
        List<AutoBuyableItem> items = new ArrayList<>();
        items.add(makeNamedItem("Золотое яблоко", Items.GOLDEN_APPLE));
        items.add(makeNamedItem("Зачарованное золотое яблоко", Items.ENCHANTED_GOLDEN_APPLE));
        items.add(makeNamedItem("Порох", Items.GUNPOWDER));
        items.add(makeNamedItem("Тотем бессмертия", Items.TOTEM_OF_UNDYING));

        ItemStack shulkerAny = new ItemStack(Items.PURPLE_SHULKER_BOX);
        items.add(new NbtDefinedItem("Шалкер (все)", "Шалкер", shulkerAny, 0, false));

        return items;
    }

    public static List<AutoBuyableItem> getSpookyNbtItems() {
        List<AutoBuyableItem> items = new ArrayList<>();

        items.add(makeNamedItem("Загадочный маяк", Items.BEACON));
        items.add(makeNamedItem("Дезориентация", Items.ENDER_EYE));
        items.add(makeNamedItem("Явная пыль", Items.SUGAR));
        items.add(makeNamedItem("Пласт", Items.DRIED_KELP));
        items.add(makeNamedItem("Божья аура", Items.PHANTOM_MEMBRANE));
        items.add(makeNamedItem("Снежок заморозка", Items.SNOWBALL));
        items.add(makeNamedItem("Трапка", Items.NETHERITE_SCRAP));
        items.add(makeNamedItem("Блок дамагер", Items.JIGSAW));
        items.add(makeNamedItem("Перегрузчик чанков [5x5]", Items.STRUCTURE_BLOCK));
        items.add(makeNamedItem("Прогрузчик чанков [3x3]", Items.STRUCTURE_BLOCK));
        items.add(makeNamedItem("Прогрузчик чанков [1x1]", Items.STRUCTURE_BLOCK));
        items.add(makeNamedItem("Нерушимые элитры", Items.ELYTRA));
        items.add(makeNamedItem("Пузырёк опыта (50 ур.)", Items.EXPERIENCE_BOTTLE));
        items.add(makeLoreItem("Сигнальный огонь (обычный)", "Сигнальный огонь", Items.CAMPFIRE,
                List.of("На месте огня появится", "Мистический сундук", "Вид: Обычный")));
        items.add(makeLoreItem("Сигнальный огонь (богатый)", "Сигнальный огонь", Items.CAMPFIRE,
                List.of("На месте огня появится", "Мистический сундук", "Вид: Богатый")));
        items.add(makeLoreItem("Сигнальный огонь (случайный)", "Сигнальный огонь", Items.CAMPFIRE,
                List.of("На месте огня появится", "Мистический сундук", "Вид: Случайный")));
        items.add(makeLoreItem("Сигнальный огонь (мистический)", "Сигнальный огонь", Items.SOUL_CAMPFIRE,
                List.of("На месте огня появится", "Мистический сундук", "Вид: Легендарный")));

        items.add(makePotionItem("Снотворное", Items.SPLASH_POTION, 4737096));
        items.add(makePotionItem("Зелье Гнева", Items.SPLASH_POTION, 10040115));
        items.add(makePotionItem("Зелье Палладна", Items.SPLASH_POTION, 65535));
        items.add(makePotionItem("Зелье Радиации", Items.SPLASH_POTION, 3329330));
        items.add(makePotionItem("Зелье Ассасина", Items.SPLASH_POTION, 3355443));
        items.add(makePotionItem("Святая вода", Items.SPLASH_POTION, 16777215));
        items.add(makePotionItem("Несоздаваемое зелье", Items.POTION, 3694534));

        items.add(makeSphereItem("Сфера Титана",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODFlOTY5ODQ1OGI3ODQxYzk2YWU0ZjI0ZWM4NGFlMDE3MjQxMDA2NDFjNTY0ZTJhN2IxODVmNDA2ZThlZDIzIn19fQ=="));
        items.add(makeSphereItem("Сфера Сатира",
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODYwODUyOCwKICAicHJvZmlsZUlkIiA6ICJkMTQ4NjFiM2UwZmM0Njk5OTFlMTcyNTllMzdiZjZhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJyYXhpdG9jbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NzFhOWE0OThiNGZhNWVjNDkzNjJmOWJjODhlZGE0ZjUyYjA0ZGU0OWQ3NWFhM2NhMzMyYTFmZWExYWEwZTU3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="));
        items.add(makeSphereItem("Сфера Бестии",
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0MzgzNDkzMCwKICAicHJvZmlsZUlkIiA6ICI1MzUzNWIxN2M0ZDY0NWQ0YWUwY2U2ZjM4Zjk0NTFjYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJVYml2aXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQxMWFjMTczODFiOWZjZTliYWIzYzcyYWZkYjdmMTk4NTcwZGFmNDczMmJkODExZDMxYzIyN2Q4MGZhMzliMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"));
        items.add(makeSphereItem("Сфера Эрида",
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzg2MTE4NywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZlNGUyZjEwNDdmM2VjNmU5ZTQ1OTE4NDczOWUzM2I3YzFmYzYzYWQ4MjAyYmRhYjlmMDI0NTA4YWRkMjNlNWIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="));

        items.add(makeNamedItem("Талисман Демона", Items.TOTEM_OF_UNDYING));
        items.add(makeNamedItem("Талисман Карателя", Items.TOTEM_OF_UNDYING));
        items.add(makeNamedItem("Талисман Ирака", Items.TOTEM_OF_UNDYING));
        items.add(makeNamedItem("Талисман Вихря", Items.TOTEM_OF_UNDYING));
        items.add(makeNamedItem("Талисман Крушителя", Items.TOTEM_OF_UNDYING));
        items.add(makeNamedItem("Талисман Раздора", Items.TOTEM_OF_UNDYING));
        items.add(makeNamedItem("Талисман Тирана", Items.TOTEM_OF_UNDYING));
        items.add(makeNamedItem("Талисман Ярости", Items.TOTEM_OF_UNDYING));

        items.add(makeNamedItem("Драконий скин", Items.PAPER));
        items.add(makeNamedItem("Незабвенный скин", Items.PAPER));

        items.add(makeNamedItem("Шлем Крушителя", Items.NETHERITE_HELMET));
        items.add(makeNamedItem("Нагрудник Крушителя", Items.NETHERITE_CHESTPLATE));
        items.add(makeNamedItem("Поножи Крушителя", Items.NETHERITE_LEGGINGS));
        items.add(makeNamedItem("Ботинки Крушителя", Items.NETHERITE_BOOTS));
        items.add(makeNamedItem("Меч Крушителя", Items.NETHERITE_SWORD));
        items.add(makeNamedItem("Арбалет Крушителя", Items.CROSSBOW));
        items.add(makeNamedItem("Трезубец Крушителя", Items.TRIDENT));
        items.add(makeNamedItem("Лук Крушителя", Items.BOW));

        return items;
    }

    private static NbtDefinedItem makeNamedItem(String displayName, Item item) {
        return makeNamedItem(displayName, displayName, item);
    }

    private static NbtDefinedItem makeNamedItem(String displayName, String matchName, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(matchName));
        return new NbtDefinedItem(displayName, matchName, stack, 0, false);
    }

    private static NbtDefinedItem makeLoreItem(String displayName, String matchName, Item item, List<String> loreLines) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(matchName));
        if (loreLines != null && !loreLines.isEmpty()) {
            List<Text> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(Text.literal(line));
            }
            stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        }
        return new NbtDefinedItem(displayName, matchName, stack, 0, true);
    }

    private static NbtDefinedItem makePotionItem(String displayName, Item item, int color) {
        return makePotionItem(displayName, displayName, item, color);
    }

    private static NbtDefinedItem makePotionItem(String displayName, String matchName, Item item, int color) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(matchName));
        PotionContentsComponent contents = new PotionContentsComponent(Optional.empty(), Optional.of(color), List.of(), Optional.empty());
        stack.set(DataComponentTypes.POTION_CONTENTS, contents);
        return new NbtDefinedItem(displayName, matchName, stack, 0, false);
    }

    private static NbtDefinedItem makeSphereItem(String displayName, String texture) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        Text name = Text.literal("[★] ").formatted(Formatting.DARK_RED)
                .append(Text.literal(displayName).formatted(Formatting.GOLD));
        stack.set(DataComponentTypes.CUSTOM_NAME, name);

        if (texture != null && !texture.isBlank()) {
            UUID skullId = UUID.nameUUIDFromBytes(texture.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            GameProfile profile = new GameProfile(skullId, "");
            profile.getProperties().put("textures", new Property("textures", texture));
            stack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
        }

        return new NbtDefinedItem(displayName, displayName, stack, 0, false);
    }
}
