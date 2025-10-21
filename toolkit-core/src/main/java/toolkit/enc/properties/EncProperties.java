package toolkit.enc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import toolkit.enc.dto.SupportEncrypt;

@Component
@ConfigurationProperties(
        prefix = "enc.http"
)
@Data
public class EncProperties {
    private boolean enabled;
    private String disableHeader = "X-Disable-Encrypt";
    private boolean logDecrypt = true;
    private boolean checkSign = false;
    private boolean excludeSwagger = true;

    private SupportEncrypt encryptAlgorithm = SupportEncrypt.AES;

    private String rsaPrivateKeyBase64;
    private String rsaPublicKeyBase64Frontend;

    private String sm2PrivateKeyHex;
    private String sm2PublicKeyHexFrontend;

    private String sm4Key;
    private String aesKey;
}
