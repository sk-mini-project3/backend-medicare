package com.emr.medicare.doctor.repository;

import com.emr.medicare.doctor.entity.DoctorDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorDetailRepository
        extends JpaRepository<DoctorDetail, Long> {
}