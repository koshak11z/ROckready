package im.zov4ik.commands.defaults;

import im.zov4ik.features.impl.misc.AutoBuy;
import im.zov4ik.features.module.ModuleProvider;
import im.zov4ik.utils.client.managers.api.command.Command;
import im.zov4ik.utils.client.managers.api.command.argument.IArgConsumer;
import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.client.managers.api.command.helpers.TabCompleteHelper;
import im.zov4ik.utils.client.managers.file.FileController;
import im.zov4ik.utils.client.managers.file.exception.FileSaveException;
import im.zov4ik.zov4ik;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TelegramSettingsCommand extends Command {
    ModuleProvider moduleProvider;
    FileController fileController;

    protected TelegramSettingsCommand(zov4ik main) {
        super("telegram", "tg");
        this.moduleProvider = main.getModuleProvider();
        this.fileController = main.getFileController();
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        AutoBuy autoBuy = moduleProvider.get(AutoBuy.class);
        if (autoBuy == null) {
            logDirect("AutoBuy module not found.", Formatting.RED);
            return;
        }

        String action = args.hasAny() ? args.getString().toLowerCase(Locale.ROOT) : "show";
        switch (action) {
            case "api" -> setApi(autoBuy, args);
            case "chat", "group" -> setChat(autoBuy, args);
            case "show", "status" -> showCurrent(autoBuy);
            default -> logDirect("Usage: .telegram api <token> | .telegram chat global|whitelist|id1,id2 | .telegram show", Formatting.GRAY);
        }
    }

    private void setApi(AutoBuy autoBuy, IArgConsumer args) throws CommandException {
        args.requireMin(1);
        String token = args.rawRest().trim();
        if (token.isBlank()) {
            logDirect("Token cannot be empty.", Formatting.RED);
            return;
        }

        autoBuy.getTelegramApiToken().setText(token);
        saveNow();
        logDirect("Telegram API token updated.", Formatting.GREEN);
    }

    private void setChat(AutoBuy autoBuy, IArgConsumer args) throws CommandException {
        args.requireMin(1);
        String value = args.rawRest().trim();
        if (value.isBlank()) {
            logDirect("Specify mode or ids. Example: .telegram chat global", Formatting.RED);
            return;
        }

        String lower = value.toLowerCase(Locale.ROOT);
        if ("global".equals(lower)) {
            autoBuy.getTelegramChatMode().setSelected("Global");
            saveNow();
            logDirect("Telegram chat mode: GLOBAL", Formatting.GREEN);
            return;
        }

        if ("whitelist".equals(lower)) {
            autoBuy.getTelegramChatMode().setSelected("Whitelist");
            saveNow();
            logDirect("Telegram chat mode: WHITELIST", Formatting.GREEN);
            return;
        }

        List<Long> ids = parseChatIds(value);
        if (ids.isEmpty()) {
            logDirect("Cannot parse ids. Example: .telegram chat -1001,-1002,-1003", Formatting.RED);
            return;
        }

        autoBuy.getTelegramChatMode().setSelected("Whitelist");
        autoBuy.getTelegramGroupId().setText(ids.stream().map(String::valueOf).collect(Collectors.joining(",")));
        saveNow();
        logDirect("Telegram whitelist updated (" + ids.size() + ").", Formatting.GREEN);
    }

    private void showCurrent(AutoBuy autoBuy) {
        String token = autoBuy.getTelegramApiToken().getText();
        String mode = autoBuy.getTelegramChatMode().getSelected();
        String ids = autoBuy.getTelegramGroupId().getText();

        logDirect("Telegram API: " + maskToken(token), Formatting.GRAY);
        logDirect("Telegram chat mode: " + mode, Formatting.GRAY);
        logDirect("Telegram whitelist ids: " + (ids == null || ids.isBlank() ? "not set" : ids), Formatting.GRAY);
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "not set";
        }
        if (token.length() <= 10) {
            return "********";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }

    private List<Long> parseChatIds(String raw) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        String[] parts = raw.split("[,;\\s]+");
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            try {
                long value = Long.parseLong(part.trim());
                if (value != 0L) {
                    ids.add(value);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return List.copyOf(ids);
    }

    private void saveNow() {
        if (fileController == null) {
            return;
        }
        try {
            fileController.saveFiles();
        } catch (FileSaveException exception) {
            logDirect("Failed to save settings: " + exception.getMessage(), Formatting.RED);
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            return new TabCompleteHelper()
                    .prepend("api", "chat", "group", "show")
                    .filterPrefix(args.peekString())
                    .stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Configure AutoBuy Telegram settings";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Configures AutoBuy Telegram settings without GUI.",
                "",
                "Usage:",
                "> telegram api <token> - set Telegram bot API token",
                "> telegram chat global - allow commands from any chat id",
                "> telegram chat whitelist - whitelist mode",
                "> telegram chat id1,id2,id3 - set whitelist ids",
                "> telegram show - show current values"
        );
    }
}
