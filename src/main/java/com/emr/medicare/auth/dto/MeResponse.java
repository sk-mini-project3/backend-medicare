package com.emr.medicare.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 로그인 사용자 프로필 (DB users 기준) */
@Getter
@AllArgsConstructor
public class MeResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String role;
    private final String phone;
}
