/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.SplashPotionItem
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package moscow.rockstar.systems.modules.modules.combat;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.player.GuiMove;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.PotionUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@ModuleInfo(name="Auto Potion", category=ModuleCategory.COMBAT, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0431\u0440\u043e\u0441\u0430\u0435\u0442 \u0437\u0435\u043b\u044c\u044f")
public class AutoPotion
extends BaseModule {
    private final SelectSetting potions = new SelectSetting(this, "modules.settings.auto_potion.potions");
    private final SelectSetting.Value health;
    private final SelectSetting.Value cerka;
    private SliderSetting healthHealth = null;
    private SliderSetting cerkaHealth = null;
    private final SelectSetting allow = new SelectSetting(this, "modules.settings.auto_potion.allow");
    private final SelectSetting.Value up = new SelectSetting.Value(this.allow, "modules.settings.auto_potion.allow.throw_up").select();
    private final SelectSetting.Value walls = new SelectSetting.Value(this.allow, "modules.settings.auto_potion.allow.throw_walls").select();
    private final SelectSetting.Value don = new SelectSetting.Value(this.allow, "modules.settings.auto_potion.allow.donate");
    private final Timer staying = new Timer();
    private final Timer ground = new Timer();
    private final Timer wall = new Timer();
    private final Timer roof = new Timer();
    private UseTask task;
    private final EventListener<InputEvent> onInput = event -> {
        if (this.task == null) {
            return;
        }
        GuiMove guiMove = Rockstar.getInstance().getModuleManager().getModule(GuiMove.class);
        boolean hotbar = this.task.slot instanceof HotbarSlot;
        if (guiMove.isEnabled() && guiMove.slowing() && !hotbar) {
            event.setForward(0.0f);
            event.setStrafe(0.0f);
        }
    };
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        Rotation rotation = this.getRotation();
        GuiMove guiMove = Rockstar.getInstance().getModuleManager().getModule(GuiMove.class);
        if (this.task != null) {
            int offset;
            Rockstar.getInstance().getRotationHandler().rotate(rotation, RotationPriority.USE_ITEM);
            boolean hotbar = this.task.slot instanceof HotbarSlot;
            int n = offset = guiMove.isEnabled() && guiMove.slowing() && !hotbar ? 1 : 0;
            if (hotbar) {
                switch (this.task.stage) {
                    case 0: {
                        InventoryUtility.selectHotbarSlot(this.task.slot.getIdForServer() - 36);
                        break;
                    }
                    case 1: {
                        this.usePotion();
                        break;
                    }
                    case 2: {
                        InventoryUtility.selectHotbarSlot(this.task.prevSlot);
                    }
                }
            } else {
                switch (this.task.stage - offset) {
                    case 0: {
                        InventoryUtility.hotbarSwap(this.task.slot.getIdForServer(), this.task.prevSlot);
                        InventoryUtility.selectHotbarSlot(this.task.prevSlot);
                        break;
                    }
                    case 1: {
                        this.usePotion();
                        break;
                    }
                    case 2: {
                        InventoryUtility.hotbarSwap(this.task.slot.getIdForServer(), this.task.prevSlot);
                    }
                }
            }
            if (this.task.stage >= 2 + offset) {
                this.task = null;
            } else {
                ++this.task.stage;
                return;
            }
        }
        if (rotation.getYaw() == -1.0f || rotation.getPitch() == -1.0f) {
            return;
        }
        for (SelectSetting.Value selectedValue : this.potions.getSelectedValues()) {
            SlotGroup<ItemSlot> search;
            ItemSlot slot;
            PotionValue potionValue = (PotionValue)selectedValue;
            if (AutoPotion.mc.player.hasStatusEffect(potionValue.effect) || !potionValue.throwTimer.finished(2000L) || !potionValue.canThrow() || (slot = (search = SlotGroups.inventory().and(SlotGroups.hotbar())).findItem(this.potionPredicate(potionValue.effect))) == null) continue;
            this.task = new UseTask(slot, InventoryUtility.getCurrentHotbarSlot().getSlotId());
            Rockstar.getInstance().getRotationHandler().rotate(rotation, MoveCorrection.SILENT, 180.0f, 180.0f, 180.0f, RotationPriority.USE_ITEM);
            potionValue.throwTimer.reset();
            break;
        }
    };

    public AutoPotion() {
        new PotionValue(this.potions, "modules.settings.auto_potion.potions.strength", (RegistryEntry<StatusEffect>)StatusEffects.STRENGTH).select();
        new PotionValue(this.potions, "modules.settings.auto_potion.potions.speed", (RegistryEntry<StatusEffect>)StatusEffects.SPEED).select();
        new PotionValue(this.potions, "modules.settings.auto_potion.potions.fire_resistance", (RegistryEntry<StatusEffect>)StatusEffects.FIRE_RESISTANCE).select();
        this.health = new PotionValue(this.potions, "modules.settings.auto_potion.potions.heal", (RegistryEntry<StatusEffect>)StatusEffects.INSTANT_HEALTH, () -> AutoPotion.mc.player.getHealth() <= this.healthHealth.getCurrentValue()).select();
        this.cerka = new PotionValue(this.potions, "modules.settings.auto_potion.potions.heal.cerka", (RegistryEntry<StatusEffect>)StatusEffects.WEAKNESS, () -> {
            SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
            ItemSlot health = slotsToSearch.findItem(stack -> PotionUtility.hasEffect(stack, (RegistryEntry<StatusEffect>)StatusEffects.INSTANT_HEALTH) && !AutoPotion.mc.player.getItemCooldownManager().isCoolingDown(stack));
            ItemSlot golden = slotsToSearch.findItem(stack -> stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE && !AutoPotion.mc.player.getItemCooldownManager().isCoolingDown(stack));
            boolean near = false;
            for (AbstractClientPlayerEntity player : AutoPotion.mc.world.getPlayers()) {
                if (!(AutoPotion.mc.player.distanceTo((Entity)player) < 5.0f) || player == AutoPotion.mc.player) continue;
                near = true;
            }
            return AutoPotion.mc.player.getHealth() + AutoPotion.mc.player.getAbsorptionAmount() <= this.cerkaHealth.getCurrentValue() && golden == null && health == null && near;
        }).select();
        this.healthHealth = new SliderSetting((SettingsContainer)this, "modules.settings.auto_potion.potions.heal_health", () -> !this.health.isSelected()).min(1.0f).max(19.0f).step(0.5f).currentValue(6.0f);
        this.cerkaHealth = new SliderSetting((SettingsContainer)this, "modules.settings.auto_potion.potions.cerka_health", () -> !this.cerka.isSelected()).min(1.0f).max(19.0f).step(0.5f).currentValue(6.0f);
    }

    private void usePotion() {
        Rotation rotation = this.getRotation();
        AutoPotion.mc.interactionManager.sendSequencedPacket(AutoPotion.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, rotation.getYaw(), rotation.getPitch()));
    }

    private Rotation getRotation() {
        boolean canThrow = false;
        float yaw = AutoPotion.mc.player.getYaw();
        float pitch = 90.0f;
        if (this.cerka.isSelected() && ((PotionValue)this.cerka).canThrow()) {
            for (AbstractClientPlayerEntity player : AutoPotion.mc.world.getPlayers()) {
                if (!(AutoPotion.mc.player.distanceTo((Entity)player) < 5.0f) || player == AutoPotion.mc.player) continue;
                Vec3d eyes = AutoPotion.mc.player.getCameraPosVec(1.0f);
                double dx = player.getPos().x - eyes.x;
                double dy = player.getPos().y - eyes.y;
                double dz = player.getPos().z - eyes.z;
                double dist = Math.sqrt(dx * dx + dz * dz);
                return new Rotation((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0), (float)(-Math.toDegrees(Math.atan2(dy, dist))));
            }
        }
        if (this.up.isSelected()) {
            if (AutoPotion.mc.player.getVelocity().x == 0.0 && AutoPotion.mc.player.getVelocity().z == 0.0 && Math.abs(AutoPotion.mc.player.getVelocity().y) < (double)0.1f && EntityUtility.getBlockAbove((Entity)AutoPotion.mc.player) == Blocks.AIR) {
                if (this.staying.finished(500L)) {
                    pitch = -90.0f;
                    return new Rotation(yaw, pitch);
                }
            } else {
                this.staying.reset();
            }
        }
        if (AutoPotion.mc.player.isOnGround()) {
            if (this.ground.finished(300L)) {
                BlockHitResult groundResult = AutoPotion.mc.world.raycast(new RaycastContext(AutoPotion.mc.player.getEyePos(), AutoPotion.mc.player.getEyePos().add(AutoPotion.mc.player.getRotationVector(pitch, yaw).multiply(2.0)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)AutoPotion.mc.player));
                if (groundResult.getType() == HitResult.Type.BLOCK) {
                    canThrow = true;
                } else {
                    pitch = 76.0f;
                    for (int i = 0; i < 360; i += 45) {
                        BlockHitResult result = AutoPotion.mc.world.raycast(new RaycastContext(AutoPotion.mc.player.getEyePos(), AutoPotion.mc.player.getEyePos().add(AutoPotion.mc.player.getRotationVector(pitch, (float)i).multiply(2.0)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)AutoPotion.mc.player));
                        if (result.getType() != HitResult.Type.BLOCK) continue;
                        yaw = RotationMath.adjustAngle(AutoPotion.mc.player.getYaw(), i);
                        canThrow = true;
                    }
                }
            }
        } else {
            this.ground.reset();
            if (this.walls.isSelected()) {
                BlockHitResult result;
                boolean wallThrow = false;
                pitch = 5.0f;
                for (int i = 0; i < 360; i += 45) {
                    result = AutoPotion.mc.world.raycast(new RaycastContext(AutoPotion.mc.player.getEyePos(), AutoPotion.mc.player.getEyePos().add(AutoPotion.mc.player.getRotationVector(pitch, (float)i).multiply(0.5)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)AutoPotion.mc.player));
                    if (result.getType() != HitResult.Type.BLOCK) continue;
                    yaw = RotationMath.adjustAngle(AutoPotion.mc.player.getYaw(), i);
                    wallThrow = true;
                }
                if (!wallThrow) {
                    this.wall.reset();
                } else if (this.wall.finished(300L)) {
                    canThrow = true;
                }
                if (!canThrow) {
                    boolean roofThrow = false;
                    pitch = -90.0f;
                    result = AutoPotion.mc.world.raycast(new RaycastContext(AutoPotion.mc.player.getEyePos(), AutoPotion.mc.player.getEyePos().add(AutoPotion.mc.player.getRotationVector(pitch, AutoPotion.mc.player.getYaw()).multiply(0.5)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)AutoPotion.mc.player));
                    if (result.getType() == HitResult.Type.BLOCK) {
                        yaw = AutoPotion.mc.player.getYaw();
                        roofThrow = true;
                    }
                    if (!roofThrow) {
                        this.roof.reset();
                    } else if (this.roof.finished(300L)) {
                        canThrow = true;
                    }
                }
            }
        }
        return canThrow ? new Rotation(yaw, pitch) : new Rotation(-1.0f, -1.0f);
    }

    private Predicate<ItemStack> potionPredicate(RegistryEntry<StatusEffect> type) {
        return stack -> {
            if (stack.isEmpty() || !(stack.getItem() instanceof SplashPotionItem)) {
                return false;
            }
            List<StatusEffectInstance> effects = PotionUtility.effects(stack);
            return this.don.isSelected() || type == StatusEffects.WEAKNESS ? effects.stream().anyMatch(effect -> effect.getEffectType() == type) : effects.size() == 1 && effects.getFirst().getEffectType() == type;
        };
    }

    static class PotionValue
    extends SelectSetting.Value {
        final RegistryEntry<StatusEffect> effect;
        final Timer throwTimer = new Timer();
        Supplier<Boolean> canThrow = () -> true;

        public PotionValue(SelectSetting parent, String name, RegistryEntry<StatusEffect> effect) {
            super(parent, name);
            this.effect = effect;
        }

        public PotionValue(SelectSetting parent, String name, RegistryEntry<StatusEffect> effect, Supplier<Boolean> canThrow) {
            super(parent, name);
            this.effect = effect;
            this.canThrow = canThrow;
        }

        public boolean canThrow() {
            return this.canThrow.get();
        }
    }

    static class UseTask {
        int stage;
        final ItemSlot slot;
        final int prevSlot;

        @Generated
        public UseTask(ItemSlot slot, int prevSlot) {
            this.slot = slot;
            this.prevSlot = prevSlot;
        }
    }
}

