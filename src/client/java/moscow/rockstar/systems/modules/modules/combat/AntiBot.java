/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket$Action
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket$Entry
 */
package moscow.rockstar.systems.modules.modules.combat;

import com.mojang.authlib.GameProfile;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

@ModuleInfo(name="Anti Bot", category=ModuleCategory.COMBAT, desc="\u041f\u043e\u043c\u0435\u0447\u0430\u0435\u0442 \u0431\u043e\u0442\u043e\u0432 \u0441\u043e\u0437\u0434\u0430\u043d\u043d\u044b\u0445 \u0430\u043d\u0442\u0438 \u0447\u0438\u0442\u043e\u043c")
public class AntiBot
extends BaseModule {
    private final ModeSetting modeSetting = new ModeSetting(this, "\u041c\u043e\u0434");
    private final ModeSetting.Value rw = new ModeSetting.Value(this.modeSetting, "RW");
    private final ModeSetting.Value defaults = new ModeSetting.Value(this.modeSetting, "Default");
    private final Set<UUID> bots = Collections.newSetFromMap(new ConcurrentHashMap());
    private final Set<UUID> warnPlayers = ConcurrentHashMap.newKeySet();
    private final EventListener<ReceivePacketEvent> onPacket = event -> {
        PlayerListS2CPacket packet;
        block6: {
            block5: {
                Packet<?> patt0$temp = event.getPacket();
                if (!(patt0$temp instanceof PlayerListS2CPacket)) break block5;
                packet = (PlayerListS2CPacket)patt0$temp;
                if (this.rw.isSelected()) break block6;
            }
            return;
        }
        if (!packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
            return;
        }
        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            GameProfile profile = entry.profile();
            if (this.warnPlayers.contains(profile.getId()) || this.bots.contains(profile.getId()) || !profile.getProperties().isEmpty() || entry.latency() == 0) continue;
            this.warnPlayers.add(profile.getId());
        }
    };

    @Override
    public void onDisable() {
        this.bots.clear();
        super.onDisable();
    }

    @Override
    public void tick() {
        if (AntiBot.mc.world == null || AntiBot.mc.player == null) {
            return;
        }
        if (this.rw.isSelected()) {
            this.checkPlayersForFakes();
            this.checkWarnedPlayers();
            this.cleanupBots();
        } else {
            for (PlayerEntity player : AntiBot.mc.world.getPlayers()) {
                if (!(player.getHealth() <= 0.0f) && !player.noClip) continue;
                this.bots.add(player.getUuid());
            }
        }
        super.tick();
    }

    private void checkPlayersForFakes() {
        Entity currentTarget = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        for (PlayerEntity player : AntiBot.mc.world.getPlayers()) {
            if (this.isInvalidPlayer(player) || !this.isSuspiciousTargetFake(player, currentTarget) || player.age >= 30) continue;
            this.bots.add(player.getUuid());
        }
    }

    private void checkWarnedPlayers() {
        for (UUID uuid : this.warnPlayers) {
            PlayerEntity player = AntiBot.mc.world.getPlayerByUuid(uuid);
            if (player == null || !this.hasFullArmor(player) && !this.hasSuspiciousUUID(player)) continue;
            this.bots.add(player.getUuid());
        }
    }

    private void cleanupBots() {
        if (AntiBot.mc.player.age % 100 == 0) {
            this.bots.removeIf(uuid -> AntiBot.mc.world.getPlayerByUuid(uuid) == null);
        }
    }

    private boolean isInvalidPlayer(PlayerEntity player) {
        return player == AntiBot.mc.player || this.bots.contains(player.getUuid());
    }

    private boolean isSuspiciousTargetFake(PlayerEntity player, Entity target) {
        if (!(target instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity realTarget = (PlayerEntity)target;
        boolean sameIdentity = player.getGameProfile().getName().equals(realTarget.getGameProfile().getName()) || player.getId() == realTarget.getId();
        return sameIdentity && !player.getInventory().equals(realTarget.getInventory());
    }

    private boolean hasFullArmor(PlayerEntity player) {
        int armorCount = 0;
        for (ItemStack stack : player.getArmorItems()) {
            if (stack.isEmpty()) continue;
            ++armorCount;
        }
        return armorCount == 4;
    }

    private boolean hasSuspiciousUUID(PlayerEntity player) {
        try {
            UUID nameAsUUID = UUID.fromString(player.getGameProfile().getName());
            return !player.getUuid().equals(nameAsUUID);
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRWBot(PlayerEntity player) {
        return this.bots.contains(player.getUuid());
    }
}

