package com.emr.medicare.nurse.repository;

import com.emr.medicare.nurse.entity.NurseDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NurseDetailRepository
        extends JpaRepository<NurseDetail, Long> {
}