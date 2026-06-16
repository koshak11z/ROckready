package im.zov4ik.features.impl.misc.autobuy.catalog.items.list;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;

public class KrushItems {

    public static ItemStack getHelmet() {
        ItemStack stack = new ItemStack(Items.NETHERITE_HELMET);
        addEnchantments(stack,
                Enchantments.UNBREAKING, 5,
                Enchantments.MENDING, 1,
                Enchantments.FIRE_PROTECTION, 5,
                Enchantments.PROJECTILE_PROTECTION, 5,
                Enchantments.BLAST_PROTECTION, 5,
                Enchantments.AQUA_AFFINITY, 1,
                Enchantments.RESPIRATION, 3,
                Enchantments.PROTECTION, 5);
        setupItem(stack, createStyledName("Шлем Крушителя"),
                List.of(
                        Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
                ));
        return stack;
    }

    public static ItemStack getChestplate() {
        ItemStack stack = new ItemStack(Items.NETHERITE_CHESTPLATE);
        addEnchantments(stack,
                Enchantments.BLAST_PROTECTION, 5,
                Enchantments.MENDING, 1,
                Enchantments.FIRE_PROTECTION, 5,
                Enchantments.PROJECTILE_PROTECTION, 5,
                Enchantments.PROTECTION, 5,
                Enchantments.UNBREAKING, 5);
        setupItem(stack, createStyledName("Нагрудник Крушителя"),
                List.of(
                        Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
                ));
        return stack;
    }

    public static ItemStack getLeggings() {
        ItemStack stack = new ItemStack(Items.NETHERITE_LEGGINGS);
        addEnchantments(stack,
                Enchantments.BLAST_PROTECTION, 5,
                Enchantments.MENDING, 1,
                Enchantments.FIRE_PROTECTION, 5,
                Enchantments.PROJECTILE_PROTECTION, 5,
                Enchantments.PROTECTION, 5,
                Enchantments.UNBREAKING, 5);
        setupItem(stack, createStyledName("Поножи Крушителя"),
                List.of(
                        Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
                ));
        return stack;
    }

    public static ItemStack getBoots() {
        ItemStack stack = new ItemStack(Items.NETHERITE_BOOTS);
        addEnchantments(stack,
                Enchantments.MENDING, 1,
                Enchantments.FIRE_PROTECTION, 5,
                Enchantments.DEPTH_STRIDER, 3,
                Enchantments.PROJECTILE_PROTECTION, 5,
                Enchantments.FEATHER_FALLING, 4,
                Enchantments.SOUL_SPEED, 3,
                Enchantments.BLAST_PROTECTION, 5,
                Enchantments.PROTECTION, 5,
                Enchantments.UNBREAKING, 5);
        setupItem(stack, createStyledName("Ботинки Крушителя"),
                List.of(
                        Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
                ));
        return stack;
    }

    public static ItemStack getSword() {
        ItemStack stack = new ItemStack(Items.NETHERITE_SWORD);
        addEnchantments(stack,
                Enchantments.UNBREAKING, 5,
                Enchantments.MENDING, 1,
                Enchantments.SMITE, 7,
                Enchantments.SWEEPING_EDGE, 3,
                Enchantments.FIRE_ASPECT, 2,
                Enchantments.BANE_OF_ARTHROPODS, 7,
                Enchantments.SHARPNESS, 7,
                Enchantments.LOOTING, 5);
        setupItem(stack, createStyledName("Меч Крушителя"),
                List.of(
                        Text.literal("Опытный III").formatted(Formatting.GRAY),
                        Text.literal("Вампиризм II").formatted(Formatting.GRAY),
                        Text.literal("Окисление II").formatted(Formatting.GRAY),
                        Text.literal("РЇРґ III").formatted(Formatting.GRAY),
                        Text.literal("Детекция III").formatted(Formatting.GRAY),
                        Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
                ));
        return stack;
    }

    public static ItemStack getPickaxe() {
        ItemStack stack = new ItemStack(Items.NETHERITE_PICKAXE);
        addEnchantments(stack,
                Enchantments.UNBREAKING, 5,
                Enchantments.MENDING, 1,
                Enchantments.EFFICIENCY, 10,
                Enchantments.FORTUNE, 5);
        setupItem(stack, createStyledName("Кирка Крушителя"),
                List.of(
                        Text.literal("Бульдозер II").formatted(Formatting.GRAY),
                        Text.literal("Опытный III").formatted(Formatting.GRAY),
                        Text.literal("Магнит").formatted(Formatting.GRAY),
                        Text.literal("Авто-Плавка").formatted(Formatting.GRAY),
                        Text.literal("Паутина").formatted(Formatting.GRAY),
                        Text.literal("Пингер").formatted(Formatting.GRAY),
                        Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
                ));
        return stack;
    }

