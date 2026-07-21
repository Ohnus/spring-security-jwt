package com.example.security_login.api.user;

import com.example.security_login.domain.user.dto.UserExistRequestDto;
import com.example.security_login.domain.user.dto.UserSignupRequestDto;
import com.example.security_login.domain.user.dto.UserUpdateRequestDto;
import com.example.security_login.domain.user.service.UserService;
import com.example.security_login.global.response.ResultCode;
import com.example.security_login.global.response.ResultResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 아이디 중복 검사
    @PostMapping(value = "/exist", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> existApi(@Valid @RequestBody UserExistRequestDto existDto) {
        return ResponseEntity.ok(userService.existsByUsername(existDto));
    }

    // 자체 서비스 회원가입
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResultResponse<?> signupApi(@Valid @RequestBody UserSignupRequestDto signupDto) {
        userService.signup(signupDto);
        return ResultResponse.success(ResultCode.USER_CREATE_SUCCESS);
    }

    // 자체 서비스 회원 정보 수정
    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResultResponse<?> updateApi(@Valid @RequestBody UserUpdateRequestDto updateDto) {
        userService.updateUser(updateDto);
        return ResultResponse.success(ResultCode.USER_INFO_UPDATE_SUCCESS);
    }

}
