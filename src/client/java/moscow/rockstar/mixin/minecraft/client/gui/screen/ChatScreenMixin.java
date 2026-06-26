/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.ChatInputSuggestor
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.widget.TextFieldWidget
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import baritone.api.BaritoneAPI;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.commands.ConfigCommand;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.event.impl.render.ChatRenderEvent;
import moscow.rockstar.systems.event.impl.window.ChatClickEvent;
import moscow.rockstar.systems.event.impl.window.ChatReleaseEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ChatScreen.class})
public class ChatScreenMixin
extends Screen
implements IMinecraft {
    @Shadow
    protected TextFieldWidget field_2382;
    @Shadow
    private ChatInputSuggestor field_21616;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method={"sendMessage(Ljava/lang/String;Z)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendMessage(String text, boolean addToHistory, CallbackInfo ci) {
        if (ConfigCommand.isAwaitingReset() && ConfigCommand.tryConfirm(text)) {
            ChatScreenMixin.mc.inGameHud.getChatHud().addToMessageHistory(text);
            ci.cancel();
            return;
        }
        if (text != null && text.startsWith("#")) {
            try {
                String command = text.substring(1).trim();
                if (command.isEmpty()) command = "help";
                BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
                ChatScreenMixin.mc.inGameHud.getChatHud().addToMessageHistory(text);
                ci.cancel();
                return;
            } catch (Throwable ignored) {
            }
        }
        if (Rockstar.getInstance().getCommandManager().dispatch(text)) {
            ChatScreenMixin.mc.inGameHud.getChatHud().addToMessageHistory(text);
            ci.cancel();
        }
    }

    @Inject(method={"render"}, at={@At(value="RETURN")})
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Rockstar.getInstance().getEventManager().triggerEvent(new ChatRenderEvent(CustomDrawContext.of(context), delta));
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")})
    private void onMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        Rockstar.getInstance().getEventManager().triggerEvent(new ChatClickEvent((float)mouseX, (float)mouseY, button));
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Rockstar.getInstance().getEventManager().triggerEvent(new ChatReleaseEvent((float)mouseX, (float)mouseY, button));
        return true;
    }
}

