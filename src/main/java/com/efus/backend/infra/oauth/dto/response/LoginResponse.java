package com.efus.backend.infra.oauth.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Integer expiresIn,
        Boolean isNewUser,
        UserInfo user
) {
    public record UserInfo(
            Long userId,
            String name,
            String email,
            String profileImageUrl
    ) {}
}
