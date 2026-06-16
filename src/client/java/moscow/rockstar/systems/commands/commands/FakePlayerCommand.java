/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.network.OtherClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.MovementType
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.listener.ClientPlayPacketListener
 *  net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.commands.commands;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class FakePlayerCommand
implements IMinecraft {
    private OtherClientPlayerEntity fakePlayer;
    private float moveForward = 0.0f;
    private float moveStrafe = 0.0f;
    private final EventListener<AttackEvent> onAttackEvent = event -> {
        if (this.fakePlayer != null && event.getEntity() == this.fakePlayer && this.fakePlayer.hurtTime == 0) {
            FakePlayerCommand.mc.world.playSound((PlayerEntity)FakePlayerCommand.mc.player, this.fakePlayer.getX(), this.fakePlayer.getY(), this.fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (FakePlayerCommand.mc.player.fallDistance > 0.0f) {
                FakePlayerCommand.mc.world.playSound((PlayerEntity)FakePlayerCommand.mc.player, this.fakePlayer.getX(), this.fakePlayer.getY(), this.fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            } else {
                FakePlayerCommand.mc.world.playSound((PlayerEntity)FakePlayerCommand.mc.player, this.fakePlayer.getX(), this.fakePlayer.getY(), this.fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            this.fakePlayer.onDamaged(FakePlayerCommand.mc.world.getDamageSources().generic());
            this.fakePlayer.setHealth(this.fakePlayer.getHealth() + this.fakePlayer.getAbsorptionAmount() - 1.0f);
            if (this.fakePlayer.isDead()) {
                this.fakePlayer.setHealth(10.0f);
                new EntityStatusS2CPacket((Entity)this.fakePlayer, (byte)35).apply((ClientPlayPacketListener)FakePlayerCommand.mc.player.networkHandler);
            }
        }
    };
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (this.fakePlayer == null || FakePlayerCommand.mc.currentScreen != null) {
            return;
        }
        int key = event.getKey();
        int action = event.getAction();
        if (key == 265) {
            this.moveForward = action == 1 || action == 2 ? 1.0f : 0.0f;
        } else if (key == 264) {
            this.moveForward = action == 1 || action == 2 ? -1.0f : 0.0f;
        } else if (key == 263) {
            this.moveStrafe = action == 1 || action == 2 ? 1.0f : 0.0f;
        } else if (key == 262) {
            this.moveStrafe = action == 1 || action == 2 ? -1.0f : 0.0f;
        }
    };
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
        if (this.fakePlayer == null || FakePlayerCommand.mc.player == null) {
            return;
        }
        if (this.moveForward != 0.0f || this.moveStrafe != 0.0f) {
            float yaw = FakePlayerCommand.mc.player.getYaw();
            double speed = 0.2;
            double motionX = (double)this.moveStrafe * Math.cos(Math.toRadians(yaw)) - (double)this.moveForward * Math.sin(Math.toRadians(yaw));
            double motionZ = (double)this.moveForward * Math.cos(Math.toRadians(yaw)) + (double)this.moveStrafe * Math.sin(Math.toRadians(yaw));
            Vec3d velocity = new Vec3d(motionX * speed, this.fakePlayer.getVelocity().y, motionZ * speed);
            this.fakePlayer.setVelocity(velocity);
            this.fakePlayer.move(MovementType.SELF, velocity);
            this.fakePlayer.setSprinting(true);
        } else {
            this.fakePlayer.setSprinting(false);
            this.fakePlayer.setVelocity(0.0, this.fakePlayer.getVelocity().y, 0.0);
            this.fakePlayer.limbAnimator.setSpeed(0.0f);
        }
    };

    public FakePlayerCommand() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    public Command command() {
        return CommandBuilder.begin("fakeplayer").aliases("fp").desc("commands.fakeplayer.description").param("action", p -> p.literal("add", "del")).handler(this::handle).build();
    }

    private void handle(CommandContext ctx) {
        String action = (String)ctx.arguments().getFirst();
        switch (action.toLowerCase()) {
            case "add": {
                this.add();
                break;
            }
            case "del": {
                this.del();
            }
        }
    }

    public void add() {
        if (this.fakePlayer != null) {
            this.fakePlayer.discard();
            this.fakePlayer = null;
        }
        this.fakePlayer = new OtherClientPlayerEntity(FakePlayerCommand.mc.world, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), "FakePlayer"));
        this.fakePlayer.copyPositionAndRotation((Entity)FakePlayerCommand.mc.player);
        this.fakePlayer.setStackInHand(Hand.MAIN_HAND, FakePlayerCommand.mc.player.getMainHandStack().copy());
        this.fakePlayer.setStackInHand(Hand.OFF_HAND, FakePlayerCommand.mc.player.getOffHandStack().copy());
        this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
        this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
        this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
        FakePlayerCommand.mc.world.addEntity((Entity)this.fakePlayer);
        Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.SUCCESS, Localizator.translate("commands.fakeplayer.success"), Localizator.translate("commands.fakeplayer.added"));
    }

    public void del() {
        if (this.fakePlayer == null) {
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.ERROR, Localizator.translate("commands.fakeplayer.error"), Localizator.translate("commands.fakeplayer.not_exists"));
            return;
        }
        this.fakePlayer.discard();
        this.fakePlayer = null;
        Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.SUCCESS, Localizator.translate("commands.fakeplayer.success"), Localizator.translate("commands.fakeplayer.removed"));
    }
}
