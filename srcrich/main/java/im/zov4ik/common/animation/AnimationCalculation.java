package im.zov4ik.common.animation;

public interface AnimationCalculation {
    default double calculation(double value) {
        return 0;
    }
}