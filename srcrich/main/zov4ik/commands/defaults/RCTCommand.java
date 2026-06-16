package im.zov4ik.commands.defaults;

import im.zov4ik.common.repository.rct.RCTRepository;
import im.zov4ik.display.hud.Notifications;
import im.zov4ik.utils.client.managers.api.command.Command;
import im.zov4ik.utils.client.managers.api.command.argument.IArgConsumer;
import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.client.packet.network.Network;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.zov4ik;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class RCTCommand extends Command implements QuickImports {
    private final RCTRepository repository;

    protected RCTCommand(zov4ik main) {
        super("rct");
        repository = main.getRCTRepository();
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        if (Network.isPvp()) {
            Notifications.getInstance().addList("[RCT] Вы находитесь в режиме " + Formatting.RED + "пвп", 3000);
            return;
        }

        String targetType = null;
        int targetNum = -1;

        if (args.hasAny()) {
            args.requireMin(1);
            targetNum = args.getArgs().getFirst().getAs(Integer.class);
            targetType = "СолоЛайт";
        } else {
            targetNum = Network.getAnarchy();
            if (mc.world != null) {
                Scoreboard scoreboard = mc.world.getScoreboard();
                ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
                if (objective != null) {
                    for (ScoreboardEntry entry : scoreboard.getScoreboardEntries(objective)) {
                        String text = Team.decorateName(scoreboard.getScoreHolderTeam(entry.owner()), entry.name())
                                .getString()
                                .replaceAll("§.", "");
                        if (text.contains("Лайт")) {
                            if (text.contains("СолоЛайт")) {
                                targetType = "СолоЛайт";
                            } else if (text.contains("ДуоЛайт")) {
                                targetType = "ДуоЛайт";
                            } else if (text.contains("ТриоЛайт")) {
                                targetType = "ТриоЛайт";
                            } else if (text.contains("КланЛайт")) {
                                targetType = "КланЛайт";
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (targetType != null && targetNum != -1) {
            repository.reconnect(targetType, targetNum);
        } else {
            Notifications.getInstance().addList("[RCT] Не удалось найти Лайт анархию в скорборде", 3000);
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Перезаходит на анархию HolyWorld";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Перезаходит на анархию HolyWorld",
                "",
                "Использование:",
                "> rct <номер> - Заходит на <номер> СолоЛайт",
                "> rct - Перезаходит на анархию из скорборда (Лайт)"
        );
    }
}
