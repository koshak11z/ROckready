package im.zov4ik.utils.client.managers.api.command.datatypes;

import im.zov4ik.zov4ik;
import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.client.managers.api.command.helpers.TabCompleteHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public enum ConfigFileDataType implements IDatatypeFor<String> {
    INSTANCE;

    @Override
    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        Stream<String> friends = getConfigs()
                .stream()
                .map(String::toString);

        String context = ctx
                .getConsumer()
                .getString();

        return new TabCompleteHelper()
                .append(friends)
                .filterPrefix(context)
                .sortAlphabetically()
                .stream();
    }

    @Override
    public String get(IDatatypeContext datatypeContext) throws CommandException {
        String username = datatypeContext
                .getConsumer()
                .getString();

        return getConfigs().stream()
                .filter(s -> s.equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public List<String> getConfigs() {
        List<String> configs = new ArrayList<>();
        File customDir = new File(zov4ik.getInstance().getClientInfoProvider().clientDir(), "Custom");
        if (!customDir.exists()) {
            customDir.mkdirs();
        }
        File[] configFiles = customDir.listFiles();

        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".json")) {
                    String configName = configFile.getName().replace(".json", "");
                    if (!configs.contains(configName)) {
                        configs.add(configName);
                    }
                }
            }
        }

        File[] legacyFiles = zov4ik.getInstance().getClientInfoProvider().filesDir().listFiles();
        if (legacyFiles != null) {
            for (File configFile : legacyFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".json")) {
                    String configName = configFile.getName().replace(".json", "");
                    if (!configs.contains(configName)) {
                        configs.add(configName);
                    }
                }
            }
        }

        return configs;
    }
}
