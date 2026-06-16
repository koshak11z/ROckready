package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

@ModuleInfo(name = "FunTime AutoSell", category = ModuleCategory.OTHER, desc = "Автопродажа предмета в руке по минимальной цене")
public class FunTimeAutoSell extends BaseModule {
    private final SliderSetting delay = new SliderSetting(this, "Задержка").min(100.0f).max(2000.0f).step(50.0f).currentValue(500.0f).suffix(" ms");
    private final Timer timer = new Timer();

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null || !this.timer.finished((long)this.delay.getCurrentValue())) return;
        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty()) return;
        String id = Registries.ITEM.getId(stack.getItem()).toString();
        mc.player.networkHandler.sendChatCommand("ah sell " + Math.max(1, stack.getCount()) + " " + id);
        this.timer.reset();
    };
}
