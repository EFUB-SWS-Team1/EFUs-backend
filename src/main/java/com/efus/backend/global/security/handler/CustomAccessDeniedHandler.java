package com.efus.backend.global.security.handler;

import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.global.exception.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        log.warn("권한 부족 (403): {}", request.getRequestURI());

        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

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