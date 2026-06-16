/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.utility.game.prediction;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class FallPredictor
implements IMinecraft {
    private static RegistryEntry<Enchantment> FALL = null;
    private static final double GRAVITY = 0.08;
    private static final double DRAG = 0.98;

    public static float predictFallDamage(PlayerEntity player, int futureTicks) {
        float raw;
        Vec3d pos = player.getPos();
        Vec3d vel = player.getVelocity();
        Box bbox = player.getBoundingBox().offset(0.0, 0.0, 0.0);
        double fallDist = 0.0;
        for (int t = 0; t < futureTicks; ++t) {
            vel = vel.add(0.0, -0.08, 0.0).multiply(0.98, 0.98, 0.98);
            pos = pos.add(vel);
            if (!FallPredictor.mc.world.isSpaceEmpty((bbox = bbox.offset(vel)).offset(0.0, -0.001, 0.0))) break;
            if (!(vel.y < 0.0)) continue;
            fallDist -= vel.y;
        }
        if ((raw = (float)fallDist) <= 3.0f) {
            return 0.0f;
        }
        int distanceBlocks = MathHelper.floor((float)(raw - 3.0f));
        float damage = distanceBlocks;
        ItemStack boots = player.getInventory().getArmorStack(0);
        int ffLevel = EnchantmentUtility.getEnchantmentLevel(boots, (RegistryKey<Enchantment>)Enchantments.FEATHER_FALLING);
        if (ffLevel > 0) {
            damage = Math.max(damage - damage * 0.15f * (float)ffLevel, 0.0f);
        }
        if (player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return 0.0f;
        }
        return damage;
    }

    @Generated
    private FallPredictor() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

