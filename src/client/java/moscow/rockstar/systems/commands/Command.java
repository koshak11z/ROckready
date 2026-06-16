/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.commands;

import java.util.List;
import moscow.rockstar.systems.commands.CommandHandler;
import moscow.rockstar.systems.commands.Parameter;

public interface Command {
    public List<String> names();

    public String description();

    public List<Parameter<?>> parameters();

    public List<Command> subcommands();

    public boolean executable();

    public CommandHandler handler();
}

