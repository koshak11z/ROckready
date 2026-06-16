package im.zov4ik.commands.defaults;

import im.zov4ik.utils.client.managers.api.command.Command;
import im.zov4ik.utils.client.managers.api.command.argument.IArgConsumer;
import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.client.chat.ChatMessage;
import im.zov4ik.features.impl.misc.Spammer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SpammerCommand extends Command {
    public SpammerCommand() {
        super("spammer", "Устанавливает сообщение для модуля Spammer", "spammer");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            String current = Spammer.getMessage();
            if (current == null || current.isEmpty()) {
                ChatMessage.brandmessage("Сообщение не задано. Использование: .spammer <сообщение>");
            } else {
                ChatMessage.brandmessage("Текущее сообщение: " + current);
            }
            return;
        }
        String msg = args.rawRest().trim();
        if (msg.isEmpty()) {
            ChatMessage.brandmessage("Укажите сообщение: .spammer <сообщение>");
            return;
        }
        Spammer.setMessage(msg);
        ChatMessage.brandmessage("Сообщение Spammer установлено: " + msg);
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Устанавливает сообщение для Spammer.";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Устанавливает сообщение, которое модуль Spammer отправляет на анархиях.",
                "",
                "Использование:",
                ".spammer <сообщение> - задаёт сообщение для рассылки",
                ".spammer - показать текущее сообщение"
        );
    }
}
