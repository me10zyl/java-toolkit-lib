package toolkit.enc.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PublicKey {
    private String publicKeyHex;
    public PublicKey(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
    }
}
