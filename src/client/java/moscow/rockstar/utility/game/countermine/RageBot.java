/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.utility.game.countermine;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.modules.modules.other.CounterMine;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.target.TargetComparators;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.animation.types.RotationAnimation;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.countermine.CMUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RageBot
implements IMinecraft {
    public static float TARGET_YAW;
    private BooleanSetting aim;
    private BooleanSetting rage;
    private BooleanSetting silent;
    private BooleanSetting autoShoot;
    private BooleanSetting autoStop;
    private SliderSetting shootDelay;
    private boolean stopping;
    private int stopTicks = -1;
    private final Timer shootingTimer = new Timer();
    private final RotationAnimation anim = new RotationAnimation(100L, 100L, Easing.BAKEK);
    private CounterMine counterMine;
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        this.stopping = false;
        this.stopTicks = Math.abs(EntityUtility.getVelocity()) <= (double)0.1f ? ++this.stopTicks : 0;
        if (!this.aim.isEnabled()) {
            return;
        }
        TARGET_YAW = RageBot.mc.player.getYaw();
        TargetSettings settings = new TargetSettings.Builder().targetPlayers(true).requiredRange(200.0f).sortBy(TargetComparators.FOV).build();
        Rockstar.getInstance().getTargetManager().update(settings);
        Entity targetEntity = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        if (targetEntity == null) {
            return;
        }
        Vec3d pos = targetEntity.getPos().add(0.0, (double)0.2f, 0.0);
        Rotation toTarget = CMUtility.calculateRotation(pos);
        float yaw = toTarget.getYaw();
        float pitch = toTarget.getPitch();
        float deltaYaw = RotationMath.getAngleDifference(yaw, RageBot.mc.player.getYaw());
        if (!this.rage.isEnabled() && deltaYaw > 145.0f) {
            return;
        }
        if (this.rage.isEnabled()) {
            if (this.silent.isEnabled()) {
                Rockstar.getInstance().getRotationHandler().rotate(toTarget, MoveCorrection.NONE, 180.0f, 180.0f, 180.0f, RotationPriority.TO_TARGET);
            } else {
                RageBot.mc.player.setYaw(yaw);
                RageBot.mc.player.setPitch(pitch);
                RageBot.mc.player.setHeadYaw(yaw);
            }
        } else if (this.silent.isEnabled()) {
            if (!this.counterMine.getAntiAim().getAntiAim().isEnabled() && this.counterMine.getJumping().finished(1000L)) {
                RageBot.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(RageBot.mc.player.getX(), RageBot.mc.player.getY(), RageBot.mc.player.getZ(), RageBot.mc.player.getYaw(), RageBot.mc.player.getPitch(), RageBot.mc.player.isOnGround(), RageBot.mc.player.isOnGround()));
            }
            Rockstar.getInstance().getRotationHandler().rotate(new Rotation(yaw, pitch), MoveCorrection.NONE, 180.0f, 180.0f, 180.0f, RotationPriority.TO_TARGET);
        } else {
            this.anim.setRotation(new Rotation(yaw, pitch));
            RageBot.mc.player.setYaw(this.anim.getRotation().getYaw());
            RageBot.mc.player.setPitch(this.anim.getRotation().getPitch());
            RageBot.mc.player.setHeadYaw(this.anim.getRotation().getYaw());
        }
        TARGET_YAW = yaw;
        if (!this.autoShoot.isEnabled() || (!this.rage.isEnabled() ? !MathUtility.canSeen(pos) : !MathUtility.canShoot(pos)) || !this.shootingTimer.finished((long)this.shootDelay.getCurrentValue())) {
            return;
        }
        if (this.autoStop.isEnabled() && this.stopTicks <= 0) {
            this.stop();
            return;
        }
        if (RageBot.mc.player.isOnGround() || Math.abs(RageBot.mc.player.getVelocity().y) < (double)0.05f) {
            this.shot();
            this.stop();
        }
    };
    private final EventListener<InputEvent> onMove = event -> {
        if (this.stopping) {
            event.setForward(0.0f);
            event.setStrafe(0.0f);
        }
    };

    public RageBot(CounterMine cm) {
        this.counterMine = cm;
        this.aim = new BooleanSetting(cm, "Aim");
        this.rage = new BooleanSetting((SettingsContainer)cm, "Rage", () -> !this.aim.isEnabled());
        this.silent = new BooleanSetting((SettingsContainer)cm, "Silent", () -> !this.aim.isEnabled());
        this.autoShoot = new BooleanSetting((SettingsContainer)cm, "AutoShoot", () -> !this.aim.isEnabled());
        this.autoStop = new BooleanSetting((SettingsContainer)cm, "AutoStop", () -> !this.aim.isEnabled() || !this.autoShoot.isEnabled());
        this.shootDelay = new SliderSetting(cm, "Shoot Delay").min(0.0f).max(2000.0f).step(50.0f).currentValue(1000.0f).suffix(number -> " ms");
    }

    private void shot() {
        RageBot.mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, BlockPos.ORIGIN, Direction.DOWN));
        RageBot.mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, BlockPos.ORIGIN, Direction.DOWN));
        this.shootingTimer.reset();
    }

    private void stop() {
        EntityUtility.setSpeed(0.0);
        this.stopping = true;
    }
}

