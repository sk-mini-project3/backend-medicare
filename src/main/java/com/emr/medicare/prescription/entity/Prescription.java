package com.emr.medicare.prescription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Long prescriptionId;

    @Column(name = "reservation_id")
    private Long reservationId;          // FK → Reservations.reservation_id

    @Column(name = "patient_id", nullable = false)
    private Long patientId;              // FK → Users.user_id

    @Column(name = "doctor_id")
    private Long doctorId;               // FK → Users.user_id (처방 작성 의사)

    @Column(length = 200)
    private String medication;           // 약품명

    @Column(length = 200)
    private String dosage;               // 용량 및 복약 지도

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // SHA-256(prescriptionId + patientId + medication + dosage + createdAt)
    // 처방전 위변조 감지용 — 저장 후 서비스에서 setHash()로 채움
    @Column(length = 64)
    private String hash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionStatus status;

    @Column(name = "nurse_id")
    private Long nurseId;                // FK → Users.user_id (임시 처방 작성 간호사)

    @Column(name = "approved_by")
    private Long approvedBy;             // FK → Users.user_id (최종 승인 의사)

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    // PENDING → APPROVED 단방향 전이만 허용 (승인 취소 불가)
    public void approve(Long doctorId) {
        this.status = PrescriptionStatus.APPROVED;
        this.approvedBy = doctorId;
    }
}
