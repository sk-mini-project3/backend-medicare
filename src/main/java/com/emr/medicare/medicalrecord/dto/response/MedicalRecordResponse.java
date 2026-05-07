package com.emr.medicare.medicalrecord.dto.response;

import com.emr.medicare.medicalrecord.entity.MedicalRecord;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MedicalRecordResponse {

    private final Long recordId;
    private final Long reservationId;
    private final Long patientId;
    private final Long doctorId;
    private final String diagnosis;
    private final String treatmentNotes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public MedicalRecordResponse(MedicalRecord entity) {
        this.recordId = entity.getRecordId();
        this.reservationId = entity.getReservationId();
        this.patientId = entity.getPatientId();
        this.doctorId = entity.getDoctorId();
        this.diagnosis = entity.getDiagnosis();
        this.treatmentNotes = entity.getTreatmentNotes();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }
}
