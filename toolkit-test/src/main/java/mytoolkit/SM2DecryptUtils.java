package mytoolkit;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.apache.tomcat.util.buf.HexUtils;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.security.Security;
import java.math.BigInteger;

public class SM2DecryptUtils {
    static {
        Security.addProvider(new BouncyCastleProvider()); // 注册BC Provider
    }

    /**
     * SM2私钥解密
     * @param privateKeyHex 私钥（Hex编码，与加密用的公钥配对）
     * @param encryptedHex 加密数据（字节数组，前端加密后传递）
     * @return 解密后的明文
     */
    public static String decrypt(String privateKeyHex, String encryptedHex) throws Exception {
        // 1. 加载SM2曲线参数
        X9ECParameters sm2Params = GMNamedCurves.getByName("sm2p256v1");
        ECDomainParameters domainParams = new ECDomainParameters(
                sm2Params.getCurve(),
                sm2Params.getG(),
                sm2Params.getN()
        );

        // 2. 解析私钥
        BigInteger privateKeyD = new BigInteger(privateKeyHex, 16);
        ECPrivateKeyParameters privateKeyParams = new ECPrivateKeyParameters(privateKeyD, domainParams);

        // 3. 初始化SM2引擎（默认C1C3C2模式）
        SM2Engine sm2Engine = new SM2Engine();
        // 解密模式：false，直接传入私钥
        sm2Engine.init(false, privateKeyParams);

        // 4. 执行解密
        byte[] encryptedBytes = Hex.decode(encryptedHex);
        byte[] decryptedBytes = sm2Engine.processBlock(encryptedBytes, 0, encryptedBytes.length);
        return new String(decryptedBytes, "UTF-8");
    }

    // 测试方法
    public static void main(String[] args) throws Exception {
//        // 私钥（Hex编码，需与加密用的公钥配对）
//        String privateKeyHex = "009bee2085e9ecbdc43b83982f55e0b2a80644f9f143afde542394c3a5653ad886";
//        // 前端加密后传递的密文（Base64解码为字节数组，或Hex解码）
//        byte[] encryptedData = Base64.decode("XOMT6T/pNT38x8fJOsaSUN9MyxEO9yCzzTzSdXubHiEu7xtsuvtxC9K0PQ4DG6opNjtgShQ1sZ6xlqUDCHnU/XqEbvqocpCFAXHvWKmvaHLldDNQbxN5sXNu7drQXD+XucCL36/pi2tP5X60nwnTmr"); // 若前端是Base64，则用Base64.getDecoder().decode(...)
//
//        // 解密
//        String plainText = decrypt(privateKeyHex, HexUtils.toHexString(encryptedData));
//        System.out.println("解密结果：" + plainText);

        byte[] decode = Hex.decode("30820122300d06092a864886f70d01010105000382010f003082010a0282010100a17b5c10f57e4815c2577a4ac9417c770c462d9466d109b4a20f85ddd814c359990d1b869e2c392ee9e9ad434af627aedc5eb1dd48d98ecc97c4df1c826119e0d0a6701ce2763b1a6977cd344f6edb261c28912aaf5def5ddcfd1c0fb775c38880e6aa7d28913b47463bbb02f1982aeca7ccc7e959f0e1a6d7e2adf518440ee5bd6c5b56360d842858b70775690afb3380e751513a03f06620bd8862d3c93ec5d7266755822edd51e89c7326596b15c3841bac927013825d4d4826f93719feadf26507f2c7e2ea97b13b5acc4e0d7f7f059a2dc5a585b83c3714540a37858164c45625151681bbaabf6f9c281b83d995028bfdc1ccda2c08c92d156f6cd7cf5b0203010001");
        System.out.println(Base64.encode(decode));
    }
}