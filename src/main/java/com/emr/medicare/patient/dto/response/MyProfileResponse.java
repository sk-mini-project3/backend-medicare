package com.emr.medicare.patient.dto.response;

import com.emr.medicare.patient.entity.PatientDetails;
import com.emr.medicare.user.entity.User;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MyProfileResponse {

    private final String name;
    private final String email;
    private final String phone;
    private final String gender;
    private final LocalDate birthDate;
    private final String bloodType;
    private final String insuranceInfo;
    private final String allergies;

    public MyProfileResponse(User user, PatientDetails details) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.gender = details != null ? details.getGender() : null;
        this.birthDate = details != null ? details.getBirthDate() : null;
        this.bloodType = details != null ? details.getBloodType() : null;
        this.insuranceInfo = details != null ? details.getInsuranceInfo() : null;
        this.allergies = details != null ? details.getAllergies() : null;
    }
}
