/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.util.Hand
 */
package moscow.rockstar.utility.integration;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.modules.player.GuiMove;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class SwapIntegration
implements IMinecraft {
    private Item itemToUse = null;
    private HotbarSlot originalSlot = null;
    private boolean isProcessingItem = false;
    private ItemSlot targetSlot = null;
    private final Timer itemUseTimer = new Timer();
    private ItemUseState currentState = ItemUseState.IDLE;
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (this.isProcessingItem) {
            this.processItemUse();
        }
    };

    public SwapIntegration() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    private void processItemUse() {
        if (SwapIntegration.mc.player == null || SwapIntegration.mc.world == null || SwapIntegration.mc.interactionManager == null || SwapIntegration.mc.player.getItemCooldownManager() == null) {
            this.isProcessingItem = false;
            this.currentState = ItemUseState.IDLE;
            return;
        }
        if (!(this.targetSlot instanceof HotbarSlot)) {
            Rockstar.getInstance().getModuleManager().getModule(GuiMove.class).setStay(true);
        }
        switch (this.currentState.ordinal()) {
            case 1: {
                if (this.targetSlot instanceof HotbarSlot) {
                    SwapIntegration.mc.interactionManager.sendSequencedPacket(SwapIntegration.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, SwapIntegration.mc.player.getYaw(), SwapIntegration.mc.player.getPitch()));
                    this.currentState = ItemUseState.RETURNING_SLOT;
                    break;
                }
                if (!Rockstar.getInstance().getModuleManager().getModule(GuiMove.class).canSend()) break;
                SwapIntegration.mc.interactionManager.sendSequencedPacket(SwapIntegration.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, SwapIntegration.mc.player.getYaw(), SwapIntegration.mc.player.getPitch()));
                this.currentState = ItemUseState.RETURNING_SLOT;
                break;
            }
            case 2: {
                if (this.targetSlot instanceof HotbarSlot) {
                    InventoryUtility.selectHotbarSlot(this.originalSlot);
                    this.resetUseState();
                    break;
                }
                if (!Rockstar.getInstance().getModuleManager().getModule(GuiMove.class).canSend()) break;
                HotbarSlot currentSlot = InventoryUtility.getCurrentHotbarSlot();
                InventoryUtility.hotbarSwap(this.targetSlot.getIdForServer(), this.originalSlot.getSlotId());
                this.resetUseState();
                break;
            }
            default: {
                this.isProcessingItem = false;
                this.currentState = ItemUseState.IDLE;
            }
        }
    }

    public void useItem(Item itemType) {
        if (SwapIntegration.mc.player == null || SwapIntegration.mc.world == null || SwapIntegration.mc.interactionManager == null || SwapIntegration.mc.currentScreen != null) {
            return;
        }
        if (this.isProcessingItem) {
            return;
        }
        SlotGroup<ItemSlot> group = SlotGroups.hotbar().and(SlotGroups.inventory());
        ItemSlot itemSlot = group.findItem(itemType);
        if (itemSlot == null) {
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.ERROR, "\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d", "\u0412\u0430\u043c \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u0438\u043c\u0435\u0442\u044c " + itemType.getName().getString() + " \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435");
            return;
        }
        if (SwapIntegration.mc.player.getItemCooldownManager().isCoolingDown(itemSlot.itemStack())) {
            return;
        }
        this.itemToUse = itemType;
        this.originalSlot = InventoryUtility.getCurrentHotbarSlot();
        this.targetSlot = itemSlot;
        this.isProcessingItem = true;
        this.currentState = ItemUseState.USING_ITEM;
        this.itemUseTimer.reset();
        if (itemSlot instanceof HotbarSlot) {
            HotbarSlot itemHotbarSlot = (HotbarSlot)itemSlot;
            if (InventoryUtility.getCurrentHotbarSlot().item() != itemType) {
                InventoryUtility.selectHotbarSlot(itemHotbarSlot);
            }
        } else if (itemSlot instanceof InventorySlot) {
            InventorySlot itemInventorySlot = (InventorySlot)itemSlot;
            HotbarSlot currentSlot = InventoryUtility.getCurrentHotbarSlot();
            InventoryUtility.hotbarSwap(itemInventorySlot.getIdForServer(), currentSlot.getSlotId());
        }
    }

    private void resetUseState() {
        this.isProcessingItem = false;
        this.currentState = ItemUseState.IDLE;
        this.itemToUse = null;
        this.originalSlot = null;
        this.targetSlot = null;
    }

    private static enum ItemUseState {
        IDLE,
        USING_ITEM,
        RETURNING_SLOT;

    }
}

