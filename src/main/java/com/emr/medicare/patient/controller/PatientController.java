package com.emr.medicare.patient.controller;

import com.emr.medicare.common.response.ApiResponse;
import com.emr.medicare.patient.dto.request.PatientDetailsCreateRequest;
import com.emr.medicare.patient.dto.request.PatientDetailsUpdateRequest;
import com.emr.medicare.patient.dto.response.MyProfileResponse;
import com.emr.medicare.patient.dto.response.PatientNurseLookupResponse;
import com.emr.medicare.patient.dto.response.PatientDetailsResponse;
import com.emr.medicare.patient.service.PatientDetailsService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientDetailsService patientDetailsService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(patientDetailsService.getMyProfile()));
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<PatientDetailsResponse>> create(
            @PathVariable Long userId,
            @Valid @RequestBody PatientDetailsCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(patientDetailsService.create(userId, request)));
    }

    @GetMapping("/{userId}/lookup")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<PatientNurseLookupResponse>> lookupForStaff(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(patientDetailsService.lookupForStaff(userId)));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<PatientDetailsResponse>> getById(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(patientDetailsService.getById(userId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<List<PatientDetailsResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(patientDetailsService.getAll()));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<PatientDetailsResponse>> update(
            @PathVariable Long userId,
            @Valid @RequestBody PatientDetailsUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(patientDetailsService.update(userId, request)));
    }
}
