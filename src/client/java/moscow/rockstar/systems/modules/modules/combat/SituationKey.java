package moscow.rockstar.systems.modules.modules.combat;

import lombok.Value;

@Value
public class SituationKey {
    String targetType;      // "player", "mob", "animal"
    String distanceBucket;  // "close", "medium", "far"
    String movementState;   // "stationary", "walking", "sprinting", "jumping"
    String critState;       // "crit", "nocrit"
    String healthState;     // "high", "medium", "low"

    @Override
    public String toString() {
        return targetType + "_" + distanceBucket + "_" + movementState + "_" + critState + "_" + healthState;
    }
}