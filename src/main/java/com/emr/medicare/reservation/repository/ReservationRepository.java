package com.emr.medicare.reservation.repository;

import com.emr.medicare.reservation.entity.Reservation;
import com.emr.medicare.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPatientId(Long patientId);

    List<Reservation> findByDoctorId(Long doctorId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByDoctorIdAndReservationDateBetween(Long doctorId, LocalDateTime from, LocalDateTime to);

    // status, doctorId, 날짜 범위를 선택적으로 조합하는 동적 필터 쿼리
    // null 파라미터는 조건에서 자동 제외됨 (IS NULL OR ... 패턴)
    @Query("SELECT r FROM Reservation r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:doctorId IS NULL OR r.doctorId = :doctorId) AND " +
           "(:from IS NULL OR r.reservationDate >= :from) AND " +
           "(:to IS NULL OR r.reservationDate <= :to)")
    List<Reservation> findWithFilters(
            @Param("status") ReservationStatus status,
            @Param("doctorId") Long doctorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
