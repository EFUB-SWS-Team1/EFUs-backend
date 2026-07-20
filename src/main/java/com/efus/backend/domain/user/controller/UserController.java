package com.efus.backend.domain.user.controller;

import com.efus.backend.domain.user.dto.response.UserInfoResponse;
import com.efus.backend.domain.user.service.UserService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.global.security.jwt.JwtProvider;
import com.efus.backend.global.security.jwt.TokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/api/users/me")
    public ResponseEntity<UserInfoResponse> getMyInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // 서비스로 헤더 값을 통째로 넘기고 결과만 받아서 반환
        return ResponseEntity.ok(userService.getMyInfo(authorization));
    }
}