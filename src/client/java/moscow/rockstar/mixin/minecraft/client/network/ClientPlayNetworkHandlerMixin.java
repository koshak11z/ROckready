/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientCommonNetworkHandler
 *  net.minecraft.client.network.ClientConnectionState
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.network.ClientConnection
 *  net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
 *  net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket
 *  net.minecraft.network.packet.s2c.play.GameJoinS2CPacket
 *  net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.chunk.WorldChunk
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.network;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.commands.ConfigCommand;
import moscow.rockstar.systems.event.impl.game.PickupEvent;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.modules.modules.visuals.XRay;
import moscow.rockstar.utility.game.WorldUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPlayNetworkHandler.class})
public abstract class ClientPlayNetworkHandlerMixin
extends ClientCommonNetworkHandler
implements IMinecraft {
    @Unique
    private Rotation oldRotation = Rotation.ZERO;

    protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    // Перехват кликабельных [Да]/[Нет] из ".cfg reset": клик по тексту шлёт "да"/"нет"
    // через sendChatMessage — гасим его и обрабатываем подтверждение, не отправляя в чат.
    @Inject(method={"sendChatMessage(Ljava/lang/String;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$onSendChatMessage(String content, CallbackInfo ci) {
        if (ConfigCommand.isAwaitingReset() && ConfigCommand.tryConfirm(content)) {
            ci.cancel();
        }
    }

    @Inject(method={"onItemPickupAnimation"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/world/ClientWorld;getEntityById(I)Lnet/minecraft/entity/Entity;", ordinal=0)})
    private void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet, CallbackInfo info) {
        Entity itemEntity = this.client.world.getEntityById(packet.getEntityId());
        Entity entity = this.client.world.getEntityById(packet.getCollectorEntityId());
        if (itemEntity instanceof ItemEntity && entity == this.client.player) {
            Rockstar.getInstance().getEventManager().triggerEvent(new PickupEvent(((ItemEntity)itemEntity).getStack(), packet.getStackAmount()));
        }
    }

    @Inject(method={"onBlockEntityUpdate"}, at={@At(value="TAIL")})
    private void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
        if (ClientPlayNetworkHandlerMixin.mc.world == null) {
            return;
        }
        BlockPos pos = packet.getPos();
        BlockEntity blockEntity = ClientPlayNetworkHandlerMixin.mc.world.getBlockEntity(pos);
        if (blockEntity != null && !WorldUtility.blockEntities.contains(blockEntity)) {
            WorldUtility.blockEntities.add(blockEntity);
        }
    }

    @Inject(method={"onChunkData"}, at={@At(value="TAIL")})
    private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        if (ClientPlayNetworkHandlerMixin.mc.world == null) {
            return;
        }
        WorldChunk chunk = ClientPlayNetworkHandlerMixin.mc.world.getChunk(packet.getChunkX(), packet.getChunkZ());
        chunk.getBlockEntities().values().forEach(be -> {
            if (!WorldUtility.blockEntities.contains(be)) {
                WorldUtility.blockEntities.add((BlockEntity)be);
            }
        });
        XRay xray = Rockstar.getInstance().getModuleManager().getModule(XRay.class);
        MinecraftClient mc = MinecraftClient.getInstance();
        if (xray == null || !xray.isEnabled() || mc.world == null) {
            return;
        }
        new Thread(() -> xray.scanChunk(chunk)).start();
    }

    @Inject(method={"onGameJoin"}, at={@At(value="TAIL")})
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        WorldUtility.blockEntities.clear();
        Rockstar.getInstance().getEventManager().triggerEvent(new WorldChangeEvent());
    }

    @Inject(method={"onPlayerPositionLook"}, at={@At(value="HEAD")})
    public void savePlayerRotation(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (ClientPlayNetworkHandlerMixin.mc.player == null) {
            return;
        }
        this.oldRotation = new Rotation(ClientPlayNetworkHandlerMixin.mc.player.getYaw(), ClientPlayNetworkHandlerMixin.mc.player.getPitch());
    }

    @Inject(method={"onPlayerPositionLook"}, at={@At(value="RETURN")})
    public void modifyPlayerRotation(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (ClientPlayNetworkHandlerMixin.mc.player == null) {
            return;
        }
        Rotation realServerRotation = new Rotation(packet.change().yaw(), packet.change().pitch());
    }
}

