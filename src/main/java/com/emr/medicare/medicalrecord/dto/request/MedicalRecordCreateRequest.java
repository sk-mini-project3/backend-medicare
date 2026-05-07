package com.emr.medicare.medicalrecord.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MedicalRecordCreateRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;

    private Long reservationId;

    private String diagnosis;

    private String treatmentNotes;
}
