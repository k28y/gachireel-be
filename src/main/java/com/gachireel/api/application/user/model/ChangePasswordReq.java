package com.gachireel.api.application.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordReq(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8) String newPassword
) {}