/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.GameMode
 */
package moscow.rockstar.systems.modules.modules.player;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.FakePlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;

@ModuleInfo(name="Free Camera", category=ModuleCategory.PLAYER, desc="\u0421\u0432\u043e\u0431\u043e\u0434\u043d\u0430\u044f \u043a\u0430\u043c\u0435\u0440\u0430")
public class FreeCam
extends BaseModule {
    private boolean wasFlyingAllowed = false;
    private boolean wasFlying = false;
    private float oldFlyingSpeed = 0.0f;
    private FakePlayerEntity dummy = null;
    private GameMode prevGameMode;
    private final SliderSetting speed = new SliderSetting(this, "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c").currentValue(1.0f).max(15.0f).min(0.1f).step(0.1f).currentValue(3.0f);
    private final BooleanSetting display = new BooleanSetting(this, "\u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0442\u044c \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b");
    private int x;
    private int y;
    private int z;
    private final EventListener<HudRenderEvent> render2d = event -> {
        if (this.display.isEnabled()) {
            BlockPos diff = FreeCam.mc.player.getBlockPos().subtract(new Vec3i(this.x, this.y, this.z));
            String pos = "X: " + diff.getX() + " Y: " + diff.getY() + " Z: " + diff.getZ();
            Font bold = Fonts.BOLD.getFont(8.0f);
            event.getContext().drawText(bold, Text.of((String)pos), sr.getScaledWidth() / 2.0f - bold.width(pos) / 2.0f + 8.0f, sr.getScaledHeight() / 2.0f - 20.0f);
        }
    };
    private final EventListener<SendPacketEvent> onSendPacket = event -> {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.cancel();
        }
    };

    @Override
    public void tick() {
        if (FreeCam.mc.player == null) {
            return;
        }
        FreeCam.mc.player.getAbilities().setFlySpeed(this.speed.getCurrentValue() / 10.0f);
        FreeCam.mc.player.getAbilities().flying = true;
        super.tick();
    }

    @Override
    public void onEnable() {
        if (FreeCam.mc.player == null || FreeCam.mc.world == null) {
            return;
        }
        this.wasFlyingAllowed = FreeCam.mc.player.getAbilities().allowFlying;
        this.wasFlying = FreeCam.mc.player.getAbilities().flying;
        this.oldFlyingSpeed = FreeCam.mc.player.getAbilities().getFlySpeed();
        this.prevGameMode = FreeCam.mc.interactionManager.getCurrentGameMode();
        this.dummy = new FakePlayerEntity(FreeCam.mc.world, new GameProfile(UUID.randomUUID(), mc.getSession().getUsername()));
        this.dummy.copyFrom((Entity)FreeCam.mc.player);
        this.dummy.copyPositionAndRotation((Entity)FreeCam.mc.player);
        this.dummy.spawn();
        FreeCam.mc.player.getAbilities().allowFlying = true;
        FreeCam.mc.player.getAbilities().flying = true;
        FreeCam.mc.interactionManager.setGameMode(GameMode.SPECTATOR);
        FreeCam.mc.player.getAbilities().setFlySpeed(this.speed.getCurrentValue() / 10.0f);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (this.dummy == null || FreeCam.mc.world == null || FreeCam.mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }
        FreeCam.mc.player.copyPositionAndRotation((Entity)this.dummy);
        mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(this.dummy.getX(), this.dummy.getY(), this.dummy.getZ(), false, FreeCam.mc.player.horizontalCollision));
        FreeCam.mc.player.getAbilities().allowFlying = this.wasFlyingAllowed;
        FreeCam.mc.player.getAbilities().flying = this.wasFlying;
        FreeCam.mc.player.getAbilities().setFlySpeed(this.oldFlyingSpeed);
        FreeCam.mc.interactionManager.setGameMode(this.prevGameMode);
        this.dummy.remove();
        this.dummy = null;
        FreeCam.mc.player.setVelocity(0.0, 0.0, 0.0);
        super.onDisable();
    }
}

