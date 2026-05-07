package com.emr.medicare.medicalrecord.controller;

import com.emr.medicare.common.response.ApiResponse;
import com.emr.medicare.medicalrecord.dto.request.MedicalRecordCreateRequest;
import com.emr.medicare.medicalrecord.dto.request.MedicalRecordUpdateRequest;
import com.emr.medicare.medicalrecord.dto.response.MedicalRecordResponse;
import com.emr.medicare.medicalrecord.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> create(
            @Valid @RequestBody MedicalRecordCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(medicalRecordService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(medicalRecordService.getById(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getByPatientId(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(medicalRecordService.getByPatientId(patientId)));
    }

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getByReservationId(
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(ApiResponse.success(medicalRecordService.getByReservationId(reservationId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> update(
            @PathVariable Long id,
            @RequestBody MedicalRecordUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(medicalRecordService.update(id, request)));
    }

    // A파트 JWT 완성 후: @RequestParam 제거하고 SecurityUtils.getCurrentUserId()로 교체
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getMy(
            @RequestParam Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(medicalRecordService.getMyRecords(patientId)));
    }
}
