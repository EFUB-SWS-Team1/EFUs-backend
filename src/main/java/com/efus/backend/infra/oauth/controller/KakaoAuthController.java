package com.efus.backend.infra.oauth.controller;

import com.efus.backend.domain.user.entity.User;
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

    @PostMapping("/api/auth/kakao/login")
    public ResponseEntity<LoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {

        // 1. 카카오 서버 연동 (토큰 발급 및 유저 정보 조회)
        KakaoTokenResponse tokenResponse = kakaoAuthService.getKakaoAccessToken(request.authorizationCode());
        KakaoUserInfoResponse userInfo = kakaoAuthService.getKakaoUserInfo(tokenResponse.accessToken());

        // 2. DB에 가입 또는 로그인 처리
        KakaoAuthService.LoginProcessResult processResult = kakaoAuthService.loginOrSignUp(userInfo);
        User dbUser = processResult.user();
        boolean isNewUser = processResult.isNewUser();

        // 3. 명세서 형식에 맞춘 응답 Body 생성
        LoginResponse.UserInfo userDto = new LoginResponse.UserInfo(
                dbUser.getId(),
                dbUser.getNickname(),
                dbUser.getEmail(),
                dbUser.getProfileImageUrl()
        );

        LoginResponse responseBody = new LoginResponse(
                "임시_EFUs_Access_Token",
                "Bearer",
                3600,
                isNewUser, // DB 판단 결과에 따라 true/false 자동 할당
                userDto
        );

        // 4. 명세서 형식에 맞춘 Header (Set-Cookie) 생성
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "dummy_refresh_token_value")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }
}
