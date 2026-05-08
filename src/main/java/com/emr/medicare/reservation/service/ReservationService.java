package com.emr.medicare.reservation.service;

import com.emr.medicare.common.exception.BaseException;
import com.emr.medicare.common.util.SecurityUtils;
import com.emr.medicare.reservation.dto.request.ReservationCreateRequest;
import com.emr.medicare.reservation.dto.request.ReservationStatusUpdateRequest;
import com.emr.medicare.reservation.dto.response.ReservationResponse;
import com.emr.medicare.reservation.entity.Reservation;
import com.emr.medicare.reservation.entity.ReservationStatus;
import com.emr.medicare.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationResponse create(ReservationCreateRequest request) {
        Reservation reservation = Reservation.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .reservationDate(request.getReservationDate())
                .symptoms(request.getSymptoms())
                // 신규 예약은 요청자 역할에 무관하게 항상 WAITING으로 시작
                // 이후 간호사가 NURSE_APPROVED, 의사가 COMPLETED로 변경
                .status(ReservationStatus.WAITING)
                .build();
        return new ReservationResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse getById(Long reservationId) {
        return new ReservationResponse(findOrThrow(reservationId));
    }

    public List<ReservationResponse> getAll(ReservationStatus status, Long doctorId, LocalDate date) {
        // 파라미터가 모두 null이면 전체 조회, 하나라도 있으면 필터 쿼리 사용
        LocalDateTime from = (date != null) ? date.atStartOfDay() : null;
        LocalDateTime to   = (date != null) ? date.atTime(23, 59, 59) : null;
        return reservationRepository.findWithFilters(status, doctorId, from, to).stream()
                .map(ReservationResponse::new)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getMyReservations() {
        Long patientId = SecurityUtils.getCurrentUserId();
        return reservationRepository.findByPatientId(patientId).stream()
                .map(ReservationResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponse updateStatus(Long reservationId, ReservationStatusUpdateRequest request) {
        Reservation reservation = findOrThrow(reservationId);
        validateStatusTransition(reservation.getStatus(), request.getStatus());
        reservation.changeStatus(request.getStatus());
        return new ReservationResponse(reservation);
    }

    // COMPLETED는 진료 완료 확정 상태 — 이후 되돌리면 처방전·진료기록과 불일치 발생
    private void validateStatusTransition(ReservationStatus current, ReservationStatus next) {
        if (current == ReservationStatus.COMPLETED) {
            throw new BaseException(HttpStatus.BAD_REQUEST, "완료된 예약의 상태는 변경할 수 없습니다.");
        }
    }

    private Reservation findOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."));
    }
}
