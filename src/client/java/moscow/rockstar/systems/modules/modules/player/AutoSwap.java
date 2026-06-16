/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package moscow.rockstar.systems.modules.modules.player;

import java.util.Comparator;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@ModuleInfo(name="Auto Swap", category=ModuleCategory.PLAYER)
public class AutoSwap
extends BaseModule {
    private final BindSetting button = new BindSetting(this, "modules.settings.auto_swap.button");
    private final ModeSetting itemMode = new ModeSetting(this, "modules.settings.auto_swap.item");
    private final ModeSetting.Value swapTal = new ModeSetting.Value(this.itemMode, "modules.settings.auto_swap.item.talisman").select();
    private final ModeSetting swapToMode = new ModeSetting(this, "modules.settings.auto_swap.swap_to");
    private final ModeSetting.Value swapToTal = new ModeSetting.Value(this.swapToMode, "modules.settings.auto_swap.swap_to.talisman").select();
    private final BooleanSetting syncGuiMove = new BooleanSetting(this, "Синхронизировать с GuiMove");
    private final Timer timer = new Timer();
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (event.getAction() != 1) {
            return;
        }
        if (this.button.isKey(event.getKey())) {
            this.swap();
        }
    };
    private final EventListener<MouseEvent> onMouseEvent = event -> {
        if (this.button.isKey(event.getButton())) {
            this.swap();
        }
    };

    public AutoSwap() {
        new ModeSetting.Value(this.swapToMode, "modules.settings.auto_swap.swap_to.orb");
        new ModeSetting.Value(this.itemMode, "modules.settings.auto_swap.item.orb");
    }

    /*
     * Unable to fully structure code
     */
    private void swap() {
        if (AutoSwap.mc.currentScreen != null || ServerUtility.isST() && !this.timer.finished(1000L)) {
            return;
        }
        this.syncWithGuiMove();
        SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar()).and(SlotGroups.offhand());
        List<ItemSlot> slots = slotsToSearch.findItems(this.swapTal.isSelected() ? Items.TOTEM_OF_UNDYING : Items.PLAYER_HEAD);
        List<ItemSlot> slots1 = slotsToSearch.findItems(this.swapToTal.isSelected() ? Items.TOTEM_OF_UNDYING : Items.PLAYER_HEAD);
        ItemSlot slot = slots.stream().min(Comparator.comparingInt(AutoSwap::lambda$swap$2)).orElse(null);
        ItemSlot slot1 = slots1.stream().filter(candidate -> AutoSwap.lambda$swap$3(slot, candidate)).min(Comparator.comparingInt(AutoSwap::lambda$swap$4)).orElse(null);
        if (slot == null || slot1 == null) {
            return;
        }
        boolean canSwap = true;
        if (AutoSwap.mc.player.getOffHandStack().getItem() != slot.item() && AutoSwap.mc.player.getOffHandStack().getItem() != slot1.item()) {
            InventoryUtility.moveToOffHand(slot);
            canSwap = false;
        } else if (slot instanceof HotbarSlot) {
            HotbarSlot hotbarSlot = (HotbarSlot)slot;
            if (!AutoSwap.mc.player.isUsingItem()) {
                AutoSwap.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(hotbarSlot.getSlotId()));
                AutoSwap.mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                AutoSwap.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(AutoSwap.mc.player.getInventory().selectedSlot));
                canSwap = false;
            }
        }
        if (canSwap) {
            if (slot1 instanceof HotbarSlot) {
                HotbarSlot hotbarSlot = (HotbarSlot)slot1;
                if (!AutoSwap.mc.player.isUsingItem()) {
                    AutoSwap.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(hotbarSlot.getSlotId()));
                    AutoSwap.mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                    AutoSwap.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(AutoSwap.mc.player.getInventory().selectedSlot));
                }
            }
            slot.swapTo(slot1);
        }
        this.timer.reset();
        Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.SUCCESS, this.getName(), AutoSwap.mc.player.getOffHandStack().getName().getString().replace("[", "").replace("] ", "").replace("xxx ", "").replace(" xxx", "").replace("123 ", "").replace(" 123", ""));
    }

    private void syncWithGuiMove() {
        if (this.syncGuiMove.isEnabled()) {
            Rockstar.getInstance().getModuleManager().getModule(GuiMove.class).setStay(true);
        }
    }

    private static /* synthetic */ int lambda$swap$4(ItemSlot stack) {
        return ItemUtility.bestFactor(stack.itemStack()) - (stack.getIdForServer() == 45 ? 99 : 0);
    }

    private static /* synthetic */ boolean lambda$swap$3(ItemSlot slot, ItemSlot slotW) {
        return slot != slotW;
    }

    private static /* synthetic */ int lambda$swap$2(ItemSlot stack) {
        return ItemUtility.bestFactor(stack.itemStack()) - (stack.getIdForServer() == 45 ? 99 : 0);
    }
}
