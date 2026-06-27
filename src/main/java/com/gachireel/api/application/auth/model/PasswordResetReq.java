package com.gachireel.api.application.auth.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetReq(
        @NotBlank @Email String email,
        @NotBlank String code,
        @NotBlank @Size(min = 8) String newPassword
) {}