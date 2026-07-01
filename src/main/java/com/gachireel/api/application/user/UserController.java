package com.gachireel.api.application.user;

import com.gachireel.api.application.user.model.ChangePasswordReq;
import com.gachireel.api.application.user.model.GetMyProfileRes;
import com.gachireel.api.application.user.model.GetUserProfileRes;
import com.gachireel.api.application.user.model.UpdateProfileReq;
import com.gachireel.api.common.response.ResultResponse;
import com.gachireel.api.configuration.model.UserPrincipal;
import com.gachireel.api.configuration.security.JwtTokenManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenManager jwtTokenManager;

    // 내 정보 조회
    @GetMapping("/me")
    public ResultResponse<GetMyProfileRes> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return new ResultResponse<>("내 정보 조회 성공", userService.getMyProfile(principal.getLoginUserId()));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResultResponse<?> deleteAccount(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest req, HttpServletResponse res) {
        userService.deleteAccount(principal.getLoginUserId());
        jwtTokenManager.logOut(req, res);
        return ResultResponse.builder()
                .message("회원 탈퇴가 완료됐습니다.")
                .build();
    }

    // 다른 유저 공개 프로필 조회
    @GetMapping("/{id}")
    public ResultResponse<GetUserProfileRes> getUserProfile(@PathVariable Long id) {
        return new ResultResponse<>("유저 조회 성공", userService.getUserProfile(id));
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ResultResponse<?> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid UpdateProfileReq request) {
        userService.updateProfile(principal.getLoginUserId(), request);
        return ResultResponse.builder()
                .message("내 정보 수정 완료")
                .build();
    }

    // 로그인 상태 비밀번호 변경
    @PatchMapping("/me/password")
    public ResultResponse<?> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid ChangePasswordReq request) {
        userService.changePassword(principal.getLoginUserId(), request);
        return ResultResponse.builder()
                .message("비밀번호가 변경됐습니다.")
                .build();
    }
}