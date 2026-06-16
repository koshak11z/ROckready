package im.zov4ik.utils.client.managers.api.command.exception;

import im.zov4ik.utils.client.managers.api.command.ICommand;
import im.zov4ik.utils.client.managers.api.command.argument.ICommandArgument;
import im.zov4ik.utils.display.interfaces.QuickLogger;

import java.util.List;

public class CommandUnhandledException extends RuntimeException implements ICommandException, QuickLogger {

    public CommandUnhandledException(String message) {
        super(message);
    }

    public CommandUnhandledException(Throwable cause) {
        super(cause);
    }

    @Override
    public void handle(ICommand command, List<ICommandArgument> args) {
    }
}
