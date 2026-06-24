package com.gachireel.api.configuration.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtMember {
    private long loginUserId;
    private String loginUserRole;
}