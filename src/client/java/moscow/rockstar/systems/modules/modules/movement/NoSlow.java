/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.item.consume.UseAction
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.util.Hand
 */
package moscow.rockstar.systems.modules.modules.movement;

import java.util.Random;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.SlowDownEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

@ModuleInfo(name="No Slow", category=ModuleCategory.MOVEMENT)
public class NoSlow
        extends BaseModule {
    private int ticks;
    private final Timer timer = new Timer();
    private final Random random = new Random();
    private long lastSprintToggleMs;
    private boolean sprintPause;
    private long eatShiftStartMs;
    private boolean eatShiftActive;
    private boolean prevSneakPressed;
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.noslow.mode");
    private final ModeSetting.Value grim = new ModeSetting.Value(this.mode, "modules.settings.noslow.grim");
    private final ModeSetting.Value spooky = new ModeSetting.Value(this.mode, "modules.settings.noslow.spooky");
    private final ModeSetting.Value holyWorld = new ModeSetting.Value(this.mode, "modules.settings.noslow.holly");
    private final ModeSetting.Value snow = new ModeSetting.Value(this.mode, "modules.settings.noslow.snow");
    private final EventListener<SlowDownEvent> onSlowEventEvent = event -> {
        Hand hand;
        if (NoSlow.mc.player == null || NoSlow.mc.world == null || NoSlow.mc.interactionManager == null) {
            return;
        }
        boolean snowMode = false;
        if (this.snow.isSelected()) {
            Block block = EntityUtility.getBlockStandingOnPlayer();
            if (block != Blocks.SNOW && block != Blocks.SNOW_BLOCK) {
                return;
            }
            snowMode = true;
            if (NoSlow.mc.options.jumpKey.isPressed()) {
                this.ticks = 0;
                return;
            }
            NoSlow.mc.player.setSprinting(false);
        }
        if (!NoSlow.mc.player.isUsingItem()) {
            this.ticks = 0;
            if (this.eatShiftActive) {
                NoSlow.mc.options.sneakKey.setPressed(this.prevSneakPressed);
                this.eatShiftActive = false;
            }
            this.eatShiftStartMs = 0L;
            return;
        }
        if (NoSlow.mc.player.getActiveHand() == Hand.MAIN_HAND && NoSlow.mc.player.getMainHandStack().getUseAction() == UseAction.EAT) {
            if (this.eatShiftStartMs == 0L) {
                this.eatShiftStartMs = System.currentTimeMillis();
                this.prevSneakPressed = NoSlow.mc.options.sneakKey.isPressed();
                this.eatShiftActive = true;
            }
            if (System.currentTimeMillis() - this.eatShiftStartMs < 50L) {
                NoSlow.mc.options.sneakKey.setPressed(true);
                return;
            }
            if (this.eatShiftActive) {
                NoSlow.mc.options.sneakKey.setPressed(this.prevSneakPressed);
                this.eatShiftActive = false;
            }
        } else {
            if (this.eatShiftActive) {
                NoSlow.mc.options.sneakKey.setPressed(this.prevSneakPressed);
                this.eatShiftActive = false;
            }
            this.eatShiftStartMs = 0L;
        }
        if (this.spooky.isSelected() || this.holyWorld.isSelected()) {
            ++this.ticks;
        }
        if ((NoSlow.mc.player.getMainHandStack().getUseAction() == UseAction.BLOCK || NoSlow.mc.player.getOffHandStack().getUseAction() == UseAction.EAT) && NoSlow.mc.player.getActiveHand() == Hand.MAIN_HAND || !NoSlow.mc.player.isUsingItem()) {
            return;
        }
        if (!snowMode) {
            this.updateRandomSprint();
        }
        if (NoSlow.mc.player.getActiveHand() == Hand.MAIN_HAND && !this.spooky.isSelected()) {
            NoSlow.mc.interactionManager.sendSequencedPacket(NoSlow.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, sequence, NoSlow.mc.player.getYaw(), NoSlow.mc.player.getPitch()));
            event.cancel();
            return;
        }
        Hand hand2 = hand = NoSlow.mc.player.getActiveHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (!this.spooky.isSelected() && !this.holyWorld.isSelected()) {
            NoSlow.mc.interactionManager.sendSequencedPacket(NoSlow.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, NoSlow.mc.player.getYaw(), NoSlow.mc.player.getPitch()));
        }
        if (this.ticks >= 2 || this.grim.isSelected() || this.holyWorld.isSelected() || this.snow.isSelected()) {
            event.cancel();
            this.ticks = 0;
        }
    };
    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {};
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
        if (NoSlow.mc.player == null || NoSlow.mc.player.getItemCooldownManager() == null || mc.getNetworkHandler() == null || !this.grim.isSelected()) {
            return;
        }
        if (NoSlow.mc.player.getItemCooldownManager().isCoolingDown(NoSlow.mc.player.getMainHandStack().getItem().getDefaultStack()) || NoSlow.mc.player.getItemCooldownManager().isCoolingDown(NoSlow.mc.player.getOffHandStack().getItem().getDefaultStack()) || !NoSlow.mc.player.isUsingItem() || !(NoSlow.mc.player.fallDistance < 1.0f) || NoSlow.mc.player.getActiveHand() == Hand.OFF_HAND) {
            // empty if block
        }
    };

    private void updateRandomSprint() {
        long now = System.currentTimeMillis();
        if (now - this.lastSprintToggleMs > 300L) {
            this.sprintPause = this.random.nextFloat() < 0.35f;
            this.lastSprintToggleMs = now;
        }
        NoSlow.mc.player.setSprinting(!this.sprintPause);
    }
}

