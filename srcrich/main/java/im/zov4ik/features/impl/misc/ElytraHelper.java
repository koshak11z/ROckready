package im.zov4ik.features.impl.misc;

import antidaunleak.api.annotation.Native;
import im.zov4ik.events.keyboard.KeyEvent;
import im.zov4ik.events.player.InputEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.impl.movement.AutoSprint;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BindSetting;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.interactions.inv.InventoryFlowManager;
import im.zov4ik.utils.interactions.inv.InventoryTask;
import im.zov4ik.utils.interactions.inv.InventoryToolkit;
import im.zov4ik.utils.math.script.Script;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ElytraHelper extends Module {

    final BindSetting elytraSetting = new BindSetting("Замена элитр", "Меняет нагрудник на элитры");
    final BindSetting fireworkSetting = new BindSetting("Использовать фейерверк", "Меняет и использует фейерверки");
    final BooleanSetting startSetting = new BooleanSetting("Быстрый старт", "При замене на элитры автоматически взлетает и использует фейерверки").setValue(false);
    final BooleanSetting recast = new BooleanSetting("Авто взлет", "Автоматически начинает полет").setValue(false);
    final BooleanSetting autoFireworkSetting = new BooleanSetting("Авто фейерверк", "Автоматически использовать фейерверк каждые X мс").setValue(false);
    final SliderSettings fireworkDelay = new SliderSettings("Задержка использования в мс", "Задержка использования фейерверка в мс")
            .setValue(500F).range(10F, 1500F).visible(() -> autoFireworkSetting.isValue());

    final Script script = new Script();

    enum ElytraPhase { READY, SLOWING_DOWN, WAITING_STOP, SWAP, SPEEDING_UP, FINISHED }
    ElytraPhase elytraPhase = ElytraPhase.READY;
    long actionStartTime = 0L;
    Slot targetSlot = null;
    boolean playerFullyStopped = false;
    boolean wasForwardPressed, wasBackPressed, wasLeftPressed, wasRightPressed, wasJumpPressed;
    boolean keysOverridden = false;

    enum FireworkPhase { READY, START }
    FireworkPhase fireworkPhase = FireworkPhase.READY;
    int originalHotbarSlot = -1;
    long lastAutoFireworkTime = 0L;

    public ElytraHelper() {
        super("ElytraHelper", "Elytra Helper", ModuleCategory.MISC);
        setup(elytraSetting, fireworkSetting, startSetting, recast, autoFireworkSetting, fireworkDelay);
    }

    @EventHandler
    public void onInput(InputEvent e) {
        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA) && recast.isValue()) {
            if (mc.player.isOnGround()) e.setJumping(true);
            else if (!mc.player.isGliding()) PlayerInteractionHelper.startFallFlying();
        }
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (!script.isFinished()) return;

        if (e.isKeyDown(elytraSetting.getKey())) {
            if (!InventoryTask.useInventoryMoveLegitMode()) {
                executeDefaultSwap();
            } else if (elytraPhase == ElytraPhase.READY) {
                startLegitSwap();
            }
        } else if (e.isKeyDown(fireworkSetting.getKey()) && mc.player.isGliding() && fireworkPhase == FireworkPhase.READY) {
            fireworkPhase = FireworkPhase.START;
        }
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        script.update();

        if (elytraPhase != ElytraPhase.READY) {
            processLegitSwap();
        }

        if (fireworkPhase != FireworkPhase.READY) {
            processFireworkUsage();
        }

        if (autoFireworkSetting.isValue() && mc.player != null && mc.player.isGliding()) {
            long now = System.currentTimeMillis();
            if (now - lastAutoFireworkTime >= (long) fireworkDelay.getValue()
                    && fireworkPhase == FireworkPhase.READY
                    && elytraPhase == ElytraPhase.READY) {
                if (useFireworkWithFastSwap(false)) {
                    lastAutoFireworkTime = now;
                }
            }
        }
    }

    public boolean fireWorkMethod() {
        if (mc.player == null || mc.world == null || !mc.player.isGliding()) {
            return false;
        }
        if (!script.isFinished() || !InventoryFlowManager.script.isFinished()) {
            return false;
        }
        if (fireworkPhase != FireworkPhase.READY || elytraPhase != ElytraPhase.READY) {
            return false;
        }

        boolean launched = useFireworkWithFastSwap(false);
        if (launched) {
            lastAutoFireworkTime = System.currentTimeMillis();
        }
        return launched;
    }

    private void processFireworkUsage() {
        if (mc.player == null || mc.currentScreen != null || !InventoryFlowManager.script.isFinished()) {
            resetFireworkState();
            return;
        }

        useFireworkWithFastSwap(true);
        resetFireworkState();
    }

    private void resetFireworkState() {
        if (originalHotbarSlot != -1 && mc.player != null) {
            InventoryToolkit.switchTo(originalHotbarSlot);
        }
        fireworkPhase = FireworkPhase.READY;
        originalHotbarSlot = -1;
    }

    private void executeDefaultSwap() {
        Slot slot = chestPlate();

        if (slot != null) {
            Slot fireWork = InventoryTask.getSlot(Items.FIREWORK_ROCKET);
            boolean elytra = slot.getStack().getItem().equals(Items.ELYTRA);
            InventoryTask.moveItem(slot, 6, false, true);

            if (startSetting.isValue() && fireWork != null && elytra) {
                script.cleanup().addTickStep(4, () -> {
                    if (mc.player.isOnGround()) mc.player.jump();
                }).addTickStep(3, () -> {
                    PlayerInteractionHelper.startFallFlying();
                    useFireworkWithFastSwap(false);
                });
            }
        }
    }

    private void startLegitSwap() {
        targetSlot = chestPlate();
        if (targetSlot == null) return;

        wasForwardPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
        wasBackPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
        wasLeftPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
        wasRightPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
        wasJumpPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode());

        elytraPhase = ElytraPhase.SLOWING_DOWN;
        actionStartTime = System.currentTimeMillis();
        playerFullyStopped = false;
        keysOverridden = false;
    }

    private void processLegitSwap() {
        if (mc.player == null || mc.currentScreen != null) {
            resetLegitState();
            return;
        }

        long elapsed = System.currentTimeMillis() - actionStartTime;

        switch (elytraPhase) {
            case SLOWING_DOWN -> {
                mc.player.input.movementForward = 0;
                mc.player.input.movementSideways = 0;
                if (mc.player.isSprinting()) {
                    mc.player.setSprinting(false);
                    AutoSprint.tickStop = 1;
                }
                if (!keysOverridden) {
                    mc.options.forwardKey.setPressed(false);
                    mc.options.backKey.setPressed(false);
                    mc.options.leftKey.setPressed(false);
                    mc.options.rightKey.setPressed(false);
                    mc.options.jumpKey.setPressed(false);
                    keysOverridden = true;
                }
                if (elapsed > 1) {
                    elytraPhase = ElytraPhase.WAITING_STOP;
                }
            }
            case WAITING_STOP -> {
                mc.player.input.movementForward = 0;
                mc.player.input.movementSideways = 0;
                double velocityX = Math.abs(mc.player.getVelocity().x);
                double velocityZ = Math.abs(mc.player.getVelocity().z);
                if (velocityX < 0.001 && velocityZ < 0.001 || elapsed > 1) {
                    playerFullyStopped = true;
                    elytraPhase = ElytraPhase.SWAP;
                }
            }
            case SWAP -> {
                if (playerFullyStopped) {
                    if (targetSlot != null) {
                        boolean elytra = targetSlot.getStack().getItem().equals(Items.ELYTRA);
                        InventoryTask.moveItem(targetSlot, 6, false, false);

                        if (startSetting.isValue() && elytra && InventoryTask.getSlot(Items.FIREWORK_ROCKET) != null) {
                            script.cleanup().addTickStep(2, () -> {
                                if (mc.player.isOnGround()) mc.player.jump();
                            }).addTickStep(1, () -> {
                                PlayerInteractionHelper.startFallFlying();
                                useFireworkWithFastSwap(false);
                            });
                        }
                    }
                    elytraPhase = ElytraPhase.SPEEDING_UP;
                    actionStartTime = System.currentTimeMillis();

                    if (keysOverridden) {
                        restoreKeyStates();
                    }
                }
            }
            case SPEEDING_UP -> {
                long speedupElapsed = System.currentTimeMillis() - actionStartTime;
                float speedupProgress = Math.min(1.0f, speedupElapsed / 1.0f);
                if (mc.player.input != null) {
                    boolean forward = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
                    float targetForward = forward ? 1.0f : 0;
                    mc.player.input.movementForward = lerp(mc.player.input.movementForward, targetForward * speedupProgress, 0.4f);
                    if (speedupProgress > 0.4f && forward && !mc.player.isSprinting()) {
                        mc.player.setSprinting(true);
                    }
                }
                if (speedupElapsed > 1) {
                    elytraPhase = ElytraPhase.FINISHED;
                }
            }
            case FINISHED -> resetLegitState();
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

    private void resetLegitState() {
        if (keysOverridden) {
            restoreKeyStates();
        }
        elytraPhase = ElytraPhase.READY;
        targetSlot = null;
        playerFullyStopped = false;
    }

    private Slot chestPlate() {
        if (Objects.requireNonNull(mc.player).getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
            return InventoryTask.getSlot(List.of(
                    Items.NETHERITE_CHESTPLATE,
                    Items.DIAMOND_CHESTPLATE,
                    Items.IRON_CHESTPLATE,
                    Items.GOLDEN_CHESTPLATE,
                    Items.CHAINMAIL_CHESTPLATE,
                    Items.LEATHER_CHESTPLATE
            ));
        } else {
            return InventoryTask.getSlot(Items.ELYTRA);
        }
    }

    private boolean useFireworkWithFastSwap(boolean notifyIfMissing) {
        Slot fireworkSlot = InventoryTask.getSlot(Items.FIREWORK_ROCKET);
        if (fireworkSlot == null) {
            if (notifyIfMissing) {
                ChatMessage.brandmessage("Нету фейерверков");
            }
            return false;
        }

        originalHotbarSlot = mc.player.getInventory().selectedSlot;
        InventoryFlowManager.addUseItemTask(fireworkSlot.id, originalHotbarSlot, () -> PlayerInteractionHelper.interactItem(Hand.MAIN_HAND));
        return true;
    }
}
