/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.util.math.BlockPos
 */
package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;

@ModuleInfo(name="Fast Item Use", category=ModuleCategory.OTHER, desc="modules.descriptions.fast_item_use")
public class FastItemUse
extends BaseModule {
    private final BooleanSetting bow = new BooleanSetting((SettingsContainer)this, "modules.settings.fast_item_use.bow", "modules.settings.fast_item_use.bow.description").enable();
    private final BooleanSetting trident = new BooleanSetting((SettingsContainer)this, "modules.settings.fast_item_use.trident", "modules.settings.fast_item_use.trident.description").enable();
    private final BooleanSetting crossbow = new BooleanSetting((SettingsContainer)this, "modules.settings.fast_item_use.crossbow", "modules.settings.fast_item_use.crossbow.description").enable();
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
        if (this.trident.isEnabled() && this.canReleaseTrident()) {
            this.releaseItem();
        }
        if (this.bow.isEnabled() && this.canReleaseBow()) {
            this.releaseItem();
        }
        if (this.crossbow.isEnabled() && this.canReleaseCrossbow()) {
            this.releaseItem();
        }
    };

    private void releaseItem() {
        if (FastItemUse.mc.player == null) {
            return;
        }
        FastItemUse.mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, FastItemUse.mc.player.getHorizontalFacing()));
        FastItemUse.mc.player.stopUsingItem();
    }

    private boolean canReleaseTrident() {
        if (FastItemUse.mc.player == null) {
            return false;
        }
        ItemStack heldStack = FastItemUse.mc.player.getMainHandStack();
        return heldStack.getItem() == Items.TRIDENT && EnchantmentUtility.getEnchantmentLevel(heldStack, (RegistryKey<Enchantment>)Enchantments.RIPTIDE) > 0 && FastItemUse.mc.player.isUsingItem() && (double)FastItemUse.mc.player.getItemUseTime() >= 10.0 && FastItemUse.mc.player.getAttackCooldownProgress(0.5f) > 0.92f;
    }

    private boolean canReleaseBow() {
        if (FastItemUse.mc.player == null) {
            return false;
        }
        return FastItemUse.mc.player.getMainHandStack().getItem() == Items.BOW && FastItemUse.mc.player.isUsingItem() && (double)FastItemUse.mc.player.getItemUseTime() >= 10.0;
    }

    private boolean canReleaseCrossbow() {
        if (FastItemUse.mc.player == null) {
            return false;
        }
        return FastItemUse.mc.player.getMainHandStack().getItem() == Items.CROSSBOW && FastItemUse.mc.player.isUsingItem() && FastItemUse.mc.player.getItemUseTime() >= 10;
    }
}

