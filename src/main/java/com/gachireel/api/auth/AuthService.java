package com.gachireel.api.auth;

import com.gachireel.api.auth.dto.LoginReq;
import com.gachireel.api.common.enumcode.UserStatus;
import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import com.gachireel.api.user.entity.User;
import com.gachireel.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User login(LoginReq request) {
        // 회원 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        // 승인 대기 상태일 시
        if (user.getStatus() == UserStatus.PENDING) {
            throw new AppException(ErrorCode.APPROVAL_PENDING);
        }
        // 승인 거절 상태일 시
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new AppException(ErrorCode.ACCOUNT_REJECTED);
        }

        return user;
    }
}