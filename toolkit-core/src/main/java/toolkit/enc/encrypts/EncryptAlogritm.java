package toolkit.enc.encrypts;


import toolkit.enc.dto.KeyPair;
import toolkit.enc.dto.PrivateKey;
import toolkit.enc.dto.PublicKey;

public interface EncryptAlogritm {
    default String encryptToBase64(String plainText, byte[] key, byte[] iv) {
        throw new RuntimeException("Unsupported encrypt algorithm");
    }

    default String decryptFromBase64(String cipherTextBase64, byte[] key, byte[] iv) {
        throw new RuntimeException("Unsupported encrypt algorithm");
    }

    default String encryptToBase64(PublicKey publicKey, byte[] data) {
        throw new RuntimeException("Unsupported encrypt algorithm");
    }

    default String encryptToBase64(PrivateKey privateKey, byte[] data) {
        throw new RuntimeException("Unsupported encrypt algorithm");
    }

    default byte[] decryptFromBase64(PrivateKey privateKey, String encryptedDataBase64) {
        throw new RuntimeException("Unsupported encrypt algorithm");
    }

    default String hash(byte[] data) {
        throw new RuntimeException("Unsupported encrypt algorithm");
    }

    default KeyPair generateKeyPair() {
        throw new RuntimeException("Unsupported encrypt algorithm");
    }

    default PublicKey getPublicKey(PrivateKey privateKey) {
          throw new RuntimeException("Unsupported encrypt algorithm");
    }

}
