/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  lombok.Generated
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.modules.constructions.swinganim.presets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.text.Text;

public class SwingPresetFile
implements IMinecraft {
    private final File file;
    private final String fileName;
    private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
    private final Animation activeAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

    public SwingPresetFile(String fileName) {
        this.fileName = fileName;
        File configsFolder = new File(String.valueOf(FileManager.DIRECTORY) + "/presets", "swing");
        if (!configsFolder.exists()) {
            configsFolder.mkdir();
        }
        this.file = new File(configsFolder, fileName + ".%s".formatted("rock"));
    }

    public void load() {
        if (!this.file.exists()) {
            Rockstar.LOGGER.warn("Config file not found: {}", (Object)this.file.getAbsolutePath());
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file));){
            JsonObject jsonObject = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            JsonObject animation = jsonObject.getAsJsonObject("animation");
            for (Setting setting : Rockstar.getInstance().getSwingManager().getSharedSettings().getSettings()) {
                if (!animation.has(setting.getName())) continue;
                setting.load(animation.get(setting.getName()));
            }
            JsonObject startPhase = jsonObject.getAsJsonObject("startPhase");
            for (Setting setting : Rockstar.getInstance().getSwingManager().getStartPhase().getSettings()) {
                if (!startPhase.has(setting.getName())) continue;
                setting.load(startPhase.get(setting.getName()));
            }
            JsonObject jsonObject2 = jsonObject.getAsJsonObject("endPhase");
            for (Setting setting : Rockstar.getInstance().getSwingManager().getEndPhase().getSettings()) {
                if (!jsonObject2.has(setting.getName())) continue;
                setting.load(jsonObject2.get(setting.getName()));
            }
            if (!this.fileName.equals("autosave")) {
                Rockstar.getInstance().getSwingPresetManager().setCurrent(this);
            }
        }
        catch (Exception e) {
            Rockstar.LOGGER.error("Failed to load config file {}: {}", (Object)this.fileName, (Object)e.getMessage());
        }
    }

    public void save() {
        try {
            if (!this.file.exists() && !this.file.createNewFile()) {
                throw new IOException("Failed to create config file: " + this.file.getAbsolutePath());
            }
            JsonObject json = new JsonObject();
            JsonObject animation = new JsonObject();
            for (Setting setting : Rockstar.getInstance().getSwingManager().getSharedSettings().getSettings()) {
                animation.add(setting.getName(), setting.save());
            }
            json.add("animation", (JsonElement)animation);
            JsonObject startPhase = new JsonObject();
            for (Setting setting : Rockstar.getInstance().getSwingManager().getStartPhase().getSettings()) {
                startPhase.add(setting.getName(), setting.save());
            }
            json.add("startPhase", (JsonElement)startPhase);
            JsonObject jsonObject = new JsonObject();
            for (Setting setting : Rockstar.getInstance().getSwingManager().getEndPhase().getSettings()) {
                jsonObject.add(setting.getName(), setting.save());
            }
            json.add("endPhase", (JsonElement)jsonObject);
            try (FileWriter fileWriter = new FileWriter(this.file);){
                fileWriter.write(FileManager.GSON.toJson((JsonElement)json));
            }
            System.out.println("saved");
            if (!this.fileName.equals("autosave")) {
                Rockstar.getInstance().getSwingPresetManager().setCurrent(this);
            }
        }
        catch (IOException e) {
            Rockstar.LOGGER.error("Failed to save config file", (Throwable)e);
        }
    }

    public void delete() {
        Path filePath = this.file.toPath();
        try {
            Files.delete(filePath);
            Rockstar.getInstance().getSwingPresetManager().getSwingPresetFiles().remove(this);
            Rockstar.LOGGER.info("Config file deleted: {}", (Object)filePath);
        }
        catch (NoSuchFileException e) {
            Rockstar.LOGGER.warn("Tried to delete a file that does not exist: {}", (Object)filePath);
        }
        catch (IOException e) {
            MessageUtility.error(Text.of((String)"\u041f\u0440\u043e\u0438\u0437\u043e\u0448\u043b\u0430 \u043e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u0438\u0438"));
            Rockstar.LOGGER.warn("Failed to delete config file: {}. Reason: {}", (Object)filePath, (Object)e.getMessage());
        }
    }

    private JsonObject getSettingsJsonObject(Module module) {
        JsonObject settingsObject = new JsonObject();
        for (Setting setting : module.getSettings()) {
            settingsObject.add(setting.getName(), setting.save());
        }
        return settingsObject;
    }

    @Generated
    public File getFile() {
        return this.file;
    }

    @Generated
    public String getFileName() {
        return this.fileName;
    }

    @Generated
    public Animation getHoverAnimation() {
        return this.hoverAnimation;
    }

    @Generated
    public Animation getActiveAnimation() {
        return this.activeAnimation;
    }
}

