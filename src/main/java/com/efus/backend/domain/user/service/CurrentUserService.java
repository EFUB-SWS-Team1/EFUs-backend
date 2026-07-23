package com.efus.backend.domain.user.service;

import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.repository.UserRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UserRepository userRepository;
// TODO: 도메인 간 접근 규칙(로그인 사용자)

//    public User getCurrentUser() {
//        // 1. SecurityContext에서 현재 인증된 사용자의 ID 추출 (JwtFilter에서 세팅했다고 가정)
//        String kakaoId = SecurityContextHolder.getContext().getAuthentication().getName();
//
//        // 2. DB에서 유저 객체를 찾아 반환
//        return userRepository.findByKakaoId(Long.valueOf(kakaoId))
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//    }
}
