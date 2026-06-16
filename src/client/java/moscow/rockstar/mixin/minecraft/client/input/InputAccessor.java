/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.input.Input
 *  net.minecraft.util.PlayerInput
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.minecraft.client.input;

import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Input.class})
public interface InputAccessor {
    @Accessor(value="movementForward")
    public float getMovementForward();

    @Accessor(value="movementForward")
    public void setMovementForward(float var1);

    @Accessor(value="movementSideways")
    public float getMovementSideways();

    @Accessor(value="movementSideways")
    public void setMovementSideways(float var1);

    @Accessor(value="playerInput")
    public PlayerInput getInput();

    @Accessor(value="playerInput")
    public void setInput(PlayerInput var1);
}

