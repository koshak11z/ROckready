package im.zov4ik.common.discord.callbacks;

import com.sun.jna.Callback;
import im.zov4ik.common.discord.utils.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(DiscordUser var1);
}