package com.example.security_login.global.config;

import com.example.security_login.domain.user.entity.UserRoleType;
import com.example.security_login.global.auth.filter.JwtFilter;
import com.example.security_login.global.auth.filter.LoginFilter;
import com.example.security_login.global.exception.ErrorCode;
import com.example.security_login.global.exception.ErrorResponse;
import com.example.security_login.global.response.ResultCode;
import com.example.security_login.global.response.ResultResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationSuccessHandler loginSuccessHandler;
    private final AuthenticationFailureHandler loginFailureHandler;
    private final AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final AuthenticationFailureHandler oAuth2FailureHandler;
    private final LogoutHandler customLogoutHandler;
    private final JwtFilter jwtFilter;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
                          @Qualifier("LoginSuccessHandler") AuthenticationSuccessHandler loginSuccessHandler,
                          @Qualifier("LoginFailureHandler") AuthenticationFailureHandler loginFailureHandler,
                          @Qualifier("OAuth2SuccessHandler") AuthenticationSuccessHandler oAuth2SuccessHandler,
                          @Qualifier("OAuth2FailureHandler") AuthenticationFailureHandler oAuth2FailureHandler,
                          @Qualifier("CustomLogoutHandler") LogoutHandler customLogoutHandler,
                          JwtFilter jwtFilter) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.loginSuccessHandler = loginSuccessHandler;
        this.loginFailureHandler = loginFailureHandler;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
        this.customLogoutHandler = customLogoutHandler;
        this.jwtFilter = jwtFilter;
    }

    // 비밀번호 BCrypt 암호화 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 자체 서비스 로그인 필터를 위한 AuthenticationManage Bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withRolePrefix("ROLE_")
                .role(UserRoleType.ROLE_ADMIN.name()).implies(UserRoleType.ROLE_USER.name())
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {

        // csrf 보안 필터 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // cors 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 커스텀 로그아웃 핸들러 추가
        http.logout(logout -> logout
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> {
                    ResultCode resultCode = ResultCode.USER_LOGOUT_SUCCESS;

                    response.setStatus(resultCode.getStatus().value());
                    response.setContentType("application/json;charset=UTF-8");

                    ResultResponse<?> resultResponse = ResultResponse.success(resultCode);
                    objectMapper.writeValue(response.getWriter(), resultResponse);
                }));

        // 기본 form 기반 인증 필터 비활성화
        http.formLogin(AbstractHttpConfigurer::disable);

        // 기본 basic 인증 필터 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable);

        // OAuth2 설정
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler));

        // 인가 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/reissue").permitAll()
                .requestMatchers(HttpMethod.GET, "/users/check-username").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/signup").permitAll()
                .requestMatchers(HttpMethod.GET, "/users/me").hasAuthority(UserRoleType.ROLE_USER.name())
                .requestMatchers(HttpMethod.PUT, "/users/me").hasAuthority(UserRoleType.ROLE_USER.name())
                .requestMatchers(HttpMethod.DELETE, "/users/me").hasAuthority(UserRoleType.ROLE_USER.name())
                .anyRequest().authenticated());

        // 로그인 이후 API 요청 시 토큰이 없거나 권한이 부족할 때 401, 403
        http.exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, authException) -> {
                    System.out.println("요청 경로: " + request.getRequestURI());
                    ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

                    response.setStatus(errorCode.getStatus().value());
                    response.setContentType("application/json;charset=UTF-8");

                    ErrorResponse errorResponse = ErrorResponse.of(errorCode);
                    objectMapper.writeValue(response.getWriter(), errorResponse);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    ErrorCode errorCode = ErrorCode.FORBIDDEN;

                    response.setStatus(errorCode.getStatus().value());
                    response.setContentType("application/json;charset=UTF-8");

                    ErrorResponse errorResponse = ErrorResponse.of(errorCode);
                    objectMapper.writeValue(response.getWriter(), errorResponse);
                })
        );

        // 커스텀 필터 추가(JwtFilter / LoginFilter: Success, Failure)
        http.addFilterBefore(jwtFilter, LogoutFilter.class);
        http.addFilterBefore(new LoginFilter(authenticationManager(authenticationConfiguration),
                loginSuccessHandler, loginFailureHandler), UsernamePasswordAuthenticationFilter.class);

        // 세션 필터 STATELESS
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // cors
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        // 허용 메서드
        // CORS는 실제 요청 전에 Preflight 요청(OPTIONS) 날리므로 포함 필수
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 모든 헤더 허용
        configuration.setAllowedHeaders(List.of("*"));
        // 인증정보 포함 (JWT / 쿠키)
        configuration.setAllowCredentials(true);
        // 노출할 헤더 (JWT Authorization)
        // 브라우저는 기본적으로 Authorization 헤더를 JS에서 못 읽으므로 설정
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        configuration.setMaxAge(3600L);

        // 해당 url에 대해 config CORS 정책 사용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
