package com.emr.medicare.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nurse_verification_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NurseVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 병원 내부 간호사 인증코드
    @Column(unique = true)
    private String nurseCode;

    // 실제 간호사 이름
    private String ownerName;

    // 사용 여부
    private boolean used;
}