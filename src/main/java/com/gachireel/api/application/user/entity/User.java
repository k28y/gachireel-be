package com.gachireel.api.application.user.entity;

import com.gachireel.api.common.entity.CreatedUpdatedAt;
import com.gachireel.api.common.enumcode.UserRole;
import com.gachireel.api.common.enumcode.UserStatus;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends CreatedUpdatedAt {

    @Id
    @Tsid
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(length = 200)
    private String bio;

    @Column(length = 500)
    private String pic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column
    private java.time.LocalDateTime approvedAt;

    @Column
    private java.time.LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_by")
    private User referredBy;

    @Column(length = 100)
    private String ratingCriteria1;

    @Column(length = 100)
    private String ratingCriteria2;

    @Column(length = 100)
    private String ratingCriteria3;

    @Column(length = 100)
    private String ratingCriteria4;

    @Column(length = 100)
    private String ratingCriteria5;

    public void approve() {
        this.status = UserStatus.ACTIVE;
        this.approvedAt = java.time.LocalDateTime.now();
    }

    public void reject() {
        this.status = UserStatus.REJECTED;
    }

    public void revertRejection() {
        this.status = UserStatus.PENDING;
    }

    public void updateProfile(String nickname, String bio,
                              String ratingCriteria1, String ratingCriteria2, String ratingCriteria3,
                              String ratingCriteria4, String ratingCriteria5) {
        // null이 아닌 경우에만 업데이트. 빈 문자열("")로 보내면 해당 필드를 초기화(null 저장)
        if (nickname != null) this.nickname = nickname;
        if (bio != null) this.bio = bio.isBlank() ? null : bio;
        if (ratingCriteria1 != null) this.ratingCriteria1 = ratingCriteria1.isBlank() ? null : ratingCriteria1;
        if (ratingCriteria2 != null) this.ratingCriteria2 = ratingCriteria2.isBlank() ? null : ratingCriteria2;
        if (ratingCriteria3 != null) this.ratingCriteria3 = ratingCriteria3.isBlank() ? null : ratingCriteria3;
        if (ratingCriteria4 != null) this.ratingCriteria4 = ratingCriteria4.isBlank() ? null : ratingCriteria4;
        if (ratingCriteria5 != null) this.ratingCriteria5 = ratingCriteria5.isBlank() ? null : ratingCriteria5;
    }

    // 탈퇴시 이메일 익명화, 개인정보 초기화
    public void delete() {
        this.status = UserStatus.DELETED;
        this.deletedAt = java.time.LocalDateTime.now();
        this.email = "deleted_" + this.id + "@deleted";
        this.nickname = "탈퇴한 사용자";
        this.bio = null;
        this.pic = null;
        this.ratingCriteria1 = null;
        this.ratingCriteria2 = null;
        this.ratingCriteria3 = null;
        this.ratingCriteria4 = null;
        this.ratingCriteria5 = null;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}