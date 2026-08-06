package com.efus.backend.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : errorCode.getMessage();

        log.warn("Validation 예외 발생! [요청 주소: {}] 메시지: {}", request.getRequestURI(), errorMessage);

        String timestamp = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        ErrorDto errorDto = new ErrorDto(
                timestamp,
                errorCode.getStatus(),
                errorCode.getError(),
                errorCode.getCode(),
                errorMessage,
                request.getRequestURI()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(errorDto);
    }
}
