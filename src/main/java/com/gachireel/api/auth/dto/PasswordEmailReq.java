package com.gachireel.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordEmailReq(@NotBlank @Email String email) {}