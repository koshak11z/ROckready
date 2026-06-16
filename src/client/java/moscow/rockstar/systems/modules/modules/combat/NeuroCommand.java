package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.modules.modules.combat.neyro.NeyroManager;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.text.Text;

import java.util.Locale;

/**
 * Compatibility shim for old source trees.
 * The old .neuro implementation was removed; this class intentionally builds a .neyro command
 * with the Rockstar CommandBuilder API (desc/param), so stale references compile.
 */
@Deprecated
public class NeuroCommand implements IMinecraft {
    public Command command() {
        return CommandBuilder.begin("neyro")
                .desc("compatibility neyro command")
                .param("action", p -> p.literal("record", "stop", "save", "load", "clear", "dir", "list"))
                .param("name", p -> p.optional())
                .handler(this::handle)
                .build();
    }

    private void handle(CommandContext ctx) {
        NeyroManager manager = NeyroManager.INSTANCE;
        String action = String.valueOf(ctx.arguments().get(0)).toLowerCase(Locale.ROOT);
        String name = ctx.arguments().size() > 1 && ctx.arguments().get(1) != null ? String.valueOf(ctx.arguments().get(1)) : "default";
        switch (action) {
            case "record" -> { manager.startRecording(name); info("§aЗапись Neyro начата: " + manager.getLastRecordingName()); }
            case "stop" -> info(manager.stopAndSaveRecording(name) ? "§aЗапись Neyro сохранена: " + manager.getLastRecordingName() : "§cЗапись остановлена, но паттерн пустой");
            case "save" -> info(manager.saveRecording(name) ? "§aNeyro сохранён: " + name : "§cНе удалось сохранить Neyro");
            case "load" -> info(manager.loadRecording(name) ? "§aNeyro загружен: " + name : "§cНе удалось загрузить Neyro");
            case "clear" -> { manager.clearCurrentSession(); info("§eТекущая Neyro-сессия очищена"); }
            case "dir" -> info("§7Neyro dir: " + manager.getNeyroDirectory().getAbsolutePath());
            case "list" -> info("§7Neyro: " + String.join(", ", manager.listRecordings()));
            default -> info("§7Используй .neyro record/stop/save/load/clear/dir/list <name>");
        }
    }

    private static void info(String message) {
        MessageUtility.info(Text.of("§7[Neyro] " + message));
    }
}
