package com.efus.backend.infra.oauth.service;

import com.efus.backend.domain.user.entity.Status;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.repository.UserRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.global.security.jwt.JwtTokenProvider;
import com.efus.backend.infra.oauth.client.KakaoOAuthClient;
import com.efus.backend.infra.oauth.dto.response.KakaoTokenResponse;
import com.efus.backend.infra.oauth.dto.response.KakaoUserInfoResponse;
import com.efus.backend.infra.oauth.dto.response.LoginResponse;
import com.efus.backend.infra.oauth.dto.response.ReissueResponse;
import com.efus.backend.infra.oauth.entity.RefreshToken;
import com.efus.backend.infra.oauth.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoOAuthClient kakaoOAuthClient;

    @Transactional
    public LoginResult kakaoLogin(String authorizationCode) {
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.getKakaoAccessToken(authorizationCode);
        KakaoUserInfoResponse userInfo = kakaoOAuthClient.getKakaoUserInfo(tokenResponse.accessToken());
        Long kakaoId = userInfo.id();

        boolean isNewUser = false;
        User user = userRepository.findByKakaoId(kakaoId).orElse(null);

        if (user == null) {
            user = User.builder()
                    .kakaoId(kakaoId)
                    .name(userInfo.kakaoAccount().profile().nickname())
                    .email(userInfo.kakaoAccount().email())
                    .profileImageUrl(userInfo.kakaoAccount().profile().profileImageUrl())
                    .status(Status.ACTIVE)
                    .build();
            user = userRepository.save(user);
            isNewUser = true;
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        saveOrUpdateRefreshToken(user.getId(), refreshToken);

        LoginResponse loginResponse = LoginResponse.from(accessToken, jwtTokenProvider.getAccessTokenExpirationInSeconds(), isNewUser, user);
        return new LoginResult(loginResponse, refreshToken, isNewUser);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public ReissueResult reissue(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        jwtTokenProvider.validateRefreshToken(refreshTokenValue);
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);

        RefreshToken storedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedToken.getToken().equals(refreshTokenValue)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        boolean rotated = false;
        String newRefreshToken = null;

        if (jwtTokenProvider.shouldRotateRefreshToken(refreshTokenValue)) {
            newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
            storedToken.rotateToken(newRefreshToken);
            rotated = true;
        }

        ReissueResponse response = ReissueResponse.from(newAccessToken, jwtTokenProvider.getAccessTokenExpirationInSeconds(), rotated);
        return new ReissueResult(response, newRefreshToken, rotated);
    }

    private void saveOrUpdateRefreshToken(Long userId, String token) {
        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        existing -> existing.rotateToken(token),
                        () -> refreshTokenRepository.save(RefreshToken.builder().userId(userId).token(token).build())
                );
    }

    public record LoginResult(LoginResponse loginResponse, String refreshToken, boolean isNewUser) {}
    public record ReissueResult(ReissueResponse reissueResponse, String newRefreshToken, boolean rotated) {}
}