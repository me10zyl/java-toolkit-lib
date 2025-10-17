package toolkit.enc.encrypts;

public class SM4ECBEncryptAlogritm implements EncryptAlogritm {
    @Override
    public String encryptToBase64(String plainText, byte[] key, byte[] iv) {
        return SM4Util.encryptECB(plainText, key);
    }

    @Override
    public String decryptFromBase64(String cipherTextBase64, byte[] key, byte[] iv) {
        return SM4Util.decryptECB(cipherTextBase64, key);
    }

}
