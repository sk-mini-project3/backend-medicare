package com.emr.medicare.reservation.dto.request;

import com.emr.medicare.reservation.entity.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationStatusUpdateRequest {

    @NotNull
    private ReservationStatus status;
}
