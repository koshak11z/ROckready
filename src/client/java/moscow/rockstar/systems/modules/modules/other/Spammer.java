package moscow.rockstar.systems.modules.modules.other;

import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Spammer", category = ModuleCategory.OTHER, desc = "Рассылка сообщения по анархиям")
public class Spammer extends BaseModule {
    private final SliderSetting rejoinDelaySetting = new SliderSetting(this, "Кд перезахода").min(100.0f).max(5000.0f).step(100.0f).currentValue(500.0f).suffix(" ms");
    private final SliderSetting sendDelaySetting = new SliderSetting(this, "Кд сообщений").min(100.0f).max(5000.0f).step(100.0f).currentValue(500.0f).suffix(" ms");
    private final SliderSetting repeatCountSetting = new SliderSetting(this, "Повторов").min(1.0f).max(50.0f).step(1.0f).currentValue(5.0f);

    private static String message = "";
    private final List<Integer> anarchies = buildAnarchyList();
    private int anarchyIndex;
    private int sentOnCurrent;
    private long lastJoinAt;
    private long lastSendAt;
    private Phase phase = Phase.JOIN;

    private enum Phase { JOIN, SPAM }

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null || mc.player.networkHandler == null || this.anarchies.isEmpty()) return;
        long now = System.currentTimeMillis();
        long rejoinDelay = (long)this.rejoinDelaySetting.getCurrentValue();
        long sendDelay = (long)this.sendDelaySetting.getCurrentValue();
        int repeatCount = (int)this.repeatCountSetting.getCurrentValue();

        switch (this.phase) {
            case JOIN -> {
                if (now - this.lastJoinAt < rejoinDelay) return;
                int an = this.anarchies.get(this.anarchyIndex);
                mc.player.networkHandler.sendChatCommand("an" + an);
                this.lastJoinAt = now;
                this.lastSendAt = now;
                this.sentOnCurrent = 0;
                this.phase = Phase.SPAM;
            }
            case SPAM -> {
                if (message == null || message.isEmpty()) return;
                if (now - this.lastSendAt < sendDelay || now - this.lastJoinAt < sendDelay) return;
                sendMessage(message);
                this.lastSendAt = now;
                this.sentOnCurrent++;
                if (this.sentOnCurrent >= repeatCount) {
                    this.anarchyIndex = (this.anarchyIndex + 1) % this.anarchies.size();
                    this.phase = Phase.JOIN;
                }
            }
        }
    };

    @Override
    public void onEnable() {
        this.anarchyIndex = 0;
        this.sentOnCurrent = 0;
        this.lastJoinAt = 0L;
        this.lastSendAt = 0L;
        this.phase = Phase.JOIN;
        super.onEnable();
    }

    private void sendMessage(String msg) {
        if (msg.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(msg.substring(1));
        } else {
            mc.player.networkHandler.sendChatMessage(msg);
        }
    }

    private static List<Integer> buildAnarchyList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 64; i++) {
            if (i == 1 || i == 2 || i == 3 || i == 16 || i == 17 || i == 33 || i == 49) continue;
            list.add(i);
        }
        return list;
    }

    @Generated
    public static String getMessage() {
        return message;
    }

    @Generated
    public static void setMessage(String message) {
        Spammer.message = message;
    }
}
