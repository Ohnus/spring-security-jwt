package com.example.security_login.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRoleType {

    USER("user"),
    ADMIN("admin");

    private final String description;
}
