package com.emr.medicare.patient.dto.response;

import com.emr.medicare.patient.entity.PatientDetails;
import com.emr.medicare.user.entity.User;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 간호사·의사 EMR 조회용: users 기본 정보 + patient_details(있을 때만)를 한 번에 반환합니다.
 */
@Getter
public class PatientNurseLookupResponse {

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
    /** patient_details 행 존재 여부 */
    private final boolean patientDetailsRegistered;

    private PatientNurseLookupResponse(
            Long userId,
            String name,
            String phone,
            String gender,
            LocalDate birthDate,
            String emergencyContact,
            String bloodType,
            String address,
            String insuranceInfo,
            String allergies,
            boolean patientDetailsRegistered
    ) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.gender = gender;
        this.birthDate = birthDate;
        this.emergencyContact = emergencyContact;
        this.bloodType = bloodType;
        this.address = address;
        this.insuranceInfo = insuranceInfo;
        this.allergies = allergies;
        this.patientDetailsRegistered = patientDetailsRegistered;
    }

    public static PatientNurseLookupResponse of(User user, PatientDetails details) {
        if (details != null) {
            return new PatientNurseLookupResponse(
                    user.getUserId(),
                    user.getName(),
                    user.getPhone(),
                    details.getGender(),
                    details.getBirthDate(),
                    details.getEmergencyContact(),
                    details.getBloodType(),
                    details.getAddress(),
                    details.getInsuranceInfo(),
                    details.getAllergies(),
                    true
            );
        }
        return new PatientNurseLookupResponse(
                user.getUserId(),
                user.getName(),
                user.getPhone(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }
}
