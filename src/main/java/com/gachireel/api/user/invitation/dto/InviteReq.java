package com.gachireel.api.user.invitation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteReq(@NotBlank @Email String email) {}