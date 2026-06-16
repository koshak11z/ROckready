package pw.lucent.bridge.shared.packet.impl.client.community;

import pw.lucent.bridge.shared.packet.api.abstracts.ClientPacket;

public class C2SPacketIRCMessage extends ClientPacket {
    private final String message;

    public C2SPacketIRCMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
