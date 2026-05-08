package com.emr.medicare.prescription.entity;

public enum PrescriptionStatus {
    PENDING,   // 처방 대기 (간호사 임시 작성 또는 의사 작성 후 승인 전)
    APPROVED,  // 의사 최종 승인 완료
    REJECTED   // 의사 반려 — 재작성 필요
}
