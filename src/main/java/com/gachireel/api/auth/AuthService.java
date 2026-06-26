package com.gachireel.api.auth;

import com.gachireel.api.auth.dto.LoginReq;
import com.gachireel.api.auth.dto.RegisterReq;
import com.gachireel.api.common.enumcode.InvitationStatus;
import com.gachireel.api.common.enumcode.UserRole;
import com.gachireel.api.common.enumcode.UserStatus;
import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import com.gachireel.api.user.entity.Invitation;
import com.gachireel.api.user.entity.User;
import com.gachireel.api.user.repository.InvitationRepository;
import com.gachireel.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User login(LoginReq request) {
        // 회원 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        // 승인 대기 상태일 시
        if (user.getStatus() == UserStatus.PENDING) {
            throw new AppException(ErrorCode.APPROVAL_PENDING);
        }
        // 승인 거절 상태일 시
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new AppException(ErrorCode.ACCOUNT_REJECTED);
        }

        return user;
    }

    @Transactional
    public void register(RegisterReq request) {
        // 초대 토큰으로 초대 내역 조회
        Invitation invitation = invitationRepository.findByToken(request.token())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INVITATION));

        // 이미 사용됐거나 취소된 토큰인지 체크
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_INVITATION);
        }

        // 초대 토큰 만료 여부 체크
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.EXPIRED_INVITATION);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.nickname())) {
            throw new AppException(ErrorCode.NICKNAME_ALREADY_TAKEN);
        }

        // 유저 생성 (관리자 승인 전까지 PENDING 상태)
        userRepository.save(User.builder()
                .email(invitation.getEmail())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(UserRole.USER)
                .status(UserStatus.PENDING)
                .referredBy(invitation.getInviter())
                .build());

        // 초대 토큰 사용 처리
        invitation.markAsUsed();
    }
}