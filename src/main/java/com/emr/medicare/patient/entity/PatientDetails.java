package com.emr.medicare.patient.entity;

import com.emr.medicare.common.util.EncryptionConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "patient_details")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDetails {

    // @GeneratedValue 없음 — user_id는 Users 테이블의 PK를 그대로 사용하는 FK (1:1 확장 구조)
    @Id
    @Column(name = "user_id")
    private Long userId;

    // 개인정보보호법상 주민등록번호는 암호화 저장 필수 → AES-256/CBC 적용
    @Convert(converter = EncryptionConverter.class)
    @Column(name = "resident_number")
    private String residentNumber;

    @Column(length = 10)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;

    @Column(name = "blood_type", length = 10)
    private String bloodType;

    @Column(length = 200)
    private String address;

    @Column(name = "insurance_info", length = 100)
    private String insuranceInfo;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    public void update(String gender, LocalDate birthDate, String emergencyContact,
                       String bloodType, String address, String insuranceInfo, String allergies) {
        this.gender = gender;
        this.birthDate = birthDate;
        this.emergencyContact = emergencyContact;
        this.bloodType = bloodType;
        this.address = address;
        this.insuranceInfo = insuranceInfo;
        this.allergies = allergies;
    }
}
