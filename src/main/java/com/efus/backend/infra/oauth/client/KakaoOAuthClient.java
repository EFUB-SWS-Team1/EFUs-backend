package com.efus.backend.infra.oauth.client;

import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.infra.oauth.dto.response.KakaoTokenResponse;
import com.efus.backend.infra.oauth.dto.response.KakaoUserInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * 카카오 로그인 구현에 있어 가장 최전방에서 카카오 서버와 직접 통신(HTTP 요청)하는 역할
 * Spring Boot 애플리케이션(우리 서버)이 클라이언트가 되어 카카오의 API 서버로 요청을 보내고 응답을 받아오는 '외부 API 통신 전담 객체'
 */

@Component
public class KakaoOAuthClient {

    /**
     * 클래스 선언부 및 필드 (인프라 설정)
     */
    // 외부 API(카카오 서버)로 HTTP 요청을 보내기 위한 스프링의 최신 HTTP 클라이언트
    private final RestClient restClient = RestClient.create();

    // 보안상 코드에 직접 적으면 안 되는 민감 정보 -> application.yml의 값을 주입받음
    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 카카오 Access Token 발급
     * 프론트엔드가 유저를 카카오 로그인 페이지로 보내고,
     * 유저가 로그인을 마치면 카카오가 프론트엔드에게 '인가 코드(Authorization Code)'라는 임시 비밀번호를 줍니다.
     * 프론트엔드가 이 코드를 우리 백엔드로 넘겨주면, 이 메서드가 실행됩니다.
     */
    public KakaoTokenResponse getKakaoAccessToken(String authorizationCode) {
        // 카카오 공식 문서에서 권장하는 x-www-form-urlencoded 형태(폼 데이터)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", authorizationCode);

        return restClient.post()
                // 카카오의 토큰 발급 서버 주소로 POST 요청
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> { throw new CustomException(ErrorCode.KAKAO_AUTHENTICATION_FAILED); })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> { throw new CustomException(ErrorCode.KAKAO_API_ERROR); })
                .body(KakaoTokenResponse.class);
    }

    /**
     * 카카오 유저 정보(프로필) 조회
     * 위의 메서드에서 성공적으로 카카오 Access Token을 얻어냈다면,
     * 이제 그 토큰을 들고 "이 토큰의 주인이 누군지(이름, 프로필 사진, 카카오 고유 ID 등) 알려줘!"라고 카카오에 다시 요청하는 메서드
     */
    public KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {
        return restClient.get()
                // 카카오 서버의 실제 주소
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> { throw new CustomException(ErrorCode.KAKAO_AUTHENTICATION_FAILED); })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> { throw new CustomException(ErrorCode.KAKAO_API_ERROR); })
                // 성공적으로 받아온 유저의 프로필 JSON 데이터를 KakaoUserInfoResponse DTO로 변환하여 서비스 계층으로 넘겨줌
                .body(KakaoUserInfoResponse.class);
    }
}