package com.bankfusion.corebanking.dto;

import jakarta.validation.constraints.NotBlank;
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
public class InterestRateChangeRqDTO {
    @NotBlank(message = "Account ID is Mandatory")
    private String accountID;

    @NotBlank(message = "Interest Rate Type is Mandatory")
    private Character interestRateType;

    @NotNull(message = "Interest Rate is Mandatory")
    @PositiveOrZero
    private BigDecimal interestRate;
}
