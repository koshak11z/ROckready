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
import im.zov4ik.utils.math.task.TaskPriority;
import im.zov4ik.utils.math.time.StopWatch;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * AppleFarm — автоматический фарм яблок и древесины.
 *
 * Цикл: посадка саженца → костная мука → вырубка ствола → уничтожение листвы → повтор.
 *
 * Grim bypass:
 *   1. Двутиковая схема — ротация на тике N, действие на тике N+1
 *   2. hitVec на поверхности грани (паттерн AncientBot)
 *   3. adjustSensitivity() — углы привязаны к GCD мыши (AimModulo360)
 *   4. selectSlot + syncSelectedSlot (паттерн AncientBot, без дублей HELD_ITEM_CHANGE)
 *   5. Слот переключается на тике ротации — сервер знает до действия
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppleFarm extends Module {

    // ========================== SETTINGS ==========================

    final SelectSetting  breakMode       = new SelectSetting("Break Mode", "Режим ломания блоков").value("Packet", "Legit");
    final BooleanSetting silentRotations = new BooleanSetting("Silent Rotations", "Серверные ротации без визуала").setValue(true);
    final BooleanSetting autoCollect     = new BooleanSetting("Auto Collect Drops", "Автоматический сбор дропа").setValue(true);
    final SliderSettings breakDelay      = new SliderSettings("Break Delay", "Задержка между ломанием (мс, Packet)").setValue(50).range(0, 200);
    final SliderSettings placeDelay      = new SliderSettings("Place Delay", "Задержка между размещением/использованием (мс)").setValue(50).range(0, 200);
    final SliderSettings searchRadius    = new SliderSettings("Search Radius", "Радиус поиска блоков").setValue(5).range(3, 8);

    // ========================== STATE =============================

    enum FarmState { IDLE, PLANT, GROW, CHOP, CLEAR_LEAVES }

    FarmState farmState = FarmState.IDLE;
    BlockPos dirtPos;

    /**
     * Двутиковая схема:
     * lastRotatedTarget — к чему ротировались на ПРОШЛОМ тике.
     * Flying-пакет с этой ротацией уже ушёл серверу.
     * Если текущая цель == lastRotatedTarget → canAct = true.
     */
    BlockPos lastRotatedTarget;
    boolean canAct;

    /** Legit: текущий блок для cancelBlockBreaking. */
    BlockPos currentLegitTarget;

    final StopWatch actionTimer = new StopWatch();

    // ========================== CONSTRUCTOR =======================

    public AppleFarm() {
        super("AppleFarm", "Apple Farm", ModuleCategory.PLAYER);
        setup(breakMode, silentRotations, autoCollect, breakDelay, placeDelay, searchRadius);
    }

    // ========================== LIFECYCLE =========================

    @Override
    public void activate() {
        if (PlayerInteractionHelper.nullCheck()) {
            setState(false);
            return;
        }

        String missing = checkInventory();
        if (missing != null) {
            chatMessage(Formatting.RED, "Не хватает: " + missing);
            setState(false);
            return;
        }

        dirtPos = findDirtInFront();
        if (dirtPos == null) {
            chatMessage(Formatting.RED, "Не найден блок земли перед игроком (радиус " + searchRadius.getInt() + ").");
            setState(false);
            return;
        }

        farmState = FarmState.IDLE;
        lastRotatedTarget = null;
        canAct = false;
        currentLegitTarget = null;
        actionTimer.reset();

        chatMessage(Formatting.GREEN, "Запущен (" + breakMode.getSelected() + "). Целевой блок: "
                + dirtPos.getX() + ", " + dirtPos.getY() + ", " + dirtPos.getZ());
    }

    @Override
    public void deactivate() {
        farmState = FarmState.IDLE;
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

    /**
     * PRE: определяем фазу, цель, ротируемся, переключаем слот.
     * Слот переключается ДО flying-пакета — сервер узнает в этом же тике.
     */
    private void handlePre() {
        resolveState();
        canAct = false;

        BlockPos target = getActionTarget();
        if (target == null) return;

        // Грань и hitVec на поверхности грани (AncientBot pattern)
        Direction face = getActionFace(target);
        Vec3d hitVec = faceHitVec(target, face);

        // Ротация к hitVec + GCD snap (AimModulo360 bypass)
        rotateTo(hitVec);

        // Слот переключаем на тике ротации — уходит в пакете ДО flying,
        // сервер знает о слоте к моменту действия на следующем тике.
        // Используем AncientBot pattern: selectedSlot + syncSelectedSlot
        // (а не InventoryTask.switchTo который дублирует HELD_ITEM_CHANGE)
        int neededSlot = getNeededSlot();
        if (neededSlot != -1) {
            selectSlot(neededSlot);
        }

        // Двутиковая проверка
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

    /**
     * POST: если canAct — выполняем действие (ломание / interact).
     * Ротация и слот уже известны серверу из предыдущего тика.
     */
    private void handlePost() {
        if (!canAct) return;

        switch (farmState) {
            case CHOP         -> doChop();
            case CLEAR_LEAVES -> doClearLeaves();
            case PLANT        -> doPlant();
            case GROW         -> doGrow();
            default -> {}
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

    /**
     * PLANT: всегда UP (ставим саженец на верх земли).
     * Остальные: ближайшая к глазам игрока.
     */
    private Direction getActionFace(BlockPos target) {
        if (farmState == FarmState.PLANT) return Direction.UP;
        return getClosestFace(target);
    }

    /**
     * hitVec на поверхности грани (AncientBot pattern).
     * Grim валидирует что hitVec лежит на указанной face.
     */
    private Vec3d faceHitVec(BlockPos pos, Direction face) {
        return Vec3d.ofCenter(pos).add(
                face.getOffsetX() * 0.5D,
                face.getOffsetY() * 0.5D,
                face.getOffsetZ() * 0.5D
        );
    }

    /**
     * Слот инструмента для текущей фазы. -1 = не переключать.
     */
    private int getNeededSlot() {
        return switch (farmState) {
            case CHOP         -> findAxeSlot();
            case CLEAR_LEAVES -> findHoeSlot();
            case PLANT        -> findSaplingSlot();
            case GROW         -> findBoneMealSlot();
            default -> -1;
        };
    }

    // =================== ACTIONS =================================

    private void doChop() {
        List<BlockPos> logs = findLogsAbove();
        if (logs.isEmpty()) { transitionAfterBreak(); return; }

        if (findAxeSlot() == -1) { chatMessage(Formatting.RED, "Топор не найден!"); setState(false); return; }

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

        if (findHoeSlot() == -1) { chatMessage(Formatting.RED, "Мотыга не найдена!"); setState(false); return; }

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

        if (findSaplingSlot() == -1) { chatMessage(Formatting.RED, "Саженцы закончились!"); setState(false); return; }

        Direction face = Direction.UP;
        Vec3d hitVec = faceHitVec(dirtPos, face);
        interactBlock(dirtPos, face, hitVec);
        actionTimer.reset();
    }

    private void doGrow() {
        if (!actionTimer.finished(placeDelay.getValue())) return;

        if (findBoneMealSlot() == -1) { chatMessage(Formatting.RED, "Костная мука закончилась!"); setState(false); return; }

        BlockPos saplingPos = dirtPos.up();
        Direction face = getClosestFace(saplingPos);
        Vec3d hitVec = faceHitVec(saplingPos, face);
        interactBlock(saplingPos, face, hitVec);
        actionTimer.reset();
    }

    // =================== STATE MACHINE ===========================

    private void resolveState() {
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

    // ======================== SLOT ================================

    /**
     * Паттерн из AncientBot — НЕ используем InventoryTask.switchTo()!
     *
     * InventoryTask.switchTo() отправляет UpdateSelectedSlotC2SPacket вручную,
     * но НЕ обновляет lastSelectedSlot в interactionManager.
     * Потом vanilla syncSelectedSlot() видит рассинхрон и шлёт ДУБЛЬ → BadPacketsA.
     *
     * Правильно: задать selectedSlot + syncSelectedSlot() (как AncientBot).
     */
    private void selectSlot(int slot) {
        if (slot < 0 || slot > 8 || mc.player == null) return;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.syncSelectedSlot();
    }

    // ======================== GEOMETRY ============================

    private Direction getClosestFace(BlockPos pos) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d blockCenter = Vec3d.ofCenter(pos);

        double dx = eyePos.x - blockCenter.x;
        double dy = eyePos.y - blockCenter.y;
        double dz = eyePos.z - blockCenter.z;

        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);

        if (ay >= ax && ay >= az) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        } else if (ax >= az) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    /**
     * Ротация к точке (Vec3d) с GCD snap.
     * adjustSensitivity() привязывает дельту к GCD мыши → AimModulo360 bypass.
     */
    private void rotateTo(Vec3d point) {
        Turns angle = MathAngle.calculateAngle(point);
        Turns adjusted = angle.adjustSensitivity();

        if (!silentRotations.isValue()) {
            mc.player.setYaw(adjusted.getYaw());
            mc.player.setPitch(adjusted.getPitch());
        }
        TurnsConnection.INSTANCE.rotateTo(adjusted, TurnsConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    // =================== INVENTORY ===============================

    private String checkInventory() {
        if (findHoeSlot()      == -1) return "Мотыга (Hoe)";
        if (findBoneMealSlot() == -1) return "Костная мука (Bone Meal)";
        if (findAxeSlot()      == -1) return "Топор (Axe)";
        if (findSaplingSlot()  == -1) return "Саженец (Sapling)";
        return null;
    }

    private int findHoeSlot() {
        return findHotbarSlot(stack -> stack.getItem() instanceof HoeItem);
    }

    private int findAxeSlot() {
        return findHotbarSlot(stack -> stack.getItem() instanceof AxeItem);
    }

    private int findSaplingSlot() {
        return findHotbarSlot(stack -> {
            Block block = Block.getBlockFromItem(stack.getItem());
            return block != null && block.getDefaultState().isIn(BlockTags.SAPLINGS);
        });
    }

    private int findBoneMealSlot() {
        return findHotbarSlot(stack -> stack.isOf(Items.BONE_MEAL));
    }

    private int findHotbarSlot(java.util.function.Predicate<ItemStack> predicate) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && predicate.test(stack)) return i;
        }
        return -1;
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
                    mc.player.getZ() + nz * d
            );
            if (isDirt(mc.world.getBlockState(check).getBlock())) return check;
        }

        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -2; y <= 0; y++) {
                    BlockPos check = playerPos.add(x, y, z);
                    if (isDirt(mc.world.getBlockState(check).getBlock())) {
                        double dist = check.getSquaredDistance(playerPos);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closest = check;
                        }
                    }
                }
            }
        }
        return closest;
    }

    private boolean isDirt(Block b) {
        return b == Blocks.DIRT || b == Blocks.GRASS_BLOCK || b == Blocks.ROOTED_DIRT || b == Blocks.COARSE_DIRT;
    }

    private List<BlockPos> findLogsAbove() {
        List<BlockPos> logs = new ArrayList<>();

        for (int y = 1; y <= 30; y++) {
            BlockPos check = dirtPos.up(y);
            if (mc.world.getBlockState(check).isIn(BlockTags.LOGS)) {
                logs.add(check);
            }
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
                            && neighbor.getSquaredDistance(dirtPos) < 100) {
                        if (mc.world.getBlockState(neighbor).isIn(BlockTags.LOGS)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                            logs.add(neighbor);
                        }
                    }
                }
            }
        }

        logs.sort(Comparator.comparingInt(BlockPos::getY));
        return logs;
    }

    private List<BlockPos> findLeavesNearby(int radius) {
        List<BlockPos> leaves = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= 20; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos check = dirtPos.add(x, y + 1, z);
                    if (mc.world.getBlockState(check).isIn(BlockTags.LEAVES)) {
                        leaves.add(check);
                    }
                }
            }
        }

        final Vec3d playerPos = mc.player.getPos();
        leaves.sort(Comparator.comparingDouble(p ->
                p.getSquaredDistance(playerPos.x, playerPos.y, playerPos.z)));
        return leaves;
    }

    private boolean hasTreeBlocksAbove() {
        for (int y = 2; y <= 30; y++) {
            if (mc.world.getBlockState(dirtPos.up(y)).isIn(BlockTags.LOGS)) return true;
        }
        return false;
    }

    private boolean hasLeavesNearby() {
        return !findLeavesNearby(searchRadius.getInt()).isEmpty();
    }

    // =================== HELPERS =================================

    private void chatMessage(Formatting color, String msg) {
        if (mc.player != null) {
            mc.player.sendMessage(
                    Text.literal("[AppleFarm] ").formatted(Formatting.GOLD)
                            .append(Text.literal(msg).formatted(color)),
                    false
            );
        }
    }
}
