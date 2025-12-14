package com.fincorex.corebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DelinquencyStageRqDTO {
    @NotBlank(message = "Stage Description is Mandatory")
    private String stageDescription;

    @NotNull(message = "Over Due Days is Mandatory")
    @PositiveOrZero(message = "Over Due Days should be Positive or Zero")
    private Long overDueDays;

    @NotNull(message = "Delinquency Profile ID is Mandatory")
    private Long delinquencyProfileID;
}
