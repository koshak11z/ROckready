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
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public final class ConfigCommand {
    private static final ParameterValidator<String> CONFIG_NAME = ValidationResult::ok;
    // Ожидание ответа Да/нет после ".cfg reset". Перехватывается в ChatScreenMixin.
    private static volatile boolean awaitingReset = false;

    public static boolean isAwaitingReset() {
        return awaitingReset;
    }

    private static void requestReset() {
        awaitingReset = true;
        // Кликабельные [Да]/[Нет] — по нажатию отправляется "да"/"нет", которое
        // перехватывается на сетевом уровне (ClientPlayNetworkHandlerMixin) и не уходит в чат.
        MutableText prompt = Text.literal("Вы уверены что хотите сбросить конфиг? ");
        MutableText yes = Text.literal("[Да]");
        yes.setStyle(yes.getStyle()
                .withColor(Formatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "да"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Сбросить конфиг"))));
        MutableText no = Text.literal(" [Нет]");
        no.setStyle(no.getStyle()
                .withColor(Formatting.RED)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "нет"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Отмена"))));
        prompt.append(yes).append(no);
        MessageUtility.info(prompt);
    }

    /**
     * Перехват ответа на запрос сброса. Возвращает true, если сообщение было ответом
     * (Да/нет) и его не нужно отправлять в чат.
     */
    public static boolean tryConfirm(String message) {
        if (!awaitingReset || message == null) {
            return false;
        }
        String m = message.trim().toLowerCase();
        if (m.equals("да") || m.equals("д") || m.equals("yes") || m.equals("y") || m.equals("lf")) {
            awaitingReset = false;
            performReset();
            return true;
        }
        if (m.equals("нет") || m.equals("н") || m.equals("no") || m.equals("n") || m.equals("ytn")) {
            awaitingReset = false;
            MessageUtility.info(Text.of((String)"Сброс конфига отменён"));
            return true;
        }
        // Любой другой ввод снимает ожидание и проходит дальше как обычное сообщение.
        awaitingReset = false;
        return false;
    }

    private static void performReset() {
        Rockstar rock = Rockstar.getInstance();
        // 1. Выключаем все модули (тихо) и снимаем все бинды.
        for (Module module : rock.getModuleManager().getModules()) {
            if (module.isEnabled()) {
                module.setEnabled(false, true);
            }
            module.setKey(-1);
        }
        // 2. Сбрасываем тему к стандартной.
        rock.getThemeManager().reset();
        // 3. Пытаемся сохранить сброшенное состояние (HUD + автосейв-конфиг).
        try {
            rock.getFileManager().writeFile("client");
            if (rock.getConfigManager().getAutoSaveConfig() != null) {
                rock.getConfigManager().getAutoSaveConfig().save();
            }
        } catch (Exception ignored) {
        }
        MessageUtility.info(Text.of((String)"Конфиг сброшен: модули выключены, бинды и тема очищены"));
    }

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
        DIR("dir", "direction"),
        RESET("reset", "\u0441\u0431\u0440\u043e\u0441", "clear");

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
                case 5 -> s -> ConfigCommand.requestReset();
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
