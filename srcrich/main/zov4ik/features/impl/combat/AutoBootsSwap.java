package im.zov4ik.features.impl.combat;

import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import im.zov4ik.events.keyboard.KeyEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BindSetting;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.utils.client.managers.event.EventHandler;

import java.util.Comparator;
import java.util.List;

public class AutoBootsSwap extends Module {
    private final SelectSetting firstItem = new SelectSetting("First Item", "Select first boots type")
            .value("Netherite Boots", "Diamond Boots", "Iron Boots", "Golden Boots", "Chainmail Boots", "Leather Boots")
            .selected("Netherite Boots");

    private final SelectSetting secondItem = new SelectSetting("Second Item", "Select second boots type")
            .value("Netherite Boots", "Diamond Boots", "Iron Boots", "Golden Boots", "Chainmail Boots", "Leather Boots")
            .selected("Diamond Boots");

    private final BindSetting bind = new BindSetting("Item use key", "Swap boots on key");

    private enum Phase { READY, SLOWING_DOWN, WAITING_STOP, SWAP, SPEEDING_UP, FINISHED }

    private Phase phase = Phase.READY;
    private int targetSlotId = -1;
    private long actionStartTime;
    private boolean keysOverridden;
    private boolean wasForwardPressed;
    private boolean wasBackPressed;
    private boolean wasLeftPressed;
    private boolean wasRightPressed;
    private boolean wasJumpPressed;

    public AutoBootsSwap() {
        super("AutoBootsSwap", ModuleCategory.COMBAT);
        setup(firstItem, secondItem, bind);
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (!e.isKeyReleased(bind.getKey())) {
            return;
        }
        if (phase != Phase.READY) {
            return;
        }
        startAction();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (phase == Phase.READY) {
            return;
        }
        handleAction();
    }

