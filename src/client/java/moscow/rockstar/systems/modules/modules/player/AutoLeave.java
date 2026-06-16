/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

@ModuleInfo(name="Auto Leave", category=ModuleCategory.PLAYER)
public class AutoLeave
extends BaseModule {
    private final ModeSetting leave = new ModeSetting(this, "modules.settings.auto_leave.leave");
    private final ModeSetting.Value distLeave = new ModeSetting.Value(this.leave, "modules.settings.auto_leave.leave.distance");
    private final ModeSetting.Value healthLeave = new ModeSetting.Value(this.leave, "modules.settings.auto_leave.leave.health");
    private final ModeSetting.Value banLeave = new ModeSetting.Value(this.leave, "modules.settings.auto_leave.leave.ban");
    private final SliderSetting dist = new SliderSetting((SettingsContainer)this, "modules.settings.auto_leave.distance", () -> this.healthLeave.isSelected() || this.banLeave.isSelected()).suffix(number -> " %s".formatted(Localizator.translate("block")) + TextUtility.makeCountTranslated(number)).step(1.0f).min(1.0f).max(150.0f).currentValue(30.0f);
    private final SliderSetting health = new SliderSetting((SettingsContainer)this, "modules.settings.auto_leave.health", () -> this.distLeave.isSelected() || this.banLeave.isSelected()).step(1.0f).min(1.0f).max(20.0f).currentValue(10.0f);
    private final SliderSetting delay = new SliderSetting((SettingsContainer)this, "modules.settings.auto_leave.delay", () -> !this.banLeave.isSelected() || this.distLeave.isSelected() || this.healthLeave.isSelected()).suffix(Localizator.translate("sec") + ".").step(1.0f).min(1.0f).max(60.0f).currentValue(40.0f);
    private final Timer timer = new Timer();
    private boolean waiting;
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.auto_leave.mode");
    private final ModeSetting.Value hub = new ModeSetting.Value(this.mode, "modules.settings.auto_leave.mode.hub");
    private final ModeSetting.Value serverLeave = new ModeSetting.Value(this.mode, "modules.settings.auto_leave.mode.server");
    private final ModeSetting.Value spawn = new ModeSetting.Value(this.mode, "modules.settings.auto_leave.mode.spawn");
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
        if (this.distLeave.isSelected()) {
            for (PlayerEntity e : AutoLeave.mc.world.getPlayers()) {
                if (e == null || e == AutoLeave.mc.player || e.distanceTo((Entity)e) > this.dist.getCurrentValue() || AutoLeave.mc.player == null || ServerUtility.hasCT || Rockstar.getInstance().getFriendManager().isFriend(e.getName().getString())) continue;
                if (this.hub.isSelected()) {
                    AutoLeave.mc.player.networkHandler.sendChatCommand("hub");
                } else if (this.serverLeave.isSelected()) {
                    AutoLeave.mc.player.networkHandler.getConnection().disconnect(Text.of((String)Localizator.translate("modules.auto_leave.near_player")));
                } else if (this.spawn.isSelected()) {
                    AutoLeave.mc.player.networkHandler.sendChatCommand("spawn");
                }
                this.toggle();
                break;
            }
        }
        if (this.healthLeave.isSelected() && AutoLeave.mc.player != null && AutoLeave.mc.player.getHealth() + AutoLeave.mc.player.getAbsorptionAmount() <= this.health.getCurrentValue()) {
            if (this.hub.isSelected()) {
                AutoLeave.mc.player.networkHandler.sendChatCommand("hub");
            } else if (this.serverLeave.isSelected()) {
                AutoLeave.mc.player.networkHandler.getConnection().disconnect(Text.of((String)Localizator.translate("modules.auto_leave.low_health")));
            } else if (this.spawn.isSelected()) {
                AutoLeave.mc.player.networkHandler.sendChatCommand("spawn");
            }
            this.toggle();
        }
        if (!this.waiting) {
            return;
        }
        if (this.timer.finished((long)this.delay.getCurrentValue() * 1000L)) {
            AutoLeave.mc.player.networkHandler.sendChatCommand("an" + ServerUtility.ftAn);
            this.waiting = false;
        }
    };
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
        GameMessageS2CPacket packet;
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof GameMessageS2CPacket && (packet = (GameMessageS2CPacket)patt0$temp).content().getString().contains(Localizator.translate("modules.auto_leave.banned_word")) && this.banLeave.isSelected()) {
            AutoLeave.mc.player.networkHandler.sendChatCommand("hub");
            this.timer.reset();
            this.waiting = true;
        }
    };
}

