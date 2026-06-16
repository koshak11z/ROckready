package im.zov4ik.mixins.client.screen.ingame;

import im.zov4ik.events.container.HandledScreenEvent;
import im.zov4ik.features.impl.render.AuctionHelper;
import im.zov4ik.utils.client.managers.event.EventManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow public int backgroundWidth;
    @Shadow public int backgroundHeight;
    @Shadow @Nullable protected Slot focusedSlot;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickScreen(CallbackInfo ci) {
        AuctionHelper helper = AuctionHelper.getInstance();
        if (helper == null || !helper.isState()) return;
        helper.tick(((HandledScreen<?>) (Object) this).getScreenHandler());
    }

    @Inject(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", at = @At("HEAD"))
    private void onDrawSlotInject(DrawContext context, Slot slot, CallbackInfo ci) {
        AuctionHelper helper = AuctionHelper.getInstance();
        if (helper == null) return;
        helper.renderSlot(context, slot);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EventManager.callEvent(new HandledScreenEvent(context, focusedSlot, mouseX, mouseY, backgroundWidth, backgroundHeight));
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        AuctionHelper helper = AuctionHelper.getInstance();
        if (helper != null) helper.reset();
    }
}