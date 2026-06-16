/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.DrawContext
 */
package moscow.rockstar.framework.base;

import lombok.Generated;
import moscow.rockstar.framework.base.CustomDrawContext;
import net.minecraft.client.gui.DrawContext;

public class UIContext
extends CustomDrawContext {
    private final int mouseX;
    private final int mouseY;
    private final float delta;

    protected UIContext(DrawContext originalContext, int mouseX, int mouseY, float delta) {
        super(originalContext);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.delta = delta;
    }

    public static UIContext of(DrawContext originalContext, int mouseX, int mouseY, float delta) {
        return new UIContext(originalContext, mouseX, mouseY, delta);
    }

    @Generated
    public int getMouseX() {
        return this.mouseX;
    }

    @Generated
    public int getMouseY() {
        return this.mouseY;
    }

    @Generated
    public float getDelta() {
        return this.delta;
    }
}

