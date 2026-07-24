package com.example.security_login.domain.user.repository;

import com.example.security_login.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // 자체 서비스 회원가입 유저 존재 확인
    boolean existsByUsername(String username);

    // 자체 서비스 유저 조회
    Optional<UserEntity> findByUsernameAndIsLockAndIsSocial(String username, boolean isLock, boolean isSocial);

    // 소셜 유저 조회
    Optional<UserEntity> findByUsernameAndIsSocial(String username, boolean isSocial);

    // 자체, 소셜 유저 조회
    Optional<UserEntity> findByUsernameAndIsLock(String username, boolean isLock);

    // 유저 삭제
    void deleteByUsername(String username);
}
