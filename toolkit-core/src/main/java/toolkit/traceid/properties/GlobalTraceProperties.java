package toolkit.traceid.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "globaltrace")
@Data
public class GlobalTraceProperties {
    private boolean enabled = true;
    private boolean enableLog = true;
}
