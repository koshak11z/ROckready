package im.zov4ik.features.impl.movement;

import antidaunleak.api.annotation.Native;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.interactions.inv.InventoryFlowManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.util.InputUtil;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.interactions.simulate.Simulations;

import im.zov4ik.utils.interactions.inv.InventoryTask;
import im.zov4ik.events.container.CloseScreenEvent;
import im.zov4ik.events.item.ClickSlotEvent;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;

import java.util.*;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryMove extends Module {
    public static InventoryMove getInstance() {
        return Instance.get(InventoryMove.class);
    }

    // ==========================================================================
    //  НАСТРОЙКИ СКОРОСТИ — МЕНЯЙ ТОЛЬКО ЗДЕСЬ
    // --------------------------------------------------------------------------
    //  slowdownMs        — сколько мс держим игрока на месте ПЕРЕД отправкой
    //                      пакетов (фаза SLOWING_DOWN). Меньше = быстрее реакция.
    //  speedupDurationMs — за сколько мс разгоняемся обратно до полной скорости
    //                      (фаза SPEEDING_UP). МЕНЬШЕ = БЫСТРЕЕ разгон.
    //  speedupLerp       — плавность набора скорости (0..1). БОЛЬШЕ = резче.
    //  sprintAt          — при каком прогрессе разгона (0..1) включать спринт.
    //                      МЕНЬШЕ = спринт включается раньше.
    // ==========================================================================

    // ----- Обычный Legit (как было) -----
    private static final long  LEGIT_SLOWDOWN_MS        = 1L;
    private static final float LEGIT_SPEEDUP_DURATION_MS = 1.0f;
    private static final float LEGIT_SPEEDUP_LERP        = 0.4f;
    private static final float LEGIT_SPRINT_AT           = 0.5f;

    // ----- LegitHW (чуть-чуть быстрее) -----
    // Хочешь ещё быстрее — уменьшай SLOWDOWN/DURATION и увеличивай LERP,
    // а SPRINT_AT уменьшай (спринт раньше).
    private static final long  LEGITHW_SLOWDOWN_MS        = 0L;   // почти без стопа
    private static final float LEGITHW_SPEEDUP_DURATION_MS = 0.5f; // разгон вдвое быстрее
    private static final float LEGITHW_SPEEDUP_LERP        = 0.6f; // резче набор скорости
    private static final float LEGITHW_SPRINT_AT           = 0.3f; // спринт раньше

    // ==========================================================================

    private final List<Packet<?>> packets = new ArrayList<>();
    private final SelectSetting mode = new SelectSetting("Режим", "Выберите режим передвижения в инвентаре")
            .value("Normal", "Legit", "LegitHW") // <-- добавлен LegitHW
            .selected("Legit");

    enum MovePhase { READY, SLOWING_DOWN, ALLOW_MOVEMENT, SPEEDING_UP, SEND_PACKETS, FINISHED }
    MovePhase movePhase = MovePhase.READY;
    long actionStartTime = 0L;
    boolean playerFullyStopped = false;
    boolean wasForwardPressed, wasBackPressed, wasLeftPressed, wasRightPressed, wasJumpPressed;
    boolean keysOverridden = false;
    boolean inventoryOpened = false;
    boolean packetsHeld = false;
    boolean inventoryActionPerformed = false;
    ClientCommandC2SPacket heldOpenInventoryPacket = null;
    boolean suppressNoActionClosePacket = false;

    public InventoryMove() {
        super("InventoryMove", "Inventory Move", ModuleCategory.MOVEMENT);
        setup(mode);
    }

    // true и для Legit, и для LegitHW — логика у них общая
    public boolean isLegitMode() {
        return mode.isSelected("Legit") || mode.isSelected("LegitHW");
    }

    // true только для ускоренного режима
    public boolean isLegitHwMode() {
        return mode.isSelected("LegitHW");
    }

    // --- Хелперы: выбирают набор параметров в зависимости от режима ---
    private long slowdownMs() {
        return isLegitHwMode() ? LEGITHW_SLOWDOWN_MS : LEGIT_SLOWDOWN_MS;
    }

    private float speedupDurationMs() {
        // защита от деления на 0 ниже по коду
        return isLegitHwMode() ? LEGITHW_SPEEDUP_DURATION_MS : LEGIT_SPEEDUP_DURATION_MS;
    }

    private float speedupLerp() {
        return isLegitHwMode() ? LEGITHW_SPEEDUP_LERP : LEGIT_SPEEDUP_LERP;
    }

    private float sprintAt() {
        return isLegitHwMode() ? LEGITHW_SPRINT_AT : LEGIT_SPRINT_AT;
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (!isLegitMode()) {
            return;
        }

        if (e.isSend()) {
            switch (e.getPacket()) {
                case ClientCommandC2SPacket command when command.getMode() == ClientCommandC2SPacket.Mode.OPEN_INVENTORY -> {
                    heldOpenInventoryPacket = command;
                    suppressNoActionClosePacket = true;
                    e.cancel();
                }
                case CloseHandledScreenC2SPacket ignored when suppressNoActionClosePacket && !inventoryActionPerformed && !packetsHeld -> {
                    heldOpenInventoryPacket = null;
                    suppressNoActionClosePacket = false;
                    e.cancel();
                }
                case ClickSlotC2SPacket slot -> {
                    flushHeldOpenInventoryPacket();
                    if ((packetsHeld || Simulations.hasPlayerMovement()) && InventoryFlowManager.shouldSkipExecution()) {
                        packets.add(slot);
                        e.cancel();
                        packetsHeld = true;
                        inventoryActionPerformed = true;
                    }
                }
                default -> {
                }
            }
            return;
        }

        switch (e.getPacket()) {
            case CloseScreenS2CPacket screen when screen.getSyncId() == 0 -> e.cancel();
            default -> {
            }
        }
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (isLegitMode()) {
            processLegitMovement();
        } else {
            if (!InventoryTask.isServerScreen() && InventoryFlowManager.shouldSkipExecution()) {
                InventoryFlowManager.updateMoveKeys();
            }
        }
    }

    private void processLegitMovement() {
        boolean hasOpenScreen = mc.currentScreen != null;

        if (hasOpenScreen && !inventoryOpened && movePhase == MovePhase.READY) {
            startLegitMovement();
            inventoryOpened = true;
        }

        if (!hasOpenScreen && inventoryOpened) {
            if (packetsHeld && movePhase == MovePhase.ALLOW_MOVEMENT) {
                movePhase = MovePhase.SLOWING_DOWN;
                actionStartTime = System.currentTimeMillis();
            } else if (!packetsHeld && movePhase == MovePhase.ALLOW_MOVEMENT) {
                resetState();
            }
            inventoryOpened = false;
            if (movePhase == MovePhase.READY) {
                return;
            }
        }

        if (movePhase != MovePhase.READY) {
            handleMovementStates();
        }
    }

    private void startLegitMovement() {
        wasForwardPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
        wasBackPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
        wasLeftPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
        wasRightPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
        wasJumpPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode());

        movePhase = MovePhase.ALLOW_MOVEMENT;
        keysOverridden = false;
        packetsHeld = false;
        inventoryActionPerformed = false;
    }

    private void handleMovementStates() {
        long elapsed = System.currentTimeMillis() - actionStartTime;

        switch (movePhase) {
            case SLOWING_DOWN -> {
                if (mc.player != null && mc.player.input != null) {
                    mc.player.input.movementForward = 0;
                    mc.player.input.movementSideways = 0;
                }

                if (!keysOverridden) {
                    mc.options.forwardKey.setPressed(false);
                    mc.options.backKey.setPressed(false);
                    mc.options.leftKey.setPressed(false);
                    mc.options.rightKey.setPressed(false);
                    mc.options.jumpKey.setPressed(false);
                    keysOverridden = true;
                }

                // <-- ДЛИТЕЛЬНОСТЬ СТОПА: для LegitHW короче (slowdownMs)
                if (elapsed > slowdownMs()) {
                    movePhase = MovePhase.SEND_PACKETS;
                    actionStartTime = System.currentTimeMillis();
                }
            }

            case ALLOW_MOVEMENT -> {
                if (!InventoryTask.isServerScreen() && InventoryFlowManager.shouldSkipExecution()) {
                    InventoryFlowManager.updateMoveKeys();
                }
            }

            case SEND_PACKETS -> {
                if (!packets.isEmpty()) {
                    Blink.getInstance().flushForInventorySync();
                    packets.forEach(PlayerInteractionHelper::sendPacketWithOutEvent);
                    packets.clear();
                    InventoryTask.updateSlots();
                }
                packetsHeld = false;
                movePhase = MovePhase.SPEEDING_UP;
                actionStartTime = System.currentTimeMillis();
            }

            case SPEEDING_UP -> {
                long speedupElapsed = System.currentTimeMillis() - actionStartTime;

                // ДЛИТЕЛЬНОСТЬ РАЗГОНА: для LegitHW меньше => прогресс растёт быстрее
                float duration = Math.max(0.001f, speedupDurationMs()); // защита от деления на 0
                float speedupProgress = Math.min(1.0f, speedupElapsed / duration);

                if (keysOverridden) {
                    restoreKeyStates();
                }

                if (mc.player != null && mc.player.input != null) {
                    boolean forward = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
                    float targetForward = forward ? 1.0f : 0;
                    // РЕЗКОСТЬ НАБОРА СКОРОСТИ: speedupLerp() (для LegitHW больше)
                    mc.player.input.movementForward = lerp(mc.player.input.movementForward, targetForward * speedupProgress, speedupLerp());

                    // ПОРОГ ВКЛЮЧЕНИЯ СПРИНТА: sprintAt() (для LegitHW раньше)
                    if (speedupProgress > sprintAt() && forward && !mc.player.isSprinting()) {
                        mc.player.setSprinting(true);
                    }
                }

                // Завершаем разгон, когда прошли всю длительность
                if (speedupElapsed >= duration) {
                    movePhase = MovePhase.FINISHED;
                }
            }

            case FINISHED -> {
                resetState();
            }
        }
    }

    private void restoreKeyStates() {
        boolean currentForward = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
        boolean currentBack = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
        boolean currentLeft = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
        boolean currentRight = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
        boolean currentJump = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode());

        mc.options.forwardKey.setPressed(wasForwardPressed && currentForward);
        mc.options.backKey.setPressed(wasBackPressed && currentBack);
        mc.options.leftKey.setPressed(wasLeftPressed && currentLeft);
        mc.options.rightKey.setPressed(wasRightPressed && currentRight);
        mc.options.jumpKey.setPressed(wasJumpPressed && currentJump);
        keysOverridden = false;
    }

    private float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    private void resetState() {
        if (keysOverridden) {
            restoreKeyStates();
        }
        movePhase = MovePhase.READY;
        playerFullyStopped = false;
        inventoryOpened = false;
        packetsHeld = false;
        inventoryActionPerformed = false;
        heldOpenInventoryPacket = null;
        suppressNoActionClosePacket = false;
        packets.clear();
    }

    private void flushHeldOpenInventoryPacket() {
        if (heldOpenInventoryPacket == null) {
            return;
        }
        PlayerInteractionHelper.sendPacketWithOutEvent(heldOpenInventoryPacket);
        heldOpenInventoryPacket = null;
        suppressNoActionClosePacket = false;
    }

    @EventHandler
    public void onClickSlot(ClickSlotEvent e) {

        if (isLegitMode()) {
            inventoryActionPerformed = true;
            flushHeldOpenInventoryPacket();
            SlotActionType actionType = e.getActionType();
            if ((packetsHeld || Simulations.hasPlayerMovement()) && ((e.getButton() == 1 && !actionType.equals(SlotActionType.SWAP) && !actionType.equals(SlotActionType.THROW)) || actionType.equals(SlotActionType.PICKUP_ALL))) {
                e.cancel();
            }
        }
    }

    @EventHandler
    public void onCloseScreen(CloseScreenEvent e) {
        if (!isLegitMode()) {
            return;
        }

        if (!inventoryActionPerformed && !packetsHeld && e.getScreen() instanceof InventoryScreen && mc.player != null) {
            heldOpenInventoryPacket = null;
            if (keysOverridden) {
                restoreKeyStates();
            }
            movePhase = MovePhase.READY;
            playerFullyStopped = false;
            inventoryOpened = false;
            packetsHeld = false;
            inventoryActionPerformed = false;
            packets.clear();
            return;
        }

        if (packetsHeld && movePhase == MovePhase.ALLOW_MOVEMENT) {
            movePhase = MovePhase.SLOWING_DOWN;
            actionStartTime = System.currentTimeMillis();
        }
    }
}