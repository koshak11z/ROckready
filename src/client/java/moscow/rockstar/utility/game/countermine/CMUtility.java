/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.EquipmentSlot$Type
 *  net.minecraft.entity.decoration.DisplayEntity$TextDisplayEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.registry.RegistryWrapper$WrapperLookup
 *  net.minecraft.resource.Resource
 *  net.minecraft.resource.ResourceManager
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.PlainTextContent
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.utility.game.countermine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class CMUtility
implements IMinecraft {
    public static Text removeTextFromComponent(Text original, String textToRemove) {
        if (original instanceof MutableText) {
            MutableText mutableText = (MutableText)original;
            return CMUtility.removeTextFromMutableText(mutableText.copy(), textToRemove);
        }
        return CMUtility.removeTextFromMutableText(original.copy(), textToRemove);
    }

    private static MutableText removeTextFromMutableText(MutableText text, String textToRemove) {
        PlainTextContent literalContent;
        String content;
        if (text instanceof PlainTextContent && (content = (literalContent = (PlainTextContent)text).string()).contains(textToRemove)) {
            String cleanedContent = content.replace(textToRemove, "");
            MutableText newText = Text.literal((String)cleanedContent);
            newText.setStyle(text.getStyle());
            for (Text sibling : text.getSiblings()) {
                newText.append(CMUtility.removeTextFromComponent(sibling, textToRemove));
            }
            return newText;
        }
        MutableText result = Text.empty().setStyle(text.getStyle());
        result.append((Text)text.copy());
        ArrayList<Text> cleanedSiblings = new ArrayList<Text>();
        for (Text sibling : text.getSiblings()) {
            Text cleanedSibling = CMUtility.removeTextFromComponent(sibling, textToRemove);
            if (cleanedSibling.getString().isEmpty()) continue;
            cleanedSiblings.add(cleanedSibling);
        }
        MutableText finalText = Text.empty().setStyle(text.getStyle());
        String mainContent = CMUtility.getMainContent((Text)text);
        if (!mainContent.isEmpty() && !(mainContent = mainContent.replace(textToRemove, "")).isEmpty()) {
            finalText.append((Text)Text.literal((String)mainContent).setStyle(text.getStyle()));
        }
        for (Text sibling : cleanedSiblings) {
            finalText.append(sibling);
        }
        return finalText;
    }

    private static String getMainContent(Text text) {
        if (text instanceof PlainTextContent) {
            PlainTextContent literalContent = (PlainTextContent)text;
            return literalContent.string();
        }
        return "";
    }

    public static void removeAllArmor() {
        for (Entity entity : CMUtility.mc.world.getPlayers()) {
            if (!(entity instanceof PlayerEntity)) continue;
            PlayerEntity livingEntity = (PlayerEntity)entity;
            CMUtility.removeArmorFromEntity(livingEntity);
        }
    }

    private static void removeArmorFromPlayer(PlayerEntity player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack currentItem;
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR && slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND || (currentItem = player.getEquippedStack(slot)).isEmpty()) continue;
            player.getInventory().insertStack(currentItem.copy());
            player.equipStack(slot, ItemStack.EMPTY);
        }
    }

    private static void removeArmorFromEntity(PlayerEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack currentArmor;
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR || (currentArmor = entity.getEquippedStack(slot)).isEmpty()) continue;
            entity.getInventory().insertStack(currentArmor.copy());
            entity.equipStack(slot, ItemStack.EMPTY);
        }
    }

    public static boolean isPlayerOnline(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) {
            return false;
        }
        for (PlayerListEntry entry : networkHandler.getPlayerList()) {
            if (!entry.getProfile().getName().equals(playerName)) continue;
            return true;
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

    public static String getModelIdFromNbt(ItemStack itemStack, RegistryWrapper.WrapperLookup registryManager) {
        NbtCompound components;
        NbtCompound compound;
        NbtElement nbt = itemStack.toNbtAllowEmpty(registryManager);
        if (nbt instanceof NbtCompound && (compound = (NbtCompound)nbt).contains("components", 10) && (components = compound.getCompound("components")).contains("minecraft:item_model", 8)) {
            return components.getString("minecraft:item_model");
        }
        return null;
    }

    public static boolean isHologramNearby(Entity entity, ClientWorld world, double searchRadius) {
        for (Entity nearbyEntity : world.getEntities()) {
            DisplayEntity.TextDisplayEntity textDisplay;
            if (!(nearbyEntity instanceof DisplayEntity.TextDisplayEntity) || (textDisplay = (DisplayEntity.TextDisplayEntity)nearbyEntity).getText() == null || textDisplay.getText().getString().isEmpty() || !((double)entity.distanceTo((Entity)textDisplay) < searchRadius)) continue;
            return true;
        }
        return false;
    }

    public static Rotation calculateRotation(Vec3d targetPos) {
        Vec3d eyes = CMUtility.mc.player.getEyePos();
        double dx = targetPos.x - eyes.x;
        double dy = targetPos.y - eyes.y;
        double dz = targetPos.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        return new Rotation(yaw, pitch);
    }

    @Generated
    private CMUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
