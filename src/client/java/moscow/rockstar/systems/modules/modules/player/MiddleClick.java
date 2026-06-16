/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 */
package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventIntegration;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.friends.FriendManager;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

@ModuleInfo(name="Middle Click", category=ModuleCategory.PLAYER, desc="\u0412\u044b\u043f\u043e\u043b\u043d\u044f\u0435\u0442 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435 \u043f\u0440\u0438 \u043d\u0430\u0436\u0430\u0442\u0438\u0438 \u043d\u0430 \u043a\u043e\u043b\u0435\u0441\u0438\u043a\u043e \u043c\u044b\u0448\u0438")
public class MiddleClick
extends BaseModule {
    private final SelectSetting actions = new SelectSetting(this, "\u0414\u0435\u0439\u0441\u0442\u0432\u0438\u0435").min(1);
    private final SelectSetting.Value clickPearl = new SelectSetting.Value(this.actions, "\u0411\u0440\u043e\u0441\u0430\u0442\u044c \u0436\u0435\u043c\u0447\u0443\u0433").select();
    private final SelectSetting.Value clickFriend = new SelectSetting.Value(this.actions, "\u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0442\u044c \u0434\u0440\u0443\u0437\u0435\u0439");
    private final BindSetting clickFriendKey = new BindSetting(this, "\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0434\u0440\u0443\u0437\u0435\u0439", () -> !this.clickFriend.isSelected());
    private final BindSetting clickPearlKey = new BindSetting(this, "\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u043f\u0435\u0440\u043b\u0430", () -> !this.clickPearl.isSelected());
    private final BooleanSetting syncGuiMove = new BooleanSetting(this, "Синхронизировать с GuiMove");
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> this.handleKey(event.getKey(), event.getAction());
    private final EventListener<MouseEvent> onMouseEvent = event -> this.handleKey(event.getButton(), event.getAction());

    private void handleKey(int key, int action) {
        if (MiddleClick.mc.currentScreen == null && action == 1) {
            if (this.clickFriend.isSelected() && this.clickFriendKey.isKey(key) && MiddleClick.mc.targetedEntity instanceof PlayerEntity) {
                String nick = MiddleClick.mc.targetedEntity.getName().getString();
                FriendManager friend = Rockstar.getInstance().getFriendManager();
                if (friend.isFriend(nick)) {
                    friend.remove(nick);
                } else {
                    friend.add(nick);
                }
            }
            if (this.clickPearl.isSelected() && this.clickPearlKey.isKey(key)) {
                this.syncWithGuiMove();
                EventIntegration.SWAP_INTEGRATION.useItem(Items.ENDER_PEARL);
            }
        }
    }

    private void syncWithGuiMove() {
        if (this.syncGuiMove.isEnabled()) {
            Rockstar.getInstance().getModuleManager().getModule(GuiMove.class).setStay(true);
        }
    }
}

