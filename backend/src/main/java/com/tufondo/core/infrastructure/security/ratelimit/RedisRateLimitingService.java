package com.tufondo.core.infrastructure.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RedisRateLimitingService {

    private final String redisHost;
    private final int redisPort;
    private boolean enabled;
    private boolean redisBacked;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, byte[]> connection;
    private LettuceBasedProxyManager<String> proxyManager;

    private final Map<String, Bucket> localBuckets = new ConcurrentHashMap<>();

    @Value("${rate-limiting.enabled:true}")
    private boolean rateLimitingEnabled;

    @Value("${rate-limiting.redis-backed:true}")
    private boolean useRedisBacked;

    public RedisRateLimitingService(
            @Value("${spring.data.redis.host:localhost}") String redisHost,
            @Value("${spring.data.redis.port:6379}") int redisPort,
            @Value("${rate-limiting.enabled:true}") boolean enabled,
            @Value("${rate-limiting.redis-backed:true}") boolean redisBacked) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.enabled = enabled;
        this.redisBacked = redisBacked;
    }

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("Rate limiting disabled");
            return;
        }

        if (redisBacked) {
            try {
                initRedisBacked();
                log.info("Redis-backed rate limiting initialized: {}:{}", redisHost, redisPort);
            } catch (Exception e) {
                log.warn("Failed to initialize Redis rate limiting, falling back to local: {}", e.getMessage());
                this.redisBacked = false;
            }
        }
    }

    private void initRedisBacked() {
        String redisUrl = String.format("redis://%s:%d", redisHost, redisPort);
        redisClient = RedisClient.create(redisUrl);
        connection = redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
        proxyManager = LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(5)))
                .build();
    }

    @PreDestroy
    public void destroy() {
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

    public boolean isAllowed(String key, int limit, Duration window) {
        if (!enabled) {
            return true;
        }

        try {
            if (redisBacked && proxyManager != null) {
                return checkRedisBacked(key, limit, window);
            } else {
                return checkLocal(key, limit, window);
            }
        } catch (Exception e) {
            log.error("Rate limit check failed for key: {}, error: {}", key, e.getMessage());
            return true;
        }
    }

    private boolean checkRedisBacked(String key, int limit, Duration window) {
        Bucket bucket = proxyManager.builder()
                .build(key, () -> createConfiguration(limit, window));

        return bucket.tryConsume(1);
    }

    private boolean checkLocal(String key, int limit, Duration window) {
        Bucket bucket = localBuckets.computeIfAbsent(key, k -> createLocalBucket(limit, window));
        return bucket.tryConsume(1);
    }

    private BucketConfiguration createConfiguration(int limit, Duration window) {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(limit, io.github.bucket4j.Refill.greedy(limit, window)))
                .build();
    }

    private Bucket createLocalBucket(int limit, Duration window) {
        Bandwidth limitBandwidth = Bandwidth.classic(limit, io.github.bucket4j.Refill.greedy(limit, window));
        return Bucket.builder()
                .addLimit(limitBandwidth)
                .build();
    }

    public RateLimitResult checkRateLimit(String key, int limit, Duration window) {
        if (!enabled) {
            return RateLimitResult.allowed(limit, 0);
        }

        try {
            if (redisBacked && proxyManager != null) {
                return checkRedisBackedResult(key, limit, window);
            } else {
                return checkLocalResult(key, limit, window);
            }
        } catch (Exception e) {
            log.error("Rate limit check failed for key: {}, error: {}", key, e.getMessage());
            return RateLimitResult.allowed(limit, 0);
        }
    }

    private RateLimitResult checkRedisBackedResult(String key, int limit, Duration window) {
        Bucket bucket = proxyManager.builder()
                .build(key, () -> createConfiguration(limit, window));

        var available = bucket.getAvailableTokens();
        boolean allowed = bucket.tryConsume(1);

        return new RateLimitResult(allowed, limit, Math.max(0, available));
    }

    private RateLimitResult checkLocalResult(String key, int limit, Duration window) {
        Bucket bucket = localBuckets.computeIfAbsent(key, k -> createLocalBucket(limit, window));
        var available = bucket.getAvailableTokens();
        boolean allowed = bucket.tryConsume(1);

        return new RateLimitResult(allowed, limit, Math.max(0, available));
    }

    public record RateLimitResult(boolean allowed, int limit, long remaining) {
        public static RateLimitResult allowed(int limit, long remaining) {
            return new RateLimitResult(true, limit, remaining);
        }
    }
}