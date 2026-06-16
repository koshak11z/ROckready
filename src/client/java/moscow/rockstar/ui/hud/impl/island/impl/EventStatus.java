/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
 */
package moscow.rockstar.ui.hud.impl.island.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.TimerStatus;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class EventStatus
extends TimerStatus {
    private final List<ActiveEvent> activeEvents = new ArrayList<ActiveEvent>();
    private final Timer timer = new Timer();
    private String pendingEvent;
    private int an = -1;
    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = (GameMessageS2CPacket)patt0$temp;
            String message = packet.content().getString().replaceAll("\\n", " ").replaceAll("[^\\p{L}\\p{N}\\s\\[\\]:.-]", "").replaceAll("\\s{2,}", " ").trim();
            if (packet.content().getString().contains("\u041f\u043e\u044f\u0432\u0438\u043b\u0441\u044f")) {
                Matcher eventMatcher = Pattern.compile("\\[([^\\]]+)\\]").matcher(message);
                Matcher coordMatcher = Pattern.compile("\u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u0430\u0445\\s+(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)").matcher(message);
                if (eventMatcher.find() && coordMatcher.find()) {
                    String eventName = eventMatcher.group(1);
                    for (EventType value : EventType.values()) {
                        if (!eventName.toLowerCase().contains(value.getName().toLowerCase())) continue;
                        this.activeEvents.removeIf(e -> e.type == value);
                        this.activeEvents.add(new ActiveEvent(value, value.getTime()));
                        this.an = ServerUtility.ftAn;
                        this.timer.reset();
                        if (this.an == ServerUtility.ftAn) {
                            Rockstar.getInstance().getWayPointsManager().add(this.activeEvents.getFirst().type.getName(), Integer.parseInt(coordMatcher.group(1)), Integer.parseInt(coordMatcher.group(2)), Integer.parseInt(coordMatcher.group(3)));
                        }
                        break;
                    }
                }
            } else {
                Matcher coordMatcher;
                for (EventType value : EventType.values()) {
                    if (!message.equalsIgnoreCase(value.getName())) continue;
                    this.pendingEvent = value.getName();
                    break;
                }
                if (message.toLowerCase().startsWith("\u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b") && (coordMatcher = Pattern.compile("\u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b:?\\s*(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)", 2).matcher(message)).find() && this.pendingEvent != null) {
                    for (EventType value : EventType.values()) {
                        if (!this.pendingEvent.equalsIgnoreCase(value.getName())) continue;
                        this.activeEvents.removeIf(e -> e.type == value);
                        this.activeEvents.add(new ActiveEvent(value, value.getTime()));
                        this.timer.reset();
                        Rockstar.getInstance().getWayPointsManager().add(value.getName(), Integer.parseInt(coordMatcher.group(1)), Integer.parseInt(coordMatcher.group(2)), Integer.parseInt(coordMatcher.group(3)));
                        break;
                    }
                    this.pendingEvent = null;
                }
            }
        }
    };
    private final EventListener<WorldChangeEvent> worldChangeEvent = event -> {
        if (ServerUtility.ftAn != this.an) {
            this.activeEvents.forEach(e -> Rockstar.getInstance().getWayPointsManager().del(e.type().getName()));
            this.activeEvents.clear();
        }
    };

    public EventStatus(SelectSetting setting) {
        super(setting, "events");
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    @Override
    public void draw(CustomDrawContext context) {
        this.activeEvents.removeIf(event -> {
            if (this.timer.getElapsedTime() >= event.type().getTime()) {
                Rockstar.getInstance().getWayPointsManager().del(event.type().getName());
                return true;
            }
            return false;
        });
        if (!this.activeEvents.isEmpty()) {
            ActiveEvent currentEvent = this.activeEvents.getFirst();
            long remaining = currentEvent.type.getTime() - this.timer.getElapsedTime();
            if (remaining > 0L) {
                int timer = (int)(remaining / 1000L);
                int min = timer / 60;
                int sec = timer % 60;
                String time = String.format("%d:%02d", min, sec);
                ColorRGBA color = this.getEventColor(currentEvent.type);
                this.update(Integer.parseInt(time.split(":")[0]) + ":", "", Integer.parseInt(time.split(":")[1]), currentEvent.type.name, color);
                super.draw(context);
            }
        }
    }

    private ColorRGBA getEventColor(EventType eventType) {
        return switch (eventType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> new ColorRGBA(138.0f, 43.0f, 226.0f);
            case 1 -> new ColorRGBA(255.0f, 69.0f, 0.0f);
            case 2 -> new ColorRGBA(255.0f, 140.0f, 0.0f);
            case 3 -> new ColorRGBA(70.0f, 130.0f, 180.0f);
            case 4 -> new ColorRGBA(243.0f, 196.0f, 82.0f);
            case 5 -> new ColorRGBA(139.0f, 222.0f, 221.0f);
            case 6 -> new ColorRGBA(141.0f, 99.0f, 184.0f);
            case 7 -> new ColorRGBA(41.0f, 253.0f, 5.0f);
            case 8 -> new ColorRGBA(90.0f, 158.0f, 152.0f);
        };
    }

    @Override
    public boolean canShow() {
        return !this.activeEvents.isEmpty();
    }

    private record ActiveEvent(EventType type, long time) {
    }

    static enum EventType {
        ALTAR("\u041c\u0438\u0441\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439 \u0410\u043b\u0442\u0430\u0440\u044c", 360000L),
        BEACON("\u041c\u0430\u044f\u043a \u0423\u0431\u0438\u0439\u0446\u0430", 360000L),
        VULCAN("\u0412\u0443\u043b\u043a\u0430\u043d", 300000L),
        METEOR("\u041c\u0435\u0442\u0435\u043e\u0440\u0438\u0442\u043d\u044b\u0439 \u0434\u043e\u0436\u0434\u044c", 180000L),
        PACKAGE("\u041f\u043e\u0441\u044b\u043b\u043a\u0430", 180000L),
        BOSS("\u0411\u043e\u0441\u0441", 180000L),
        CONTAINER("\u041a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440", 180000L),
        GRUZ("\u0413\u0440\u0443\u0437", 180000L),
        MYSTERIOUS_SHIP("\u0422\u0430\u0438\u043d\u0441\u0442\u0432\u0435\u043d\u043d\u044b\u0439 \u043a\u043e\u0440\u0430\u0431\u043b\u044c", 300000L);

        final String name;
        final long time;

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public long getTime() {
            return this.time;
        }

        @Generated
        private EventType(String name, long time) {
            this.name = name;
            this.time = time;
        }
    }
}

