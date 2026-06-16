/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.commands;

import java.util.List;
import moscow.rockstar.systems.commands.Command;

public record CommandContext(Command command, List<Object> arguments) {
}

