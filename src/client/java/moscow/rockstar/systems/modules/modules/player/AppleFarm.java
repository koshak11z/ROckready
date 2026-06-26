package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Predicate;

@ModuleInfo(name = "AppleFarm", category = ModuleCategory.PLAYER, desc = "AppleFarm full state-machine port")
public class AppleFarm extends BaseModule {
    private final ModeSetting breakMode = new ModeSetting(this, "Break Mode");
    private final ModeSetting.Value packet = new ModeSetting.Value(this.breakMode, "Packet").select();
    private final ModeSetting.Value legit = new ModeSetting.Value(this.breakMode, "Legit");
    private final ModeSetting.Value megaLegit = new ModeSetting.Value(this.breakMode, "Mega Legit");
    private final BooleanSetting silentRotations = new BooleanSetting(this, "Silent Rotations").enable();
    private final BooleanSetting autoCollect = new BooleanSetting(this, "Auto Collect Drops").enable();
    private final BooleanSetting autoStoreApples = new BooleanSetting(this, "Авто слаживание яблок");
    private final SliderSetting storeApplesEvery = new SliderSetting(this, "Слаживать каждые").min(1.0f).max(2304.0f).step(1.0f).currentValue(100.0f).suffix(" ябл.");
    private final BooleanSetting useGreener = new BooleanSetting(this, "Озеленитель");
    private final BooleanSetting autoRepair = new BooleanSetting(this, "Auto Repair").enable();
    private final SliderSetting repairAt = new SliderSetting(this, "Repair At %").min(5.0f).max(50.0f).step(1.0f).currentValue(10.0f);
    private final SliderSetting breakDelay = new SliderSetting(this, "Break Delay").min(0.0f).max(200.0f).step(5.0f).currentValue(50.0f).suffix(" ms");
    private final SliderSetting placeDelay = new SliderSetting(this, "Place Delay").min(0.0f).max(200.0f).step(5.0f).currentValue(50.0f).suffix(" ms");
    private final SliderSetting searchRadius = new SliderSetting(this, "Search Radius").min(3.0f).max(8.0f).step(1.0f).currentValue(5.0f);

    private FarmState farmState = FarmState.IDLE;
    private BlockPos dirtPos;
    private BlockPos lastRotatedTarget;
    private boolean canAct;
    private BlockPos currentLegitTarget;
    private int ourLastSlot = -1;
    private int slotSwapCooldown;
    private int inventoryMoveCooldown;
    private boolean storingApples;
    private boolean storageCommandSent;
    private int storageTicks;
    private int storageMoveCooldown;
    private int storageRetryCooldown;
    private RepairPhase repairPhase = RepairPhase.NONE;
    private int repairSetupTicks;
    private final Timer actionTimer = new Timer();
    private final Timer repairThrowTimer = new Timer();

