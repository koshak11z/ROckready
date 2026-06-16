package im.zov4ik.utils.interactions.item;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.Hand;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.zov4ik;
import im.zov4ik.events.packet.PacketEvent;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemToolkit implements QuickImports {
    public final static ItemToolkit INSTANCE = new ItemToolkit();
    public boolean useItem, releaseItem = true;

    public ItemToolkit() {
        zov4ik.getInstance().getEventManager().register(this);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        switch (e.getPacket()) {
            case PlayerActionC2SPacket player when player.getAction().equals(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) -> releaseItem = true;
            case ClientStatusC2SPacket status when status.getMode().equals(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN) -> releaseItem = true;
            case PlayerRespawnS2CPacket respawn -> releaseItem = true;
            case GameJoinS2CPacket join -> releaseItem = true;
            default -> {}
        }
    }

    public void useHand(Hand hand) {
        if (releaseItem) {
            mc.interactionManager.interactItem(mc.player, hand);
            releaseItem = false;
        }
        useItem = true;
    }
}
