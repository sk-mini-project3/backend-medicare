package com.emr.medicare.medicalrecord.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MedicalRecordUpdateRequest {

    private String diagnosis;

    private String treatmentNotes;
}
