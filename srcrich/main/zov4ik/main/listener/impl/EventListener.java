package im.zov4ik.main.listener.impl;

import im.zov4ik.utils.interactions.inv.InventoryFlowManager;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.utils.client.packet.network.Network;
import im.zov4ik.zov4ik;
import im.zov4ik.main.listener.Listener;
import im.zov4ik.events.item.UsingItemEvent;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.player.TickEvent;

public class EventListener implements Listener {
    public static boolean serverSprint;
    public static int selectedSlot;

    @EventHandler
    public void onTick(TickEvent e) {
        Network.tick();
        zov4ik.getInstance().getAttackPerpetrator().tick();
        InventoryFlowManager.tick();
        zov4ik.getInstance().getDraggableRepository().draggable().forEach(AbstractDraggable::tick);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        switch (e.getPacket()) {
            case ClientCommandC2SPacket command -> serverSprint = switch (command.getMode()) {
                case ClientCommandC2SPacket.Mode.START_SPRINTING -> true;
                case ClientCommandC2SPacket.Mode.STOP_SPRINTING -> false;
                default -> serverSprint;
            };
            case UpdateSelectedSlotC2SPacket slot -> selectedSlot = slot.getSelectedSlot();
            default -> {}
        }
        Network.packet(e);
        zov4ik.getInstance().getAttackPerpetrator().onPacket(e);
        zov4ik.getInstance().getDraggableRepository().draggable().forEach(drag -> drag.packet(e));
    }

    @EventHandler
    public void onUsingItemEvent(UsingItemEvent e) {
        zov4ik.getInstance().getAttackPerpetrator().onUsingItem(e);
    }
}
