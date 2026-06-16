/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.modules.constructions.swinganim.presets;

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
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPresetFile;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;

public class SwingPresetManager {
    private final List<SwingPresetFile> swingPresetFiles = new ArrayList<SwingPresetFile>();
    private SwingPresetFile current;
    private boolean initialized = false;

    @CompileBytecode
    public void handle() {
        if (this.getAutoSavePreset() == null) {
            this.createPreset("autosave");
        }
        if (!this.initialized) {
            this.scanPresetDirectory();
            this.initialized = true;
        }
    }

    public void directionPreset() {
        String[] commands = new String[]{"explorer " + new File(String.valueOf(FileManager.DIRECTORY) + "/presets", "swing").getAbsolutePath()};
        try {
            Runtime.getRuntime().exec(commands);
        }
        catch (Exception e) {
            Rockstar.LOGGER.error("\u0432\u0441\u0435 \u043d\u0430\u0435\u0431\u043d\u0443\u043b\u043e\u0441\u044c \u0432 dir \u043a\u043e\u043d\u0444\u0438\u0433\u0435 {}", (Object)e.getMessage());
        }
    }

    public void createPreset(String name) {
        if (name == null) {
            return;
        }
        if (this.getPreset(name, false) != null) {
            Rockstar.LOGGER.warn("Preset {} already exists", (Object)name);
            return;
        }
        SwingPresetFile preset = new SwingPresetFile(name);
        if (name.equals("autosave")) {
            preset.load();
        }
        preset.save();
        this.swingPresetFiles.add(preset);
    }

    public void listPresets() {
        MessageUtility.info(Text.of((String)"\u0421\u043f\u0438\u0441\u043e\u043a \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432:"));
        for (SwingPresetFile swingPresetFile : this.swingPresetFiles) {
            int idx = this.swingPresetFiles.indexOf(swingPresetFile) + 1;
            MessageUtility.info(Text.of((String)("[" + idx + "] " + swingPresetFile.getFileName())));
        }
    }

    private void scanPresetDirectory() {
        this.swingPresetFiles.clear();
        Path presetPath = Paths.get(String.valueOf(FileManager.DIRECTORY) + "/presets", "swing");
        if (!Files.exists(presetPath, new LinkOption[0])) {
            try {
                Files.createDirectories(presetPath, new FileAttribute[0]);
                return;
            }
            catch (IOException e) {
                Rockstar.LOGGER.error("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0438\u044e \u043f\u0440\u0435\u0441\u0435\u0442\u043e\u0432: {}", (Object)e.getMessage());
                return;
            }
        }
        try (Stream<Path> stream = Files.list(presetPath);){
            stream.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(path -> path.toString().endsWith(".rock")).forEach(path -> {
                String fileName = path.getFileName().toString();
                String name = fileName.substring(0, fileName.lastIndexOf(46));
                SwingPresetFile swingPresetFile = new SwingPresetFile(name);
                this.swingPresetFiles.add(swingPresetFile);
            });
        }
        catch (IOException e) {
            Rockstar.LOGGER.error("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0441\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0438 \u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0438\u0438 \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432: {}", (Object)e.getMessage());
        }
    }

    public SwingPresetFile getPreset(String name, boolean rescan) {
        if (rescan) {
            this.scanPresetDirectory();
        }
        return this.swingPresetFiles.stream().filter(swingPresetFile -> swingPresetFile.getFileName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public SwingPresetFile getPreset(String name) {
        return this.getPreset(name, false);
    }

    public SwingPresetFile getAutoSavePreset() {
        return this.getPreset("autosave", true);
    }

    public void refresh() {
        this.scanPresetDirectory();
    }

    @Generated
    public List<SwingPresetFile> getSwingPresetFiles() {
        return this.swingPresetFiles;
    }

    @Generated
    public SwingPresetFile getCurrent() {
        return this.current;
    }

    @Generated
    public boolean isInitialized() {
        return this.initialized;
    }

    @Generated
    public void setCurrent(SwingPresetFile current) {
        this.current = current;
    }
}

