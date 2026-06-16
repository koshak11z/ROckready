/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.lwjgl.glfw.GLFW
 */
package moscow.rockstar.utility.game.cursor;

import lombok.Generated;
import org.lwjgl.glfw.GLFW;

public enum CursorType {
    DEFAULT(GLFW.glfwCreateStandardCursor((int)221185)),
    HAND(GLFW.glfwCreateStandardCursor((int)221188)),
    ARROW_HORIZONTAL(GLFW.glfwCreateStandardCursor((int)221189)),
    ARROW_VERTICAL(GLFW.glfwCreateStandardCursor((int)221190)),
    TEXT(GLFW.glfwCreateStandardCursor((int)221186)),
    CROSSHAIR(GLFW.glfwCreateStandardCursor((int)221187)),
    BLOCK(GLFW.glfwCreateStandardCursor((int)221194)),
    RESIZE_ALL(GLFW.glfwCreateStandardCursor((int)221193));

    private final long code;

    @Generated
    public long getCode() {
        return this.code;
    }

    @Generated
    private CursorType(long code) {
        this.code = code;
    }
}

