package com.emr.medicare.patient.service;

import com.emr.medicare.common.exception.BaseException;
import com.emr.medicare.patient.dto.request.PatientDetailsCreateRequest;
import com.emr.medicare.patient.dto.request.PatientDetailsUpdateRequest;
import com.emr.medicare.patient.dto.response.PatientDetailsResponse;
import com.emr.medicare.patient.entity.PatientDetails;
import com.emr.medicare.patient.repository.PatientDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientDetailsService {

    private final PatientDetailsRepository patientDetailsRepository;

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
        return new PatientDetailsResponse(patientDetailsRepository.save(patient));
    }

    public PatientDetailsResponse getById(Long userId) {
        return new PatientDetailsResponse(findOrThrow(userId));
    }

    public List<PatientDetailsResponse> getAll() {
        return patientDetailsRepository.findAll().stream()
                .map(PatientDetailsResponse::new)
                .collect(Collectors.toList());
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
        return new PatientDetailsResponse(patient);
    }

    private PatientDetails findOrThrow(Long userId) {
        return patientDetailsRepository.findById(userId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND, "환자를 찾을 수 없습니다."));
    }
}
