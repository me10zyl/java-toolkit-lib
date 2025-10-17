package toolkit.enc.encrypts;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;


public class AESEncryptAlogritm implements EncryptAlogritm {
    @Override
    public String encryptToBase64(String plainText, byte[] key, byte[] iv) {
        return AesUtil.encrypt(plainText, key);
    }

    @Override
    public String decryptFromBase64(String cipherTextBase64, byte[] key, byte[] iv) {
        return AesUtil.decrypt(cipherTextBase64, key);
    }

    public static void main(String[] args) {
        String s2 = "+b5j1vad1TR2U5LjagNKpmQPw5SHMj0ZQL+6x+H4aJETFXC2qQlpv/+Ecd2TnQvhGtRA+BY+ox4Zj6SYT2K+S/6UmPSBN5RwoQuh6g2iPhTqKp2IKLKVe9bnlJjG9qAj2tVFNR8dB/OODG4YVO+Icw==";
//        System.out.println(Base64.decode(s2));
//        byte[] bytes = new byte[32];
//        new SecureRandom().nextBytes(bytes);
//        System.out.println(Hex.toHexString(bytes));
        AESEncryptAlogritm aesEncryptAlogritm = new AESEncryptAlogritm();
//        String s = aesEncryptAlogritm.encryptToBase64("aaa", bytes, null);
        String s1 = aesEncryptAlogritm.decryptFromBase64(s2, "2CEqJiBId9x5bILj".getBytes(StandardCharsets.UTF_8), null);
        System.out.println(s1);
    }
}
