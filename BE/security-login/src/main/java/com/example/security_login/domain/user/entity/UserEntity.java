package com.example.security_login.domain.user.entity;

import com.example.security_login.domain.user.dto.request.UserUpdateRequestDto;
import com.example.security_login.global.auth.oauth.SocialProviderType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
// JPA는 내부적으로 엔티티를 생성할 때 기본 생성자 필요(PROTECTED도 접근 가능하므로 외부에서의 호출 막기 위해 PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_entity")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, updatable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "is_lock", nullable = false)
    private Boolean isLock;

    @Column(name = "is_social", nullable = false)
    private Boolean isSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider_type")
    private SocialProviderType socialProviderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role_type", nullable = false)
    private UserRoleType userRoleType;

    @Column(name = "created_date", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Builder
    public UserEntity(String username, String password, String nickname, String email,
                      boolean isLock, boolean isSocial, SocialProviderType socialProviderType, UserRoleType userRoleType) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.isLock = isLock;
        this.isSocial = isSocial;
        this.socialProviderType = socialProviderType;
        this.userRoleType = userRoleType;
    }

    // 닉네임, 이메일 수정
    public void updateNicknameAndEmail(UserUpdateRequestDto updateDto) {
        this.nickname = updateDto.getNickname();
        this.email = updateDto.getEmail();
    }
}
