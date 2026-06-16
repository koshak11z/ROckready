/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  pw.lucent.bridge.client.core.Network
 *  pw.lucent.bridge.shared.packet.api.abstracts.ClientPacket
 *  pw.lucent.bridge.shared.packet.impl.client.community.C2SPacketIRCMessage
 */
package moscow.rockstar.systems.commands.commands;

import java.util.List;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import pw.lucent.bridge.client.core.Network;
import pw.lucent.bridge.shared.packet.api.abstracts.ClientPacket;
import pw.lucent.bridge.shared.packet.impl.client.community.C2SPacketIRCMessage;

public class IRCCommand {
    public Command command() {
        return CommandBuilder.begin("irc", b -> b.desc("\u041a\u043e\u043c\u043c\u0443\u043d\u0438\u043a\u0430\u0446\u0438\u0438 \u043c\u0435\u0436\u0434\u0443 \u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044f\u043c\u0438").handler(this::handle)).build();
    }

    private void handle(CommandContext ctx) {
        List msgParts = (List)ctx.arguments().get(0);
        String msg = String.join((CharSequence)" ", msgParts);
        Network.getInstance().getClient().sendMessage((ClientPacket)new C2SPacketIRCMessage(msg.replace("&", "\u00a7").replace("\\n", "\n")));
    }
}

