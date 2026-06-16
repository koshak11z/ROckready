/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.sound.PositionedSoundInstance
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.client.sound.SoundInstance$AttenuationType
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.util.Identifier
 */
package moscow.rockstar.utility.sounds;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class ClientSoundInstance
extends PositionedSoundInstance {
    private static final float DEFAULT_PITCH = 1.0f;
    private final String fileName;

    public ClientSoundInstance(String fileName, float volume) {
        super(Identifier.of((String)(Rockstar.MOD_ID + ":" + fileName)), SoundCategory.MASTER, volume, 1.0f, SoundInstance.createRandom(), false, 0, SoundInstance.AttenuationType.NONE, 0.0, 0.0, 0.0, true);
        this.fileName = fileName;
    }

    public ClientSoundInstance(String fileName, float volume, float pitch) {
        super(Identifier.of((String)(Rockstar.MOD_ID + ":" + fileName)), SoundCategory.MASTER, volume, pitch, SoundInstance.createRandom(), false, 0, SoundInstance.AttenuationType.NONE, 0.0, 0.0, 0.0, true);
        this.fileName = fileName;
    }

    public void play(float volume) {
        MinecraftClient.getInstance().getSoundManager().play((SoundInstance)new ClientSoundInstance(this.fileName, volume));
    }

    public void play(float volume, float pitch) {
        MinecraftClient.getInstance().getSoundManager().play((SoundInstance)new ClientSoundInstance(this.fileName, volume, pitch));
    }

    @Generated
    public String getFileName() {
        return this.fileName;
    }
}