    private void startAction() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) {
            return;
        }

        Slot targetSlot = findTargetSlot();
        if (targetSlot == null) {
            return;
        }
        targetSlotId = targetSlot.id;

        long handle = mc.getWindow().getHandle();
        wasForwardPressed = InputUtil.isKeyPressed(handle, mc.options.forwardKey.getDefaultKey().getCode());
        wasBackPressed = InputUtil.isKeyPressed(handle, mc.options.backKey.getDefaultKey().getCode());
        wasLeftPressed = InputUtil.isKeyPressed(handle, mc.options.leftKey.getDefaultKey().getCode());
        wasRightPressed = InputUtil.isKeyPressed(handle, mc.options.rightKey.getDefaultKey().getCode());
        wasJumpPressed = InputUtil.isKeyPressed(handle, mc.options.jumpKey.getDefaultKey().getCode());

        phase = Phase.SLOWING_DOWN;
        actionStartTime = System.currentTimeMillis();
        keysOverridden = false;
    }

    private void handleAction() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) {
            resetAction();
            return;
        }

        long elapsed = System.currentTimeMillis() - actionStartTime;
        switch (phase) {
            case SLOWING_DOWN -> {
                if (mc.player.input != null) {
                    mc.player.input.movementForward = 0.0F;
                    mc.player.input.movementSideways = 0.0F;
                }
                mc.player.setSprinting(false);
                if (!keysOverridden) {
                    mc.options.forwardKey.setPressed(false);
                    mc.options.backKey.setPressed(false);
                    mc.options.leftKey.setPressed(false);
                    mc.options.rightKey.setPressed(false);
                    mc.options.jumpKey.setPressed(false);
                    keysOverridden = true;
                }
                if (elapsed > 1L) {
                    phase = Phase.WAITING_STOP;
                    actionStartTime = System.currentTimeMillis();
                }
            }
            case WAITING_STOP -> {
                if (mc.player.input != null) {
                    mc.player.input.movementForward = 0.0F;
                    mc.player.input.movementSideways = 0.0F;
                }
                double vx = Math.abs(mc.player.getVelocity().x);
                double vz = Math.abs(mc.player.getVelocity().z);
                double vy = Math.abs(mc.player.getVelocity().y);
                if ((vx < 0.005D && vz < 0.005D && vy < 0.005D) || elapsed > 20L) {
                    phase = Phase.SWAP;
                }
            }
            case SWAP -> {
                performSwap();
                phase = Phase.SPEEDING_UP;
                actionStartTime = System.currentTimeMillis();
                restoreKeyStates();
            }
            case SPEEDING_UP -> {
                if (System.currentTimeMillis() - actionStartTime > 25L) {
                    phase = Phase.FINISHED;
                }
            }
            case FINISHED -> resetAction();
            case READY -> {
            }
        }
    }

    private void performSwap() {
        if (targetSlotId == -1 || mc.interactionManager == null || mc.player == null) {
            return;
        }

        Slot slot = mc.player.currentScreenHandler.slots.stream()
                .filter(s -> s.id == targetSlotId)
                .findFirst()
                .orElse(null);
        if (slot == null) {
            return;
        }

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot.id, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 8, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot.id, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, mc.player);
    }

    private Slot findTargetSlot() {
        Item first = mapBoots(firstItem.getSelected());
        Item second = mapBoots(secondItem.getSelected());
        Item equipped = mc.player.getInventory().getArmorStack(0).getItem();

        Item desired = equipped.equals(first) ? second : first;
        Slot slot = findBestSlot(desired);
        if (slot != null) {
            return slot;
        }

        Item fallback = desired.equals(first) ? second : first;
        return findBestSlot(fallback);
    }

    private Slot findBestSlot(Item item) {
        if (item == Items.AIR) {
            return null;
        }

        List<Slot> hotbar = mc.player.currentScreenHandler.slots.stream()
                .filter(s -> s.id != 8 && s.id >= 36 && s.id <= 44)
                .filter(s -> s.getStack().getItem().equals(item))
                .sorted(Comparator.comparingInt(s -> s.id))
                .toList();
        if (!hotbar.isEmpty()) {
            return hotbar.getFirst();
        }

        List<Slot> inventory = mc.player.currentScreenHandler.slots.stream()
                .filter(s -> s.id != 8 && s.id >= 9 && s.id <= 35)
                .filter(s -> s.getStack().getItem().equals(item))
                .sorted(Comparator.comparingInt(s -> s.id))
                .toList();
        return inventory.isEmpty() ? null : inventory.getFirst();
    }

    private Item mapBoots(String name) {
        return switch (name) {
            case "Netherite Boots" -> Items.NETHERITE_BOOTS;
            case "Diamond Boots" -> Items.DIAMOND_BOOTS;
            case "Iron Boots" -> Items.IRON_BOOTS;
            case "Golden Boots" -> Items.GOLDEN_BOOTS;
            case "Chainmail Boots" -> Items.CHAINMAIL_BOOTS;
            case "Leather Boots" -> Items.LEATHER_BOOTS;
            default -> Items.AIR;
        };
    }

    private void restoreKeyStates() {
        if (!keysOverridden) {
            return;
        }
        long handle = mc.getWindow().getHandle();
        boolean currentForward = InputUtil.isKeyPressed(handle, mc.options.forwardKey.getDefaultKey().getCode());
        boolean currentBack = InputUtil.isKeyPressed(handle, mc.options.backKey.getDefaultKey().getCode());
        boolean currentLeft = InputUtil.isKeyPressed(handle, mc.options.leftKey.getDefaultKey().getCode());
        boolean currentRight = InputUtil.isKeyPressed(handle, mc.options.rightKey.getDefaultKey().getCode());
        boolean currentJump = InputUtil.isKeyPressed(handle, mc.options.jumpKey.getDefaultKey().getCode());

        mc.options.forwardKey.setPressed(wasForwardPressed && currentForward);
        mc.options.backKey.setPressed(wasBackPressed && currentBack);
        mc.options.leftKey.setPressed(wasLeftPressed && currentLeft);
        mc.options.rightKey.setPressed(wasRightPressed && currentRight);
        mc.options.jumpKey.setPressed(wasJumpPressed && currentJump);
        keysOverridden = false;
    }

    private void resetAction() {
        restoreKeyStates();
        phase = Phase.READY;
        targetSlotId = -1;
    }

    @Override
    public void deactivate() {
        resetAction();
        super.deactivate();
    }
}
