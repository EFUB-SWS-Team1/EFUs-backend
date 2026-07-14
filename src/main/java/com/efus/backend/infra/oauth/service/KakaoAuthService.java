package com.efus.backend.infra.oauth.service;

import com.efus.backend.domain.user.entity.Status;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.repository.UserRepository;
import com.efus.backend.infra.oauth.dto.response.KakaoTokenResponse;
import com.efus.backend.infra.oauth.dto.response.KakaoUserInfoResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class KakaoAuthService {

    private final RestClient restClient;
    private final String clientId;
    private final String redirectUri;
    private final UserRepository userRepository;

    // application.yml에 적어둔 키와 주소를 가져오기
    public KakaoAuthService(
            @Value("${kakao.client-id}") String clientId,
            @Value("${kakao.redirect-uri}") String redirectUri,
            UserRepository userRepository) {
        this.restClient = RestClient.create();
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.userRepository = userRepository;
    }

    public KakaoTokenResponse getKakaoAccessToken(String code) {
        // 1. 카카오 서버로 보낼 파라미터 세팅
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        // 2. RestClient로 POST 요청 쏘기
        return restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(KakaoTokenResponse.class); // 응답받은 JSON을 DTO로 변환
    }

    public KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {
        return restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken) // 토큰을 헤더에 넣음
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .body(KakaoUserInfoResponse.class); // 응답받은 JSON을 방금 만든 DTO로 변환
    }

    @Transactional
    public LoginProcessResult loginOrSignUp(KakaoUserInfoResponse userInfo) {
        String kakaoId = String.valueOf(userInfo.id());

        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);

        if (existingUser.isPresent()) {
            // 1. 이미 가입된 유저라면 그대로 반환 (isNewUser = false)
            return new LoginProcessResult(existingUser.get(), false);
        } else {
            // 2. 처음 온 유저라면 DB에 INSERT (isNewUser = true)
            User newUser = User.builder()
                    .kakaoId(kakaoId)
                    .nickname(userInfo.kakaoAccount().profile().nickname())
                    .email(userInfo.kakaoAccount().email())
                    .profileImageUrl(userInfo.kakaoAccount().profile().profileImageUrl())
                    .status(Status.ACTIVE) // Status Enum 값은 일단 ACTIVE으로 설정해두었습니다.
                    .build();

            User savedUser = userRepository.save(newUser);
            return new LoginProcessResult(savedUser, true);
        }
    }

    // 서비스 내부에서 반환값을 깔끔하게 넘기기 위한 레코드
    public record LoginProcessResult(User user, boolean isNewUser) {}
}
