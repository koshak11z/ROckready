/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.util.Hand
 */
package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

@ModuleInfo(name="Elytra Strafe", category=ModuleCategory.MOVEMENT)
public class ElytraStrafe
extends BaseModule {
    private final SliderSetting fireworkSlot = new SliderSetting(this, "modules.settings.elytra_target.fireworkSlot").min(1.0f).max(9.0f).step(1.0f).currentValue(7.0f).suffix(" slot");
    private final SliderSetting fireworkDelay = new SliderSetting(this, "modules.settings.elytra_target.fireworkDelay").min(0.1f).max(2.0f).step(0.1f).currentValue(1.0f).suffix(" sec");
    private final BooleanSetting autoTakeoff = new BooleanSetting(this, "modules.settings.elytra_strafe.autoTakeoff").enable();
    private final Timer fireworkTimer = new Timer();
    private final EventListener<InputEvent> onInput = event -> {
        float pitch = 0.0f;
        if (ElytraStrafe.mc.player == null || !ElytraStrafe.mc.player.isGliding()) {
            return;
        }
        float yaw = (float)Math.toDegrees(EntityUtility.direction(ElytraStrafe.mc.player.getYaw(), event.getForward(), event.getStrafe()));
        if (ElytraStrafe.mc.options.sneakKey.isPressed() || ElytraStrafe.mc.options.jumpKey.isPressed()) {
            pitch = event.getStrafe() + event.getForward() > 0.1f ? -45.0f : -90.0f;
        }
        if (ElytraStrafe.mc.options.sneakKey.isPressed()) {
            pitch *= -1.0f;
        }
        Rockstar.getInstance().getRotationHandler().rotate(new Rotation(yaw, pitch), MoveCorrection.DIRECT, 180.0f, 180.0f, 180.0f, RotationPriority.NOT_IMPORTANT);
    };

    @Override
    public void tick() {
        if (ElytraStrafe.mc.player == null) {
            return;
        }
        if (this.autoTakeoff.isEnabled()) {
            boolean isElytraEquipped;
            boolean bl = isElytraEquipped = InventoryUtility.getChestplateSlot().item() == Items.ELYTRA;
            if (!ElytraStrafe.mc.player.isGliding() && isElytraEquipped && !ElytraStrafe.mc.player.isOnGround() && !ElytraStrafe.mc.player.isInFluid()) {
                ElytraStrafe.mc.player.startGliding();
                ElytraStrafe.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraStrafe.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            } else if (ElytraStrafe.mc.player.isOnGround() && isElytraEquipped && !ElytraStrafe.mc.player.isInFluid() && !ElytraStrafe.mc.player.isGliding()) {
                ElytraStrafe.mc.player.jump();
            }
        }
        if (!ElytraStrafe.mc.player.isGliding()) {
            return;
        }
        if (this.fireworkTimer.finished((long)(this.fireworkDelay.getCurrentValue() * 1000.0f)) && !ElytraStrafe.mc.player.isUsingItem()) {
            this.useFirework();
        }
    }

    private void useFirework() {
        SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
        HotbarSlot slot = slotsToSearch.findItem(Items.FIREWORK_ROCKET);
        if (slot != null) {
            ElytraStrafe.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
            ElytraStrafe.mc.interactionManager.sendSequencedPacket(ElytraStrafe.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, ElytraStrafe.mc.player.getYaw(), ElytraStrafe.mc.player.getPitch()));
            ElytraStrafe.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(ElytraStrafe.mc.player.getInventory().selectedSlot));
            this.fireworkTimer.reset();
            return;
        }
        SlotGroup<InventorySlot> search = SlotGroups.inventory();
        InventorySlot invSlot = search.findItem(Items.FIREWORK_ROCKET);
        if (invSlot != null) {
            InventoryUtility.hotbarSwap(invSlot.getIdForServer(), (int)(this.fireworkSlot.getCurrentValue() - 1.0f));
            this.fireworkTimer.reset();
        }
    }
}
