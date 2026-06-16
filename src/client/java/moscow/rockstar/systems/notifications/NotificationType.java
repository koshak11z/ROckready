/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.notifications;

import lombok.Generated;
import moscow.rockstar.utility.colors.ColorRGBA;

public enum NotificationType {
    SUCCESS("success", ColorRGBA.GREEN),
    ERROR("error", ColorRGBA.RED),
    INFO("info", new ColorRGBA(234.0f, 179.0f, 8.0f));

    private final String name;
    private final ColorRGBA color;

    public static NotificationType get(String name) {
        for (NotificationType type : NotificationType.values()) {
            if (!type.getName().equalsIgnoreCase(name)) continue;
            return type;
        }
        return INFO;
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public ColorRGBA getColor() {
        return this.color;
    }

    @Generated
    private NotificationType(String name, ColorRGBA color) {
        this.name = name;
        this.color = color;
    }
}

