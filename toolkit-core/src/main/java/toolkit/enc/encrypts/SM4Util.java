package toolkit.enc.encrypts;

import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

public class SM4Util {

    // 算法名称/模式/填充方式
    public static final String ALGORITHM = "SM4";
    private static final String MODE_ECB = "SM4/ECB/PKCS7Padding";
    public static final String MODE_PADDING = "SM4/CBC/PKCS5Padding";
    
    // SM4密钥长度固定为 16 字节（128位）
    // 密钥和IV应为随机生成的安全数据，这里仅为示例
    private static final byte[] KEY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] IV  = "FEDCBA9876543210".getBytes(StandardCharsets.US_ASCII); // CBC模式需要初始化向量 IV

    static {
        // 注册 Bouncy Castle 作为 JCE 提供者
        // 建议只注册一次
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * SM4 加密方法
     * @param plainText 待加密的明文文本
     * @return Base64 编码后的密文
     */
    @SneakyThrows
    public static String encrypt(String plainText, byte[] key, byte[] iv)  {
        // 1. 创建密钥对象
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        
        // 2. 创建初始化向量对象
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 3. 获取 Cipher 实例，并指定模式和提供者
        Cipher cipher = Cipher.getInstance(MODE_PADDING, BouncyCastleProvider.PROVIDER_NAME);
        
        // 4. 初始化为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        // 5. 加密数据
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 6. 返回 Base64 编码后的密文
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @SneakyThrows
    public static String encryptECB(String plainText, byte[] key)  {
        // 1. 创建密钥对象
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);



        // 3. 获取 Cipher 实例，并指定模式和提供者
        Cipher cipher = Cipher.getInstance(MODE_ECB, BouncyCastleProvider.PROVIDER_NAME);

        // 4. 初始化为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        // 5. 加密数据
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 6. 返回 Base64 编码后的密文
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @SneakyThrows
    public static String decryptECB(String cipherText, byte[] key) {
        // 1. 创建密钥对象
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);

        // 3. 获取 Cipher 实例，并指定模式和提供者
        Cipher cipher = Cipher.getInstance(MODE_ECB, BouncyCastleProvider.PROVIDER_NAME);

        // 4. 初始化为解密模式
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        // 5. 解码 Base64 密文
        byte[] encryptedBytes = Base64.getDecoder().decode(cipherText);

        // 6. 解密数据
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // 7. 返回 UTF-8 编码的明文
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * SM4 解密方法
     * @param cipherText Base64 编码的密文
     * @return 解密后的明文文本
     */
    @SneakyThrows
    public static String decrypt(String cipherText, byte[] key, byte[] iv)  {
        // 1. 创建密钥对象
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        
        // 2. 创建初始化向量对象
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 3. 获取 Cipher 实例，并指定模式和提供者
        Cipher cipher = Cipher.getInstance(MODE_PADDING, BouncyCastleProvider.PROVIDER_NAME);
        
        // 4. 初始化为解密模式
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        // 5. 解码 Base64 密文
        byte[] encryptedBytes = Base64.getDecoder().decode(cipherText);

        // 6. 解密数据
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // 7. 返回 UTF-8 编码的明文
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        try {
            String originalText = "Hello, SM4 加密测试！This is a secret message.";
            System.out.println("原始明文: " + originalText);

            // 加密
            String encrypted = encrypt(originalText, KEY, IV);
            String encryptedECB = encryptECB(originalText, KEY);
            System.out.println("SM4 密文: " + encrypted);
            System.out.println("SM4 ECB 密文: " + encryptedECB);

            // 解密
            String decrypted = decrypt(encrypted, KEY, IV);
            System.out.println("解密结果: " + decrypted);
            String decryptedECB = decryptECB(encryptedECB, KEY);
            System.out.println("SM4 ECB 解密结果: " + decryptedECB);

            // 验证解密结果
            if (originalText.equals(decrypted)) {
                System.out.println("加密和解密成功！");
            } else {
                System.out.println("加密和解密失败！");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}