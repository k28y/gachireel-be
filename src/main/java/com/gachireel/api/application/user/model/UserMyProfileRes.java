package com.gachireel.api.application.user.model;

import com.gachireel.api.application.user.entity.User;
import com.gachireel.api.common.enumcode.UserRole;

public record UserMyProfileRes(
        Long id,
        String email,
        String nickname,
        String bio,
        String pic,
        UserRole role,
        String ratingCriteria1,
        String ratingCriteria2,
        String ratingCriteria3,
        String ratingCriteria4,
        String ratingCriteria5
) {
    public static UserMyProfileRes from(User user) {
        return new UserMyProfileRes(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getBio(),
                user.getPic(),
                user.getRole(),
                user.getRatingCriteria1(),
                user.getRatingCriteria2(),
                user.getRatingCriteria3(),
                user.getRatingCriteria4(),
                user.getRatingCriteria5()
        );
    }
}