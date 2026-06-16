/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.world.ClientChunkManager
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.network.PacketByteBuf
 *  net.minecraft.network.packet.s2c.play.ChunkData$BlockEntityVisitor
 *  net.minecraft.util.math.ChunkPos
 *  net.minecraft.world.Heightmap$Type
 *  net.minecraft.world.chunk.WorldChunk
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.world.chunk;

import java.util.function.Consumer;
import moscow.rockstar.utility.chunkanimator.ChunkAnimator;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientChunkManager.class})
public class ClientChunkManagerMixin {
    @Inject(method={"loadChunkFromPacket"}, at={@At(value="TAIL")})
    private void onChunkLoad(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir) {
        ChunkPos pos = new ChunkPos(x, z);
        WorldChunk chunk = (WorldChunk)cir.getReturnValue();
        if (chunk != null) {
            float surfaceY = this.getSurfaceY(chunk);
            ChunkAnimator.startAnimation(pos, surfaceY);
        }
    }

    private float getSurfaceY(WorldChunk chunk) {
        int totalHeight = 0;
        int count = 0;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x, z);
                totalHeight += height;
                ++count;
            }
        }
        return count > 0 ? (float)totalHeight / (float)count : 64.0f;
    }
}

