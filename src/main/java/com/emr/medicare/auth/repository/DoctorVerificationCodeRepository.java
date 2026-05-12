package com.emr.medicare.auth.repository;

import com.emr.medicare.auth.entity.DoctorVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorVerificationCodeRepository
        extends JpaRepository<DoctorVerificationCode, Long> {

    Optional<DoctorVerificationCode>
    findByDoctorCode(String doctorCode);
}