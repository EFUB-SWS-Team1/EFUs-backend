package com.efus.backend.infra.oauth.controller;

import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.repository.UserRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.global.security.jwt.JwtProvider;
import com.efus.backend.global.security.jwt.TokenStatus;
import com.efus.backend.infra.oauth.dto.request.KakaoLoginRequest;
import com.efus.backend.infra.oauth.dto.response.KakaoTokenResponse;
import com.efus.backend.infra.oauth.dto.response.KakaoUserInfoResponse;
import com.efus.backend.infra.oauth.dto.response.LoginResponse;
import com.efus.backend.infra.oauth.dto.response.ReissueResponse;
import com.efus.backend.infra.oauth.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @PostMapping("/api/auth/kakao/login")
    public ResponseEntity<LoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {

        if (request.authorizationCode() == null || request.authorizationCode().trim().isEmpty()) {
            throw new CustomException(ErrorCode.AUTHORIZATION_CODE_REQUIRED);
        }

        KakaoTokenResponse tokenResponse = kakaoAuthService.getKakaoAccessToken(request.authorizationCode());
        KakaoUserInfoResponse userInfo = kakaoAuthService.getKakaoUserInfo(tokenResponse.accessToken());

        KakaoAuthService.LoginProcessResult processResult = kakaoAuthService.loginOrSignUp(userInfo);
        User dbUser = processResult.user();
        boolean isNewUser = processResult.isNewUser();

        String accessToken = jwtProvider.createAccessToken(dbUser.getUserId());
        String refreshToken = jwtProvider.createRefreshToken(dbUser.getUserId());

        LoginResponse.UserInfo userDto = new LoginResponse.UserInfo(
                dbUser.getUserId(),
                dbUser.getName(),
                dbUser.getEmail(),
                dbUser.getProfileImageUrl()
        );

        LoginResponse responseBody = new LoginResponse(
                accessToken,
                "Bearer",
                3600,
                isNewUser,
                userDto
        );

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken) // Refresh Token 삽입
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }

    @PostMapping("/api/auth/reissue")
    public ResponseEntity<ReissueResponse> reissueToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        TokenStatus status = jwtProvider.validateToken(refreshToken);
        if (status == TokenStatus.EXPIRED) {
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        } else if (status == TokenStatus.INVALID) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.getUserIdFromToken(refreshToken);

        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        ReissueResponse responseBody = new ReissueResponse(
                newAccessToken,
                "Bearer",
                3600,
                true
        );

        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String accessToken = authorization.substring(7);

        if (jwtProvider.validateToken(accessToken) != TokenStatus.VALID) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        if (refreshToken == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        if (jwtProvider.validateToken(refreshToken) != TokenStatus.VALID) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
