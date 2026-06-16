package im.zov4ik.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import im.zov4ik.utils.client.managers.event.events.Event;

@Getter
@AllArgsConstructor
public class RotationUpdateEvent implements Event {
    byte type;
}
