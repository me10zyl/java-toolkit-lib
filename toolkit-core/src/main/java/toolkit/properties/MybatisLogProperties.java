package toolkit.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sql")
@Data
public class MybatisLogProperties {
    private boolean debug;
    private boolean trace;
}