    public static ItemStack getCrossbow() {
        ItemStack stack = new ItemStack(Items.CROSSBOW);
        addEnchantments(stack,
                Enchantments.QUICK_CHARGE, 3,
                Enchantments.MENDING, 1,
                Enchantments.PIERCING, 5,
                Enchantments.UNBREAKING, 3,
                Enchantments.MULTISHOT, 1);
        setupItem(stack, createStyledName("Арбалет Крушителя"),
                List.of(
                        Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
                ));
        return stack;
    }

    public static ItemStack getTrident() {
        ItemStack stack = new ItemStack(Items.TRIDENT);
        addEnchantments(stack,
                Enchantments.UNBREAKING, 5,
                Enchantments.MENDING, 1,
                Enchantments.CHANNELING, 1,
                Enchantments.FIRE_ASPECT, 2,
                Enchantments.IMPALING, 5,
                Enchantments.SHARPNESS, 7,
                Enchantments.LOYALTY, 3);
        setupItem(stack, createStyledName("Трезубец Крушителя"),
                List.of(
                        Text.literal("Скаут III").formatted(Formatting.GRAY),
                        Text.literal("Опытный III").formatted(Formatting.GRAY),
                        Text.literal("Вампиризм II").formatted(Formatting.GRAY),
                        Text.literal("Ступор III").formatted(Formatting.GRAY),
                        Text.literal("Притяжение II").formatted(Formatting.GRAY),
                        Text.literal("Окисление II").formatted(Formatting.GRAY),
                        Text.literal("Возвращение").formatted(Formatting.GRAY),
                        Text.literal("Подрывник").formatted(Formatting.GRAY),
                        Text.literal("РЇРґ III").formatted(Formatting.GRAY),
                        Text.literal("Детекция III").formatted(Formatting.GRAY),
                        Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
                ));
        return stack;
    }

    public static ItemStack getMace() {
        ItemStack stack = new ItemStack(Items.MACE);
        addEnchantments(stack,
                Enchantments.SHARPNESS, 7,
                Enchantments.SMITE, 7,
                Enchantments.BANE_OF_ARTHROPODS, 7,
                Enchantments.DENSITY, 5,
                Enchantments.BREACH, 3,
                Enchantments.SWEEPING_EDGE, 3,
                Enchantments.FIRE_ASPECT, 2,
                Enchantments.LOOTING, 5,
                Enchantments.UNBREAKING, 5,
                Enchantments.MENDING, 1);
        setupItem(stack, createStyledName("Булава Крушителя"),
                List.of(
                        Text.literal("Опытный III").formatted(Formatting.GRAY),
                        Text.literal("Вампиризм II").formatted(Formatting.GRAY),
                        Text.literal("Окисление II").formatted(Formatting.GRAY),
                        Text.literal("РЇРґ III").formatted(Formatting.GRAY),
                        Text.literal("Детекция III").formatted(Formatting.GRAY),
                        Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
                ));
        return stack;
    }

    private static void addEnchantments(ItemStack stack, Object... enchantments) {
        NbtCompound customData = stack.get(DataComponentTypes.CUSTOM_DATA) != null ? stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt() : new NbtCompound();
        NbtList requiredEnchantments = customData.contains("RequiredEnchantments", 9)
                ? customData.getList("RequiredEnchantments", 10)
                : new NbtList();
        for (int i = 0; i < enchantments.length; i += 2) {
            RegistryKey<Enchantment> enchantKey = (RegistryKey<Enchantment>) enchantments[i];
            int level = (int) enchantments[i + 1];
            NbtCompound enchantNbt = new NbtCompound();
            enchantNbt.putString("id", enchantKey.getValue().toString());
            enchantNbt.putShort("lvl", (short) level);
            requiredEnchantments.add(enchantNbt);
        }
        customData.put("RequiredEnchantments", requiredEnchantments);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
    }

    private static void setupItem(ItemStack stack, Text name, List<Text> lore) {
        stack.set(DataComponentTypes.CUSTOM_NAME, name);
        NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA) != null ? stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt() : new NbtCompound();
        nbt.putInt("HideFlags", 127);
        nbt.putBoolean("Unbreakable", true);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        if (!lore.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        }
    }

    private static Text createStyledName(String baseName) {
        return Text.literal(baseName).formatted(Formatting.BOLD, Formatting.DARK_RED);
    }
}
