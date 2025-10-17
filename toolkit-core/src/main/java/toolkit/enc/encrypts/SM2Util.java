package toolkit.enc.encrypts;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

public class SM2Util {
    // 静态代码块，添加BouncyCastleProvider支持
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // 获取SM2曲线参数
    private static X9ECParameters getSM2Parameters() {
        return GMNamedCurves.getByName("sm2p256v1");
    }

    /**
     * 生成SM2密钥对
     * @return 包含公钥和私钥的密钥对
     */
    public static AsymmetricCipherKeyPair generateKeyPair() {
        X9ECParameters sm2ECParameters = getSM2Parameters();
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(),
                sm2ECParameters.getG(),
                sm2ECParameters.getN());

        ECKeyGenerationParameters keyGenerationParameters =
                new ECKeyGenerationParameters(domainParameters, new SecureRandom());

        ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
        keyPairGenerator.init(keyGenerationParameters);

        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 公钥转换为字符串
     * @param publicKey 公钥
     * @return 公钥字符串
     */
    public static String publicKeyToString(ECPublicKeyParameters publicKey) {
        ECPoint q = publicKey.getQ();
        return Hex.toHexString(q.getEncoded(false));
    }

    /**
     * 私钥转换为字符串
     * @param privateKey 私钥
     * @return 私钥字符串
     */
    public static String privateKeyToString(ECPrivateKeyParameters privateKey) {
        return Hex.toHexString(privateKey.getD().toByteArray());
    }

    /**
     * 字符串转换为公钥
     * @param publicKeyHex 公钥字符串
     * @return 公钥对象
     */
    public static ECPublicKeyParameters stringToPublicKey(String publicKeyHex) {
        X9ECParameters sm2ECParameters = getSM2Parameters();
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(),
                sm2ECParameters.getG(),
                sm2ECParameters.getN());

        byte[] publicKeyBytes = Hex.decode(publicKeyHex);
        ECPoint ecPoint = sm2ECParameters.getCurve().decodePoint(publicKeyBytes);

        return new ECPublicKeyParameters(ecPoint, domainParameters);
    }

    /**
     * 字符串转换为私钥
     * @param privateKeyHex 私钥字符串
     * @return 私钥对象
     */
    public static ECPrivateKeyParameters stringToPrivateKey(String privateKeyHex) {
        X9ECParameters sm2ECParameters = getSM2Parameters();
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(),
                sm2ECParameters.getG(),
                sm2ECParameters.getN());

        byte[] privateKeyBytes = Hex.decode(privateKeyHex);
        BigInteger d = new BigInteger(1, privateKeyBytes);

        return new ECPrivateKeyParameters(d, domainParameters);
    }

    /**
     * SM2加密
     * @param publicKey 公钥
     * @param data 待加密数据
     * @return 加密后的数据（Base64编码）
     */
    public static String encrypt(ECPublicKeyParameters publicKey, byte[] data) {
        try {
            SM2Engine engine = new SM2Engine();
            engine.init(true, new ParametersWithRandom(publicKey, new SecureRandom()));

            byte[] encrypted = engine.processBlock(data, 0, data.length);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException("SM2加密失败", e);
        }
    }

    /**
     * 从SM2私钥计算出公钥。
     *
     * @param privateKeyBigInt 私钥，以 BigInteger 形式表示。
     * @return 公钥，以 ECPoint 对象形式表示。
     */
    public static ECPoint getPublicKeyFromPrivateKey(BigInteger privateKeyBigInt) {
        if (privateKeyBigInt == null || privateKeyBigInt.signum() != 1) {
            throw new IllegalArgumentException("Private key must be a positive BigInteger.");
        }

        // 这就是核心公式 Q = d * G 的实现
        // SM2_PARAMS.getG() 就是基点 G
        // .multiply(privateKeyBigInt) 就是执行标量乘法
        X9ECParameters sm2ECParameters = getSM2Parameters();
        ECPoint publicKeyPoint = sm2ECParameters.getG().multiply(privateKeyBigInt);

        return publicKeyPoint;
    }

    public static String encrypt(ECPrivateKeyParameters privateKey, byte[] data) {
        try {
            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            engine.init(true, privateKey);

            byte[] encrypted = engine.processBlock(data, 0, data.length);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException("SM2加密失败", e);
        }
    }

    /**
     * SM2解密
     * @param privateKey 私钥
     * @param encryptedData 加密后的数据（Base64编码）
     * @return 解密后的数据
     */
    public static byte[] decrypt(ECPrivateKeyParameters privateKey, String encryptedData) {
        try {
            SM2Engine engine = new SM2Engine();
            engine.init(false, privateKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            return engine.processBlock(encryptedBytes, 0, encryptedBytes.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException("SM2解密失败", e);
        }
    }

    /**
     * 测试SM2加密解密
     */
    public static void main(String[] args) {
        try {
            // 生成密钥对
            AsymmetricCipherKeyPair keyPair = generateKeyPair();
            ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
            ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair.getPublic();

            // 打印密钥
            System.out.println("私钥: " + privateKeyToString(privateKey));
            System.out.println("公钥: " + publicKeyToString(publicKey));
            //4ea6a49abf31a235e5de98e31390e802500761a778d7a1d4d5f29c7cfc2f77b0
            //04a04eb78faaa1deaa1cdf7d973bac9061171643f19f9423830e3eb69262f5395cd08dd78dee62bb25b1434017043357849961d36a8eeac75ce8358770776a0693
            // 待加密的数据
            String originalData = "这是一个SM2加密算法的测试案例";
            System.out.println("原始数据: " + originalData);

            // 加密
            String encryptedData = encrypt(publicKey, originalData.getBytes("UTF-8"));
            System.out.println("加密后: " + encryptedData);

            // 解密
            byte[] decryptedData = decrypt(privateKey, encryptedData);
            System.out.println("解密后: " + new String(decryptedData, "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
