package com.example.security_login.domain.user.service;

import com.example.security_login.domain.user.dto.request.UserDeleteRequestDto;
import com.example.security_login.domain.user.dto.request.UserExistRequestDto;
import com.example.security_login.domain.user.dto.request.UserSignupRequestDto;
import com.example.security_login.domain.user.dto.request.UserUpdateRequestDto;
import com.example.security_login.domain.user.dto.response.UserResponseDto;
import com.example.security_login.domain.user.entity.UserEntity;
import com.example.security_login.domain.user.entity.UserRoleType;
import com.example.security_login.domain.user.repository.UserRepository;
import com.example.security_login.global.auth.custom.CustomOAuth2User;
import com.example.security_login.global.auth.custom.CustomUserDetails;
import com.example.security_login.global.auth.custom.CustomUserPrincipal;
import com.example.security_login.global.auth.jwt.repository.RedisRefreshTokenRepository;
import com.example.security_login.global.auth.oauth.GoogleResponse;
import com.example.security_login.global.auth.oauth.NaverResponse;
import com.example.security_login.global.auth.oauth.OAuth2Response;
import com.example.security_login.global.auth.oauth.SocialProviderType;
import com.example.security_login.global.exception.BusinessException;
import com.example.security_login.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService extends DefaultOAuth2UserService implements UserDetailsService {

    private final PasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final RedisRefreshTokenRepository refreshTokenRepository;

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
                .userRoleType(UserRoleType.ROLE_USER)
                .build();

        userRepository.save(userEntity);
    }

    // 자체 서비스 로그인
    @Override
    public UserDetails loadUserByUsername(String username) {

        // AuthenticationFailureHandler에서 401 응답
        UserEntity entity = userRepository.findByUsernameAndIsLockAndIsSocial(username, false, false)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return new CustomUserDetails(entity);
    }

    // 소셜 로그인(신규: 회원가입+로그인 / 기존: 정보 수정)
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        // 부모 메서드 호출
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 데이터 파싱
        Map<String, Object> attributes;

        // 제공자별 데이터 획득
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        attributes = oAuth2User.getAttributes();

        OAuth2Response oAuth2Response;
        try {
            oAuth2Response = switch (SocialProviderType.valueOf(registrationId)) {
                case NAVER -> new NaverResponse(attributes);
                case GOOGLE -> new GoogleResponse(attributes);
            };
        } catch (IllegalArgumentException e) {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        }

        // 존재 여부 확인
        Optional<UserEntity> existEntity = userRepository.findByUsernameAndIsSocial(oAuth2Response.getUsername(), true);
        UserEntity userEntity;

        // 존재하면 이메일&닉네임 업데이트, 없으면 신규 가입
        if(existEntity.isPresent()) {
            UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                    .nickname(oAuth2Response.getNickname())
                    .email(oAuth2Response.getEmail())
                    .build();

            userEntity = existEntity.get();
            userEntity.updateNicknameAndEmail(updateDto);
//            userRepository.save(entity.get());
        } else {
            userEntity = UserEntity.builder()
                    .username(oAuth2Response.getUsername())
                    .password("")
                    .nickname(oAuth2Response.getNickname())
                    .email(oAuth2Response.getEmail())
                    .isLock(false)
                    .isSocial(true)
                    .socialProviderType(oAuth2Response.getProvider())
                    .userRoleType(UserRoleType.ROLE_USER)
                    .build();

            userEntity = userRepository.save(userEntity);
        }

        return new CustomOAuth2User(
                userEntity.getId(),
                attributes,
                List.of(new SimpleGrantedAuthority(userEntity.getUserRoleType().name())),
                userEntity.getUsername());
    }

    // 자체 서비스 회원 정보 수정
    @Transactional
    public void updateUser(UserUpdateRequestDto updateDto) {

        // 본인만 수정 가능
        CustomUserPrincipal user = (CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getUsername().equals(updateDto.getUsername())) {
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
    public UserResponseDto readUser() {

        // 유저 정보 추출
        CustomUserPrincipal user = (CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String sessionUsername = user.getUsername();

        // 조회
        UserEntity userEntity = userRepository.findByUsernameAndIsLock(sessionUsername, false)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 응답
        return new UserResponseDto(userEntity.getUsername(), userEntity.getIsSocial(), userEntity.getNickname(), userEntity.getEmail());
    }

    // 자체, 소셜 회원 탈퇴
    @Transactional
    public void deleteUser(UserDeleteRequestDto deleteDto) {

        // 본인 및 관리자만 삭제 가능
        CustomUserPrincipal user = (CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long sessionUserId = user.getUserId();
        String sessionUsername = user.getUsername();
        String sessionRole = user.getAuthorities().iterator().next().toString();

        boolean isOwner = sessionUsername.equals(deleteDto.getUsername());
        boolean isAdmin = sessionRole.equals(UserRoleType.ROLE_ADMIN.name());

        if(!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        UserEntity userEntity = userRepository.findByUsernameAndIsLock(sessionUsername, false)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 유저 삭제
        userRepository.deleteByUsername(deleteDto.getUsername());

        // Refresh Token 삭제
        refreshTokenRepository.deleteByUserId(sessionUserId);
    }

}
