package com.gachireel.api.application.auth;

import com.gachireel.api.application.auth.model.LoginReq;
import com.gachireel.api.application.auth.model.LoginRes;
import com.gachireel.api.application.auth.model.PasswordEmailReq;
import com.gachireel.api.application.auth.model.PasswordResetReq;
import com.gachireel.api.application.auth.model.RegisterReq;
import jakarta.mail.MessagingException;
import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import com.gachireel.api.common.response.ResultResponse;
import com.gachireel.api.configuration.security.JwtTokenManager;
import com.gachireel.api.application.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenManager jwtTokenManager;

    // 가입 신청
    @PostMapping("/register")
    public ResultResponse<?> register(@RequestBody @Valid RegisterReq request) {
        authService.register(request);
        return ResultResponse.builder()
                .message("가입 신청이 완료됐습니다. 관리자 승인 후 로그인 가능합니다.")
                .build();
    }

    // 로그인
    @PostMapping("/login")
    public ResultResponse<LoginRes> login(@RequestBody LoginReq request, HttpServletRequest req, HttpServletResponse res) {
        if (jwtTokenManager.hasValidAccessToken(req)) {
            throw new AppException(ErrorCode.ALREADY_LOGGED_IN);
        }
        User user = authService.login(request);
        jwtTokenManager.issue(res, user);
        return new ResultResponse<>("로그인 성공", new LoginRes(user.getNickname(), user.getRole().name()));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResultResponse<?> logout(HttpServletRequest req, HttpServletResponse res) {
        jwtTokenManager.logOut(req, res);
        return ResultResponse.builder()
                .message("로그아웃 되었습니다.")
                .build();
    }

    // 로그인 유지 AT 만료 시 RT로 재발급
    @PostMapping("/reissue")
    public ResultResponse<?> reissue(HttpServletResponse res, HttpServletRequest req) {
        // 쿠키의 RT를 Redis에 저장된 RT와 비교 검증 후 새 AT 발급
        jwtTokenManager.reissue(req, res);
        return ResultResponse.builder()
                .message("Access Token 재발행")
                .build();
    }

    // 비밀번호 재설정 - 인증코드 발송
    @PostMapping("/password/email")
    public ResultResponse<?> sendVerificationCode(@RequestBody @Valid PasswordEmailReq request) throws MessagingException {
        authService.sendVerificationCode(request);
        return ResultResponse.builder()
                .message("인증코드를 발송했습니다.")
                .build();
    }

    // 비밀번호 재설정 - 코드 확인 후 변경
    @PostMapping("/password/reset")
    public ResultResponse<?> resetPassword(@RequestBody @Valid PasswordResetReq request) {
        authService.resetPassword(request);
        return ResultResponse.builder()
                .message("비밀번호가 변경됐습니다.")
                .build();
    }
}