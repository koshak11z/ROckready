/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="No Fall", category=ModuleCategory.PLAYER)
public class NoFall
extends BaseModule {
    @Override
    public void tick() {
        if ((double)NoFall.mc.player.fallDistance > 2.5) {
            Vec3d pos = NoFall.mc.player.getPos();
            NoFall.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, NoFall.mc.player.getYaw(), NoFall.mc.player.getPitch(), true, true));
            NoFall.mc.player.networkHandler.sendPacket((Packet)new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, NoFall.mc.player.getYaw(), NoFall.mc.player.getPitch()));
            NoFall.mc.player.fallDistance = 0.0f;
        }
    }
}

