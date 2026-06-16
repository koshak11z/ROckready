package im.zov4ik.common.discord;
import antidaunleak.api.UserProfile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;
import im.zov4ik.common.discord.utils.*;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.utils.client.discord.Buffer;
import im.zov4ik.zov4ik;
import java.io.IOException;

@Setter
@Getter
public class DiscordManager implements QuickImports {
    private final DiscordDaemonThread discordDaemonThread = new DiscordDaemonThread();
    private boolean running = true;
    private DiscordInfo info = new DiscordInfo("Unknown", "", "");
    private Identifier avatarId;

    public void init() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            return;
        }

        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                .ready((user) -> {
                    zov4ik.getInstance().getDiscordManager().setInfo(
                            new DiscordInfo(user.username,
                                    "https://cdn.discordapp.com/avatars/" + user.userId + "/" + user.avatar + ".png",
                                    user.userId));
                    Discordzov4ikPresence zov4ikPresence = new Discordzov4ikPresence.Builder()
                            .setStartTimestamp(System.currentTimeMillis() / 1000)
                            .setDetails("User: " + UserProfile.getInstance().profile("username"))
                            .setState("Uid: " + UserProfile.getInstance().profile("uid"))
                            .setLargeImage("https://i.postimg.cc/nznMWbhM/0001-0250.gif", "https://zov4ikclient.fun/")
                            .setSmallImage(zov4ik.getInstance().getDiscordManager().getInfo().avatarUrl, "https://zov4ikclient.fun/")
                            .setButtons(RPCButton.create("Телеграм", "https://t.me/zov4ikclientnew"),
                                    RPCButton.create("Дискорд", "https://discord.gg/zYctK4mjZZ"))
                            .build();
                    DiscordRPC.INSTANCE.Discord_UpdatePresence(zov4ikPresence);
                }).build();
        DiscordRPC.INSTANCE.Discord_Initialize("1419653405265105021", handlers, true, "");
        discordDaemonThread.start();
    }

    public void stopRPC() {
        DiscordRPC.INSTANCE.Discord_Shutdown();
        this.running = false;
    }

    public void load() throws IOException {
        if (avatarId == null && !info.avatarUrl.isEmpty()) {
            avatarId = Buffer.registerDynamicTexture("avatar-", Buffer.getHeadFromURL(info.avatarUrl));
        }
    }

    public Identifier getAvatarId() {
        return avatarId;
    }

    private class DiscordDaemonThread extends Thread {
        @Override
        public void run() {
            this.setName("Discord-RPC");
            try {
                while (zov4ik.getInstance().getDiscordManager().isRunning()) {
                    DiscordRPC.INSTANCE.Discord_RunCallbacks();
                    load();
                    Thread.sleep(15000);
                }
            } catch (Exception exception) {
                stopRPC();
            }
            super.run();
        }
    }

    public record DiscordInfo(String userName, String avatarUrl, String userId) {}
}