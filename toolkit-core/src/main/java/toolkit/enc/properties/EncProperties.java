package toolkit.enc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = "enc.http"
)
@Data
public class EncProperties {
    private String sm2PrivateKeyHex;

    private boolean enabled;
    private String disableHeader = "X-Disable-Encrypt";
    private boolean logDecrypt = true;

    private String sm2PublicKeyHex2;
}
