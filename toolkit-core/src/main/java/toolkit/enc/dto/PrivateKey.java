package toolkit.enc.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PrivateKey {
    private String privateKeyHex;

    public PrivateKey(String privateKeyHex) {
        this.privateKeyHex = privateKeyHex;
    }
}
