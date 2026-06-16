package im.zov4ik.protection;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class CryptoVault {

    private static final byte[] AAD = "zov4ik/aes-gcm/v1".getBytes(StandardCharsets.UTF_8);
    private static final byte[] MASTER_SEED = "zov4ik-cloud-ft-2026::gcm-key".getBytes(StandardCharsets.UTF_8);

    private CryptoVault() {
    }

    public static String decrypt(String payloadBase64, byte[] contextKeyMaterial) {
        try {
            byte[] payload = Base64.getDecoder().decode(payloadBase64);
            if (payload.length < 12 + 16) {
                throw new IllegalArgumentException("Encrypted payload is too short");
            }

            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[12];
            buffer.get(iv);
            byte[] cipherTextAndTag = new byte[buffer.remaining()];
            buffer.get(cipherTextAndTag);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(deriveKey(contextKeyMaterial), "AES"), new GCMParameterSpec(128, iv));
            cipher.updateAAD(AAD);
            byte[] plain = cipher.doFinal(cipherTextAndTag);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt protected payload", e);
        }
    }

    private static byte[] deriveKey(byte[] contextKeyMaterial) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(MASTER_SEED);
        if (contextKeyMaterial != null && contextKeyMaterial.length > 0) {
            digest.update(contextKeyMaterial);
        }
        return digest.digest();
    }
}

