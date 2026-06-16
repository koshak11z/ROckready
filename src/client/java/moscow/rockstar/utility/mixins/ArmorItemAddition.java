/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.equipment.ArmorMaterial
 *  net.minecraft.item.equipment.EquipmentType
 */
package moscow.rockstar.utility.mixins;

import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;

public interface ArmorItemAddition {
    public EquipmentType rockstar$getType();

    public ArmorMaterial rockstar$getMaterial();
}

