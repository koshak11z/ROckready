/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.ShulkerBoxBlock
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.AnvilScreen
 *  net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen
 *  net.minecraft.client.gui.screen.ingame.GenericContainerScreen
 *  net.minecraft.client.gui.screen.ingame.InventoryScreen
 *  net.minecraft.client.gui.screen.ingame.SignEditScreen
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemGroups
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket
 *  net.minecraft.screen.slot.SlotActionType
 */
package moscow.rockstar.systems.modules.modules.player;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(name="Gui Move", category=ModuleCategory.PLAYER, enabledByDefault=true)
public class GuiMove
extends BaseModule {
    private final List<Packet<?>> packets = new ArrayList();
    private final List<ClickSlotC2SPacket> pickupPackets = new ArrayList<ClickSlotC2SPacket>();
    private final ModeSetting mode = new ModeSetting(this, "\u0420\u0435\u0436\u0438\u043c");
    private final ModeSetting.Value noBypass = new ModeSetting.Value(this.mode, "\u0411\u0435\u0437 \u043e\u0431\u0445\u043e\u0434\u0430");
    private final ModeSetting.Value auto = new ModeSetting.Value(this.mode, "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439").select();
    private final ModeSetting.Value custom = new ModeSetting.Value(this.mode, "\u041d\u0430\u0441\u0442\u0440\u0430\u0438\u0432\u0430\u0435\u043c\u044b\u0439");
    private final ModeSetting containers = new ModeSetting((SettingsContainer)this, "\u0412 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u0430\u0445", this.auto::isSelected);
    private final ModeSetting.Value notWork = new ModeSetting.Value(this.containers, "\u0421\u0442\u043e\u044f\u0442\u044c");
    private final ModeSetting.Value vanillaCon = new ModeSetting.Value(this.containers, "\u0414\u0432\u0438\u0433\u0430\u0442\u044c\u0441\u044f").select();
    private final ModeSetting.Value shift = new ModeSetting.Value(this.containers, "\u0428\u0438\u0444\u0442");
    private final BooleanSetting cancelClose = new BooleanSetting((SettingsContainer)this, "\u041e\u0442\u043c\u0435\u043d\u044f\u0442\u044c \u0437\u0430\u043a\u0440\u044b\u0442\u0438\u0435", this.auto::isSelected).enable();
    private final BooleanSetting jump = new BooleanSetting((SettingsContainer)this, "\u0423\u0447\u0438\u0442\u044b\u0432\u0430\u0442\u044c \u043f\u0440\u044b\u0436\u043e\u043a", this.auto::isSelected);
    private final ModeSetting bypassMode = new ModeSetting((SettingsContainer)this, "\u041e\u0431\u0445\u043e\u0434", this.auto::isSelected);
    private final ModeSetting.Value vanilla = new ModeSetting.Value(this.bypassMode, "\u0411\u0435\u0437 \u043e\u0431\u0445\u043e\u0434\u0430");
    private final ModeSetting.Value slow = new ModeSetting.Value(this.bypassMode, "\u0417\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u0435").select();
    private final ModeSetting.Value slowClose = new ModeSetting.Value(this.bypassMode, "\u041f\u0440\u0438 \u0437\u0430\u043a\u0440\u044b\u0442\u0438\u0438");
    private final ModeSetting.Value close = new ModeSetting.Value(this.bypassMode, "\u0424\u0435\u0439\u043a \u0437\u0430\u043a\u0440\u044b\u0442\u0438\u0435");
    private final BooleanSetting ground = new BooleanSetting((SettingsContainer)this, "\u0411\u0435\u0437 \u043e\u0431\u0445\u043e\u0434\u0430 \u043d\u0430 \u0437\u0435\u043c\u043b\u0435", () -> this.vanilla.isSelected() || this.auto.isSelected());
    private final SliderSetting cooldown = new SliderSetting((SettingsContainer)this, "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430", () -> this.vanilla.isSelected() || this.close.isSelected() || this.auto.isSelected()).min(50.0f).max(500.0f).step(50.0f).currentValue(100.0f).suffix(" ms");
    private final Timer staying = new Timer();
    private final Timer grounding = new Timer();
    private boolean stay;
    private boolean sending;
    private int screenId = 0;
    private final EventListener<ClientPlayerTickEvent> onPlayerTick = event -> {
        KeyBinding[] movementKeys;
        if (this.auto.isSelected()) {
            if (ServerUtility.isST()) {
                this.notWork.select();
                this.cancelClose.enable();
                this.jump.enable();
                this.slow.select();
                this.ground.enable();
                this.cooldown.setCurrentValue(500.0f);
            } else if (ServerUtility.isFT()) {
                this.notWork.select();
                this.cancelClose.enable();
                this.jump.setEnabled(false);
                this.slowClose.select();
                this.ground.setEnabled(false);
                this.cooldown.setCurrentValue(100.0f);
            } else if (ServerUtility.isFT()) {
                this.notWork.select();
                this.cancelClose.enable();
                this.jump.setEnabled(false);
                this.slowClose.select();
                this.ground.setEnabled(false);
                this.cooldown.setCurrentValue(100.0f);
            } else if (ServerUtility.isIntave()) {
                this.notWork.select();
                this.cancelClose.enable();
                this.jump.setEnabled(false);
                this.slow.select();
                this.ground.setEnabled(false);
                this.cooldown.setCurrentValue(150.0f);
            } else if (ServerUtility.isHW()) {
                this.notWork.select();
                this.cancelClose.enable();
                this.jump.setEnabled(false);
                this.slow.select();
                this.ground.setEnabled(false);
                this.cooldown.setCurrentValue(100.0f);
            } else {
                this.notWork.select();
                this.cancelClose.enable();
                this.jump.setEnabled(false);
                this.slow.select();
                this.ground.setEnabled(false);
                this.cooldown.setCurrentValue(100.0f);
            }
        }
        this.screenId = GuiMove.mc.player.currentScreenHandler != null ? GuiMove.mc.player.currentScreenHandler.syncId : 0;
        if (this.slowClose.isSelected() && GuiMove.mc.currentScreen == null && !this.packets.isEmpty()) {
            this.stay = true;
        }
        if (this.canSend()) {
            this.sendPackets();
            this.stay = false;
        }
        if (!GuiMove.mc.player.isOnGround()) {
            this.grounding.reset();
        }
        if (!(GuiMove.mc.player.currentScreenHandler == null || !GuiMove.mc.player.currentScreenHandler.getCursorStack().isEmpty() && GuiMove.mc.player.currentScreenHandler.getCursorStack() != ItemStack.EMPTY || this.pickupPackets.isEmpty() || this.sending)) {
            this.stay = true;
            if (GuiMove.mc.player.isOnGround() && this.ground.isEnabled() || this.staying.finished((long)this.cooldown.getCurrentValue())) {
                this.sendPickupPackets();
            }
        }
        if (this.isTyping() || !this.invCheck()) {
            return;
        }
        long windowHandle = mc.getWindow().getHandle();
        for (KeyBinding key : movementKeys = new KeyBinding[]{GuiMove.mc.options.forwardKey, GuiMove.mc.options.backKey, GuiMove.mc.options.leftKey, GuiMove.mc.options.rightKey, GuiMove.mc.options.jumpKey}) {
            int keyCode = InputUtil.fromTranslationKey((String)key.getBoundKeyTranslationKey()).getCode();
            key.setPressed(InputUtil.isKeyPressed((long)windowHandle, (int)keyCode));
        }
        if (GuiMove.mc.player.getAbilities().flying) {
            int keyCode = InputUtil.fromTranslationKey((String)GuiMove.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode();
            GuiMove.mc.options.sneakKey.setPressed(InputUtil.isKeyPressed((long)windowHandle, (int)keyCode));
        }
    };
    private final EventListener<SendPacketEvent> eventEventListener = event -> {
        GenericContainerScreen container;
        Screen patt0$temp = GuiMove.mc.currentScreen;
        if (patt0$temp instanceof GenericContainerScreen && (container = (GenericContainerScreen)patt0$temp).getTitle().getString().toLowerCase().contains("\u0432\u044b\u0431\u043e\u0440")) {
            return;
        }
        if (GuiMove.mc.player == null || this.isTyping() || !this.invCheck() || this.vanilla.isSelected() || this.sending || GuiMove.mc.player.isOnGround() && this.ground.isEnabled() || this.noBypass.isSelected()) {
            return;
        }
        Packet<?> patt1$temp = event.getPacket();
        if (patt1$temp instanceof ClickSlotC2SPacket) {
            ClickSlotC2SPacket packet = (ClickSlotC2SPacket)patt1$temp;
            if (this.ground.isEnabled() && GuiMove.mc.player.isOnGround()) {
                this.grounding.reset();
            }
            if (packet.getActionType() == SlotActionType.PICKUP || packet.getActionType() == SlotActionType.PICKUP_ALL || packet.getActionType() == SlotActionType.CLONE || packet.getActionType() == SlotActionType.QUICK_CRAFT) {
                this.pickupPackets.add(packet);
                event.cancel();
                return;
            }
            if (this.slow.isSelected()) {
                this.packets.add((Packet<?>)packet);
                event.cancel();
                this.stay = true;
            } else if (this.slowClose.isSelected()) {
                BlockItem item;
                Item patt2$temp;
                this.packets.add((Packet<?>)packet);
                event.cancel();
                if (GuiMove.mc.currentScreen instanceof GenericContainerScreen || ServerUtility.isPastaFT() && (patt2$temp = packet.getStack().getItem()) instanceof BlockItem && (item = (BlockItem)patt2$temp).getBlock() instanceof ShulkerBoxBlock || packet.getActionType() == SlotActionType.THROW) {
                    this.stay = true;
                }
            } else if (this.close.isSelected()) {
                GuiMove.mc.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(this.screenId));
            }
        }
    };
    private final EventListener<InputEvent> onInput = event -> {
        if (GuiMove.mc.currentScreen instanceof GenericContainerScreen && this.shift.isSelected()) {
            event.setSneak(true);
        }
        if (this.stay) {
            event.setForward(0.0f);
            event.setStrafe(0.0f);
            if (this.jump.isEnabled()) {
                event.setJump(false);
            }
        }
        if (this.jump.isEnabled() && event.isJump() || event.getStrafe() != 0.0f || event.getForward() != 0.0f) {
            this.staying.reset();
        }
    };
    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        if (this.cancelClose.isEnabled() && event.getPacket() instanceof CloseScreenS2CPacket) {
            event.cancel();
        }
    };


    private void applyNormalMoveKeys() {
        if (this.isTyping() || GuiMove.mc.player == null || GuiMove.mc.currentScreen == null) {
            return;
        }
        long windowHandle = mc.getWindow().getHandle();
        for (KeyBinding key : new KeyBinding[]{GuiMove.mc.options.forwardKey, GuiMove.mc.options.backKey, GuiMove.mc.options.leftKey, GuiMove.mc.options.rightKey, GuiMove.mc.options.jumpKey}) {
            int keyCode = InputUtil.fromTranslationKey((String)key.getBoundKeyTranslationKey()).getCode();
            key.setPressed(InputUtil.isKeyPressed((long)windowHandle, (int)keyCode));
        }
        if (GuiMove.mc.player.getAbilities().flying) {
            int keyCode = InputUtil.fromTranslationKey((String)GuiMove.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode();
            GuiMove.mc.options.sneakKey.setPressed(InputUtil.isKeyPressed((long)windowHandle, (int)keyCode));
        }
    }

    private boolean isTyping() {
        return GuiMove.mc.currentScreen instanceof ChatScreen || GuiMove.mc.currentScreen != null && TextField.LAST_FIELD != null && TextField.LAST_FIELD.isFocused() || GuiMove.mc.currentScreen instanceof SignEditScreen || GuiMove.mc.currentScreen instanceof AnvilScreen || GuiMove.mc.currentScreen instanceof CreativeInventoryScreen && CreativeInventoryScreen.selectedTab == ItemGroups.getSearchGroup();
    }

    private boolean invCheck() {
        return !this.notWork.isSelected() || this.ground.isEnabled() && GuiMove.mc.player.isOnGround() || GuiMove.mc.currentScreen instanceof InventoryScreen || GuiMove.mc.currentScreen instanceof CreativeInventoryScreen || GuiMove.mc.currentScreen == null;
    }

    private void sendPackets() {
        if (this.packets.isEmpty()) {
            return;
        }
        this.sending = true;
        this.packets.forEach(arg_0 -> ((ClientPlayNetworkHandler)GuiMove.mc.player.networkHandler).sendPacket(arg_0));
        this.packets.clear();
        GuiMove.mc.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(this.screenId));
        this.sending = false;
    }

    public boolean canSend() {
        return this.isEnabled() && this.stay && (GuiMove.mc.player.isOnGround() && this.ground.isEnabled() && this.grounding.finished(500L) || this.staying.finished((long)this.cooldown.getCurrentValue()));
    }

    private void sendPickupPackets() {
        if (this.pickupPackets.isEmpty()) {
            return;
        }
        this.sending = true;
        this.pickupPackets.forEach(arg_0 -> ((ClientPlayNetworkHandler)GuiMove.mc.player.networkHandler).sendPacket(arg_0));
        this.pickupPackets.clear();
        this.sending = false;
    }

    public boolean slowing() {
        return this.slow.isSelected() || this.slowClose.isSelected();
    }

    @Generated
    public SliderSetting getCooldown() {
        return this.cooldown;
    }

    @Generated
    public void setStay(boolean stay) {
        this.stay = stay;
    }

    @Generated
    public void setSending(boolean sending) {
        this.sending = sending;
    }
}

