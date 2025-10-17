package toolkit.enc.encrypts;

public class AESEncryptAlogritm implements EncryptAlogritm {
    @Override
    public String encryptToBase64(String plainText, byte[] key, byte[] iv) {
        return AesUtil.encrypt(plainText, key);
    }
}
