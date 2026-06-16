/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 */
package moscow.rockstar.systems.modules.modules.other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(name="Auto Duels", category=ModuleCategory.OTHER, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043a\u0438\u0434\u0430\u0435\u0442 \u0434\u0443\u044d\u043b\u0438")
public class AutoDuels
extends BaseModule {
    private final ModeSetting mode = new ModeSetting(this, "\u041f\u0440\u0435\u0434\u043f\u043e\u0447\u0438\u0442\u0430\u0442\u044c");
    private final ModeSetting.Value soft = new ModeSetting.Value(this.mode, "\u0421\u043e\u0444\u0442\u0435\u0440\u043e\u0432");
    private final ModeSetting.Value anSoft = new ModeSetting.Value(this.mode, "\u0410\u043d\u0441\u043e\u0444\u0442\u0435\u0440\u043e\u0432");
    private final ModeSetting.Value random = new ModeSetting.Value(this.mode, "\u0420\u0430\u043d\u0434\u043e\u043c");
    private final ModeSetting kit = new ModeSetting(this, "\u041a\u0438\u0442");
    private final ModeSetting.Value shield = new ModeSetting.Value(this.kit, "\u0429\u0438\u0442");
    private final ModeSetting.Value shipi = new ModeSetting.Value(this.kit, "\u0428\u0438\u043f\u044b 3");
    private final ModeSetting.Value bow = new ModeSetting.Value(this.kit, "\u041b\u0443\u043a");
    private final ModeSetting.Value totem = new ModeSetting.Value(this.kit, "\u0422\u043e\u0442\u0435\u043c");
    private final ModeSetting.Value noDebaff = new ModeSetting.Value(this.kit, "\u041d\u043e\u0443\u0414\u0435\u0431\u0430\u0444");
    private final ModeSetting.Value balls = new ModeSetting.Value(this.kit, "\u0428\u0430\u0440\u044b");
    private final ModeSetting.Value classik = new ModeSetting.Value(this.kit, "\u041a\u043b\u0430\u0441\u0441\u0438\u043a");
    private final ModeSetting.Value cheats = new ModeSetting.Value(this.kit, "\u0427\u0438\u0442\u0435\u0440\u0441\u043a\u0438\u0439 \u0440\u0430\u0439");
    private final ModeSetting.Value nezer = new ModeSetting.Value(this.kit, "\u041d\u0435\u0437\u0435\u0440");
    private final Timer count = new Timer();
    private final List<String> sent = new ArrayList<String>();
    private final EventListener<ReceivePacketEvent> onReceive = event -> {
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = (GameMessageS2CPacket)patt0$temp;
            String msg = packet.content().getString();
            if (msg.contains("\u043f\u0440\u0438\u043d\u044f\u043b") && !msg.contains("\u043d\u0435 \u043f\u0440\u0438\u043d\u044f\u043b") || msg.contains("\u043a\u043e\u043c\u0430\u043d\u0434\u044b")) {
                this.sent.clear();
                this.toggle();
            }
            if (msg.contains("\u0411\u0430\u043b\u0430\u043d\u0441") || msg.contains("\u043e\u0442\u043a\u043b\u044e\u0447\u0438\u043b \u0437\u0430\u043f\u0440\u043e\u0441\u044b")) {
                event.cancel();
            }
        }
    };
    private final EventListener<WorldChangeEvent> world = e -> this.disable();

    @Override
    public void tick() {
        ArrayList<String> playerNames = new ArrayList<String>();
        for (PlayerListEntry entry : AutoDuels.mc.player.networkHandler.getPlayerList()) {
            playerNames.add(entry.getProfile().getName());
        }
        if (this.random.isSelected()) {
            Collections.shuffle(playerNames);
        } else if (this.soft.isSelected()) {
            Collections.reverse(playerNames);
        }
        for (String name : playerNames) {
            if (!this.count.finished(750L) || this.sent.contains(name) || name.equals(AutoDuels.mc.player.getNameForScoreboard())) continue;
            AutoDuels.mc.player.networkHandler.sendChatCommand("duel " + name);
            this.sent.add(name);
            this.count.reset();
        }
        if (AutoDuels.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            String title = AutoDuels.mc.currentScreen.getTitle().getString();
            if (title.contains("\u0412\u044b\u0431\u043e\u0440 \u043d\u0430\u0431\u043e\u0440\u0430")) {
                AutoDuels.mc.interactionManager.clickSlot(AutoDuels.mc.player.currentScreenHandler.syncId, this.kit.getValues().indexOf(this.kit.getRandomEnabledElement()), 0, SlotActionType.PICKUP, (PlayerEntity)AutoDuels.mc.player);
                AutoDuels.mc.player.currentScreenHandler.onSlotClick(this.kit.getValues().indexOf(this.kit.getRandomEnabledElement()), 0, SlotActionType.PICKUP, (PlayerEntity)AutoDuels.mc.player);
            } else if (title.contains("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430 \u043f\u043e\u0435\u0434\u0438\u043d\u043a\u0430")) {
                AutoDuels.mc.interactionManager.clickSlot(AutoDuels.mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.PICKUP, (PlayerEntity)AutoDuels.mc.player);
                AutoDuels.mc.player.currentScreenHandler.onSlotClick(0, 0, SlotActionType.PICKUP, (PlayerEntity)AutoDuels.mc.player);
            }
        }
        super.tick();
    }

    @Override
    public void onEnable() {
        this.count.reset();
        super.onEnable();
    }
}

