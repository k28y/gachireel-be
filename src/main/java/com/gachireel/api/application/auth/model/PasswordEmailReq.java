package com.gachireel.api.application.auth.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordEmailReq(@NotBlank @Email String email) {}