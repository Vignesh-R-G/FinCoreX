package com.fincorex.corebanking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanScheduleRqDTO {
    @NotNull(message = "Interest Due is Mandatory")
    @PositiveOrZero(message = "Interest Due should be Positive or Zero")
    private BigDecimal interestDue;

    @NotNull(message = "Principal Due is Mandatory")
    @PositiveOrZero(message = "Principal Due should be Positive or Zero")
    private BigDecimal principalDue;
}
