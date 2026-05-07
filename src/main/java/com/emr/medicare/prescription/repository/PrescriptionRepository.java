package com.emr.medicare.prescription.repository;

import com.emr.medicare.prescription.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientId(Long patientId);
}
