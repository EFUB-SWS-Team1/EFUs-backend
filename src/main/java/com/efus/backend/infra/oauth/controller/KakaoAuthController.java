package com.efus.backend.infra.oauth.controller;

import com.efus.backend.domain.user.entity.User;
import com.efus.backend.global.security.jwt.JwtProvider;
import com.efus.backend.infra.oauth.dto.request.KakaoLoginRequest;
import com.efus.backend.infra.oauth.dto.response.KakaoTokenResponse;
import com.efus.backend.infra.oauth.dto.response.KakaoUserInfoResponse;
import com.efus.backend.infra.oauth.dto.response.LoginResponse;
import com.efus.backend.infra.oauth.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private final JwtProvider jwtProvider;

    @PostMapping("/api/auth/kakao/login")
    public ResponseEntity<LoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {

        // 1. 카카오 서버 연동
        KakaoTokenResponse tokenResponse = kakaoAuthService.getKakaoAccessToken(request.authorizationCode());
        KakaoUserInfoResponse userInfo = kakaoAuthService.getKakaoUserInfo(tokenResponse.accessToken());

        // 2. 우리 DB에 가입 또는 로그인 처리
        KakaoAuthService.LoginProcessResult processResult = kakaoAuthService.loginOrSignUp(userInfo);
        User dbUser = processResult.user();
        boolean isNewUser = processResult.isNewUser();

        // 3. EFUs JWT 발급
        String accessToken = jwtProvider.createAccessToken(dbUser.getId());
        String refreshToken = jwtProvider.createRefreshToken(dbUser.getId());

        // 4. 명세서 형식에 맞춘 응답 Body 생성
        LoginResponse.UserInfo userDto = new LoginResponse.UserInfo(
                dbUser.getId(),
                dbUser.getNickname(),
                dbUser.getEmail(),
                dbUser.getProfileImageUrl()
        );

        LoginResponse responseBody = new LoginResponse(
                accessToken, // Access Token 삽입
                "Bearer",
                3600, // Access Token 만료까지 남은 시간
                isNewUser,
                userDto
        );

        // 5. 명세서 형식에 맞춘 Header (Set-Cookie) 생성
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken) // Refresh Token 삽입
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        // Cookie에 EFUs Refresh Token을 담아 반환
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }
}
