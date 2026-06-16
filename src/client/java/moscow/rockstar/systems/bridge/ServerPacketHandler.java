/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  pw.lucent.bridge.client.handler.PacketHandler
 *  pw.lucent.bridge.shared.packet.impl.server.community.S2CPacketIRCMessage
 */
package moscow.rockstar.systems.bridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pw.lucent.bridge.client.handler.PacketHandler;
import pw.lucent.bridge.shared.packet.impl.server.community.S2CPacketIRCMessage;

public class ServerPacketHandler
extends PacketHandler {
    public void handle(S2CPacketIRCMessage packet) {
        MinecraftClient.getInstance().player.sendMessage(Text.of((String)(String.valueOf(Formatting.DARK_RED) + "[IRC] " + String.valueOf(Formatting.WHITE) + packet.getUserInfo().getUsername() + ": " + String.valueOf(Formatting.GRAY) + packet.getMessage())), false);
    }
}

