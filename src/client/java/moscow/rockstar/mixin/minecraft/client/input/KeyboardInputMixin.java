/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.client.input.Input
 *  net.minecraft.client.input.KeyboardInput
 *  net.minecraft.util.PlayerInput
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.input;

import moscow.rockstar.Rockstar;
import moscow.rockstar.mixin.minecraft.client.input.InputAccessor;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={KeyboardInput.class})
public abstract class KeyboardInputMixin {
    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void onTick(CallbackInfo ci) {
        Input input = (Input)(Object)this;
        InputAccessor accessor = (InputAccessor)input;
        PlayerInput keys = accessor.getInput();
        float movementForward = accessor.getMovementForward();
        float movementSideways = accessor.getMovementSideways();
        boolean jumping = accessor.getInput().jump();
        boolean sneaking = accessor.getInput().sneak();
        boolean sprint = accessor.getInput().sprint();
        InputEvent event = new InputEvent(movementForward, movementSideways, jumping, sneaking, sprint);
        Rockstar.getInstance().getEventManager().triggerEvent(event);
        accessor.setMovementForward(event.getForward());
        accessor.setMovementSideways(event.getStrafe());
        boolean forwardKey = event.getForward() > 0.0f;
        boolean backwardKey = event.getForward() < 0.0f;
        boolean leftKey = event.getStrafe() > 0.0f;
        boolean rightKey = event.getStrafe() < 0.0f;
        accessor.setInput(new PlayerInput(forwardKey, backwardKey, leftKey, rightKey, event.isJump(), event.isSneak(), event.isSprint()));
    }
}
