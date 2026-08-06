package com.efus.backend.global.security.handler;

import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.global.exception.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.warn("인증 실패 (401): {}", request.getRequestURI());

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        String timestamp = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        ErrorDto errorDto = new ErrorDto(
                timestamp,
                errorCode.getStatus(),
                errorCode.getError(),
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI()
        );

        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorDto));
    }
}