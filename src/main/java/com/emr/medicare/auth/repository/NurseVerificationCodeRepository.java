package com.emr.medicare.auth.repository;

import com.emr.medicare.auth.entity.NurseVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NurseVerificationCodeRepository
        extends JpaRepository<NurseVerificationCode, Long> {

    Optional<NurseVerificationCode>
    findByNurseCode(String nurseCode);
}