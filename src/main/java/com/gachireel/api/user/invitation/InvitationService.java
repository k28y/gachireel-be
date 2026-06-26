package com.gachireel.api.user.invitation;

import com.gachireel.api.common.enumcode.InvitationStatus;
import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import com.gachireel.api.email.EmailSender;
import com.gachireel.api.user.entity.Invitation;
import com.gachireel.api.user.entity.User;
import com.gachireel.api.user.invitation.dto.InviteReq;
import com.gachireel.api.user.repository.InvitationRepository;
import com.gachireel.api.user.repository.UserRepository;
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
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        invitationRepository.deleteByEmail(req.email());

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String token = UUID.randomUUID().toString();

        invitationRepository.save(Invitation.builder()
                .inviter(inviter)
                .email(req.email())
                .token(token)
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        String inviteUrl = frontendUrl + "/join?token=" + token;
        emailSender.sendInvitationMail(req.email(), inviteUrl);
    }
}