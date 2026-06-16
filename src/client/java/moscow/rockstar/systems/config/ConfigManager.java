/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.config.ConfigFile;
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;

public class ConfigManager {
    private final List<ConfigFile> configFiles = new ArrayList<ConfigFile>();
    private ConfigFile current;
    private boolean initialized = false;

    public void handle() {
        if (this.getAutoSaveConfig() == null) {
            this.createConfig("autosave");
        }
        if (!this.initialized) {
            this.scanConfigDirectory();
            this.initialized = true;
        }
    }

    public void directionConfig() {
        try {
            File configDir = new File(FileManager.DIRECTORY, "configs");
            String[] commands = new String[]{"explorer", configDir.getAbsolutePath()};
            Runtime.getRuntime().exec(commands);
        }
        catch (Exception e) {
            Rockstar.LOGGER.error("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u043f\u0430\u043f\u043a\u0443 \u0441 \u043a\u043e\u043d\u0444\u0438\u0433\u0430\u043c\u0438: {}", (Object)e.getMessage());
        }
    }

    public void createConfig(String name) {
        if (name == null) {
            return;
        }
        this.refresh();
        ConfigFile config = new ConfigFile(name);
        if (name.equals("autosave")) {
            config.load();
        }
        config.save();
        this.configFiles.add(config);
    }

    public void listConfigs() {
        this.refresh();
        MessageUtility.info(Text.of((String)"\u0421\u043f\u0438\u0441\u043e\u043a \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432:"));
        for (ConfigFile configFile : this.configFiles) {
            int idx = this.configFiles.indexOf(configFile) + 1;
            MessageUtility.info(Text.of((String)("[" + idx + "] " + configFile.getFileName())));
        }
    }

    private void scanConfigDirectory() {
        this.configFiles.clear();
        Path configPath = Paths.get(FileManager.DIRECTORY.getPath(), "configs");
        if (!Files.exists(configPath, new LinkOption[0])) {
            try {
                Files.createDirectories(configPath, new FileAttribute[0]);
                return;
            }
            catch (IOException e) {
                Rockstar.LOGGER.error("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0438\u044e \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432: {}", (Object)e.getMessage());
                return;
            }
        }
        try (Stream<Path> stream = Files.list(configPath);){
            stream.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(path -> path.toString().endsWith(".rock")).forEach(path -> {
                String fileName = path.getFileName().toString();
                String name = fileName.substring(0, fileName.lastIndexOf(46));
                ConfigFile configFile = new ConfigFile(name);
                this.configFiles.add(configFile);
            });
        }
        catch (IOException e) {
            Rockstar.LOGGER.error("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0441\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0438 \u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0438\u0438 \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432: {}", (Object)e.getMessage());
        }
    }

    public ConfigFile getConfig(String name, boolean rescan) {
        if (rescan) {
            this.scanConfigDirectory();
        }
        return this.configFiles.stream().filter(configFile -> configFile.getFileName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public ConfigFile getConfig(String name) {
        return this.getConfig(name, false);
    }

    public ConfigFile getAutoSaveConfig() {
        return this.current != null ? this.current : this.getConfig("autosave", false);
    }

    public void refresh() {
        this.scanConfigDirectory();
    }

    @Generated
    public List<ConfigFile> getConfigFiles() {
        return this.configFiles;
    }

    @Generated
    public ConfigFile getCurrent() {
        return this.current;
    }

    @Generated
    public boolean isInitialized() {
        return this.initialized;
    }

    @Generated
    public void setCurrent(ConfigFile current) {
        this.current = current;
    }
}

