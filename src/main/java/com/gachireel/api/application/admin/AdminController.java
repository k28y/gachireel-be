package com.gachireel.api.application.admin;

import com.gachireel.api.application.admin.model.GetAdminUserRes;
import com.gachireel.api.common.enumcode.UserStatus;
import com.gachireel.api.common.response.ResultResponse;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 전체 유저 목록 조회
    @GetMapping("/users")
    public ResultResponse<List<GetAdminUserRes>> getUserList(
            @RequestParam(required = false) UserStatus status) {
        return new ResultResponse<>("유저 목록 조회 성공", adminService.getUserList(status));
    }

    // 가입 승인 (승인 메일 발송)
    @PostMapping("/users/{id}/approve")
    public ResultResponse<?> approveUser(@PathVariable Long id) throws MessagingException {
        adminService.approveUser(id);
        return ResultResponse.builder()
                .message("가입을 승인했습니다.")
                .build();
    }

    // 가입 거절 (거절 메일 발송)
    @PostMapping("/users/{id}/reject")
    public ResultResponse<?> rejectUser(@PathVariable Long id) throws MessagingException {
        adminService.rejectUser(id);
        return ResultResponse.builder()
                .message("가입을 거절했습니다.")
                .build();
    }

    // 거절 취소 (REJECTED → PENDING)
    @PostMapping("/users/{id}/revert")
    public ResultResponse<?> revertRejection(@PathVariable Long id) {
        adminService.revertRejection(id);
        return ResultResponse.builder()
                .message("거절을 취소했습니다.")
                .build();
    }
}