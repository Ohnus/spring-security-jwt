package com.example.security_login.global.auth.custom;

import com.example.security_login.domain.user.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final boolean isLock;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(UserEntity userEntity) {
        this.id = userEntity.getId();
        this.username = userEntity.getUsername();
        this.password = userEntity.getPassword();
        this.isLock = userEntity.getIsLock();
        this.authorities = List.of(new SimpleGrantedAuthority(userEntity.getUserRoleType().name()));
    }

    public Long getUserId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 회원가입 시 정상 계정은 isLock = false 이지만,
        // UserDetails의 isAccountNonLocked()는 잠긴 계정이 false이므로 !isLock
        return !isLock;
    }
}
