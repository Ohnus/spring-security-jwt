package com.example.security_login.global.auth.handler;

import com.example.security_login.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
@Qualifier("OAuth2FailureHandler")
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.warn("소셜 로그인 실패: {}", exception.getMessage(), exception);

        // 사용자의 로그인 취소, OAuth2 인증 코드 오류, Token 교환 실패, Provider 사용자 정보 조회 실패 등 401
        ErrorCode errorCode = ErrorCode.OAUTH2_LOGIN_FAILED;


        String redirectUrl = UriComponentsBuilder
                .fromUriString("http://localhost:5173/login")
                .queryParam("status", errorCode.getStatus().value())
                .queryParam("message", errorCode.name())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);

//        ErrorResponse responseBody = ErrorResponse.of(errorCode);
//        response.setStatus(errorCode.getStatus().value());
//        response.setContentType("application/json;charset=UTF-8");
//
//        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
