package pw.lucent.bridge.shared.packet.impl.server.community;

public class S2CPacketIRCMessage {
    private final UserInfo userInfo = new UserInfo();
    private final String message;

    public S2CPacketIRCMessage() {
        this("");
    }

    public S2CPacketIRCMessage(String message) {
        this.message = message;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public String getMessage() {
        return message;
    }

    public static class UserInfo {
        public String getUsername() {
            return "unknown";
        }
    }
}