    private enum FarmState { IDLE, PLANT, GROW, CHOP, CLEAR_LEAVES, REPAIR }
    private enum RepairPhase { NONE, SETUP, THROWING, CLEANUP }

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || this.dirtPos == null) return;
        if (this.slotSwapCooldown > 0) this.slotSwapCooldown--;
        if (this.inventoryMoveCooldown > 0) this.inventoryMoveCooldown--;
        if (this.storageRetryCooldown > 0) this.storageRetryCooldown--;
        if (this.storageMoveCooldown > 0) this.storageMoveCooldown--;
        if (this.handleAutoStoreApples()) return;
        this.resolveState();
        this.handlePre();
        this.handlePost();
    };

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) { this.disable(); return; }
        String missing = this.checkInventory();
        if (missing != null) {
            MessageUtility.error(Text.of("§c[AppleFarm] Не хватает: " + missing));
            this.disable();
            return;
        }
        this.dirtPos = this.findDirtInFront();
        if (this.dirtPos == null) {
            MessageUtility.error(Text.of("§c[AppleFarm] Не найден блок земли (радиус " + (int)this.searchRadius.getCurrentValue() + ")"));
            this.disable();
            return;
        }
        this.farmState = FarmState.IDLE;
        this.lastRotatedTarget = null;
        this.currentLegitTarget = null;
        this.canAct = false;
        this.slotSwapCooldown = 0;
        this.inventoryMoveCooldown = 0;
        this.storingApples = false;
        this.storageCommandSent = false;
        this.storageTicks = 0;
        this.storageMoveCooldown = 0;
        this.storageRetryCooldown = 0;
        this.ourLastSlot = mc.player.getInventory().selectedSlot;
        this.repairPhase = RepairPhase.NONE;
        this.actionTimer.reset();
        MessageUtility.info(Text.of("§a[AppleFarm] Запущен: " + this.getBreakModeName() + ", dirt=" + this.dirtPos.getX() + "," + this.dirtPos.getY() + "," + this.dirtPos.getZ()));
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (this.repairPhase == RepairPhase.THROWING || this.repairPhase == RepairPhase.CLEANUP) this.cleanupRepair();
        this.farmState = FarmState.IDLE;
        this.repairPhase = RepairPhase.NONE;
        this.lastRotatedTarget = null;
        this.currentLegitTarget = null;
        this.storingApples = false;
        this.storageCommandSent = false;
        super.onDisable();
    }

    private void handlePre() {
        if (this.farmState == FarmState.REPAIR) {
            this.handleRepairPre();
            return;
        }
        this.canAct = false;
        BlockPos target = this.getActionTarget();
        if (target == null) return;
        int rawSlot = this.getNeededSlotRaw();
        if (rawSlot >= 9 && rawSlot <= 35) {
            if (this.inventoryMoveCooldown <= 0) this.moveToHotbar(rawSlot);
            return;
        }
        Direction face = this.getActionFace(target);
        Vec3d hitVec = this.faceHitVec(target, face);
        boolean rotationReady = this.rotateTo(hitVec);
        if (target.equals(this.lastRotatedTarget) && rotationReady) this.canAct = true;
        else if (this.currentLegitTarget != null && !target.equals(this.currentLegitTarget)) {
            mc.interactionManager.cancelBlockBreaking();
            this.currentLegitTarget = null;
        }
        this.lastRotatedTarget = target;
    }

    private void handlePost() {
        if (this.farmState == FarmState.REPAIR) {
            this.handleRepairPost();
            return;
        }
        if (!this.canAct) return;
        int neededSlot = this.getNeededSlotHotbar();
        if (neededSlot == -1) return;
        if (neededSlot != this.ourLastSlot) {
            if (this.slotSwapCooldown > 0) return;
            this.selectSlot(neededSlot);
            this.ourLastSlot = neededSlot;
            this.slotSwapCooldown = 3;
        }
        switch (this.farmState) {
            case CHOP -> this.doChop();
            case CLEAR_LEAVES -> this.doClearLeaves();
            case PLANT -> this.doPlant();
            case GROW -> this.doGrow();
        }
    }

    private void handleRepairPre() {
        switch (this.repairPhase) {
            case SETUP -> {
                if (this.repairSetupTicks == 0) {
                    int hoeSlot = this.findHoeHotbarSlot();
                    if (hoeSlot == -1) { this.repairPhase = RepairPhase.NONE; return; }
                    this.selectSlot(hoeSlot);
                    this.ourLastSlot = hoeSlot;
                    int xpSlot = this.findXpBottleSlot();
                    if (xpSlot == -1) { this.repairPhase = RepairPhase.NONE; return; }
                    this.swapToOffhand(xpSlot);
                    MessageUtility.info(Text.of("§b[AppleFarm] Починка мотыги..."));
                }
                this.rotateDown();
                this.repairSetupTicks++;
                if (this.repairSetupTicks >= 4) { this.repairPhase = RepairPhase.THROWING; this.repairThrowTimer.reset(); }
            }
            case THROWING -> this.rotateDown();
            case CLEANUP -> {
                this.cleanupRepair();
                this.repairPhase = RepairPhase.NONE;
                this.lastRotatedTarget = null;
                MessageUtility.info(Text.of("§a[AppleFarm] Починка завершена"));
            }
        }
    }

    private void handleRepairPost() {
        if (this.repairPhase != RepairPhase.THROWING) return;
        ItemStack hoeStack = mc.player.getMainHandStack();
        if (hoeStack.getItem() instanceof HoeItem && hoeStack.isDamageable()) {
            double remaining = 1.0 - (double)hoeStack.getDamage() / (double)hoeStack.getMaxDamage();
            if (hoeStack.getDamage() <= 0 || remaining >= 0.99) { this.repairPhase = RepairPhase.CLEANUP; return; }
        }
        ItemStack offhand = mc.player.getOffHandStack();
        if (!offhand.isOf(Items.EXPERIENCE_BOTTLE) || offhand.isEmpty()) {
            int xpSlot = this.findXpBottleSlot();
            if (xpSlot == -1) { this.repairPhase = RepairPhase.CLEANUP; return; }
            this.swapToOffhand(xpSlot);
            this.repairThrowTimer.reset();
            return;
        }
        if (!this.repairThrowTimer.finished(350L)) return;
        mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
        this.repairThrowTimer.reset();
    }

    private void cleanupRepair() {
        if (!mc.player.getOffHandStack().isOf(Items.EXPERIENCE_BOTTLE)) return;
        int freeSlot = this.findFreeHotbarSlot();
        if (freeSlot == -1) freeSlot = 8;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, freeSlot, SlotActionType.SWAP, mc.player);
    }

    private void resolveState() {
        if (this.repairPhase != RepairPhase.NONE) { this.farmState = FarmState.REPAIR; return; }
        if (this.shouldRepairHoe()) { this.repairPhase = RepairPhase.SETUP; this.repairSetupTicks = 0; this.farmState = FarmState.REPAIR; return; }
        BlockState aboveDirt = mc.world.getBlockState(this.dirtPos.up());
        if (aboveDirt.isIn(BlockTags.SAPLINGS)) this.farmState = FarmState.GROW;
        else if (aboveDirt.isIn(BlockTags.LOGS)) this.farmState = FarmState.CHOP;
        else if (aboveDirt.isIn(BlockTags.LEAVES)) this.farmState = FarmState.CLEAR_LEAVES;
        else if (aboveDirt.isAir()) {
            if (this.hasTreeBlocksAbove()) this.farmState = FarmState.CHOP;
            else if (this.hasLeavesNearby()) this.farmState = FarmState.CLEAR_LEAVES;
            else this.farmState = FarmState.PLANT;
        }
    }

    private void transitionAfterBreak() {
        this.currentLegitTarget = null;
        this.lastRotatedTarget = null;
        this.farmState = this.farmState == FarmState.CHOP && this.hasLeavesNearby() ? FarmState.CLEAR_LEAVES : FarmState.PLANT;
    }

    private BlockPos getActionTarget() {
        return switch (this.farmState) {
            case CHOP -> { List<BlockPos> logs = this.findLogsAbove(); if (logs.isEmpty()) { this.transitionAfterBreak(); yield null; } yield logs.get(0); }
            case CLEAR_LEAVES -> { List<BlockPos> leaves = this.findLeavesNearby((int)this.searchRadius.getCurrentValue()); if (leaves.isEmpty()) { this.transitionAfterBreak(); yield null; } yield leaves.get(0); }
            case PLANT -> this.dirtPos;
            case GROW -> this.dirtPos.up();
            default -> null;
        };
    }

    private Direction getActionFace(BlockPos target) { return this.farmState == FarmState.PLANT ? Direction.UP : this.getClosestFace(target); }
    private Vec3d faceHitVec(BlockPos pos, Direction face) { return Vec3d.ofCenter(pos).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5); }

    private void doChop() { List<BlockPos> logs = this.findLogsAbove(); if (logs.isEmpty()) { this.transitionAfterBreak(); return; } this.breakTarget(logs.get(0)); }
    private void doClearLeaves() { List<BlockPos> leaves = this.findLeavesNearby((int)this.searchRadius.getCurrentValue()); if (leaves.isEmpty()) { this.transitionAfterBreak(); return; } this.breakTarget(leaves.get(0)); }
    private void breakTarget(BlockPos target) { Direction face = this.getClosestFace(target); if (this.packet.isSelected()) { if (!this.actionTimer.finished((long)this.breakDelay.getCurrentValue())) return; this.breakBlockPacket(target, face); this.actionTimer.reset(); } else this.breakBlockLegit(target, face); }
    private void doPlant() { if (!this.actionTimer.finished((long)this.placeDelay.getCurrentValue())) return; this.interactBlock(this.dirtPos, Direction.UP, this.faceHitVec(this.dirtPos, Direction.UP)); this.actionTimer.reset(); }
    private void doGrow() { if (!this.actionTimer.finished((long)this.placeDelay.getCurrentValue())) return; BlockPos saplingPos = this.dirtPos.up(); Direction face = this.getClosestFace(saplingPos); this.interactBlock(saplingPos, face, this.faceHitVec(saplingPos, face)); this.actionTimer.reset(); }

    private void breakBlockPacket(BlockPos pos, Direction face) { mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face)); mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face)); mc.player.swingHand(Hand.MAIN_HAND); }
    private void breakBlockLegit(BlockPos pos, Direction face) { if (mc.world.getBlockState(pos).isAir()) { this.currentLegitTarget = null; return; } this.currentLegitTarget = pos; mc.interactionManager.updateBlockBreakingProgress(pos, face); mc.player.swingHand(Hand.MAIN_HAND); }
    private void interactBlock(BlockPos pos, Direction face, Vec3d hitVec) { mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(hitVec, face, pos, false)); mc.player.swingHand(Hand.MAIN_HAND); }
    private void selectSlot(int slot) { if (slot < 0 || slot > 8) return; mc.player.getInventory().selectedSlot = slot; mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot)); }

    private int findSlot(Predicate<ItemStack> predicate) { for (int i = 0; i < 9; i++) if (!mc.player.getInventory().getStack(i).isEmpty() && predicate.test(mc.player.getInventory().getStack(i))) return i; for (int i = 9; i < 36; i++) if (!mc.player.getInventory().getStack(i).isEmpty() && predicate.test(mc.player.getInventory().getStack(i))) return i; return -1; }
    private int findHotbarSlot(Predicate<ItemStack> predicate) { for (int i = 0; i < 9; i++) if (!mc.player.getInventory().getStack(i).isEmpty() && predicate.test(mc.player.getInventory().getStack(i))) return i; return -1; }
    private int getNeededSlotRaw() { return switch (this.farmState) { case CHOP -> this.findSlot(s -> s.getItem() instanceof AxeItem); case CLEAR_LEAVES -> this.findSlot(s -> s.getItem() instanceof HoeItem); case PLANT -> this.findSlot(s -> { Block b = Block.getBlockFromItem(s.getItem()); return b != null && b.getDefaultState().isIn(BlockTags.SAPLINGS); }); case GROW -> this.useGreener.isEnabled() ? this.findSlot(s -> s.getItem() instanceof HoeItem) : this.findSlot(s -> s.isOf(Items.BONE_MEAL)); default -> -1; }; }
    private int getNeededSlotHotbar() { return switch (this.farmState) { case CHOP -> this.findHotbarSlot(s -> s.getItem() instanceof AxeItem); case CLEAR_LEAVES -> this.findHotbarSlot(s -> s.getItem() instanceof HoeItem); case PLANT -> this.findHotbarSlot(s -> { Block b = Block.getBlockFromItem(s.getItem()); return b != null && b.getDefaultState().isIn(BlockTags.SAPLINGS); }); case GROW -> this.useGreener.isEnabled() ? this.findHotbarSlot(s -> s.getItem() instanceof HoeItem) : this.findHotbarSlot(s -> s.isOf(Items.BONE_MEAL)); default -> -1; }; }
    private void moveToHotbar(int invSlot) { int targetHotbar = this.findFreeHotbarSlot(); if (targetHotbar == -1) targetHotbar = 8; int serverSlot = invSlot < 9 ? invSlot + 36 : invSlot; mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, serverSlot, targetHotbar, SlotActionType.SWAP, mc.player); this.inventoryMoveCooldown = 3; }
    private int findFreeHotbarSlot() { for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).isEmpty()) return i; return -1; }
    private int findHoeHotbarSlot() { return this.findHotbarSlot(s -> s.getItem() instanceof HoeItem); }
    private int findXpBottleSlot() { return this.findSlot(s -> s.isOf(Items.EXPERIENCE_BOTTLE)); }
    private void swapToOffhand(int rawSlot) { int serverSlot = rawSlot < 9 ? rawSlot + 36 : rawSlot; mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, serverSlot, 40, SlotActionType.SWAP, mc.player); }

    private boolean handleAutoStoreApples() {
        if (!this.autoStoreApples.isEnabled()) return false;
        if (!this.storingApples) {
            if (this.storageRetryCooldown > 0) return false;
            int threshold = Math.max(1, (int)this.storeApplesEvery.getCurrentValue());
            if (this.countApples() < threshold) return false;
            this.storingApples = true;
            this.storageCommandSent = false;
            this.storageTicks = 0;
            this.storageMoveCooldown = 0;
            this.currentLegitTarget = null;
            this.lastRotatedTarget = null;
            mc.interactionManager.cancelBlockBreaking();
            MessageUtility.info(Text.of("§b[AppleFarm] Складываю яблоки в /clan storage..."));
        }
        this.handleStorageDeposit();
        return true;
    }

    private void handleStorageDeposit() {
        if (!this.storageCommandSent) {
            mc.player.networkHandler.sendChatCommand("clan storage");
            this.storageCommandSent = true;
            this.storageTicks = 0;
            return;
        }
        this.storageTicks++;
        if (this.storageTicks > 100) {
            this.finishStorageDeposit("§c[AppleFarm] Не удалось открыть /clan storage");
            this.storageRetryCooldown = 100;
            return;
        }
        int containerSlots = this.getContainerSlotCount();
        if (containerSlots <= 0) return;
        if (this.storageMoveCooldown > 0) return;
        if (this.findFreeContainerSlot(containerSlots) == -1) {
            this.finishStorageDeposit("§e[AppleFarm] В clan storage нет свободных слотов");
            this.storageRetryCooldown = 100;
            return;
        }
        int appleSlot = this.findAppleScreenSlot(containerSlots);
        if (appleSlot == -1) {
            this.finishStorageDeposit("§a[AppleFarm] Яблоки сложены");
            this.storageRetryCooldown = 20;
            return;
        }
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, appleSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
        this.storageMoveCooldown = 2;
    }

    private void finishStorageDeposit(String message) {
        if (this.getContainerSlotCount() > 0) mc.player.closeHandledScreen();
        this.storingApples = false;
        this.storageCommandSent = false;
        this.storageTicks = 0;
        this.storageMoveCooldown = 0;
        MessageUtility.info(Text.of(message));
    }

    private int countApples() {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.APPLE)) count += stack.getCount();
        }
        return count;
    }

    private int getContainerSlotCount() {
        if (mc.player.currentScreenHandler instanceof PlayerScreenHandler) return 0;
        int total = mc.player.currentScreenHandler.slots.size();
        int containerSlots = total - 36;
        return containerSlots > 0 ? containerSlots : 0;
    }

    private int findFreeContainerSlot(int containerSlots) {
        for (int i = 0; i < containerSlots; i++) {
            Slot slot = mc.player.currentScreenHandler.slots.get(i);
            if (slot.inventory != mc.player.getInventory() && !slot.hasStack()) return i;
        }
        return -1;
    }

    private int findAppleScreenSlot(int containerSlots) {
        for (int i = containerSlots; i < mc.player.currentScreenHandler.slots.size(); i++) {
            Slot slot = mc.player.currentScreenHandler.slots.get(i);
            if (slot.inventory == mc.player.getInventory() && slot.hasStack() && slot.getStack().isOf(Items.APPLE)) return i;
        }
        return -1;
    }

    private String checkInventory() { if (this.findSlot(s -> s.getItem() instanceof HoeItem) == -1) return "Мотыга"; if (!this.useGreener.isEnabled() && this.findSlot(s -> s.isOf(Items.BONE_MEAL)) == -1) return "Костная мука"; if (this.findSlot(s -> s.getItem() instanceof AxeItem) == -1) return "Топор"; if (this.findSlot(s -> { Block b = Block.getBlockFromItem(s.getItem()); return b != null && b.getDefaultState().isIn(BlockTags.SAPLINGS); }) == -1) return "Саженец"; return null; }
    private Direction getClosestFace(BlockPos pos) { Vec3d eyePos = mc.player.getEyePos(); Vec3d center = Vec3d.ofCenter(pos); double dx = eyePos.x - center.x, dy = eyePos.y - center.y, dz = eyePos.z - center.z; double ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz); if (ay >= ax && ay >= az) return dy > 0 ? Direction.UP : Direction.DOWN; if (ax >= az) return dx > 0 ? Direction.EAST : Direction.WEST; return dz > 0 ? Direction.SOUTH : Direction.NORTH; }
    private boolean rotateTo(Vec3d point) {
        var rot = RotationMath.getRotationTo(point);
        if (this.silentRotations.isEnabled()) {
            float yawSpeed = this.megaLegit.isSelected() ? 120.0f : 180.0f;
            float pitchSpeed = this.megaLegit.isSelected() ? 120.0f : 180.0f;
            float accel = this.megaLegit.isSelected() ? 45.0f : 70.0f;
            Rockstar.getInstance().getRotationHandler().rotate(rot, MoveCorrection.SILENT, yawSpeed, pitchSpeed, accel, RotationPriority.TO_TARGET);
            return true;
        }
        if (this.megaLegit.isSelected()) {
            mc.player.setYaw(this.stepAngle(mc.player.getYaw(), rot.getYaw(), 12.0f));
            mc.player.setPitch(this.stepAngle(mc.player.getPitch(), rot.getPitch(), 10.0f));
            return this.isRotationClose(rot.getYaw(), rot.getPitch(), 4.0f);
        }
        mc.player.setYaw(rot.getYaw());
        mc.player.setPitch(rot.getPitch());
        return true;
    }
    private void rotateDown() { this.rotateTo(mc.player.getPos().add(0.0, -5.0, 0.0)); }
    private float stepAngle(float from, float to, float maxStep) { float delta = MathHelper.wrapDegrees(to - from); if (delta > maxStep) delta = maxStep; if (delta < -maxStep) delta = -maxStep; return from + delta; }
    private boolean isRotationClose(float yaw, float pitch, float tolerance) { return Math.abs(MathHelper.wrapDegrees(yaw - mc.player.getYaw())) <= tolerance && Math.abs(pitch - mc.player.getPitch()) <= tolerance; }
    private String getBreakModeName() { return this.packet.isSelected() ? "Packet" : this.megaLegit.isSelected() ? "Mega Legit" : "Legit"; }

    private boolean shouldRepairHoe() { if (!this.autoRepair.isEnabled()) return false; int slot = this.findHoeHotbarSlot(); if (slot == -1) return false; ItemStack hoe = mc.player.getInventory().getStack(slot); if (!hoe.isDamageable() || hoe.getMaxDamage() <= 0) return false; double remaining = 1.0 - (double)hoe.getDamage() / (double)hoe.getMaxDamage(); return remaining <= this.repairAt.getCurrentValue() / 100.0 && this.hasMending(hoe) && this.findXpBottleSlot() != -1; }
    private boolean hasMending(ItemStack stack) { return EnchantmentUtility.hasEnchantments(stack, Enchantments.MENDING); }
    private BlockPos findDirtInFront() { int radius = (int)this.searchRadius.getCurrentValue(); Vec3d look = mc.player.getRotationVector(); double hLen = Math.sqrt(look.x * look.x + look.z * look.z); double nx = hLen > 0.001 ? look.x / hLen : 0.0, nz = hLen > 0.001 ? look.z / hLen : 0.0; for (int d = 1; d <= radius; d++) { BlockPos check = BlockPos.ofFloored(mc.player.getX() + nx * d, mc.player.getY() - 1.0, mc.player.getZ() + nz * d); if (this.isDirt(mc.world.getBlockState(check).getBlock())) return check; } BlockPos base = mc.player.getBlockPos(), best = null; double bestDist = Double.MAX_VALUE; for (int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) for(int y=-2;y<=0;y++){ BlockPos check=base.add(x,y,z); if(this.isDirt(mc.world.getBlockState(check).getBlock())){ double dist=check.getSquaredDistance(base); if(dist<bestDist){bestDist=dist;best=check;}}} return best; }
    private boolean isDirt(Block b) { return b == Blocks.DIRT || b == Blocks.GRASS_BLOCK || b == Blocks.ROOTED_DIRT || b == Blocks.COARSE_DIRT; }
    private List<BlockPos> findLogsAbove() { List<BlockPos> logs = new ArrayList<>(); for (int y=1;y<=30;y++){ BlockPos p=this.dirtPos.up(y); if(mc.world.getBlockState(p).isIn(BlockTags.LOGS)) logs.add(p); } Set<BlockPos> visited=new HashSet<>(logs); Queue<BlockPos> queue=new LinkedList<>(logs); while(!queue.isEmpty()){ BlockPos cur=queue.poll(); for(Direction dir:Direction.values()){ BlockPos n=cur.offset(dir); if(!visited.contains(n)&&n.getY()>this.dirtPos.getY()&&n.getSquaredDistance(this.dirtPos)<100&&mc.world.getBlockState(n).isIn(BlockTags.LOGS)){visited.add(n);queue.add(n);logs.add(n);}}} logs.sort(Comparator.comparingInt(BlockPos::getY)); return logs; }
    private List<BlockPos> findLeavesNearby(int radius) { List<BlockPos> leaves = new ArrayList<>(); for(int x=-radius;x<=radius;x++) for(int y=0;y<=20;y++) for(int z=-radius;z<=radius;z++){ BlockPos p=this.dirtPos.add(x,y+1,z); if(mc.world.getBlockState(p).isIn(BlockTags.LEAVES)) leaves.add(p); } Vec3d pp=mc.player.getPos(); leaves.sort(Comparator.comparingDouble(p -> p.getSquaredDistance(pp.x, pp.y, pp.z))); return leaves; }
    private boolean hasTreeBlocksAbove() { for(int y=2;y<=30;y++) if(mc.world.getBlockState(this.dirtPos.up(y)).isIn(BlockTags.LOGS)) return true; return false; }
    private boolean hasLeavesNearby() { return !this.findLeavesNearby((int)this.searchRadius.getCurrentValue()).isEmpty(); }
}
