package im.zov4ik.utils.client.managers.api.command.manager;

import net.minecraft.util.Pair;
import im.zov4ik.utils.client.managers.api.command.ICommand;
import im.zov4ik.utils.client.managers.api.command.argument.ICommandArgument;
import im.zov4ik.utils.client.managers.api.command.registry.Registry;

import java.util.List;
import java.util.stream.Stream;

public interface ICommandManager {
    Registry<ICommand> getRegistry();

    ICommand getCommand(String name);

    boolean execute(String string);

    boolean execute(Pair<String, List<ICommandArgument>> expanded);

    Stream<String> tabComplete(Pair<String, List<ICommandArgument>> expanded);

    Stream<String> tabComplete(String prefix);
}
