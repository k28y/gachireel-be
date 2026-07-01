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
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다. 다시 로그인해주세요."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 인증코드입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    EMAIL_NOT_REGISTERED(HttpStatus.NOT_FOUND, "가입되지 않은 이메일입니다."),
    NICKNAME_ALREADY_TAKEN(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    USER_NOT_PENDING(HttpStatus.CONFLICT, "승인 대기 중인 유저가 아닙니다."),
    ADMIN_CANNOT_DELETED(HttpStatus.FORBIDDEN, "관리자 계정은 탈퇴할 수 없습니다."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다."),
    SAME_AS_CURRENT_PASSWORD(HttpStatus.CONFLICT, "현재 비밀번호와 동일합니다."),

    // Invitation
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다."),
    INVALID_INVITATION(HttpStatus.BAD_REQUEST, "유효하지 않은 초대 링크입니다."),
    EXPIRED_INVITATION(HttpStatus.BAD_REQUEST, "만료된 초대 링크입니다.")
    ;

    private final HttpStatus status;
    private final String message;
}