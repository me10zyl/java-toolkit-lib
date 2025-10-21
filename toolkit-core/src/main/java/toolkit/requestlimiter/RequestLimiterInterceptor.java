package toolkit.requestlimiter;


import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import toolkit.requestlimiter.anno.RateLimit;
import toolkit.requestlimiter.properties.RateLimitProperties;
import toolkit.utils.IpUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class RequestLimiterInterceptor implements HandlerInterceptor {

    @Resource
    private ProxyManager<byte[]> proxyManager;

    @Resource
    private RateLimitProperties rateLimitProperties;
    private BucketConfiguration limit;

    @PostConstruct
    private void initLimit(){
         limit =
                BucketConfiguration.builder()
                        .addLimit(limit ->
                                limit.capacity(rateLimitProperties.getCapacity()).refillGreedy(rateLimitProperties.getRefillTokens(), rateLimitProperties.getRefillDuration()))
                        .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果未启用限流，则直接通过
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        Method method = ((HandlerMethod) handler).getMethod();
        RateLimit annotation = method.getAnnotation(RateLimit.class);
        if (annotation != null) {
            // 方法上未添加 @RateLimit 注解，直接通过
            if(!annotation.enabled()){
                return true;
            }
        }

        // 获取唯一的客户端标识，例如 IP 地址
        String key = IpUtil.getClientIpAddress(request);

        // 从代理管理器中获取或创建一个令牌桶
        Bucket bucket = null;
        if(annotation == null){
//            bucket = proxyManager.getProxy(key.getBytes(StandardCharsets.UTF_8), () -> {
//                return limit;
//            });
        }
        else {
            // 方法上添加了 @RateLimit 注解，检查是否需要限流
            if(annotation.capacity() > 0 && annotation.refillTokens() > 0 && annotation.refillDuration() > 0) {
                // 从代理管理器中获取或创建一个令牌桶
//                 bucket = proxyManager.getProxy(key.getBytes(StandardCharsets.UTF_8), () -> {
//                    return BucketConfiguration.builder()
//                            .addLimit(limit ->
//                                    limit.capacity(annotation.capacity()).refillGreedy(annotation.refillTokens(), Duration.of(annotation.refillDuration(), annotation.refillDurationUnit().toChronoUnit())))
//                            .build();
//                });
            }
        }

        // 尝试消费一个令牌
        if (bucket.tryConsume(1)) {
            // 成功消费，请求继续
            return true;
        } else {
            // 令牌不足，返回 429 错误
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Too Many Requests");
            return false;
        }
    }
}
