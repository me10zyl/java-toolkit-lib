package toolkit.enc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyPair {
    private PrivateKey privateKey;
    private PublicKey publicKey;
}
