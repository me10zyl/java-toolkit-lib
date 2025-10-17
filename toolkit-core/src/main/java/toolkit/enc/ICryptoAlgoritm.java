package toolkit.enc;

public interface ICryptoAlgoritm {
    String encrypt(String value, Object target);
    String decrypt(String value, Object target);
}
