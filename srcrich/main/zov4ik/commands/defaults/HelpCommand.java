package im.zov4ik.commands.defaults;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import im.zov4ik.zov4ik;
import im.zov4ik.utils.client.managers.api.command.Command;
import im.zov4ik.utils.client.managers.api.command.ICommand;
import im.zov4ik.utils.client.managers.api.command.argument.IArgConsumer;
import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.client.managers.api.command.exception.CommandNotFoundException;
import im.zov4ik.utils.client.managers.api.command.helpers.Paginator;
import im.zov4ik.utils.client.managers.api.command.helpers.TabCompleteHelper;
import im.zov4ik.commands.manager.CommandRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static im.zov4ik.utils.client.managers.api.command.IBaritoneChatControl.FORCE_COMMAND_PREFIX;

public class HelpCommand extends Command {
    zov4ik main;

    protected HelpCommand(zov4ik main) {
        super("help");
        this.main = main;
    }

    
    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireMax(1);

        CommandRepository commandRepository = main.getCommandRepository();
        if (!args.hasAny() || args.is(Integer.class)) {
            Paginator.paginate(
                    args, new Paginator<>(
                            commandRepository.getRegistry().descendingStream()
                                    .filter(command -> !command.hiddenFromHelp())
                                    .collect(Collectors.toList())
                    ),
                    () -> logDirect("Доступные команды:"),
                    command -> {
                        String names = String.join("/", command.getNames());
                        String name = command.getNames().get(0);
                        MutableText shortDescComponent = Text.literal(" - " + command.getShortDesc());
                        shortDescComponent.setStyle(shortDescComponent.getStyle().withColor(Formatting.DARK_GRAY));
                        MutableText namesComponent = Text.literal(names);
                        namesComponent.setStyle(namesComponent.getStyle().withColor(Formatting.WHITE));
                        MutableText hoverComponent = Text.literal("");
                        hoverComponent.setStyle(hoverComponent.getStyle().withColor(Formatting.GRAY));
                        hoverComponent.append(namesComponent);
                        hoverComponent.append("\n" + command.getShortDesc());
                        hoverComponent.append("\n\nНажмите, чтобы просмотреть полную справку о команде");
                        String clickCommand = FORCE_COMMAND_PREFIX + String.format("%s %s", label, command.getNames().get(0));
                        MutableText component = Text.literal(name);
                        component.setStyle(component.getStyle().withColor(Formatting.GRAY));
                        component.append(shortDescComponent);
                        component.setStyle(component.getStyle()
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
                        return component;
                    },
                    FORCE_COMMAND_PREFIX + label
            );
        } else {
            String commandName = args.getString().toLowerCase();
            ICommand command = commandRepository.getCommand(commandName);
            if (command == null) {
                throw new CommandNotFoundException(commandName);
            }
            logDirect("");
            command.getLongDesc().forEach(this::logDirect);
            logDirect("");
            MutableText returnComponent = Text.literal("Нажмите что бы вернуться обратно в меню");
            returnComponent.setStyle(returnComponent.getStyle().withClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    FORCE_COMMAND_PREFIX + label
            )));
            logDirect(returnComponent);
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            return new TabCompleteHelper()
                    .addCommands(zov4ik.getInstance().getCommandRepository())
                    .filterPrefix(args.getString())
                    .stream();
        }
        return Stream.empty();
    }

    
    @Override
    public String getShortDesc() {
        return "Просмотр всех доступных команд";
    }

    
    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "С помощью этой команды можно просмотреть подробную справочную информацию о том, как использовать определенные команды",
                "",
                "Использование:",
                "> help - Перечисляет все команды и их краткие описания.",
                "> help <command> - Отображение справочной информации по конкретной команде."
        );
    }
}
