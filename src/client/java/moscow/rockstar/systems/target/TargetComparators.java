/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.target;

import java.util.Comparator;
import java.util.function.Function;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TargetComparators
implements IMinecraft {
    public static final Comparator<Entity> DISTANCE = Comparator.comparingDouble(entity -> entity.distanceTo((Entity)TargetComparators.mc.player));
    public static final Comparator<Entity> HEALTH = Comparator.comparingDouble(entity -> {
        double d;
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            d = living.getHealth();
        } else {
            d = 0.0;
        }
        return d;
    });
    public static final Comparator<Entity> FOV = Comparator.comparingDouble(entity -> {
        if (TargetComparators.mc.player == null) {
            return Double.MAX_VALUE;
        }
        Vec3d playerPos = TargetComparators.mc.player.getPos();
        Vec3d entityPos = entity.getPos();
        Vec3d playerLook = TargetComparators.mc.player.getRotationVec(1.0f);
        Vec3d toEntity = entityPos.subtract(playerPos).normalize();
        double dot = playerLook.dotProduct(toEntity);
        return Math.acos(MathHelper.clamp((double)dot, (double)-1.0, (double)1.0)) * 57.29577951308232;
    });
    public static final Comparator<Entity> BAD_ARMOR = Comparator.comparingDouble(entity -> {
        if (!(entity instanceof PlayerEntity)) {
            return Double.MAX_VALUE;
        }
        PlayerEntity player = (PlayerEntity)entity;
        double totalArmor = 0.0;
        for (ItemStack armorStack : player.getAllArmorItems()) {
            if (armorStack == null || armorStack.isEmpty()) continue;
            totalArmor += (double)armorStack.getItem().getDefaultStack().getCount();
        }
        return totalArmor;
    });
    public static final Comparator<Entity> GOOD_ARMOR = BAD_ARMOR.reversed();

    public static Comparator<Entity> byValue(Function<Entity, Double> valueExtractor) {
        return Comparator.comparingDouble(valueExtractor::apply);
    }

    public static Comparator<Entity> byValueReversed(Function<Entity, Double> valueExtractor) {
        return Comparator.comparingDouble(valueExtractor::apply).reversed();
    }
}

