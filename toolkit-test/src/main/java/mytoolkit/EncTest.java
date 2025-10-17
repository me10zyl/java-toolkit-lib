package mytoolkit;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.asymmetric.SM2;
import org.bouncycastle.util.encoders.Hex;
import toolkit.enc.dto.EncryptAlogritmEnum;
import toolkit.enc.dto.PrivateKey;
import toolkit.enc.dto.PublicKey;
import toolkit.enc.encrypts.EncryptAlogritm;
import toolkit.enc.encrypts.EncFactory;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class EncTest {
    public static void main(String[] args) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] ranBytes = new byte[16];
        secureRandom.nextBytes(ranBytes);
        System.out.println(Hex.toHexString(ranBytes));
    }
//    public static void main(String[] args) {
//
//        EncryptAlogritm sm2 = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.SM2);
//        String privateKeyHex = "009bee2085e9ecbdc43b83982f55e0b2a80644f9f143afde542394c3a5653ad886";
//        PublicKey publicKey = sm2.getPublicKey(new PrivateKey(privateKeyHex));
//        System.out.println(publicKey);
//        String base64 = "K+VOT89nuGqjAKVvXmjfx4k9qLY2ovB8DQXvj+ld9/G64E10WsXHu/BbZ+TGHT3l4yi04iM+V9gceN/HKeQYNZmFcZRuOQNJpGDfL941hT4S2fGQSHWuajUHHIR39aVJLvDP/R5CTtWSMXDlYoUizWaMV0Jgi/g/ycNL";
//        byte[] bytes = sm2.decryptFromBase64(new PrivateKey(privateKeyHex),
//                base64);
//        System.out.println(new String(bytes));
//        byte[] decrypt = new SM2(privateKeyHex, "0498e149fcee64727322b2dc273e57577c1794afe4be40b90d055936840113381593563f01189d55b3616745924d63f05ee3010cba1354149b386589024150bb20").decrypt(Base64.decode(base64));
//        System.out.println(new String(decrypt));
//    }
}
