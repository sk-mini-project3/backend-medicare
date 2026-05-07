package com.emr.medicare.reservation.repository;

import com.emr.medicare.reservation.entity.Reservation;
import com.emr.medicare.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPatientId(Long patientId);

    List<Reservation> findByDoctorId(Long doctorId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByDoctorIdAndReservationDateBetween(Long doctorId, LocalDateTime from, LocalDateTime to);
}
