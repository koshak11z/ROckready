package im.zov4ik.commands;

import im.zov4ik.utils.client.managers.api.command.ICommandSystem;
import im.zov4ik.utils.client.managers.api.command.argparser.IArgParserManager;
import im.zov4ik.commands.argparser.ArgParserManager;

public enum CommandSystem implements ICommandSystem {
    INSTANCE;

    @Override
    public IArgParserManager getParserManager() {
        return ArgParserManager.INSTANCE;
    }
}
