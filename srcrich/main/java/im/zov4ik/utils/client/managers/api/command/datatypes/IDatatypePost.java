package im.zov4ik.utils.client.managers.api.command.datatypes;

import im.zov4ik.utils.client.managers.api.command.exception.CommandException;

public interface IDatatypePost<T, O> extends IDatatype {
    T apply(IDatatypeContext datatypeContext, O original) throws CommandException;
}
