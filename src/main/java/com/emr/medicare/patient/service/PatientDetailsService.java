package com.emr.medicare.patient.service;

import com.emr.medicare.common.exception.BaseException;
import com.emr.medicare.common.util.SecurityUtils;
import com.emr.medicare.patient.dto.request.PatientDetailsCreateRequest;
import com.emr.medicare.patient.dto.request.PatientDetailsUpdateRequest;
import com.emr.medicare.patient.dto.response.MyProfileResponse;
import com.emr.medicare.patient.dto.response.PatientNurseLookupResponse;
import com.emr.medicare.patient.dto.response.PatientDetailsResponse;
import com.emr.medicare.patient.entity.PatientDetails;
import com.emr.medicare.patient.repository.PatientDetailsRepository;
import com.emr.medicare.user.entity.Role;
import com.emr.medicare.user.entity.User;
import com.emr.medicare.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// 클래스 레벨 readOnly=true: 조회 메서드에서 JPA 스냅샷 저장을 생략해 성능 최적화
// 쓰기 메서드에는 @Transactional을 별도 선언해 readOnly를 override
@Transactional(readOnly = true)
public class PatientDetailsService {

    private final PatientDetailsRepository patientDetailsRepository;
    private final UserRepository userRepository;

    @Transactional
    public PatientDetailsResponse create(Long userId, PatientDetailsCreateRequest request) {
        if (patientDetailsRepository.existsById(userId)) {
            throw new BaseException(HttpStatus.CONFLICT, "이미 등록된 환자입니다.");
        }
        PatientDetails patient = PatientDetails.builder()
                .userId(userId)
                .residentNumber(request.getResidentNumber())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .emergencyContact(request.getEmergencyContact())
                .bloodType(request.getBloodType())
                .address(request.getAddress())
                .insuranceInfo(request.getInsuranceInfo())
                .allergies(request.getAllergies())
                .build();
        return toResponse(patientDetailsRepository.save(patient));
    }

    public PatientDetailsResponse getById(Long userId) {
        return toResponse(findOrThrow(userId));
    }

    /**
     * EMR 화면용: 계정(users) 정보는 항상 포함하고, patient_details가 있으면 상세 필드를 채웁니다.
     */
    public PatientNurseLookupResponse lookupForStaff(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        if (user.getRole() != Role.PATIENT) {
            throw new BaseException(HttpStatus.BAD_REQUEST, "환자만 조회할 수 있습니다.");
        }
        PatientDetails details = patientDetailsRepository.findById(userId).orElse(null);
        return PatientNurseLookupResponse.of(user, details);
    }

    public List<PatientDetailsResponse> getAll() {
        return patientDetailsRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MyProfileResponse getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        PatientDetails details = patientDetailsRepository.findById(userId).orElse(null);
        return new MyProfileResponse(user, details);
    }

    @Transactional
    public PatientDetailsResponse update(Long userId, PatientDetailsUpdateRequest request) {
        PatientDetails patient = findOrThrow(userId);
        patient.update(
                request.getGender(),
                request.getBirthDate(),
                request.getEmergencyContact(),
                request.getBloodType(),
                request.getAddress(),
                request.getInsuranceInfo(),
                request.getAllergies()
        );
        // 명시적 save() 없음 — 트랜잭션 종료 시 JPA dirty checking이 변경분을 자동 UPDATE
        return toResponse(patient);
    }

    private PatientDetailsResponse toResponse(PatientDetails patient) {
        return userRepository.findById(patient.getUserId())
                .map(u -> new PatientDetailsResponse(patient, u.getName(), u.getPhone()))
                .orElseGet(() -> new PatientDetailsResponse(patient, "", ""));
    }

    private PatientDetails findOrThrow(Long userId) {
        return patientDetailsRepository.findById(userId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND, "환자를 찾을 수 없습니다."));
    }
}
