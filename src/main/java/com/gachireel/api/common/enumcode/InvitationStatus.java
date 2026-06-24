package com.gachireel.api.common.enumcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvitationStatus {
    PENDING, USED, EXPIRED
}