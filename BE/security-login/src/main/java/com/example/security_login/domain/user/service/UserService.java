package com.example.security_login.domain.user.service;

import com.example.security_login.domain.user.dto.UserExistRequestDto;
import com.example.security_login.domain.user.dto.UserSignupRequestDto;
import com.example.security_login.domain.user.dto.UserUpdateRequestDto;
import com.example.security_login.domain.user.entity.UserEntity;
import com.example.security_login.domain.user.entity.UserRoleType;
import com.example.security_login.domain.user.repository.UserRepository;
import com.example.security_login.global.exception.BusinessException;
import com.example.security_login.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

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

    // 자체 서비스 로그인
    @Override
    public UserDetails loadUserByUsername(String username) {

        // AuthenticationFailureHandler에서 401 응답
        UserEntity entity = userRepository.findByUsernameAndIsLockAndIsSocial(username, false, false)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return User.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .roles(entity.getUserRoleType().name())
                .accountLocked(entity.getIsLock())
                .build();
    }

    // 소셜 로그인

    // 자체 서비스 회원 정보 수정
    @Transactional
    public void updateUser(UserUpdateRequestDto updateDto) {

        // 본인만 수정 가능
        String sessionUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!sessionUsername.equals(updateDto.getUsername())) {
           throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 조회
        UserEntity entity = userRepository.findByUsernameAndIsLockAndIsSocial(updateDto.getUsername(), false, false)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 수정
        entity.updateNicknameAndEmail(updateDto);

        // 저장(트랜잭션 안에서 더티 체킹으로 자동 저장되지만 노파심에 save)
        userRepository.save(entity);
    }

    // 자체, 소셜 유저 정보 조회

    // 자체, 소셜 회원 탈퇴

}
