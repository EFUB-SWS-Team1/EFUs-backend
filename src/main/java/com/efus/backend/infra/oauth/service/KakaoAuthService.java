package com.efus.backend.infra.oauth.service;

import com.efus.backend.domain.user.entity.Status;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.repository.UserRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.infra.oauth.dto.response.KakaoTokenResponse;
import com.efus.backend.infra.oauth.dto.response.KakaoUserInfoResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
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
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        return restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new CustomException(ErrorCode.KAKAO_AUTHENTICATION_FAILED);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new CustomException(ErrorCode.KAKAO_API_ERROR);
                })
                .body(KakaoTokenResponse.class);
    }

    public KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {
        return restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new CustomException(ErrorCode.KAKAO_AUTHENTICATION_FAILED);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new CustomException(ErrorCode.KAKAO_API_ERROR);
                })
                .body(KakaoUserInfoResponse.class);
    }

    @Transactional
    public LoginProcessResult loginOrSignUp(KakaoUserInfoResponse userInfo) {
        String kakaoId = String.valueOf(userInfo.id());

        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);

        if (existingUser.isPresent()) {
            return new LoginProcessResult(existingUser.get(), false);
        } else {
            User newUser = User.builder()
                    .kakaoId(kakaoId)
                    .name(userInfo.kakaoAccount().profile().nickname())
                    .email(userInfo.kakaoAccount().email())
                    .profileImageUrl(userInfo.kakaoAccount().profile().profileImageUrl())
                    .status(Status.ACTIVE)
                    .build();

            User savedUser = userRepository.save(newUser);
            return new LoginProcessResult(savedUser, true);
        }
    }

    public record LoginProcessResult(User user, boolean isNewUser) {}
}
