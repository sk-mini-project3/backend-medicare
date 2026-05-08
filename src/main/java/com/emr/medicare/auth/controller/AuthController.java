package com.emr.medicare.auth.controller;

import com.emr.medicare.auth.dto.*;
import com.emr.medicare.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request
    ) {

        authService.signup(request);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "회원가입 성공"
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {

        return ResponseEntity.ok(
                authService.reissue(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
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
                Map.of(
                        "message",
                        "로그아웃 성공"
                )
        );
    }
}