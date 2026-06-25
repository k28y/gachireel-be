package com.gachireel.api.auth;

import com.gachireel.api.auth.dto.LoginReq;
import com.gachireel.api.auth.dto.LoginRes;
import com.gachireel.api.common.response.ResultResponse;
import com.gachireel.api.configuration.security.JwtTokenManager;
import com.gachireel.api.user.entity.User;
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
}