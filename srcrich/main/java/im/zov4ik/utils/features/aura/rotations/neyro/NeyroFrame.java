package im.zov4ik.utils.features.aura.rotations.neyro;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NeyroFrame {
    float deltaYaw;
    float deltaPitch;
    float yawVelocity;
    float pitchVelocity;
    long deltaTimeMs;
    boolean attacked;
    boolean sprinting;
    boolean sneaking;
    float forwardInput;
    float sidewaysInput;
    float attackCooldown;
}
