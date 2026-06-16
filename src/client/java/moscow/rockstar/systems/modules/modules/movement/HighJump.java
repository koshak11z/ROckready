/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;

@ModuleInfo(name="High Jump", category=ModuleCategory.MOVEMENT)
public class HighJump
extends BaseModule {
    @Override
    public void tick() {
        HighJump.mc.options.sneakKey.setPressed(true);
        HighJump.mc.player.setPitch(90.0f);
        HighJump.mc.options.useKey.setPressed(true);
        if (HighJump.mc.player.isOnGround() && !HighJump.mc.player.getItemCooldownManager().isCoolingDown(Items.WIND_CHARGE.getDefaultStack())) {
            this.useWindCharge();
        }
    }

    @Override
    public void onDisable() {
        int keyCode = InputUtil.fromTranslationKey((String)HighJump.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode();
        HighJump.mc.options.sneakKey.setPressed(InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)keyCode));
        keyCode = InputUtil.fromTranslationKey((String)HighJump.mc.options.useKey.getBoundKeyTranslationKey()).getCode();
        HighJump.mc.options.useKey.setPressed(InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)keyCode));
    }

    private void useWindCharge() {
        if (HighJump.mc.world == null || HighJump.mc.player == null || HighJump.mc.interactionManager == null) {
            return;
        }
        SlotGroup<ItemSlot> slotsToSearch = SlotGroups.offhand().and(SlotGroups.hotbar());
        ItemSlot slot = slotsToSearch.findItem(Items.WIND_CHARGE);
        boolean isOffhand = slot instanceof OffhandSlot;
        if (slot != null) {
            float pitch;
            HotbarSlot hotbarSlot;
            int oldHotbarSlotId = HighJump.mc.player.getInventory().selectedSlot;
            if (slot instanceof HotbarSlot && HighJump.mc.player.getInventory().selectedSlot != (hotbarSlot = (HotbarSlot)slot).getSlotId()) {
                InventoryUtility.selectHotbarSlot(hotbarSlot);
            }
            Hand hand = isOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
            float yaw = HighJump.mc.player.getYaw();
            if (!HighJump.mc.player.isOnGround() && EntityUtility.getBlock(0.0, -2.0, 0.0) == Blocks.AIR && HighJump.mc.player.getVelocity().y > (double)0.4f) {
                pitch = 75.0f;
                for (int i = 0; i < 360; i += 45) {
                    BlockHitResult result = HighJump.mc.world.raycast(new RaycastContext(HighJump.mc.player.getEyePos(), HighJump.mc.player.getEyePos().add(HighJump.mc.player.getRotationVector(pitch, (float)i).multiply(1.5)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)HighJump.mc.player));
                    if (result.getType() != HitResult.Type.BLOCK) continue;
                    yaw = RotationMath.adjustAngle(HighJump.mc.player.getYaw(), i);
                }
            } else {
                pitch = 90.0f;
            }
            float finalYaw = yaw;
            HighJump.mc.interactionManager.sendSequencedPacket(HighJump.mc.world, sequence -> new PlayerInteractItemC2SPacket(hand, sequence, finalYaw, pitch));
            HighJump.mc.player.swingHand(hand);
            if (slot instanceof HotbarSlot) {
                InventoryUtility.selectHotbarSlot(oldHotbarSlotId);
            }
        }
    }
}

