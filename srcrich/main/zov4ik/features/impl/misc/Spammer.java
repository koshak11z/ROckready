package im.zov4ik.features.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.events.player.TickEvent;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Spammer extends Module {

    public static Spammer getInstance() {
        return Instance.get(Spammer.class);
    }

    final SliderSettings rejoinDelaySetting = new SliderSettings("Кд перезахода", "Задержка перед переходом на следующую анархию (мс)")
            .setValue(500f).range(100, 5000);

    final SliderSettings sendDelaySetting = new SliderSettings("Кд сообщений", "Задержка между отправкой сообщений (мс)")
            .setValue(500f).range(100, 5000);

    final SliderSettings repeatCountSetting = new SliderSettings("Повторов", "Сколько раз отправить сообщение на каждой анархии")
            .setValue(5f).range(1, 50);

    @Getter
    @Setter
    static String message = "";

    private final List<Integer> anarchies = buildAnarchyList();
    private int anarchyIndex;
    private int sentOnCurrent;

    private long lastJoinAt;
    private long lastSendAt;

    private enum Phase { JOIN, SPAM }
    private Phase phase = Phase.JOIN;

    public Spammer() {
        super("Spammer", "Spammer", ModuleCategory.MISC);
        setup(rejoinDelaySetting, sendDelaySetting, repeatCountSetting);
    }

    @Override
    public void activate() {
        super.activate();
        anarchyIndex = 0;
        sentOnCurrent = 0;
        lastJoinAt = 0L;
        lastSendAt = 0L;
        phase = Phase.JOIN;
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.player.networkHandler == null) return;
        if (anarchies.isEmpty()) return;

        long now = System.currentTimeMillis();
        long rejoinDelay = rejoinDelaySetting.getInt();
        long sendDelay = sendDelaySetting.getInt();
        int repeatCount = repeatCountSetting.getInt();

        switch (phase) {
            case JOIN:
                if (now - lastJoinAt < rejoinDelay) return;
                int an = anarchies.get(anarchyIndex);
                mc.player.networkHandler.sendChatCommand("an" + an);
                lastJoinAt = now;
                lastSendAt = now;
                sentOnCurrent = 0;
                phase = Phase.SPAM;
                break;
            case SPAM:
                if (message == null || message.isEmpty()) return;
                if (now - lastSendAt < sendDelay) return;
                if (now - lastJoinAt < sendDelay) return;
                sendMessage(message);
                lastSendAt = now;
                sentOnCurrent++;
                if (sentOnCurrent >= repeatCount) {
                    anarchyIndex = (anarchyIndex + 1) % anarchies.size();
                    phase = Phase.JOIN;
                    lastJoinAt = now;
                }
                break;
        }
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
        for (int i = 101; i <= 110; i++) list.add(i);
        for (int i = 201; i <= 220; i++) list.add(i);
        for (int i = 301; i <= 308; i++) list.add(i);
        for (int i = 501; i <= 506; i++) list.add(i);
        for (int i = 601; i <= 606; i++) list.add(i);
        return list;
    }
}
