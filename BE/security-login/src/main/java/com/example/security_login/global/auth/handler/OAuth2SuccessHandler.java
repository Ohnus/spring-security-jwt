package com.example.security_login.global.auth.handler;

import com.example.security_login.global.auth.custom.CustomOAuth2User;
import com.example.security_login.global.auth.jwt.repository.RedisRefreshTokenRepository;
import com.example.security_login.global.auth.jwt.service.JwtService;
import com.example.security_login.global.auth.jwt.util.JwtTokenType;
import com.example.security_login.global.auth.jwt.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Qualifier("OAuth2SuccessHandler")
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final JwtService jwtService;
    private final RedisRefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,Authentication authentication)
            throws IOException, ServletException {

        // 유저 정보 추출
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Long userId = oAuth2User.getUserId();
        String username = oAuth2User.getName();
        String role = oAuth2User.getAuthorities().iterator().next().getAuthority();

        // Refresh Token 생성
        String refreshToken = jwtUtil.createJwt(userId, username, role, JwtTokenType.REFRESH);

        // Redis 저장
        refreshTokenRepository.save(userId, refreshToken);
        System.out.println(refreshTokenRepository.findByUserId(userId));

        // 응답
        Cookie refreshTokenCookie = jwtService.createCookie("refreshToken", refreshToken, 10);
        response.addCookie(refreshTokenCookie);
        response.sendRedirect("http://localhost:5173/");
    }
}
