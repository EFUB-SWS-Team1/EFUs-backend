package com.efus.backend.domain.user.service;

import com.efus.backend.domain.user.dto.response.UserInfoResponse;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.repository.UserRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.global.security.jwt.JwtProvider;
import com.efus.backend.global.security.jwt.TokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public UserInfoResponse getMyInfo(String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String accessToken = authorization.substring(7);
        TokenStatus status = jwtProvider.validateToken(accessToken);

        if (status == TokenStatus.EXPIRED) {
            throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        } else if (status == TokenStatus.INVALID) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        Long userId = jwtProvider.getUserIdFromToken(accessToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new UserInfoResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getCreatedAt()
        );
    }
}