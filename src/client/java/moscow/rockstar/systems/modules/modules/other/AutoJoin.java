/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 */
package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

@ModuleInfo(name="Auto Join", category=ModuleCategory.OTHER, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0437\u0430\u0445\u043e\u0434\u0438\u0442 \u043d\u0430 \u0440\u0435\u0436\u0438\u043c")
public class AutoJoin
extends BaseModule {
    private final String griefString = "306";
    private final ModeSetting mode = new ModeSetting(this, "\u0420\u0435\u0436\u0438\u043c");
    private final ModeSetting.Value duels = new ModeSetting.Value(this.mode, "\u0414\u0443\u044d\u043b\u0438 SpookyTime").select();
    private final ModeSetting.Value grief = new ModeSetting.Value(this.mode, "\u0413\u0440\u0438\u0444 RW/FT/Spooky");
    private final Timer timer = new Timer();
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (ServerUtility.isST() && this.duels.isSelected()) {
            SlotGroup<HotbarSlot> search = SlotGroups.hotbar();
            HotbarSlot compass = search.findItem(Items.COMPASS);
            HotbarSlot sword = search.findItem(Items.DIAMOND_SWORD);
            if (compass != null && this.timer.finished(300L)) {
                AutoJoin.mc.player.getInventory().selectedSlot = compass.getSlotId();
                AutoJoin.mc.interactionManager.sendSequencedPacket(AutoJoin.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, AutoJoin.mc.player.getYaw(), AutoJoin.mc.player.getPitch()));
                if (AutoJoin.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler && AutoJoin.mc.currentScreen.getTitle().getString().contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
                    AutoJoin.mc.interactionManager.clickSlot(AutoJoin.mc.player.currentScreenHandler.syncId, 14, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)AutoJoin.mc.player);
                }
                this.timer.reset();
            }
            if (sword != null) {
                Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.SUCCESS, "\u0423\u0441\u043f\u0435\u0448\u043d\u044b\u0439 \u0432\u0445\u043e\u0434", "\u0412\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0432\u043e\u0448\u043b\u0438 \u043d\u0430 \u0434\u0443\u044d\u043b\u0438");
                this.toggle();
            }
        }
        if ((ServerUtility.isFT() || ServerUtility.isRW() || ServerUtility.isST()) && this.grief.isSelected()) {
            AutoJoin.mc.player.networkHandler.sendChatCommand("an306");
        }
    };
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = (GameMessageS2CPacket)patt0$temp;
            if ((ServerUtility.isFT() || ServerUtility.isRW() || ServerUtility.isST()) && this.grief.isSelected()) {
                String message = packet.content().getString().toLowerCase();
                if (ServerUtility.isFT() ? message.contains("\u0432\u044b \u0443\u0436\u0435 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u044b \u043a \u044d\u0442\u043e\u043c\u0443 \u0441\u0435\u0440\u0432\u0435\u0440\u0432\u0443!") : message.contains("\u0432\u044b \u0443\u0436\u0435 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u044b \u043d\u0430 \u044d\u0442\u043e\u0442 \u0441\u0435\u0440\u0432\u0435\u0440!")) {
                    this.toggle();
                }
            }
        }
    };
}

