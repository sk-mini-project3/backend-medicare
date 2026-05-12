package com.emr.medicare.auth.repository;

import com.emr.medicare.auth.entity.NurseVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NurseVerificationCodeRepository
        extends JpaRepository<NurseVerificationCode, Long> {

    Optional<NurseVerificationCode>
    findByNurseCode(String nurseCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NurseVerificationCode n SET n.ownerName = :owner WHERE n.nurseCode = :code")
    int updateOwnerByNurseCode(@Param("code") String code, @Param("owner") String owner);
}