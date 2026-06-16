package im.zov4ik.events.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import im.zov4ik.utils.client.managers.event.events.callables.EventCancellable;

@Getter
@Setter
@AllArgsConstructor
public class UsingItemEvent extends EventCancellable {
    byte type;
}
