package com.emr.medicare.auth.controller;

import com.emr.medicare.auth.dto.*;
import com.emr.medicare.auth.service.AuthService;
import com.emr.medicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequest request
    ) {

        authService.signup(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "회원가입 성공",
                        null
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "로그인 성공",
                        authService.login(request)
                )
        );
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "토큰 재발급 성공",
                        authService.reissue(request)
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization")
            String authorizationHeader
    ) {

        String accessToken =
                authorizationHeader.replace(
                        "Bearer ",
                        ""
                );

        authService.logout(accessToken);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "로그아웃 성공",
                        null
                )
        );
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(
            @RequestBody PasswordResetRequest request
    ) {

        String token =
                authService.createPasswordResetToken(
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "비밀번호 재설정 토큰 생성 완료",
                        token
                )
        );
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @RequestBody PasswordResetConfirmRequest request
    ) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "비밀번호 변경 완료",
                        null
                )
        );
    }
}