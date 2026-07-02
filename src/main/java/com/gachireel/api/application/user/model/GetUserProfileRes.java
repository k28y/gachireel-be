package com.gachireel.api.application.user.model;

import com.gachireel.api.application.user.entity.User;

public record GetUserProfileRes(
        Long id,
        String nickname,
        String bio,
        String pic,
        String ratingCriteria1,
        String ratingCriteria2,
        String ratingCriteria3,
        String ratingCriteria4,
        String ratingCriteria5
) {
    public static GetUserProfileRes from(User user) {
        return new GetUserProfileRes(
                user.getId(),
                user.getNickname(),
                user.getBio(),
                user.getPic(),
                user.getRatingCriteria1(),
                user.getRatingCriteria2(),
                user.getRatingCriteria3(),
                user.getRatingCriteria4(),
                user.getRatingCriteria5()
        );
    }
}