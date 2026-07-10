package com.example.security_login.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    /* ==================
    *  User(회원)
    *  ================== */
    USER_CREATE_SUCCESS(HttpStatus.CREATED, "회원가입이 완료되었습니다."),
    USER_LOGIN_SUCCESS(HttpStatus.OK, "로그인 되었습니다."),
    USER_LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 되었습니다."),
    USER_INFO_SUCCESS(HttpStatus.OK, "유저 정보 조회 성공"),
    USER_INFO_UPDATE_SUCCESS(HttpStatus.OK, "회원 정보가 수정되었습니다."),
    USER_PASSWORD_CHANGE_SUCCESS(HttpStatus.OK, "비밀번호가 변경되었습니다."),
    USER_DELETE_SUCCESS(HttpStatus.OK, "회원 탈퇴가 완료되었습니다.");

    private final HttpStatus status;
    private final String message;
}
