package com.efus.backend.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorDto> handleCustomException(CustomException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("CustomException 발생! [요청 주소: {}] 에러 코드: {}, 메시지: {}",
                request.getRequestURI(), errorCode.getCode(), errorCode.getMessage());

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



        return ResponseEntity.status(errorCode.getStatus()).body(errorDto);
    }
}
