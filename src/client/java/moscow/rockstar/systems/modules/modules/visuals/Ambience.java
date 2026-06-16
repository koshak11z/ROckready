/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
 */
package moscow.rockstar.systems.modules.modules.visuals;

import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

@ModuleInfo(name="Ambience", category=ModuleCategory.VISUALS, desc="modules.descriptions.ambience")
public class Ambience
extends BaseModule {
    public final BooleanSetting endSky = new BooleanSetting(this, "modules.settings.ambience.end_sky");
    private final BooleanSetting customTime = new BooleanSetting(this, "modules.settings.ambience.custom_time");
    private final SliderSetting time = new SliderSetting((SettingsContainer)this, "modules.settings.ambience.time", () -> !this.customTime.isEnabled()).step(1000.0f).min(0.0f).max(24000.0f).currentValue(12000.0f);
    public final BooleanSetting bright = new BooleanSetting(this, "modules.settings.ambience.bright").enable();
    private long oldTime;
    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket && this.customTime.isEnabled()) {
            event.cancel();
        }
    };

    @Override
    public void tick() {
        if (Ambience.mc.world == null) {
            return;
        }
        if (this.customTime.isEnabled()) {
            Ambience.mc.world.getLevelProperties().setTimeOfDay((long)this.time.getCurrentValue());
        }
        super.tick();
    }

    @Override
    public void onEnable() {
        if (!EntityUtility.isInGame() || Ambience.mc.world == null) {
            return;
        }
        this.oldTime = Ambience.mc.world.getTime();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (!EntityUtility.isInGame() || Ambience.mc.world == null) {
            return;
        }
        Ambience.mc.world.getLevelProperties().setTimeOfDay(this.oldTime);
        super.onDisable();
    }

    @Generated
    public BooleanSetting getEndSky() {
        return this.endSky;
    }

    @Generated
    public BooleanSetting getCustomTime() {
        return this.customTime;
    }

    @Generated
    public SliderSetting getTime() {
        return this.time;
    }

    @Generated
    public BooleanSetting getBright() {
        return this.bright;
    }

    @Generated
    public long getOldTime() {
        return this.oldTime;
    }

    @Generated
    public EventListener<ReceivePacketEvent> getOnReceivePacket() {
        return this.onReceivePacket;
    }
}

