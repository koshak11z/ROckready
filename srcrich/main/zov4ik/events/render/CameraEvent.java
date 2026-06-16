package im.zov4ik.events.render;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import im.zov4ik.utils.client.managers.event.events.callables.EventCancellable;
import im.zov4ik.utils.features.aura.warp.Turns;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CameraEvent extends EventCancellable {
    boolean cameraClip;
    float distance;
    Turns angle;
}
