/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.block.entity.EnderChestBlockEntity
 *  net.minecraft.client.gui.screen.ingame.GenericContainerScreen
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.inventory.Inventory
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package moscow.rockstar.systems.modules.modules.player;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.WorldUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@ModuleInfo(name="Stealer", category=ModuleCategory.PLAYER)
public class Stealer
extends BaseModule {
    private final SliderSetting delay = new SliderSetting(this, "modules.settings.stealer.delay").min(0.0f).max(5.0f).step(0.1f).currentValue(0.4f);
    private final BooleanSetting close = new BooleanSetting((SettingsContainer)this, "modules.settings.stealer.close", "modules.settings.stealer.close.description");
    private final BooleanSetting off = new BooleanSetting((SettingsContainer)this, "modules.settings.stealer.off", "modules.settings.stealer.off.description");
    private final BooleanSetting openMystic = new BooleanSetting((SettingsContainer)this, "modules.settings.stealer.open_mystic", "modules.settings.stealer.open_mystic.description");
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.stealer.mode");
    private final ModeSetting.Value up = new ModeSetting.Value(this.mode, "modules.settings.stealer.mode.up").select();
    private final ModeSetting.Value down = new ModeSetting.Value(this.mode, "modules.settings.stealer.mode.down");
    private final ModeSetting.Value center = new ModeSetting.Value(this.mode, "modules.settings.stealer.mode.center");
    private final ModeSetting.Value random = new ModeSetting.Value(this.mode, "modules.settings.stealer.mode.random");
    private final Timer clickTimer = new Timer();
    private final Timer openTimer = new Timer();
    private final List<EnderChestBlockEntity> blackList = new ArrayList<EnderChestBlockEntity>();
    private EnderChestBlockEntity target;

    @Override
    public void tick() {
        ScreenHandler screenHandler;
        if (Stealer.mc.currentScreen instanceof GenericContainerScreen && (screenHandler = Stealer.mc.player.currentScreenHandler) instanceof GenericContainerScreenHandler) {
            int slot;
            GenericContainerScreenHandler handler = (GenericContainerScreenHandler)screenHandler;
            int size = handler.getInventory().size();
            if (this.mode.is(this.up)) {
                for (int i = 0; i < size && this.clickTimer.finished((long)(this.delay.getCurrentValue() * 1000.0f + MathUtility.random(-100.0, 100.0))); ++i) {
                    if (handler.getSlot(i).getStack().isEmpty()) continue;
                    Stealer.mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.mc.player);
                    this.clickTimer.reset();
                }
            } else if (this.mode.is(this.down)) {
                for (int i = size - 1; i >= 0 && this.clickTimer.finished((long)(this.delay.getCurrentValue() * 1000.0f + MathUtility.random(-100.0, 100.0))); --i) {
                    if (handler.getSlot(i).getStack().isEmpty()) continue;
                    Stealer.mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.mc.player);
                    this.clickTimer.reset();
                }
            } else if (this.mode.is(this.center)) {
                int centerIndex = size / 2;
                for (int i = 0; i <= centerIndex && this.clickTimer.finished((long)(this.delay.getCurrentValue() * 1000.0f + MathUtility.random(-100.0, 100.0))); ++i) {
                    int left = centerIndex - i;
                    int right = centerIndex + i;
                    if (left >= 0 && !handler.getSlot(left).getStack().isEmpty()) {
                        Stealer.mc.interactionManager.clickSlot(handler.syncId, left, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.mc.player);
                        this.clickTimer.reset();
                        continue;
                    }
                    if (right >= size || handler.getSlot(right).getStack().isEmpty()) continue;
                    Stealer.mc.interactionManager.clickSlot(handler.syncId, right, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.mc.player);
                    this.clickTimer.reset();
                }
            } else if (this.mode.is(this.random) && this.clickTimer.finished((long)(this.delay.getCurrentValue() * 1000.0f + MathUtility.random(-100.0, 100.0))) && !handler.getSlot(slot = (int)MathUtility.random(0.0, size)).getStack().isEmpty()) {
                Stealer.mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.mc.player);
                this.clickTimer.reset();
            }
            if (this.isEmpty(handler)) {
                if (this.target != null && this.openMystic.isEnabled()) {
                    this.blackList.add(this.target);
                }
                if (this.off.isEnabled()) {
                    this.toggle();
                }
                if (this.close.isEnabled()) {
                    Stealer.mc.player.closeHandledScreen();
                }
            }
            return;
        }
        if (!this.openMystic.isEnabled()) {
            return;
        }
        if (this.target == null || !this.isValidTarget(this.target)) {
            this.target = this.findTarget();
            this.openTimer.reset();
        }
        if (this.target != null && this.openTimer.finished(200L)) {
            BlockPos pos = this.target.getPos();
            Vec3d hitVec = Vec3d.ofCenter((Vec3i)pos);
            Rotation rotation = RotationMath.getRotationTo(hitVec);
            Rockstar.getInstance().getRotationHandler().rotate(rotation, MoveCorrection.NONE, 22.0f, 22.0f, 22.0f, RotationPriority.USE_ITEM);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos, false);
            Stealer.mc.interactionManager.interactBlock(Stealer.mc.player, Hand.MAIN_HAND, hit);
            Stealer.mc.player.swingHand(Hand.MAIN_HAND);
            this.openTimer.reset();
        }
    }

    private boolean isValidTarget(EnderChestBlockEntity entity) {
        return Stealer.mc.player.squaredDistanceTo(entity.getPos().toCenterPos()) < 16.0 && !this.blackList.contains(entity);
    }

    private EnderChestBlockEntity findTarget() {
        for (BlockEntity entity : WorldUtility.blockEntities) {
            EnderChestBlockEntity e;
            if (!(entity instanceof EnderChestBlockEntity) || !this.isValidTarget(e = (EnderChestBlockEntity)entity)) continue;
            return e;
        }
        return null;
    }

    private boolean isEmpty(GenericContainerScreenHandler handler) {
        Inventory inv = handler.getInventory();
        for (int i = 0; i < inv.size(); ++i) {
            if (inv.getStack(i).isEmpty()) continue;
            return false;
        }
        return true;
    }
}

