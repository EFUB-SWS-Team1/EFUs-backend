package com.efus.backend.infra.oauth.controller;

import com.efus.backend.infra.oauth.dto.KakaoLoginRequest;
import com.efus.backend.infra.oauth.dto.KakaoTokenResponse;
import com.efus.backend.infra.oauth.dto.KakaoUserInfoResponse;
import com.efus.backend.infra.oauth.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/api/auth/kakao/login")
    public String kakaoLogin(@RequestBody KakaoLoginRequest request) {

        // 1. 프론트에서 받은 코드로 카카오 Access Token 획득
        KakaoTokenResponse tokenResponse = kakaoAuthService.getKakaoAccessToken(request.authorizationCode());

        // 2. 획득한 토큰으로 카카오 유저 정보 조회
        KakaoUserInfoResponse userInfo = kakaoAuthService.getKakaoUserInfo(tokenResponse.accessToken());

        // 3. 콘솔에 내 정보가 잘 나오는지 로그 찍어보기
        System.out.println("카카오 고유 ID: " + userInfo.id());
        System.out.println("이름(닉네임): " + userInfo.kakaoAccount().profile().nickname());
        System.out.println("이메일: " + userInfo.kakaoAccount().email());
        System.out.println("프로필 사진 URL: " + userInfo.kakaoAccount().profile().profileImageUrl());

        // 임시로 화면에 내 이름 띄워주기
        return userInfo.kakaoAccount().profile().nickname() + " 회원 정보 조회 성공";
    }
}
