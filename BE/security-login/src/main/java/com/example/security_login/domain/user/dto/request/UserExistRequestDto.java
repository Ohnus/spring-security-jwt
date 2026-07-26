package com.example.security_login.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserExistRequestDto {

    @NotBlank(message = "아이디를 입력하세요.")
    @Size(min = 4, message = "아이디는 최소 4자 이상입니다.")
    private final String username;
}
