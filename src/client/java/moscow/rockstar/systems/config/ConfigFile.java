/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  lombok.Generated
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.exception.UnknownModuleException;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.modules.modules.visuals.MenuModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.text.Text;

public class ConfigFile
implements IMinecraft {
    private List<Module> modules = Rockstar.getInstance().getModuleManager().getModules();
    private File file;
    private String fileName;

    public ConfigFile(String fileName) {
        this.fileName = fileName;
        File configsFolder = new File(FileManager.DIRECTORY, "configs");
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
            if (!jsonObject.has("modules")) {
                Rockstar.LOGGER.warn("Invalid config format: missing 'modules' array in {}", (Object)this.fileName);
                return;
            }
            JsonArray modulesArray = jsonObject.getAsJsonArray("modules");
            int loadedModules = 0;
            for (JsonElement moduleElement : modulesArray) {
                JsonObject moduleObject = moduleElement.getAsJsonObject();
                if (!moduleObject.has("name")) continue;
                String moduleName = moduleObject.get("name").getAsString();
                boolean enabled = moduleObject.has("enabled") && moduleObject.get("enabled").getAsBoolean();
                int key = moduleObject.has("key") ? moduleObject.get("key").getAsInt() : 0;
                try {
                    Module module = Rockstar.getInstance().getModuleManager().getModule(moduleName);
                    if (!(module instanceof MenuModule)) {
                        module.setEnabled(enabled, true);
                        module.setKey(key);
                    }
                    if (moduleObject.has("settings")) {
                        JsonObject settingsObject = moduleObject.getAsJsonObject("settings");
                        for (Setting setting : module.getSettings()) {
                            if (!settingsObject.has(setting.getName())) continue;
                            setting.load(settingsObject.get(setting.getName()));
                        }
                    }
                    ++loadedModules;
                }
                catch (UnknownModuleException e) {
                    Rockstar.LOGGER.warn("Module not found during config load: {}", (Object)moduleName);
                }
            }
            ClientSounds.MODULE.play(Rockstar.getInstance().getModuleManager().getModule(Sounds.class).getVolume().getCurrentValue(), 1.0f);
            Rockstar.getInstance().getNotificationManager().addNotification(NotificationType.SUCCESS, Localizator.translate("configs.loaded"));
            Rockstar.LOGGER.info("Loaded {} modules from config {}", (Object)loadedModules, (Object)this.fileName);
            if (!this.fileName.equals("autosave")) {
                Rockstar.getInstance().getConfigManager().setCurrent(this);
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
            JsonArray modulesJsonArray = this.getModulesJsonArray();
            json.add("modules", (JsonElement)modulesJsonArray);
            try (FileWriter fileWriter = new FileWriter(this.file);){
                fileWriter.write(FileManager.GSON.toJson((JsonElement)json));
            }
            if (!this.fileName.equals("autosave")) {
                Rockstar.getInstance().getConfigManager().setCurrent(this);
            }
            Rockstar.LOGGER.info("Successfully saved config " + this.fileName);
        }
        catch (IOException e) {
            Rockstar.LOGGER.error("Failed to save config file", (Throwable)e);
        }
    }

    public void delete() {
        if (this.file.exists() && this.file.delete()) {
            Rockstar.getInstance().getConfigManager().getConfigFiles().remove(this);
            MessageUtility.info(Text.of((String)("\u041a\u043e\u043d\u0444\u0438\u0433 " + this.fileName + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0443\u0434\u0430\u043b\u0435\u043d")));
            Rockstar.LOGGER.info("Config file deleted: {}", (Object)this.file.getAbsolutePath());
        } else {
            MessageUtility.error(Text.of((String)"\u041f\u0440\u043e\u0438\u0437\u043e\u0448\u043b\u0430 \u043e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u0438\u0438"));
            Rockstar.LOGGER.warn("Failed to delete config file: {}", (Object)this.file.getAbsolutePath());
        }
    }

    private JsonArray getModulesJsonArray() {
        JsonArray modulesJsonArray = new JsonArray();
        for (Module module : this.modules) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("name", module.getName());
            moduleObject.addProperty("enabled", Boolean.valueOf(module.isEnabled()));
            moduleObject.addProperty("key", (Number)module.getKey());
            moduleObject.add("settings", (JsonElement)this.getSettingsJsonObject(module));
            modulesJsonArray.add((JsonElement)moduleObject);
        }
        return modulesJsonArray;
    }

    private JsonObject getSettingsJsonObject(Module module) {
        JsonObject settingsObject = new JsonObject();
        for (Setting setting : module.getSettings()) {
            settingsObject.add(setting.getName(), setting.save());
        }
        return settingsObject;
    }

    @Generated
    public String getFileName() {
        return this.fileName;
    }
}
