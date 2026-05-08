package com.emr.medicare.verification.service;

import com.emr.medicare.user.entity.Role;
import com.emr.medicare.user.repository.UserRepository;
import com.emr.medicare.verification.config.VerificationCodeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeProperties verificationCodeProperties;
    private final UserRepository userRepository;

    public void validateSignupCode(Role role, String verificationCode) {
        if (role == Role.PATIENT) {
            return;
        }

        if (verificationCode == null || verificationCode.isBlank()) {
            throw new IllegalArgumentException("의사/간호사는 인증코드가 필요합니다.");
        }

        if (role == Role.DOCTOR) {
            validateDoctorCode(verificationCode);
            return;
        }

        if (role == Role.NURSE) {
            validateNurseCode(verificationCode);
            return;
        }

        throw new IllegalArgumentException("지원하지 않는 역할입니다.");
    }

    private void validateDoctorCode(String verificationCode) {
        if (!verificationCodeProperties.getDoctorCodes().contains(verificationCode)) {
            throw new IllegalArgumentException("유효하지 않은 의사 인증코드입니다.");
        }

        checkAlreadyUsed(verificationCode);
    }

    private void validateNurseCode(String verificationCode) {
        if (!verificationCodeProperties.getNurseCodes().contains(verificationCode)) {
            throw new IllegalArgumentException("유효하지 않은 간호사 인증코드입니다.");
        }

        checkAlreadyUsed(verificationCode);
    }

    private void checkAlreadyUsed(String verificationCode) {
        if (userRepository.existsByVerificationCode(verificationCode)) {
            throw new IllegalArgumentException("이미 사용된 인증코드입니다.");
        }
    }
}