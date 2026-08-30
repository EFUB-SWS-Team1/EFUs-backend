package com.efus.backend.infra.oauth.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Profile("prod")
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String KEY_PREFIX = "refreshToken:";

    private final StringRedisTemplate redisTemplate;
    private final Duration refreshTokenTtl;

    public RedisRefreshTokenStore(
            StringRedisTemplate redisTemplate,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenTtl = Duration.ofSeconds(refreshTokenValidityInSeconds);
    }

    @Override
    public void saveOrUpdate(Long userId, String token) {
        redisTemplate.opsForValue().set(key(userId), token, refreshTokenTtl);
    }

    @Override
    public Optional<String> findTokenByUserId(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
    }

    @Override
    public void deleteByUserId(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
