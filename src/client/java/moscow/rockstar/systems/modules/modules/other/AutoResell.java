/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 */
package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(name="Auto Resell", category=ModuleCategory.OTHER, desc="modules.descriptions.auto_resell")
public class AutoResell
extends BaseModule {
    private final Timer openTimer = new Timer();
    private final Timer clickTimer = new Timer();
    private boolean isAutoProcess;
    private boolean auctionHandled;
    private boolean storageHandled;
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (AutoResell.mc.player == null || AutoResell.mc.interactionManager == null || !ServerUtility.isFT()) {
            return;
        }
        if (this.openTimer.finished(60000L)) {
            if (!this.isAuctionOrStorageOpen()) {
                AutoResell.mc.player.networkHandler.sendChatCommand("ah " + AutoResell.mc.player.getName().getString());
                this.isAutoProcess = true;
                this.clickTimer.reset();
            }
            this.openTimer.reset();
        }
        this.handleAuctionAndStorage();
    };

    private void handleAuctionAndStorage() {
        boolean isAuctionOpen = this.isTitleContains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d\u044b");
        boolean isStorageOpen = this.isTitleContains("\u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435");
        if (this.isAutoProcess && isAuctionOpen && !this.auctionHandled && this.clickTimer.finished(300L)) {
            AutoResell.mc.interactionManager.clickSlot(AutoResell.mc.player.currentScreenHandler.syncId, 46, 0, SlotActionType.PICKUP, (PlayerEntity)AutoResell.mc.player);
            this.auctionHandled = true;
            this.clickTimer.reset();
        }
        if (this.isAutoProcess && isStorageOpen && !this.storageHandled && this.clickTimer.finished(300L)) {
            AutoResell.mc.interactionManager.clickSlot(AutoResell.mc.player.currentScreenHandler.syncId, 52, 0, SlotActionType.PICKUP, (PlayerEntity)AutoResell.mc.player);
            AutoResell.mc.player.closeHandledScreen();
            this.storageHandled = true;
            this.isAutoProcess = false;
            this.clickTimer.reset();
        }
        if (!isAuctionOpen) {
            this.auctionHandled = false;
        }
        if (!isStorageOpen) {
            this.storageHandled = false;
        }
    }

    private boolean isAuctionOrStorageOpen() {
        return this.isTitleContains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d\u044b") || this.isTitleContains("\u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435");
    }

    private boolean isTitleContains(String string) {
        if (AutoResell.mc.currentScreen == null || AutoResell.mc.player == null) {
            return false;
        }
        String title = AutoResell.mc.currentScreen.getTitle().getString().toLowerCase();
        return AutoResell.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler && title.contains(string.toLowerCase());
    }
}

