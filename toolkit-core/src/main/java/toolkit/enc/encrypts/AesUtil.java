package toolkit.enc.encrypts;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AesUtil {
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY_STRING = "WMXzZXwb1Tmm6aW0VcSxzTtvaRVj3cRz"; // 16, 24 或 32 字节

    private static SecretKey getSecretKey(byte[] key) {
        return new SecretKeySpec(key, ALGORITHM);
    }

    public static String encrypt(String value, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key));
            byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedValue, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE,  new SecretKeySpec(key, ALGORITHM));
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
