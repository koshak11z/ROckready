/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.item.equipment.EquipmentType
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventIntegration;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.FireworkEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.combat.ElytraTarget;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.game.prediction.ElytraPredictionSystem;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.ArmorSlot;
import moscow.rockstar.utility.mixins.ArmorItemAddition;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Elytra Utils", category=ModuleCategory.PLAYER, desc="\u041f\u043e\u043c\u043e\u0449\u043d\u0438\u043a \u0441 \u044d\u043b\u0438\u0442\u0440\u0430\u043c\u0438")
public class ElytraUtils
extends BaseModule {
    private final BindSetting swapKey = new BindSetting(this, "\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0441\u0432\u0430\u043f\u0430");
    private final BindSetting fireworkKey = new BindSetting(this, "\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u0430");
    private final BooleanSetting automat = new BooleanSetting((SettingsContainer)this, "\u0410\u0432\u0442\u043e \u0432\u0437\u043b\u0451\u0442", "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0432\u0437\u043b\u0435\u0442\u0430\u0435\u0442 \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0430\u0445").enable();
    private final BooleanSetting withUse = new BooleanSetting(this, "\u0410\u0432\u0442\u043e \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435", "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u0442 \u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a \u0434\u043b\u044f \u0432\u0437\u043b\u0435\u0442\u0430", () -> !this.automat.isEnabled()).enable();
    private final BooleanSetting unEquip = new BooleanSetting((SettingsContainer)this, "\u0413\u0440\u0443\u0434\u0430\u043a \u043d\u0430 \u0437\u0435\u043c\u043b\u0435", "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043d\u0430\u0434\u0435\u0432\u0430\u0435\u0442 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a \u0438\u043b\u0438 \u0441\u043d\u0438\u043c\u0430\u0435\u0442 \u044d\u043b\u0438\u0442\u0440\u044b \u043f\u0440\u0438 \u043f\u0440\u0438\u0437\u0435\u043c\u043b\u0435\u043d\u0438\u0438").enable();
    private final BooleanSetting boost = new BooleanSetting((SettingsContainer)this, "\u0423\u0441\u043a\u043e\u0440\u044f\u0442\u044c", "\u0423\u0441\u043a\u043e\u0440\u044f\u0435\u0442 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u0435 \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0435");
    private final BooleanSetting syncGuiMove = new BooleanSetting(this, "Синхронизировать с GuiMove");
    private boolean wasFlying;
    private SwapTask swapTask;
    private final EventListener<ClientPlayerTickEvent> onUpdate = event -> {
        boolean isElytraEquipped;
        if (ElytraUtils.mc.player.isGliding()) {
            this.wasFlying = true;
        }
        ArmorSlot chestplateSlot = InventoryUtility.getChestplateSlot();
        SlotGroup<ItemSlot> group = SlotGroups.hotbar().and(SlotGroups.inventory()).and(SlotGroups.offhand());
        ItemSlot chestplateItemSlot = group.findItem(itemStack -> {
            ArmorItem armorItem;
            Item patt0$temp = itemStack.getItem();
            return patt0$temp instanceof ArmorItem && ((ArmorItemAddition)(armorItem = (ArmorItem)patt0$temp)).rockstar$getType() == EquipmentType.CHESTPLATE;
        });
        ItemSlot slot = group.findItem(Items.FIREWORK_ROCKET);
        boolean bl = isElytraEquipped = chestplateSlot.item() == Items.ELYTRA;
        if (this.swapTask != null) {
            switch (this.swapTask.stage) {
                case 0: 
                case 2: {
                    this.syncWithGuiMove();
                    InventoryUtility.hotbarSwap(this.swapTask.from.getIdForServer(), 40);
                    break;
                }
                case 1: {
                    this.syncWithGuiMove();
                    InventoryUtility.hotbarSwap(this.swapTask.chest.getIdForServer(), 40);
                }
            }
            if (this.swapTask.stage++ >= 2) {
                this.swapTask = null;
            }
        }
        if (this.automat.isEnabled() && isElytraEquipped && !ElytraUtils.mc.player.isGliding() && !ElytraUtils.mc.player.isOnGround() && !ElytraUtils.mc.player.isInFluid()) {
            ElytraUtils.mc.player.startGliding();
            mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraUtils.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            if (this.withUse.isEnabled() && slot != null) {
                this.syncWithGuiMove();
                EventIntegration.SWAP_INTEGRATION.useItem(Items.FIREWORK_ROCKET);
            }
        } else if (ElytraUtils.mc.player.isOnGround() && this.automat.isEnabled() && isElytraEquipped && !ElytraUtils.mc.player.isInFluid()) {
            ElytraUtils.mc.player.jump();
        }
        if (this.unEquip.isEnabled() && ElytraUtils.mc.player.isOnGround() && isElytraEquipped && this.wasFlying && ElytraUtils.mc.player.getGlidingTicks() > 18) {
            if (chestplateItemSlot != null) {
                this.syncWithGuiMove();
                chestplateItemSlot.swapTo(chestplateSlot);
            } else {
                ElytraUtils.mc.interactionManager.clickSlot(0, 6, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)ElytraUtils.mc.player);
            }
            this.wasFlying = false;
        }
    };
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (this.swapKey.isKey(event.getKey()) && event.getAction() == 1 && ElytraUtils.mc.currentScreen == null) {
            this.swapElytraChestplate();
        }
        if (this.fireworkKey.isKey(event.getKey()) && event.getAction() == 1 && ElytraUtils.mc.currentScreen == null) {
            this.syncWithGuiMove();
                EventIntegration.SWAP_INTEGRATION.useItem(Items.FIREWORK_ROCKET);
        }
    };
    private final EventListener<MouseEvent> onMouseButtonPress = event -> {
        if (this.swapKey.isKey(event.getButton()) && event.getAction() == 1 && ElytraUtils.mc.currentScreen == null) {
            this.swapElytraChestplate();
        }
        if (this.fireworkKey.isKey(event.getButton()) && event.getAction() == 1 && ElytraUtils.mc.currentScreen == null) {
            this.syncWithGuiMove();
                EventIntegration.SWAP_INTEGRATION.useItem(Items.FIREWORK_ROCKET);
        }
    };
    private final EventListener<FireworkEvent> onFirework = event -> {
        if (this.boost.isEnabled() && event.getEntity() == ElytraUtils.mc.player) {
            PlayerEntity player;
            LivingEntity target = Rockstar.getInstance().getTargetManager().getLivingTarget();
            if (!(target instanceof PlayerEntity) || !ElytraPredictionSystem.isLeaving(player = (PlayerEntity)target)) {
                // empty if block
            }
            double boostPower = 1.5 * this.getAdvancedBoost();
            RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
            Vec3d rotationVector = rotationHandler.isIdling() ? rotationHandler.getPlayerRotation().getRotationVector() : rotationHandler.getCurrentRotation().getRotationVector();
            Vec3d currentVelocity = event.getVelocity();
            Vec3d newVelocity = currentVelocity.add(rotationVector.x * 0.1 + (rotationVector.x * boostPower - currentVelocity.x) * 0.5, rotationVector.y * 0.1 + (rotationVector.y * boostPower - currentVelocity.y) * 0.5, rotationVector.z * 0.1 + (rotationVector.z * boostPower - currentVelocity.z) * 0.5);
            event.setVelocity(newVelocity);
        }
    };
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
            Rotation rot = rotationHandler.isIdling() ? rotationHandler.getPlayerRotation() : rotationHandler.getCurrentRotation();
            System.out.println(String.format("ELYTRA BOOSTER HUETA. ANGLES: yaw(%s) pitch(%s) speed(%s)", Float.valueOf(rot.getYaw()), Float.valueOf(rot.getPitch()), this.getAdvancedBoost()));
        }
    };

    private void syncWithGuiMove() {
        if (this.syncGuiMove.isEnabled()) {
            Rockstar.getInstance().getModuleManager().getModule(GuiMove.class).setStay(true);
        }
    }

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
            this.swapTask = new SwapTask(elytraItemSlot, chestplateSlot);
        } else if (chestplateItemSlot != null) {
            this.swapTask = new SwapTask(chestplateItemSlot, chestplateSlot);
        }
    }

    private double getAdvancedBoost() {
        double yawAcceleration;
        RotationHandler rotationHandler;
        if (Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isDefensiveActive()) {
            // empty if block
        }
        Rotation rot = (rotationHandler = Rockstar.getInstance().getRotationHandler()).isIdling() ? rotationHandler.getPlayerRotation() : rotationHandler.getCurrentRotation();
        float playerYaw = rot.getYaw();
        float playerPitch = rot.getPitch();
        double A = 0.239037;
        double B = 4.489648;
        double C = 1.236087;
        double MAX_ACCELERATION_YAW = 1.47;
        double YAW_TOLERANCE = 7.9;
        double MAX_PITCH_BOOST = 1.01;
        double MAX_PITCH = -45.0;
        double MIN_PITCH = 10.0;
        double effectiveYaw = (double)Math.abs(playerYaw) % 90.0;
        if (Math.abs(effectiveYaw - 45.0) <= 7.9) {
            yawAcceleration = 1.47;
        } else {
            double argument = 4.489648 * (effectiveYaw - 45.0);
            yawAcceleration = 0.239037 * Math.cos(Math.toRadians(argument)) + 1.236087;
        }
        if (playerPitch >= 10.0f) {
            return Math.abs(effectiveYaw - 45.0) <= 5.0 ? 1.8 : yawAcceleration;
        }
        if (playerPitch >= 0.0f) {
            return 1.0;
        }
        if (playerPitch < -80.0f) {
            return 1.0;
        }
        double pitchRatio = Math.min(1.0, (double)Math.abs(playerPitch) / Math.abs(-45.0));
        double pitchMultiplier = 1.0 + 0.010000000000000009 * pitchRatio;
        double totalAcceleration = yawAcceleration * pitchMultiplier;
        totalAcceleration = Math.min(totalAcceleration, 1.49);
        return totalAcceleration;
    }

    private static int findClosestVector(float lastYaw, int[] vectors) {
        int index = 0;
        int minDistIndex = -1;
        float minDist = Float.MAX_VALUE;
        for (int vector : vectors) {
            float dist = Math.abs(MathHelper.wrapDegrees((float)lastYaw) - (float)vector);
            if (dist < minDist) {
                minDist = dist;
                minDistIndex = index;
            }
            ++index;
        }
        return minDistIndex;
    }

    private double calculateDynamicBoostPower(LivingEntity player) {
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        double minSpeed = 1.4;
        double maxSpeed = 1.9;
        double yawFactor = this.calculateYawFactor(yaw);
        double pitchFactor = this.calculatePitchFactor(pitch);
        double combinedFactor = yawFactor * pitchFactor;
        double boostPower = minSpeed + (maxSpeed - minSpeed) * combinedFactor;
        return Math.max(minSpeed, Math.min(maxSpeed, boostPower));
    }

    private double calculateYawFactor(float yaw) {
        yaw = (yaw % 360.0f + 360.0f) % 360.0f;
        double[] diagonalAngles = new double[]{45.0, 135.0, 225.0, 315.0};
        double minDistanceToDiagonal = Double.MAX_VALUE;
        for (double diagonal : diagonalAngles) {
            double distance = Math.min(Math.abs((double)yaw - diagonal), Math.min(Math.abs((double)yaw - diagonal + 360.0), Math.abs((double)yaw - diagonal - 360.0)));
            minDistanceToDiagonal = Math.min(minDistanceToDiagonal, distance);
        }
        if (minDistanceToDiagonal <= 45.0) {
            return 1.0 - minDistanceToDiagonal / 45.0 * 0.85;
        }
        return 0.15;
    }

    private double calculatePitchFactor(float pitch) {
        float absPitch = Math.abs(pitch);
        if (absPitch <= 10.0f) {
            return 1.0;
        }
        if (absPitch <= 30.0f) {
            return 1.0 - (double)(absPitch - 10.0f) / 20.0 * 0.3;
        }
        if (absPitch <= 60.0f) {
            return 0.7 - (double)(absPitch - 30.0f) / 30.0 * 0.4;
        }
        return 0.3;
    }

    @Override
    public void onDisable() {
        this.wasFlying = false;
    }

    @Override
    public void onEnable() {
        this.wasFlying = false;
    }

    private static class SwapTask {
        int stage;
        final ItemSlot from;
        final ItemSlot chest;

        SwapTask(ItemSlot from, ItemSlot chest) {
            this.from = from;
            this.chest = chest;
        }
    }
}

