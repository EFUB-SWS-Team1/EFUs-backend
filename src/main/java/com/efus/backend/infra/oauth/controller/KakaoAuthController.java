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

    /**
     * 카카오 로그인
     * 프론트엔드가 넘겨준 인가 코드를 받아 카카오 로그인을 처리하고 인증 토큰을 내려줍니다.
     */
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {

        // KakaoAuthService에 인가 코드를 넘겨주고, 비즈니스 로직이 담긴 LoginResult를 받아옵니다.
        KakaoAuthService.LoginResult result = kakaoAuthService.kakaoLogin(request.authorizationCode());
        // createRefreshTokenCookie를 호출해 Refresh Token을 브라우저에 안전하게 저장할 쿠키 객체로 만듭니다.
        ResponseCookie cookie = createRefreshTokenCookie(result.refreshToken(), REFRESH_COOKIE_MAX_AGE);

        // 가입 여부(isNewUser)에 따라 HTTP 201(생성됨) 또는 200(성공) 코드를 분기 처리합니다.
        HttpStatus status = result.isNewUser() ? HttpStatus.CREATED : HttpStatus.OK;
        String message = result.isNewUser() ? "회원가입 및 로그인 성공" : "로그인 성공";

        // 공통 응답 객체(ApiResponse)로 바디를 감싸고, 헤더에 쿠키를 실어 프론트엔드로 반환합니다.
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(result.loginResponse(), message));
    }

    /**
     * 토큰 재발급
     * Access Token이 만료되었을 때, 브라우저가 보낸 Refresh Token 쿠키를 읽어 새 토큰을 발급합니다.
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissueToken(
            // @CookieValue(required = false)로 브라우저에 있는 refreshToken 쿠키를 빼옵니다. (없으면 null이 들어옵니다.)
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        // 서비스에 토큰 재발급을 지시합니다.
        KakaoAuthService.ReissueResult result = kakaoAuthService.reissue(refreshToken);
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();

        // 만약 서비스에서 Refresh Token까지 새로 교체했다면(result.rotated()), 새 쿠키를 구워서 응답 헤더에 덮어씌웁니다.
        if (result.rotated()) {
            ResponseCookie newCookie = createRefreshTokenCookie(result.newRefreshToken(), REFRESH_COOKIE_MAX_AGE);
            responseBuilder.header(HttpHeaders.SET_COOKIE, newCookie.toString());
        }

        return responseBuilder.body(ApiResponse.success(result.reissueResponse(), "토큰 재발급 성공"));
    }

    /**
     * 로그아웃
     * 사용자의 로그인 세션을 종료하고 토큰을 폐기합니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        // 서비스를 호출해 DB에 저장된 Refresh Token을 지웁니다.
        kakaoAuthService.logout(refreshToken);
        // maxAge(0)인 껍데기 쿠키를 프론트엔드로 내려보내서, 브라우저가 기존 쿠키를 즉시 삭제하도록 유도합니다.
        ResponseCookie deleteCookie = createRefreshTokenCookie("", 0); // 쿠키 수명을 0으로 만들어 즉시 삭제

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ApiResponse.successWithoutData());
    }

    // 쿠키 생성 헬퍼 메서드
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