package com.efus.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Default
    INVALID_REQUEST(400, "Bad Request", "INVALID_REQUEST", "잘못된 요청입니다."),

    // 인증 관련
    AUTHORIZATION_CODE_REQUIRED(400, "Bad Request", "AUTHORIZATION_CODE_REQUIRED","카카오 인가 코드는 필수입니다."),
    KAKAO_AUTHENTICATION_FAILED(401, "Unauthorized", "KAKAO_AUTHENTICATION_FAILED", "카카오 인증에 실패했습니다."),
    UNAUTHORIZED(401, "Unauthorized", "UNAUTHORIZED", "로그인이 필요합니다."),
    REFRESH_TOKEN_REQUIRED(401, "Unauthorized", "REFRESH_TOKEN_REQUIRED", "Refresh Token 쿠키가 없는 경우"),
    INVALID_REFRESH_TOKEN(401, "Unauthorized", "INVALID_REFRESH_TOKEN", "유효하지 않은 Refresh Token입니다."),
    EXPIRED_REFRESH_TOKEN(401, "Unauthorized", "EXPIRED_REFRESH_TOKEN", "Refresh Token이 만료되었습니다. 다시 로그인해 주세요."),
    INVALID_ACCESS_TOKEN(401, "Unauthorized", "INVALID_ACCESS_TOKEN", "유효하지 않은 Access Token입니다."),
    EXPIRED_ACCESS_TOKEN(401, "Unauthorized", "EXPIRED_ACCESS_TOKEN","Access Token이 만료되었습니다."),

    // 잘못된 요청
    USER_NOT_FOUND(404, "Not Found", "USER_NOT_FOUND", "사용자 정보를 찾을 수 없습니다."),


    TERM_NOT_FOUND(404, "Not Found", "TERM_NOT_FOUND", "기수를 찾을 수 없습니다."),
    TERM_CLOSED(400, "Bad Request", "TERM_CLOSED", "현재 활성화된 기수가 아닙니다."),

    // 서버 관련
    KAKAO_API_ERROR(502, "Bad Gateway","KAKAO_API_ERROR", "카카오 서버와 통신하는 중 오류가 발생했습니다."),

    //Transaction 관련
    TRANSACTION_NOT_FOUND(404, "Not Found", "TRANSACTION_NOT_FOUND", "거래를 찾을 수 없습니다."),
    TRANSACTION_ALREADY_DELETED(400, "Bad Request", "TRANSACTION_ALREADY_DELETED", "삭제된 거래입니다."),
    TRANSACTION_HISTORY_SERIALIZATION_FAILED(
            500,
            "Internal Server Error",
            "TRANSACTION_HISTORY_SERIALIZATION_FAILED",
            "거래 이력 데이터 변환에 실패했습니다."
    ),

    // Invitation 관련
    INVALID_INVITATION_ROLE(400, "Bad Request", "INVALID_INVITATION_ROLE", "초대 코드 역할은 STAFF 또는 MEMBER여야 합니다."),
    INVITATION_NOT_FOUND(404, "Not Found", "INVITATION_NOT_FOUND", "초대 코드를 찾을 수 없습니다."),
    INVITATION_EXPIRED(410, "Gone", "INVITATION_EXPIRED", "만료된 초대 코드입니다."),
    INVITATION_INACTIVE(410, "Gone", "INVITATION_INACTIVE", "더 이상 사용할 수 없는 초대 코드입니다."),
    INVITATION_CODE_REQUIRED(400, "Bad Request", "INVITATION_CODE_REQUIRED", "초대 코드는 필수입니다."),
    TERM_MEMBER_ALREADY_EXISTS(409, "Conflict", "TERM_MEMBER_ALREADY_EXISTS", "이미 가입한 기수입니다."),
    INVITATION_JOIN_FAILED(500, "Internal Server Error", "INVITATION_JOIN_FAILED", "기수 가입 처리 중 오류가 발생했습니다."),
    INVITATION_CODE_GENERATION_FAILED(500, "Internal Server Error", "INVITATION_CODE_GENERATION_FAILED", "초대 코드 생성에 실패했습니다."),

    // Receipt 관련
    RECEIPT_NOT_FOUND(404, "Not Found", "RECEIPT_NOT_FOUND", "영수증을 찾을 수 없습니다."),
    INVALID_RECEIPT_FILE(400, "Bad Request", "INVALID_RECEIPT_FILE", "올바르지 않은 영수증 파일입니다."),
    RECEIPT_UPLOAD_FAILED(500, "Internal Server Error", "RECEIPT_UPLOAD_FAILED", "영수증 업로드에 실패했습니다."),
    RECEIPT_DELETE_FAILED(500, "Internal Server Error", "RECEIPT_DELETE_FAILED", "영수증 삭제에 실패했습니다."),
    RECEIPT_OCR_FAILED(500, "Internal Server Error", "RECEIPT_OCR_FAILED", "영수증 OCR 금액 인식에 실패했습니다."),

    // Funding 관련
    FUNDING_NOT_FOUND(404, "Not Found", "FUNDING_NOT_FOUND", "행사를 찾을 수 없습니다."),
    FUNDING_ACCESS_DENIED(403, "Forbidden", "FUNDING_ACCESS_DENIED", "행사에 접근할 권한이 없습니다."),
    STAFF_REQUIRED(403, "Forbidden", "STAFF_REQUIRED", "스태프만 요청할 수 있습니다."),
    INVALID_FUNDING_PERIOD(400, "Bad Request", "INVALID_FUNDING_PERIOD", "행사 기간이 올바르지 않습니다."),
    INVALID_FUNDING_BUDGET(400, "Bad Request", "INVALID_FUNDING_BUDGET", "행사 예산은 0보다 커야 합니다."),
    INVALID_PARTICIPANT_COUNT(400, "Bad Request", "INVALID_PARTICIPANT_COUNT", "참여 인원은 0보다 커야 합니다.");

    private final int status;
    private final String error;
    private final String code;
    private final String message;
}
