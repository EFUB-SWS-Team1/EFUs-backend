package com.efus.backend.domain.user.dto.response;

import java.time.LocalDateTime;

public record UserInfoResponse(
        Long userId,
        String name,
        String email,
        String profileImageUrl,
        LocalDateTime createdAt
) {
}
