package com.efus.backend.infra.oauth.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        Boolean isNewUser,
        UserInfo user
) {
    public record UserInfo(
            Long userId,
            String name,
            String email,
            String profileImageUrl
    ) {}

    public static LoginResponse from(String accessToken, long expiresIn, boolean isNewUser, com.efus.backend.domain.user.entity.User user) {
        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresIn,
                isNewUser,
                new UserInfo(user.getId(), user.getName(), user.getEmail(), user.getProfileImageUrl())
        );
    }
}