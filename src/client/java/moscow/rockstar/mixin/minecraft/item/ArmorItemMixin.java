/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.Item$Settings
 *  net.minecraft.item.equipment.ArmorMaterial
 *  net.minecraft.item.equipment.EquipmentType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.item;

import moscow.rockstar.utility.mixins.ArmorItemAddition;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ArmorItem.class})
public abstract class ArmorItemMixin
implements ArmorItemAddition {
    @Unique
    private EquipmentType rockstar$type;
    @Unique
    private ArmorMaterial rockstar$material;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    public void saveArgs(ArmorMaterial material, EquipmentType type, Item.Settings settings, CallbackInfo ci) {
        this.rockstar$type = type;
        this.rockstar$material = material;
    }

    @Override
    public ArmorMaterial rockstar$getMaterial() {
        return this.rockstar$material;
    }

    @Override
    public EquipmentType rockstar$getType() {
        return this.rockstar$type;
    }
}

