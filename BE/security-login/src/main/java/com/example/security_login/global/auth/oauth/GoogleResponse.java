package com.example.security_login.global.auth.oauth;

import java.util.Map;

public class GoogleResponse implements OAuth2Response {

    private final Map<String, Object> attributes;

    public GoogleResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public SocialProviderType getProvider() {
        return SocialProviderType.GOOGLE;
    }

    @Override
    public String getId() {
        return attributes.get("sub").toString();
    }

    @Override
    public String getUsername() {
        return SocialProviderType.GOOGLE.name() + "_" + attributes.get("sub").toString();
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getNickname() {
        return attributes.get("name").toString();
    }
}
