package com.gachireel.api.user.entity;

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
}