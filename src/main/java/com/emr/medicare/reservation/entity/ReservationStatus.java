package com.emr.medicare.reservation.entity;

public enum ReservationStatus {
    WAITING,        // 대기중
    NURSE_APPROVED, // 간호사 승인 완료
    COMPLETED       // 진료 완료
}
