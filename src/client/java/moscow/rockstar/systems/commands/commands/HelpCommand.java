/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.commands.commands;

import java.util.ArrayList;
import java.util.Comparator;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class HelpCommand {
    @Compile
    public Command command() {
        return CommandBuilder.begin("help", b -> b.aliases("\u043f\u043e\u043c\u043e\u0449\u044c", "\u043a\u043e\u043c\u0430\u043d\u0434\u044b", "commands", "helpme").desc("commands.help.description").handler(this::handle)).build();
    }

    @Compile
    private void handle(CommandContext ctx) {
        ArrayList<Command> list = new ArrayList<Command>(Rockstar.getInstance().getCommandManager().commands());
        list.sort(Comparator.comparing(c -> c.names().getFirst(), String.CASE_INSENSITIVE_ORDER));
        ArrayList<String> infos = new ArrayList<String>();
        int counter = 1;
        for (Command command : list) {
            infos.add(String.format("%d) %s%s - %s", counter++, Rockstar.getInstance().getCommandManager().getPrefix(), command.names().getFirst(), Localizator.translate(command.description())));
        }
        MessageUtility.info(Text.of((String)("\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u044b:\n" + String.join((CharSequence)"\n", infos))));
    }
}

