package com.efus.backend.infra.oauth.dto.response;

public record ReissueResponse(
        String accessToken,
        String tokenType,
        Integer expiresIn,
        Boolean refreshTokenRotated
) {}
