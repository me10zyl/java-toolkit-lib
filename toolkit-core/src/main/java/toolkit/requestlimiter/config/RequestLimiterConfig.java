package toolkit.requestlimiter.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.serialization.Mapper;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import toolkit.requestlimiter.properties.RateLimitProperties;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RequestLimiterConfig {

    @Bean
    public RateLimitProperties rateLimitProperties() {
        return new RateLimitProperties();
    }

    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    @Bean
    public ProxyManager<byte[]> proxyManager(RedissonClient redissonClient) {
        log.info("request limiter proxy manager inited.");
        return RedissonBasedProxyManager.builderFor(((Redisson) redissonClient).getCommandExecutor())
                .withKeyMapper(Mapper.BYTES).build();
    }

    // 1. 配置 Redisson 客户端
    @Bean
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(RedisConnectionFactory redisConnectionFactory) {
        Config config = new Config();
        try {
            RedisClusterConnection clusterConnection = redisConnectionFactory.getClusterConnection();
            boolean isCluster = false;
            if(clusterConnection != null) {
                for (RedisClusterNode clusterGetNode : clusterConnection.clusterGetNodes()) {
                    config.useClusterServers().addNodeAddress("redis://" + clusterGetNode.toString());
                }
            }
        }catch (InvalidDataAccessApiUsageException exception){
            LettuceConnectionFactory factory = (LettuceConnectionFactory) redisConnectionFactory;
            String redisURI =  //factory.getHostName()
                    "redis://" + factory.getHostName() + ":" + factory.getPort();
            //ReflectUtil.getFieldValue((ReflectUtil.getFieldValue((LettuceConnectionFactory) redisConnectionFactory, "client")), "redisURI").toString();
            SingleServerConfig singleServerConfig = config.useSingleServer();
            singleServerConfig.setAddress(redisURI);
            singleServerConfig.setDatabase(factory.getDatabase());
            singleServerConfig.setPassword(factory.getPassword());
        }
        return Redisson.create(config);
    }
}
