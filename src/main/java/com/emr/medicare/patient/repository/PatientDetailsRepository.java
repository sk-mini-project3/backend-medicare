package com.emr.medicare.patient.repository;

import com.emr.medicare.patient.entity.PatientDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientDetailsRepository extends JpaRepository<PatientDetails, Long> {
}
