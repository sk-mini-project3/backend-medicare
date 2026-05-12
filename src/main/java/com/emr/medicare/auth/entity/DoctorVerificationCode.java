package com.emr.medicare.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "doctor_verification_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 병원 내부 의사 인증코드
    @Column(unique = true)
    private String doctorCode;

    // 실제 의사 이름
    private String ownerName;

    // 사용 여부
    private boolean used;
}