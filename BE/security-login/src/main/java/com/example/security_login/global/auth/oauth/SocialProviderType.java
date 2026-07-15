package com.example.security_login.global.auth.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProviderType {

    NAVER("네이버"),
    GOOGLE("구글");

    private final String description;
}
