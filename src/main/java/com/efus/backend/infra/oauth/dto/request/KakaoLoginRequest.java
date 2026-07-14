package com.efus.backend.infra.oauth.dto.request;

public record KakaoLoginRequest(
        String authorizationCode
) {
}
