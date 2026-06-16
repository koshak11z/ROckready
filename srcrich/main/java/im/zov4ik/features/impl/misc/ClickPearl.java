package im.zov4ik.features.impl.misc;

import antidaunleak.api.annotation.Native;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BindSetting;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.interactions.inv.InventoryFlowManager;
import im.zov4ik.utils.interactions.inv.InventoryResult;
import im.zov4ik.utils.interactions.inv.InventoryToolkit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ClickPearl extends Module {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    private final BindSetting keySetting = new BindSetting("Кнопка", "Кнопка для использования");

    private boolean prevKeyPressed = false;
    private long lastThrowTime = 0L;

    public ClickPearl() {
        super("ClickPearl", "Click Pearl", ModuleCategory.MISC);
        setup(keySetting);
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (MC.player == null || MC.world == null) {
            return;
        }

        boolean keyDown = isBindActive();
        if (!prevKeyPressed && keyDown && System.currentTimeMillis() - lastThrowTime > 100L) {
            lastThrowTime = System.currentTimeMillis();
            throwPearl();
        }
        prevKeyPressed = keyDown;
    }

    private void throwPearl() {
        if (MC.currentScreen != null || !InventoryFlowManager.script.isFinished()) {
            return;
        }

        int selectedSlot = MC.player.getInventory().selectedSlot;
        InventoryResult hotbar = InventoryToolkit.findItemInHotBar(Items.ENDER_PEARL);
        if (hotbar.found()) {
            InventoryFlowManager.addUseItemTask(hotbar.slot() + 36, selectedSlot, this::usePearl);
            return;
        }

        InventoryResult inventory = InventoryToolkit.findItemInInventory(Items.ENDER_PEARL);
        if (!inventory.found()) {
            ChatMessage.brandmessage("Нету жемчуга");
            return;
        }

        InventoryFlowManager.addUseItemTask(inventory.slot(), selectedSlot, this::usePearl);
    }

    private void usePearl() {
        PlayerInteractionHelper.interactItem(Hand.MAIN_HAND);
    }

    private boolean isBindActive() {
        long window = MC.getWindow().getHandle();
        int key = keySetting.getKey();

        if (key >= GLFW.GLFW_MOUSE_BUTTON_1 && key <= GLFW.GLFW_MOUSE_BUTTON_8) {
            return GLFW.glfwGetMouseButton(window, key) == GLFW.GLFW_PRESS;
        }
        return InputUtil.isKeyPressed(window, key);
    }
}
