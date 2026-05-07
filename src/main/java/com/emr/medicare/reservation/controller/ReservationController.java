package com.emr.medicare.reservation.controller;

import com.emr.medicare.common.response.ApiResponse;
import com.emr.medicare.reservation.dto.request.ReservationCreateRequest;
import com.emr.medicare.reservation.dto.request.ReservationStatusUpdateRequest;
import com.emr.medicare.reservation.dto.response.ReservationResponse;
import com.emr.medicare.reservation.service.ReservationService;
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
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT','NURSE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> create(
            @Valid @RequestBody ReservationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reservationService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','PATIENT')")
    public ResponseEntity<ApiResponse<ReservationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(reservationService.getAll()));
    }

    // A파트 JWT 완성 후: @RequestParam 제거하고 SecurityUtils.getCurrentUserId()로 교체
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMy(
            @RequestParam Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.getMyReservations(patientId)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('NURSE','DOCTOR')")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.updateStatus(id, request)));
    }
}
