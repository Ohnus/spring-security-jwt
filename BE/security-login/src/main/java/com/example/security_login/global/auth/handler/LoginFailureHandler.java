package com.example.security_login.global.auth.handler;

import com.example.security_login.global.exception.ErrorCode;
import com.example.security_login.global.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        ErrorCode errorCode;

        if(exception instanceof AuthenticationServiceException) {
            // POST 이외의 요청, JSON 깨짐 등 잘못된 요청 400 처리
            errorCode = ErrorCode.INVALID_LOGIN_REQUEST;
        } else {
            // 아이디, 비밀번호 불일치 등은 401 처리
            errorCode = ErrorCode.LOGIN_FAILED;
        }

        ErrorResponse responseBody = ErrorResponse.of(errorCode);

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
