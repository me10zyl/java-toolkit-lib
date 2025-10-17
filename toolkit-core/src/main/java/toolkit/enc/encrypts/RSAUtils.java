package toolkit.enc.encrypts;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {
    // 算法名称
    private static final String ALGORITHM = "RSA";
    // 密钥长度（推荐 2048 或 4096，长度越长安全性越高，但性能越低）
    private static final int KEY_SIZE = 2048;
    // 加密模式：RSA/ECB/PKCS1Padding（JDK 默认支持）
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    /**
     * 生成 RSA 密钥对（公钥 + 私钥）
     * @return 包含公钥和私钥的 KeyPair 对象
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE); // 初始化密钥长度
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 公钥加密
     * @param publicKey 公钥（Base64 编码字符串）
     * @param data 待加密的明文
     * @return 加密后的密文（Base64 编码）
     */
    public static byte[] encryptByPublicKey(byte[] publicKeyBytes, byte[] data) throws Exception {
        // 2. 生成公钥对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PublicKey publicKeyObj = keyFactory.generatePublic(keySpec);

        // 3. 初始化加密器
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKeyObj);

        // 4. 加密（RSA 加密长度有限制，此处简化处理短文本，长文本需分段加密）
        byte[] encryptedBytes = cipher.doFinal(data);
        // 5. 密文 Base64 编码
        return encryptedBytes;
    }

    /**
     * 私钥解密
     * @param privateKey 私钥（Base64 编码字符串）
     * @param encryptedData 加密后的密文（Base64 编码）
     * @return 解密后的明文
     */
    public static byte[] decryptByPrivateKey(byte[] privateKeyBytes, String encryptedData) throws Exception {
        // 1. Base64 解码私钥
        // 2. 生成私钥对象
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privateKeyObj = keyFactory.generatePrivate(keySpec);

        // 3. 初始化解密器
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKeyObj);

        // 4. 解密
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        // 5. 转换为明文
        return decryptedBytes;
    }

    /**
     * 私钥加密（通常用于签名，非推荐加密方式，因私钥需保密）
     * @param privateKeyBytes 私钥（Base64 编码字符串）
     * @param data 待加密的明文
     * @return 加密后的密文（Base64 编码）
     */
    public static byte[] encryptByPrivateKey(byte[] privateKeyBytes, byte[] data) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privateKeyObj = keyFactory.generatePrivate(keySpec);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, privateKeyObj);

        byte[] encryptedBytes = cipher.doFinal(data);
        return encryptedBytes;
    }

    /**
     * 公钥解密（对应私钥加密的场景）
     * @param publicKey 公钥（Base64 编码字符串）
     * @param encryptedData 加密后的密文（Base64 编码）
     * @return 解密后的明文
     */
    public static String decryptByPublicKey(String publicKey, String encryptedData) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PublicKey publicKeyObj = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, publicKeyObj);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // 辅助方法：获取 Base64 编码的公钥
    public static String getPublicKeyString(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    // 辅助方法：获取 Base64 编码的私钥
    public static String getPrivateKeyString(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }
}