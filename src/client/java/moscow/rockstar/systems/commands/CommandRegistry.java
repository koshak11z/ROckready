/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.StringRange
 *  com.mojang.brigadier.suggestion.Suggestion
 *  com.mojang.brigadier.suggestion.Suggestions
 *  lombok.Generated
 */
package moscow.rockstar.systems.commands;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.Parameter;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.commands.commands.AuthCommand;
import moscow.rockstar.systems.commands.commands.AutoPilotCommand;
import moscow.rockstar.systems.commands.commands.BindCommand;
import moscow.rockstar.systems.commands.commands.CatCommand;
import moscow.rockstar.systems.commands.commands.ConfigCommand;
import moscow.rockstar.systems.commands.commands.FakePlayerCommand;
import moscow.rockstar.systems.commands.commands.FriendCommand;
import moscow.rockstar.systems.commands.commands.HelpCommand;
import moscow.rockstar.systems.commands.commands.InventoryCommand;
import moscow.rockstar.systems.commands.commands.PrefixCommand;
import moscow.rockstar.systems.commands.commands.ReHubCommand;
import moscow.rockstar.systems.commands.commands.SpammerCommand;
import moscow.rockstar.systems.commands.commands.TargetCommand;
import moscow.rockstar.systems.commands.commands.ToggleCommand;
import moscow.rockstar.systems.commands.commands.VclipCommand;
import moscow.rockstar.systems.commands.commands.WaypointsCommand;
import moscow.rockstar.systems.commands.commands.NeyroCommand;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.Initialization;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class CommandRegistry {
    private final List<Command> commands = new ArrayList<Command>();
    private String prefix = ".";

    public void register(Command command) {
        this.commands.add(command);
    }

    @Compile
    @VMProtect(type=VMProtectType.MUTATION)
    @Initialization
    public void initCommands() {
        this.register(new CatCommand().command());
        this.register(new AutoPilotCommand().command());
        this.register(new ConfigCommand().command());
        this.register(new FakePlayerCommand().command());
        this.register(new FriendCommand().command());
        this.register(new HelpCommand().command());
        this.register(new InventoryCommand().command());
        this.register(new PrefixCommand().command());
        this.register(new ReHubCommand().command());
        this.register(new TargetCommand().command());
        this.register(new VclipCommand().command());
        this.register(new AuthCommand().command());
        this.register(new ToggleCommand().command());
        this.register(new WaypointsCommand().command());
        this.register(new BindCommand().command());
        this.register(new SpammerCommand().command());
        this.register(new NeyroCommand().command());
    }

    public List<Command> commands() {
        return Collections.unmodifiableList(this.commands);
    }

    public boolean dispatch(String line) {
        if (!line.startsWith(this.prefix)) {
            return false;
        }
        String[] toks = line.substring(this.prefix.length()).split("\\s+");
        List<String> args = Arrays.asList(toks);
        Pair<Command, Integer> pair = this.findSub(args, null, 0);
        if (pair == null) {
            return false;
        }
        Command cmd = pair.command();
        int idx = pair.index();
        if (!cmd.executable()) {
            return false;
        }
        List<Object> parsed = this.parseArgs(cmd, toks, idx);
        if (parsed == null) {
            return true;
        }
        cmd.handler().execute(new CommandContext(cmd, parsed));
        return true;
    }

    private Pair<Command, Integer> findSub(List<String> args, Command parent, int idx) {
        List<Command> pool;
        List<Command> list = pool = parent == null ? this.commands : parent.subcommands();
        if (idx >= args.size()) {
            return this.createResultOrNull(parent, idx - 1);
        }
        String current = args.get(idx);
        for (Command cmd : pool) {
            for (String name : cmd.names()) {
                if (!name.equalsIgnoreCase(current)) continue;
                Pair<Command, Integer> deeper = this.findSub(args, cmd, idx + 1);
                if (deeper != null) {
                    return deeper;
                }
                return new Pair<Command, Integer>(cmd, idx);
            }
        }
        return this.createResultOrNull(parent, idx - 1);
    }

    private Pair<Command, Integer> createResultOrNull(Command parent, int index) {
        return parent != null ? new Pair<Command, Integer>(parent, index) : null;
    }

    private List<Object> parseArgs(Command cmd, String[] tok, int startIdx) {
        List<Parameter<?>> params = cmd.parameters();
        ArrayList<Object> parsed = new ArrayList<Object>();
        int argCursor = startIdx + 1;
        int tokLen = tok.length;
        for (Parameter<?> p : params) {
            if (p.vararg()) {
                ArrayList vararg = new ArrayList();
                for (int j = argCursor; j < tokLen; ++j) {
                    ValidationResult result = p.validator().validate(tok[j]);
                    if (result instanceof ValidationResult.Error) {
                        return null;
                    }
                    vararg.add(((ValidationResult.Ok)result).value());
                }
                parsed.add(vararg);
                return parsed;
            }
            if (argCursor >= tokLen) {
                if (p.required()) {
                    return null;
                }
                parsed.add(null);
                continue;
            }
            ValidationResult result = p.validator().validate(tok[argCursor]);
            if (result instanceof ValidationResult.Error) {
                return null;
            }
            parsed.add(((ValidationResult.Ok)result).value());
            ++argCursor;
        }
        return parsed;
    }

    public CompletableFuture<Suggestions> autoComplete(String orig, int cursor) {
        Command matched;
        if (!orig.startsWith(this.prefix) || cursor < this.prefix.length()) {
            return Suggestions.empty();
        }
        String text = orig.substring(0, Math.min(cursor, orig.length()));
        String afterPrefix = text.substring(this.prefix.length());
        boolean trailingSpace = afterPrefix.endsWith(" ");
        String trimmed = afterPrefix.trim();
        String[] tokens = trimmed.isEmpty() ? new String[]{} : trimmed.split("\\s+");
        List<Command> pool = this.commands;
        Command current = null;
        int argStart = 0;
        for (int i = 0; i < tokens.length && (matched = this.findExact(pool, tokens[i])) != null; ++i) {
            current = matched;
            argStart = i + 1;
            pool = matched.subcommands();
            if (pool.isEmpty()) break;
        }
        int argsCount = tokens.length - argStart;
        String partial = !trailingSpace && tokens.length > 0 ? tokens[tokens.length - 1] : "";
        int start = Math.max(this.prefix.length(), orig.lastIndexOf(32, Math.max(0, cursor - 1)) + 1);
        StringRange range = StringRange.between((int)start, (int)cursor);
        ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
        if (current == null) {
            String check = partial.toLowerCase();
            for (Command c : pool) {
                String name = c.names().getFirst();
                if (!name.toLowerCase().startsWith(check)) continue;
                suggestions.add(new Suggestion(range, name));
            }
        } else if (!pool.isEmpty() && argsCount == 0) {
            String check = partial.toLowerCase();
            for (Command c : pool) {
                String name = c.names().getFirst();
                if (!name.toLowerCase().startsWith(check)) continue;
                suggestions.add(new Suggestion(range, name));
            }
        } else {
            List<Parameter<?>> params = current.parameters();
            int paramIndex = argsCount - (trailingSpace ? 0 : 1);
            if (paramIndex < 0) {
                paramIndex = 0;
            }
            Parameter<?> param = null;
            if (paramIndex >= params.size()) {
                if (!params.isEmpty() && params.getLast().vararg()) {
                    param = params.getLast();
                }
            } else {
                param = params.get(paramIndex);
            }
            if (param != null) {
                String check = partial.toLowerCase();
                for (String s : param.validator().suggestions(check)) {
                    suggestions.add(new Suggestion(range, s));
                }
            }
        }
        if (!suggestions.isEmpty()) {
            return CompletableFuture.completedFuture(new Suggestions(range, suggestions));
        }
        return Suggestions.empty();
    }

    private Command findExact(List<Command> pool, String token) {
        for (Command c : pool) {
            for (String n : c.names()) {
                if (!n.equalsIgnoreCase(token)) continue;
                return c;
            }
        }
        return null;
    }

    @Generated
    public String getPrefix() {
        return this.prefix;
    }

    @Generated
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private record Pair<T, U>(T command, U index) {
    }
}

