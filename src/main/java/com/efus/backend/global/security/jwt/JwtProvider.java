package com.efus.backend.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    // application.yml에 설정한 값들을 생성자에서 주입받아 초기화
    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityTime,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityTime) {

        // 1. 시크릿 키 문자열을 바이트 배열로 변환하고 암호화 키 객체로 만들기
        byte[] keyBytes = secretKey.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);

        // 2. 밀리초(ms) 단위로 변환
        this.accessTokenValidityTime = accessTokenValidityTime * 1000;
        this.refreshTokenValidityTime = refreshTokenValidityTime * 1000;
    }

    // Access Token 생성 메서드
    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenValidityTime);
    }

    // Refresh Token 생성 메서드
    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenValidityTime);
    }

    // 실제 JWT 문자열을 만드는 공통 로직
    private String createToken(Long userId, long expireTime) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 토큰의 주인
                .setIssuedAt(now) // 발급 시간
                .setExpiration(expireDate) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 생성한 암호화 키와 알고리즘으로 서명
                .compact();
    }

    // 토큰 유효성 검증 메서드
    public boolean validateToken(String token) {
        try {
            // 우리 서버의 비밀키로 토큰을 열었을 때 에러가 안 나면 정상 토큰
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 만료되었거나, 서명이 틀렸거나, 값이 비어있을 때
            return false;
        }
    }

    // 토큰에서 유저 ID 꺼내기
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
}
