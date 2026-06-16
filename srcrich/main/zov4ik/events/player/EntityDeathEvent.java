package im.zov4ik.events.player;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import im.zov4ik.utils.client.managers.event.events.Event;

@Getter
public class EntityDeathEvent implements Event {
    private final Entity entity;
    private final DamageSource source;

    public EntityDeathEvent(Entity entity, DamageSource source) {
        this.entity = entity;
        this.source = source;
    }
}