package im.zov4ik.common.discord.callbacks;

import com.sun.jna.Callback;
import im.zov4ik.common.discord.utils.DiscordUser;

public interface JoinRequestCallback extends Callback {
    void apply(DiscordUser var1);
}