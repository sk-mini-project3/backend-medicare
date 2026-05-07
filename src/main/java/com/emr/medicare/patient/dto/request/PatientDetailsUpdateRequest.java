package com.emr.medicare.patient.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PatientDetailsUpdateRequest {

    private String gender;
    private LocalDate birthDate;
    private String emergencyContact;
    private String bloodType;
    private String address;
    private String insuranceInfo;
    private String allergies;
}
