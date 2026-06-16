/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.Item$TooltipContext
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.tooltip.TooltipType
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.modules.modules.player.autofarm;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.player.AutoFarm;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class AutoFarmCounter
implements IMinecraft {
    private final Timer timer = new Timer();
    public int price = 0;

    public int getTotalSelectedCrops() {
        AutoFarm autoFarm = Rockstar.getInstance().getModuleManager().getModule(AutoFarm.class);
        int totalCount = 0;
        for (int i = 0; i < AutoFarmCounter.mc.player.getInventory().main.size(); ++i) {
            ItemStack stack = (ItemStack)AutoFarmCounter.mc.player.getInventory().main.get(i);
            if (stack.isEmpty()) continue;
            if (autoFarm.getSweetBerries().isSelected() && stack.getItem() == Items.SWEET_BERRIES) {
                totalCount += stack.getCount();
                continue;
            }
            if (autoFarm.getCarrot().isSelected() && stack.getItem() == Items.CARROT) {
                totalCount += stack.getCount();
                continue;
            }
            if (autoFarm.getPotato().isSelected() && stack.getItem() == Items.POTATO) {
                totalCount += stack.getCount();
                continue;
            }
            if (!autoFarm.getBeetroot().isSelected() || stack.getItem() != Items.BEETROOT_SEEDS) continue;
            totalCount += stack.getCount();
        }
        ItemStack offHandStack = AutoFarmCounter.mc.player.getOffHandStack();
        if (!offHandStack.isEmpty()) {
            if (autoFarm.getSweetBerries().isSelected() && offHandStack.getItem() == Items.SWEET_BERRIES) {
                totalCount += offHandStack.getCount();
            } else if (autoFarm.getCarrot().isSelected() && offHandStack.getItem() == Items.CARROT) {
                totalCount += offHandStack.getCount();
            } else if (autoFarm.getPotato().isSelected() && offHandStack.getItem() == Items.POTATO) {
                totalCount += offHandStack.getCount();
            } else if (autoFarm.getBeetroot().isSelected() && offHandStack.getItem() == Items.BEETROOT_SEEDS) {
                totalCount += offHandStack.getCount();
            }
        }
        return totalCount;
    }

    public int getNonSelectedCropSlots() {
        AutoFarm autoFarm = Rockstar.getInstance().getModuleManager().getModule(AutoFarm.class);
        int nonSelectedSlots = 0;
        for (int i = 0; i < AutoFarmCounter.mc.player.getInventory().main.size(); ++i) {
            boolean isSelectedCrop;
            ItemStack stack = (ItemStack)AutoFarmCounter.mc.player.getInventory().main.get(i);
            if (stack.isEmpty()) continue;
            boolean bl = isSelectedCrop = autoFarm.getSweetBerries().isSelected() && stack.getItem() == Items.SWEET_BERRIES || autoFarm.getCarrot().isSelected() && stack.getItem() == Items.CARROT || autoFarm.getPotato().isSelected() && stack.getItem() == Items.POTATO || autoFarm.getBeetroot().isSelected() && stack.getItem() == Items.BEETROOT_SEEDS;
            if (isSelectedCrop) continue;
            ++nonSelectedSlots;
        }
        ItemStack offHandStack = AutoFarmCounter.mc.player.getOffHandStack();
        if (!offHandStack.isEmpty()) {
            boolean isSelectedCrop;
            boolean bl = isSelectedCrop = autoFarm.getSweetBerries().isSelected() && offHandStack.getItem() == Items.SWEET_BERRIES || autoFarm.getCarrot().isSelected() && offHandStack.getItem() == Items.CARROT || autoFarm.getPotato().isSelected() && offHandStack.getItem() == Items.POTATO || autoFarm.getBeetroot().isSelected() && offHandStack.getItem() == Items.BEETROOT_SEEDS;
            if (!isSelectedCrop) {
                ++nonSelectedSlots;
            }
        }
        return nonSelectedSlots;
    }

    public void checkPrice() {
        AutoFarm autoFarm = Rockstar.getInstance().getModuleManager().getModule(AutoFarm.class);
        List<Item> items = List.of(Items.SWEET_BERRIES, Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS);
        if (AutoFarmCounter.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler && this.price == 0) {
            if (ServerUtility.isFT() ? AutoFarmCounter.mc.currentScreen.getTitle().getString().equals("\u25cf \u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0441\u0435\u043a\u0446\u0438\u044e") : AutoFarmCounter.mc.currentScreen.getTitle().getString().equals("\u25cf \u0412\u044b\u0431\u0435\u0440\u0438 \u0441\u0435\u043a\u0446\u0438\u044e")) {
                AutoFarmCounter.mc.interactionManager.clickSlot(AutoFarmCounter.mc.player.currentScreenHandler.syncId, 14, 0, SlotActionType.PICKUP, (PlayerEntity)AutoFarmCounter.mc.player);
            }
            if (AutoFarmCounter.mc.currentScreen.getTitle().getString().equals("\u0421\u043a\u0443\u043f\u0449\u0438\u043a \u0435\u0434\u044b") && this.timer.finished(40L)) {
                ItemStack itemStack = AutoFarmCounter.mc.player.currentScreenHandler.getSlot(autoFarm.ssItem()).getStack();
                List<Text> tooltip = itemStack.getTooltip(Item.TooltipContext.DEFAULT, (PlayerEntity)AutoFarmCounter.mc.player, (TooltipType)TooltipType.BASIC);
                for (Text line : tooltip) {
                    Pattern pattern;
                    Matcher matcher;
                    String text = line.getString();
                    if (!text.contains("\u0426\u0435\u043d\u0430 \u0437\u0430") || !text.contains("$") || !(matcher = (pattern = Pattern.compile("(\\d+)\\$")).matcher(text)).find()) continue;
                    this.price = Integer.parseInt(matcher.group(1));
                }
                AutoFarmCounter.mc.player.closeHandledScreen();
                this.timer.reset();
            }
        } else if (this.timer.finished(120L) && this.price == 0) {
            AutoFarmCounter.mc.player.networkHandler.sendChatCommand("buyer");
            this.timer.reset();
        }
    }

    @Generated
    public Timer getTimer() {
        return this.timer;
    }

    @Generated
    public int getPrice() {
        return this.price;
    }
}
