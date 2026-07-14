package com.efus.backend.infra.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(
        @JsonProperty("id") Long id, // 카카오 고유 회원번호
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            @JsonProperty("profile") Profile profile,
            @JsonProperty("email") String email
    ) {}

    public record Profile(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("profile_image_url") String profileImageUrl
    ) {}
}
