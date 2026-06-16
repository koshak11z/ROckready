package im.zov4ik.features.impl.misc;

import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.zov4ik;
import im.zov4ik.utils.client.chat.ChatMessage;

public class IRC extends Module {
    public IRC() {
        super("IRC", ModuleCategory.MISC);
    }

    @Override
    public void setState(boolean state) {
        super.setState(state);
        if (state) {
            activate();
        } else {
            deactivate();
        }
    }

    @Override
    public void activate() {
        zov4ik.getInstance().setShowIrcMessages(true);
        zov4ik.getInstance().getIrcManager().connect();
    }

    @Override
    public void deactivate() {
        zov4ik.getInstance().setShowIrcMessages(false);
        zov4ik.getInstance().getIrcManager().disconnect();
    }

    public void sendMessage(String message) {
        if (!isState()) {
            ChatMessage.ircmessageWithRed("Модуль IRC выключен");
            return;
        }
        if (zov4ik.getInstance().getIrcManager().getClient() != null && zov4ik.getInstance().getIrcManager().getClient().isOpen()) {
            zov4ik.getInstance().getIrcManager().getClient().sendMessage(message);
        }
    }
}