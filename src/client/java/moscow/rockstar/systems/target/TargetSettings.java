/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.decoration.ArmorStandEntity
 *  net.minecraft.entity.decoration.DisplayEntity$ItemDisplayEntity
 *  net.minecraft.entity.mob.MobEntity
 *  net.minecraft.entity.passive.AnimalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 */
package moscow.rockstar.systems.target;

import java.util.Comparator;
import java.util.function.Function;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.combat.AntiBot;
import moscow.rockstar.systems.modules.modules.other.CounterMine;
import moscow.rockstar.systems.target.TargetComparators;
import moscow.rockstar.utility.game.countermine.Point;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TargetSettings
implements IMinecraft {
    private boolean targetPlayers = false;
    private boolean targetAnimals = false;
    private boolean targetMobs = false;
    private boolean targetInvisibles = false;
    private boolean targetNakedPlayers = false;
    private boolean targetFriends = false;
    private boolean targetArmorStands = false;
    private float requiredRange = -1.0f;
    private Comparator<Entity> targetComparator = TargetComparators.DISTANCE;

    public boolean isEntityValid(Entity entity) {
        LivingEntity living;
        if (TargetSettings.mc.player == null || TargetSettings.mc.world == null || entity == null) {
            return false;
        }
        if (ServerUtility.isCM() && this.targetPlayers && entity instanceof DisplayEntity.ItemDisplayEntity) {
            for (Point point : Rockstar.getInstance().getModuleManager().getModule(CounterMine.class).getScanner().getPoints()) {
                if (!point.isTarget() || point.getEntity() != entity || point.isFriend()) continue;
                return this.isWithinRange(entity);
            }
        }
        if (!(entity instanceof LivingEntity) || entity == TargetSettings.mc.player) {
            return false;
        }
        if (entity instanceof LivingEntity && (living = (LivingEntity)entity).isDead()) {
            return false;
        }
        if (!this.isWithinRange(entity)) {
            return false;
        }
        if (entity instanceof ArmorStandEntity) {
            return this.targetArmorStands;
        }
        if (!this.targetInvisibles && entity.isInvisible()) {
            return false;
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            AntiBot antiBotModule = Rockstar.getInstance().getModuleManager().getModule(AntiBot.class);
            if (antiBotModule.isRWBot(player)) {
                return false;
            }
            boolean isFriend = Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString());
            if (!this.targetFriends && isFriend) {
                return false;
            }
            boolean isNaked = this.isPlayerNaked(player);
            if (this.targetPlayers || this.targetNakedPlayers) {
                if (this.targetPlayers && this.targetNakedPlayers) {
                    return true;
                }
                if (this.targetNakedPlayers) {
                    return isNaked;
                }
                return !isNaked;
            }
            return false;
        }
        if (entity instanceof AnimalEntity) {
            return this.targetAnimals;
        }
        if (entity instanceof MobEntity) {
            return this.targetMobs;
        }
        return false;
    }

    public boolean isWithinRange(Entity entity) {
        if (this.getRequiredRange() <= 0.0f) {
            return true;
        }
        return entity.distanceTo((Entity)TargetSettings.mc.player) <= this.getRequiredRange();
    }

    private boolean isPlayerNaked(PlayerEntity player) {
        for (ItemStack armorStack : player.getAllArmorItems()) {
            if (armorStack == null || armorStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Generated
    public boolean isTargetPlayers() {
        return this.targetPlayers;
    }

    @Generated
    public boolean isTargetAnimals() {
        return this.targetAnimals;
    }

    @Generated
    public boolean isTargetMobs() {
        return this.targetMobs;
    }

    @Generated
    public boolean isTargetInvisibles() {
        return this.targetInvisibles;
    }

    @Generated
    public boolean isTargetNakedPlayers() {
        return this.targetNakedPlayers;
    }

    @Generated
    public boolean isTargetFriends() {
        return this.targetFriends;
    }

    @Generated
    public boolean isTargetArmorStands() {
        return this.targetArmorStands;
    }

    @Generated
    public float getRequiredRange() {
        return this.requiredRange;
    }

    @Generated
    public Comparator<Entity> getTargetComparator() {
        return this.targetComparator;
    }

    public static class Builder {
        private final TargetSettings settings = new TargetSettings();

        public Builder targetPlayers(boolean targetPlayers) {
            this.settings.targetPlayers = targetPlayers;
            return this;
        }

        public Builder targetAnimals(boolean targetAnimals) {
            this.settings.targetAnimals = targetAnimals;
            return this;
        }

        public Builder targetMobs(boolean targetMobs) {
            this.settings.targetMobs = targetMobs;
            return this;
        }

        public Builder targetInvisibles(boolean targetInvisibles) {
            this.settings.targetInvisibles = targetInvisibles;
            return this;
        }

        public Builder targetNakedPlayers(boolean targetNakedPlayers) {
            this.settings.targetNakedPlayers = targetNakedPlayers;
            return this;
        }

        public Builder targetFriends(boolean targetFriends) {
            this.settings.targetFriends = targetFriends;
            return this;
        }

        public Builder targetArmorStands(boolean targetArmorStands) {
            this.settings.targetArmorStands = targetArmorStands;
            return this;
        }

        public Builder requiredRange(float range) {
            this.settings.requiredRange = range;
            return this;
        }

        public Builder sortBy(Comparator<Entity> comparator) {
            this.settings.targetComparator = comparator;
            return this;
        }

        public Builder sortByValue(Function<Entity, Double> valueExtractor) {
            this.settings.targetComparator = TargetComparators.byValue(valueExtractor);
            return this;
        }

        public Builder sortByValueReversed(Function<Entity, Double> valueExtractor) {
            this.settings.targetComparator = TargetComparators.byValueReversed(valueExtractor);
            return this;
        }

        public TargetSettings build() {
            return this.settings;
        }
    }
}

