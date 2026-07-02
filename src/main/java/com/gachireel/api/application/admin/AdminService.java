package com.gachireel.api.application.admin;

import com.gachireel.api.application.admin.model.GetAdminUserRes;
import com.gachireel.api.application.email.EmailSender;
import com.gachireel.api.common.enumcode.UserStatus;
import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import com.gachireel.api.application.user.entity.User;
import com.gachireel.api.application.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EmailSender emailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional(readOnly = true)
    public List<GetAdminUserRes> getUserList(UserStatus status) {
        // status가 없으면 전체 조회, 있으면 해당 상태만 필터링
        List<User> users = (status != null)
                ? userRepository.findAllByStatusOrderByCreatedAtDesc(status)
                : userRepository.findAllByOrderByCreatedAtDesc();
        return users.stream().map(GetAdminUserRes::from).toList();
    }

    @Transactional
    public void approveUser(Long userId) throws MessagingException {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 승인 대기 상태인지 체크
        if (user.getStatus() != UserStatus.PENDING) {
            throw new AppException(ErrorCode.USER_NOT_PENDING);
        }

        // 승인 처리 (ACTIVE 상태로 변경 + 승인 일시 기록)
        user.approve();
        // 승인 메일 발송
        emailSender.sendApprovalMail(user.getEmail(), frontendUrl);
    }

    @Transactional
    public void rejectUser(Long userId) throws MessagingException {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 승인 대기 상태인지 체크
        if (user.getStatus() != UserStatus.PENDING) {
            throw new AppException(ErrorCode.USER_NOT_PENDING);
        }

        // 거절 처리 (REJECTED 상태로 변경)
        user.reject();
        // 거절 메일 발송
        emailSender.sendRejectionMail(user.getEmail());
    }

    @Transactional
    public void revertRejection(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.REJECTED) {
            throw new AppException(ErrorCode.USER_NOT_REJECTED);
        }

        // 거절 취소 → 다시 승인 대기 상태로
        user.revertRejection();
    }
}