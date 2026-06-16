package moscow.rockstar.utility.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.TickEvent;
import baritone.api.event.events.WorldEvent;
import baritone.api.event.events.type.EventState;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.world.ClientWorld;

import java.util.function.BiFunction;

public final class BaritoneBootstrap implements IMinecraft {
    private boolean booted;

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        try {
            BiFunction<EventState, TickEvent.Type, TickEvent> provider = TickEvent.createNextProvider();
            for (IBaritone baritone : BaritoneAPI.getProvider().getAllBaritones()) {
                TickEvent.Type type = baritone.getPlayerContext().player() != null && baritone.getPlayerContext().world() != null ? TickEvent.Type.IN : TickEvent.Type.OUT;
                baritone.getGameEventHandler().onTick(provider.apply(EventState.PRE, type));
                baritone.getGameEventHandler().onPostTick(provider.apply(EventState.POST, type));
            }
        } catch (Throwable ignored) {
        }
    };

    private final EventListener<WorldChangeEvent> onWorld = event -> {
        try {
            ClientWorld world = mc.world;
            BaritoneAPI.getProvider().getPrimaryBaritone().getGameEventHandler().onWorldEvent(new WorldEvent(world, EventState.POST));
        } catch (Throwable ignored) {
        }
    };

    public void boot() {
        if (this.booted) return;
        this.booted = true;
        try {
            BaritoneAPI.getProvider().getPrimaryBaritone();
        } catch (Throwable ignored) {
        }
        Rockstar.getInstance().getEventManager().subscribe(this);
    }
}
