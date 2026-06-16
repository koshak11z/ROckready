/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
 */
package moscow.rockstar.systems.modules.modules.other;

import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

@ModuleInfo(name="Sounds", category=ModuleCategory.OTHER, enabledByDefault=true, desc="\u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u0442 \u0437\u0432\u0443\u043a\u0438 \u043a\u043b\u0438\u0435\u043d\u0442\u0430")
public class Sounds
extends BaseModule {
    private final SliderSetting volume = new SliderSetting(this, "\u0413\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c \u0437\u0432\u0443\u043a\u0430").step(0.1f).min(0.1f).max(1.0f).currentValue(1.0f);
    private final EventListener<ReceivePacketEvent> receivePacket = event -> {
        GameMessageS2CPacket packet;
        String msg;
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof GameMessageS2CPacket && ((msg = (packet = (GameMessageS2CPacket)patt0$temp).content().getString()).contains("\u0412\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043a\u0443\u043f\u0438\u043b\u0438") || msg.contains("\u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043e \u0438\u0433\u0440\u043e\u043a\u0443"))) {
            ClientSounds.APPLEPAY.play(this.volume.getCurrentValue(), 1.0f);
        }
    };

    @Generated
    public SliderSetting getVolume() {
        return this.volume;
    }

    @Generated
    public EventListener<ReceivePacketEvent> getReceivePacket() {
        return this.receivePacket;
    }
}

