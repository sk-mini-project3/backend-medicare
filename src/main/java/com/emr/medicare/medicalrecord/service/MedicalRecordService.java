package com.emr.medicare.medicalrecord.service;

import com.emr.medicare.common.exception.BaseException;
import com.emr.medicare.common.util.SecurityUtils;
import com.emr.medicare.medicalrecord.dto.request.MedicalRecordCreateRequest;
import com.emr.medicare.medicalrecord.dto.request.MedicalRecordUpdateRequest;
import com.emr.medicare.medicalrecord.dto.response.MedicalRecordResponse;
import com.emr.medicare.medicalrecord.entity.MedicalRecord;
import com.emr.medicare.medicalrecord.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    @Transactional
    public MedicalRecordResponse create(MedicalRecordCreateRequest request) {
        MedicalRecord record = MedicalRecord.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .reservationId(request.getReservationId())
                .diagnosis(sanitize(request.getDiagnosis()))
                .treatmentNotes(sanitize(request.getTreatmentNotes()))
                .build();
        return new MedicalRecordResponse(medicalRecordRepository.save(record));
    }

    public MedicalRecordResponse getById(Long recordId) {
        return new MedicalRecordResponse(findOrThrow(recordId));
    }

    public List<MedicalRecordResponse> getByPatientId(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId).stream()
                .map(MedicalRecordResponse::new)
                .collect(Collectors.toList());
    }

    public MedicalRecordResponse getByReservationId(Long reservationId) {
        return medicalRecordRepository.findByReservationId(reservationId)
                .map(MedicalRecordResponse::new)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND, "해당 예약의 진료기록을 찾을 수 없습니다."));
    }

    public List<MedicalRecordResponse> getMyRecords() {
        Long patientId = SecurityUtils.getCurrentUserId();
        return medicalRecordRepository.findByPatientId(patientId).stream()
                .map(MedicalRecordResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalRecordResponse update(Long recordId, MedicalRecordUpdateRequest request) {
        MedicalRecord record = findOrThrow(recordId);
        // JPA dirty checking — @PreUpdate가 updatedAt을 자동 갱신하므로 save() 불필요
        record.update(sanitize(request.getDiagnosis()), sanitize(request.getTreatmentNotes()));
        return new MedicalRecordResponse(record);
    }

    private MedicalRecord findOrThrow(Long recordId) {
        return medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND, "진료기록을 찾을 수 없습니다."));
    }

    private String sanitize(String input) {
        if (input == null) return null;
        return Jsoup.clean(input, Safelist.none());
    }
}
