package com.gachireel.api.application.auth;

import com.gachireel.api.application.auth.model.LoginReq;
import com.gachireel.api.application.auth.model.PasswordEmailReq;
import com.gachireel.api.application.auth.model.PasswordResetReq;
import com.gachireel.api.application.auth.model.RegisterReq;
import com.gachireel.api.common.enumcode.InvitationStatus;
import com.gachireel.api.common.enumcode.UserRole;
import com.gachireel.api.common.enumcode.UserStatus;
import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import com.gachireel.api.application.email.EmailSender;
import com.gachireel.api.application.user.entity.Invitation;
import com.gachireel.api.application.user.entity.User;
import com.gachireel.api.application.user.repository.InvitationRepository;
import com.gachireel.api.application.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationStore verificationStore;
    private final EmailSender emailSender;

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

        // 신규 유저 생성 (관리자 승인 전까지 PENDING 상태)
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

    public void sendVerificationCode(PasswordEmailReq request) throws MessagingException {
        // 가입된 이메일인지 확인
        userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_REGISTERED));

        // 6자리 인증코드 생성
        String code = String.format("%06d", new SecureRandom().nextInt(1000000));

        // 인메모리에 저장 (5분 유효)
        verificationStore.save(request.email(), code);

        // 이메일 발송
        emailSender.sendVerificationCodeMail(request.email(), code);
    }

    @Transactional
    public void resetPassword(PasswordResetReq request) {
        // 인증코드 검증
        if (!verificationStore.verify(request.email(), request.code())) {
            throw new AppException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 유저 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_REGISTERED));

        // 비밀번호 변경
        user.changePassword(passwordEncoder.encode(request.newPassword()));

        // 인증코드 삭제
        verificationStore.remove(request.email());
    }
}