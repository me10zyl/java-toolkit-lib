package toolkit.enc.encrypts;

public class SM4CBCEncryptAlogritm implements EncryptAlogritm {
    @Override
    public String encryptToBase64(String plainText, byte[] key, byte[] iv) {
        if(plainText == null){
            return null;
        }
        return SM4Util.encrypt(plainText, key, iv);
    }

    @Override
    public String decryptFromBase64(String cipherTextBase64, byte[] key, byte[] iv) {
        return SM4Util.decrypt(cipherTextBase64, key, iv);
    }
}
