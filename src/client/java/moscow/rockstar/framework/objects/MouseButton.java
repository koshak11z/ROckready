/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.framework.objects;

import lombok.Generated;

public enum MouseButton {
    LEFT(0),
    RIGHT(1),
    MIDDLE(2),
    BUTTON_4(3),
    BUTTON_5(4),
    BUTTON_6(5),
    BUTTON_7(6);

    private final int buttonIndex;

    public static MouseButton fromButtonIndex(int index) {
        for (MouseButton button : MouseButton.values()) {
            if (button.getButtonIndex() != index) continue;
            return button;
        }
        return LEFT;
    }

    @Generated
    private MouseButton(int buttonIndex) {
        this.buttonIndex = buttonIndex;
    }

    @Generated
    public int getButtonIndex() {
        return this.buttonIndex;
    }
}

