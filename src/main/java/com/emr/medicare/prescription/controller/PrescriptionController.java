package com.emr.medicare.prescription.controller;

import com.emr.medicare.common.response.ApiResponse;
import com.emr.medicare.common.util.SecurityUtils;
import com.emr.medicare.prescription.dto.request.PrescriptionCreateRequest;
import com.emr.medicare.prescription.dto.response.PrescriptionResponse;
import com.emr.medicare.prescription.entity.PrescriptionStatus;
import com.emr.medicare.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(
            @Valid @RequestBody PrescriptionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(prescriptionService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getAll(
            @RequestParam(required = false) PrescriptionStatus status,
            @RequestParam(required = false) Long nurseId,
            @RequestParam(required = false) Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getAll(status, nurseId, doctorId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','PATIENT')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getById(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getByPatientId(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getByPatientId(patientId)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getMy() {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getMyPrescriptions()));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> approve(@PathVariable Long id) {
        Long doctorId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.approve(id, doctorId)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.reject(id)));
    }

    @GetMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> verify(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.verify(id)));
    }
}