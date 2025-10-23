package mytoolkit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import toolkit.requestlimiter.RequestLimiterInterceptor;
import toolkit.requestlimiter.properties.RateLimitProperties;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ProxyManager<byte[]> proxyManager;
    @Autowired
    private RateLimitProperties rateLimitProperties;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLimiterInterceptor(proxyManager, rateLimitProperties));
    }
}
