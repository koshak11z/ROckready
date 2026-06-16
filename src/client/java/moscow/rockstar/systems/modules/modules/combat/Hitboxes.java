/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 */
package moscow.rockstar.systems.modules.modules.combat;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.target.TargetSettings;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@ModuleInfo(name="Hitboxes", category=ModuleCategory.COMBAT, desc="modules.descriptions.hitboxes")
public class Hitboxes
extends BaseModule {
    private final SliderSetting scale = new SliderSetting(this, "size").min(0.0f).max(1.0f).step(0.1f).currentValue(0.3f);
    private final SelectSetting targets = new SelectSetting(this, "targets");
    private final SelectSetting.Value players = new SelectSetting.Value(this.targets, "players").select();
    private final SelectSetting.Value animals = new SelectSetting.Value(this.targets, "animals").select();
    private final SelectSetting.Value mobs = new SelectSetting.Value(this.targets, "mobs").select();
    private final SelectSetting.Value invisibles = new SelectSetting.Value(this.targets, "invisibles").select();
    private final SelectSetting.Value nakedPlayers = new SelectSetting.Value(this.targets, "nakedPlayers").select();
    private final SelectSetting.Value friends = new SelectSetting.Value(this.targets, "friends");

    public boolean shouldModifyHitbox(LivingEntity entity) {
        TargetSettings settings = new TargetSettings.Builder().targetPlayers(this.players.isSelected()).targetAnimals(this.animals.isSelected()).targetMobs(this.mobs.isSelected()).targetInvisibles(this.invisibles.isSelected()).targetNakedPlayers(this.nakedPlayers.isSelected()).targetFriends(this.friends.isSelected()).build();
        if (entity instanceof ClientPlayerEntity) {
            return false;
        }
        if (entity.isDead()) {
            return false;
        }
        if (Rockstar.getInstance().isPanic()) {
            return false;
        }
        return settings.isEntityValid((Entity)entity);
    }

    @Generated
    public SliderSetting getScale() {
        return this.scale;
    }
}

