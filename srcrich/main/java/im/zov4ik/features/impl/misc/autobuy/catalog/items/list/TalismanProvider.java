package im.zov4ik.features.impl.misc.autobuy.catalog.items.list;

import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.customitem.CustomItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.defaultsetpricec.Defaultpricec;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TalismanProvider {
    public static List<AutoBuyableItem> getTalismans() {
        List<AutoBuyableItem> talismans = new ArrayList<>();

        
        NbtCompound razdorNbt = createTalismanNbt(
                new AttributeData("minecraft:generic.attack_damage", 4.0, 0, "offhand"),
                new AttributeData("minecraft:generic.max_health", 2.0, 0, "offhand"),
                new AttributeData("minecraft:generic.movement_speed", 0.1, 1, "offhand"),
                new AttributeData("minecraft:generic.attack_speed", 0.1, 1, "offhand"),
                new AttributeData("minecraft:generic.armor", -3.0, 0, "offhand")
        );
        talismans.add(new CustomItem("[★] Талисман Раздора", razdorNbt, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Раздора")));

        
        NbtCompound punisherNbt = createTalismanNbt(
                new AttributeData("minecraft:generic.attack_damage", 7.0, 0, "offhand"),
                new AttributeData("minecraft:generic.max_health", -4.0, 0, "offhand"),
                new AttributeData("minecraft:generic.movement_speed", 0.1, 1, "offhand")
        );
        talismans.add(new CustomItem("[★] Талисман Карателя", punisherNbt, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Карателя")));

        
        NbtCompound krushitelNbt = createTalismanNbt(
                new AttributeData("minecraft:generic.max_health", 4.0, 0, "offhand"),
                new AttributeData("minecraft:generic.attack_damage", 3.0, 0, "offhand"),
                new AttributeData("minecraft:generic.armor_toughness", 2.0, 0, "offhand"),
                new AttributeData("minecraft:generic.armor", 2.0, 0, "offhand")
        );
        talismans.add(new CustomItem("Талисман Крушителя", krushitelNbt, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Крушителя")));

        
        NbtCompound tiranNbt = createTalismanNbt(
                new AttributeData("minecraft:generic.attack_damage", 2.0, 0, "offhand"),
                new AttributeData("minecraft:generic.armor", 2.0, 0, "offhand"),
                new AttributeData("minecraft:generic.max_health", -4.0, 0, "offhand")
        );
        talismans.add(new CustomItem("[★] Талисман Тирана", tiranNbt, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Тирана")));

        
        NbtCompound yarostNbt = createTalismanNbt(
                new AttributeData("minecraft:generic.attack_damage", 5.0, 0, "offhand"),
                new AttributeData("minecraft:generic.max_health", -4.0, 0, "offhand")
        );
        talismans.add(new CustomItem("[★] Талисман Ярости", yarostNbt, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Ярости")));

        
        NbtCompound demonNbt = createTalismanNbt(
                new AttributeData("minecraft:generic.attack_damage", 2.5, 0, "offhand"),
                new AttributeData("minecraft:generic.attack_speed", 0.1, 1, "offhand")
        );
        talismans.add(new CustomItem("[★] Талисман Демона", demonNbt, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Демона")));

        
        NbtCompound vihrNbt = createTalismanNbt(
                new AttributeData("minecraft:generic.max_health", 2.0, 0, "offhand"),
                new AttributeData("minecraft:generic.movement_speed", 0.15, 1, "offhand"),
                new AttributeData("minecraft:generic.attack_speed", 0.15, 1, "offhand")
        );
        talismans.add(new CustomItem("[★] Талисман Вихря", vihrNbt, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Вихря")));

        
        NbtCompound mrakNbt = createTalismanNbt(
                new AttributeData("minecraft:generic.armor", 1.5, 0, "offhand"),
                new AttributeData("minecraft:generic.max_health", 1.5, 0, "offhand")
        );
        talismans.add(new CustomItem("[★] Талисман Мрака", mrakNbt, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Мрака")));

        return talismans;
    }

    private static NbtCompound createTalismanNbt(AttributeData... attributes) {
        NbtCompound nbt = new NbtCompound();
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
        return nbt;
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
}
