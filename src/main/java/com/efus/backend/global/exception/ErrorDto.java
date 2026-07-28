package com.efus.backend.global.exception;

public record ErrorDto(
        String timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {}
