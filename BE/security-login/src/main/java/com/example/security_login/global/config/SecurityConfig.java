package com.example.security_login.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // csrf 보안 필터 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // cors 설정

        // 기본 form 기반 인증 필터 비활성화
        http.formLogin(AbstractHttpConfigurer::disable);

        // 기본 basic 인증 필터 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable);

        // OAuth2 설정
//        http.oauth2Login(oauth2 -> oauth2.successHandler());

        // 인가 설정
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll());

        // 커스텀 필터 추가

        // 세션 필터 STATELESS
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
