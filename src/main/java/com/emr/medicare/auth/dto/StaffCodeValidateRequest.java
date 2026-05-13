package com.emr.medicare.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class StaffCodeValidateRequest {

    @NotBlank
    private String role;

    @NotBlank
    private String code;
}
