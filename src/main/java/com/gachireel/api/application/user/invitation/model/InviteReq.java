package com.gachireel.api.application.user.invitation.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteReq(@NotBlank @Email String email) {}