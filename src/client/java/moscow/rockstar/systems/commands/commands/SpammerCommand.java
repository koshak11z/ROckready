package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.modules.modules.other.Spammer;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import java.util.List;

public class SpammerCommand {
    @Compile
    public Command command() {
        return CommandBuilder.begin("spammer")
                .desc("Устанавливает сообщение для Spammer")
                .param("message", p -> p.optional().vararg().validator(text -> ValidationResult.ok(text)))
                .handler(context -> {
                    List<Object> args = context.arguments();
                    if (args.isEmpty() || args.getFirst() == null || ((List<?>)args.getFirst()).isEmpty()) {
                        String current = Spammer.getMessage();
                        if (current == null || current.isEmpty()) {
                            MessageUtility.info(Text.of("§e[Spammer] §fСообщение не задано. Использование: .spammer <сообщение>"));
                        } else {
                            MessageUtility.info(Text.of("§e[Spammer] §fТекущее сообщение: §7" + current));
                        }
                        return;
                    }
                    @SuppressWarnings("unchecked")
                    List<Object> words = (List<Object>)args.getFirst();
                    String msg = String.join(" ", words.stream().map(String::valueOf).toList()).trim();
                    if (msg.isEmpty()) {
                        MessageUtility.info(Text.of("§e[Spammer] §fУкажите сообщение: .spammer <сообщение>"));
                        return;
                    }
                    Spammer.setMessage(msg);
                    MessageUtility.info(Text.of("§a[Spammer] §fСообщение установлено: §7" + msg));
                })
                .build();
    }
}
