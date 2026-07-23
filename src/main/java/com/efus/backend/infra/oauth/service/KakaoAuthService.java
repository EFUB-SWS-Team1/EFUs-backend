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

    /**
     * 카카오 로그인
     * 카카오 서버에서 유저 정보를 가져와 우리 DB에 연동하고 JWT를 발급합니다.
     */
    @Transactional
    public LoginResult kakaoLogin(String authorizationCode) {
        // KakaoOAuthClient를 통해 카카오 서버에 HTTP 요청을 보내고 유저 프로필(카카오 고유 ID, 닉네임 등)을 받아옵니다.
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.getKakaoAccessToken(authorizationCode);
        KakaoUserInfoResponse userInfo = kakaoOAuthClient.getKakaoUserInfo(tokenResponse.accessToken());
        Long kakaoId = userInfo.id();

        // 카카오 고유 ID로 우리 DB(UserRepository)를 뒤져서 이미 가입한 사람인지 확인합니다.
        boolean isNewUser = false;
        User user = userRepository.findByKakaoId(kakaoId).orElse(null);

        // 없으면 새 User 엔티티를 만들어 DB에 저장합니다.
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

        // JwtTokenProvider로 해당 유저의 ID를 담은 Access/Refresh Token을 찍어냅니다.
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        saveOrUpdateRefreshToken(user.getId(), refreshToken);

        // 발급된 Refresh Token을 우리 DB(RefreshTokenRepository)에 저장하고 결과를 컨트롤러로 돌려보냅니다.
        LoginResponse loginResponse = LoginResponse.from(accessToken, jwtTokenProvider.getAccessTokenExpirationInSeconds(), isNewUser, user);
        return new LoginResult(loginResponse, refreshToken, isNewUser);
    }

    /**
     * 로그아웃
     * 탈취된 토큰의 생명줄을 서버에서 영구적으로 끊어버리기
     */
    @Transactional
    public void logout(String refreshTokenValue) {
        // 요청 값 검증
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        // 2. 토큰에서 유저 식별
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);

        // 3. DB에서 토큰 완전 삭제
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * 토큰 재발급
     * Refresh Token의 유효성을 꼼꼼히 검사하고 Access Token을 갱신합니다.
     */
    @Transactional
    public ReissueResult reissue(String refreshTokenValue) {
        // 방어적 프로그래밍 관점에서 넘어온 토큰이 널(Null)인지 먼저 검사합니다.
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        // JwtTokenProvider를 통해 토큰 자체의 암호화 서명이 유효한지, 기간이 안 지났는지 검증합니다.
        jwtTokenProvider.validateRefreshToken(refreshTokenValue);
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);

        // 토큰에서 유저 ID를 뽑아낸 뒤, 우리 DB에 저장된 Refresh Token과 일치하는지 비교합니다. (탈취된 예전 토큰을 막기 위한 필수 보안 로직입니다.)
        RefreshToken storedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedToken.getToken().equals(refreshTokenValue)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 모든 검문을 통과하면 새 Access Token을 만듭니다.
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        boolean rotated = false;
        String newRefreshToken = null;

        // Refresh Token의 남은 수명을 계산해서, 절반 이하로 남았다면 새 Refresh Token으로 교체(Rotation)하여 보안과 편의성을 둘 다 챙깁니다.
        if (jwtTokenProvider.shouldRotateRefreshToken(refreshTokenValue)) {
            newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
            storedToken.rotateToken(newRefreshToken);
            rotated = true;
        }

        ReissueResponse response = ReissueResponse.from(newAccessToken, jwtTokenProvider.getAccessTokenExpirationInSeconds(), rotated);
        return new ReissueResult(response, newRefreshToken, rotated);
    }

    /**
     * saveOrUpdateRefreshToken 헬퍼 메서드
     * 사용자가 여러 기기에서 로그인하거나 재로그인할 때 Refresh Token을 안전하게 저장(또는 덮어쓰기)합니다.
     */
    private void saveOrUpdateRefreshToken(Long userId, String token) {
        // DB에 해당 유저의 토큰이 이미 존재하면 새 토큰 값으로 덮어쓰고(rotateToken), 아예 없다면 새로 만들어서(save) 저장합니다.
        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        existing -> existing.rotateToken(token),
                        () -> refreshTokenRepository.save(RefreshToken.builder().userId(userId).token(token).build())
                );
    }

    public record LoginResult(LoginResponse loginResponse, String refreshToken, boolean isNewUser) {}
    public record ReissueResult(ReissueResponse reissueResponse, String newRefreshToken, boolean rotated) {}
}