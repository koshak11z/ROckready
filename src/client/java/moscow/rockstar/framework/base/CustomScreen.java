/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.text.Text
 */
package moscow.rockstar.framework.base;

import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.objects.MouseButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class CustomScreen
extends Screen {
    protected CustomScreen() {
        super((Text)Text.empty());
    }

    public abstract void render(UIContext var1);

    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        UIContext uiContext = UIContext.of(context, mouseX, mouseY, delta);
        this.render(uiContext);
    }

    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        MouseButton mouseButton = MouseButton.fromButtonIndex(button);
        this.onMouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public final boolean mouseReleased(double mouseX, double mouseY, int button) {
        MouseButton mouseButton = MouseButton.fromButtonIndex(button);
        this.onMouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public final boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        MouseButton mouseButton = MouseButton.fromButtonIndex(button);
        this.onMouseDragged(mouseX, mouseY, mouseButton, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
    }

    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
    }

    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
    }
}

