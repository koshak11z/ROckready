package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.CloseScreenEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.ui.components.textfield.TextField;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemGroups;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "InventoryMove", category = ModuleCategory.MOVEMENT, desc = "Передвижение в инвентаре из srcrich")
public class InventoryMove extends BaseModule {
    private static final long LEGIT_SLOWDOWN_MS = 1L;
    private static final float LEGIT_SPEEDUP_DURATION_MS = 1.0f;
    private static final float LEGIT_SPEEDUP_LERP = 0.4f;
    private static final float LEGIT_SPRINT_AT = 0.5f;
    private static final long LEGITHW_SLOWDOWN_MS = 0L;
    private static final float LEGITHW_SPEEDUP_DURATION_MS = 0.5f;
    private static final float LEGITHW_SPEEDUP_LERP = 0.6f;
    private static final float LEGITHW_SPRINT_AT = 0.3f;

    private final List<Packet<?>> packets = new ArrayList<>();
    private final ModeSetting mode = new ModeSetting(this, "Режим");
    private final ModeSetting.Value normal = new ModeSetting.Value(this.mode, "Обычный").select();
    private final ModeSetting.Value legit = new ModeSetting.Value(this.mode, "Legit");
    private final ModeSetting.Value legitHW = new ModeSetting.Value(this.mode, "LegitHW");
    private final SliderSetting legitDelay = new SliderSetting(this, "Legit delay", this.normal::isSelected).min(0.0f).max(200.0f).step(1.0f).currentValue(LEGIT_SLOWDOWN_MS).suffix(" ms");

    private MovePhase movePhase = MovePhase.READY;
    private long actionStartTime;
    private boolean wasForwardPressed;
    private boolean wasBackPressed;
    private boolean wasLeftPressed;
    private boolean wasRightPressed;
    private boolean wasJumpPressed;
    private boolean keysOverridden;
    private boolean inventoryOpened;
    private boolean packetsHeld;
    private boolean inventoryActionPerformed;
    private ClientCommandC2SPacket heldOpenInventoryPacket;
    private boolean suppressNoActionClosePacket;
    private boolean sending;

