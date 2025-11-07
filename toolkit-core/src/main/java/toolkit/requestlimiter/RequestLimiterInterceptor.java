package toolkit.requestlimiter;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.serialization.Mapper;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import toolkit.requestlimiter.anno.RateLimit;
import toolkit.requestlimiter.properties.RateLimitProperties;
import toolkit.utils.IpUtil;
import toolkit.utils.TimeUnitConverter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;


public class RequestLimiterInterceptor implements HandlerInterceptor {

    private final ProxyManager<String> proxyManager;
    private final RateLimitProperties rateLimitProperties;
    private final BucketConfiguration limit;

    public RequestLimiterInterceptor(ProxyManager<String> proxyManager, RateLimitProperties rateLimitProperties) {
        this.proxyManager = proxyManager;
        this.rateLimitProperties = rateLimitProperties;
        this.limit =
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
            if (!annotation.enabled()) {
                return true;
            }
        }

        // 获取唯一的客户端标识，例如 IP 地址
        String key = "bucket:ratelimiter:" + IpUtil.getClientIpAddress(request) + ":" + request.getRequestURI();

        // 从代理管理器中获取或创建一个令牌桶
        Bucket bucket = null;
        if (annotation == null) {
            bucket =  proxyManager.builder().build(key, () -> {
                return limit;
            });
        } else {
            long capacity = rateLimitProperties.getCapacity();
            int refillTokens = rateLimitProperties.getRefillTokens();
            Duration refillDuration = rateLimitProperties.getRefillDuration();
            if (annotation.capacity() > 0) {
                capacity = annotation.capacity();
            }
            if (annotation.refillTokens() > 0) {
                refillTokens = annotation.refillTokens();
            }
            if (annotation.refillDuration() > 0) {
                refillDuration = Duration.of(annotation.refillDuration(), TimeUnitConverter.toTemporalUnit(annotation.refillDurationUnit()));
            }
            // 从代理管理器中获取或创建一个令牌桶
            Bandwidth bd = BandwidthBuilder.builder().capacity(capacity)
                    .refillGreedy(refillTokens, refillDuration).build();

            bucket = proxyManager.builder().build(key, () -> {
                return BucketConfiguration.builder()
                        .addLimit(bd)
                        .build();
            });
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
