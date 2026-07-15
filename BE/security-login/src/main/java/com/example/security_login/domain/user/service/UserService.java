package com.example.security_login.domain.user.service;

import com.example.security_login.domain.user.dto.UserExistRequestDto;
import com.example.security_login.domain.user.dto.UserSignupRequestDto;
import com.example.security_login.domain.user.entity.UserEntity;
import com.example.security_login.domain.user.entity.UserRoleType;
import com.example.security_login.domain.user.repository.UserRepository;
import com.example.security_login.global.exception.BusinessException;
import com.example.security_login.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final PasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    // 자체 서비스 회원가입을 위한 유저 존재 여부
    public boolean existsByUsername(UserExistRequestDto existDto) {
        return userRepository.existsByUsername(existDto.getUsername());
    }

    // 자체 서비스 회원가입
    @Transactional
    public void signup(UserSignupRequestDto signupDto) {

        if(userRepository.existsByUsername(signupDto.getUsername())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        UserEntity userEntity = UserEntity.builder()
                .username(signupDto.getUsername())
                .password(bCryptPasswordEncoder.encode(signupDto.getPassword()))
                .nickname(signupDto.getNickname())
                .email(signupDto.getEmail())
                .isLock(false)
                .isSocial(false)
                .userRoleType(UserRoleType.USER)
                .build();

        userRepository.save(userEntity);
    }
}
