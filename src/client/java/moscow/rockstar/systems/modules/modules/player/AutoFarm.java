/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.CropBlock
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.player;

import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.player.autofarm.AutoFarmCounter;
import moscow.rockstar.systems.modules.modules.player.autofarm.AutoFarmNukeTurkey;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Auto Farm", category=ModuleCategory.PLAYER)
public class AutoFarm
extends BaseModule {
    private enum FarmPhase {
        SYNC_HANDS, PLANT, GROW, BREAK, WAIT_SERVER
    }

    private final ModeSetting mode = new ModeSetting(this, "modules.settings.auto_farm.mode");
    private final ModeSetting.Value carrot = new ModeSetting.Value(this.mode, "modules.settings.auto_farm.mode.carrot").select();
    private final ModeSetting.Value sweetBerries = new ModeSetting.Value(this.mode, "Sweet Berries");
    private final ModeSetting.Value potato = new ModeSetting.Value(this.mode, "modules.settings.auto_farm.mode.potato");
    private final ModeSetting.Value beetroot = new ModeSetting.Value(this.mode, "modules.settings.auto_farm.mode.beetroot");
    private final BooleanSetting mineBlocks = new BooleanSetting(this, "modules.settings.auto_farm.mine_blocks");
    private final SelectSetting blocks = new SelectSetting((SettingsContainer)this, "modules.settings.auto_farm.blocks", () -> !this.mineBlocks.isEnabled());
    private final SelectSetting.Value melon = new SelectSetting.Value(this.blocks, "modules.settings.auto_farm.blocks.melon");
    private final SelectSetting.Value tikva = new SelectSetting.Value(this.blocks, "modules.settings.auto_farm.blocks.pumpkin");
    private final SelectSetting.Value allCrops = new SelectSetting.Value(this.blocks, "modules.settings.auto_farm.blocks.other_crops");
    private final BooleanSetting autoExp = new BooleanSetting(this, "modules.settings.auto_farm.auto_exp").enabled(true);
    private final BooleanSetting autoSell = new BooleanSetting(this, "modules.settings.auto_farm.auto_sell").enabled(true);
    private final AutoFarmNukeTurkey nukeTurkey = new AutoFarmNukeTurkey();
    private final AutoFarmCounter count = new AutoFarmCounter();
    private final Timer timer = new Timer();
    private final Timer farmActionTimer = new Timer();
    private boolean repairing;
    private boolean cursorCheck;
    private int afterBreakTicks;
    private FarmPhase farmPhase = FarmPhase.SYNC_HANDS;
    private BlockPos lastActionPos;
    private int sameStateTicks;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
        if (AutoFarm.mc.player == null || AutoFarm.mc.world == null || AutoFarm.mc.interactionManager == null) return;
        this.rotateDownSilent();
        if (this.afterBreakTicks > 0) {
            --this.afterBreakTicks;
        }
        if (!this.repairing) {
            this.ensureSelectedItemInOffhand();
            this.ensureHoeInMainHand();
        }
        SlotGroup<ItemSlot> search = SlotGroups.inventory().and(SlotGroups.hotbar()).and(SlotGroups.offhand());
        ItemSlot exp = search.findItem(Items.EXPERIENCE_BOTTLE);
        List<Item> hoes = List.of(Items.NETHERITE_HOE, Items.DIAMOND_HOE);
        List<Item> items = List.of(Items.SWEET_BERRIES, Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS);
        ItemStack mainHand = AutoFarm.mc.player.getMainHandStack();
        ItemStack offHand = AutoFarm.mc.player.getOffHandStack();
        BlockState farmState = AutoFarm.mc.world.getBlockState(AutoFarm.mc.player.getBlockPos());
        BlockState cropState = AutoFarm.mc.world.getBlockState(AutoFarm.mc.player.getBlockPos().up());
        BlockPos cropPos = AutoFarm.mc.player.getBlockPos().up();
        BlockHitResult blockHitResult = this.hit(AutoFarm.mc.player.getBlockPos(), Direction.UP);
        BlockHitResult cropHitResult = this.hit(cropPos, Direction.UP);
        if (!(this.mineBlocks.isEnabled() || this.melon.isSelected() || this.tikva.isSelected() || this.allCrops.isSelected())) {
            if (this.autoExp.isEnabled()) {
                int max = mainHand.getMaxDamage();
                int cur = max - mainHand.getDamage();
                double percent = (double)cur / (double)max;
                if (!AutoFarm.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    int emptySlot;
                    this.cursorCheck = true;
                    if (this.timer.finished(60L) && (emptySlot = AutoFarm.mc.player.getInventory().getEmptySlot()) != -1) {
                        AutoFarm.mc.interactionManager.clickSlot(0, emptySlot < 9 ? emptySlot + 36 : emptySlot, 0, SlotActionType.PICKUP, (PlayerEntity)AutoFarm.mc.player);
                    }
                    return;
                }
                if (this.cursorCheck) {
                    this.cursorCheck = false;
                    this.timer.reset();
                }
                if (this.repairing) {
                    if (exp != null && offHand.getItem() != exp.item() && this.timer.finished(60L)) {
                        InventoryUtility.moveItem(exp.getIdForServer(), 45, false);
                        this.timer.reset();
                    } else if (exp == null && !items.contains(offHand.getItem()) && this.findItem() != -1 && this.timer.finished(60L)) {
                        InventoryUtility.moveItem(this.findItem(), 45, false);
                        this.timer.reset();
                    } else if (exp != null && offHand.getItem() == exp.item() && this.timer.finished(60L)) {
                        this.rotateDownSilent();
                        AutoFarm.mc.interactionManager.sendSequencedPacket(AutoFarm.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, sequence, AutoFarm.mc.player.getYaw(), 90.0f));
                        this.timer.reset();
                        if (percent > 0.6) {
                            this.repairing = false;
                            if (this.findItem() != -1) {
                                InventoryUtility.moveItem(this.findItem(), 45, false);
                            }
                        }
                    }
                }
                if (hoes.contains(mainHand.getItem()) && percent < 0.5 && !this.repairing && exp != null) {
                    this.repairing = true;
                }
            }
            if (this.autoSell.isEnabled() && AutoFarm.mc.player.getInventory().getEmptySlot() == -1) {
                if (AutoFarm.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
                    if (ServerUtility.isFT() ? AutoFarm.mc.currentScreen.getTitle().getString().equals(Localizator.translate("modules.auto_farm.screen.select_section_ft")) : AutoFarm.mc.currentScreen.getTitle().getString().equals(Localizator.translate("modules.auto_farm.screen.select_section"))) {
                        AutoFarm.mc.interactionManager.clickSlot(AutoFarm.mc.player.currentScreenHandler.syncId, 14, 0, SlotActionType.PICKUP, (PlayerEntity)AutoFarm.mc.player);
                    }
                    if (AutoFarm.mc.currentScreen.getTitle().getString().equals(Localizator.translate("modules.auto_farm.screen.food_seller"))) {
                        AutoFarm.mc.interactionManager.clickSlot(AutoFarm.mc.player.currentScreenHandler.syncId, this.ssItem(), 0, SlotActionType.PICKUP, (PlayerEntity)AutoFarm.mc.player);
                        if (this.timer.finished(120L)) {
                            AutoFarm.mc.player.closeHandledScreen();
                            this.timer.reset();
                        }
                    }
                } else if (this.timer.finished(120L)) {
                    AutoFarm.mc.player.networkHandler.sendChatCommand("buyer");
                    this.timer.reset();
                }
            } else if (farmState.getBlock().equals(Blocks.FARMLAND) && !this.repairing) {
                this.tickPerfectFarm(cropState, cropPos, blockHitResult, cropHitResult);
            }
        } else {
            this.nukeTurkey.nuke();
        }
    };

    private void tickPerfectFarm(BlockState cropState, BlockPos cropPos, BlockHitResult blockHitResult, BlockHitResult cropHitResult) {
        this.rotateDownSilent();
        Item main = AutoFarm.mc.player.getMainHandStack().getItem();
        Item off = AutoFarm.mc.player.getOffHandStack().getItem();
        boolean hoeReady = main == Items.NETHERITE_HOE || main == Items.DIAMOND_HOE;
        boolean seedReady = off == this.selectedFarmItem();

        if (!hoeReady || !seedReady) {
            this.farmPhase = FarmPhase.SYNC_HANDS;
            this.ensureSelectedItemInOffhand();
            this.ensureHoeInMainHand();
            return;
        }

        FarmPhase next = this.resolvePhase(cropState);
        if (next == this.farmPhase && cropPos.equals(this.lastActionPos)) {
            ++this.sameStateTicks;
        } else {
            this.sameStateTicks = 0;
        }
        this.farmPhase = next;
        this.lastActionPos = cropPos;

        if (this.sameStateTicks > 18) {
            this.farmActionTimer.reset();
            this.afterBreakTicks = 0;
            this.sameStateTicks = 0;
        }

        switch (this.farmPhase) {
            case PLANT -> {
                if (this.afterBreakTicks > 0 || !this.farmActionTimer.finished(55L)) return;
                this.interactFarm(Hand.OFF_HAND, blockHitResult);
                this.farmActionTimer.reset();
            }
            case GROW -> {
                if (!this.farmActionTimer.finished(45L)) return;
                this.interactFarm(Hand.MAIN_HAND, cropHitResult);
                this.farmActionTimer.reset();
            }
            case BREAK -> {
                if (!this.farmActionTimer.finished(70L)) return;
                this.breakBlockStable(cropPos, Direction.UP);
                this.afterBreakTicks = 4;
                this.farmActionTimer.reset();
            }
            case WAIT_SERVER -> {
                // Ничего не спамим: ждём серверную смену блока после packet break.
            }
            case SYNC_HANDS -> {
                this.ensureSelectedItemInOffhand();
                this.ensureHoeInMainHand();
            }
        }
    }

    private FarmPhase resolvePhase(BlockState cropState) {
        if (this.afterBreakTicks > 0) return FarmPhase.WAIT_SERVER;
        Block block = cropState.getBlock();
        if (cropState.isAir()) return FarmPhase.PLANT;
        if (block instanceof CropBlock crop) {
            return crop.isMature(cropState) ? FarmPhase.BREAK : FarmPhase.GROW;
        }
        return FarmPhase.WAIT_SERVER;
    }

    private void interactFarm(Hand hand, BlockHitResult hit) {
        if (AutoFarm.mc.player == null || AutoFarm.mc.interactionManager == null) return;
        AutoFarm.mc.interactionManager.interactBlock(AutoFarm.mc.player, hand, hit);
        AutoFarm.mc.player.swingHand(hand);
    }

    private BlockHitResult hit(BlockPos pos, Direction face) {
        return new BlockHitResult(Vec3d.ofCenter(pos).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5), face, pos, false);
    }

    private void rotateDownSilent() {
        if (AutoFarm.mc.player == null) return;
        Rockstar.getInstance().getRotationHandler().rotate(new Rotation(AutoFarm.mc.player.getYaw(), 90.0f), MoveCorrection.SILENT, 180.0f, 180.0f, 180.0f, RotationPriority.USE_ITEM);
    }

    private void breakBlockStable(BlockPos pos, Direction direction) {
        if (AutoFarm.mc.player == null || AutoFarm.mc.player.networkHandler == null) return;
        AutoFarm.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction));
        AutoFarm.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction));
        AutoFarm.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
        AutoFarm.mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void ensureHoeInMainHand() {
        if (AutoFarm.mc.player == null || AutoFarm.mc.interactionManager == null) return;
        ItemStack main = AutoFarm.mc.player.getMainHandStack();
        if (main.getItem() == Items.NETHERITE_HOE || main.getItem() == Items.DIAMOND_HOE) return;
        SlotGroup<ItemSlot> search = SlotGroups.inventory().and(SlotGroups.hotbar());
        ItemSlot hoe = search.findItem(stack -> stack.getItem() == Items.NETHERITE_HOE || stack.getItem() == Items.DIAMOND_HOE);
        if (hoe == null || !this.farmActionTimer.finished(80L)) return;
        if (hoe instanceof HotbarSlot hotbarSlot) {
            InventoryUtility.selectHotbarSlot(hotbarSlot);
        } else if (hoe instanceof InventorySlot inventorySlot) {
            inventorySlot.swapTo(InventoryUtility.getCurrentHotbarSlot());
        }
        this.farmActionTimer.reset();
    }

    private Item selectedFarmItem() {
        if (this.sweetBerries.isSelected()) return Items.SWEET_BERRIES;
        if (this.carrot.isSelected()) return Items.CARROT;
        if (this.potato.isSelected()) return Items.POTATO;
        if (this.beetroot.isSelected()) return Items.BEETROOT_SEEDS;
        return Items.CARROT;
    }

    private void ensureSelectedItemInOffhand() {
        if (AutoFarm.mc.player == null || AutoFarm.mc.interactionManager == null) return;
        Item selected = this.selectedFarmItem();
        if (AutoFarm.mc.player.getOffHandStack().getItem() == selected) return;
        int slot = this.findItem();
        if (slot != -1 && this.timer.finished(60L)) {
            InventoryUtility.moveItem(slot, 45, false);
            this.timer.reset();
        }
    }

    public int findItem() {
        SlotGroup<ItemSlot> search = SlotGroups.inventory().and(SlotGroups.hotbar());
        Item selected = this.selectedFarmItem();
        ItemSlot foundSlot = search.findItem(stack -> stack.getItem() == selected);
        return foundSlot != null ? foundSlot.getIdForServer() : -1;
    }

    public int ssItem() {
        if (this.sweetBerries.isSelected()) {
            return 12;
        }
        if (this.carrot.isSelected()) {
            return 13;
        }
        if (this.potato.isSelected()) {
            return 14;
        }
        if (this.beetroot.isSelected()) {
            return 15;
        }
        return -1;
    }

    @Override
    public void onDisable() {
        this.repairing = false;
        this.afterBreakTicks = 0;
        this.sameStateTicks = 0;
        this.lastActionPos = null;
        this.farmPhase = FarmPhase.SYNC_HANDS;
        this.count.price = 0;
    }

    @Generated
    public ModeSetting getMode() {
        return this.mode;
    }

    @Generated
    public ModeSetting.Value getCarrot() {
        return this.carrot;
    }

    @Generated
    public ModeSetting.Value getSweetBerries() {
        return this.sweetBerries;
    }

    @Generated
    public ModeSetting.Value getPotato() {
        return this.potato;
    }

    @Generated
    public ModeSetting.Value getBeetroot() {
        return this.beetroot;
    }

    @Generated
    public BooleanSetting getMineBlocks() {
        return this.mineBlocks;
    }

    @Generated
    public SelectSetting getBlocks() {
        return this.blocks;
    }

    @Generated
    public SelectSetting.Value getMelon() {
        return this.melon;
    }

    @Generated
    public SelectSetting.Value getTikva() {
        return this.tikva;
    }

    @Generated
    public SelectSetting.Value getAllCrops() {
        return this.allCrops;
    }

    @Generated
    public BooleanSetting getAutoExp() {
        return this.autoExp;
    }

    @Generated
    public BooleanSetting getAutoSell() {
        return this.autoSell;
    }

    @Generated
    public AutoFarmNukeTurkey getNukeTurkey() {
        return this.nukeTurkey;
    }

    @Generated
    public AutoFarmCounter getCount() {
        return this.count;
    }

    @Generated
    public Timer getTimer() {
        return this.timer;
    }

    @Generated
    public boolean isRepairing() {
        return this.repairing;
    }

    @Generated
    public boolean isCursorCheck() {
        return this.cursorCheck;
    }

    @Generated
    public EventListener<ClientPlayerTickEvent> getOnClientPlayerTickEvent() {
        return this.onClientPlayerTickEvent;
    }
}

