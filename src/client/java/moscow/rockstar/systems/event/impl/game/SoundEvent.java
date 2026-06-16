/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.sound.SoundInstance
 */
package moscow.rockstar.systems.event.impl.game;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.client.sound.SoundInstance;

public class SoundEvent
extends Event {
    public SoundInstance sound;

    public SoundEvent(SoundInstance sound) {
        this.sound = sound;
    }

    @Generated
    public SoundInstance getSound() {
        return this.sound;
    }
}

