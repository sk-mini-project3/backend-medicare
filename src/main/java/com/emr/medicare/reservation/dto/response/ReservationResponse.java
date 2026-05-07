package com.emr.medicare.reservation.dto.response;

import com.emr.medicare.reservation.entity.Reservation;
import com.emr.medicare.reservation.entity.ReservationStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationResponse {

    private final Long reservationId;
    private final Long patientId;
    private final Long doctorId;
    private final LocalDateTime reservationDate;
    private final String symptoms;
    private final ReservationStatus status;

    public ReservationResponse(Reservation entity) {
        this.reservationId = entity.getReservationId();
        this.patientId = entity.getPatientId();
        this.doctorId = entity.getDoctorId();
        this.reservationDate = entity.getReservationDate();
        this.symptoms = entity.getSymptoms();
        this.status = entity.getStatus();
    }
}
