package im.zov4ik.features.impl.player;

import im.zov4ik.events.player.RotationUpdateEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.client.managers.event.types.EventType;
import im.zov4ik.utils.features.aura.utils.MathAngle;
import im.zov4ik.utils.features.aura.warp.Turns;
import im.zov4ik.utils.features.aura.warp.TurnsConfig;
import im.zov4ik.utils.features.aura.warp.TurnsConnection;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.interactions.inv.InventoryTask;
import im.zov4ik.utils.math.task.TaskPriority;
import im.zov4ik.utils.math.time.StopWatch;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * AppleFarm — автоматический фарм яблок с авто-починкой мотыги.
 *
 * Цикл: посадка → рост (костная мука / озеленитель) → вырубка ствола → листва → повтор.
 * Починка: при прочности ≤ порога + Mending → пузырьки опыта из всего инвентаря.
 * Поиск предметов: весь инвентарь (хотбар + основной), с авто-перемещением в хотбар.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppleFarm extends Module {

    // ========================== SETTINGS ==========================

    final SelectSetting  breakMode       = new SelectSetting("Break Mode", "Режим ломания блоков").value("Packet", "Legit");
    final BooleanSetting silentRotations = new BooleanSetting("Silent Rotations", "Серверные ротации без визуала").setValue(true);
    final BooleanSetting autoCollect     = new BooleanSetting("Auto Collect Drops", "Автоматический сбор дропа").setValue(true);
    final BooleanSetting useGreener      = new BooleanSetting("Озеленитель", "Мотыга ПКМ по саженцу вместо костной муки").setValue(false);
    final BooleanSetting autoRepair      = new BooleanSetting("Auto Repair", "Авто-починка мотыги пузырьками опыта").setValue(true);
    final SliderSettings repairAt        = new SliderSettings("Repair At %", "Порог прочности для починки").setValue(10).range(5, 50);
    final SliderSettings breakDelay      = new SliderSettings("Break Delay", "Задержка между ломанием (мс, Packet)").setValue(50).range(0, 200);
    final SliderSettings placeDelay      = new SliderSettings("Place Delay", "Задержка между размещением/использованием (мс)").setValue(50).range(0, 200);
    final SliderSettings searchRadius    = new SliderSettings("Search Radius", "Радиус поиска блоков").setValue(5).range(3, 8);

    // ========================== STATE =============================

    enum FarmState { IDLE, PLANT, GROW, CHOP, CLEAR_LEAVES, REPAIR }

    FarmState farmState = FarmState.IDLE;
    BlockPos dirtPos;

    // Двутиковая схема
    BlockPos lastRotatedTarget;
    boolean canAct;

    // Legit breaking
    BlockPos currentLegitTarget;

    // Слот-менеджмент
    int ourLastSlot = -1;
    int slotSwapCooldown;

    // Перемещение из инвентаря в хотбар
    int inventoryMoveCooldown;

    // Починка
    enum RepairPhase { NONE, SETUP, THROWING, CLEANUP }
    RepairPhase repairPhase = RepairPhase.NONE;
    int repairSetupTicks;
    final StopWatch repairThrowTimer = new StopWatch();

    final StopWatch actionTimer = new StopWatch();

    // ========================== CONSTRUCTOR =======================

    public AppleFarm() {
        super("AppleFarm", "Apple Farm", ModuleCategory.PLAYER);
        setup(breakMode, silentRotations, autoCollect, useGreener, autoRepair, repairAt,
              breakDelay, placeDelay, searchRadius);
    }

    // ========================== LIFECYCLE =========================

    @Override
    public void activate() {
        if (PlayerInteractionHelper.nullCheck()) { setState(false); return; }

        String missing = checkInventory();
        if (missing != null) {
            chatMessage(Formatting.RED, "Не хватает: " + missing);
            setState(false);
            return;
        }

        dirtPos = findDirtInFront();
        if (dirtPos == null) {
            chatMessage(Formatting.RED, "Не найден блок земли (радиус " + searchRadius.getInt() + ").");
            setState(false);
            return;
        }

        farmState = FarmState.IDLE;
        lastRotatedTarget = null;
        canAct = false;
        currentLegitTarget = null;
        slotSwapCooldown = 0;
        inventoryMoveCooldown = 0;
        ourLastSlot = mc.player.getInventory().selectedSlot;
        repairPhase = RepairPhase.NONE;
        actionTimer.reset();

        String mode = useGreener.isValue() ? "Озеленитель" : "Костная мука";
        chatMessage(Formatting.GREEN, "Запущен (" + breakMode.getSelected() + ", " + mode + "). Блок: "
                + dirtPos.getX() + ", " + dirtPos.getY() + ", " + dirtPos.getZ());
    }

    @Override
    public void deactivate() {
        if (repairPhase == RepairPhase.THROWING || repairPhase == RepairPhase.CLEANUP) {
            cleanupRepair();
        }
        farmState = FarmState.IDLE;
        repairPhase = RepairPhase.NONE;
        lastRotatedTarget = null;
        canAct = false;
        currentLegitTarget = null;
        TurnsConnection.INSTANCE.clear();
        chatMessage(Formatting.GRAY, "Остановлен.");
    }

    // ========================== MAIN EVENT ========================

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (PlayerInteractionHelper.nullCheck() || dirtPos == null) return;

        if (e.getType() == EventType.PRE) {
            handlePre();
        } else if (e.getType() == EventType.POST) {
            handlePost();
        }
    }

    // ========================== PRE ==============================

    private void handlePre() {
        if (slotSwapCooldown > 0) slotSwapCooldown--;
        if (inventoryMoveCooldown > 0) inventoryMoveCooldown--;

        resolveState();

        // --- REPAIR: отдельный цикл ---
        if (farmState == FarmState.REPAIR) {
            handleRepairPre();
            return;
        }

        // --- FARMING: двутиковая схема ---
        canAct = false;

        BlockPos target = getActionTarget();
        if (target == null) return;

        // Предмет в основном инвентаре? Подтянуть в хотбар.
        int rawSlot = getNeededSlotRaw();
        if (rawSlot >= 9 && rawSlot <= 35) {
            if (inventoryMoveCooldown > 0) return;
            moveToHotbar(rawSlot);
            return;
        }

        Direction face = getActionFace(target);
        Vec3d hitVec = faceHitVec(target, face);
        rotateTo(hitVec);

        if (target.equals(lastRotatedTarget)) {
            canAct = true;
        } else {
            if (currentLegitTarget != null && !target.equals(currentLegitTarget)) {
                mc.interactionManager.cancelBlockBreaking();
                currentLegitTarget = null;
            }
        }
        lastRotatedTarget = target;
    }

    // ========================== POST =============================

    private void handlePost() {
        if (farmState == FarmState.REPAIR) {
            handleRepairPost();
            return;
        }

        if (!canAct) return;

        int neededSlot = getNeededSlotHotbar();
        if (neededSlot == -1) return;

        if (neededSlot != ourLastSlot) {
            if (slotSwapCooldown > 0) return;
            selectSlot(neededSlot);
            ourLastSlot = neededSlot;
            slotSwapCooldown = 3;
        }

        switch (farmState) {
            case CHOP         -> doChop();
            case CLEAR_LEAVES -> doClearLeaves();
            case PLANT        -> doPlant();
            case GROW         -> doGrow();
            default -> {}
        }
    }

    // =================== REPAIR MECHANIC =========================

    private void handleRepairPre() {
        switch (repairPhase) {
            case SETUP -> {
                if (repairSetupTicks == 0) {
                    int hoeSlot = findHoeHotbarSlot();
                    if (hoeSlot == -1) {
                        chatMessage(Formatting.RED, "Мотыга не в хотбаре!");
                        repairPhase = RepairPhase.NONE;
                        return;
                    }
                    selectSlot(hoeSlot);
                    ourLastSlot = hoeSlot;

                    int xpSlot = findXpBottleSlot();
                    if (xpSlot == -1) {
                        chatMessage(Formatting.RED, "Пузырьки опыта не найдены!");
                        repairPhase = RepairPhase.NONE;
                        return;
                    }
                    InventoryTask.swapToOffhand(xpSlot);

                    chatMessage(Formatting.AQUA, "Починка мотыги...");
                }

                rotateDown();
                repairSetupTicks++;

                if (repairSetupTicks >= 4) {
                    repairPhase = RepairPhase.THROWING;
                    repairThrowTimer.reset();
                }
            }
            case THROWING -> {
                rotateDown();
            }
            case CLEANUP -> {
                cleanupRepair();
                repairPhase = RepairPhase.NONE;
                lastRotatedTarget = null;
                chatMessage(Formatting.GREEN, "Починка завершена!");
            }
            default -> {}
        }
    }

    private void handleRepairPost() {
        if (repairPhase != RepairPhase.THROWING) return;

        ItemStack hoeStack = mc.player.getMainHandStack();
        if (hoeStack.getItem() instanceof HoeItem && hoeStack.isDamageable()) {
            if (hoeStack.getDamage() <= 0) {
                repairPhase = RepairPhase.CLEANUP;
                return;
            }
            double remaining = 1.0 - (double) hoeStack.getDamage() / hoeStack.getMaxDamage();
            if (remaining >= 0.99) {
                repairPhase = RepairPhase.CLEANUP;
                return;
            }
        }

        ItemStack offhand = mc.player.getOffHandStack();
        if (!offhand.isOf(Items.EXPERIENCE_BOTTLE) || offhand.isEmpty()) {
            int xpSlot = findXpBottleSlot();
            if (xpSlot == -1) {
                chatMessage(Formatting.YELLOW, "Пузырьки закончились!");
                repairPhase = RepairPhase.CLEANUP;
                return;
            }
            InventoryTask.swapToOffhand(xpSlot);
            repairThrowTimer.reset();
            return;
        }

        if (!repairThrowTimer.finished(350)) return;

        PlayerInteractionHelper.interactItem(Hand.OFF_HAND);
        repairThrowTimer.reset();
    }

    private void cleanupRepair() {
        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.isOf(Items.EXPERIENCE_BOTTLE) && !offhand.isEmpty()) {
            int freeSlot = findFreeHotbarSlot();
            if (freeSlot == -1) freeSlot = 8;
            InventoryTask.clickSlot(45, freeSlot, SlotActionType.SWAP, false);
        }
    }

    private void rotateDown() {
        Vec3d downPoint = mc.player.getPos().add(0, -5, 0);
        Turns angle = MathAngle.calculateAngle(downPoint);
        Turns adjusted = angle.adjustSensitivity();

        mc.player.setYaw(adjusted.getYaw());
        mc.player.setPitch(adjusted.getPitch());
        TurnsConnection.INSTANCE.rotateTo(adjusted, TurnsConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    private boolean shouldRepairHoe() {
        if (!autoRepair.isValue()) return false;

        int hoeSlot = findHoeHotbarSlot();
        if (hoeSlot == -1) return false;

        ItemStack hoe = mc.player.getInventory().getStack(hoeSlot);
        if (!hoe.isDamageable() || hoe.getMaxDamage() <= 0) return false;

        double remaining = 1.0 - (double) hoe.getDamage() / hoe.getMaxDamage();
        if (remaining > repairAt.getValue() / 100.0) return false;

        if (!hasMending(hoe)) return false;

        return findXpBottleSlot() != -1;
    }

    private boolean hasMending(ItemStack stack) {
        var enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) return false;

        for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
            String id = entry.getIdAsString();
            if (id != null && id.toLowerCase().contains("mending")) {
                return true;
            }
        }
        return false;
    }

    // =================== STATE MACHINE ===========================

    private void resolveState() {
        if (repairPhase != RepairPhase.NONE) {
            farmState = FarmState.REPAIR;
            return;
        }

        if (shouldRepairHoe()) {
            repairPhase = RepairPhase.SETUP;
            repairSetupTicks = 0;
            farmState = FarmState.REPAIR;
            return;
        }

        BlockState aboveDirt = mc.world.getBlockState(dirtPos.up());

        if (aboveDirt.isIn(BlockTags.SAPLINGS)) {
            farmState = FarmState.GROW;
        } else if (aboveDirt.isIn(BlockTags.LOGS)) {
            farmState = FarmState.CHOP;
        } else if (aboveDirt.isIn(BlockTags.LEAVES)) {
            farmState = FarmState.CLEAR_LEAVES;
        } else if (aboveDirt.isAir()) {
            if (hasTreeBlocksAbove()) {
                farmState = FarmState.CHOP;
            } else if (hasLeavesNearby()) {
                farmState = FarmState.CLEAR_LEAVES;
            } else {
                farmState = FarmState.PLANT;
            }
        }
    }

    private void transitionAfterBreak() {
        currentLegitTarget = null;
        lastRotatedTarget = null;

        if (farmState == FarmState.CHOP) {
            farmState = hasLeavesNearby() ? FarmState.CLEAR_LEAVES : FarmState.PLANT;
        } else {
            farmState = FarmState.PLANT;
        }
    }

    // =================== ACTION TARGET ===========================

    private BlockPos getActionTarget() {
        return switch (farmState) {
            case CHOP -> {
                List<BlockPos> logs = findLogsAbove();
                if (logs.isEmpty()) { transitionAfterBreak(); yield null; }
                yield logs.get(0);
            }
            case CLEAR_LEAVES -> {
                List<BlockPos> leaves = findLeavesNearby(searchRadius.getInt());
                if (leaves.isEmpty()) { transitionAfterBreak(); yield null; }
                yield leaves.get(0);
            }
            case PLANT -> dirtPos;
            case GROW  -> dirtPos.up();
            default -> null;
        };
    }

    private Direction getActionFace(BlockPos target) {
        if (farmState == FarmState.PLANT) return Direction.UP;
        return getClosestFace(target);
    }

    private Vec3d faceHitVec(BlockPos pos, Direction face) {
        return Vec3d.ofCenter(pos).add(
                face.getOffsetX() * 0.5D,
                face.getOffsetY() * 0.5D,
                face.getOffsetZ() * 0.5D
        );
    }

    // =================== ACTIONS =================================

    private void doChop() {
        List<BlockPos> logs = findLogsAbove();
        if (logs.isEmpty()) { transitionAfterBreak(); return; }

        BlockPos target = logs.get(0);
        Direction face = getClosestFace(target);

        if (isPacketMode()) {
            if (!actionTimer.finished(breakDelay.getValue())) return;
            breakBlockPacket(target, face);
            actionTimer.reset();
        } else {
            breakBlockLegit(target, face);
        }
    }

    private void doClearLeaves() {
        List<BlockPos> leaves = findLeavesNearby(searchRadius.getInt());
        if (leaves.isEmpty()) { transitionAfterBreak(); return; }

        BlockPos target = leaves.get(0);
        Direction face = getClosestFace(target);

        if (isPacketMode()) {
            if (!actionTimer.finished(breakDelay.getValue())) return;
            breakBlockPacket(target, face);
            actionTimer.reset();
        } else {
            breakBlockLegit(target, face);
        }
    }

    private void doPlant() {
        if (!actionTimer.finished(placeDelay.getValue())) return;
        Direction face = Direction.UP;
        Vec3d hitVec = faceHitVec(dirtPos, face);
        interactBlock(dirtPos, face, hitVec);
        actionTimer.reset();
    }

    private void doGrow() {
        if (!actionTimer.finished(placeDelay.getValue())) return;
        BlockPos saplingPos = dirtPos.up();
        Direction face = getClosestFace(saplingPos);
        Vec3d hitVec = faceHitVec(saplingPos, face);
        interactBlock(saplingPos, face, hitVec);
        actionTimer.reset();
    }

    // ======================== BREAK ==============================

    private boolean isPacketMode() {
        return breakMode.isSelected("Packet");
    }

    private void breakBlockPacket(BlockPos pos, Direction face) {
        mc.player.networkHandler.sendPacket(
                new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face));
        mc.player.networkHandler.sendPacket(
                new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face));
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void breakBlockLegit(BlockPos pos, Direction face) {
        if (mc.world.getBlockState(pos).isAir()) {
            currentLegitTarget = null;
            return;
        }
        currentLegitTarget = pos;
        mc.interactionManager.updateBlockBreakingProgress(pos, face);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    // ======================== INTERACT ============================

    private void interactBlock(BlockPos pos, Direction face, Vec3d hitVec) {
        BlockHitResult hit = new BlockHitResult(hitVec, face, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    // ======================== SLOT (AncientBot pattern) ===========

    private void selectSlot(int slot) {
        if (slot < 0 || slot > 8 || mc.player == null) return;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.syncSelectedSlot();
    }

    // ======================== INVENTORY ==========================

    /**
     * Поиск по ВСЕМУ инвентарю (0-35). Приоритет хотбару.
     */
    private int findSlot(java.util.function.Predicate<ItemStack> predicate) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && predicate.test(stack)) return i;
        }
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && predicate.test(stack)) return i;
        }
        return -1;
    }

    private int findHotbarSlot(java.util.function.Predicate<ItemStack> predicate) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && predicate.test(stack)) return i;
        }
        return -1;
    }

    /**
     * «Сырой» слот (0-35). Озеленитель: в GROW ищет мотыгу вместо костной муки.
     */
    private int getNeededSlotRaw() {
        return switch (farmState) {
            case CHOP         -> findSlot(s -> s.getItem() instanceof AxeItem);
            case CLEAR_LEAVES -> findSlot(s -> s.getItem() instanceof HoeItem);
            case PLANT        -> findSlot(s -> {
                Block b = Block.getBlockFromItem(s.getItem());
                return b != null && b.getDefaultState().isIn(BlockTags.SAPLINGS);
            });
            case GROW -> useGreener.isValue()
                    ? findSlot(s -> s.getItem() instanceof HoeItem)
                    : findSlot(s -> s.isOf(Items.BONE_MEAL));
            default -> -1;
        };
    }

    /**
     * Хотбар-слот (0-8). Озеленитель: в GROW ищет мотыгу.
     */
    private int getNeededSlotHotbar() {
        return switch (farmState) {
            case CHOP         -> findHotbarSlot(s -> s.getItem() instanceof AxeItem);
            case CLEAR_LEAVES -> findHotbarSlot(s -> s.getItem() instanceof HoeItem);
            case PLANT        -> findHotbarSlot(s -> {
                Block b = Block.getBlockFromItem(s.getItem());
                return b != null && b.getDefaultState().isIn(BlockTags.SAPLINGS);
            });
            case GROW -> useGreener.isValue()
                    ? findHotbarSlot(s -> s.getItem() instanceof HoeItem)
                    : findHotbarSlot(s -> s.isOf(Items.BONE_MEAL));
            default -> -1;
        };
    }

    /**
     * Перемещение из основного инвентаря (9-35) в хотбар.
     */
    private void moveToHotbar(int invSlot) {
        int targetHotbar = findFreeHotbarSlot();
        if (targetHotbar == -1) targetHotbar = 8;
        InventoryTask.clickSlot(invSlot, targetHotbar, SlotActionType.SWAP, false);
        inventoryMoveCooldown = 3;
    }

    private int findFreeHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) return i;
        }
        return -1;
    }

    // ======================== SPECIFIC FINDERS ====================

    private int findHoeHotbarSlot() {
        return findHotbarSlot(s -> s.getItem() instanceof HoeItem);
    }

    private int findXpBottleSlot() {
        return findSlot(s -> s.isOf(Items.EXPERIENCE_BOTTLE));
    }

    // ======================== INVENTORY CHECK =====================

    private String checkInventory() {
        if (findSlot(s -> s.getItem() instanceof HoeItem) == -1) return "Мотыга (Hoe)";
        if (!useGreener.isValue() && findSlot(s -> s.isOf(Items.BONE_MEAL)) == -1) return "Костная мука (Bone Meal)";
        if (findSlot(s -> s.getItem() instanceof AxeItem) == -1) return "Топор (Axe)";
        if (findSlot(s -> {
            Block b = Block.getBlockFromItem(s.getItem());
            return b != null && b.getDefaultState().isIn(BlockTags.SAPLINGS);
        }) == -1) return "Саженец (Sapling)";
        return null;
    }

    // ======================== GEOMETRY ============================

    private Direction getClosestFace(BlockPos pos) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d blockCenter = Vec3d.ofCenter(pos);

        double dx = eyePos.x - blockCenter.x;
        double dy = eyePos.y - blockCenter.y;
        double dz = eyePos.z - blockCenter.z;

        double ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);

        if (ay >= ax && ay >= az) return dy > 0 ? Direction.UP : Direction.DOWN;
        if (ax >= az) return dx > 0 ? Direction.EAST : Direction.WEST;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private void rotateTo(Vec3d point) {
        Turns angle = MathAngle.calculateAngle(point);
        Turns adjusted = angle.adjustSensitivity();

        if (!silentRotations.isValue()) {
            mc.player.setYaw(adjusted.getYaw());
            mc.player.setPitch(adjusted.getPitch());
        }
        TurnsConnection.INSTANCE.rotateTo(adjusted, TurnsConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    // =================== WORLD SCANNING ==========================

    private BlockPos findDirtInFront() {
        int radius = searchRadius.getInt();
        Vec3d look = mc.player.getRotationVector();

        double hLen = Math.sqrt(look.x * look.x + look.z * look.z);
        double nx = hLen > 0.001 ? look.x / hLen : 0;
        double nz = hLen > 0.001 ? look.z / hLen : 0;

        for (int d = 1; d <= radius; d++) {
            BlockPos check = BlockPos.ofFloored(
                    mc.player.getX() + nx * d,
                    mc.player.getY() - 1,
                    mc.player.getZ() + nz * d);
            if (isDirt(mc.world.getBlockState(check).getBlock())) return check;
        }

        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++)
                for (int y = -2; y <= 0; y++) {
                    BlockPos check = playerPos.add(x, y, z);
                    if (isDirt(mc.world.getBlockState(check).getBlock())) {
                        double dist = check.getSquaredDistance(playerPos);
                        if (dist < closestDist) { closestDist = dist; closest = check; }
                    }
                }
        return closest;
    }

    private boolean isDirt(Block b) {
        return b == Blocks.DIRT || b == Blocks.GRASS_BLOCK
            || b == Blocks.ROOTED_DIRT || b == Blocks.COARSE_DIRT;
    }

    private List<BlockPos> findLogsAbove() {
        List<BlockPos> logs = new ArrayList<>();

        for (int y = 1; y <= 30; y++) {
            BlockPos check = dirtPos.up(y);
            if (mc.world.getBlockState(check).isIn(BlockTags.LOGS)) logs.add(check);
        }

        if (!logs.isEmpty()) {
            Set<BlockPos> visited = new HashSet<>(logs);
            Queue<BlockPos> queue = new LinkedList<>(logs);
            while (!queue.isEmpty()) {
                BlockPos cur = queue.poll();
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = cur.offset(dir);
                    if (!visited.contains(neighbor)
                            && neighbor.getY() > dirtPos.getY()
                            && neighbor.getSquaredDistance(dirtPos) < 100
                            && mc.world.getBlockState(neighbor).isIn(BlockTags.LOGS)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                        logs.add(neighbor);
                    }
                }
            }
        }

        logs.sort(Comparator.comparingInt(BlockPos::getY));
        return logs;
    }

    private List<BlockPos> findLeavesNearby(int radius) {
        List<BlockPos> leaves = new ArrayList<>();
        for (int x = -radius; x <= radius; x++)
            for (int y = 0; y <= 20; y++)
                for (int z = -radius; z <= radius; z++) {
                    BlockPos check = dirtPos.add(x, y + 1, z);
                    if (mc.world.getBlockState(check).isIn(BlockTags.LEAVES)) leaves.add(check);
                }

        final Vec3d pp = mc.player.getPos();
        leaves.sort(Comparator.comparingDouble(p -> p.getSquaredDistance(pp.x, pp.y, pp.z)));
        return leaves;
    }

    private boolean hasTreeBlocksAbove() {
        for (int y = 2; y <= 30; y++)
            if (mc.world.getBlockState(dirtPos.up(y)).isIn(BlockTags.LOGS)) return true;
        return false;
    }

    private boolean hasLeavesNearby() {
        return !findLeavesNearby(searchRadius.getInt()).isEmpty();
    }

    // =================== HELPERS =================================

    private void chatMessage(Formatting color, String msg) {
        if (mc.player != null)
            mc.player.sendMessage(
                    Text.literal("[AppleFarm] ").formatted(Formatting.GOLD)
                        .append(Text.literal(msg).formatted(color)),
                    false);
    }
}
