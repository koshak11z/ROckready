package im.zov4ik.utils.display.font;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import im.zov4ik.zov4ik;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Fonts {

    @SneakyThrows
    public static FontRenderer create(float size, String name) {
        String path = "assets/minecraft/fonts/" + name + ".ttf";

        try (InputStream inputStream = zov4ik.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(size / 2f);
                return new FontRenderer(font, size / 2f);
            }
        }

        Font fallbackFont = new Font("SansSerif", Font.PLAIN, Math.max(1, Math.round(size / 2f)));
        System.err.println("Missing font resource: " + path + ", using fallback font.");
        return new FontRenderer(fallbackFont, size / 2f);
    }

    private static final Map<FontKey, FontRenderer> fontCache = new HashMap<>();

    public static void init() {
        for (Type type : Type.values()) {
            for (int size = 4; size <= 32; size++) {
                fontCache.put(new FontKey(size, type), create(size, type.getType()));
            }
        }
    }



    public static FontRenderer getSize(int size) {
        return getSize(size, Type.INST);
    }

    public static FontRenderer getSize(int size, Type type) {
        return fontCache.computeIfAbsent(new FontKey(size, type), k -> create(size, type.getType()));
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        DEFAULT("sf_medium"),
        REGULAR("sf_regular"),
        SEMI("sf_semibold"),
        BOLD("sf_bold"),
        BOLDED("bold"),
        MANROPEEXTRABOLD("manropeextrabold"),
        MANROPEBOLD("manropebold"),
        zov4ikREGULAR("rich_regular"),
        ICONzov4ikREG("iconrichreg"),
        INST("suisseintl"),
        ICONS("icons"),
        ICONSTYPENEW("icon2"),
        GUIICONS("guiicons"),
        ICONSCATEGORY("categoryicons"),;

        private final String type;
    }

    private record FontKey(int size, Type type) {
    }
}
