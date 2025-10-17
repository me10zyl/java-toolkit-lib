package toolkit.enc.encrypts;

import java.nio.charset.StandardCharsets;

public class MD5HashAlogritm implements EncryptAlogritm{
    @Override
    public String hash(byte[] data) {
        return MD5Util.getMd5Hex(new String(data, StandardCharsets.UTF_8));
    }
}
