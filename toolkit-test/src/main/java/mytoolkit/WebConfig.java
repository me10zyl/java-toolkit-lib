package mytoolkit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import toolkit.requestlimiter.RequestLimiterInterceptor;
import toolkit.requestlimiter.anno.EnableRequestLimiter;
import toolkit.requestlimiter.properties.RateLimitProperties;

@Configuration
@EnableRequestLimiter
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {


    private final ProxyManager<String> proxyManager;
    private final RateLimitProperties rateLimitProperties;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLimiterInterceptor(proxyManager, rateLimitProperties));
    }
}
