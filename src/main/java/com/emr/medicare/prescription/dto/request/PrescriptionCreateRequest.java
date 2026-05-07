package com.emr.medicare.prescription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PrescriptionCreateRequest {

    @NotNull
    private Long patientId;

    private Long doctorId;

    private Long reservationId;

    private Long nurseId;

    private String medication;

    private String dosage;
}
