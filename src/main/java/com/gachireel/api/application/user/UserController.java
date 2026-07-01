package com.gachireel.api.application.user;

import com.gachireel.api.application.user.model.ChangePasswordReq;
import com.gachireel.api.application.user.model.UpdateProfileReq;
import com.gachireel.api.application.user.model.GetMyProfileRes;
import com.gachireel.api.common.response.ResultResponse;
import com.gachireel.api.configuration.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResultResponse<GetMyProfileRes> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return new ResultResponse<>("내 정보 조회 성공", userService.getMe(principal.getLoginUserId()));
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