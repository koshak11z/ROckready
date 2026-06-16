package im.zov4ik.protection;

import im.zov4ik.utils.client.logs.Logger;
import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

public final class ProtectionBootstrap {

    private static final String ENC_CLOUD_WS = "+yDsPUhgHkWjaTrwa7uMJBZtJSGHR2Hy7aSP/3VxnCt/EzrwjqdySx+K6fz+QFgzG/Z8wQ==";
    private static final String ENC_FT_WS = "c8pYwmYnyenAN1sIYz0hmbelDLN/OJdcgCt+3h8KBHWPjElcEDWMxkLzcmV0egz0cEFNcA==";
    private static final String HASH_FABRIC_MOD = "970387659C48BE4406EB0C4745F79BD119B7506DB5155ECEB87316896B69CC90";
    private static final String HASH_MIXINS = "795C1F6D8E0E2EFFC4FE863D130B0F7563C8977BCE2D956E5E591052E862931E";

    private static volatile boolean initialized;
    private static String cloudEndpoint;
    private static String ftEndpoint;
    private static NativeGate gate;

    private ProtectionBootstrap() {
    }

    public static synchronized void bootstrapOrThrow() {
        if (initialized) {
            return;
        }

        boolean strictMode = resolveStrictMode();
        gate = NativeGate.initialize();

        if (!gate.isLoaded()) {
            Logger.warn("Protection native layer is not loaded: " + gate.detail());
            if (strictMode) {
                throw new IllegalStateException("Strict protection mode requires a native library");
            }
        } else if (!gate.verifyChallenge()) {
            if (strictMode) {
                throw new IllegalStateException("Native challenge verification failed");
            }
            Logger.warn("Native challenge verification failed, fallback mode is used");
        }

        if (strictMode && hasDebugAgentAttached()) {
            throw new IllegalStateException("Debug agent detected in strict protection mode");
        }

        byte[] nativeContext = gate.contextKeyMaterial();
        if (strictMode && nativeContext.length < 16) {
            throw new IllegalStateException("Native context key material is invalid");
        }

        if (!verifyResourceHash("/fabric.mod.json", HASH_FABRIC_MOD) || !verifyResourceHash("/mixins.json", HASH_MIXINS)) {
            if (strictMode) {
                throw new IllegalStateException("Critical resources were modified");
            }
            Logger.warn("Critical resource hash mismatch, continuing in non-strict mode");
        }

        try {
            cloudEndpoint = CryptoVault.decrypt(ENC_CLOUD_WS, null);
            ftEndpoint = CryptoVault.decrypt(ENC_FT_WS, null);
        } catch (Exception e) {
            if (strictMode) {
                throw e;
            }
            Logger.warn("Protection decrypt failed, using fallback endpoints: " + e.getMessage());
            cloudEndpoint = fallbackCloudEndpoint();
            ftEndpoint = fallbackFtEndpoint();
        }

        initialized = true;
        Logger.info("Protection bootstrap initialized. Native=" + gate.isLoaded());
    }

    private static boolean resolveStrictMode() {
        String property = System.getProperty("zov4ik.protection.strict");
        if (property != null && !property.isBlank()) {
            return Boolean.parseBoolean(property);
        }
        return !FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static String cloudEndpoint() {
        ensureInitialized();
        return cloudEndpoint;
    }

    public static String ftEndpoint() {
        ensureInitialized();
        return ftEndpoint;
    }

    public static boolean nativeLoaded() {
        return gate != null && gate.isLoaded();
    }

    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Protection bootstrap was not initialized");
        }
    }

    private static boolean hasDebugAgentAttached() {
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String arg : args) {
            String lowered = arg.toLowerCase();
            if (lowered.contains("-javaagent") || lowered.contains("-agentlib:jdwp")) {
                return true;
            }
        }
        return false;
    }

    private static boolean verifyResourceHash(String resourcePath, String expectedHex) {
        try (InputStream input = ProtectionBootstrap.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return false;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            String actual = HexFormat.of().withUpperCase().formatHex(digest.digest());
            return actual.equals(expectedHex);
        } catch (Exception e) {
            return false;
        }
    }

    private static String fallbackCloudEndpoint() {
        return decode(new int[]{119, 115, 58, 47, 47, 52, 53, 46, 49, 53, 53, 46, 50, 48, 53, 46, 50, 48, 50, 58, 56, 48, 56, 48});
    }

    private static String fallbackFtEndpoint() {
        return decode(new int[]{119, 115, 58, 47, 47, 52, 53, 46, 49, 53, 53, 46, 50, 48, 53, 46, 50, 48, 50, 58, 54, 51, 49, 50});
    }

    private static String decode(int[] chars) {
        byte[] data = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            data[i] = (byte) chars[i];
        }
        return new String(data, StandardCharsets.UTF_8);
    }
}
