package im.zov4ik.utils.client.managers.api.command.datatypes;

import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.display.interfaces.QuickImports;

import java.util.stream.Stream;

public interface IDatatype extends QuickImports {
    Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException;
}
