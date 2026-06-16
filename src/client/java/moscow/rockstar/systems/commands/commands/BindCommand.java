/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  org.lwjgl.glfw.GLFW
 */
package moscow.rockstar.systems.commands.commands;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ParameterBuilder;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.game.TextUtility;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class BindCommand {
    @Compile
    public Command command() {
        List<String> moduleNames = Rockstar.getInstance().getModuleManager().getModules().stream().map(module -> module.getName().replace(" ", "")).toList();
        List<String> keyNames = this.getAvailableKeyNames();
        return CommandBuilder.begin("bind", commandBuilder -> commandBuilder.aliases("binds", "\u0431\u0438\u043d\u0434").desc("\u0411\u0438\u043d\u0434 \u043d\u0430 \u043c\u043e\u0434\u0443\u043b\u044c")).param("action", p -> p.literal("add", "delete", "remove", "create", "list")).param("module", p -> p.optional().validator(ParameterBuilder.MODULE).suggests(moduleNames)).param("key", p -> p.optional().validator(text -> text.isBlank() ? ValidationResult.error("key is empty") : ValidationResult.ok(text)).suggests(keyNames)).handler(this::handle).build();
    }

    @Compile
    private void handle(CommandContext context) {
        String action = (String)context.arguments().getFirst();
        Module module = (Module)context.arguments().get(1);
        String keyStr = (String)context.arguments().get(2);
        if (action.equalsIgnoreCase("list")) {
            List<Module> modules = Rockstar.getInstance().getModuleManager().getModules().stream().filter(m -> m.getKey() != -1).toList();
            if (modules.isEmpty()) {
                MessageUtility.info(Text.of((String)"\u0421\u043f\u0438\u0441\u043e\u043a \u0431\u0438\u043d\u0434\u043e\u0432 \u043f\u0443\u0441\u0442"));
                return;
            }
            MessageUtility.info(Text.of((String)"\u0421\u043f\u0438\u0441\u043e\u043a \u0431\u0438\u043d\u0434\u043e\u0432:"));
            for (int i = 0; i < modules.size(); ++i) {
                Module m2 = modules.get(i);
                MessageUtility.info(Text.of((String)(String.valueOf(Formatting.GRAY) + "[" + (i + 1) + "] " + String.valueOf(Formatting.WHITE) + m2.getName() + String.valueOf(Formatting.GRAY) + " (" + TextUtility.getKeyName(m2.getKey()) + ")")));
            }
            return;
        }
        if (module == null) {
            MessageUtility.error(Text.of((String)"\u041c\u043e\u0434\u0443\u043b\u044c \u043d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d"));
            return;
        }
        if (action.equalsIgnoreCase("add") || action.equalsIgnoreCase("create")) {
            if (keyStr == null) {
                MessageUtility.error(Text.of((String)"\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u043d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u0430"));
                return;
            }
            int keyCode = this.getKeyCodeFromString(keyStr);
            if (keyCode == -1) {
                MessageUtility.error(Text.of((String)("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043a\u043b\u0430\u0432\u0438\u0448\u0430: " + keyStr)));
                return;
            }
            module.setKey(keyCode);
            MessageUtility.info(Text.of((String)("\u0411\u0438\u043d\u0434 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d \u043d\u0430 \u043a\u043b\u0430\u0432\u0438\u0448\u0443 " + TextUtility.getKeyName(keyCode))));
        } else if (action.equalsIgnoreCase("delete") || action.equalsIgnoreCase("remove")) {
            module.setKey(-1);
            MessageUtility.info(Text.of((String)("\u0411\u0438\u043d\u0434 \u0443\u0434\u0430\u043b\u0435\u043d \u0441 \u043c\u043e\u0434\u0443\u043b\u044f " + module.getName())));
        }
    }

    private int getKeyCodeFromString(String input) {
        if (input == null || input.isBlank()) {
            return -1;
        }
        input = input.toUpperCase(Locale.ROOT).replace(" ", "_");
        try {
            return (Integer)GLFW.class.getField("GLFW_KEY_" + input).get(null);
        }
        catch (Exception ignored) {
            return -1;
        }
    }

    @Compile
    private List<String> getAvailableKeyNames() {
        return Stream.of(GLFW.class.getFields()).map(Field::getName).filter(name -> name.startsWith("GLFW_KEY_")).map(name -> name.substring("GLFW_KEY_".length())).filter(name -> !name.matches("LAST|UNKNOWN|WORLD_\\d+")).collect(Collectors.toList());
    }
}

