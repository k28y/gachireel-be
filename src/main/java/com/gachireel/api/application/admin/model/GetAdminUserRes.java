package com.gachireel.api.application.admin.model;

import com.gachireel.api.application.user.entity.User;
import com.gachireel.api.common.enumcode.UserRole;
import com.gachireel.api.common.enumcode.UserStatus;

import java.time.LocalDateTime;

public record GetAdminUserRes(
        Long id,
        String email,
        String nickname,
        UserStatus status,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime approvedAt
) {
    public static GetAdminUserRes from(User user) {
        return new GetAdminUserRes(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getStatus(),
                user.getRole(),
                user.getCreatedAt(),
                user.getApprovedAt()
        );
    }
}