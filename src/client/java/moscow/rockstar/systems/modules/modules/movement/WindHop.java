/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.item.equipment.EquipmentType
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.EntityJumpEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.ArmorSlot;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import moscow.rockstar.utility.mixins.ArmorItemAddition;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;

@ModuleInfo(name="Wind Hop", category=ModuleCategory.MOVEMENT, desc="modules.descriptions.wind_hop")
public class WindHop
extends BaseModule {
    private final BooleanSetting autoJump = new BooleanSetting((SettingsContainer)this, "modules.settings.wind_hop.autoJump", "modules.settings.wind_hop.autoJump.description").enable();
    private final BooleanSetting swingArm = new BooleanSetting((SettingsContainer)this, "modules.settings.wind_hop.swingArm", "modules.settings.wind_hop.swingArm.description").enable();
    private final BooleanSetting predictJump = new BooleanSetting((SettingsContainer)this, "modules.settings.wind_hop.predictJump", "modules.settings.wind_hop.predictJump.description").enable();
    private boolean elytra;
    private final EventListener<EntityJumpEvent> onJump = event -> {
        if (event.getEntity() == WindHop.mc.player) {
            this.useWindCharge();
        }
    };

    private void swapElytraChestplate() {
        boolean isElytraEquipped;
        ArmorSlot chestplateSlot = InventoryUtility.getChestplateSlot();
        SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
        ItemSlot elytraItemSlot = slotsToSearch.findItem(itemStack -> itemStack.getItem() == Items.ELYTRA && !itemStack.willBreakNextUse());
        ItemSlot chestplateItemSlot = slotsToSearch.findItem(itemStack -> {
            ArmorItem armorItem;
            Item patt0$temp = itemStack.getItem();
            return patt0$temp instanceof ArmorItem && ((ArmorItemAddition)(armorItem = (ArmorItem)patt0$temp)).rockstar$getType() == EquipmentType.CHESTPLATE;
        });
        boolean bl = isElytraEquipped = chestplateSlot.item() == Items.ELYTRA;
        if (!isElytraEquipped && elytraItemSlot != null) {
            elytraItemSlot.swapTo(chestplateSlot);
        } else if (chestplateItemSlot != null) {
            chestplateItemSlot.swapTo(chestplateSlot);
        }
    }

    @Override
    public void tick() {
        if (WindHop.mc.player == null || WindHop.mc.world == null) {
            return;
        }
        SlotGroup<ItemSlot> slotsToSearch = SlotGroups.offhand().and(SlotGroups.hotbar());
        ItemSlot slot = slotsToSearch.findItem(Items.WIND_CHARGE);
        boolean canUse = false;
        if (!WindHop.mc.player.isOnGround() && EntityUtility.getBlock(0.0, -2.0, 0.0) == Blocks.AIR && WindHop.mc.player.getVelocity().y > (double)0.4f) {
            float pitch = 75.0f;
            for (int i = 0; i < 360; i += 45) {
                BlockHitResult result = WindHop.mc.world.raycast(new RaycastContext(WindHop.mc.player.getEyePos(), WindHop.mc.player.getEyePos().add(WindHop.mc.player.getRotationVector(pitch, (float)i).multiply(1.5)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)WindHop.mc.player));
                if (result.getType() != HitResult.Type.BLOCK) continue;
                canUse = true;
            }
        } else {
            boolean bl = canUse = this.predictJump.isEnabled() && EntityUtility.getBlock(0.0, -1.0, 0.0) != Blocks.AIR && WindHop.mc.player.fallDistance > 2.0f;
        }
        if (canUse && (WindHop.mc.options.jumpKey.isPressed() || this.autoJump.isEnabled()) && slot != null) {
            this.useWindCharge();
        }
        if (WindHop.mc.player.isOnGround() && this.autoJump.isEnabled() && slot != null) {
            WindHop.mc.player.jump();
        }
        super.tick();
    }

    private void useWindCharge() {
        if (WindHop.mc.world == null || WindHop.mc.player == null || WindHop.mc.interactionManager == null) {
            return;
        }
        SlotGroup<ItemSlot> slotsToSearch = SlotGroups.offhand().and(SlotGroups.hotbar());
        ItemSlot slot = slotsToSearch.findItem(Items.WIND_CHARGE);
        boolean isOffhand = slot instanceof OffhandSlot;
        if (slot != null) {
            float pitch;
            HotbarSlot hotbarSlot;
            int oldHotbarSlotId = WindHop.mc.player.getInventory().selectedSlot;
            if (slot instanceof HotbarSlot && WindHop.mc.player.getInventory().selectedSlot != (hotbarSlot = (HotbarSlot)slot).getSlotId()) {
                InventoryUtility.selectHotbarSlot(hotbarSlot);
            }
            Hand hand = isOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
            float yaw = WindHop.mc.player.getYaw();
            if (!WindHop.mc.player.isOnGround() && EntityUtility.getBlock(0.0, -2.0, 0.0) == Blocks.AIR && WindHop.mc.player.getVelocity().y > (double)0.4f) {
                pitch = 75.0f;
                for (int i = 0; i < 360; i += 45) {
                    BlockHitResult result = WindHop.mc.world.raycast(new RaycastContext(WindHop.mc.player.getEyePos(), WindHop.mc.player.getEyePos().add(WindHop.mc.player.getRotationVector(pitch, (float)i).multiply(1.5)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)WindHop.mc.player));
                    if (result.getType() != HitResult.Type.BLOCK) continue;
                    yaw = RotationMath.adjustAngle(WindHop.mc.player.getYaw(), i);
                }
            } else {
                pitch = 90.0f;
            }
            float finalYaw = yaw;
            WindHop.mc.interactionManager.sendSequencedPacket(WindHop.mc.world, sequence -> new PlayerInteractItemC2SPacket(hand, sequence, finalYaw, pitch));
            if (this.swingArm.isEnabled()) {
                WindHop.mc.player.swingHand(hand);
            }
            if (slot instanceof HotbarSlot) {
                InventoryUtility.selectHotbarSlot(oldHotbarSlotId);
            }
        }
    }
}

