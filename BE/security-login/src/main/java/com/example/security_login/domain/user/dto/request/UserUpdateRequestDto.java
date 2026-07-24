package com.example.security_login.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserUpdateRequestDto {

    @NotBlank(message = "아이디를 입력하세요.")
    private String username;

    @NotBlank(message = "닉네임을 입력하세요.")
    private String nickname;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Builder
    public UserUpdateRequestDto(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
}
