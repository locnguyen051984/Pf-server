package com.Pf.auth_service.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.cluster.RedisClusterClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public LettuceBasedProxyManager<byte[]> proxyManager(RedisConnectionFactory redisConnectionFactory) {
        if (!(redisConnectionFactory instanceof LettuceConnectionFactory)) {
            throw new IllegalStateException("RedisConnectionFactory is not LettuceConnectionFactory. Rate Limiting requires Lettuce.");
        }
        
        LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) redisConnectionFactory;
        Object nativeClient = lettuceConnectionFactory.getNativeClient();
        
        if (nativeClient instanceof RedisClient redisClient) {
            return LettuceBasedProxyManager.builderFor(redisClient)
                .withExpirationStrategy(
                    ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10))
                )
                .build();
        } else if (nativeClient instanceof RedisClusterClient redisClusterClient) {
            return LettuceBasedProxyManager.builderFor(redisClusterClient)
                .withExpirationStrategy(
                    ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10))
                )
                .build();
        }
        
        throw new IllegalStateException("Unsupported native client: " + (nativeClient != null ? nativeClient.getClass() : "null"));
    }
}
