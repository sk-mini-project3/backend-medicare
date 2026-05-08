package com.emr.medicare.prescription.repository;

import com.emr.medicare.prescription.entity.Prescription;
import com.emr.medicare.prescription.entity.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientId(Long patientId);

    // status, nurseId, doctorId를 선택적으로 조합하는 동적 필터 쿼리
    // null 파라미터는 조건에서 자동 제외됨
    @Query("SELECT p FROM Prescription p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:nurseId IS NULL OR p.nurseId = :nurseId) AND " +
           "(:doctorId IS NULL OR p.doctorId = :doctorId)")
    List<Prescription> findWithFilters(
            @Param("status") PrescriptionStatus status,
            @Param("nurseId") Long nurseId,
            @Param("doctorId") Long doctorId
    );
}
