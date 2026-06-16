/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.commands.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ParameterValidator;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.config.ConfigFile;
import moscow.rockstar.systems.config.ConfigManager;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public final class ConfigCommand {
    private static final ParameterValidator<String> CONFIG_NAME = ValidationResult::ok;

    @Compile
    public Command command() {
        List<String> configNames = Rockstar.getInstance().getConfigManager().getConfigFiles().stream().map(ConfigFile::getFileName).toList();
        return CommandBuilder.begin("config", b -> b.aliases("cfg", "\u043a\u0444\u0433", "\u043a\u043e\u043d\u0444\u0438\u0433").desc("commands.config.description").param("action", p -> p.validator(text -> Action.from(text).<ValidationResult>map(action -> ValidationResult.ok(action)).orElseGet(() -> ValidationResult.error(Localizator.translate("commands.config.invalid_action")))).suggests(Action.allNames())).param("id", p -> p.optional().validator(CONFIG_NAME).suggests(configNames)).handler(this::handle)).build();
    }

    @Compile
    private void handle(CommandContext ctx) {
        Action action = (Action)((Object)ctx.arguments().get(0));
        String id = (String)ctx.arguments().get(1);
        action.createHandler().accept(id);
    }

    private static enum Action {
        SAVE("save", "create", "add", "\u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "\u044b\u0444\u043c\u0443"),
        REMOVE("delete", "remove", "del", "\u0443\u0434\u0430\u043b\u0438\u0442\u044c", "\u0432\u0443\u0434\u0443\u0435\u0443"),
        LIST("list", "\u0434\u0448\u044b\u0435"),
        LOAD("load", "use", "\u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c", "\u0434\u0449\u0444\u0432"),
        DIR("dir", "direction");

        private final List<String> names;

        private Action(String ... names) {
            this.names = Arrays.stream(names).map(String::toLowerCase).collect(Collectors.toList());
        }

        @Compile
        private Consumer<String> createHandler() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> this::saveConfig;
                case 1 -> s -> {
                    if (s != null) {
                        Rockstar.getInstance().getConfigManager().getConfig((String)s).delete();
                    }
                };
                case 3 -> s -> {
                    Rockstar.getInstance().getConfigManager().refresh();
                    if (s != null && Rockstar.getInstance().getConfigManager().getConfig((String)s) != null) {
                        Rockstar.getInstance().getConfigManager().getConfig((String)s).load();
                    }
                };
                case 2 -> s -> Rockstar.getInstance().getConfigManager().listConfigs();
                case 4 -> s -> Rockstar.getInstance().getConfigManager().directionConfig();
            };
        }

        @Compile
        private void saveConfig(String configName) {
            if (configName == null) {
                return;
            }
            ConfigManager configManager = Rockstar.getInstance().getConfigManager();
            configManager.createConfig(configName);
            MessageUtility.info(Text.of((String)Localizator.translate("commands.config.saved", configName)));
        }

        @Compile
        static Optional<Action> from(String input) {
            String key = input.toLowerCase();
            return Arrays.stream(Action.values()).filter(a -> a.names.contains(key)).findFirst();
        }

        @Compile
        static List<String> allNames() {
            return Arrays.stream(Action.values()).map(a -> a.names.getFirst()).collect(Collectors.toList());
        }
    }
}
