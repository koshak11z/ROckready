package im.zov4ik.commands.defaults;

import im.zov4ik.zov4ik;
import im.zov4ik.utils.client.managers.api.command.ICommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DefaultCommands {
    public static List<ICommand> createAll() {
        zov4ik main = zov4ik.getInstance();
        List<ICommand> commands = new ArrayList<>(Arrays.asList(
                new ConfigCommand(main),
                new MacroCommand(main),
                new HelpCommand(main),
                new BindCommand(main),
                new WayCommand(main),
                new RCTCommand(main),
                new FriendCommand(),
                new IRCCommand(),
                new PrefixCommand(),
                new TargetCommand(),
                new StaffCommand(),
                new BlockESPCommand(),
                new TabParserCommand(),
                new NeyroCommand(),
                new FakeFpsCommand(),
                new TelegramSettingsCommand(main),
                new SpammerCommand(),
                new AutoSellCommand()
        ));
        return Collections.unmodifiableList(commands);
    }
}
