package im.zov4ik.features.impl.misc.funtime.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;

public final class FunTimeAutoSellUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private FunTimeAutoSellUtil() {}

    public static void moveToFirstSlot(int slotIndex) {
        if (mc.player == null || mc.interactionManager == null) return;
        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, slotIndex, 0, SlotActionType.SWAP, mc.player);
    }

    public static void putBack(int fromHotbarSlot, int toInventorySlot) {
        if (mc.player == null || mc.interactionManager == null) return;
        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, toInventorySlot, fromHotbarSlot, SlotActionType.SWAP, mc.player);
    }

    public static boolean isErrorMessage(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("ошибка") && (lower.contains("предмет") || lower.contains("не существует"));
    }

    public static void sellItem(int price, int count) {
        if (mc.player == null || mc.player.networkHandler == null) return;
        mc.player.networkHandler.sendChatCommand("ah sell " + price + " " + count);
    }
}
