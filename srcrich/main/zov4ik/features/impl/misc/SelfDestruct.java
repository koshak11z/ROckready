package im.zov4ik.features.impl.misc;

import antidaunleak.api.UserProfile;
import im.zov4ik.zov4ik;
import im.zov4ik.common.discord.DiscordManager;
import im.zov4ik.events.chat.ChatEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.client.managers.file.FileRepository;
import im.zov4ik.utils.client.managers.file.exception.FileLoadException;
import im.zov4ik.utils.client.managers.file.impl.PrefixFile;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.utils.math.time.StopWatch;
import lombok.experimental.NonFinal;
import org.apache.logging.log4j.core.appender.rolling.action.IfAll;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MathUtil;

public class SelfDestruct extends Module {
    public static boolean unhooked;

    public SelfDestruct() {
        super("SelfDestruct", "Self Destruct", ModuleCategory.MISC);
    }
    @NonFinal
    StopWatch timer = new StopWatch();

    @Override
    public void activate() {
        unhooked = true;

        zov4ik.getInstance().getDiscordManager().stopRPC();

        for (Module module : zov4ik.getInstance().getModuleProvider().getModules()) {
            if (module != this && module.isState()) {
                module.setState(false);
            }
        }

        ChatMessage.brandmessage("Для возвращения чита впишите в чат ваш username в чите");
        ChatMessage.brandmessage("Сообщение удалится через пол секунды");
        if (timer.every(500)) {
            mc.inGameHud.getChatHud().clear(true);
        }

        for (Module module : zov4ik.getInstance().getModuleProvider().getModules()) {
            module.setKey(GLFW.GLFW_KEY_UNKNOWN);
        }

        zov4ik.getInstance().getCommandDispatcher().prefix = "" + Calculate.getRandom(0, 9999999);

        super.activate();
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        String msg = event.getMessage().trim();
        if (msg.equalsIgnoreCase(UserProfile.getInstance().profile("username"))) {
            unhooked = false;
            zov4ik.getInstance().getDiscordManager().setRunning(true);
            state = false;
            zov4ik.getInstance().getCommandDispatcher().prefix = ".";
            ChatMessage.brandmessage("Unhook reset to FALSE");
            event.setCancelled(true);
        }
    }

    @Override
    public void deactivate() {
        unhooked = false;
        super.deactivate();
    }
}
