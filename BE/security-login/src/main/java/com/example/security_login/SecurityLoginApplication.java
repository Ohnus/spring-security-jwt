package com.example.security_login;

import com.example.security_login.global.auth.jwt.util.JwtTokenType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
public class SecurityLoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityLoginApplication.class, args);
    }

}
