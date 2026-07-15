package com.example.security_login.domain.user.repository;

import com.example.security_login.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // 자체 회원가입 유저 존재 확인
    boolean existsByUsername(String username);

}
