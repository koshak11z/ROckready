package im.zov4ik.features.impl.movement;

import im.zov4ik.display.hud.Notifications;
import im.zov4ik.events.player.MotionEvent;
import im.zov4ik.events.player.RotationUpdateEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.client.managers.event.types.EventType;
import im.zov4ik.utils.features.aura.rotations.impl.SnapAngle;
import im.zov4ik.utils.features.aura.utils.MathAngle;
import im.zov4ik.utils.features.aura.warp.Turns;
import im.zov4ik.utils.features.aura.warp.TurnsConfig;
import im.zov4ik.utils.features.aura.warp.TurnsConnection;
import im.zov4ik.utils.interactions.inv.InventoryTask;
import im.zov4ik.utils.interactions.simulate.PlayerSimulation;
import im.zov4ik.utils.math.script.Script;
import im.zov4ik.utils.math.task.TaskPriority;
import im.zov4ik.utils.math.time.StopWatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.LightningRodBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Spider extends Module {
    Script script = new Script();

    StopWatch matrixJumpTimer = new StopWatch();
    StopWatch funTimeTimer = new StopWatch();
    StopWatch funTimeOtvodTimer = new StopWatch();
    StopWatch flowerSpiderTimer = new StopWatch();
    StopWatch flowerNotifyTimer = new StopWatch();

    @NonFinal
    int nextFunTimeDelay = 150;

    @NonFinal
    int nextFlowerDelay = 145;

    SelectSetting mode = new SelectSetting("Режим", "Выбирает режим паука")
            .value("Block", "MatrixNew", "FunTime", "FunTimeTest", "FunTime Otvod")
            .selected("Block");

    public Spider() {
        super("Spider", ModuleCategory.MOVEMENT);
        setup(mode);
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (!mode.isSelected("Block") || event.getType() != EventType.PRE || mc.player == null || mc.world == null) {
            return;
        }

        boolean offHand = mc.player.getOffHandStack().getItem() instanceof BlockItem;
        int slotId = InventoryTask.getHotbarSlotId(index -> mc.player.getInventory().getStack(index).getItem() instanceof BlockItem);
        BlockPos blockPos = findPos();

        if (script.isFinished() && (offHand || slotId != -1) && !blockPos.equals(BlockPos.ORIGIN)) {
            ItemStack stack = offHand ? mc.player.getOffHandStack() : mc.player.getInventory().getStack(slotId);
            Hand hand = offHand ? Hand.OFF_HAND : Hand.MAIN_HAND;
            Vec3d vec = blockPos.toCenterPos();
            Direction direction = Direction.getFacing(vec.x - mc.player.getX(), vec.y - mc.player.getY(), vec.z - mc.player.getZ());
            Turns angle = MathAngle.calculateAngle(vec.subtract(new Vec3d(direction.getVector()).multiply(0.5D)));
            Turns.VecRotation rotation = new Turns.VecRotation(angle, angle.toVector());

            TurnsConnection.INSTANCE.rotateTo(
                    rotation,
                    mc.player,
                    1,
                    new TurnsConfig(new SnapAngle(), true, true),
                    TaskPriority.HIGH_IMPORTANCE_1,
                    this
            );

            if (canPlace(stack)) {
                int previousSlot = mc.player.getInventory().selectedSlot;

                if (!offHand) {
                    mc.player.getInventory().selectedSlot = slotId;
                }

                mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(vec, direction.getOpposite(), blockPos, false));
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));

                if (!offHand) {
                    mc.player.getInventory().selectedSlot = previousSlot;
                }
            }
        }
    }

    @EventHandler
    public void onMove(MotionEvent event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            return;
        }

        if (mode.isSelected("FunTime")) {
            handleFunTime(event);
            return;
        }

        if (mode.isSelected("FunTimeTest")) {
            handleFunTimeTest(event);
            return;
        }

        if (mode.isSelected("FunTime Otvod")) {
            handleFunTimeOtvod(event);
            return;
        }

        if (!mode.isSelected("MatrixNew")) {
            return;
        }

        if (!mc.player.horizontalCollision || !isMatrixSurfaceNearby()) {
            return;
        }

        if (matrixJumpTimer.every(100)) {
            event.setOnGround(true);
            mc.player.setOnGround(true);
            mc.player.jump();
            mc.player.fallDistance = 0.0F;
        }
    }

    private void handleFunTime(MotionEvent event) {
        mc.options.jumpKey.setPressed(mc.options.forwardKey.isPressed());
        mc.player.setPitch(75.0F);

        if (!mc.player.horizontalCollision) {
            return;
        }

        if (!funTimeTimer.finished(nextFunTimeDelay)) {
            return;
        }

        int slot = findFunTimeItem();

        if (slot == -1) {
            Notifications.getInstance().addList("Предметы (кнопка/рычаг/крюк) не найдены!", 3000);
            setState(false);
            return;
        }

        funTimeTimer.reset();
        rollNextFunTimeDelay();

        event.setOnGround(true);
        mc.player.setOnGround(true);
        mc.player.jump();
        mc.player.fallDistance = 0.0F;

        int previousSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        HitResult hit = mc.player.raycast(4.5D, 1.0F, false);

        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        mc.player.getInventory().selectedSlot = previousSlot;
        mc.player.input.movementForward = 0.0F;
    }

    private void handleFunTimeTest(MotionEvent event) {
        mc.options.jumpKey.setPressed(mc.options.forwardKey.isPressed());
        mc.player.setPitch(85.0F);

        if (!mc.player.horizontalCollision) {
            return;
        }

        Hand hand = getUseHand();

        if (hand == null) {
            if (flowerNotifyTimer.finished(1500)) {
                Notifications.getInstance().addList("Предмет в руке не найден!", 3000);
                flowerNotifyTimer.reset();
            }

            setState(false);
            return;
        }

        if (!flowerSpiderTimer.finished(nextFlowerDelay)) {
            return;
        }

        HitResult hit = mc.player.raycast(4.5D, 1.0F, false);

        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        flowerSpiderTimer.reset();
        rollNextFlowerDelay();

        event.setOnGround(true);
        mc.player.setOnGround(true);
        mc.player.jump();
        mc.player.fallDistance = 0.0F;

        useHandAndRemovePlaced(hand, blockHit);

        mc.player.input.movementForward = 0.0F;
    }

    private Hand getUseHand() {
        if (!mc.player.getMainHandStack().isEmpty()) {
            return Hand.MAIN_HAND;
        }

        if (!mc.player.getOffHandStack().isEmpty()) {
            return Hand.OFF_HAND;
        }

        return null;
    }

    private void useHandAndRemovePlaced(Hand hand, BlockHitResult blockHit) {
        BlockPos clickedPos = blockHit.getBlockPos();
        Direction side = blockHit.getSide();
        BlockPos possiblePlacedPos = clickedPos.offset(side);

        BlockState beforeState = mc.world.getBlockState(possiblePlacedPos);

        mc.interactionManager.interactBlock(mc.player, hand, blockHit);
        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));

        removePlacedBlock(possiblePlacedPos, side.getOpposite(), beforeState);
    }

    private void removePlacedBlock(BlockPos pos, Direction direction, BlockState beforeState) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) {
            return;
        }

        BlockState afterState = mc.world.getBlockState(pos);

        if (!beforeState.isAir()) {
            return;
        }

        if (afterState.isAir()) {
            return;
        }

        mc.interactionManager.attackBlock(pos, direction);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private int findFunTimeItem() {
        Item[] targets = {
                Items.STONE_BUTTON,
                Items.LEVER,
                Items.TRIPWIRE_HOOK
        };

        for (int index = 0; index < 9; index++) {
            ItemStack stack = mc.player.getInventory().getStack(index);

            if (stack.isEmpty()) {
                continue;
            }

            for (Item target : targets) {
                if (stack.isOf(target)) {
                    return index;
                }
            }
        }

        return -1;
    }

    private void handleFunTimeOtvod(MotionEvent event) {
        if (!mc.player.horizontalCollision || !funTimeOtvodTimer.every(110)) {
            return;
        }

        mc.player.setPitch(60.0F);

        event.setOnGround(true);
        mc.player.setOnGround(true);
        mc.player.jump();
        mc.player.fallDistance = 0.0F;

        int slot = findItemInHotbar(Items.BAMBOO);

        if (slot == -1) {
            return;
        }

        HitResult hit = mc.player.raycast(4.5D, 1.0F, false);

        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        int previousSlot = mc.player.getInventory().selectedSlot;

        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = previousSlot;
    }

    private int findItemInHotbar(Item item) {
        for (int index = 0; index < 9; index++) {
            ItemStack stack = mc.player.getInventory().getStack(index);

            if (!stack.isEmpty() && stack.isOf(item)) {
                return index;
            }
        }

        return -1;
    }

    private void rollNextFunTimeDelay() {
        nextFunTimeDelay = ThreadLocalRandom.current().nextInt(125, 150);
    }

    private void rollNextFlowerDelay() {
        nextFlowerDelay = ThreadLocalRandom.current().nextInt(135, 170);
    }

    private boolean canPlace(ItemStack stack) {
        BlockPos blockPos = getBlockPos();

        if (blockPos.getY() >= mc.player.getBlockY()) {
            return false;
        }

        BlockItem blockItem = (BlockItem) stack.getItem();
        VoxelShape shape = blockItem.getBlock().getDefaultState().getCollisionShape(mc.world, blockPos);

        if (shape.isEmpty()) {
            return false;
        }

        Box box = shape.getBoundingBox().offset(blockPos);

        return !box.intersects(mc.player.getBoundingBox())
                && box.intersects(PlayerSimulation.simulateLocalPlayer(4).boundingBox);
    }

    private BlockPos findPos() {
        BlockPos blockPos = getBlockPos();

        if (mc.world.getBlockState(blockPos).isSolid()) {
            return BlockPos.ORIGIN;
        }

        return Stream.of(blockPos.west(), blockPos.east(), blockPos.south(), blockPos.north())
                .filter(pos -> mc.world.getBlockState(pos).isSolid())
                .findFirst()
                .orElse(BlockPos.ORIGIN);
    }

    private BlockPos getBlockPos() {
        return BlockPos.ofFloored(PlayerSimulation.simulateLocalPlayer(1).pos.add(0.0D, -1.0E-3D, 0.0D));
    }

    private boolean isMatrixSurfaceNearby() {
        BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());

        if (isMatrixClimbSurface(mc.world.getBlockState(playerPos), playerPos)
                || isMatrixClimbSurface(mc.world.getBlockState(playerPos.down()), playerPos.down())) {
            return true;
        }

        for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos frontPos = playerPos.offset(direction);

            if (isMatrixClimbSurface(mc.world.getBlockState(frontPos), frontPos)
                    || isMatrixClimbSurface(mc.world.getBlockState(frontPos.up()), frontPos.up())
                    || isMatrixClimbSurface(mc.world.getBlockState(frontPos.down()), frontPos.down())) {
                return true;
            }
        }

        return false;
    }

    private boolean isMatrixClimbSurface(BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        VoxelShape shape = state.getCollisionShape(mc.world, pos);

        if (shape.isEmpty()) {
            return false;
        }

        return block instanceof FenceBlock
                || block instanceof FenceGateBlock
                || block instanceof WallBlock
                || block instanceof PaneBlock
                || block instanceof TrapdoorBlock
                || block instanceof AnvilBlock
                || block instanceof ChestBlock
                || block instanceof EnderChestBlock
                || block instanceof TrappedChestBlock
                || block instanceof LadderBlock
                || block instanceof StairsBlock
                || block instanceof LanternBlock
                || block instanceof LightningRodBlock;
    }

    @Override
    public void deactivate() {
        if (mc.player != null) {
            mc.options.jumpKey.setPressed(false);
        }

        nextFunTimeDelay = 150;
        nextFlowerDelay = 145;

        funTimeTimer.reset();
        funTimeOtvodTimer.reset();
        flowerSpiderTimer.reset();
        flowerNotifyTimer.reset();

        super.deactivate();
    }
}