package com.example.security_login.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserDeleteRequestDto {

    @NotBlank(message = "아이디를 입력하세요.")
    private String username;
}
