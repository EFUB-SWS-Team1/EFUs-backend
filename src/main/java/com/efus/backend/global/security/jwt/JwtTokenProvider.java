package com.efus.backend.global.security.jwt;

import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityTime,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityTime) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenValidityTime = accessTokenValidityTime * 1000;
        this.refreshTokenValidityTime = refreshTokenValidityTime * 1000;
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenValidityTime);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenValidityTime);
    }

    private String createToken(Long userId, long expireTime) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public boolean validateAccessToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void validateRefreshToken(String token) {
        try {
            parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    public long getAccessTokenExpirationInSeconds() {
        return accessTokenValidityTime / 1000;
    }

    public boolean shouldRotateRefreshToken(String token) {
        Date expiration = parseClaims(token).getExpiration();
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return remaining < (refreshTokenValidityTime / 2);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}