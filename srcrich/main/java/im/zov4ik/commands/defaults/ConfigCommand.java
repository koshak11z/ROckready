package im.zov4ik.commands.defaults;

import im.zov4ik.main.client.ClientInfoProvider;
import im.zov4ik.utils.client.managers.api.command.Command;
import im.zov4ik.utils.client.managers.api.command.argument.IArgConsumer;
import im.zov4ik.utils.client.managers.api.command.datatypes.ConfigFileDataType;
import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.client.managers.api.command.helpers.Paginator;
import im.zov4ik.utils.client.managers.api.command.helpers.TabCompleteHelper;
import im.zov4ik.utils.client.managers.file.exception.FileLoadException;
import im.zov4ik.utils.client.managers.file.exception.FileSaveException;
import im.zov4ik.utils.client.managers.file.impl.AutoBuyConfigFile;
import im.zov4ik.utils.client.managers.file.impl.ModuleFile;
import im.zov4ik.zov4ik;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static im.zov4ik.utils.client.managers.api.command.IBaritoneChatControl.FORCE_COMMAND_PREFIX;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigCommand extends Command {
    zov4ik main;
    ClientInfoProvider clientInfoProvider;

    protected ConfigCommand(zov4ik main) {
        super("config", "cfg");
        this.main = main;
        this.clientInfoProvider = main.getClientInfoProvider();
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        String arg = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";

        switch (arg) {
            case "load" -> {
                args.requireExactly(1);
                loadConfig(args.getString());
            }
            case "save" -> {
                args.requireExactly(1);
                saveConfig(args.getString());
            }
            case "delete" -> {
                args.requireExactly(1);
                deleteConfig(args.getString());
            }
            case "list" -> {
                args.requireMax(1);
                paginateConfigs(args, label);
            }
            case "dir" -> handleDirSubcommand(args);
            case "autobuy" -> handleAutoBuySubcommand(args, label);
            default -> logDirect("Неизвестная подкоманда: " + arg, Formatting.RED);
        }
    }

    private void handleDirSubcommand(IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            args.requireMax(0);
            openDirectory(getCustomDir(), "Папка с конфигами не найдена");
            return;
        }

        String target = args.getString().toLowerCase(Locale.US);
        args.requireMax(0);
        if ("autobuy".equals(target)) {
            openDirectory(getAutoBuyConfigsDir(), "Папка с конфигами AutoBuy не найдена");
            return;
        }

        logDirect("Неизвестная директория: " + target, Formatting.RED);
    }

    private void handleAutoBuySubcommand(IArgConsumer args, String label) throws CommandException {
        String sub = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";

        switch (sub) {
            case "save" -> {
                args.requireExactly(1);
                saveAutoBuyConfig(args.getString());
            }
            case "load" -> {
                args.requireExactly(1);
                loadAutoBuyConfig(args.getString());
            }
            case "delete" -> {
                args.requireExactly(1);
                deleteAutoBuyConfig(args.getString());
            }
            case "dir" -> {
                args.requireMax(0);
                openDirectory(getAutoBuyConfigsDir(), "Папка с конфигами AutoBuy не найдена");
            }
            case "list" -> {
                args.requireMax(1);
                paginateAutoBuyConfigs(args, label);
            }
            default -> logDirect("Неизвестная подкоманда autobuy: " + sub, Formatting.RED);
        }
    }

    private void saveConfig(String rawName) {
        String name = sanitizeConfigName(rawName);
        if (name.isEmpty()) {
            logDirect("Некорректное имя конфига", Formatting.RED);
            return;
        }

        try {
            ModuleFile moduleFile = createModuleFile();
            moduleFile.saveToFile(getCustomDir(), name + ".json");
            logDirect(String.format("Конфигурация %s сохранена!", name));
        } catch (FileSaveException e) {
            logDirect("Ошибка при сохранении конфига: " + safeMessage(e), Formatting.RED);
        }
    }

    private void loadConfig(String rawName) {
        String name = sanitizeConfigName(rawName);
        if (name.isEmpty()) {
            logDirect("Некорректное имя конфига", Formatting.RED);
            return;
        }

        try {
            ModuleFile moduleFile = createModuleFile();
            File configFile = new File(getCustomDir(), name + ".json");
            if (configFile.exists()) {
                moduleFile.loadFromFile(getCustomDir(), name + ".json");
                logDirect(String.format("Конфигурация %s загружена!", name));
                return;
            }

            File legacyFile = new File(clientInfoProvider.filesDir(), name + ".json");
            if (legacyFile.exists()) {
                moduleFile.loadFromFile(clientInfoProvider.filesDir(), name + ".json");
                moduleFile.saveToFile(getCustomDir(), name + ".json");
                logDirect(String.format("Конфигурация %s загружена из старой папки Files и перенесена в Custom!", name));
                return;
            }

            logDirect(String.format("Конфигурация %s не найдена!", name));
        } catch (FileLoadException e) {
            logDirect("Ошибка при загрузке конфига: " + safeMessage(e), Formatting.RED);
        } catch (FileSaveException e) {
            logDirect("Ошибка при переносе конфига в Custom: " + safeMessage(e), Formatting.RED);
        }
    }

    private void deleteConfig(String rawName) {
        String name = sanitizeConfigName(rawName);
        if (name.isEmpty()) {
            logDirect("Некорректное имя конфига", Formatting.RED);
            return;
        }

        File customFile = new File(getCustomDir(), name + ".json");
        File legacyFile = new File(clientInfoProvider.filesDir(), name + ".json");

        boolean deletedCustom = !customFile.exists() || customFile.delete();
        boolean deletedLegacy = !legacyFile.exists() || legacyFile.delete();

        if (deletedCustom && deletedLegacy) {
            logDirect(String.format("Конфигурация %s удалена!", name));
        } else {
            logDirect(String.format("Не удалось удалить конфигурацию %s", name), Formatting.RED);
        }
    }

    private void saveAutoBuyConfig(String rawName) {
        String name = sanitizeConfigName(rawName);
        if (name.isEmpty()) {
            logDirect("Некорректное имя конфигурации AutoBuy", Formatting.RED);
            return;
        }

        String relativePath = "Configs/" + name + ".json";
        AutoBuyConfigFile autoBuyConfigFile = new AutoBuyConfigFile();

        try {
            autoBuyConfigFile.saveToFile(clientInfoProvider.filesDir());
            autoBuyConfigFile.saveToFile(clientInfoProvider.filesDir(), relativePath);
            logDirect(String.format("AutoBuy конфигурация %s сохранена!", name));
        } catch (FileSaveException e) {
            logDirect("Ошибка при сохранении AutoBuy конфига: " + safeMessage(e), Formatting.RED);
        }
    }

    private void loadAutoBuyConfig(String rawName) {
        String name = sanitizeConfigName(rawName);
        if (name.isEmpty()) {
            logDirect("Некорректное имя конфигурации AutoBuy", Formatting.RED);
            return;
        }

        File file = new File(getAutoBuyConfigsDir(), name + ".json");
        if (!file.exists()) {
            logDirect(String.format("AutoBuy конфигурация %s не найдена!", name));
            return;
        }

        String relativePath = "Configs/" + name + ".json";
        AutoBuyConfigFile autoBuyConfigFile = new AutoBuyConfigFile();

        try {
            autoBuyConfigFile.loadFromFile(clientInfoProvider.filesDir(), relativePath);
            autoBuyConfigFile.saveToFile(clientInfoProvider.filesDir());
            logDirect(String.format("AutoBuy конфигурация %s загружена!", name));
        } catch (FileLoadException | FileSaveException e) {
            logDirect("Ошибка при загрузке AutoBuy конфига: " + safeMessage(e), Formatting.RED);
        }
    }

    private void deleteAutoBuyConfig(String rawName) {
        String name = sanitizeConfigName(rawName);
        if (name.isEmpty()) {
            logDirect("Некорректное имя конфигурации AutoBuy", Formatting.RED);
            return;
        }

        File file = new File(getAutoBuyConfigsDir(), name + ".json");
        if (!file.exists()) {
            logDirect(String.format("AutoBuy конфигурация %s не найдена!", name));
            return;
        }

        if (file.delete()) {
            logDirect(String.format("AutoBuy конфигурация %s удалена!", name));
        } else {
            logDirect(String.format("Не удалось удалить AutoBuy конфигурацию %s", name), Formatting.RED);
        }
    }

    private void paginateConfigs(IArgConsumer args, String label) throws CommandException {
        Paginator.paginate(
                args,
                new Paginator<>(getConfigs()),
                () -> logDirect("Список конфигов:"),
                config -> {
                    MutableText namesComponent = Text.literal(config);
                    namesComponent.setStyle(namesComponent.getStyle().withColor(Formatting.WHITE));
                    return namesComponent;
                },
                FORCE_COMMAND_PREFIX + label
        );
    }

    private void paginateAutoBuyConfigs(IArgConsumer args, String label) throws CommandException {
        Paginator.paginate(
                args,
                new Paginator<>(getAutoBuyConfigs()),
                () -> logDirect("Список AutoBuy конфигов:"),
                config -> {
                    MutableText namesComponent = Text.literal(config);
                    namesComponent.setStyle(namesComponent.getStyle().withColor(Formatting.WHITE));
                    return namesComponent;
                },
                FORCE_COMMAND_PREFIX + label + " autobuy"
        );
    }

    private void openDirectory(File directory, String errorMessage) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try {
            new ProcessBuilder("explorer", directory.getAbsolutePath()).start();
        } catch (IOException e) {
            logDirect(errorMessage + ": " + e.getMessage(), Formatting.RED);
        }
    }

    private ModuleFile createModuleFile() {
        return new ModuleFile(main.getModuleRepository(), main.getDraggableRepository());
    }

    private File getCustomDir() {
        File customDir = new File(clientInfoProvider.clientDir(), "Custom");
        if (!customDir.exists()) {
            customDir.mkdirs();
        }
        return customDir;
    }

    private File getAutoBuyConfigsDir() {
        File autoBuyDir = new File(clientInfoProvider.clientDir(), "AutoBuy");
        if (!autoBuyDir.exists()) {
            autoBuyDir.mkdirs();
        }

        File configsDir = new File(autoBuyDir, "Configs");
        if (!configsDir.exists()) {
            configsDir.mkdirs();
        }
        return configsDir;
    }

    private String sanitizeConfigName(String rawName) {
        if (rawName == null) {
            return "";
        }

        String name = rawName.trim();
        if (name.toLowerCase(Locale.US).endsWith(".json")) {
            name = name.substring(0, name.length() - 5);
        }
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        return name.trim();
    }

    private String safeMessage(Exception e) {
        if (e.getMessage() != null) {
            return e.getMessage();
        }
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            return e.getCause().getMessage();
        }
        return e.getClass().getSimpleName();
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            return new TabCompleteHelper()
                    .sortAlphabetically()
                    .prepend("load", "save", "delete", "list", "dir", "autobuy")
                    .stream();
        }

        String arg = args.getString();
        if (arg.equalsIgnoreCase("autobuy")) {
            if (!args.hasAny()) {
                return new TabCompleteHelper()
                        .sortAlphabetically()
                        .prepend("save", "load", "delete", "list", "dir")
                        .stream();
            }

            String sub = args.getString();
            if (args.hasExactlyOne() && (sub.equalsIgnoreCase("load") || sub.equalsIgnoreCase("delete") || sub.equalsIgnoreCase("save"))) {
                String context = args.getString();
                return new TabCompleteHelper()
                        .append(getAutoBuyConfigs().stream())
                        .filterPrefix(context)
                        .sortAlphabetically()
                        .stream();
            }

            return new TabCompleteHelper()
                    .sortAlphabetically()
                    .prepend("save", "load", "delete", "list", "dir")
                    .filterPrefix(sub)
                    .stream();
        }

        if (args.hasExactlyOne()) {
            if (arg.equalsIgnoreCase("load") || arg.equalsIgnoreCase("save") || arg.equalsIgnoreCase("delete")) {
                return args.tabCompleteDatatype(ConfigFileDataType.INSTANCE);
            }
            if (arg.equalsIgnoreCase("dir")) {
                String context = args.getString();
                return new TabCompleteHelper()
                        .append(Stream.of("autobuy"))
                        .filterPrefix(context)
                        .stream();
            }
        }

        return new TabCompleteHelper()
                .sortAlphabetically()
                .prepend("load", "save", "delete", "list", "dir", "autobuy")
                .filterPrefix(arg)
                .stream();
    }

    @Override
    public String getShortDesc() {
        return "Позволяет сохранять и загружать конфиги, включая AutoBuy профили";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Управление конфигами клиента и профилями AutoBuy.",
                "",
                "Использование:",
                "> config load <name> - Загружает обычный конфиг из папки Custom.",
                "> config save <name> - Сохраняет обычный конфиг в папку Custom.",
                "> config delete <name> - Удаляет обычный конфиг.",
                "> config list - Показывает список обычных конфигов.",
                "> config dir - Открывает папку обычных конфигов.",
                "> config dir autobuy - Открывает папку AutoBuy конфигов.",
                "> config autobuy save <name> - Сохраняет профиль AutoBuy.",
                "> config autobuy load <name> - Загружает профиль AutoBuy.",
                "> config autobuy delete <name> - Удаляет профиль AutoBuy.",
                "> config autobuy list - Показывает список профилей AutoBuy.",
                "> config autobuy dir - Открывает папку профилей AutoBuy."
        );
    }

    public List<String> getConfigs() {
        List<String> configs = new ArrayList<>();

        File[] configFiles = getCustomDir().listFiles();
        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".json")) {
                    String configName = configFile.getName().replace(".json", "");
                    if (!configs.contains(configName)) {
                        configs.add(configName);
                    }
                }
            }
        }

        File[] legacyFiles = clientInfoProvider.filesDir().listFiles();
        if (legacyFiles != null) {
            for (File configFile : legacyFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".json")) {
                    String configName = configFile.getName().replace(".json", "");
                    if (!configs.contains(configName)) {
                        configs.add(configName);
                    }
                }
            }
        }

        return configs;
    }

    public List<String> getAutoBuyConfigs() {
        List<String> configs = new ArrayList<>();
        File[] configFiles = getAutoBuyConfigsDir().listFiles();

        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".json")) {
                    String configName = configFile.getName().replace(".json", "");
                    configs.add(configName);
                }
            }
        }

        return configs;
    }
}
