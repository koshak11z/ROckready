/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
 */
package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.game.server.ServerUtility;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

@ModuleInfo(name="Auto Accept", category=ModuleCategory.OTHER, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043f\u0440\u0438\u043d\u0438\u043c\u0430\u0435\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044e")
public class AutoAccept
extends BaseModule {
    private final ModeSetting acceptMode = new ModeSetting(this, "\u041f\u0440\u0438\u043d\u0438\u043c\u0430\u0442\u044c");
    private final ModeSetting.Value acceptAll = new ModeSetting.Value(this.acceptMode, "\u0412\u0441\u0435\u0445");
    private final ModeSetting.Value friendsOnly = new ModeSetting.Value(this.acceptMode, "\u0422\u043e\u043b\u044c\u043a\u043e \u0434\u0440\u0443\u0437\u0435\u0439");
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = (GameMessageS2CPacket)patt0$temp;
            if (AutoAccept.mc.player != null && packet.content().getString().contains("\u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f") && !ServerUtility.hasCT && this.canAccept(packet.content().getString())) {
                AutoAccept.mc.player.networkHandler.sendChatCommand("tpaccept");
            }
        }
    };

    private boolean canAccept(String message) {
        if (this.acceptMode.is(this.acceptAll)) {
            return true;
        }
        if (this.acceptMode.is(this.friendsOnly)) {
            if (Rockstar.getInstance().getFriendManager().isFriend(message.split(" ")[1]) || Rockstar.getInstance().getFriendManager().isFriend(message.replace("\u0a77 \u043f\u0440\u043e\u0441\u0438\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u043a \u0412\u0430\u043c.\u0a77\u00a7l [\u0a72\u00a7l\u2714\u0a77\u00a7l]\u0a77\u00a7l [\u0a7c\u00a7l\u2717\u0a77\u00a7l]", "").replace("\u0a76", "")) || Rockstar.getInstance().getFriendManager().isFriend(message.replace("\u279d \u041d\u0438\u043a: ", ""))) {
                return true;
            }
            if (message.contains("\u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f")) {
                String[] parts = message.split(" ");
                return parts.length >= 2 && Rockstar.getInstance().getFriendManager().isFriend(parts[2]);
            }
        }
        return false;
    }
}

