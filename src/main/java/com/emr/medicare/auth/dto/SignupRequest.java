package com.emr.medicare.auth.dto;

import com.emr.medicare.user.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotNull(message = "회원 유형은 필수입니다.")
    private Role role;

    // DOCTOR/NURSE만 필요
    private String verificationCode;

}