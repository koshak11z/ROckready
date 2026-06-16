package pw.lucent.bridge.client.core;

import pw.lucent.bridge.shared.packet.api.abstracts.ClientPacket;

public final class Network {
    private static final Network INSTANCE = new Network();
    private final Client client = new Client();

    public static Network getInstance() {
        return INSTANCE;
    }

    public Client getClient() {
        return client;
    }

    public void onTick() {
    }

    public static final class Client {
        public void sendMessage(ClientPacket packet) {
        }
    }
}
