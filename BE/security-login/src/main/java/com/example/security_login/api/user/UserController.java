package com.example.security_login.api.user;

import com.example.security_login.domain.user.dto.request.UserDeleteRequestDto;
import com.example.security_login.domain.user.dto.request.UserExistRequestDto;
import com.example.security_login.domain.user.dto.request.UserSignupRequestDto;
import com.example.security_login.domain.user.dto.request.UserUpdateRequestDto;
import com.example.security_login.domain.user.service.UserService;
import com.example.security_login.global.response.ResultCode;
import com.example.security_login.global.response.ResultResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 아이디 중복 검사
    @GetMapping(value = "/check-username", consumes = MediaType.APPLICATION_JSON_VALUE)
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
    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResultResponse<?> updateApi(@Valid @RequestBody UserUpdateRequestDto updateDto) {
        userService.updateUser(updateDto);
        return ResultResponse.success(ResultCode.USER_INFO_UPDATE_SUCCESS);
    }

    // 자체, 소셜 회원 정보 조회
    @GetMapping(value = "/me")
    public ResultResponse<?> readApi() {
        return ResultResponse.of(ResultCode.USER_INFO_SUCCESS, userService.readUser());
    }

    // 자체, 소셜 회원 탈퇴
    @DeleteMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResultResponse<?> deleteApi(@Valid @RequestBody UserDeleteRequestDto deleteDto) {
        userService.deleteUser(deleteDto);
        return ResultResponse.success(ResultCode.USER_DELETE_SUCCESS);
    }

}
