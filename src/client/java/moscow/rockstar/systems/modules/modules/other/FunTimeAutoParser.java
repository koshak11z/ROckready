package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.setting.settings.StringSetting;
import moscow.rockstar.utility.time.Timer;

@ModuleInfo(name = "FunTime AutoParser", category = ModuleCategory.OTHER, desc = "Парсер цен /ah search")
public class FunTimeAutoParser extends BaseModule {
    private final StringSetting query = new StringSetting(this, "Search item").text("diamond");
    private final SliderSetting commandDelay = new SliderSetting(this, "Задержка команды").min(50.0f).max(2000.0f).step(50.0f).currentValue(250.0f).suffix(" ms");
    private final Timer timer = new Timer();

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null || !this.timer.finished((long)this.commandDelay.getCurrentValue())) return;
        String q = this.query.getText() == null ? "" : this.query.getText().trim();
        if (!q.isEmpty()) mc.player.networkHandler.sendChatCommand("ah search " + q);
        this.timer.reset();
    };
}
