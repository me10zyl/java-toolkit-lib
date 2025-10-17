package toolkit.enc.encrypts;

public class SM3HashAlogritm implements EncryptAlogritm{
    @Override
    public String hash(byte[] data) {
        return SM3Util.hash(data);
    }
}
