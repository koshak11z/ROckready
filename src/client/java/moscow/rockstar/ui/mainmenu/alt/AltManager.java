package moscow.rockstar.ui.mainmenu.alt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import moscow.rockstar.mixin.minecraft.client.IMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

/**
 * Offline (cracked) alt account storage + login. Persisted by {@code ClientDataFile} under "alts".
 * Logging in swaps the client {@link Session} to an offline one with the chosen nick — enough for
 * the cracked/offline servers this client targets. Premium accounts are out of scope by design.
 */
public final class AltManager {
    private static final List<String> ALTS = new ArrayList<>();

    private AltManager() {
    }

    public static List<String> getAlts() {
        return ALTS;
    }

    public static boolean isValid(String name) {
        return name != null && name.trim().matches("[A-Za-z0-9_]{3,16}");
    }

    public static void add(String name) {
        if (!isValid(name)) {
            return;
        }
        String trimmed = name.trim();
        if (ALTS.stream().noneMatch(a -> a.equalsIgnoreCase(trimmed))) {
            ALTS.add(trimmed);
        }
    }

    public static void remove(String name) {
        ALTS.removeIf(a -> a.equalsIgnoreCase(name));
    }

    public static void clear() {
        ALTS.clear();
    }

    /** Swap the live session to an offline account with the given nick. */
    public static void login(String name) {
        if (!isValid(name)) {
            return;
        }
        String trimmed = name.trim();
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + trimmed).getBytes(StandardCharsets.UTF_8));
        Session session = new Session(trimmed, uuid, "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
        ((IMinecraftClient) MinecraftClient.getInstance()).setSession(session);
    }

    public static String currentName() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }
}
