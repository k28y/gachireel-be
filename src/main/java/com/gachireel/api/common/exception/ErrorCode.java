package com.gachireel.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    APPROVAL_PENDING(HttpStatus.FORBIDDEN, "가입 승인 대기 중입니다."),
    ACCOUNT_REJECTED(HttpStatus.FORBIDDEN, "가입이 거절된 계정입니다.");

    private final HttpStatus status;
    private final String message;
}