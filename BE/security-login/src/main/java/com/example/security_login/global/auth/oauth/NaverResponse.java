package com.example.security_login.global.auth.oauth;

import java.util.Map;

public class NaverResponse implements OAuth2Response {

    private final Map<String, Object> attributes;

    public NaverResponse(Map<String, Object> attributes) {
        this.attributes = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public SocialProviderType getProvider() {
        return SocialProviderType.NAVER;
    }

    @Override
    public String getId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getUsername() {
        return SocialProviderType.NAVER.name() + "_" + attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getNickname() {
        return attributes.get("nickname").toString();
    }
}
