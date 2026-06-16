package im.zov4ik.common.repository.rct;

import im.zov4ik.display.hud.Notifications;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.client.managers.event.EventManager;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.utils.display.interfaces.QuickLogger;
import im.zov4ik.utils.interactions.inv.InventoryTask;
import im.zov4ik.utils.math.time.StopWatch;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class RCTRepository implements QuickImports, QuickLogger {
    private final StopWatch stopWatch = new StopWatch();
    private int step = 0;
    private String liteType;
    private int anarchy;

    public RCTRepository(EventManager eventManager) {
        eventManager.register(this);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (step > 0 && e.getPacket() instanceof GameMessageS2CPacket message) {
            String text = message.content().getString().toLowerCase();
            if (!text.contains("хаб") && text.contains("не удалось")) {
                Notifications.getInstance().addList("[RCT] На данную анархию " + Formatting.RED + "нельзя" + Formatting.RESET + " зайти", 3000);
                step = 0;
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (step == 0 || mc.player == null || mc.player.networkHandler == null) {
            return;
        }

        if (step == 1) {
            if (stopWatch.every(600)) {
                sendHubCommand();
            }

            int px = MathHelper.floor(mc.player.getX());
            int py = MathHelper.floor(mc.player.getY());
            int pz = MathHelper.floor(mc.player.getZ());

            if (Math.abs(px - 317) <= 2 && Math.abs(py - 29) <= 2 && Math.abs(pz - 302) <= 2
                    || isLobbyByScoreboard()) {
                step = 2;
                stopWatch.reset();
            }
        } else if (step == 2) {
            if (mc.currentScreen instanceof GenericContainerScreen screen
                    && screen.getTitle().getString().contains("Выбор Лайт анархии")) {
                if (!stopWatch.finished(200)) {
                    return;
                }

                int targetCount = 1;
                if (anarchy >= 1 && anarchy <= 16) {
                    targetCount = 1;
                } else if (anarchy >= 17 && anarchy <= 37) {
                    targetCount = 2;
                } else if (anarchy >= 38 && anarchy <= 53) {
                    targetCount = 3;
                } else if (anarchy >= 54 && anarchy <= 69) {
                    targetCount = 16;
                }

                boolean found = false;
                for (int i = 0; i < screen.getScreenHandler().slots.size(); i++) {
                    Slot slot = screen.getScreenHandler().slots.get(i);
                    if (slot == null || !slot.hasStack()) {
                        continue;
                    }

                    if (slot.getStack().getCount() == targetCount
                            && (slot.getStack().getItem().toString().contains("armor_stand") || i < 9)) {
                        InventoryTask.clickSlot(slot.id, 0, SlotActionType.PICKUP, false);
                        found = true;
                        break;
                    }
                }

                if (found) {
                    step = 3;
                    stopWatch.reset();
                }
            } else {
                if (stopWatch.every(500)) {
                    mc.player.networkHandler.sendChatCommand("lite");
                }
            }
        } else if (step == 3) {
            if (mc.currentScreen instanceof GenericContainerScreen screen
                    && screen.getTitle().getString().contains("Выбор Лайт анархии")) {
                if (!stopWatch.finished(400)) {
                    return;
                }

                for (int i = 0; i < screen.getScreenHandler().slots.size(); i++) {
                    if (anarchy >= 65 && anarchy <= 69) {
                        int targetSlot = switch (anarchy) {
                            case 65 -> 29;
                            case 66 -> 30;
                            case 67 -> 31;
                            case 68 -> 32;
                            case 69 -> 33;
                            default -> -1;
                        };

                        if (targetSlot != -1 && targetSlot < screen.getScreenHandler().slots.size()) {
                            Slot mapped = screen.getScreenHandler().slots.get(targetSlot);
                            InventoryTask.clickSlot(mapped.id, 0, SlotActionType.PICKUP, false);
                            step = 0;
                            mc.player.closeHandledScreen();
                            break;
                        }
                    }

                    Slot slot = screen.getScreenHandler().slots.get(i);
                    if (slot == null || !slot.hasStack()) {
                        continue;
                    }

                    String displayName = slot.getStack().getName().getString().replaceAll("§.", "");
                    String strippedName = displayName.replaceAll("\\s+", "").toLowerCase();
                    boolean isArmorStand = slot.getStack().getItem().toString().contains("armor_stand");

                    boolean match = false;
                    if (slot.getStack().getCount() == anarchy && !isArmorStand) {
                        match = true;
                    } else if (!isArmorStand) {
                        if (strippedName.contains("#" + anarchy)
                                || strippedName.endsWith(String.valueOf(anarchy))
                                || strippedName.contains("лайт" + anarchy)) {
                            match = true;
                        }
                    }

                    if (match) {
                        InventoryTask.clickSlot(slot.id, 0, SlotActionType.PICKUP, false);
                        step = 0;
                        mc.player.closeHandledScreen();
                        break;
                    }
                }
            } else {
                if (stopWatch.every(1000)) {
                    mc.player.networkHandler.sendChatCommand("lite");
                }
            }
        }
    }

    public void reconnect(String type, int anarchy) {
        if (anarchy > 0 && anarchy < 200) {
            this.liteType = type != null ? type : "СолоЛайт";
            this.anarchy = anarchy;
            this.step = 1;
            this.stopWatch.reset();
            sendHubCommand();
        } else {
            Notifications.getInstance().addList("[RCT] Неверный " + Formatting.RED + "номер анархии", 3000);
        }
    }

    private void sendHubCommand() {
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand("hub");
        }
    }

    private boolean isLobbyByScoreboard() {
        if (mc.world == null) {
            return false;
        }
        var scoreboard = mc.world.getScoreboard();
        var objective = scoreboard.getObjectiveForSlot(net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) {
            return false;
        }
        for (var entry : scoreboard.getScoreboardEntries(objective)) {
            String line = net.minecraft.scoreboard.Team
                    .decorateName(scoreboard.getScoreHolderTeam(entry.owner()), entry.name())
                    .getString()
                    .replaceAll("§.", "")
                    .toLowerCase();
            if (line.contains("лобби") || line.contains("lobby")) {
                return true;
            }
        }
        return false;
    }
}
