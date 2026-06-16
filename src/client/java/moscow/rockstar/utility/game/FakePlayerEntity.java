/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.network.OtherClientPlayerEntity
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.Entity$RemovalReason
 */
package moscow.rockstar.utility.game;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

public class FakePlayerEntity
extends OtherClientPlayerEntity {
    public FakePlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    public void spawn() {
        this.unsetRemoved();
        this.clientWorld.addEntity((Entity)this);
    }

    public void remove() {
        this.clientWorld.removeEntity(this.getId(), Entity.RemovalReason.DISCARDED);
        this.onRemoved();
    }

    public void takeKnockback(double strength, double x, double z) {
    }
}

