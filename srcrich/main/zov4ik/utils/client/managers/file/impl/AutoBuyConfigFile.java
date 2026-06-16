package im.zov4ik.utils.client.managers.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import im.zov4ik.features.impl.misc.AutoBuy;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.originalitems.ItemRegistry;
import im.zov4ik.features.impl.misc.autobuy.catalog.settings.AutoBuySettingsManager;
import im.zov4ik.features.module.setting.Setting;
import im.zov4ik.features.module.setting.implement.TextSetting;
import im.zov4ik.utils.client.managers.file.ClientFile;
import im.zov4ik.utils.client.managers.file.exception.FileLoadException;
import im.zov4ik.utils.client.managers.file.exception.FileSaveException;
import im.zov4ik.zov4ik;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AutoBuyConfigFile extends ClientFile {
    private static final String DEFAULT_FILE_NAME = "AutoBuyConfig.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AutoBuyConfigFile() {
        super("AutoBuy/AutoBuyConfig");
    }

    @Override
    public void loadFromFile(File path) throws FileLoadException {
        loadFromFile(path, DEFAULT_FILE_NAME);
    }

    @Override
    public void loadFromFile(File path, String fileName) throws FileLoadException {
        File file = resolveConfigFile(path, fileName);
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json != null) {
                Map<String, Boolean> enabledMap = new HashMap<>();
                if (json.has("enabled_items")) {
                    JsonObject enabledItems = json.getAsJsonObject("enabled_items");
                    for (String key : enabledItems.keySet()) {
                        enabledMap.put(key, enabledItems.get(key).getAsBoolean());
                    }
                }

                if (json.has("settings")) {
                    JsonObject settings = json.getAsJsonObject("settings");
                    AutoBuySettingsManager.getInstance().loadFromJson(settings);
                    ItemRegistry.reload();
                }

                applyEnabledItems(enabledMap);
                loadPriceSettings(json);
            }
        } catch (IOException e) {
            throw new FileLoadException("Failed to load AutoBuy config from file", e);
        }
    }

    @Override
    public void saveToFile(File path) throws FileSaveException {
        saveToFile(path, DEFAULT_FILE_NAME);
    }

    @Override
    public void saveToFile(File path, String fileName) throws FileSaveException {
        JsonObject json = new JsonObject();

        JsonObject enabledItems = new JsonObject();
        for (AutoBuyableItem item : ItemRegistry.getAllItems()) {
            enabledItems.addProperty(item.getDisplayName(), item.isEnabled());
            AutoBuySettingsManager.getInstance().saveSettings(item.getDisplayName(), item.getSettings());
        }
        json.add("enabled_items", enabledItems);

        JsonObject settings = AutoBuySettingsManager.getInstance().saveToJson();
        json.add("settings", settings);
        savePriceSettings(json);

        File file = resolveConfigFile(path, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            throw new FileSaveException("Failed to save AutoBuy config to file", e);
        }
    }

    private void applyEnabledItems(Map<String, Boolean> enabledMap) {
        if (enabledMap.isEmpty()) {
            return;
        }

        for (AutoBuyableItem item : ItemRegistry.getAllItems()) {
            Boolean enabled = enabledMap.get(item.getDisplayName());
            if (enabled != null) {
                item.setEnabled(enabled);
            }
        }
    }

    private void savePriceSettings(JsonObject json) {
        AutoBuy autoBuy = resolveAutoBuyModule();
        if (autoBuy == null) {
            return;
        }

        JsonObject prices = new JsonObject();
        for (Setting setting : autoBuy.settings()) {
            if (setting instanceof TextSetting textSetting
                    && (setting.getName().startsWith("price_") || setting.getName().startsWith("durability_"))) {
                prices.addProperty(setting.getName(), textSetting.getText());
            }
        }

        json.add("prices", prices);
    }

    private void loadPriceSettings(JsonObject json) {
        if (!json.has("prices")) {
            return;
        }

        AutoBuy autoBuy = resolveAutoBuyModule();
        if (autoBuy == null) {
            return;
        }

        JsonObject prices = json.getAsJsonObject("prices");
        for (Setting setting : autoBuy.settings()) {
            if (setting instanceof TextSetting textSetting
                    && (setting.getName().startsWith("price_") || setting.getName().startsWith("durability_"))
                    && prices.has(setting.getName())) {
                textSetting.setText(prices.get(setting.getName()).getAsString());
            }
        }
    }

    private AutoBuy resolveAutoBuyModule() {
        if (zov4ik.getInstance() == null || zov4ik.getInstance().getModuleProvider() == null) {
            return null;
        }
        return zov4ik.getInstance().getModuleProvider().get(AutoBuy.class);
    }

    private File resolveConfigFile(File path, String fileName) {
        File autoBuyDir = resolveAutoBuyDir(path);
        File file = new File(autoBuyDir, fileName);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return file;
    }

    private File resolveAutoBuyDir(File path) {
        File baseDir = path;
        if (baseDir != null && baseDir.getParentFile() != null) {
            baseDir = baseDir.getParentFile();
        }
        if (baseDir == null) {
            baseDir = new File(".");
        }

        File autoBuyDir = new File(baseDir, "AutoBuy");
        if (!autoBuyDir.exists()) {
            autoBuyDir.mkdirs();
        }
        return autoBuyDir;
    }
}
