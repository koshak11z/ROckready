package im.zov4ik.utils.interactions.inv;

import im.zov4ik.features.impl.movement.InventoryMove;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.interactions.simulate.Simulations;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.SlotActionType;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.utils.math.task.TaskPriority;
import im.zov4ik.utils.math.script.Script;
import im.zov4ik.utils.client.packet.network.Network;
import im.zov4ik.events.player.InputEvent;
import im.zov4ik.utils.features.aura.utils.MathAngle;
import im.zov4ik.utils.features.aura.warp.TurnsConfig;
import im.zov4ik.utils.features.aura.warp.TurnsConnection;
import im.zov4ik.display.screens.clickgui.MenuScreen;

import java.util.List;

@UtilityClass
public class InventoryFlowManager implements QuickImports {
    public final List<KeyBinding> moveKeys = List.of(mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey);
    public static final Script script = new Script(), postScript = new Script();
    public boolean canMove = true;
    private boolean executingTask = false;

    public void tick() {
        script.update();
    }

    public void postMotion() {
        postScript.update();
    }

    public void input(InputEvent e) {
        if (!canMove) e.inputNone();
    }

    public void addTask(Runnable task) {
        if (useLegitInventorySync()) {
            queueLegitTask(task);
            return;
        }

        if (script.isFinished() && Simulations.hasPlayerMovement()) {
            switch (Network.server) {
                case "FunTime" -> {
                    script.cleanup().addTickStep(0, () -> {
                        InventoryFlowManager.disableMoveKeys();
                        InventoryFlowManager.rotateToCamera();
                    }).addTickStep(1, () -> {
                        task.run();
                        enableMoveKeys();
                    });
                    return;
                }
                case "ReallyWorld" -> {
                    if (mc.player.isOnGround()) {
                        script.cleanup().addTickStep(0, InventoryFlowManager::disableMoveKeys).addTickStep(2, InventoryFlowManager::rotateToCamera).addTickStep(3, task::run)
                                .addTickStep(4, InventoryFlowManager::enableMoveKeys);
                        return;
                    }
                }
                case "SpookyTime", "CopyTime" -> {
                    script.cleanup().addTickStep(0, ()-> {
                                InventoryFlowManager.disableMoveKeys();
                                InventoryFlowManager.rotateToCamera();
                            }).addTickStep(1, task::run)
                            .addTickStep(2, InventoryFlowManager::enableMoveKeys);
                    return;
                }
            }
        }
        script.addTickStep(0, InventoryFlowManager::rotateToCamera);
        postScript.cleanup().addTickStep(0, () -> {
            task.run();
            InventoryTask.closeScreen(true);
        });
    }

    public void addUseItemTask(int sourceSlotId, int targetHotbarSlot, Runnable useAction) {
        addUseItemTask(sourceSlotId, targetHotbarSlot, useAction, null);
    }

    public void addUseItemTask(int sourceSlotId, int targetHotbarSlot, Runnable useAction, Runnable afterUse) {
        if (mc.player == null) {
            return;
        }

        int selectedSlot = mc.player.getInventory().selectedSlot;
        int normalizedSource = normalizeScreenSlot(sourceSlotId);
        int selectedHotbarSlot = Math.max(0, Math.min(8, targetHotbarSlot));
        int selectedScreenSlot = selectedHotbarSlot + 36;
        boolean requiresSwap = normalizedSource != selectedScreenSlot;
        boolean sendClosePacket = mc.currentScreen != null;

        script.cleanup()
                .addStep(0, () -> {
                    disableMoveKeys();
                    rotateToCamera();
                    if (mc.player != null && mc.player.isSprinting()) {
                        mc.player.setSprinting(false);
                    }
                })
                .addStep(3, () -> {
                    executingTask = true;
                    try {
                        if (requiresSwap) {
                            InventoryTask.clickSlot(normalizedSource, selectedHotbarSlot, SlotActionType.SWAP, false);
                        }
                        if (sendClosePacket) {
                            InventoryTask.closeScreen(true);
                        }
                    } finally {
                        executingTask = false;
                    }
                })
                .addStep(3, () -> {
                    executingTask = true;
                    try {
                        useAction.run();
                    } finally {
                        executingTask = false;
                    }
                })
                .addStep(5, () -> {
                    executingTask = true;
                    try {
                        if (requiresSwap) {
                            InventoryTask.clickSlot(normalizedSource, selectedHotbarSlot, SlotActionType.SWAP, false);
                        }
                        if (afterUse != null) {
                            afterUse.run();
                        }
                        if (mc.player.getInventory().selectedSlot != selectedSlot) {
                            InventoryTask.switchTo(selectedSlot);
                        }
                    } finally {
                        executingTask = false;
                    }
                })
                .addStep(3, () -> {
                    if (mc.player != null && mc.player.input != null) {
                        mc.player.input.movementForward = 0;
                        mc.player.input.movementSideways = 0;
                    }
                })
                .addStep(5, InventoryFlowManager::enableMoveKeys);
    }

    public boolean isExecutingTask() {
        return executingTask;
    }

    public boolean useLegitInventorySync() {
        InventoryMove inventoryMove = InventoryMove.getInstance();
        return inventoryMove != null && inventoryMove.isLegitMode();
    }

    private void queueLegitTask(Runnable task) {
        script.cleanup()
                .addStep(0, () -> {
                    disableMoveKeys();
                    rotateToCamera();
                    if (mc.player != null && mc.player.isSprinting()) {
                        mc.player.setSprinting(false);
                    }
                })
                .addStep(3, () -> {
                    executingTask = true;
                    try {
                        task.run();
                        InventoryTask.closeScreen(true);
                    } finally {
                        executingTask = false;
                    }
                })
                .addStep(3, InventoryFlowManager::enableMoveKeys);
    }

    private int normalizeScreenSlot(int slotId) {
        if (slotId >= 0 && slotId <= 8) {
            return slotId + 36;
        }
        return slotId;
    }

    private void rotateToCamera() {
        Module module = new Module("InventoryComponent","Inventory Component", ModuleCategory.PLAYER);
        module.state = true;
        TurnsConnection.INSTANCE.rotateTo(MathAngle.cameraAngle(), TurnsConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_3, module);
    }

    public void disableMoveKeys() {
        canMove = false;
        unPressMoveKeys();
    }

    public void enableMoveKeys() {
        InventoryTask.closeScreen(true);
        canMove = true;
        updateMoveKeys();
    }

    public void unPressMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(false));
    }

    public void updateMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyBinding.getDefaultKey().getCode())));
    }

    public boolean shouldSkipExecution() {
        return mc.currentScreen != null && !PlayerInteractionHelper.isChat(mc.currentScreen) && !(mc.currentScreen instanceof SignEditScreen) && !(mc.currentScreen instanceof AnvilScreen)
                && !(mc.currentScreen instanceof AbstractCommandBlockScreen) && !(mc.currentScreen instanceof StructureBlockScreen) && !(mc.currentScreen instanceof MenuScreen);
    }
}
