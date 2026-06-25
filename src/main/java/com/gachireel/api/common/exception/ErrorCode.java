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
    ACCOUNT_REJECTED(HttpStatus.FORBIDDEN, "가입이 거절된 계정입니다."),
    ALREADY_LOGGED_IN(HttpStatus.CONFLICT, "이미 로그인된 상태입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다. 다시 로그인해주세요.");

    private final HttpStatus status;
    private final String message;
}