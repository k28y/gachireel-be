package com.gachireel.api.auth;

import com.gachireel.api.auth.dto.LoginReq;
import com.gachireel.api.auth.dto.LoginRes;
import com.gachireel.api.common.response.ResultResponse;
import com.gachireel.api.configuration.security.JwtTokenManager;
import com.gachireel.api.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @PostMapping("/login")
    public ResultResponse<LoginRes> login(@RequestBody LoginReq request, HttpServletResponse response) {
        User user = authService.login(request);
        jwtTokenManager.issue(response, user);
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
}