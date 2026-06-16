package im.zov4ik.features.impl.movement;

import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.events.render.WorldRenderEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.geometry.Render3D;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.math.time.StopWatch;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Blink extends Module {
    private static final long ATTACK_POST_BYPASS_MS = 25L;

    public static Blink getInstance() {
        return Instance.get(Blink.class);
    }

    final List<Packet<?>> packets = new CopyOnWriteArrayList<>();
    final SliderSettings impulseDelay = new SliderSettings("Импульсы", "Раз во сколько мс отправлять накопленные пакеты")
            .setValue(250F).range(100F, 1000F);

    Box box;
    public static int tickStop = -1;
    StopWatch timer = new StopWatch();
    long bypassUntilMs = 0L;

    public Blink() {
        super("Blink", ModuleCategory.MOVEMENT);
        setup(impulseDelay);
    }

    @Override
    public void activate() {
        if (mc.player != null) {
            box = mc.player.getBoundingBox();
        }
        timer.reset();
        packets.clear();
        bypassUntilMs = 0L;
    }

    @Override
    public void deactivate() {
        flushQueuedPackets();
    }

    public void flushForInventorySync() {
        if (!isState()) {
            return;
        }
        flushQueuedPackets();
        bypassUntilMs = System.currentTimeMillis() + 40L;
        timer.reset();
    }

    @EventHandler
    public void tick(TickEvent e) {
        if (PlayerInteractionHelper.nullCheck()) {
            return;
        }

        long now = System.currentTimeMillis();
        tickStop--;

        if (tickStop >= 0 && !packets.isEmpty()) {
            flushQueuedPackets();
            timer.reset();
            return;
        }

        if (now < bypassUntilMs) {
            return;
        }

        if (!packets.isEmpty() && timer.finished((long) impulseDelay.getValue())) {
            flushQueuedPackets();
            timer.reset();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (PlayerInteractionHelper.nullCheck()) {
            return;
        }

        switch (e.getPacket()) {
            case PlayerRespawnS2CPacket ignored -> setState(false);
            case GameJoinS2CPacket ignored -> setState(false);
            case ClientStatusC2SPacket status when status.getMode().equals(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN) -> setState(false);
            default -> {
            }
        }

        if (!e.isSend()) {
            return;
        }

        long now = System.currentTimeMillis();
        Packet<?> packet = e.getPacket();

        if (now < bypassUntilMs) {
            return;
        }

        if (packet instanceof PlayerInteractEntityC2SPacket) {
            flushQueuedPackets();
            bypassUntilMs = now + ATTACK_POST_BYPASS_MS;
            timer.reset();
            return;
        }

        if (shouldFlushAndBypass(packet)) {
            flushQueuedPackets();
            timer.reset();
            return;
        }

        if (tickStop < 0) {
            packets.add(packet);
            e.cancel();
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (box != null) {
            Render3D.drawBox(box, ColorAssist.getClientColor(), 1);
        }
    }

    private boolean shouldFlushAndBypass(Packet<?> packet) {
        return packet instanceof PlayerInteractItemC2SPacket
                || packet instanceof ClickSlotC2SPacket
                || packet instanceof UpdateSelectedSlotC2SPacket
                || packet instanceof CloseHandledScreenC2SPacket
                || packet instanceof HandSwingC2SPacket
                || packet instanceof PlayerActionC2SPacket
                || packet instanceof ClientCommandC2SPacket command && (
                command.getMode() == ClientCommandC2SPacket.Mode.START_FALL_FLYING
                        || command.getMode() == ClientCommandC2SPacket.Mode.OPEN_INVENTORY
        );
    }

    private void flushQueuedPackets() {
        if (packets.isEmpty()) {
            return;
        }
        if (mc.player != null) {
            box = mc.player.getBoundingBox();
        }
        packets.forEach(PlayerInteractionHelper::sendPacketWithOutEvent);
        packets.clear();
    }
}
