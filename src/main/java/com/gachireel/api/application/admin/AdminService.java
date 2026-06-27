package com.gachireel.api.application.admin;

import com.gachireel.api.common.enumcode.UserStatus;
import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import com.gachireel.api.application.user.entity.User;
import com.gachireel.api.application.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional
    public void approveUser(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 승인 대기 상태인지 체크
        if (user.getStatus() != UserStatus.PENDING) {
            throw new AppException(ErrorCode.USER_NOT_PENDING);
        }

        // 승인 처리 (ACTIVE 상태로 변경 + 승인 일시 기록)
        user.approve();
    }

    @Transactional
    public void rejectUser(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 승인 대기 상태인지 체크
        if (user.getStatus() != UserStatus.PENDING) {
            throw new AppException(ErrorCode.USER_NOT_PENDING);
        }

        // 거절 처리 (REJECTED 상태로 변경)
        user.reject();
    }
}