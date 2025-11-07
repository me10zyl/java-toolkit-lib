package mytoolkit;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import toolkit.requestlimiter.config.RequestLimiterConfig;

@Configuration
@Import(RequestLimiterConfig.class)
public class RequestLimitConf {
}
