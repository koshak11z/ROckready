package im.zov4ik.protection;

import im.zov4ik.utils.client.logs.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class NativeGate {

    private static final String LIB_BASENAME = "zovguard";
    private static final byte[] FALLBACK_CONTEXT = "zov4ik-fallback-context-v1".getBytes();

    private final boolean loaded;
    private final String detail;

    private NativeGate(boolean loaded, String detail) {
        this.loaded = loaded;
        this.detail = detail;
    }

    public static NativeGate initialize() {
        try {
            loadNativeLibrary();
            return new NativeGate(true, "native loaded");
        } catch (Throwable t) {
            return new NativeGate(false, t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String detail() {
        return detail;
    }

    public byte[] contextKeyMaterial() {
        if (loaded) {
            try {
                byte[] data = nativeKeyMaterial();
                if (data != null && data.length > 0) {
                    return data;
                }
            } catch (Throwable ignored) {
            }
        }
        return hash(FALLBACK_CONTEXT);
    }

    public boolean verifyChallenge() {
        if (!loaded) {
            return false;
        }
        try {
            long nonce = ThreadLocalRandom.current().nextLong();
            long expected = javaChallengeReference(nonce);
            long nativeResult = nativeSolveChallenge(nonce);
            return expected == nativeResult;
        } catch (Throwable t) {
            Logger.warn("Native challenge verification failed: " + t.getMessage());
            return false;
        }
    }

    private static void loadNativeLibrary() throws Exception {
        String explicitPath = System.getProperty("zov4ik.native.path", "").trim();
        if (!explicitPath.isEmpty()) {
            System.load(explicitPath);
            return;
        }

        try {
            System.loadLibrary(LIB_BASENAME);
            return;
        } catch (UnsatisfiedLinkError ignored) {
        }

        String resourcePath = resolveBundledLibraryPath();
        try (InputStream input = NativeGate.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new UnsatisfiedLinkError("Bundled native library not found at " + resourcePath);
            }

            String extension = fileExtension(resourcePath);
            Path tempFile = Files.createTempFile(LIB_BASENAME + "-", extension);
            tempFile.toFile().deleteOnExit();
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            System.load(tempFile.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new UnsatisfiedLinkError("Failed to extract bundled native library: " + e.getMessage());
        }
    }

    private static String resolveBundledLibraryPath() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        boolean x64 = arch.contains("64");

        if (os.contains("win")) {
            return x64 ? "/native/win64/zovguard.dll" : "/native/win32/zovguard.dll";
        }
        if (os.contains("linux")) {
            return x64 ? "/native/linux64/libzovguard.so" : "/native/linux32/libzovguard.so";
        }
        if (os.contains("mac")) {
            return "/native/macos/libzovguard.dylib";
        }
        throw new UnsatisfiedLinkError("Unsupported OS for bundled native library: " + os);
    }

    private static String fileExtension(String path) {
        int index = path.lastIndexOf('.');
        return index < 0 ? ".bin" : path.substring(index);
    }

    private static long javaChallengeReference(long nonce) {
        long x = nonce ^ 0x4F3AF98D7A61C22BL;
        x = Long.rotateLeft(x, 17);
        x += 0x71C3B5D0A42EF6D9L;
        x ^= (x >>> 29);
        return x;
    }

    private static byte[] hash(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (Exception e) {
            return data.clone();
        }
    }

    private static native long nativeSolveChallenge(long nonce);

    private static native byte[] nativeKeyMaterial();
}

