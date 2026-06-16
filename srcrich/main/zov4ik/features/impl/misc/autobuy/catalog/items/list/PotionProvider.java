package im.zov4ik.features.impl.misc.autobuy.catalog.items.list;

import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.customitem.CustomItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.defaultsetpricec.Defaultpricec;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PotionProvider {
    public static List<AutoBuyableItem> getPotions() {
        List<AutoBuyableItem> potions = new ArrayList<>();

        
        
        potions.add(new CustomItem("[★] Святая вода", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Святая вода"),
                createPotionContents(16777215, List.of(
                        new StatusEffectInstance(StatusEffects.REGENERATION, 1200, 2),
                        new StatusEffectInstance(StatusEffects.INVISIBILITY, 12000, 1),
                        new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 1)
                )), null));

        
        potions.add(new CustomItem("[★] Зелье Гнева", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Зелье Гнева"),
                createPotionContents(10040115, List.of(
                        new StatusEffectInstance(StatusEffects.STRENGTH, 600, 4),
                        new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 3)
                )), null));

        
        
        potions.add(new CustomItem("[★] Зелье Палладина", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Зелье Палладина"),
                createPotionContents(65535, List.of(
                        new StatusEffectInstance(StatusEffects.RESISTANCE, 12000, 0),
                        new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 12000, 0),
                        new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 1200, 2),
                        new StatusEffectInstance(StatusEffects.INVISIBILITY, 18000, 2)
                )), null));

        
        
        potions.add(new CustomItem("[★] Зелье Ассасина", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Зелье Ассасина"),
                createPotionContents(3355443, List.of(
                        new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 3),
                        new StatusEffectInstance(StatusEffects.SPEED, 6000, 2),
                        new StatusEffectInstance(StatusEffects.HASTE, 1200, 0),
                        new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1)
                )), null));

        
        
        potions.add(new CustomItem("[★] Зелье Радиации", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Зелье Радиации"),
                createPotionContents(4737096, List.of(
                        new StatusEffectInstance(StatusEffects.WEAKNESS, 1800, 1),
                        new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, 1),
                        new StatusEffectInstance(StatusEffects.WITHER, 1800, 2),
                        new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0)
                )), null));

        
        
        
        potions.add(new CustomItem("[★] Снотворное", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Снотворное"),
                createPotionContents(3329330, List.of(
                        new StatusEffectInstance(StatusEffects.POISON, 400, 1),
                        new StatusEffectInstance(StatusEffects.WITHER, 400, 1),
                        new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 2),
                        new StatusEffectInstance(StatusEffects.HUNGER, 400, 4),
                        new StatusEffectInstance(StatusEffects.GLOWING, 400, 0)
                )), null));

        return potions;
    }

    private static PotionContentsComponent createPotionContents(int color, List<StatusEffectInstance> effects) {
        List<RegistryEntry<net.minecraft.entity.effect.StatusEffect>> effectEntries = new ArrayList<>();
        for (StatusEffectInstance effect : effects) {
            effectEntries.add(effect.getEffectType());
        }
        return new PotionContentsComponent(Optional.empty(), Optional.of(color), effects, Optional.empty());
    }
}
