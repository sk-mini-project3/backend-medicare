package com.emr.medicare.prescription.dto.response;

import com.emr.medicare.prescription.entity.Prescription;
import com.emr.medicare.prescription.entity.PrescriptionStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PrescriptionResponse {

    private final Long prescriptionId;
    private final Long reservationId;
    private final Long patientId;
    private final Long doctorId;
    private final String medication;
    private final String dosage;
    private final LocalDateTime createdAt;
    private final String hash;
    private final PrescriptionStatus status;
    private final Long nurseId;
    private final Long approvedBy;

    public PrescriptionResponse(Prescription entity) {
        this.prescriptionId = entity.getPrescriptionId();
        this.reservationId = entity.getReservationId();
        this.patientId = entity.getPatientId();
        this.doctorId = entity.getDoctorId();
        this.medication = entity.getMedication();
        this.dosage = entity.getDosage();
        this.createdAt = entity.getCreatedAt();
        this.hash = entity.getHash();
        this.status = entity.getStatus();
        this.nurseId = entity.getNurseId();
        this.approvedBy = entity.getApprovedBy();
    }
}
