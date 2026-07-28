package com.efus.backend.infra.oauth.dto.response;

public record ReissueResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        Boolean refreshTokenRotated
) {
    public static ReissueResponse from(String accessToken, long expiresIn, boolean rotated) {
        return new ReissueResponse(accessToken, "Bearer", expiresIn, rotated);
    }
}