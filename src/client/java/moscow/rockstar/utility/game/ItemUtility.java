/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.BundleContentsComponent
 *  net.minecraft.component.type.ContainerComponent
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.registry.DynamicRegistryManager
 *  net.minecraft.registry.RegistryWrapper$WrapperLookup
 *  net.minecraft.resource.Resource
 *  net.minecraft.resource.ResourceManager
 *  net.minecraft.util.Identifier
 */
package moscow.rockstar.utility.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Generated;
import moscow.rockstar.utility.game.DonateItem;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class ItemUtility
implements IMinecraft {
    public static List<ItemStack> getItemsInShulker(ItemStack s) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        ContainerComponent container = (ContainerComponent)s.get(DataComponentTypes.CONTAINER);
        if (container == null) {
            BundleContentsComponent container1 = (BundleContentsComponent)s.get(DataComponentTypes.BUNDLE_CONTENTS);
            if (container1 == null) {
                return items;
            }
            for (ItemStack stack : container1.iterate()) {
                items.add(stack);
            }
            return items;
        }
        for (ItemStack stack : container.iterateNonEmpty()) {
            items.add(stack);
        }
        return items;
    }

    public static NbtCompound getNBT(ItemStack stack) {
        NbtCompound components;
        NbtCompound compound;
        DynamicRegistryManager registries = ItemUtility.mc.world.getRegistryManager();
        NbtElement nbt = stack.toNbtAllowEmpty((RegistryWrapper.WrapperLookup)registries);
        if (nbt instanceof NbtCompound && (compound = (NbtCompound)nbt).contains("components", 10) && (components = compound.getCompound("components")).contains("minecraft:custom_data", 10)) {
            return components.getCompound("minecraft:custom_data");
        }
        return null;
    }

    public static boolean checkDonItem(ItemStack itemStack, String startWith) {
        NbtCompound customData = ItemUtility.getNBT(itemStack);
        if (customData == null) {
            return false;
        }
        if (customData.contains("don-item")) {
            String donItemName = customData.getString("don-item");
            return donItemName.contains(startWith);
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String findHashedModel(String hashedId) {
        ResourceManager resourceManager = mc.getResourceManager();
        Identifier modelPath = Identifier.of((String)"minecraft", (String)("models/item/" + hashedId.replace("minecraft:", "") + ".json"));
        Optional resource = resourceManager.getResource(modelPath);
        if (!resource.isPresent()) {
            return null;
        }
        try (BufferedReader reader = ((Resource)resource.get()).getReader()) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            System.err.println("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u0438 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u043e\u0439 \u043c\u043e\u0434\u0435\u043b\u0438: " + e.getMessage());
            return null;
        }
    }

    public static boolean isDonItem(ItemStack itemStack) {
        NbtCompound customData = ItemUtility.getNBT(itemStack);
        if (customData == null) {
            return false;
        }
        return customData.contains("don-item");
    }

    public static String donNBT(ItemStack itemStack) {
        NbtCompound customData = ItemUtility.getNBT(itemStack);
        if (customData == null) {
            return "";
        }
        NbtCompound sphereEffect = customData.getCompound("sphereEffect");
        if (customData.contains("don-item")) {
            return customData.getString("don-item");
        }
        if (customData.contains("spooky-item")) {
            return customData.getString("spooky-item");
        }
        if (ServerUtility.is("holyworld") && customData.contains("sphereEffect", 10) && itemStack.getItem() == Items.TOTEM_OF_UNDYING && sphereEffect.contains("rank")) {
            if (sphereEffect.getString("rank").equals("ETERNITY")) {
                return sphereEffect.getString("name");
            }
            return sphereEffect.getString("rank");
        }
        return "";
    }

    public static DonateItem getDonateItem(ItemStack stack) {
        for (DonateItem item : DonateItem.values()) {
            for (String key : item.getNbt()) {
                if (!ItemUtility.donNBT(stack).equals(key)) continue;
                return item;
            }
        }
        return null;
    }

    public static int totemFactor(ItemStack stack) {
        if (stack.hasEnchantments()) {
            for (DonateItem item : DonateItem.values()) {
                for (String key : item.getNbt()) {
                    if (!ItemUtility.donNBT(stack).equals(key)) continue;
                    return 12 - item.getTotem();
                }
            }
            return 0;
        }
        return -1;
    }

    public static int bestFactor(ItemStack stack) {
        if (stack.hasEnchantments() || ItemUtility.isDonItem(stack)) {
            for (DonateItem item : DonateItem.values()) {
                for (String key : item.getNbt()) {
                    if (!ItemUtility.donNBT(stack).equals(key)) continue;
                    return 15 - item.getFactor();
                }
            }
            return 16;
        }
        return 17;
    }

    @Generated
    private ItemUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
