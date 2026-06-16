/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.enums.CameraSubmersionType
 *  net.minecraft.client.render.Camera
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.StatusEffects
 */
package moscow.rockstar.systems.modules.modules.visuals;

import lombok.Generated;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.RangeSetting;
import moscow.rockstar.utility.colors.Colors;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;

@ModuleInfo(name="Custom Fog", category=ModuleCategory.VISUALS)
public class CustomFog
extends BaseModule {
    private final RangeSetting distance = new RangeSetting(this, "modules.settings.custom_fog.distance").min(1.0f).max(100.0f).step(1.0f).firstValue(10.0f).secondValue(50.0f);
    private final ColorSetting fogColor = new ColorSetting(this, "modules.settings.custom_fog.color").color(Colors.ACCENT).alpha(true);

    public boolean shouldModifyFog(Camera camera) {
        if (!this.isEnabled() || CustomFog.mc.world == null || CustomFog.mc.player == null) {
            return false;
        }
        Entity entity = camera.getFocusedEntity();
        if (camera.getSubmersionType() == CameraSubmersionType.WATER) {
            return false;
        }
        if (camera.getSubmersionType() == CameraSubmersionType.LAVA) {
            return false;
        }
        if (camera.getSubmersionType() == CameraSubmersionType.POWDER_SNOW) {
            return false;
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (livingEntity.hasStatusEffect(StatusEffects.BLINDNESS)) {
                return false;
            }
            if (livingEntity.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                return false;
            }
        }
        return true;
    }

    @Generated
    public RangeSetting getDistance() {
        return this.distance;
    }

    @Generated
    public ColorSetting getFogColor() {
        return this.fogColor;
    }
}

