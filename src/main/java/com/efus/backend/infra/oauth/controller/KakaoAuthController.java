package com.efus.backend.infra.oauth.controller;

import com.efus.backend.global.response.ApiResponse;
import com.efus.backend.infra.oauth.dto.request.KakaoLoginRequest;
import com.efus.backend.infra.oauth.dto.response.LoginResponse;
import com.efus.backend.infra.oauth.dto.response.ReissueResponse;
import com.efus.backend.infra.oauth.service.KakaoAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private static final long REFRESH_COOKIE_MAX_AGE = 1209600; // 14일

    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {

        KakaoAuthService.LoginResult result = kakaoAuthService.kakaoLogin(request.authorizationCode());
        ResponseCookie cookie = createRefreshTokenCookie(result.refreshToken(), REFRESH_COOKIE_MAX_AGE);

        HttpStatus status = result.isNewUser() ? HttpStatus.CREATED : HttpStatus.OK;
        String message = result.isNewUser() ? "회원가입 및 로그인 성공" : "로그인 성공";

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(result.loginResponse(), message));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissueToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        KakaoAuthService.ReissueResult result = kakaoAuthService.reissue(refreshToken);
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();

        if (result.rotated()) {
            ResponseCookie newCookie = createRefreshTokenCookie(result.newRefreshToken(), REFRESH_COOKIE_MAX_AGE);
            responseBuilder.header(HttpHeaders.SET_COOKIE, newCookie.toString());
        }

        return responseBuilder.body(ApiResponse.success(result.reissueResponse(), "토큰 재발급 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        kakaoAuthService.logout(refreshToken);
        ResponseCookie deleteCookie = createRefreshTokenCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ApiResponse.successWithoutData());
    }

    private ResponseCookie createRefreshTokenCookie(String token, long maxAge) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}