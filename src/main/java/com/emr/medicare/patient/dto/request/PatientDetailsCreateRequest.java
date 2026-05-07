package com.emr.medicare.patient.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PatientDetailsCreateRequest {

    @NotBlank
    private String residentNumber;

    @NotBlank
    private String gender;

    @NotNull
    private LocalDate birthDate;

    private String emergencyContact;
    private String bloodType;
    private String address;
    private String insuranceInfo;
    private String allergies;
}
