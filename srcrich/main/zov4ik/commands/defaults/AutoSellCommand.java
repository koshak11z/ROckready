package im.zov4ik.commands.defaults;

import im.zov4ik.utils.client.managers.api.command.Command;
import im.zov4ik.utils.client.managers.api.command.argument.IArgConsumer;
import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.features.impl.misc.AutoSell;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class AutoSellCommand extends Command {
    public AutoSellCommand() {
        super("autosell", "Управление модулем AutoSell", "autosell");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        AutoSell module = AutoSell.getInstance();
        if (module == null) {
            ChatMessage.brandmessage("§cAutoSell не найден");
            return;
        }
        if (!args.hasAny()) {
            sendUsage();
            return;
        }
        String first = args.peekString();
        String lower = first.toLowerCase(Locale.ROOT);
        if (lower.equals("stop") || lower.equals("off")) {
            if (module.isState()) module.switchState();
            ChatMessage.brandmessage("§cAutoSell выключен");
            return;
        }
        if (lower.equals("start") || lower.equals("on")) {
            args.getString();
            if (!module.isState()) module.switchState();
            ChatMessage.brandmessage("§aAutoSell включён");
            return;
        }
        int price;
        int stackSize;
        try {
            price = Integer.parseInt(args.getString());
            stackSize = Integer.parseInt(args.getString());
        } catch (NumberFormatException ex) {
            sendUsage();
            return;
        }
        if (price <= 0 || stackSize <= 0) {
            ChatMessage.brandmessage("§cЦена и размер стака должны быть > 0");
            return;
        }
        module.applyCommandArgs(price, stackSize);
        if (!module.isState()) module.switchState();
        ChatMessage.brandmessage("§aAutoSell включён: цена=" + price + " стак=" + stackSize);
    }

    private void sendUsage() {
        ChatMessage.helpmessage("Использование .autosell:");
        ChatMessage.brandmessage(".autosell <цена> <размер_стака> - включить с параметрами");
        ChatMessage.brandmessage(".autosell stop - выключить");
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (!args.hasAny()) return Stream.of("start", "stop");
        if (args.hasExactlyOne()) {
            String p = args.peekString().toLowerCase(Locale.ROOT);
            return Stream.of("start", "stop").filter(s -> s.startsWith(p));
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Управляет модулем AutoSell.";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Управляет модулем AutoSell для /ah sellgui.",
                "",
                "Использование:",
                ".autosell <цена> <размер_стака> - задаёт параметры и включает модуль",
                ".autosell stop - выключает модуль"
        );
    }
}
