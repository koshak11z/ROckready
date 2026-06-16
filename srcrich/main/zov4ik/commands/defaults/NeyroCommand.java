package im.zov4ik.commands.defaults;

import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import im.zov4ik.utils.client.managers.api.command.Command;
import im.zov4ik.utils.client.managers.api.command.argument.IArgConsumer;
import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.client.managers.api.command.helpers.TabCompleteHelper;
import im.zov4ik.utils.features.aura.rotations.neyro.NeyroManager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class NeyroCommand extends Command {

    public NeyroCommand() {
        super("neyro");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            printUsage();
            return;
        }

        String subcommand = args.getString().toLowerCase(Locale.US);
        NeyroManager manager = NeyroManager.INSTANCE;
        switch (subcommand) {
            case "record" -> handleRecord(manager);
            case "save" -> handleSave(manager, args);
            case "load" -> handleLoad(manager, args);
            case "list" -> handleList(manager);
            case "dir" -> handleDir(manager);
            case "clear" -> handleClear(manager);
            case "delete" -> handleDelete(manager, args);
            default -> {
                logDirect("Неизвестная подкоманда: " + subcommand, Formatting.RED);
                printUsage();
            }
        }
    }

    private void handleRecord(NeyroManager manager) {
        if (manager.isRecording()) {
            manager.stopRecording();
            int frames = manager.getCurrentRecording() != null ? manager.getCurrentRecording().size() : 0;
            logDirect("Запись остановлена. Фреймов: " + frames, Formatting.GREEN);
            return;
        }

        manager.startRecording();
        logDirect("Запись начата. Перед вами появился тренировочный NPC.", Formatting.GREEN);
        logDirect("Наводитесь на него и бейте: ротация будет записываться.", Formatting.GRAY);
    }

    private void handleSave(NeyroManager manager, IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            logDirect("Укажите имя: .neyro save <имя>", Formatting.RED);
            return;
        }

        String name = args.getString();
        if (manager.isRecording()) {
            manager.stopRecording();
        }

        if (manager.saveRecording(name)) {
            int frames = manager.getCurrentRecording() != null ? manager.getCurrentRecording().size() : 0;
            logDirect("Ротация '" + name + "' сохранена. Фреймов: " + frames, Formatting.GREEN);
        } else {
            logDirect("Не удалось сохранить запись. Сначала запишите ротацию.", Formatting.RED);
        }
    }

    private void handleLoad(NeyroManager manager, IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            logDirect("Укажите имя: .neyro load <имя>", Formatting.RED);
            return;
        }

        String name = args.getString();
        if (manager.loadRecording(name)) {
            int frames = manager.getActiveRecording() != null ? manager.getActiveRecording().size() : 0;
            logDirect("Ротация '" + name + "' загружена. Фреймов: " + frames, Formatting.GREEN);
        } else {
            logDirect("Ротация '" + name + "' не найдена.", Formatting.RED);
        }
    }

    private void handleList(NeyroManager manager) {
        List<String> recordings = manager.listRecordings();
        if (recordings.isEmpty()) {
            logDirect("Сохранённых ротаций пока нет.");
            return;
        }

        logDirect("Список ротаций Neyro (" + recordings.size() + "):", Formatting.GREEN);
        for (String name : recordings) {
            logDirect(" - " + name, Formatting.GRAY);
        }
    }

    private void handleDir(NeyroManager manager) {
        Util.getOperatingSystem().open(manager.getNeyroDirectory());
        logDirect("Папка Neyro открыта.", Formatting.GREEN);
    }

    private void handleClear(NeyroManager manager) {
        manager.clearAllRecordings();
        logDirect("Все сохранённые Neyro-записи удалены.", Formatting.GREEN);
    }

    private void handleDelete(NeyroManager manager, IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            logDirect("Укажите имя: .neyro delete <имя>", Formatting.RED);
            return;
        }

        String name = args.getString();
        if (manager.deleteRecording(name)) {
            logDirect("Ротация '" + name + "' удалена.", Formatting.GREEN);
        } else {
            logDirect("Ротация '" + name + "' не найдена.", Formatting.RED);
        }
    }

    private void printUsage() {
        logDirect("Neyro: record, save <name>, load <name>, list, dir, clear, delete <name>", Formatting.GRAY);
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            return Stream.empty();
        }

        if (args.hasExactlyOne()) {
            return new TabCompleteHelper()
                    .prepend("record", "save", "load", "list", "dir", "clear", "delete")
                    .filterPrefix(args.peekString())
                    .stream();
        }

        String subcommand = args.peekString().toLowerCase(Locale.US);
        if ("load".equals(subcommand) || "delete".equals(subcommand)) {
            return new TabCompleteHelper()
                    .append(NeyroManager.INSTANCE.listRecordings().toArray(new String[0]))
                    .filterPrefix(args.getString())
                    .stream();
        }

        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Управление записью и воспроизведением Neyro-ротаций";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Позволяет записывать собственную ротацию и использовать её в Aura через режим Neyro.",
                "",
                "Использование:",
                "> neyro record - начать/остановить запись",
                "> neyro save <name> - сохранить текущую запись",
                "> neyro load <name> - загрузить запись",
                "> neyro list - показать список записей",
                "> neyro dir - открыть папку с JSON",
                "> neyro clear - удалить все записи",
                "> neyro delete <name> - удалить одну запись"
        );
    }
}
