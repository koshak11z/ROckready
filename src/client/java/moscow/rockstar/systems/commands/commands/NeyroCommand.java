package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.modules.modules.combat.neyro.NeyroManager;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import java.util.List;
import java.util.Locale;

public class NeyroCommand {
    @Compile
    public Command command() {
        return CommandBuilder.begin("neyro")
                .desc("Управление записью и воспроизведением Neyro-ротаций")
                .param("action", p -> p.literal("record", "save", "load", "list", "dir", "clear", "delete", "stop"))
                .param("name", p -> p.optional().suggests(NeyroManager.INSTANCE.listRecordings()))
                .handler(this::handle)
                .build();
    }

    private void handle(CommandContext ctx) {
        NeyroManager manager = NeyroManager.INSTANCE;
        String action = ((String)ctx.arguments().get(0)).toLowerCase(Locale.US);
        String name = ctx.arguments().size() > 1 ? (String)ctx.arguments().get(1) : null;
        switch (action) {
            case "record" -> handleRecord(manager, name);
            case "stop" -> handleStop(manager, name);
            case "save" -> handleSave(manager, name);
            case "load" -> handleLoad(manager, name);
            case "list" -> handleList(manager);
            case "dir" -> handleDir(manager);
            case "clear" -> handleClear(manager);
            case "delete" -> handleDelete(manager, name);
            default -> printUsage();
        }
    }

    private void handleRecord(NeyroManager manager, String name) {
        if (manager.isRecording()) {
            handleStop(manager, name);
            return;
        }
        manager.startRecording(name == null || name.isBlank() ? "default" : name);
        MessageUtility.info(Text.of("§a[Neyro] §fЗапись начата: §e" + manager.getLastRecordingName()));
        MessageUtility.info(Text.of("§7Теперь запись идёт сама по tick-событию. После записи: §e.neyro stop§7 или §e.neyro save <name>"));
    }

    private void handleStop(NeyroManager manager, String name) {
        if (!manager.isRecording()) {
            MessageUtility.info(Text.of("§e[Neyro] §fЗапись не активна."));
            return;
        }
        int frames = manager.getCurrentRecording() != null ? manager.getCurrentRecording().size() : 0;
        boolean saved = manager.stopAndSaveRecording(name);
        if (saved) {
            MessageUtility.info(Text.of("§a[Neyro] §fЗапись остановлена и сохранена: §e" + manager.getLastRecordingName() + " §7(" + frames + " фреймов)"));
        } else {
            MessageUtility.error(Text.of("§c[Neyro] §fЗапись остановлена, но не сохранена: фреймов §e" + frames));
        }
    }

    private void handleSave(NeyroManager manager, String name) {
        if (name == null || name.isBlank()) {
            MessageUtility.error(Text.of("§c[Neyro] §fУкажите имя: .neyro save <имя>"));
            return;
        }
        if (manager.isRecording()) manager.stopRecording();
        if (manager.saveRecording(name)) {
            int frames = manager.getCurrentRecording() != null ? manager.getCurrentRecording().size() : 0;
            MessageUtility.info(Text.of("§a[Neyro] §fРотация '§e" + name + "§f' сохранена. Фреймов: §e" + frames));
        } else {
            MessageUtility.error(Text.of("§c[Neyro] §fНе удалось сохранить запись. Сначала запишите ротацию."));
        }
    }

    private void handleLoad(NeyroManager manager, String name) {
        if (name == null || name.isBlank()) {
            MessageUtility.error(Text.of("§c[Neyro] §fУкажите имя: .neyro load <имя>"));
            return;
        }
        if (manager.loadRecording(name)) {
            manager.startPlayback();
            int frames = manager.getActiveRecording() != null ? manager.getActiveRecording().size() : 0;
            MessageUtility.info(Text.of("§a[Neyro] §fРотация '§e" + name + "§f' загружена. Фреймов: §e" + frames));
            MessageUtility.info(Text.of("§7Выберите режим поворотов §eNeyro§7 в AuraLegacy."));
        } else {
            MessageUtility.error(Text.of("§c[Neyro] §fРотация '" + name + "' не найдена."));
        }
    }

    private void handleList(NeyroManager manager) {
        List<String> recordings = manager.listRecordings();
        if (recordings.isEmpty()) {
            MessageUtility.info(Text.of("§e[Neyro] §fСохранённых ротаций пока нет."));
            return;
        }
        MessageUtility.info(Text.of("§a[Neyro] §fСписок ротаций (" + recordings.size() + "):"));
        for (String recording : recordings) MessageUtility.info(Text.of("§7- §f" + recording));
    }

    private void handleDir(NeyroManager manager) {
        Util.getOperatingSystem().open(manager.getNeyroDirectory());
        MessageUtility.info(Text.of("§a[Neyro] §fПапка Neyro открыта."));
    }

    private void handleClear(NeyroManager manager) {
        manager.clearAllRecordings();
        MessageUtility.info(Text.of("§a[Neyro] §fВсе сохранённые Neyro-записи удалены."));
    }

    private void handleDelete(NeyroManager manager, String name) {
        if (name == null || name.isBlank()) {
            MessageUtility.error(Text.of("§c[Neyro] §fУкажите имя: .neyro delete <имя>"));
            return;
        }
        if (manager.deleteRecording(name)) MessageUtility.info(Text.of("§a[Neyro] §fРотация '§e" + name + "§f' удалена."));
        else MessageUtility.error(Text.of("§c[Neyro] §fРотация '" + name + "' не найдена."));
    }

    private void printUsage() {
        MessageUtility.info(Text.of("§7Neyro: record [name], stop [name], save <name>, load <name>, list, dir, clear, delete <name>"));
    }
}
