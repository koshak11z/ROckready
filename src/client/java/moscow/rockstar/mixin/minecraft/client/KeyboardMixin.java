/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Keyboard
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.gui.screen.Screen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Keyboard.class})
public class KeyboardMixin
implements IMinecraft {
    @Inject(method={"onKey"}, at={@At(value="HEAD")})
    public void triggerKeyEvent(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (key == -1) {
            return;
        }
        Rockstar.getInstance().getEventManager().triggerEvent(new KeyPressEvent(action, key));
        if (KeyboardMixin.mc.currentScreen != null) {
            return;
        }
        if (key == 46 && action == 1) {
            mc.setScreen((Screen)new ChatScreen(""));
        }
    }
}

