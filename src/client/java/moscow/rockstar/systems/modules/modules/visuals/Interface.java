/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.modules.modules.visuals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.localization.Language;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;

@ModuleInfo(name="Interface", category=ModuleCategory.VISUALS, enabledByDefault=true)
public class Interface
extends BaseModule {
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.interface.mode");
    private final ModeSetting.Value liquidGlass = new ModeSetting.Value(this.mode, "modules.settings.interface.liquidGlass");
    private final ModeSetting.Value minimalism = new ModeSetting.Value(this.mode, "modules.settings.interface.minimalism");
    private final ModeSetting.Value custom = new ModeSetting.Value(this.mode, "modules.settings.interface.custom");
    private final ModeSetting themeMode = new ModeSetting((SettingsContainer)this, "modules.settings.interface.theme", () -> this.liquidGlass.isSelected() || this.custom.isSelected());
    public final ModeSetting.Value dark = new ModeSetting.Value(this.themeMode, "modules.settings.interface.dark");
    public final ModeSetting.Value light = new ModeSetting.Value(this.themeMode, "modules.settings.interface.light");
    private final ModeSetting language = new ModeSetting(this, "modules.settings.interface.language");
    private final SliderSetting blur = new SliderSetting(this, "modules.settings.interface.blur").suffix("%").currentValue(100.0f).min(0.0f).max(100.0f).step(1.0f);
    private final BooleanSetting blurMenu = new BooleanSetting(this, "modules.settings.interface.blur_menu").enable();
    private final BooleanSetting blurSidebar = new BooleanSetting(this, "modules.settings.interface.blur_sidebar").enable();
    private final BooleanSetting blurPanels = new BooleanSetting(this, "modules.settings.interface.blur_panels").enable();
    private final BooleanSetting blurThemeEditor = new BooleanSetting(this, "modules.settings.interface.blur_theme_editor").enable();
    private final BooleanSetting blurSearch = new BooleanSetting(this, "modules.settings.interface.blur_search").enable();
    private final BooleanSetting blurHud = new BooleanSetting(this, "modules.settings.interface.blur_hud").enable();
    private final BooleanSetting blurPopups = new BooleanSetting(this, "modules.settings.interface.blur_popups").enable();
    private final BooleanSetting blurNotifications = new BooleanSetting(this, "modules.settings.interface.blur_notifications").enable();
    private final Animation liquidGlassAnim = new Animation(500L, Easing.BOTH_CUBIC);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean languageAutoDetected;
    private int lastLang = 0;
    private final EventListener<HudRenderEvent> onHudRenderEvent = event -> {
        this.liquidGlassAnim.setEasing(Easing.FIGMA_EASE_IN_OUT);
        if (this.liquidGlass.isSelected()) {
            this.liquidGlassAnim.update(1.0f);
        } else if (this.custom.isSelected()) {
            this.liquidGlassAnim.update(0.5f);
        } else {
            this.liquidGlassAnim.update(0.0f);
        }
        int lang = this.language.getValues().indexOf(this.language.getValue());
        if (lang != this.lastLang) {
            Localizator.setLanguage(lang == 0 ? Language.RU_RU : (lang == 1 ? Language.EN_US : (lang == 2 ? Language.UK_UA : Language.PL_PL)));
            this.languageAutoDetected = false;
        }
        this.lastLang = lang;
        Rockstar.getInstance().getThemeManager().setCurrentTheme(this.dark.isSelected() ? Theme.DARK : Theme.LIGHT);
    };

    public Interface() {
        new ModeSetting.Value(this.language, "\u0420\u0443\u0441\u0441\u043a\u0438\u0439");
        new ModeSetting.Value(this.language, "English");
        new ModeSetting.Value(this.language, "\u0423\u043a\u0440\u0430\u0457\u043d\u0441\u044c\u043a\u0430");
        new ModeSetting.Value(this.language, "polski");
    }

    private void detectLanguageByIP() {
        this.executor.submit(() -> {
            try {
                String countryCode = this.getCountryCodeByIP();
                if (countryCode != null) {
                    switch (countryCode.toUpperCase()) {
                        case "UA": {
                            this.language.setValue(this.language.getValues().get(2));
                            Localizator.setLanguage(Language.UK_UA);
                            break;
                        }
                        case "PL": {
                            this.language.setValue(this.language.getValues().get(3));
                            Localizator.setLanguage(Language.PL_PL);
                            break;
                        }
                        default: {
                            this.language.setValue(this.language.getValues().getFirst());
                            Localizator.setLanguage(Language.RU_RU);
                        }
                    }
                    this.languageAutoDetected = true;
                    this.lastLang = this.language.getValues().indexOf(this.language.getValue());
                }
            }
            catch (Exception e) {
                Rockstar.LOGGER.error("Failed to detect language by IP", (Throwable)e);
            }
        });
    }

    private String getCountryCodeByIP() throws IOException {
        URL url = new URL("http://ip-api.com/json/?fields=countryCode");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));){
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            String json = response.toString();
            int start = json.indexOf("countryCode\":\"") + 14;
            if (start >= 14) {
                int end = json.indexOf("\"", start);
                String string = json.substring(start, end);
                return string;
            }
        }
        return null;
    }

    public static boolean glassSelected() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).liquidGlass.isSelected();
    }

    public static boolean customSelected() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).custom.isSelected();
    }

    public static float glass() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).liquidGlassAnim.getValue();
    }

    public static float minimalizm() {
        return 1.0f - Interface.glass();
    }

    public static float blur() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).blur.getCurrentValue() / 100.0f;
    }

    public static boolean blurMenuEnabled() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).blurMenu.isEnabled();
    }

    public static boolean blurSidebarEnabled() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).blurSidebar.isEnabled();
    }

    public static boolean blurPanelsEnabled() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).blurPanels.isEnabled();
    }

    public static boolean blurThemeEditorEnabled() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).blurThemeEditor.isEnabled();
    }

    public static boolean blurSearchEnabled() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).blurSearch.isEnabled();
    }

    public static boolean blurHudEnabled() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).blurHud.isEnabled();
    }

    public static boolean blurPopupsEnabled() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).blurPopups.isEnabled();
    }

    public static boolean blurNotificationsEnabled() {
        return Rockstar.getInstance().getModuleManager().getModule(Interface.class).blurNotifications.isEnabled();
    }

    public static boolean showGlass() {
        return Interface.glass() > 0.0f;
    }

    public static boolean showMinimalizm() {
        return Interface.glass() < 1.0f;
    }

    @Generated
    public ModeSetting getMode() {
        return this.mode;
    }

    @Generated
    public ModeSetting.Value getLiquidGlass() {
        return this.liquidGlass;
    }

    @Generated
    public ModeSetting.Value getMinimalism() {
        return this.minimalism;
    }

    @Generated
    public ModeSetting getThemeMode() {
        return this.themeMode;
    }

    @Generated
    public ModeSetting.Value getDark() {
        return this.dark;
    }

    @Generated
    public ModeSetting.Value getLight() {
        return this.light;
    }

    @Generated
    public ModeSetting getLanguage() {
        return this.language;
    }

    @Generated
    public SliderSetting getBlur() {
        return this.blur;
    }

    @Generated
    public BooleanSetting getBlurMenu() {
        return this.blurMenu;
    }

    @Generated
    public BooleanSetting getBlurSidebar() {
        return this.blurSidebar;
    }

    @Generated
    public BooleanSetting getBlurPanels() {
        return this.blurPanels;
    }

    @Generated
    public BooleanSetting getBlurThemeEditor() {
        return this.blurThemeEditor;
    }

    @Generated
    public BooleanSetting getBlurSearch() {
        return this.blurSearch;
    }

    @Generated
    public BooleanSetting getBlurHud() {
        return this.blurHud;
    }

    @Generated
    public BooleanSetting getBlurPopups() {
        return this.blurPopups;
    }

    @Generated
    public BooleanSetting getBlurNotifications() {
        return this.blurNotifications;
    }

    @Generated
    public Animation getLiquidGlassAnim() {
        return this.liquidGlassAnim;
    }

    @Generated
    public ExecutorService getExecutor() {
        return this.executor;
    }

    @Generated
    public boolean isLanguageAutoDetected() {
        return this.languageAutoDetected;
    }

    @Generated
    public int getLastLang() {
        return this.lastLang;
    }

    @Generated
    public EventListener<HudRenderEvent> getOnHudRenderEvent() {
        return this.onHudRenderEvent;
    }
}

