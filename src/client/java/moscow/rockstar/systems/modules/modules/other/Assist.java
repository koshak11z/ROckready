/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientCommonNetworkHandler$ConfirmServerResourcePackScreen
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.player.PlayerInventory
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket
 *  net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket$Status
 *  net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
 *  net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 */
package moscow.rockstar.systems.modules.modules.other;

import java.util.function.BooleanSupplier;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventIntegration;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.player.GuiMove;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

@ModuleInfo(name="Assist", category=ModuleCategory.OTHER, desc="Помощник для разных серверов")
public class Assist
        extends BaseModule {
    private final BooleanSupplier rwCondition = () -> ServerUtility.isHW() || ServerUtility.isPastaFT() || ServerUtility.isCM() || ServerUtility.isSaturn() || ServerUtility.isIntave();
    private final BooleanSupplier ftCondition = () -> ServerUtility.isHW() || ServerUtility.isRW() || ServerUtility.isCM() || ServerUtility.isSaturn() || ServerUtility.isIntave();
    private final BooleanSupplier hwCondition = () -> ServerUtility.isPastaFT() || ServerUtility.isRW() || ServerUtility.isCM() || ServerUtility.isSaturn() || ServerUtility.isIntave();
    private final BooleanSetting spoof = new BooleanSetting(this, "Спуф рп", "Позволяет зайти на сервер без скачивания ресурс пака", this.rwCondition);
    private final BooleanSetting closeMenu = new BooleanSetting(this, "Закрывать меню", "Автоматически закрывает меню при заходе на гриф", this.rwCondition).enable();
    private final BooleanSetting autoFix = new BooleanSetting((SettingsContainer)this, "Автопочинка", this.rwCondition);
    private final BooleanSetting warnArmor = new BooleanSetting((SettingsContainer)this, "Поломка брони", "Предупреждает если броня поломана");
    private final BooleanSetting fly = new BooleanSetting((SettingsContainer)this, "Драгон флай", "Ускоряет флай из креатива").enable();
    private final SliderSetting flySpeedXZ = new SliderSetting((SettingsContainer)this, "Ускорять по XZ", () -> !this.fly.isEnabled()).currentValue(1.0f).max(5.0f).min(1.0f).step(0.5f).currentValue(5.0f);
    private final SliderSetting flySpeedY = new SliderSetting((SettingsContainer)this, "Ускорять по Y", () -> !this.fly.isEnabled()).currentValue(1.0f).max(5.0f).min(1.0f).step(0.5f).currentValue(5.0f);
    private final BooleanSetting autoPiona = new BooleanSetting(this, "Авто пиона", "Автоматически прописывает /piona при заходе на Funtime", ServerUtility::isRW).enable();
    private final BindSetting dezorentKey = new BindSetting(this, "Дезориентация", this.ftCondition);
    private final BindSetting trapkaKey = new BindSetting(this, "Трапка", this.ftCondition);
    private final BindSetting smerchKey = new BindSetting(this, "Огненный смерч", this.ftCondition);
    private final BindSetting plastKey = new BindSetting(this, "Пласт", this.ftCondition);
    private final BindSetting auraKey = new BindSetting(this, "Божья аура", this.ftCondition);
    private final BindSetting pilbKey = new BindSetting(this, "Явная пыль", this.ftCondition);
    private final BindSetting windChargeKey = new BindSetting(this, "Заряд ветра", this.ftCondition);
    private final BindSetting snowFreezeKey = new BindSetting(this, "Снежок заморозка", this.ftCondition);
    private final BooleanSetting autoZako = new BooleanSetting((SettingsContainer)this, "Авто /zako", this.hwCondition);
    private final BooleanSetting autoStop = new BooleanSetting((SettingsContainer)this, "Авто-стоп", this.hwCondition);
    private final BindSetting stanKey = new BindSetting(this, "Стан", this.hwCondition);
    private final BindSetting snowKey = new BindSetting(this, "Ком снега", this.hwCondition);
    private final BindSetting bombKey = new BindSetting(this, "Взрывная штучка", this.hwCondition);
    private final BindSetting hwTrapKey = new BindSetting(this, "Трапка", this.hwCondition);
    private final BindSetting boomTrapKey = new BindSetting(this, "Взрывная трапка", this.hwCondition);
    private final BindSetting goolKey = new BindSetting(this, "Прощальный гул", this.hwCondition);
    private final BindSetting backpackKey = new BindSetting(this, "Рюкзак", this.hwCondition);
    private final BooleanSetting syncGuiMove = new BooleanSetting(this, "Синхронизировать с GuiMove");
    private final Timer timer = new Timer();
    private final Timer timerStop = new Timer();
    private boolean stopHandle;
    private boolean zakoCommandSent = true;
    private boolean pionaCommandSent = true;
    private boolean visible;
    private final EventListener<MouseEvent> onMouseEvent = event -> this.handleButtonPress(event.getButton());
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (event.getAction() != 1) {
            return;
        }
        this.handleButtonPress(event.getKey());
    };
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
        String title;
        GameMessageS2CPacket packet;
        OpenScreenS2CPacket screenPacket;
        String message;
        Packet<?> patt0$temp;
        if (this.autoPiona.isEnabled() && (patt0$temp = event.getPacket()) instanceof GameMessageS2CPacket && ((message = (packet = (GameMessageS2CPacket)patt0$temp).content().getString().toLowerCase()).contains("10,000 было начислено вам") || message.contains("повторите текст еще раз"))) {
            this.pionaCommandSent = false;
            this.timer.reset();
        }
        if (this.autoZako.isEnabled() && (patt0$temp = event.getPacket()) instanceof GameMessageS2CPacket) {
            packet = (GameMessageS2CPacket)patt0$temp;
            message = packet.content().getString();
            if (message.contains("Вы уже активировали этот промокод")) {
                this.zakoCommandSent = false;
                this.timer.reset();
            } else if (message.contains("Прямо сейчас идет набор")) {
                this.zakoCommandSent = true;
            }
        }
        if (this.autoStop.isEnabled() && (patt0$temp = event.getPacket()) instanceof GameMessageS2CPacket && (message = (packet = (GameMessageS2CPacket)patt0$temp).content().getString()).contains("Телепортация принята")) {
            this.stopHandle = true;
        }
        if (this.closeMenu.isEnabled() && (patt0$temp = event.getPacket()) instanceof OpenScreenS2CPacket && ((title = (screenPacket = (OpenScreenS2CPacket)patt0$temp).getName().getString()).contains("Меню") || title.contains("ꈁꈀꈂꈁꈂꈁ"))) {
            Assist.mc.player.closeScreen();
            event.cancel();
        }
    };
    private final EventListener<InputEvent> inputEvent = event -> {
        if (this.autoStop.isEnabled() && this.stopHandle && !this.timerStop.finished(3200L)) {
            event.setForward(0.0f);
            event.setJump(false);
            event.setStrafe(0.0f);
            this.timerStop.reset();
        }
    };
    private Timer shootTimer = new Timer();
    private Timer chargeTimer = new Timer();
    private ItemSlot previousSlot = null;
    private boolean isCharging = false;
    private boolean needsSlotSwapBack = false;
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {};

    @Override
    public void tick() {
        float currentDamage;
        float maxDamage;
        if (Assist.mc.player == null || Assist.mc.world == null || Assist.mc.interactionManager == null) {
            return;
        }
        if (this.warnArmor.isEnabled()) {
            float armorPoint = 1.0f;
            for (ItemStack stack : Assist.mc.player.getAllArmorItems()) {
                if (stack.isEmpty()) continue;
                maxDamage = stack.getMaxDamage();
                currentDamage = maxDamage - (float)stack.getDamage();
                armorPoint = currentDamage / maxDamage;
            }
            if ((double)armorPoint < 0.36) {
                if (this.visible) {
                    Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.INFO, "Поломка", "Ваша броня на грани поломки");
                    this.visible = false;
                }
            } else {
                this.visible = true;
            }
        }
        if (this.fly.isEnabled() && Assist.mc.player.getAbilities().flying) {
            if (!Assist.mc.player.isSneaking() && Assist.mc.options.jumpKey.isPressed()) {
                Assist.mc.player.setVelocity(Assist.mc.player.getVelocity().x, (double)this.flySpeedY.getCurrentValue(), Assist.mc.player.getVelocity().z);
            } else if (Assist.mc.options.sneakKey.isPressed()) {
                Assist.mc.player.setVelocity(Assist.mc.player.getVelocity().x, (double)(-this.flySpeedY.getCurrentValue()), Assist.mc.player.getVelocity().z);
            }
            EntityUtility.setSpeed(this.flySpeedXZ.getCurrentValue());
        }
        if (this.autoFix.isEnabled() && !ServerUtility.hasCT) {
            PlayerInventory inventory = Assist.mc.player.getInventory();
            for (int i = 0; i < inventory.size(); ++i) {
                ItemStack stack;
                stack = inventory.getStack(i);
                if (stack.isEmpty() || !stack.isDamageable() || !((currentDamage = (maxDamage = (float)stack.getMaxDamage()) - (float)stack.getDamage()) / maxDamage > 0.5f) || Assist.mc.player.age % 25 != 0) continue;
                Assist.mc.player.networkHandler.sendChatCommand("fix all");
                break;
            }
        }
        if (this.spoof.isEnabled() && this.spoof.isVisible() && Assist.mc.player.age > 20 && Assist.mc.currentScreen instanceof ClientCommonNetworkHandler.ConfirmServerResourcePackScreen) {
            Assist.mc.player.networkHandler.sendPacket((Packet)new ResourcePackStatusC2SPacket(Assist.mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
            Assist.mc.player.networkHandler.sendPacket((Packet)new ResourcePackStatusC2SPacket(Assist.mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            Assist.mc.player.closeScreen();
        }
        if (this.autoPiona.isEnabled()) {
            if (Assist.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler && Assist.mc.currentScreen.getTitle().getString().contains("Вам подарок")) {
                Assist.mc.interactionManager.clickSlot(Assist.mc.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, (PlayerEntity)Assist.mc.player);
            }
            if (this.timer.finished(1000L) && !this.pionaCommandSent) {
                this.pionaCommandSent = true;
                Assist.mc.player.networkHandler.sendChatCommand("piona");
            }
        }
        if (this.autoZako.isEnabled() && this.timer.finished(500L) && this.zakoCommandSent) {
            this.zakoCommandSent = false;
            Assist.mc.player.networkHandler.sendChatCommand("zako");
        }
        super.tick();
    }

    private void handleButtonPress(int button) {
        if (this.isSwapKey(button)) {
            this.syncWithGuiMove();
        }
        if (this.dezorentKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.ENDER_EYE);
        } else if (this.trapkaKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.NETHERITE_SCRAP);
        } else if (this.smerchKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.FIRE_CHARGE);
        } else if (this.stanKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.NETHER_STAR);
        } else if (this.plastKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.DRIED_KELP);
        } else if (this.auraKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.PHANTOM_MEMBRANE);
        } else if (this.pilbKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.SUGAR);
        } else if (this.windChargeKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.WIND_CHARGE);
        } else if (this.snowFreezeKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.SNOWBALL);
        } else if (this.snowKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.SNOWBALL);
        } else if (this.bombKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.FIRE_CHARGE);
        } else if (this.hwTrapKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.POPPED_CHORUS_FRUIT);
        } else if (this.boomTrapKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.PRISMARINE_SHARD);
        } else if (this.goolKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.FIREWORK_STAR);
        } else if (this.backpackKey.isKey(button)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.MAGENTA_SHULKER_BOX);
        }
    }

    private boolean isSwapKey(int button) {
        return this.dezorentKey.isKey(button) || this.trapkaKey.isKey(button) || this.smerchKey.isKey(button) || this.stanKey.isKey(button)
                || this.plastKey.isKey(button) || this.auraKey.isKey(button) || this.pilbKey.isKey(button) || this.windChargeKey.isKey(button)
                || this.snowFreezeKey.isKey(button) || this.snowKey.isKey(button) || this.bombKey.isKey(button) || this.hwTrapKey.isKey(button)
                || this.boomTrapKey.isKey(button) || this.goolKey.isKey(button) || this.backpackKey.isKey(button);
    }

    private void syncWithGuiMove() {
        if (this.syncGuiMove.isEnabled()) {
            Rockstar.getInstance().getModuleManager().getModule(GuiMove.class).setStay(true);
        }
    }

    private void attemptShoot() {
        ItemStack crossbowStack;
        SlotGroup<ItemSlot> group = SlotGroups.hotbar().and(SlotGroups.inventory());
        ItemSlot crossbowSlot = group.findItem(Items.CROSSBOW);
        if (crossbowSlot == null) {
            return;
        }
        if (this.previousSlot == null) {
            this.previousSlot = InventoryUtility.getCurrentHotbarSlot();
        }
        if (crossbowSlot instanceof HotbarSlot) {
            HotbarSlot hotbarSlot = (HotbarSlot)crossbowSlot;
            if (InventoryUtility.getCurrentHotbarSlot().item() != Items.CROSSBOW) {
                InventoryUtility.selectHotbarSlot(hotbarSlot);
            }
        }
        if (!this.isCrossbowCharged(crossbowStack = InventoryUtility.getCurrentHotbarSlot().itemStack())) {
            this.startCharging();
            return;
        }
        this.shoot();
        this.startCharging();
    }

    private boolean isCrossbowCharged(ItemStack crossbow) {
        if (crossbow.isEmpty() || crossbow.getItem() != Items.CROSSBOW) {
            return false;
        }
        NbtCompound nbt = ItemUtility.getNBT(crossbow);
        return nbt != null && nbt.getBoolean("Charged");
    }

    private void startCharging() {
        if (this.isCharging) {
            return;
        }
        this.isCharging = true;
        this.chargeTimer.reset();
        MinecraftClient.getInstance().options.useKey.setPressed(true);
    }

    private void finishCharging() {
        if (!this.isCharging) {
            return;
        }
        this.isCharging = false;
        MinecraftClient.getInstance().options.useKey.setPressed(false);
        this.needsSlotSwapBack = true;
    }

    private void shoot() {
        if (Assist.mc.interactionManager != null) {
            Assist.mc.interactionManager.interactItem((PlayerEntity)Assist.mc.player, Hand.MAIN_HAND);
        }
    }

    public void stop() {
        ItemSlot itemSlot;
        if (this.isCharging) {
            MinecraftClient.getInstance().options.useKey.setPressed(false);
            this.isCharging = false;
        }
        if (this.previousSlot != null && (itemSlot = this.previousSlot) instanceof HotbarSlot) {
            HotbarSlot hotbarSlot = (HotbarSlot)itemSlot;
            InventoryUtility.selectHotbarSlot(hotbarSlot);
            this.previousSlot = null;
        }
        this.needsSlotSwapBack = false;
    }
}