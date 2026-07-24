package com.example.security_login.api.jwt;

import com.example.security_login.global.auth.jwt.dto.AccessTokenResponseDto;
import com.example.security_login.global.auth.jwt.service.JwtService;
import com.example.security_login.global.exception.BusinessException;
import com.example.security_login.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class JwtController {

    private final JwtService jwtService;

    // Refresh Token을 통한 Access 및 Refresh Token 재발급
    @PostMapping(value = "/auth/reissue")
    public AccessTokenResponseDto tokenReissue(
            HttpServletResponse response,
            @CookieValue(value = "refreshToken", required = false) Optional<String> refreshToken)
    {
        return jwtService.reissueTokens(response, refreshToken
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)));
    }
}
