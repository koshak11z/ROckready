package im.zov4ik.mixins.player.entity;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import im.zov4ik.utils.client.managers.event.EventManager;
import im.zov4ik.utils.client.managers.event.types.EventType;
import im.zov4ik.utils.interactions.item.ItemToolkit;
import im.zov4ik.events.block.BlockBreakingEvent;
import im.zov4ik.events.block.BreakBlockEvent;
import im.zov4ik.events.item.ClickSlotEvent;
import im.zov4ik.events.item.UsingItemEvent;
import im.zov4ik.events.player.AttackEvent;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"),cancellable = true)
    public void attackEntityHook(PlayerEntity player, Entity target, CallbackInfo info) {
        AttackEvent event = new AttackEvent(target);
        EventManager.callEvent(event);
        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "interactItem", at = @At(value = "RETURN"))
    public void interactItemHook(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue() instanceof ActionResult.Success success && !success.swingSource().equals(ActionResult.SwingSource.CLIENT)) {
            UsingItemEvent event = new UsingItemEvent(EventType.PRE);
            EventManager.callEvent(event);
        }
    }

    @Inject(method = "stopUsingItem", at = @At("HEAD"),cancellable = true)
    public void stopUsingItemHook(CallbackInfo ci) {
        UsingItemEvent event = new UsingItemEvent(EventType.POST);
        EventManager.callEvent(event);
        if (ItemToolkit.INSTANCE.isUseItem()) {
            ItemToolkit.INSTANCE.setUseItem(false);
            ci.cancel();
        }
    }

    @Inject(method = "interactItem", at = @At(value = "HEAD"), cancellable = true)
    private void gameModeHook(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        UsingItemEvent event = new UsingItemEvent(EventType.START);
        EventManager.callEvent(event);
        if (event.isCancelled()) cir.setReturnValue(ActionResult.PASS);
    }

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    public void clickSlotHook(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo info) {
        ClickSlotEvent event = new ClickSlotEvent(syncId,slotId,button,actionType);
        EventManager.callEvent(event);
        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At(value = "HEAD"))
    private void injectBlockBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        EventManager.callEvent(new BlockBreakingEvent(pos, direction));
    }

    @Inject(method = "breakBlock", at = @At(value = "RETURN"))
    private void injectBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        EventManager.callEvent(new BreakBlockEvent(pos));
    }
}
