package com.gachireel.api.application.user;

import com.gachireel.api.application.user.model.ChangePasswordReq;
import com.gachireel.api.application.user.model.GetMyProfileRes;
import com.gachireel.api.application.user.model.GetUserProfileRes;
import com.gachireel.api.application.user.model.UpdateProfileReq;
import com.gachireel.api.application.user.entity.User;
import com.gachireel.api.application.user.repository.UserRepository;
import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public GetMyProfileRes getMyProfile(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return GetMyProfileRes.from(user);
    }

    @Transactional(readOnly = true)
    public GetUserProfileRes getUserProfile(long targetId) {
        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return GetUserProfileRes.from(user);
    }

    @Transactional
    public void updateProfile(long userId, UpdateProfileReq request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 변경 시 자신을 제외한 중복 체크
        if (request.nickname() != null &&
                userRepository.existsByNicknameAndIdNot(request.nickname(), userId)) {
            throw new AppException(ErrorCode.NICKNAME_ALREADY_TAKEN);
        }

        user.updateProfile(
                request.nickname(),
                request.bio(),
                request.ratingCriteria1(),
                request.ratingCriteria2(),
                request.ratingCriteria3(),
                request.ratingCriteria4(),
                request.ratingCriteria5()
        );
    }

    @Transactional
    public void changePassword(long userId, ChangePasswordReq request) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        // 현재 비밀번호와 새 비밀번호가 동일한지 확인
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
        }

        // 비밀번호 변경
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }
}