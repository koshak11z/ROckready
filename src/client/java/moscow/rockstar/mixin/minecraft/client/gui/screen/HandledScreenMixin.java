/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.ingame.HandledScreen
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.collection.DefaultedList
 *  org.lwjgl.glfw.GLFW
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.event.impl.render.ScreenRenderEvent;
import moscow.rockstar.systems.event.impl.window.ContainerClickEvent;
import moscow.rockstar.systems.event.impl.window.ContainerReleaseEvent;
import moscow.rockstar.systems.modules.modules.player.InvUtils;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={HandledScreen.class})
public abstract class HandledScreenMixin
implements IMinecraft {
    @Unique
    private final Timer timer = new Timer();

    @Shadow
    protected abstract boolean method_2387(Slot var1, double var2, double var4);

    @Shadow
    protected abstract void method_2383(Slot var1, int var2, int var3, SlotActionType var4);

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CustomDrawContext customDrawContext = CustomDrawContext.of(context);
        Rockstar.getInstance().getEventManager().triggerEvent(new ScreenRenderEvent(customDrawContext, delta));
        DefaultedList<Slot> slots = HandledScreenMixin.mc.player.currentScreenHandler.slots;
        for (Slot slot : slots) {
            InvUtils invUtils = Rockstar.getInstance().getModuleManager().getModule(InvUtils.class);
            if (!this.method_2387(slot, mouseX, mouseY) || !slot.isEnabled() || !invUtils.isEnabled() || !invUtils.getScroller().isSelected() || !this.timer.finished((long)invUtils.getScrollDelay().getCurrentValue()) || !InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340) || GLFW.glfwGetMouseButton((long)mc.getWindow().getHandle(), (int)0) != 1) continue;
            this.method_2383(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
            this.timer.reset();
        }
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")})
    private void onMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        Rockstar.getInstance().getEventManager().triggerEvent(new ContainerClickEvent((float)mouseX, (float)mouseY, button));
    }

    @Inject(method={"mouseReleased"}, at={@At(value="HEAD")})
    public void mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        Rockstar.getInstance().getEventManager().triggerEvent(new ContainerReleaseEvent((float)mouseX, (float)mouseY, button));
    }
}
