package com.gachireel.api.invitation;

import com.gachireel.api.common.response.ResultResponse;
import com.gachireel.api.configuration.model.UserPrincipal;
import com.gachireel.api.invitation.dto.InviteReq;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    public ResultResponse<?> createInvitation(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid InviteReq req) throws MessagingException {
        invitationService.createInvitation(principal.getLoginUserId(), req);
        return new ResultResponse<>("초대 링크를 발송했습니다.", null);
    }
}