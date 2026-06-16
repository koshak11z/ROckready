/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.target.TargetManager;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class TargetCommand {
    @Compile
    public Command command() {
        return CommandBuilder.begin("target", b -> b.aliases("targets").desc("\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u0442\u0430\u0440\u0433\u0435\u0442\u043e\u0432").param("action", p -> p.literal("add", "remove", "del", "delete", "clear", "list")).param("id", p -> p.optional().validator(ValidationResult::ok)).handler(this::handle)).build();
    }

    @Compile
    private void handle(CommandContext ctx) {
        String action = (String)ctx.arguments().get(0);
        String id = (String)ctx.arguments().get(1);
        TargetManager tm = Rockstar.getInstance().getTargetManager();
        switch (action.toLowerCase()) {
            case "add": {
                tm.addTarget(id);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                tm.removeTarget(id);
                break;
            }
            case "clear": {
                tm.clearTarget();
                break;
            }
            case "list": {
                tm.listTarget();
            }
        }
    }
}

