/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.client.session.Session
 *  net.minecraft.client.session.Session$AccountType
 */
package moscow.rockstar.systems.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.config.ConfigFile;
import moscow.rockstar.systems.file.ClientFile;
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.systems.file.api.FileInfo;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingManager;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingPhase;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPreset;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPresetManager;
import moscow.rockstar.systems.modules.modules.other.AutoAuth;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.systems.theme.ThemeManager;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.hud.impl.VanillaHudElement;
import moscow.rockstar.ui.mainmenu.alt.AltManager;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.session.Session;

@FileInfo(name="client")
public class ClientDataFile
extends ClientFile
implements IMinecraft {
    @Override
    public void write() {
        JsonObject json = new JsonObject();
        json.addProperty("username", mc.getSession().getUsername());
        json.addProperty("theme", Rockstar.getInstance().getThemeManager().getCurrentTheme().name());
        json.add("customTheme", (JsonElement)this.getCustomThemeJsonObject());
        json.addProperty("swing", Rockstar.getInstance().getSwingManager().getCurrent());
        json.add("hudElements", (JsonElement)this.getHudElementsJsonArray());
        json.add("alts", (JsonElement)this.getAltsJsonArray());
        json.add("friends", (JsonElement)this.getFriendsJsonArray());
        json.add("colorPickerPresets", (JsonElement)this.getColorPickerPresetsJsonArray());
        json.add("password", (JsonElement)this.getPassword());
        ConfigFile currentConfig = Rockstar.getInstance().getConfigManager().getCurrent();
        if (currentConfig != null) {
            json.addProperty("lastConfig", currentConfig.getFileName());
        }
        try (FileWriter writer = new FileWriter(this.file);){
            writer.write(FileManager.GSON.toJson((JsonElement)json));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read() {
        try (FileReader reader = new FileReader(this.getFile());){
            JsonObject object = (JsonObject)FileManager.GSON.fromJson((Reader)reader, JsonObject.class);
            if (object.has("username")) {
                String username = object.get("username").getAsString();
                Session session = new Session(username, UUID.randomUUID(), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
            }
            if (object.has("password")) {
                this.loadPass(object.getAsJsonArray("password"));
            }
            if (object.has("swing")) {
                String swing = object.get("swing").getAsString();
                SwingManager swingManager = Rockstar.getInstance().getSwingManager();
                SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
                for (SwingPreset value : Rockstar.getInstance().getSwingManager().getPresets()) {
                    if (!value.getName().equals(swing)) continue;
                    swingManager.getBezier().start(value.getBezierStart()).end(value.getBezierEnd());
                    swingManager.getBack().enabled(value.isSwingBack());
                    swingManager.getSpeed().setCurrentValue(value.getSpeed());
                    SwingPhase start = swingManager.getStartPhase();
                    start.getAnchorX().setCurrentValue(value.getFrom().anchorX());
                    start.getAnchorY().setCurrentValue(value.getFrom().anchorY());
                    start.getAnchorZ().setCurrentValue(value.getFrom().anchorZ());
                    start.getMoveX().setCurrentValue(value.getFrom().moveX());
                    start.getMoveY().setCurrentValue(value.getFrom().moveY());
                    start.getMoveZ().setCurrentValue(value.getFrom().moveZ());
                    start.getRotateX().setCurrentValue(value.getFrom().rotateX());
                    start.getRotateY().setCurrentValue(value.getFrom().rotateY());
                    start.getRotateZ().setCurrentValue(value.getFrom().rotateZ());
                    SwingPhase end = swingManager.getEndPhase();
                    end.getAnchorX().setCurrentValue(value.getTo().anchorX());
                    end.getAnchorY().setCurrentValue(value.getTo().anchorY());
                    end.getAnchorZ().setCurrentValue(value.getTo().anchorZ());
                    end.getMoveX().setCurrentValue(value.getTo().moveX());
                    end.getMoveY().setCurrentValue(value.getTo().moveY());
                    end.getMoveZ().setCurrentValue(value.getTo().moveZ());
                    end.getRotateX().setCurrentValue(value.getTo().rotateX());
                    end.getRotateY().setCurrentValue(value.getTo().rotateY());
                    end.getRotateZ().setCurrentValue(value.getTo().rotateZ());
                    swingManager.setCurrent(swing);
                }
            }
            if (object.has("theme")) {
                String themeName = object.get("theme").getAsString();
                try {
                    Theme theme = Theme.valueOf(themeName);
                    Rockstar.getInstance().getThemeManager().setCurrentTheme(theme);
                }
                catch (IllegalArgumentException e) {
                    Rockstar.getInstance().getThemeManager().setCurrentTheme(Theme.DARK);
                }
            }
            if (object.has("customTheme")) {
                this.loadCustomTheme(object.getAsJsonObject("customTheme"));
            }
            if (object.has("friends")) {
                JsonArray friendsArray = object.getAsJsonArray("friends");
                Rockstar.getInstance().getFriendManager().clear();
                for (JsonElement friendElement : friendsArray) {
                    Rockstar.getInstance().getFriendManager().add(friendElement.getAsString());
                }
            }
            if (object.has("colorPickerPresets")) {
                this.loadColorPickerPresets(object.getAsJsonArray("colorPickerPresets"));
            }
            if (object.has("alts")) {
                AltManager.clear();
                for (JsonElement altElement : object.getAsJsonArray("alts")) {
                    AltManager.add(altElement.getAsString());
                }
            }
            if (object.has("hudElements")) {
                JsonArray hudElementsArray = object.getAsJsonArray("hudElements");
                for (JsonElement elemObj : hudElementsArray) {
                    JsonObject elementObject = elemObj.getAsJsonObject();
                    String name = elementObject.get("name").getAsString();
                    float x = elementObject.get("x").getAsFloat();
                    float y = elementObject.get("y").getAsFloat();
                    boolean showing = elementObject.get("showing").getAsBoolean();
                    Object element = Rockstar.getInstance().getHud().getElementByName(name);
                    if (element == null) continue;
                    ((HudElement)element).setX(x);
                    ((HudElement)element).setY(y);
                    ((HudElement)element).setShowing(showing);
                    if (element instanceof VanillaHudElement vanilla && elementObject.has("offX") && elementObject.has("offY")) {
                        vanilla.setOffsets(elementObject.get("offX").getAsFloat(), elementObject.get("offY").getAsFloat());
                    }
                    if (!elementObject.has("settings")) continue;
                    JsonObject settingsObject = elementObject.getAsJsonObject("settings");
                    for (Setting setting : ((HudElement)element).getSettings()) {
                        if (!settingsObject.has(setting.getName())) continue;
                        setting.load(settingsObject.get(setting.getName()));
                    }
                }
            }
            if (object.has("lastConfig")) {
                String configName = object.get("lastConfig").getAsString();
                ConfigFile config = Rockstar.getInstance().getConfigManager().getConfig(configName);
                if (config != null) {
                    config.load();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JsonArray getHudElementsJsonArray() {
        JsonArray hudElementsArray = new JsonArray();
        for (HudElement element : Rockstar.getInstance().getHud().getElements()) {
            // Never write NaN coords — Gson refuses NaN and would fail the ENTIRE config save,
            // wiping every HUD position on the next launch.
            if (Float.isNaN(element.getX()) || Float.isNaN(element.getY())) {
                continue;
            }
            JsonObject elementObject = new JsonObject();
            elementObject.addProperty("name", element.getName());
            elementObject.addProperty("x", (Number)Float.valueOf(element.getX()));
            elementObject.addProperty("y", (Number)Float.valueOf(element.getY()));
            elementObject.addProperty("showing", Boolean.valueOf(element.isShowing()));
            if (element instanceof VanillaHudElement vanilla) {
                elementObject.addProperty("offX", (Number)Float.valueOf(vanilla.getOffX()));
                elementObject.addProperty("offY", (Number)Float.valueOf(vanilla.getOffY()));
            }
            elementObject.add("settings", (JsonElement)this.getSettingsJsonObject(element));
            hudElementsArray.add((JsonElement)elementObject);
        }
        return hudElementsArray;
    }

    private JsonObject getSettingsJsonObject(HudElement element) {
        JsonObject settingsObject = new JsonObject();
        for (Setting setting : element.getSettings()) {
            settingsObject.add(setting.getName(), setting.save());
        }
        return settingsObject;
    }

    private JsonArray getAltsJsonArray() {
        JsonArray altsArray = new JsonArray();
        for (String name : AltManager.getAlts()) {
            altsArray.add(name);
        }
        return altsArray;
    }

    private JsonArray getFriendsJsonArray() {
        JsonArray friendsJsonArray = new JsonArray();
        for (String friendsName : Rockstar.getInstance().getFriendManager().listFriends()) {
            friendsJsonArray.add(friendsName);
        }
        return friendsJsonArray;
    }

    private JsonArray getPassword() {
        JsonArray passwordJsonArray = new JsonArray();
        for (Map.Entry<String, String> pass : Rockstar.getInstance().getModuleManager().getModule(AutoAuth.class).listPassword().entrySet()) {
            JsonObject passObject = new JsonObject();
            passObject.addProperty("nick", pass.getValue());
            passObject.addProperty("pass", pass.getKey());
            passwordJsonArray.add((JsonElement)passObject);
        }
        return passwordJsonArray;
    }

    private JsonObject getCustomThemeJsonObject() {
        ThemeManager themeManager = Rockstar.getInstance().getThemeManager();
        JsonObject customThemeObject = new JsonObject();
        customThemeObject.add("accent", (JsonElement)this.colorToJson(themeManager.getCustomAccentColor()));
        customThemeObject.add("text", (JsonElement)this.colorToJson(themeManager.getCustomTextColor()));
        customThemeObject.add("guiTextActive", (JsonElement)this.colorToJson(themeManager.getCustomGuiTextActiveColor()));
        customThemeObject.add("guiTextInactive", (JsonElement)this.colorToJson(themeManager.getCustomGuiTextInactiveColor()));
        customThemeObject.add("headerText", (JsonElement)this.colorToJson(themeManager.getCustomHeaderTextColor()));
        customThemeObject.add("logoBackground", (JsonElement)this.colorToJson(themeManager.getCustomLogoBackgroundColor()));
        customThemeObject.add("logoText", (JsonElement)this.colorToJson(themeManager.getCustomLogoTextColor()));
        customThemeObject.add("visuals", (JsonElement)this.colorToJson(themeManager.getCustomTargetESPColor()));
        customThemeObject.add("targetESP", (JsonElement)this.colorToJson(themeManager.getCustomTargetESPColor()));
        customThemeObject.add("world", (JsonElement)this.colorToJson(themeManager.getCustomWorldColor()));
        customThemeObject.add("background", (JsonElement)this.colorToJson(themeManager.getCustomBackgroundColor()));
        customThemeObject.add("additional", (JsonElement)this.colorToJson(themeManager.getCustomAdditionalColor()));
        customThemeObject.add("outline", (JsonElement)this.colorToJson(themeManager.getCustomOutlineColor()));
        customThemeObject.add("flat", (JsonElement)this.colorToJson(themeManager.getCustomFlatColor()));
        customThemeObject.add("sliderTrack", (JsonElement)this.colorToJson(themeManager.getCustomSliderTrackColor()));
        customThemeObject.add("sliderCircle", (JsonElement)this.colorToJson(themeManager.getCustomSliderCircleColor()));
        customThemeObject.add("sliderWindow", (JsonElement)this.colorToJson(themeManager.getCustomSliderWindowColor()));
        customThemeObject.add("tooltipText", (JsonElement)this.colorToJson(themeManager.getCustomTooltipTextColor()));
        return customThemeObject;
    }

    private void loadCustomTheme(JsonObject customThemeObject) {
        ThemeManager themeManager = Rockstar.getInstance().getThemeManager();
        if (customThemeObject.has("accent")) {
            themeManager.setCustomAccentColor(this.jsonToColor(customThemeObject.getAsJsonObject("accent")));
        }
        if (customThemeObject.has("text")) {
            themeManager.setCustomTextColor(this.jsonToColor(customThemeObject.getAsJsonObject("text")));
        }
        if (customThemeObject.has("guiTextActive")) {
            themeManager.setCustomGuiTextActiveColor(this.jsonToColor(customThemeObject.getAsJsonObject("guiTextActive")));
        }
        if (customThemeObject.has("guiTextInactive")) {
            themeManager.setCustomGuiTextInactiveColor(this.jsonToColor(customThemeObject.getAsJsonObject("guiTextInactive")));
        }
        if (customThemeObject.has("headerText")) {
            themeManager.setCustomHeaderTextColor(this.jsonToColor(customThemeObject.getAsJsonObject("headerText")));
        }
        if (customThemeObject.has("logoBackground")) {
            themeManager.setCustomLogoBackgroundColor(this.jsonToColor(customThemeObject.getAsJsonObject("logoBackground")));
        }
        if (customThemeObject.has("logoText")) {
            themeManager.setCustomLogoTextColor(this.jsonToColor(customThemeObject.getAsJsonObject("logoText")));
        }
        if (customThemeObject.has("visuals")) {
            themeManager.setCustomTargetESPColor(this.jsonToColor(customThemeObject.getAsJsonObject("visuals")));
        } else {
            if (customThemeObject.has("targetESP")) {
                themeManager.setCustomTargetESPColor(this.jsonToColor(customThemeObject.getAsJsonObject("targetESP")));
            }
            if (customThemeObject.has("world")) {
                themeManager.setCustomWorldColor(this.jsonToColor(customThemeObject.getAsJsonObject("world")));
            }
        }
        if (customThemeObject.has("background")) {
            themeManager.setCustomBackgroundColor(this.jsonToColor(customThemeObject.getAsJsonObject("background")));
        }
        if (customThemeObject.has("additional")) {
            themeManager.setCustomAdditionalColor(this.jsonToColor(customThemeObject.getAsJsonObject("additional")));
        }
        if (customThemeObject.has("outline")) {
            themeManager.setCustomOutlineColor(this.jsonToColor(customThemeObject.getAsJsonObject("outline")));
        }
        if (customThemeObject.has("flat")) {
            themeManager.setCustomFlatColor(this.jsonToColor(customThemeObject.getAsJsonObject("flat")));
        }
        if (customThemeObject.has("sliderTrack")) {
            themeManager.setCustomSliderTrackColor(this.jsonToColor(customThemeObject.getAsJsonObject("sliderTrack")));
        }
        if (customThemeObject.has("sliderCircle")) {
            themeManager.setCustomSliderCircleColor(this.jsonToColor(customThemeObject.getAsJsonObject("sliderCircle")));
        }
        if (customThemeObject.has("sliderWindow")) {
            themeManager.setCustomSliderWindowColor(this.jsonToColor(customThemeObject.getAsJsonObject("sliderWindow")));
        }
        if (customThemeObject.has("tooltipText")) {
            themeManager.setCustomTooltipTextColor(this.jsonToColor(customThemeObject.getAsJsonObject("tooltipText")));
        }
    }

    private JsonObject colorToJson(ColorRGBA color) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("r", (Number)Float.valueOf(color.getRed()));
        jsonObject.addProperty("g", (Number)Float.valueOf(color.getGreen()));
        jsonObject.addProperty("b", (Number)Float.valueOf(color.getBlue()));
        jsonObject.addProperty("a", (Number)Float.valueOf(color.getAlpha()));
        return jsonObject;
    }

    private ColorRGBA jsonToColor(JsonObject jsonObject) {
        float red = jsonObject.get("r").getAsFloat();
        float green = jsonObject.get("g").getAsFloat();
        float blue = jsonObject.get("b").getAsFloat();
        float alpha = jsonObject.get("a").getAsFloat();
        return new ColorRGBA(red, green, blue, alpha);
    }

    private JsonArray getColorPickerPresetsJsonArray() {
        JsonArray presetsArray = new JsonArray();
        List<ColorPicker.Preset> presets = ColorPicker.COLOR_PRESETS;
        for (ColorPicker.Preset preset : presets) {
            if (!preset.isShowing()) continue;
            JsonObject presetObject = new JsonObject();
            ColorRGBA color = preset.getColor();
            presetObject.addProperty("red", (Number)Float.valueOf(color.getRed()));
            presetObject.addProperty("green", (Number)Float.valueOf(color.getGreen()));
            presetObject.addProperty("blue", (Number)Float.valueOf(color.getBlue()));
            presetObject.addProperty("alpha", (Number)Float.valueOf(color.getAlpha()));
            presetsArray.add((JsonElement)presetObject);
        }
        return presetsArray;
    }

    private void loadColorPickerPresets(JsonArray presetsArray) {
        ArrayList<ColorPicker.Preset> loadedPresets = new ArrayList<ColorPicker.Preset>();
        for (JsonElement presetElement : presetsArray) {
            JsonObject presetObject = presetElement.getAsJsonObject();
            float red = presetObject.get("red").getAsFloat();
            float green = presetObject.get("green").getAsFloat();
            float blue = presetObject.get("blue").getAsFloat();
            float alpha = presetObject.get("alpha").getAsFloat();
            ColorRGBA color = new ColorRGBA(red, green, blue, alpha);
            loadedPresets.add(new ColorPicker.Preset(color));
        }
        ColorPicker.setColorPresets(loadedPresets);
    }

    private void loadPass(JsonArray password) {
        for (JsonElement passElement : password) {
            JsonObject passObject = passElement.getAsJsonObject();
            String nick = passObject.get("nick").getAsString();
            String pass = passObject.get("pass").getAsString();
            Rockstar.getInstance().getModuleManager().getModule(AutoAuth.class).put(nick, pass);
        }
    }
}

