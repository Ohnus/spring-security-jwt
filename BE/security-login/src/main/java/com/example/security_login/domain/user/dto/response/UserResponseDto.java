package com.example.security_login.domain.user.dto.response;

public record UserResponseDto(String username, boolean isSocial, String nickname, String email) {
}
