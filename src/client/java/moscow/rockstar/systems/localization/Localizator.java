/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  lombok.Generated
 */
package moscow.rockstar.systems.localization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.localization.Language;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public final class Localizator {
    private static final Language DEFAULT_LANG;
    private static Language currentLanguage;
    private static final Map<String, String> translations;

    public static void loadTranslations() {
        String langFile = "/assets/" + Rockstar.MOD_ID + "/lang/" + currentLanguage.getCode() + ".lang";
        try {
            String line;
            InputStream inputStream = Localizator.class.getResourceAsStream(langFile);
            if (inputStream == null) {
                throw new RuntimeException("Language file not found: " + langFile);
            }
            translations.clear();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                ++lineNumber;
                if ((line = Localizator.removeComments(line).trim()).isEmpty()) continue;
                Localizator.parseLine(line, lineNumber, langFile);
            }
            reader.close();
            inputStream.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load translations for language: " + currentLanguage.getCode(), e);
        }
    }

    public static void setLanguage(@Nonnull Language lang) {
        currentLanguage = lang;
        Localizator.loadTranslations();
    }

    public static String translate(String key) {
        return translations.getOrDefault(key, key);
    }

    public static String translate(String key, Object ... args) {
        String format = translations.getOrDefault(key, key);
        return String.format(format, args);
    }

    public static String translateOrEmpty(String key) {
        return translations.getOrDefault(key, " ");
    }

    @VMProtect(type=VMProtectType.MUTATION)
    private static void parseLine(String line, int lineNumber, String fileName) {
        int equalIndex = line.indexOf(61);
        if (equalIndex == -1) {
            Rockstar.LOGGER.warn("Warning: Invalid line format at line {} in {}: {}", new Object[]{lineNumber, fileName, line});
            return;
        }
        String key = line.substring(0, equalIndex).trim();
        String value = line.substring(equalIndex + 1).trim();
        if (key.isEmpty()) {
            Rockstar.LOGGER.warn("Warning: Empty key at line {} in {}", (Object)lineNumber, (Object)fileName);
            return;
        }
        translations.put(key, value);
    }

    private static String removeComments(String line) {
        int commentIndex = line.indexOf("#");
        if (commentIndex != -1) {
            return line.substring(0, commentIndex);
        }
        return line;
    }

    @Generated
    private Localizator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static Language getCurrentLanguage() {
        return currentLanguage;
    }

    static {
        currentLanguage = DEFAULT_LANG = Language.RU_RU;
        translations = new HashMap<String, String>();
    }
}

