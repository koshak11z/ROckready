package im.zov4ik.commands.defaults;

import im.zov4ik.utils.client.managers.api.command.Command;
import im.zov4ik.utils.client.managers.api.command.argument.IArgConsumer;
import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.client.managers.api.command.helpers.TabCompleteHelper;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class FakeFpsCommand extends Command {
    private static int fakeFps = -1;
    private static int minFakeFps = -1;
    private static int maxFakeFps = -1;

    public FakeFpsCommand() {
        super("fakefps");
    }

    public static boolean isEnabled() {
        return fakeFps > 0 || (minFakeFps > 0 && maxFakeFps >= minFakeFps);
    }

    public static int getFakeFps() {
        if (fakeFps > 0) {
            return fakeFps;
        }
        if (minFakeFps > 0 && maxFakeFps >= minFakeFps) {
            return ThreadLocalRandom.current().nextInt(minFakeFps, maxFakeFps + 1);
        }
        return -1;
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            if (fakeFps > 0) {
                logDirect("Fake FPS: " + fakeFps, Formatting.GREEN);
            } else if (minFakeFps > 0 && maxFakeFps >= minFakeFps) {
                logDirect("Fake FPS range: " + minFakeFps + "-" + maxFakeFps, Formatting.GREEN);
            } else {
                logDirect("Fake FPS is disabled.", Formatting.GRAY);
            }
            logDirect("Usage: .fakefps <number>, .fakefps <min-max> or .fakefps off", Formatting.GRAY);
            return;
        }

        String value = args.getString();
        if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("reset") || value.equalsIgnoreCase("clear")) {
            fakeFps = -1;
            minFakeFps = -1;
            maxFakeFps = -1;
            logDirect("Fake FPS disabled. F3 now shows real FPS.", Formatting.GREEN);
            return;
        }

        if (value.contains("-")) {
            String[] split = value.split("-", 2);
            if (split.length != 2 || split[0].isBlank() || split[1].isBlank()) {
                throw new CommandException("Usage: .fakefps <number>, .fakefps <min-max> or .fakefps off");
            }

            final int min;
            final int max;
            try {
                min = Integer.parseInt(split[0]);
                max = Integer.parseInt(split[1]);
            } catch (NumberFormatException exception) {
                throw new CommandException("Usage: .fakefps <number>, .fakefps <min-max> or .fakefps off");
            }

            if (min <= 0 || max <= 0) {
                throw new CommandException("FPS range values must be greater than 0.");
            }
            if (min > max) {
                throw new CommandException("Invalid range: min must be <= max.");
            }

            fakeFps = -1;
            minFakeFps = min;
            maxFakeFps = max;
            logDirect("Fake FPS range set to " + minFakeFps + "-" + maxFakeFps + ".", Formatting.GREEN);
            return;
        }

        final int parsedFps;
        try {
            parsedFps = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new CommandException("Usage: .fakefps <number>, .fakefps <min-max> or .fakefps off");
        }

        if (parsedFps <= 0) {
            throw new CommandException("FPS must be greater than 0.");
        }

        fakeFps = parsedFps;
        minFakeFps = -1;
        maxFakeFps = -1;
        logDirect("Fake FPS set to " + fakeFps + ".", Formatting.GREEN);
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            return new TabCompleteHelper()
                    .prepend("off", "reset", "200", "200-1000", "100000")
                    .filterPrefix(args.peekString())
                    .stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Spoofs FPS in F3 debug screen.";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Replaces the FPS number in F3 with a custom value or random range.",
                "",
                "Usage:",
                "> fakefps <number> - set custom FPS for F3",
                "> fakefps <min-max> - random FPS in range for F3",
                "> fakefps off - show real FPS in F3 again"
        );
    }
}
