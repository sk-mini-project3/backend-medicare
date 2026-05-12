package com.emr.medicare.reservation.service;

import com.emr.medicare.common.exception.BaseException;
import com.emr.medicare.common.util.SecurityUtils;
import com.emr.medicare.reservation.dto.request.ReservationCreateRequest;
import com.emr.medicare.reservation.dto.request.ReservationStatusUpdateRequest;
import com.emr.medicare.reservation.dto.response.ReservationResponse;
import com.emr.medicare.reservation.entity.Reservation;
import com.emr.medicare.reservation.entity.ReservationStatus;
import com.emr.medicare.reservation.repository.ReservationRepository;
import com.emr.medicare.user.entity.User;
import com.emr.medicare.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReservationResponse create(ReservationCreateRequest request) {
        Reservation reservation = Reservation.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .reservationDate(request.getReservationDate())
                .symptoms(sanitize(request.getSymptoms()))
                // 신규 예약은 요청자 역할에 무관하게 항상 WAITING으로 시작
                // 이후 간호사가 NURSE_APPROVED, 의사가 COMPLETED로 변경
                .status(ReservationStatus.WAITING)
                .build();
        return toResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse getById(Long reservationId) {
        return toResponse(findOrThrow(reservationId));
    }

    public List<ReservationResponse> getAll(ReservationStatus status, Long doctorId, LocalDate date) {
        LocalDateTime from = (date != null) ? date.atStartOfDay() : null;
        LocalDateTime to = (date != null) ? date.atTime(23, 59, 59) : null;
        List<Reservation> list = reservationRepository.findWithFilters(status, doctorId, from, to);
        return mapWithPatientNames(list);
    }

    public List<ReservationResponse> getByPatientId(Long patientId) {
        List<Reservation> list = reservationRepository.findByPatientId(patientId);
        return mapWithPatientNames(list);
    }

    public List<ReservationResponse> getMyReservations() {
        Long patientId = SecurityUtils.getCurrentUserId();
        List<Reservation> list = reservationRepository.findByPatientId(patientId);
        return mapWithPatientNames(list);
    }

    @Transactional
    public ReservationResponse updateStatus(Long reservationId, ReservationStatusUpdateRequest request) {
        Reservation reservation = findOrThrow(reservationId);
        validateStatusTransition(reservation.getStatus(), request.getStatus());
        reservation.changeStatus(request.getStatus());
        return toResponse(reservation);
    }

    private List<ReservationResponse> mapWithPatientNames(List<Reservation> list) {
        Set<Long> ids = list.stream()
                .map(Reservation::getPatientId)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Long, String> names = loadPatientNames(ids);
        return list.stream()
                .map(r -> new ReservationResponse(r, names.get(r.getPatientId())))
                .collect(Collectors.toList());
    }

    private Map<Long, String> loadPatientNames(Set<Long> patientIds) {
        if (patientIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(patientIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getName, (a, b) -> a));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        String name = userRepository.findById(reservation.getPatientId())
                .map(User::getName)
                .orElse(null);
        return new ReservationResponse(reservation, name);
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

    private String sanitize(String input) {
        if (input == null) return null;
        return Jsoup.clean(input, Safelist.none());
    }
}
