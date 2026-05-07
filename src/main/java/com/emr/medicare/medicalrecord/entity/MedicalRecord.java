package com.emr.medicare.medicalrecord.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "reservation_id")
    private Long reservationId;          // FK → Reservations.reservation_id

    @Column(name = "patient_id", nullable = false)
    private Long patientId;              // FK → Users.user_id

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;               // FK → Users.user_id

    @Column(length = 200)
    private String diagnosis;            // 최종 진단명

    // 의사 소견 및 처치 상세 — TEXT로 길이 제한 없이 저장
    @Column(name = "treatment_notes", columnDefinition = "TEXT")
    private String treatmentNotes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void update(String diagnosis, String treatmentNotes) {
        this.diagnosis = diagnosis;
        this.treatmentNotes = treatmentNotes;
    }
}
