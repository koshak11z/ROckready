/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
 */
package moscow.rockstar.systems.modules.modules.other;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.StringSetting;
import moscow.rockstar.utility.game.TextUtility;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

@ModuleInfo(name="Auto Auth", category=ModuleCategory.OTHER, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0435\u0442 \u0430\u043a\u043a\u0430\u0443\u043d\u0442 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435")
public class AutoAuth
extends BaseModule {
    private final BooleanSetting random = new BooleanSetting(this, "\u0420\u0430\u043d\u0434\u043e\u043c");
    private final StringSetting password = new StringSetting((SettingsContainer)this, "\u041f\u0430\u0440\u043e\u043b\u044c", this.random::isEnabled).text("123123");
    private final Map<String, String> nickAndPassword = new HashMap<String, String>();
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = (GameMessageS2CPacket)patt0$temp;
            if (AutoAuth.mc.player != null) {
                String message = packet.content().getString().toLowerCase();
                String randomPass = TextUtility.getRandomNick();
                String password = this.random.isEnabled() ? randomPass : this.password.getText();
                this.nickAndPassword.put(AutoAuth.mc.player.getDisplayName().getString(), " " + randomPass);
                if (message.contains("\u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0439\u0442\u0435\u0441\u044c") || message.contains("/reg")) {
                    AutoAuth.mc.player.networkHandler.sendChatCommand(String.format("reg %s %s", password, password));
                } else if (message.contains("\u0430\u0432\u0442\u043e\u0440\u0438\u0437\u0443\u0439\u0442\u0435\u0441\u044c") || message.contains("/login") || message.contains("/l") && message.matches("/l(\\s|$)")) {
                    AutoAuth.mc.player.networkHandler.sendChatCommand(String.format("l %s", password));
                }
            }
        }
    };

    public Map<String, String> listPassword() {
        return Collections.unmodifiableMap(this.nickAndPassword);
    }

    public void put(String key, String value) {
        this.nickAndPassword.put(key, value);
    }
}

