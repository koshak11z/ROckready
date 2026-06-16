/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jagrosh.discordipc.entities.RichPresence$Builder
 */
package moscow.rockstar.systems.discord;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.RichPresence;
import com.jagrosh.discordipc.entities.DiscordBuild;
import java.time.OffsetDateTime;
import moscow.rockstar.utility.interfaces.IMinecraft;

public class DiscordManager
implements IMinecraft {
    private final IPCClient client = new IPCClient(1368178867952422993L);

    private RichPresence.Builder getBuilder() {
        return new RichPresence.Builder().setDetails(String.format("Branch: %s", "Beta")).setState("UID: 1").setStartTimestamp(OffsetDateTime.now()).setLargeImage("animlogo", "t.me/rockclient").setButton1Text("\u041d\u0430 \u043f\u043e\u043a\u0443\u0448\u0430\u0442\u044c \u043f\u0436").setButton1Url("https://www.tbank.ru/cf/28gZw5lU2t4").setButton2Text("Discord").setButton2Url("https://dsc.gg/rockclient");
    }

    public void connect() {
        try {
            this.client.setListener(new IPCListener(){

                public void onReady(IPCClient client) {
                    client.sendRichPresence(DiscordManager.this.getBuilder().build());
                }
            });
            this.client.connect(new DiscordBuild[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}
