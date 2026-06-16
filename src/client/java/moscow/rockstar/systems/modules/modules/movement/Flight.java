/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.movement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEndEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Flight", category=ModuleCategory.MOVEMENT)
public class Flight
        extends BaseModule {
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.flight.mode");
    private final ModeSetting.Value vanilla = new ModeSetting.Value(this.mode, "modules.settings.flight.vanilla");
    private final ModeSetting.Value elytraY = new ModeSetting.Value(this.mode, "ElytraY");
    private final ModeSetting.Value levitationBoost = new ModeSetting.Value(this.mode, "modules.settings.flight.levitationBoost");
    private final ModeSetting.Value elytraGlide = new ModeSetting.Value(this.mode, "Elytra Glide").select();
    private final ModeSetting.Value grimGlide = new ModeSetting.Value(this.mode, "Grim Glide");
    private boolean wasFlyingAllowed = false;
    private boolean wasFlying = false;
    private final SliderSetting speed = new SliderSetting((SettingsContainer)this, "modules.settings.flight.speed", () -> !this.vanilla.isSelected()).currentValue(1.0f).max(10.0f).min(0.1f).step(0.1f);
    private int ticks;
    private Timer ticksTimer = new Timer();
    int ticksTwo = 0;
    private final EventListener<ClientPlayerTickEndEvent> tickEnd = event -> {
        if (this.grimGlide.isSelected() && Flight.mc.player.isGliding()) {
            ++this.ticksTwo;
            Vec3d pos = Flight.mc.player.getPos();
            float yaw = Flight.mc.player.getYaw();
            double forward = 0.085f;
            double motion = Flight.getBps((Entity)Flight.mc.player, 1);
            if (motion >= 52.0) {
                forward = 0.0;
            }
            double dx = -Math.sin(Math.toRadians(yaw)) * forward;
            double dz = Math.cos(Math.toRadians(yaw)) * forward;
            Flight.mc.player.setVelocity(dx * (double)MathUtility.random(1.1f, 1.21f), Flight.mc.player.getVelocity().y - (double)0.01f, dz * (double)MathUtility.random(1.1f, 1.21f));
            if (this.ticksTimer.finished(45L)) {
                Flight.mc.player.setPosition(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                this.ticksTimer.reset();
            }
            Flight.mc.player.setVelocity(dx * (double)MathUtility.random(1.1f, 1.21f), Flight.mc.player.getVelocity().y + (double)0.015f, dz * (double)MathUtility.random(1.1f, 1.21f));
        }
    };

    @Override
    public void onEnable() {
        if (Flight.mc.player == null) {
            return;
        }
        if (this.vanilla.isSelected()) {
            Flight.mc.player.getAbilities().allowFlying = true;
        }
        super.onEnable();
    }

    @Override
    public void tick() {
        SlotGroup<HotbarSlot> slotsToSearch;
        HotbarSlot slot;
        if (Flight.mc.player == null) {
            return;
        }
        if (this.vanilla.isSelected()) {
            this.wasFlyingAllowed = Flight.mc.player.getAbilities().allowFlying;
            this.wasFlying = Flight.mc.player.getAbilities().flying;
            Flight.mc.player.getAbilities().allowFlying = true;
            Flight.mc.player.getAbilities().setFlySpeed(this.speed.getCurrentValue() / 10.0f);
            super.tick();
        } else if (this.elytraY.isSelected()) {
            if (Flight.mc.player.isGliding()) {
                Flight.mc.player.setVelocity(Flight.mc.player.getVelocity().x, Flight.mc.player.getVelocity().y + 0.05999999761581421, Flight.mc.player.getVelocity().z);
            }
        } else if (this.levitationBoost.isSelected()) {
            if (Flight.mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
                StatusEffectInstance effect = Flight.mc.player.getStatusEffect(StatusEffects.LEVITATION);
                int amplifier = effect == null ? 0 : effect.getAmplifier() + 1;
                Vec3d velocity = Flight.mc.player.getVelocity();
                Flight.mc.player.setVelocity(velocity.x, velocity.y + 0.035f * (float)amplifier, velocity.z);
            }
        } else if (this.elytraGlide.isSelected() && (slot = (slotsToSearch = SlotGroups.hotbar()).findItem(Items.ELYTRA)) != null && Flight.mc.player.age % 10 != 0) {
            HotbarSlot currentItem = InventoryUtility.getCurrentHotbarSlot();
            Flight.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
            InventoryUtility.selectHotbarSlot(slot);
            Flight.mc.interactionManager.interactItem((PlayerEntity)Flight.mc.player, Hand.MAIN_HAND);
            ((Slot)Flight.mc.player.currentScreenHandler.slots.get(6)).setStack(new ItemStack((ItemConvertible)Items.ELYTRA));
            if (Flight.mc.player.isSprinting() && Flight.mc.player.input.hasForwardMovement() && Flight.mc.player.checkGliding()) {
                Flight.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)Flight.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
            InventoryUtility.selectHotbarSlot(currentItem);
            Flight.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(Flight.mc.player.getInventory().selectedSlot));
        }
    }

    public static double getBps(Entity entity, int decimal) {
        double x = entity.getX() - entity.prevX;
        double y = entity.getY() - entity.prevY;
        double z = entity.getZ() - entity.prevZ;
        double speed = Math.sqrt(x * x + y * y + z * z) * 20.0;
        return Flight.roundHalfUp(speed, decimal);
    }

    public static double roundHalfUp(double num, double increment) {
        double v = (double)Math.round(num / increment) * increment;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onDisable() {
        if (Flight.mc.player == null) {
            return;
        }
        if (this.vanilla.isSelected()) {
            Flight.mc.player.getAbilities().setFlySpeed(0.05f);
            Flight.mc.player.getAbilities().allowFlying = false;
            Flight.mc.player.getAbilities().flying = false;
        }
        super.onDisable();
    }
}

