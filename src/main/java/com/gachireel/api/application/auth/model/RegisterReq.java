package com.gachireel.api.application.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterReq(
        @NotBlank String token,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @Size(max = 30) String nickname
) {}