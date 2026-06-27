package com.gachireel.api.application.user.invitation;

import com.gachireel.api.common.enumcode.InvitationStatus;
import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import com.gachireel.api.application.email.EmailSender;
import com.gachireel.api.application.user.entity.Invitation;
import com.gachireel.api.application.user.entity.User;
import com.gachireel.api.application.user.invitation.model.InviteReq;
import com.gachireel.api.application.user.repository.InvitationRepository;
import com.gachireel.api.application.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public void createInvitation(long inviterId, InviteReq req) throws MessagingException {
        // 이미 가입된 이메일이라면 초대 불가
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        // 동일 이메일로 기존 초대가 있으면 삭제 (재초대 허용)
        invitationRepository.deleteByEmail(req.email());

        // 초대자 조회
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 고유한 초대 토큰 생성
        String token = UUID.randomUUID().toString();

        // 초대 데이터 저장 (7일 후 만료)
        invitationRepository.save(Invitation.builder()
                .inviter(inviter)
                .email(req.email())
                .token(token)
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        // 초대 링크 생성 후 이메일 발송
        String inviteUrl = frontendUrl + "/join?token=" + token;
        emailSender.sendInvitationMail(req.email(), inviteUrl);
    }
}