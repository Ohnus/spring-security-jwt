package com.example.security_login.global.auth.oauth;

public interface OAuth2Response {

    // 제공자 ( NAVER, GOOGLE )
    SocialProviderType getProvider();

    // 회원 고유 식별 ID
    String getId();

    // 서비스에 저장할 회원 ID 조합(Provider + 고유 식별 ID / ex. NAVER_123456)
    String getUsername();

    // 회원 이메일
    String getEmail();

    // 회원 닉네임
    String getNickname();
}
