/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.PickaxeItem
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.util.Hand
 */
package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.StartBreakBlockEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

@ModuleInfo(name="Mine Helper", category=ModuleCategory.PLAYER, desc="\u041f\u043e\u043c\u043e\u0449\u043d\u0438\u043a \u0432 \u0448\u0430\u0445\u0442\u0435")
public class MineHelper
extends BaseModule {
    private final BooleanSetting save = new BooleanSetting((SettingsContainer)this, "\u0421\u043e\u0445\u0440\u0430\u043d\u044f\u0442\u044c \u043a\u0438\u0440\u043a\u0443", "\u041d\u0435 \u0434\u0430\u0435\u0442 \u0441\u043b\u043e\u043c\u0430\u0442\u044c \u0431\u043b\u043e\u043a, \u0435\u0441\u043b\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0434\u043e\u0441\u0442\u0438\u0433 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d\u043d\u043e\u0439 \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u0438").enable();
    public final SliderSetting percent = new SliderSetting(this, "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c").step(1.0f).min(1.0f).max(70.0f).currentValue(10.0f).suffix("%");
    private final BooleanSetting autoReplace = new BooleanSetting((SettingsContainer)this, "\u0410\u0432\u0442\u043e \u0437\u0430\u043c\u0435\u043d\u0430", "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043c\u0435\u043d\u044f\u0435\u0442 \u043a\u0438\u0440\u043a\u0443 \u043f\u0440\u0438 \u043d\u0438\u0437\u043a\u043e\u0439 \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u0438");
    private final BooleanSetting autoRepair = new BooleanSetting((SettingsContainer)this, "\u0410\u0432\u0442\u043e \u043f\u043e\u0447\u0438\u043d\u043a\u0430", "\u0427\u0438\u043d\u0438\u0442 \u043a\u0438\u0440\u043a\u0443 \u0441 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435\u043c \u043e\u043f\u044b\u0442\u0430");
    private final BindSetting bind = new BindSetting(this, "\u041a\u043d\u043e\u043f\u043a\u0430 \u043f\u043e\u0447\u0438\u043d\u043a\u0438", () -> !this.autoRepair.isEnabled());
    private final Timer timer = new Timer();
    private boolean rotate;
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (this.bind.isKey(event.getKey()) && event.getAction() == 1) {
            this.rotate = true;
            this.repairPickaxeWithBottle();
        }
    };
    private final EventListener<StartBreakBlockEvent> onStartBreakBlockEvent = event -> {
        if (MineHelper.mc.player == null) {
            return;
        }
        ItemStack currentStack = MineHelper.mc.player.getMainHandStack();
        if (!this.isValidPickaxe(currentStack)) {
            return;
        }
        double durabilityPercent = this.getDurabilityPercent(currentStack);
        if (!this.save.isEnabled() || durabilityPercent >= (double)this.percent.getCurrentValue()) {
            return;
        }
        event.cancel();
        this.handleLowDurability(currentStack);
    };
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (MineHelper.mc.player == null || !this.rotate) {
            return;
        }
        Rockstar.getInstance().getRotationHandler().rotate(new Rotation(MineHelper.mc.player.getYaw(), 90.0f), MoveCorrection.SILENT, 180.0f, 180.0f, 180.0f, RotationPriority.USE_ITEM);
    };

    private void handleLowDurability(ItemStack currentStack) {
        boolean switched = false;
        if (this.autoReplace.isEnabled()) {
            switched = this.trySwitchPickaxe(currentStack);
        }
        if (!switched && this.timer.finished(800L)) {
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.ERROR, "\u041a\u0438\u0440\u043a\u0430 \u043f\u043e\u0447\u0442\u0438 \u0441\u043b\u043e\u043c\u0430\u043d\u0430!", "\u041d\u0435\u0442 \u0437\u0430\u043c\u0435\u043d\u044b/\u043e\u043f\u044b\u0442\u0430 \u0434\u043b\u044f \u043f\u043e\u0447\u0438\u043d\u043a\u0438");
            this.timer.reset();
        }
    }

    private void repairPickaxeWithBottle() {
        if (MineHelper.mc.player == null || MineHelper.mc.currentScreen != null) {
            return;
        }
        ItemStack pickaxe = MineHelper.mc.player.getMainHandStack();
        if (!this.isValidPickaxe(pickaxe) || pickaxe.getDamage() == 0) {
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.ERROR, "\u041e\u0448\u0438\u0431\u043a\u0430", "\u041d\u0435\u0442 \u043a\u0438\u0440\u043a\u0438 \u0438\u043b\u0438 \u043e\u043d\u0430 \u043d\u0435 \u043f\u043e\u0432\u0440\u0435\u0436\u0434\u0435\u043d\u0430!");
            this.rotate = false;
            return;
        }
        if (!this.ensureBottleInOffhand()) {
            return;
        }
        this.useExperienceBottle();
    }

    private boolean ensureBottleInOffhand() {
        ItemStack offhand = MineHelper.mc.player.getOffHandStack();
        if (offhand.getItem() == Items.EXPERIENCE_BOTTLE) {
            return true;
        }
        SlotGroup<ItemSlot> searchArea = SlotGroups.inventory().and(SlotGroups.hotbar());
        ItemSlot bottleSlot = searchArea.findItem(stack -> stack.getItem() == Items.EXPERIENCE_BOTTLE);
        if (bottleSlot == null) {
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.ERROR, "\u041d\u0435\u0442 \u0431\u0443\u0442\u044b\u043b\u043e\u043a \u043e\u043f\u044b\u0442\u0430!", "\u0412\u0430\u043c \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u0438\u043c\u0435\u0442\u044c \u0431\u0443\u0442\u044b\u043b\u043e\u0447\u043a\u0438 \u043e\u043f\u044b\u0442\u0430 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435");
            this.rotate = false;
            return false;
        }
        InventoryUtility.moveItem(bottleSlot, InventoryUtility.getOffHandSlot());
        return true;
    }

    private void useExperienceBottle() {
        if (MineHelper.mc.player.getOffHandStack().getItem() != Items.EXPERIENCE_BOTTLE) {
            MineHelper.mc.options.useKey.setPressed(false);
            this.rotate = false;
            return;
        }
        RotationHandler rotation = Rockstar.getInstance().getRotationHandler();
        MineHelper.mc.interactionManager.sendSequencedPacket(MineHelper.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, sequence, rotation.getServerRotation().getYaw(), rotation.getServerRotation().getYaw()));
    }

    private boolean trySwitchPickaxe(ItemStack currentStack) {
        HotbarSlot bestSlot = this.findBestPickaxeSlot(currentStack);
        if (bestSlot == null) {
            return false;
        }
        InventoryUtility.selectHotbarSlot(bestSlot);
        if (this.timer.finished(800L)) {
            ItemStack newStack = bestSlot.itemStack();
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.SUCCESS, "\u0417\u0430\u043c\u0435\u043d\u0430 \u043a\u0438\u0440\u043a\u0438", String.format("\u0417\u0430\u043c\u0435\u043d\u0438\u043b \u043a\u0438\u0440\u043a\u0443 \u0441 %.1f%% \u043d\u0430 %.1f%%", this.getDurabilityPercent(currentStack), this.getDurabilityPercent(newStack)));
            this.timer.reset();
        }
        return true;
    }

    private HotbarSlot findBestPickaxeSlot(ItemStack currentStack) {
        double currentDurability = this.getDurabilityPercent(currentStack);
        HotbarSlot bestSlot = null;
        double bestDurability = currentDurability;
        for (int i = 0; i < 9; ++i) {
            double durability;
            HotbarSlot slot = InventoryUtility.getHotbarSlot(i);
            ItemStack stack = slot.itemStack();
            if (!this.isValidPickaxe(stack) || !((durability = this.getDurabilityPercent(stack)) > bestDurability)) continue;
            bestDurability = durability;
            bestSlot = slot;
        }
        return bestSlot;
    }

    private boolean isValidPickaxe(ItemStack stack) {
        return stack != null && stack.isDamageable() && stack.getItem() instanceof PickaxeItem;
    }

    private double getDurabilityPercent(ItemStack stack) {
        return (double)(stack.getMaxDamage() - stack.getDamage()) / (double)stack.getMaxDamage() * 100.0;
    }
}