    private enum MovePhase { READY, SLOWING_DOWN, ALLOW_MOVEMENT, SEND_PACKETS, SPEEDING_UP, FINISHED }

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null || mc.world == null) return;
        if (this.isLegitMode()) this.processLegitMovement();
        else this.updateMoveKeys();
    };

    private final EventListener<SendPacketEvent> onSendPacket = event -> {
        if (this.sending || !this.isLegitMode()) return;
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientCommandC2SPacket command && command.getMode() == ClientCommandC2SPacket.Mode.OPEN_INVENTORY) {
            this.heldOpenInventoryPacket = command;
            this.suppressNoActionClosePacket = true;
            event.cancel();
            return;
        }
        if (packet instanceof CloseHandledScreenC2SPacket && this.suppressNoActionClosePacket && !this.inventoryActionPerformed && !this.packetsHeld) {
            this.heldOpenInventoryPacket = null;
            this.suppressNoActionClosePacket = false;
            event.cancel();
            return;
        }
        if (packet instanceof ClickSlotC2SPacket slot) {
            this.flushHeldOpenInventoryPacket();
            this.inventoryActionPerformed = true;
            if ((this.packetsHeld || this.hasMovementInput()) && this.shouldMoveInScreen()) {
                this.packets.add(slot);
                this.packetsHeld = true;
                event.cancel();
            }
        }
    };

    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        if (this.isLegitMode() && event.getPacket() instanceof CloseScreenS2CPacket screen && screen.getSyncId() == 0) event.cancel();
    };

    private final EventListener<CloseScreenEvent> onCloseScreen = event -> {
        if (!this.isLegitMode()) return;
        if (!this.inventoryActionPerformed && !this.packetsHeld && event.getScreen() instanceof InventoryScreen) {
            this.resetState();
            return;
        }
        if (this.packetsHeld && this.movePhase == MovePhase.ALLOW_MOVEMENT) {
            this.movePhase = MovePhase.SLOWING_DOWN;
            this.actionStartTime = System.currentTimeMillis();
        }
    };

    private boolean isLegitMode() {
        return this.legit.isSelected() || this.legitHW.isSelected();
    }

    private boolean isLegitHwMode() {
        return this.legitHW.isSelected();
    }

    private long slowdownMs() {
        return this.isLegitHwMode() ? LEGITHW_SLOWDOWN_MS : (long)this.legitDelay.getCurrentValue();
    }

    private float speedupDurationMs() {
        return this.isLegitHwMode() ? LEGITHW_SPEEDUP_DURATION_MS : LEGIT_SPEEDUP_DURATION_MS;
    }

    private float speedupLerp() {
        return this.isLegitHwMode() ? LEGITHW_SPEEDUP_LERP : LEGIT_SPEEDUP_LERP;
    }

    private float sprintAt() {
        return this.isLegitHwMode() ? LEGITHW_SPRINT_AT : LEGIT_SPRINT_AT;
    }

    private void processLegitMovement() {
        boolean hasOpenScreen = this.shouldMoveInScreen();
        if (hasOpenScreen && !this.inventoryOpened && this.movePhase == MovePhase.READY) {
            this.startLegitMovement();
            this.inventoryOpened = true;
        }
        if (!hasOpenScreen && this.inventoryOpened) {
            if (this.packetsHeld && this.movePhase == MovePhase.ALLOW_MOVEMENT) {
                this.movePhase = MovePhase.SLOWING_DOWN;
                this.actionStartTime = System.currentTimeMillis();
            } else if (!this.packetsHeld && this.movePhase == MovePhase.ALLOW_MOVEMENT) {
                this.resetState();
            }
            this.inventoryOpened = false;
        }
        if (this.movePhase != MovePhase.READY) this.handleMovementStates();
    }

    private void startLegitMovement() {
        this.wasForwardPressed = this.isPressed(mc.options.forwardKey);
        this.wasBackPressed = this.isPressed(mc.options.backKey);
        this.wasLeftPressed = this.isPressed(mc.options.leftKey);
        this.wasRightPressed = this.isPressed(mc.options.rightKey);
        this.wasJumpPressed = this.isPressed(mc.options.jumpKey);
        this.movePhase = MovePhase.ALLOW_MOVEMENT;
        this.keysOverridden = false;
        this.packetsHeld = false;
        this.inventoryActionPerformed = false;
    }

    private void handleMovementStates() {
        long elapsed = System.currentTimeMillis() - this.actionStartTime;
        switch (this.movePhase) {
            case SLOWING_DOWN -> {
                if (mc.player.input != null) {
                    mc.player.input.movementForward = 0.0f;
                    mc.player.input.movementSideways = 0.0f;
                }
                if (!this.keysOverridden) {
                    mc.options.forwardKey.setPressed(false);
                    mc.options.backKey.setPressed(false);
                    mc.options.leftKey.setPressed(false);
                    mc.options.rightKey.setPressed(false);
                    mc.options.jumpKey.setPressed(false);
                    this.keysOverridden = true;
                }
                if (elapsed > this.slowdownMs()) {
                    this.movePhase = MovePhase.SEND_PACKETS;
                    this.actionStartTime = System.currentTimeMillis();
                }
            }
            case ALLOW_MOVEMENT -> this.updateMoveKeys();
            case SEND_PACKETS -> {
                this.flushPackets();
                this.packetsHeld = false;
                this.movePhase = MovePhase.SPEEDING_UP;
                this.actionStartTime = System.currentTimeMillis();
            }
            case SPEEDING_UP -> {
                long speedupElapsed = System.currentTimeMillis() - this.actionStartTime;
                float duration = Math.max(0.001f, this.speedupDurationMs());
                float progress = Math.min(1.0f, speedupElapsed / duration);
                if (this.keysOverridden) this.restoreKeyStates();
                if (mc.player.input != null) {
                    boolean forward = this.isPressed(mc.options.forwardKey);
                    float targetForward = forward ? 1.0f : 0.0f;
                    mc.player.input.movementForward = this.lerp(mc.player.input.movementForward, targetForward * progress, this.speedupLerp());
                    if (progress > this.sprintAt() && forward && !mc.player.isSprinting()) mc.player.setSprinting(true);
                }
                if (speedupElapsed >= duration) this.movePhase = MovePhase.FINISHED;
            }
            case FINISHED -> this.resetState();
        }
    }

    private void updateMoveKeys() {
        if (!this.shouldMoveInScreen()) return;
        KeyBinding[] keys = new KeyBinding[]{mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey};
        for (KeyBinding key : keys) key.setPressed(this.isPressed(key));
        if (mc.player != null && mc.player.getAbilities().flying) mc.options.sneakKey.setPressed(this.isPressed(mc.options.sneakKey));
    }

    private boolean shouldMoveInScreen() {
        return mc.currentScreen != null && !this.isTyping();
    }

    private boolean isTyping() {
        return mc.currentScreen instanceof ChatScreen
                || mc.currentScreen instanceof SignEditScreen
                || mc.currentScreen instanceof AnvilScreen
                || mc.currentScreen instanceof CreativeInventoryScreen && CreativeInventoryScreen.selectedTab == ItemGroups.getSearchGroup()
                || mc.currentScreen != null && TextField.LAST_FIELD != null && TextField.LAST_FIELD.isFocused();
    }

    private boolean isPressed(KeyBinding key) {
        int keyCode = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode();
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyCode);
    }

    private boolean hasMovementInput() {
        return this.isPressed(mc.options.forwardKey) || this.isPressed(mc.options.backKey) || this.isPressed(mc.options.leftKey) || this.isPressed(mc.options.rightKey) || this.isPressed(mc.options.jumpKey);
    }

    private void restoreKeyStates() {
        mc.options.forwardKey.setPressed(this.wasForwardPressed && this.isPressed(mc.options.forwardKey));
        mc.options.backKey.setPressed(this.wasBackPressed && this.isPressed(mc.options.backKey));
        mc.options.leftKey.setPressed(this.wasLeftPressed && this.isPressed(mc.options.leftKey));
        mc.options.rightKey.setPressed(this.wasRightPressed && this.isPressed(mc.options.rightKey));
        mc.options.jumpKey.setPressed(this.wasJumpPressed && this.isPressed(mc.options.jumpKey));
        this.keysOverridden = false;
    }

    private void flushHeldOpenInventoryPacket() {
        if (this.heldOpenInventoryPacket == null || mc.player == null) return;
        this.sending = true;
        mc.player.networkHandler.sendPacket(this.heldOpenInventoryPacket);
        this.sending = false;
        this.heldOpenInventoryPacket = null;
        this.suppressNoActionClosePacket = false;
    }

    private void flushPackets() {
        if (mc.player == null) return;
        this.sending = true;
        for (Packet<?> packet : this.packets) mc.player.networkHandler.sendPacket(packet);
        this.sending = false;
        this.packets.clear();
    }

    private float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    private void resetState() {
        if (this.keysOverridden) this.restoreKeyStates();
        this.movePhase = MovePhase.READY;
        this.inventoryOpened = false;
        this.packetsHeld = false;
        this.inventoryActionPerformed = false;
        this.heldOpenInventoryPacket = null;
        this.suppressNoActionClosePacket = false;
        this.packets.clear();
    }

    @Override
    public void onDisable() {
        this.flushHeldOpenInventoryPacket();
        this.flushPackets();
        this.resetState();
        super.onDisable();
    }
}
