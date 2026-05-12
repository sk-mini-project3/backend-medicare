package com.emr.medicare.auth.repository;

import com.emr.medicare.auth.entity.DoctorVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoctorVerificationCodeRepository
        extends JpaRepository<DoctorVerificationCode, Long> {

    Optional<DoctorVerificationCode>
    findByDoctorCode(String doctorCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE DoctorVerificationCode d SET d.ownerName = :owner WHERE d.doctorCode = :code")
    int updateOwnerByDoctorCode(@Param("code") String code, @Param("owner") String owner);
}