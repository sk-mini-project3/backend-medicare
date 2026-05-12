package com.emr.medicare.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordResetConfirmRequest {

    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}