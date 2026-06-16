/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
 *  net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.mixin.accessors.EntityVelocityUpdateAccessor;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Velocity", category=ModuleCategory.COMBAT, desc="modules.descriptions.velocity")
public class Velocity
extends BaseModule {
    private final ModeSetting mode = new ModeSetting(this, "mode");
    private final ModeSetting.Value cancel = new ModeSetting.Value(this.mode, "modules.settings.velocity.default");
    private final ModeSetting.Value compensation = new ModeSetting.Value(this.mode, "modules.settings.velocity.compensation");
    private final ModeSetting.Value modify = new ModeSetting.Value(this.mode, "modules.settings.velocity.modify");
    private final SliderSetting modifierX = new SliderSetting((SettingsContainer)this, "modules.settings.velocity.velocity_x", () -> !this.mode.is(this.modify)).suffix("%").currentValue(50.0f).min(0.0f).max(100.0f).step(1.0f);
    private final SliderSetting modifierY = new SliderSetting((SettingsContainer)this, "modules.settings.velocity.velocity_y", () -> !this.mode.is(this.modify)).suffix("%").currentValue(50.0f).min(0.0f).max(100.0f).step(1.0f);
    private final SliderSetting modifierZ = new SliderSetting((SettingsContainer)this, "modules.settings.velocity.velocity_z", () -> !this.mode.is(this.modify)).suffix("%").currentValue(50.0f).min(0.0f).max(100.0f).step(1.0f);
    private Vec3d lastMotion = Vec3d.ZERO;
    private boolean gotVelocity;
    private boolean wasHurt;
    private boolean jumped;
    private final EventListener<ReceivePacketEvent> onVelocityPacket = event -> {
        EntityVelocityUpdateS2CPacket velocityPacket;
        if (Velocity.mc.player == null || Velocity.mc.player.isDead()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        boolean isVelocityPacket = packet instanceof EntityVelocityUpdateS2CPacket && (velocityPacket = (EntityVelocityUpdateS2CPacket)packet).getEntityId() == Velocity.mc.player.getId();
        boolean isExplosionPacket = packet instanceof ExplosionS2CPacket;
        if (isVelocityPacket || isExplosionPacket) {
            EntityVelocityUpdateS2CPacket velocityPacket2;
            EntityVelocityUpdateS2CPacket velocityPacket3;
            EntityVelocityUpdateS2CPacket velocityPacket4;
            if (this.mode.is(this.cancel) && packet instanceof EntityVelocityUpdateS2CPacket && (velocityPacket4 = (EntityVelocityUpdateS2CPacket)packet).getEntityId() == Velocity.mc.player.getId()) {
                event.cancel();
            } else if (this.mode.is(this.modify) && packet instanceof EntityVelocityUpdateS2CPacket && (velocityPacket3 = (EntityVelocityUpdateS2CPacket)packet).getEntityId() == Velocity.mc.player.getId()) {
                int velocityX = (int)(velocityPacket3.getVelocityX() * 8000.0 * (double)this.modifierX.getCurrentValue() / 100.0);
                int velocityY = (int)(velocityPacket3.getVelocityY() * 8000.0 * (double)this.modifierY.getCurrentValue() / 100.0);
                int velocityZ = (int)(velocityPacket3.getVelocityZ() * 8000.0 * (double)this.modifierZ.getCurrentValue() / 100.0);
                EntityVelocityUpdateAccessor accessor = (EntityVelocityUpdateAccessor)velocityPacket3;
                accessor.setVelocityX(velocityX);
                accessor.setVelocityY(velocityY);
                accessor.setVelocityZ(velocityZ);
            } else if (this.mode.is(this.compensation) && packet instanceof EntityVelocityUpdateS2CPacket && (velocityPacket2 = (EntityVelocityUpdateS2CPacket)packet).getEntityId() == Velocity.mc.player.getId()) {
                this.lastMotion = new Vec3d(velocityPacket2.getVelocityX() * 8000.0, velocityPacket2.getVelocityY() * 8000.0, velocityPacket2.getVelocityZ() * 8000.0);
                this.gotVelocity = true;
            }
        }
    };

    @Override
    public void tick() {
        if (!this.mode.is(this.compensation) || !this.gotVelocity || Velocity.mc.player == null) {
            return;
        }
        if (Velocity.mc.player.hurtTime > 0) {
            this.wasHurt = true;
        }
        if (this.wasHurt && Velocity.mc.player.hurtTime == 0) {
            if (Velocity.mc.player.isOnGround()) {
                Velocity.mc.player.jump();
                this.jumped = true;
            }
            Vec3d moveDir = this.lastMotion.normalize();
            Vec3d updatedMotion = moveDir.multiply((double)-0.2f);
            Velocity.mc.player.setVelocity(updatedMotion.x, updatedMotion.y, updatedMotion.z);
            this.wasHurt = false;
            this.gotVelocity = false;
        }
        if (this.jumped && Velocity.mc.player.isOnGround()) {
            this.jumped = false;
        }
        super.tick();
    }
}

