/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.block.entity.BrewingStandBlockEntity
 *  net.minecraft.block.entity.ChestBlockEntity
 *  net.minecraft.client.gui.screen.ingame.BrewingStandScreen
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.Potions
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.screen.BrewingStandScreenHandler
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package moscow.rockstar.systems.modules.modules.player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@ModuleInfo(name="Auto Brew", category=ModuleCategory.PLAYER, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0432\u0430\u0440\u0438\u0442 \u0437\u0435\u043b\u044c\u044f")
public class AutoBrew
extends BaseModule {
    private final ModeSetting potions = new ModeSetting(this, "\u0412\u0430\u0440\u0438\u0442\u044c");
    private final ModeSetting.Value strength = new ModeSetting.Value(this.potions, "\u0417\u0435\u043b\u044c\u0435 \u0441\u0438\u043b\u044b").select();
    private final ModeSetting.Value speed = new ModeSetting.Value(this.potions, "\u0417\u0435\u043b\u044c\u0435 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438");
    private final ModeSetting.Value fire = new ModeSetting.Value(this.potions, "\u0417\u0435\u043b\u044c\u0435 \u043e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u0438");
    private final SliderSetting delay = new SliderSetting((SettingsContainer)this, "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043d\u0430 \u043f\u0435\u0440\u0435\u043c\u0435\u0449\u0435\u043d\u0438\u0435 \u0438\u043d\u0433\u0440\u0435\u0434\u0438\u0435\u043d\u0442\u043e\u0432").step(10.0f).min(100.0f).max(1000.0f).currentValue(100.0f);
    private final Timer timer = new Timer();
    private State state = State.IDLE;
    private final Timer actionTimer = new Timer();
    private BrewingStandBlockEntity currentBrewer;
    private ChestBlockEntity currentChest;
    private final List<BlockPos> processedBrewers = new ArrayList<BlockPos>();
    private List<BrewingStandBlockEntity> brewersQueue = new ArrayList<BrewingStandBlockEntity>();
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
        switch (this.state.ordinal()) {
            case 0: {
                this.handleIdleState();
                break;
            }
            case 1: {
                this.handleOpeningState();
                break;
            }
            case 2: {
                this.handleProcessingState();
                break;
            }
            case 3: {
                this.handleDepositingState();
                break;
            }
            case 4: {
                this.handleClosingState();
            }
        }
    };

    private void handleOpeningState() {
        if (AutoBrew.mc.currentScreen instanceof BrewingStandScreen) {
            this.state = State.PROCESSING;
            return;
        }
        if (this.actionTimer.finished(500L)) {
            BlockPos pos = this.currentBrewer.getPos();
            Vec3d vec = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            BlockHitResult hit = new BlockHitResult(vec, Direction.UP, pos, false);
            AutoBrew.mc.interactionManager.interactBlock(AutoBrew.mc.player, Hand.MAIN_HAND, hit);
            this.actionTimer.reset();
        }
    }

    private void handleProcessingState() {
        ScreenHandler screenHandler = AutoBrew.mc.player.currentScreenHandler;
        if (!(screenHandler instanceof BrewingStandScreenHandler)) {
            this.state = State.IDLE;
            return;
        }
        BrewingStandScreenHandler brew = (BrewingStandScreenHandler)screenHandler;
        if (brew.getFuel() > 0 && brew.getSlot(3).getStack().getItem() != Items.AIR) {
            return;
        }
        if (brew.getSlot(4).getStack().getItem() == Items.AIR && brew.getFuel() == 0) {
            if (this.findIngredient(Items.BLAZE_POWDER) == -1) {
                return;
            }
            this.swapOneItem(Items.BLAZE_POWDER, 4);
        }
        for (int i = 0; i < 3; ++i) {
            if (brew.getSlot(i).getStack().getItem() != Items.AIR) continue;
            if (this.findWaterBottle(brew) == -1) {
                return;
            }
            InventoryUtility.quickMove(this.findWaterBottle(brew));
        }
        if (brew.getSlot(3).getStack().getItem() == Items.AIR) {
            if (this.isPotionType(brew, (Potion)Potions.WATER.value())) {
                if (this.findIngredient(Items.NETHER_WART) == -1) {
                    Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.ERROR, "\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d", "\u0412\u0430\u043c \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u0438\u043c\u0435\u0442\u044c " + Items.NETHER_WART.getName().getString() + " \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435");
                }
                this.handleIngredient(Items.NETHER_WART, 3);
            }
            if (this.strength.isSelected() && this.isPotionType(brew, (Potion)Potions.AWKWARD.value())) {
                this.handleIngredient(Items.BLAZE_POWDER, 3);
            } else if (this.speed.isSelected() && this.isPotionType(brew, (Potion)Potions.AWKWARD.value())) {
                this.handleIngredient(Items.SUGAR, 3);
            } else if (this.fire.isSelected() && this.isPotionType(brew, (Potion)Potions.AWKWARD.value())) {
                this.handleIngredient(Items.MAGMA_CREAM, 3);
            }
            if (this.isPotionType(brew, (Potion)Potions.STRENGTH.value()) || this.isPotionType(brew, (Potion)Potions.SWIFTNESS.value())) {
                this.handleIngredient(Items.GLOWSTONE_DUST, 3);
            }
            if (this.isPotionType(brew, (Potion)Potions.FIRE_RESISTANCE.value())) {
                this.handleIngredient(Items.REDSTONE, 3);
            }
            if (this.isPotionType(brew, (Potion)Potions.STRONG_STRENGTH.value()) || this.isPotionType(brew, (Potion)Potions.STRONG_SWIFTNESS.value()) || this.isPotionType(brew, (Potion)Potions.LONG_FIRE_RESISTANCE.value())) {
                this.lootPotions(brew);
                this.state = State.DEPOSITING;
            }
        }
    }

    private void handleIdleState() {
        if (this.actionTimer.finished(1000L)) {
            if (this.brewersQueue.isEmpty()) {
                this.brewersQueue = this.findBrewers();
            }
            if (!this.brewersQueue.isEmpty()) {
                this.currentBrewer = this.brewersQueue.removeFirst();
                this.state = State.OPENING_BREWER;
                this.actionTimer.reset();
            }
        }
    }

    private void handleIngredient(Item item, int slot) {
        if (this.findIngredient(item) == -1) {
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.ERROR, "\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d", "\u0412\u0430\u043c \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u0438\u043c\u0435\u0442\u044c " + item.getName().getString() + " \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435");
            this.toggle();
            return;
        }
        this.swapOneItem(item, slot);
        AutoBrew.mc.player.closeHandledScreen();
    }

    private void handleDepositingState() {
        if (this.actionTimer.finished(500L)) {
            List<ChestBlockEntity> chests = this.findChests();
            if (!chests.isEmpty()) {
                this.currentChest = chests.getFirst();
                this.depositPotions();
            }
            this.state = State.CLOSING;
            this.actionTimer.reset();
        }
    }

    private void handleClosingState() {
        if (this.actionTimer.finished(500L)) {
            AutoBrew.mc.player.closeHandledScreen();
            if (this.currentBrewer != null) {
                this.processedBrewers.add(this.currentBrewer.getPos());
            }
            this.state = State.IDLE;
            this.currentBrewer = null;
            this.currentChest = null;
            this.actionTimer.reset();
        }
    }

    private List<BrewingStandBlockEntity> findBrewers() {
        ArrayList<BrewingStandBlockEntity> brewers = new ArrayList<BrewingStandBlockEntity>();
        int range = 10;
        BlockPos playerPos = BlockPos.ofFloored((Position)AutoBrew.mc.player.getPos());
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockEntity blockEntity = AutoBrew.mc.world.getBlockEntity(pos);
                    if (!(blockEntity instanceof BrewingStandBlockEntity)) continue;
                    BrewingStandBlockEntity brewer = (BrewingStandBlockEntity)blockEntity;
                    brewers.add(brewer);
                }
            }
        }
        return brewers;
    }

    private List<ChestBlockEntity> findChests() {
        ArrayList<ChestBlockEntity> chests = new ArrayList<ChestBlockEntity>();
        int range = 10;
        BlockPos playerPos = BlockPos.ofFloored((Position)AutoBrew.mc.player.getPos());
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockEntity blockEntity = AutoBrew.mc.world.getBlockEntity(pos);
                    if (!(blockEntity instanceof ChestBlockEntity)) continue;
                    ChestBlockEntity chest = (ChestBlockEntity)blockEntity;
                    chests.add(chest);
                }
            }
        }
        chests.sort(Comparator.comparingDouble(c -> c.getPos().getSquaredDistance((Vec3i)playerPos)));
        return chests;
    }

    private void depositPotions() {
        if (this.currentChest == null) {
            return;
        }
        for (int i = 0; i < AutoBrew.mc.player.getInventory().size(); ++i) {
            int chestSlot;
            ItemStack stack = AutoBrew.mc.player.getInventory().getStack(i);
            if (!this.isPotion(stack) || (chestSlot = this.findChestSlot(this.currentChest)) == -1) continue;
            AutoBrew.mc.interactionManager.clickSlot(AutoBrew.mc.player.playerScreenHandler.syncId, i < 9 ? i + 36 : i, chestSlot, SlotActionType.QUICK_MOVE, (PlayerEntity)AutoBrew.mc.player);
        }
    }

    private boolean isPotion(ItemStack stack) {
        return stack.getItem() == Items.POTION || stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION;
    }

    private int findChestSlot(ChestBlockEntity chest) {
        for (int i = 0; i < chest.size(); ++i) {
            if (!chest.getStack(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private void lootPotions(BrewingStandScreenHandler brew) {
        for (int i = 0; i < 3; ++i) {
            if (brew.getSlot(i).getStack().isEmpty()) continue;
            InventoryUtility.quickMove(i);
        }
    }

    private void swapOneItem(Item item, int to) {
        int slot;
        if (this.timer.finished((long)(this.delay.getCurrentValue() * 2.0f)) && (slot = this.findIngredient(item)) != -1) {
            InventoryUtility.swapOneItem(slot, to);
            this.timer.reset();
        }
    }

    private int findIngredient(Item item) {
        for (int i = 5; i < 41; ++i) {
            if (((Slot)AutoBrew.mc.player.currentScreenHandler.slots.get(i)).getStack().getItem() != item) continue;
            return i;
        }
        return -1;
    }

    private boolean isPotionType(BrewingStandScreenHandler brew, Potion potion) {
        boolean needIng = true;
        for (int i = 0; i < 3; ++i) {
            ItemStack stack = ((Slot)brew.slots.get(i)).getStack();
            if (stack.getItem() != Items.POTION || ((RegistryEntry)((PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS)).potion().get()).value() == potion) continue;
            needIng = false;
        }
        return needIng;
    }

    private int findWaterBottle(BrewingStandScreenHandler brew) {
        for (int i = 5; i < 41; ++i) {
            ItemStack stack = ((Slot)brew.slots.get(i)).getStack();
            if (stack.getItem() != Items.POTION || !((PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS)).potion().isPresent() || ((RegistryEntry)((PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS)).potion().get()).value() != Potions.WATER.value()) continue;
            return i;
        }
        return -1;
    }

    private static enum State {
        IDLE,
        OPENING_BREWER,
        PROCESSING,
        DEPOSITING,
        CLOSING;

    }
}

