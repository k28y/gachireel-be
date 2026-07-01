package com.gachireel.api.application.user.model;

import jakarta.validation.constraints.Size;

public record UpdateProfileReq(
        @Size(min = 2, max = 30) String nickname,
        @Size(max = 200) String bio,
        @Size(max = 100) String ratingCriteria1,
        @Size(max = 100) String ratingCriteria2,
        @Size(max = 100) String ratingCriteria3,
        @Size(max = 100) String ratingCriteria4,
        @Size(max = 100) String ratingCriteria5
) {}