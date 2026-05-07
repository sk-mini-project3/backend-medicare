package com.emr.medicare.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;     // FK → Users.user_id (환자)

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;      // FK → Users.user_id (의사)

    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    // 상태 변경은 changeStatus()로만 허용 — setter 노출 시 서비스 레이어 우회 가능성 차단
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    public void changeStatus(ReservationStatus status) {
        this.status = status;
    }
}
