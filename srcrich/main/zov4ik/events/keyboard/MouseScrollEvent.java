package im.zov4ik.events.keyboard;

public class MouseScrollEvent extends HotBarScrollEvent {
    public MouseScrollEvent(double horizontal, double vertical) {
        super(horizontal, vertical);
    }

    public double getHorizontal() {
        return getHorizontalValue();
    }

    public double getVertical() {
        return getVerticalValue();
    }

    private double getHorizontalValue() {
        return super.getHorizontal();
    }

    private double getVerticalValue() {
        return super.getVertical();
    }
}
