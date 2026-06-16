/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.math.MathHelper
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.systems.modules.constructions.swinganim;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingAnimScreen;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingSettings;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class SwingPhase
extends SwingSettings {
    private final SliderSetting anchorX = new PhaseSlider(this, "swing.anchorX").step(0.05f).min(-5.0f).max(5.0f).currentValue(0.0f);
    private final SliderSetting anchorY = new PhaseSlider(this, "swing.anchorY").step(0.05f).min(-5.0f).max(5.0f).currentValue(0.0f);
    private final SliderSetting anchorZ = new PhaseSlider(this, "swing.anchorZ").step(0.05f).min(-5.0f).max(5.0f).currentValue(0.0f);
    private final SliderSetting moveX = new PhaseSlider(this, "swing.moveX").step(0.05f).min(-5.0f).max(5.0f).currentValue(0.0f);
    private final SliderSetting moveY = new PhaseSlider(this, "swing.moveY").step(0.05f).min(-5.0f).max(5.0f).currentValue(0.0f);
    private final SliderSetting moveZ = new PhaseSlider(this, "swing.moveZ").step(0.05f).min(-3.0f).max(3.0f).currentValue(0.0f);
    private final SliderSetting rotateX = new PhaseSlider(this, "swing.rotateX").step(15.0f).min(-360.0f).max(360.0f).currentValue(0.0f);
    private final SliderSetting rotateY = new PhaseSlider(this, "swing.rotateY").step(15.0f).min(-360.0f).max(360.0f).currentValue(0.0f);
    private final SliderSetting rotateZ = new PhaseSlider(this, "swing.rotateZ").step(15.0f).min(-360.0f).max(360.0f).currentValue(0.0f);

    @Generated
    public SliderSetting getAnchorX() {
        return this.anchorX;
    }

    @Generated
    public SliderSetting getAnchorY() {
        return this.anchorY;
    }

    @Generated
    public SliderSetting getAnchorZ() {
        return this.anchorZ;
    }

    @Generated
    public SliderSetting getMoveX() {
        return this.moveX;
    }

    @Generated
    public SliderSetting getMoveY() {
        return this.moveY;
    }

    @Generated
    public SliderSetting getMoveZ() {
        return this.moveZ;
    }

    @Generated
    public SliderSetting getRotateX() {
        return this.rotateX;
    }

    @Generated
    public SliderSetting getRotateY() {
        return this.rotateY;
    }

    @Generated
    public SliderSetting getRotateZ() {
        return this.rotateZ;
    }

    public static class PhaseSlider
    extends SliderSetting {
        public PhaseSlider(@NotNull SettingsContainer parent, String name) {
            super(parent, name);
        }

        @Override
        public void setCurrentValue(float currentValue) {
            super.setCurrentValue(currentValue);
            if (SwingAnimScreen.hasShiftDown()) {
                PhaseSlider slider;
                for (Setting setting : Rockstar.getInstance().getSwingManager().getStartPhase().getSettings()) {
                    if (!setting.getName().equals(this.getName()) || !(setting instanceof PhaseSlider)) continue;
                    slider = (PhaseSlider)setting;
                    slider.silentSet(currentValue);
                }
                for (Setting setting : Rockstar.getInstance().getSwingManager().getEndPhase().getSettings()) {
                    if (!setting.getName().equals(this.getName()) || !(setting instanceof PhaseSlider)) continue;
                    slider = (PhaseSlider)setting;
                    slider.silentSet(currentValue);
                }
            }
        }

        private void silentSet(float value) {
            this.currentValue = MathHelper.clamp((float)((float)((double)Math.round((double)value * (1.0 / (double)this.step)) / (1.0 / (double)this.step))), (float)this.min, (float)this.max);
        }
    }
}

