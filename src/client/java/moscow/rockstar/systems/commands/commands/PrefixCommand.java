/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.CommandRegistry;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class PrefixCommand {
    @Compile
    public Command command() {
        return CommandBuilder.begin("prefix", b -> b.desc("commands.prefix.description").param("action", p -> p.optional().literal("list", "clear", "default", "set", "create")).param("new", p -> p.optional().validator(text -> text.length() > 1 ? ValidationResult.error(Localizator.translate("commands.prefix.invalid_length")) : ValidationResult.ok(text))).handler(this::handle)).build();
    }

    @Compile
    private void handle(CommandContext ctx) {
        String action = (String)ctx.arguments().get(0);
        String newPrefix = (String)ctx.arguments().get(1);
        CommandRegistry registry = Rockstar.getInstance().getCommandManager();
        String current = registry.getPrefix();
        if (action == null) {
            MessageUtility.info(Text.of((String)Localizator.translate("commands.prefix.current", current)));
            return;
        }
        switch (action.toLowerCase()) {
            case "list": {
                MessageUtility.info(Text.of((String)Localizator.translate("commands.prefix.current", current)));
                break;
            }
            case "clear": 
            case "default": 
            case "reset": {
                registry.setPrefix(".");
                MessageUtility.info(Text.of((String)Localizator.translate("commands.prefix.reset")));
                break;
            }
            case "set": 
            case "create": {
                if (newPrefix == null || newPrefix.isEmpty()) {
                    MessageUtility.error(Text.of((String)Localizator.translate("commands.prefix.empty")));
                    return;
                }
                registry.setPrefix(newPrefix);
                MessageUtility.info(Text.of((String)Localizator.translate("commands.prefix.set", newPrefix)));
            }
        }
    }
}

