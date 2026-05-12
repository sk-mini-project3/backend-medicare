package com.emr.medicare.patient.dto.response;

import com.emr.medicare.patient.entity.PatientDetails;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class PatientDetailsResponse {

    private final Long userId;
    private final String name;
    private final String phone;
    private final String gender;
    private final LocalDate birthDate;
    private final String emergencyContact;
    private final String bloodType;
    private final String address;
    private final String insuranceInfo;
    private final String allergies;

    public PatientDetailsResponse(PatientDetails entity, String name, String phone) {
        this.userId = entity.getUserId();
        this.name = name != null ? name : "";
        this.phone = phone != null ? phone : "";
        this.gender = entity.getGender();
        this.birthDate = entity.getBirthDate();
        this.emergencyContact = entity.getEmergencyContact();
        this.bloodType = entity.getBloodType();
        this.address = entity.getAddress();
        this.insuranceInfo = entity.getInsuranceInfo();
        this.allergies = entity.getAllergies();
    }
}
