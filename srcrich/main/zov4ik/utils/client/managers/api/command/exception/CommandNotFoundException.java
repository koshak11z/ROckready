package im.zov4ik.utils.client.managers.api.command.exception;

import im.zov4ik.utils.client.managers.api.command.ICommand;
import im.zov4ik.utils.client.managers.api.command.argument.ICommandArgument;
import im.zov4ik.utils.display.interfaces.QuickLogger;

import java.util.List;

public class CommandNotFoundException extends CommandException implements QuickLogger {

    public final String command;

    public CommandNotFoundException(String command) {
        super(String.format("Команда не найдена: %s", command));
        this.command = command;
    }

    @Override
    public void handle(ICommand command, List<ICommandArgument> args) {
       logDirect(getMessage());
    }
}
