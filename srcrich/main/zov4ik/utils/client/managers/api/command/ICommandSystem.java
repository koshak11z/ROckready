package im.zov4ik.utils.client.managers.api.command;

import im.zov4ik.utils.client.managers.api.command.argparser.IArgParserManager;

public interface ICommandSystem {
    IArgParserManager getParserManager();
}
