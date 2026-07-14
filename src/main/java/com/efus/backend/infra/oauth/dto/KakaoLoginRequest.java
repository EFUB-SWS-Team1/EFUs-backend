package com.efus.backend.infra.oauth.dto;

public record KakaoLoginRequest(
        String authorizationCode
) {
}
