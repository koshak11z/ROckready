/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={EntityVelocityUpdateS2CPacket.class})
public interface EntityVelocityUpdateAccessor {
    @Mutable
    @Accessor(value="velocityX")
    public void setVelocityX(int var1);

    @Mutable
    @Accessor(value="velocityY")
    public void setVelocityY(int var1);

    @Mutable
    @Accessor(value="velocityZ")
    public void setVelocityZ(int var1);
}

