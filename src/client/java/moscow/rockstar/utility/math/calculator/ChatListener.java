/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket
 */
package moscow.rockstar.utility.math.calculator;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;

public class ChatListener
implements IMinecraft {
    private final EventListener<SendPacketEvent> onSendPacket = event -> {
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof ChatCommandSignedC2SPacket) {
            ChatCommandSignedC2SPacket packet = (ChatCommandSignedC2SPacket)patt0$temp;
            if (ChatListener.mc.player == null) {
                return;
            }
            String message = packet.command();
            if (message.startsWith("ah me")) {
                ChatListener.mc.player.networkHandler.sendChatMessage("/ah " + ChatListener.mc.player.getName().getString());
                event.cancel();
            }
            if (message.startsWith("ah sell ")) {
                String expression = message.replaceFirst("ah sell ", "");
                String result = MathUtility.calculate(expression);
                ChatListener.mc.player.networkHandler.sendChatMessage("/ah sell " + Math.round(Float.parseFloat(result)));
                event.cancel();
            }
        }
    };

    public ChatListener() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }
}

