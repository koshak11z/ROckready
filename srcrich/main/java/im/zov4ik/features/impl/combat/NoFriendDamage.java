package im.zov4ik.features.impl.combat;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import im.zov4ik.common.repository.friend.FriendUtils;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.events.player.AttackEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoFriendDamage extends Module {
    public NoFriendDamage() {
        super("NoFriendDamage", "No Friend Damage", ModuleCategory.COMBAT);
    }

    @EventHandler
    public void onAttack(AttackEvent e) {
        e.setCancelled(FriendUtils.isFriend(e.getEntity()));
    }
}

